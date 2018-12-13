package com.willowleaf.ldapsync.domain.porter;

import com.willowleaf.ldapsync.domain.Dictionary;
import com.willowleaf.ldapsync.domain.LdapOperator;
import com.willowleaf.ldapsync.domain.LdapPorter;
import com.willowleaf.ldapsync.domain.persistence.Persistence;
import com.willowleaf.ldapsync.domain.model.Department;
import com.willowleaf.ldapsync.domain.model.Employee;
import com.willowleaf.ldapsync.domain.Organization;
import com.willowleaf.ldapsync.domain.model.Position;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.FutureTask;

public class SingleLdapPorter extends LdapPorter {

    public SingleLdapPorter(@Nonnull LdapOperator operator, @Nonnull Persistence persistence) {
        super(operator, persistence);
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
        return new Organization(dataSource, getDepartmentsTask.get(), getEmployeesTask.get(), positions, persistence);
    }

}
