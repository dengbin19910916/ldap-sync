package com.willowleaf.ldapsync.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ZookeeperProperties.class)
public class CuratorAutoConfiguration {

    private final ZookeeperProperties zookeeperProperties;

    public CuratorAutoConfiguration(ZookeeperProperties zookeeperProperties) {
        this.zookeeperProperties = zookeeperProperties;
    }

    @Bean
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework client() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                zookeeperProperties.getHost(),
                new RetryNTimes(5, 5_000)
        );
        client.start();
        return client;
    }
}
