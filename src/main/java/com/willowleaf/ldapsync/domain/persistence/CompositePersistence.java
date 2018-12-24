package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.domain.model.Department;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class CompositePersistence extends Persistence {

    private final DatabasePersistence databasePersistence;
    private final RedisPersistence redisPersistence;
    private final ElasticsearchPersistence elasticsearchPersistence;

    public CompositePersistence(DatabasePersistence databasePersistence,
                                RedisPersistence redisPersistence,
                                ElasticsearchPersistence elasticsearchPersistence) {
        this.databasePersistence = databasePersistence;
        this.redisPersistence = redisPersistence;
        this.elasticsearchPersistence = elasticsearchPersistence;
    }

    @Override
    public void save(@Nonnull final Department department) {
        try {
            databasePersistence.save(department);
//            redisPersistence.save(department);
            elasticsearchPersistence.save(department);
        } catch (RuntimeException e) {
            databasePersistence.remove(department);
            redisPersistence.remove(department);

            log.error("持久化错误! \n部门: " + department.getName()
                            + "[" + department.getId() + "], 员工数: " + department.getEmployees().size()
                            + ", \n员工列表: " + department.getEmployees().stream()
                            .map(employee -> employee.getName() + "[" + employee.getId() + "]").collect(toList()),
                    e);
            throw e;
        }
    }
}
