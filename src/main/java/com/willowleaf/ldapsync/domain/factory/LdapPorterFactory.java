package com.willowleaf.ldapsync.domain.factory;

import com.willowleaf.ldapsync.domain.LdapOperator;
import com.willowleaf.ldapsync.domain.LdapPorter;
import com.willowleaf.ldapsync.domain.persistence.Persistence;
import com.willowleaf.ldapsync.domain.persistence.CompositePersistence;
import com.willowleaf.ldapsync.domain.porter.CycleLdapPorter;
import com.willowleaf.ldapsync.domain.porter.SingleLdapPorter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * 创建{@code LdapPorter}对象。
 *
 * @see LdapPorter
 * @see LdapOperator
 */
@Component
public class LdapPorterFactory {

    private final LdapOperatorFactory ldapOperatorFactory;
    private final Persistence persistence;

    public LdapPorterFactory(LdapOperatorFactory ldapOperatorFactory, CompositePersistence persistence) {
        this.ldapOperatorFactory = ldapOperatorFactory;
        this.persistence = persistence;
    }

    /**
     * 返回LDAP的搬运工。
     *
     * @param dataSourceId 数据源ID
     * @return LDAP搬运工
     */
    public LdapPorter getLdapPorter(@Nonnull Integer dataSourceId) {
        LdapOperator ldapOperator = ldapOperatorFactory.getLdapOperator(dataSourceId);

        switch (ldapOperator.getDataSource().getPullStrategy()) {
            case SINGLE:
                return new SingleLdapPorter(ldapOperator, persistence);
            case CYCLE:
                return new CycleLdapPorter(ldapOperator, persistence);
            default:
                throw new RuntimeException();   // It won't happen.
        }
    }

}
