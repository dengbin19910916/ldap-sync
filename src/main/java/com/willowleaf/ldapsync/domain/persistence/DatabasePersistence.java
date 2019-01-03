package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.data.DepartmentRepository;
import com.willowleaf.ldapsync.data.EmployeeRepository;
import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Employee;
import com.willowleaf.ldapsync.domain.Organization;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class DatabasePersistence implements Organization.Persistence {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DatabasePersistence(DepartmentRepository departmentRepository,
                               EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void save(@Nonnull final Department department) {
        List<Employee> employees = department.getEmployees();
        department.setEmployees(null);      // Hibernate JPA 实现如此
        departmentRepository.save(department);
        employeeRepository.saveAll(employees);
        department.setEmployees(employees);
    }

    @Override
    public void remove(@Nonnull final Department department, Exception e) {
        departmentRepository.delete(department);
        employeeRepository.deleteAll(department.getEmployees());
    }
}
