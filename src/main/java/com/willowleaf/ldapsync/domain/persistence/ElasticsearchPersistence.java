package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.domain.model.Department;
import com.willowleaf.ldapsync.domain.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Slf4j
@Component
public class ElasticsearchPersistence extends Persistence {

    // Elasticsearch 6.X一个index只能有一个type，7+以后会取消type，所有当前版本使用固定的type值
    private static final String TYPE = "doc";

    @Override
    public void save(@Nonnull Department department) {
        try {
            saveDepartment(department);

            try {
                saveEmployees(department.getEmployees());
            } catch (IOException e) {
                log.error("Elasticsearch保存员工信息失败", e);
                client.delete(new DeleteRequest(getDeptIndex(department.getDataSource()), TYPE, department.getNumber()), DEFAULT);
                throw e;
            }
        } catch (IOException e) {
            log.error("Elasticsearch保存部门信息失败", e);
            throw new RuntimeException(e);
        }
    }

    private void saveDepartment(Department department) throws IOException {
        client.index(buildRequest(getDeptIndex(department.getDataSource()), department.getNumber(), department), DEFAULT);
    }

    private void saveEmployees(List<Employee> employees) throws IOException {
        if (employees.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        String empIndex = getEmpIndex(employees.get(0).getDataSource());
        for (Employee employee : employees) {   // add方法是线程不安全的，不能使用并发流
            bulkRequest.add(buildRequest(empIndex, employee.getUid(), employee));
        }
        client.bulk(bulkRequest, DEFAULT);
    }

    private IndexRequest buildRequest(String index, String id, Object model) throws IOException {
        XContentBuilder document = buildDocument(model);

        return new IndexRequest(index, TYPE, id)
                .source(document);
    }

    private XContentBuilder buildDocument(Object object) throws IOException {
        XContentBuilder builder = jsonBuilder().startObject();

        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (needful(field)) {
                field.setAccessible(true);
                try {
                    builder.field(field.getName(), field.get(object));
                } catch (IllegalAccessException ignored) {
                    // It won't happen.
                }
            }
        }

        builder.endObject();
        return builder;
    }

    /**
     * 返回字段是否需要持久化。
     *
     * @param field 数据对象的字段
     * @return 需要持久化字段的类型
     */
    private boolean needful(Field field) {
        Class<?> type = field.getType();
        return type == String.class
                || type == Integer.class
                || type == Long.class;
    }
}
