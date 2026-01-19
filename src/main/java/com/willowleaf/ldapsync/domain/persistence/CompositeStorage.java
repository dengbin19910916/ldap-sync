package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class CompositeStorage implements Organization.Storage {

    private final DatabaseStorage databaseStorage;
    private final ElasticsearchStorage elasticsearchStorage;

    public CompositeStorage(DatabaseStorage databaseStorage,
                            ElasticsearchStorage elasticsearchStorage) {
        this.databaseStorage = databaseStorage;
        this.elasticsearchStorage = elasticsearchStorage;
    }

    @Override
    public void save(@Nonnull final Department department) {
        try {
            databaseStorage.save(department);
            elasticsearchStorage.save(department);
        } catch (Exception e) {
            remove(department, e);
            throw e;
        }
    }

    @Override
    public void remove(@Nonnull Department department, Exception e) {
        databaseStorage.remove(department, e);
        elasticsearchStorage.remove(department, e);

        log.error("持久化错误! \n部门: {}[{}], 员工数: {}, \n员工列表: {}", department.getName(),
                department.getId(), department.getEmployees().size(), department.getEmployees().stream()
                .map(employee -> employee.getName() + "[" + employee.getId() + "]").collect(toList()), e);
    }
}
