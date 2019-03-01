package com.willowleaf.ldapsync.site;

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/ldap")
public class LdapSyncController {

    private final LdapSyncService ldapSyncService;

    private final CuratorFramework client;

    public LdapSyncController(LdapSyncService ldapSyncService,
                              CuratorFramework client) {
        this.ldapSyncService = ldapSyncService;
        this.client = client;
    }

    private static final Integer FINISHED = 0;
    private static final Integer PROCESSING = 1;

    /**
     * 将LDAP的组织架构数据同步至用户中心。
     *
     * @param dataSourceId 数据源ID
     */
    @SneakyThrows
    @GetMapping("/sync/{dataSourceId:\\d+}")
    public Integer syncData(@PathVariable Integer dataSourceId) {
        InterProcessLock lock = new InterProcessMutex(client, "/lock/ldap/sync/" + dataSourceId);
        if (lock.acquire(0, TimeUnit.SECONDS)) {
            try {
                ldapSyncService.syncData(dataSourceId);
                return FINISHED;
            } finally {
                lock.release();
            }
        }

        return PROCESSING;
    }
}
