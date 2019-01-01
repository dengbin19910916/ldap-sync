package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.data.DepartmentRepository;
import com.willowleaf.ldapsync.data.EmployeeRepository;
import com.willowleaf.ldapsync.domain.model.Department;
import com.willowleaf.ldapsync.domain.model.Employee;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class DatabasePersistence implements Persistence {

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

    void remove(@Nonnull final Department department) {
        departmentRepository.delete(department);
        employeeRepository.deleteAll(department.getEmployees());
    }
}
