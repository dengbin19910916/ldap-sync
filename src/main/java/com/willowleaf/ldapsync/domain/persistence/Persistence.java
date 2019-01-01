package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.domain.model.Department;

import javax.annotation.Nonnull;

/**
 * <pre>
 * 将标准的组织数据持久化。
 *
 * 使用组合模式分离数据库持久化和Elasticsearch持久化。
 * </pre>
 *
 * @see CompositePersistence
 * @see DatabasePersistence
 * @see ElasticsearchPersistence
 */
public interface Persistence {

    /**
     * <pre>
     * 持久化组织架构数据。
     *
     * 实现类不允许修改Department对象的数据。
     * </pre>
     *
     * @param department 部门信息，包含部门下的所有员工信息及员工的所有岗位信息
     * @see Department#getEmployees() 部门员工列表
     */
    void save(@Nonnull final Department department);
}
