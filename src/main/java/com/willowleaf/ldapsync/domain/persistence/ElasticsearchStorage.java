package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.annotation.Ignore;
import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Employee;
import com.willowleaf.ldapsync.domain.Organization;
import lombok.SneakyThrows;
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
public class ElasticsearchStorage implements Organization.Storage {

    private String deptIndex;
    private String empIndex;

    private final RestHighLevelClient client;

    public ElasticsearchStorage(RestHighLevelClient client) {
        this.client = client;
    }

    @SneakyThrows
    @Override
    public void save(@Nonnull final Department department) {
        saveDepartment(department);
        saveEmployees(department.getEmployees());
    }

    @SneakyThrows
    @Override
    public void remove(@Nonnull Department department, Exception e) {
        client.delete(new DeleteRequest(getDeptIndex(department.getDataSource())).id(department.getNumber()), DEFAULT);
        BulkRequest bulkRequest = new BulkRequest();
        for (Employee employee : department.getEmployees()) {
            DeleteRequest deleteRequest = new DeleteRequest(getEmpIndex(employee.getDataSource()))
                    .id(employee.getUid());
            bulkRequest.add(deleteRequest);
        }
        client.bulk(bulkRequest, DEFAULT);
    }

    private void saveDepartment(Department department) throws IOException {
        client.update(buildRequest(getDeptIndex(department.getDataSource()),
                department.getNumber(), department), DEFAULT);
    }

    private void saveEmployees(@Nonnull List<Employee> employees) throws IOException {
        if (employees.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        String empIndex = getEmpIndex(employees.get(0).getDataSource());
        for (Employee employee : employees) {
            bulkRequest.add(buildRequest(empIndex, employee.getUid(), employee));   // add方法是线程不安全的
        }
        client.bulk(bulkRequest, DEFAULT);
    }

    private String getDeptIndex(@Nonnull DataSource dataSource) {
        if (deptIndex == null) {
            deptIndex = getIndex("dept", dataSource);
        }
        return deptIndex;
    }

    private String getEmpIndex(@Nonnull DataSource dataSource) {
        if (empIndex == null) {
            empIndex = getIndex("emp", dataSource);
        }
        return empIndex;
    }

    /**
     * index的格式为{prefix}_{dataSourceId}。
     */
    @Nonnull
    private String getIndex(String prefix, @Nonnull DataSource dataSource) {
        return prefix + "_" + dataSource.getId();
    }

    private UpdateRequest buildRequest(String index, String id, Object model) {
        XContentBuilder document = buildDocument(model);

        IndexRequest indexRequest = new IndexRequest(index).id(id).source(document);
        return new UpdateRequest()
                .id(id)
                .index(index)
                .doc(document)
                .upsert(indexRequest);  // upsert: insert or update
    }

    @Nonnull
    @SneakyThrows
    private XContentBuilder buildDocument(@Nonnull Object object) {
        try (XContentBuilder builder = jsonBuilder()) {
            builder.startObject();
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (isPersisted(field)) {
                    field.setAccessible(true);
                    builder.field(field.getName(), field.get(object));
                }
            }

            return builder.endObject();
        }
    }

    /**
     * 返回字段是否需要持久化。
     *
     * @param field 数据对象的字段
     * @return 需要持久化字段的类型
     */
    private boolean isPersisted(@Nonnull Field field) {
        Ignore annotation = field.getDeclaredAnnotation(Ignore.class);
        return annotation == null;
    }
}
