package com.willowleaf.ldapsync.domain.porter;

import com.willowleaf.ldapsync.domain.*;
import com.willowleaf.ldapsync.domain.Department;
import com.willowleaf.ldapsync.domain.Employee;
import com.willowleaf.ldapsync.domain.Position;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.FutureTask;

public class CycleLdapPorter extends LdapPorter {

    public CycleLdapPorter(@Nonnull DataSource dataSource, @Nonnull Organization.Persistence persistence) {
        super(dataSource, persistence);
    }

    @SneakyThrows
    public Organization pull() {
        // 3. 异步获取所有的岗位信息
        FutureTask<List<Position>> positionTask = new FutureTask<>(() ->
                pullElements(dataSource.getDictionary(Dictionary.Type.POSITION), Position.class));
        new Thread(positionTask).start();

        // 1. 获取部门信息
        List<Department> departments = pullElements(dataSource.getDictionary(Dictionary.Type.DEPARTMENT), Department.class);

        // 2. 获部门的员工信息
        Dictionary employeeDictionary = dataSource.getDictionary(Dictionary.Type.EMPLOYEE);
        String sourceName = employeeDictionary.getAttributeMaps().parallelStream()
                .filter(attributeMap -> "departmentNumber".equals(attributeMap.getTargetName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("员工信息（departmentNumber）没有配置LDAP映射信息."))
                .getSourceName();
        departments.parallelStream().forEach(department -> {
            List<Employee> employees = pullElements(employeeDictionary, Employee.class, sourceName, department.getNumber());
            department.setEmployees(employees);
            employees.parallelStream().forEach(employee -> employee.setDepartment(department));
        });

        // 4. 获取员工的岗位信息
        List<Position> positions = positionTask.get();
        return new Organization(dataSource, departments, positions, persistence);
    }

}
