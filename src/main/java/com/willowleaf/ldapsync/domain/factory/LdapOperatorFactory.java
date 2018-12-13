package com.willowleaf.ldapsync.domain.factory;

import com.willowleaf.ldapsync.data.DataSourceRepository;
import com.willowleaf.ldapsync.data.DictionaryRepository;
import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.LdapOperator;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class LdapOperatorFactory {

    private final DataSourceRepository dataSourceRepository;
    private final DictionaryRepository dictionaryRepository;

    public LdapOperatorFactory(DataSourceRepository dataSourceRepository,
                               DictionaryRepository dictionaryRepository) {
        this.dataSourceRepository = dataSourceRepository;
        this.dictionaryRepository = dictionaryRepository;
    }

    /**
     * 返回LDAP数据源操作者。
     *
     * @param dataSourceId 数据源ID
     * @return LDAP数据源操作者
     */
    LdapOperator getLdapOperator(@Nonnull Integer dataSourceId) {
        DataSource dataSource = dataSourceRepository.findById(dataSourceId).orElseThrow(() -> new IllegalArgumentException("无效数据源ID"));
        dataSource.setDictionaryRepository(dictionaryRepository);
        return new LdapOperator(dataSource);
    }
}
