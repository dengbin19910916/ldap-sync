package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.data.DepartmentRepository;
import com.willowleaf.ldapsync.data.EmployeeRepository;
import com.willowleaf.ldapsync.domain.Organization;
import com.willowleaf.ldapsync.domain.model.Department;
import lombok.SneakyThrows;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

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
public abstract class Persistence {

    @Autowired
    protected RestHighLevelClient client;
    @Autowired
    protected DepartmentRepository departmentRepository;
    @Autowired
    protected EmployeeRepository employeeRepository;

    /**
     * 持久化组织架构数据。
     *
     * @param organization 组织架构数据，包含所有的部门，员工和岗位信息，其中部门持有员工信息列表
     * @see Department 部门信息(包含当前部门下的所有员工信息)
     * @see Department#getEmployees() 部门员工列表
     */
    @SneakyThrows
    public void save(@Nonnull Organization organization) {
        if (!organization.isEmpty()) {
            organization.getDepartments().parallelStream().forEach(this::save);
        }
    }

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
    public abstract void save(@Nonnull final Department department);
}
