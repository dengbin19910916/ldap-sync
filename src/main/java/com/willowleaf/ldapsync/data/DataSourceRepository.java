package com.willowleaf.ldapsync.data;

import com.willowleaf.ldapsync.domain.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepository extends JpaRepository<DataSource, Integer> {
}
