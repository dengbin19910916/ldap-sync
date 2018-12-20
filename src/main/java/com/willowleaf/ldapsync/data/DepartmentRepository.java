package com.willowleaf.ldapsync.data;

import com.willowleaf.ldapsync.domain.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
