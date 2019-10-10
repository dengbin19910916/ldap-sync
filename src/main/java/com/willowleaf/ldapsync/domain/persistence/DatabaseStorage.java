package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.data.DepartmentRepository;
import com.willowleaf.ldapsync.data.EmployeeRepository;
import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Organization;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class DatabaseStorage implements Organization.Storage {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DatabaseStorage(DepartmentRepository departmentRepository,
                           EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void save(@Nonnull final Department department) {
        departmentRepository.save(department);
    }

    @Override
    public void remove(@Nonnull final Department department, Exception e) {
        departmentRepository.delete(department);
        employeeRepository.deleteAll(department.getEmployees());
    }
}
