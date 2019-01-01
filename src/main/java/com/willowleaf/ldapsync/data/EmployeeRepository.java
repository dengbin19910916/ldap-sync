package com.willowleaf.ldapsync.data;

import com.willowleaf.ldapsync.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
