package com.willowleaf.ldapsync.data;

import com.willowleaf.ldapsync.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from employee where data_source_id = ?1", nativeQuery = true)
    void deleteByDataSourceId(Integer dataSourceId);
}
