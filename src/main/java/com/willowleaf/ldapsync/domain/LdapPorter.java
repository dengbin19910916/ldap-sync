package com.willowleaf.ldapsync.domain;

import lombok.Getter;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * LDAP数据搬运工。
 *
 * @see Department
 * @see Employee
 * @see Position
 */
public abstract class LdapPorter {

    @Getter
    protected DataSource dataSource;
    protected Organization.Storage storage;

    public LdapPorter(@Nonnull DataSource dataSource, @Nonnull Organization.Storage storage) {
        this.dataSource = dataSource;
        this.storage = storage;
    }

    /**
     * 从LDAP数据源拉取数据并将数据转换为统一组织架构模型。
     *
     * @return 组织架构信息
     */
    public abstract Organization pull();

    protected <T> List<T> pullElements(Dictionary dictionary, Class<T> clazz) {
        return pullElements(dictionary, clazz, null, null);
    }

    protected <T> List<T> pullElements(Dictionary dictionary, Class<T> clazz, String name, String value) {
        return dataSource.search(dictionary.getBase(), andFilter(dictionary.getFilter(), name, value),
                dictionary.getAttributeMaps(), clazz)
                .parallelStream()
                .peek(element -> {
                    try {
                        clazz.getDeclaredMethod("setDataSource", DataSource.class).invoke(element, dataSource);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 返回一个完整的LDAP查询语句，使用 & 连接已有的filter和name=value。
     */
    private String andFilter(@Nonnull String filter, String name, String value) {
        return isEmpty(name) || isEmpty(value) ? filter : "(&" + filter + "(" + name + "=" + value + "))";
    }
}
