package com.willowleaf.ldapsync.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * LDAP的属性转换至同步程序使用的映射信息。
 */
@Data
@NoArgsConstructor
@Entity
public class AttributeMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 源名称是LDAP数据源的属性名称，是每个DN所使用的名称。
     */
    public String sourceName;

    /**
     * 目标名称是同步程序的字段名称。
     */
    private String targetName;

    /**
     * 日期格式。多种格式使用 , 分割。
     */
    private String pattern;

    @ManyToOne
    private Dictionary dictionary;

    @Override
    public String toString() {
        return "AttributeMap{" +
                "id=" + id +
                ", sourceName='" + sourceName + '\'' +
                ", targetName='" + targetName + '\'' +
                ", pattern='" + pattern + '\'' +
                '}';
    }
}
