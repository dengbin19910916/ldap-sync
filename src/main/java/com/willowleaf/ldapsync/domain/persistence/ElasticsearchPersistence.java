package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Employee;
import com.willowleaf.ldapsync.domain.Organization;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestHighLevelClient;
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
public class ElasticsearchPersistence implements Organization.Persistence {

    // Elasticsearch 6.X一个index只能有一个type，7+以后会取消type，所有当前版本使用固定的type值
    private static final String TYPE = "doc";

    private static final String INDEX_PREFIX_DEPT = "dept";
    private static final String INDEX_PREFIX_EMP = "emp";

    private final RestHighLevelClient client;

    public ElasticsearchPersistence(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public void save(@Nonnull final Department department) {
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
        client.update(buildRequest(getDeptIndex(department.getDataSource()), department.getNumber(), department), DEFAULT);
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

    private static String getIndex(String indexPrefix, DataSource dataSource) {
        return indexPrefix + "_" + dataSource.getId();
    }

    /**
     * index的格式为dept_{dataSourceId}。
     */
    private static String getDeptIndex(@Nonnull DataSource dataSource) {
        return getIndex(INDEX_PREFIX_DEPT, dataSource);
    }

    /**
     * index的格式为emp_{dataSourceId}。
     */
    private static String getEmpIndex(@Nonnull DataSource dataSource) {
        return getIndex(INDEX_PREFIX_EMP, dataSource);
    }

    private UpdateRequest buildRequest(String index, String id, Object model) throws IOException {
        XContentBuilder document = buildDocument(model);

        IndexRequest indexRequest = new IndexRequest(index, TYPE, id).source(document);
        return new UpdateRequest(index, TYPE, id)
                .doc(document)
                .upsert(indexRequest);  // upsert: insert or update

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
