package com.willowleaf.ldapsync.domain.model;

import com.willowleaf.ldapsync.domain.DataSource;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 标准的岗位信息模型。<br/>
 * 一个员工可能拥有多个岗位。
 */
@Data
public class Position {

    /**
     * 岗位ID。
     */
    private String id;
    /**
     * 编号（LDAP的唯一标识）。
     */
    private String number;
    /**
     * 名称。
     */
    private String name;
    /**
     * 数据源。
     */
    private DataSource dataSource;
    /**
     * 岗位的员工列表。
     */
    private List<Employee> employees = new CopyOnWriteArrayList<>();
}
