package com.willowleaf.ldapsync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperProperties {

    private String host = "localhost:2181";
}
