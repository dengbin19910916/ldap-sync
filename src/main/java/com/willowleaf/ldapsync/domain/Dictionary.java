package com.willowleaf.ldapsync.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * LDAP字典可以查询base和filter信息与属性映射信息。
 */
@Data
@Entity
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated
    private Type type;

    @Column(length = 2000)
    private String base;

    @Column(length = 2000)
    private String filter;

    @OneToMany(mappedBy = "dictionary")
    private List<AttributeMap> attributeMaps;

    @ManyToOne
    private DataSource dataSource;

    /**
     * 通过字典类型获取对应的字典信息。
     */
    public enum Type {
        /**
         * 部门。
         */
        DEPARTMENT,
        /**
         * 员工。
         */
        EMPLOYEE,
        /**
         * 岗位。
         */
        POSITION
    }

    @Override
    public String toString() {
        return type.name();
    }
}
