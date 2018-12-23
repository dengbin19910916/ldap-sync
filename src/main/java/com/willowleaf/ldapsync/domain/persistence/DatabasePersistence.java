package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.domain.model.Department;
import com.willowleaf.ldapsync.domain.model.Employee;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class DatabasePersistence extends Persistence {

    @Override
    public void save(@Nonnull final Department department) {
        List<Employee> employees = department.getEmployees();
        department.setEmployees(null);      // Hibernate JPA 实现如此
        departmentRepository.save(department);
        employeeRepository.saveAll(employees);
        department.setEmployees(employees);
    }
}
