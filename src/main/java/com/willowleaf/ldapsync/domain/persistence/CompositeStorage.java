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

    private final DatabaseStorage databasePersistence;
    private final ElasticsearchStorage elasticsearchPersistence;

    public CompositeStorage(DatabaseStorage databasePersistence,
                            ElasticsearchStorage elasticsearchPersistence) {
        this.databasePersistence = databasePersistence;
        this.elasticsearchPersistence = elasticsearchPersistence;
    }

    @Override
    public void save(@Nonnull final Department department) {
        try {
            databasePersistence.save(department);
            elasticsearchPersistence.save(department);
        } catch (RuntimeException e) {
            remove(department, e);
            throw e;
        }
    }

    @Override
    public void remove(@Nonnull Department department, Exception e) {
        databasePersistence.remove(department, e);
        elasticsearchPersistence.remove(department, e);

        log.error("持久化错误! \n部门: " + department.getName()
                        + "[" + department.getId() + "], 员工数: " + department.getEmployees().size()
                        + ", \n员工列表: " + department.getEmployees().stream()
                        .map(employee -> employee.getName() + "[" + employee.getId() + "]").collect(toList()),
                e);
    }
}
