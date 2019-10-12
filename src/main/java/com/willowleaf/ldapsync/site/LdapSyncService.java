package com.willowleaf.ldapsync.site;

import com.willowleaf.ldapsync.domain.LdapPorter;
import com.willowleaf.ldapsync.domain.Organization;
import com.willowleaf.ldapsync.domain.factory.LdapPorterFactory;
import org.springframework.stereotype.Service;

@Service
public class LdapSyncService {

    private final LdapPorterFactory ldapPorterFactory;

    public LdapSyncService(LdapPorterFactory ldapPorterFactory) {
        this.ldapPorterFactory = ldapPorterFactory;
    }

    void syncData(Integer dataSourceId) {
        LdapPorter porter = ldapPorterFactory.getLdapPorter(dataSourceId);

        // 1. 拉取数据并将其转换为标准的组织信息
        Organization organization = porter.pull();

        // 2. 持久化数据
        organization.save();
    }
}
