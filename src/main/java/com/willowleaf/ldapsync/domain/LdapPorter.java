package com.willowleaf.ldapsync.domain;

import lombok.Getter;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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

    /**
     * 返回数据集合。
     *
     * @see Department
     * @see Employee
     * @see Position
     */
    protected <T> List<T> pullElements(Dictionary dictionary, Class<T> clazz) {
        return getElements(
                dataSource.search(dictionary.getBase(), dictionary.getFilter(),
                        dictionary.getAttributeMaps(), clazz)
                        .parallelStream(),
                clazz);
    }

    /**
     * 返回员工数据集合。
     *
     * @see Employee
     */
    protected List<Employee> pullEmployeeElements(Dictionary dictionary, String name, String value) {
        Class<Employee> employeeClass = Employee.class;
        return getElements(
                dataSource.search(dictionary.getBase(), andFilter(dictionary.getFilter(), name, value),
                        dictionary.getAttributeMaps(), employeeClass)
                        .stream(),
                employeeClass);
    }

    /**
     * 返回一个完整的LDAP查询语句，使用 & 连接已有的filter和name=value。
     */
    private String andFilter(@Nonnull String filter, @Nonnull String name, @Nonnull String value) {
        return "(&" + filter + "(" + name + "=" + value + "))";
    }

    private <T> List<T> getElements(Stream<T> stream, Class<?> clazz) {
        return stream
                .peek(element -> {
                    try {
                        clazz.getDeclaredMethod("setDataSource", DataSource.class)
                                .invoke(element, dataSource);
                    } catch (IllegalAccessException
                            | InvocationTargetException
                            | NoSuchMethodException ignored) {
                    }
                })
                .collect(toList());
    }
}
