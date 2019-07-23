package com.willowleaf.ldapsync.domain.porter;

import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.Dictionary;
import com.willowleaf.ldapsync.domain.LdapPorter;
import com.willowleaf.ldapsync.domain.Organization;
import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Employee;
import com.willowleaf.ldapsync.domain.Position;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * 一次拉取出所有的数据，在内存中进行关系处理。
 */
public class SingleLdapPorter extends LdapPorter {

    public SingleLdapPorter(@Nonnull DataSource dataSource, @Nonnull Organization.Storage storage) {
        super(dataSource, storage);
    }

    @SneakyThrows
    public Organization pull() {
        // 1. 异步获取部门信息
        FutureTask<List<Department>> getDepartmentsTask = new FutureTask<>(() ->
                pullElements(dataSource.getDictionary(Dictionary.Type.DEPARTMENT), Department.class));
        new Thread(getDepartmentsTask).start();

        // 2. 异步获取员工信息
        FutureTask<List<Employee>> getEmployeesTask = new FutureTask<>(() ->
                pullElements(dataSource.getDictionary(Dictionary.Type.EMPLOYEE), Employee.class));
        new Thread(getEmployeesTask).start();

        // 3. 获取所有的岗位信息
        List<Position> positions = pullElements(dataSource.getDictionary(Dictionary.Type.POSITION), Position.class);

        // 4. 构造组织信息
        return new Organization(dataSource, getDepartmentsTask.get(), getEmployeesTask.get(), positions, storage);
    }

}
