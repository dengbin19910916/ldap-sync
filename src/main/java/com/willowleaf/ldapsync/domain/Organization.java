package com.willowleaf.ldapsync.domain;

import lombok.Getter;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * 标准的组织模型，不论任何源数据都必须转换成标准模型。
 */
public class Organization {

    /**
     * 组织信息所属的数据源。
     */
    @Getter
    private DataSource dataSource;

    /**
     * 所有部门。
     */
    @Getter
    private List<Department> departments;

    /**
     * 所有员工。
     */
    @Getter
    private List<Employee> employees;

    /**
     * 所有岗位。
     */
    @Getter
    private List<Position> positions;

    private Storage storage;

    /**
     * 创建组织架构。
     *
     * @param dataSource  数据源
     * @param departments 所有的部门信息，包含员工信息
     * @param positions   所有的岗位信息
     */
    public Organization(@Nonnull DataSource dataSource,
                        @Nonnull List<Department> departments,
                        @Nonnull List<Position> positions,
                        @Nonnull Storage storage) {
        this(
                dataSource,
                departments,
                departments.parallelStream()
                        .flatMap(department -> department.getEmployees().stream())
                        .collect(toList()),
                positions,
                storage
        );
    }

    /**
     * 创建组织架构。
     *
     * @param dataSource  数据源
     * @param departments 所有的部门信息，不包含员工信息
     * @param employees   所有的员工信息
     * @param positions   所有的岗位信息
     */
    public Organization(@Nonnull DataSource dataSource,
                        @Nonnull List<Department> departments,
                        @Nonnull List<Employee> employees,
                        @Nonnull List<Position> positions,
                        @Nonnull Storage storage) {
        this.dataSource = dataSource;
        this.departments = departments;
        this.employees = employees;
        this.positions = positions;
        this.storage = storage;

        init();
    }

    /**
     * <pre>
     * 持久化组织架构数据。
     * 组织架构数据包含了所有的部门，员工和岗位信息，其中部门持有员工信息列表。
     * </pre>
     *
     * @see Department 部门信息(包含部门的所有员工信息)
     * @see Employee 员工信息(包含员工的所有岗位信息)
     */
    public void save() {
        if (!isEmpty(departments)) {
            getDepartments().parallelStream().forEach(storage::save);
        }
    }

    @SneakyThrows
    private void init() {
        Map<String, List<Department>> departmentMap = departments.parallelStream()
                .collect(groupingBy(Department::getNumber));

        Thread calcRelationTask = new Thread(() -> calcRelation(departmentMap));
        calcRelationTask.start();
        buildDepartmentTree(departmentMap);
        calcRelationTask.join();
    }

    private void buildDepartmentTree(Map<String, List<Department>> departmentMap) {
        Flux.fromIterable(departments)
                .subscribe(
                        department -> {
                            String departmentParentNumber = department.getParentNumber();
                            if (departmentParentNumber != null) {
                                Department parent = departmentMap.get(departmentParentNumber).get(0);
                                if (parent != null) {
                                    department.setParent(parent);
                                    parent.getChildren().add(department);
                                }
                            }
                        },
                        error -> {
                            throw new RuntimeException("部门树节点生产失败");
                        },
                        this::setPath
                );
    }

    /**
     * 计算员工与部门、岗位之间的关系。
     */
    private void calcRelation(Map<String, List<Department>> departmentMap) {
        Map<String, List<Position>> positionMap = positions.parallelStream()
                .collect(groupingBy(Position::getNumber));

        employees = employees.parallelStream()
                .filter(employee -> employee.getDepartmentNumber() != null
                        && departmentMap.containsKey(employee.getDepartmentNumber()))  // 过滤掉没有部门的员工
                .peek(employee -> {
                    if (employee.getDepartment() == null) {
                        String departmentNumber = employee.getDepartmentNumber();
                        if (departmentNumber != null) {
                            Department department = departmentMap.get(departmentNumber).get(0);
                            employee.setDepartment(department);
                            department.getEmployees().add(employee);
                        }
                    }

                    String positionNumber = employee.getPositionNumber();
                    if (positionNumber != null) {
                        Position position = positionMap.get(positionNumber).get(0);
                        employee.getPositions().add(position);
                        employee.setPositionName(position == null ? null : position.getName());
                        if (position != null) {
                            position.getEmployees().add(employee);
                        }
                    }
                })
                .collect(toList());
    }

    /**
     * 设置部门的idPath，numberPath和namePath。
     */
    private void setPath() {
        departments.parallelStream()
                .forEach(department -> {
                    department.getIdPath();
                    department.getNumberPath();
                    department.getNamePath();
                });
    }

    @Override
    public String toString() {
        return dataSource.getName() + "组织架构信息: \n" +
                "\t部门总数: " + departments.size() +
                "\t员工总数: " + employees.size();
    }

    /**
     * <pre>
     * 将标准的组织数据持久化。
     *
     * 使用组合模式分离数据库持久化和Elasticsearch持久化。
     * </pre>
     */
    public interface Storage {

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

        /**
         * 移除组织架构数据。
         *
         * @param department 部门信息，包含部门下的所有员工信息及员工的所有岗位信息
         * @param e          异常信息
         */
        void remove(@Nonnull final Department department, Exception e);
    }
}
