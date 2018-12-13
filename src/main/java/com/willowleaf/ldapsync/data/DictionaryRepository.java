package com.willowleaf.ldapsync.data;

import com.willowleaf.ldapsync.domain.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, Integer> {
}
