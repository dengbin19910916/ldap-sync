package com.willowleaf.ldapsync.domain.factory;

import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.LdapPorter;
import com.willowleaf.ldapsync.domain.Organization;
import com.willowleaf.ldapsync.domain.persistence.CompositeStorage;
import com.willowleaf.ldapsync.domain.porter.CycleLdapPorter;
import com.willowleaf.ldapsync.domain.porter.SingleLdapPorter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * 创建{@code LdapPorter}对象。
 *
 * @see LdapPorter
 */
@Component
public class LdapPorterFactory {

    private final DataSourceFactory dataSourceFactory;
    private final Organization.Storage storage;

    public LdapPorterFactory(DataSourceFactory dataSourceFactory, CompositeStorage persistence) {
        this.dataSourceFactory = dataSourceFactory;
        this.storage = persistence;
    }

    /**
     * 返回LDAP的搬运工。
     *
     * @param dataSourceId 数据源ID
     * @return LDAP搬运工
     */
    public LdapPorter getLdapPorter(@Nonnull Integer dataSourceId) {
        DataSource dataSource = dataSourceFactory.getDataSource(dataSourceId);

        switch (dataSource.getPullStrategy()) {
            case SINGLE:
                return new SingleLdapPorter(dataSource, storage);
            case CYCLE:
                return new CycleLdapPorter(dataSource, storage);
            default:
                throw new RuntimeException(String.format("Datasource [%s] not exists.", dataSourceId));
        }
    }

}
