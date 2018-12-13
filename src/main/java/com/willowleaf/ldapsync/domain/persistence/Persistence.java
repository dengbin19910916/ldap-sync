package com.willowleaf.ldapsync.domain.persistence;

import com.willowleaf.ldapsync.data.DepartmentRepository;
import com.willowleaf.ldapsync.data.EmployeeRepository;
import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.Organization;
import com.willowleaf.ldapsync.domain.model.Department;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.elasticsearch.client.RequestOptions.DEFAULT;

/**
 * 将标准的组织数据持久化。
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

    private static final String INDEX_PREFIX_DEPT = "dept";
    private static final String INDEX_PREFIX_EMP = "emp";

    private static String getIndex(String indexPrefix, DataSource dataSource) {
        return indexPrefix + "_" + dataSource.getId();
    }

    /**
     * index的格式为dept_{dataSourceId}。
     */
    static String getDeptIndex(@Nonnull DataSource dataSource) {
        return getIndex(INDEX_PREFIX_DEPT, dataSource);
    }

    /**
     * index的格式为emp_{dataSourceId}。
     */
    static String getEmpIndex(@Nonnull DataSource dataSource) {
        return getIndex(INDEX_PREFIX_EMP, dataSource);
    }

    private void deleteIndex(String index) throws IOException {
        if (client.indices().exists(new GetIndexRequest().indices(index), DEFAULT)) {
            client.indices().delete(new DeleteIndexRequest(index), DEFAULT);
        }
    }

    private void deleteIfExists(@Nonnull Organization organization) throws IOException {
        deleteIndex(getDeptIndex(organization.getDataSource()));
        deleteIndex(getEmpIndex(organization.getDataSource()));

        employeeRepository.deleteByDataSourceId(organization.getDataSource().getId());
        departmentRepository.deleteByDataSourceId(organization.getDataSource().getId());
    }

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
            deleteIfExists(organization);
            organization.getDepartments().parallelStream().forEach(this::save);
        }
    }

    /**
     * 持久化组织架构数据。
     *
     * @param department 部门信息，包含部门下的所有员工信息及员工的所有岗位信息
     * @see Department#getEmployees() 部门员工列表
     */
    public abstract void save(@Nonnull Department department);
}
