package com.willowleaf.ldapsync.domain;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    /**
     * 部门信息表。key - 部门编号，value - 部门信息。
     */
    private Map<String, Department> departmentMap = new ConcurrentHashMap<>();

    /**
     * 岗位信息表。key - 岗位编号，value - 岗位信息。
     */
    private Map<String, Position> positionMap = new ConcurrentHashMap<>();

    /**
     * 组织是否为空是否已经被计算。
     */
    private Boolean empty;

    private Persistence persistence;

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
                        @Nonnull Persistence persistence) {
        this(
                dataSource,
                departments,
                departments.parallelStream()
                        .flatMap(department -> department.getEmployees().parallelStream())
                        .collect(Collectors.toList()),
                positions,
                persistence
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
                        @Nonnull Persistence persistence) {
        this.dataSource = dataSource;
        this.departments = departments;
        this.employees = employees;
        this.positions = positions;
        this.persistence = persistence;

        init();
    }

    /**
     * <pre>
     * 持久化组织架构数据。
     * 组织架构数据包含了所有的部门，员工和岗位信息，其中部门持有员工信息列表。
     * </pre>
     *
     * @see Department 部门信息(包含部门的所有员工信息)
     * @see Employee 员工信息(包含员工的所有岗位信息）
     */
    public void save() {
        if (!isEmpty()) {
            getDepartments().parallelStream().forEach(persistence::save);
        }
    }

    @SneakyThrows
    private void init() {
        Thread buildDepartmentTreeTask = new Thread(this::buildDepartmentTree);
        buildDepartmentTreeTask.start();

        Thread calcRelationTask = new Thread(this::calcRelation);
        calcRelationTask.start();

        buildDepartmentTreeTask.join();
        setPath();
        calcRelationTask.join();
    }

    private void buildDepartmentTree() {
        Map<String, Department> departmentMap = getDepartmentMap();
        departments.parallelStream().forEach(department -> {
            String departmentParentNumber = department.getParentNumber();
            if (departmentParentNumber != null) {
                Department parent = departmentMap.get(departmentParentNumber);
                if (parent != null) {
                    department.setParent(parent);
                    parent.getChildren().add(department);
                }
            }
        });
    }

    /**
     * 计算员工与部门、岗位之间的关系。
     */
    private void calcRelation() {
        Map<String, Department> departmentMap = getDepartmentMap();
        Map<String, Position> positionMap = getPositionMap();
        employees = employees.parallelStream()
                .filter(employee -> employee.getDepartmentNumber() != null
                        && departmentMap.containsKey(employee.getDepartmentNumber()))  // 过滤掉没有部门的员工
                .peek(employee -> {
                    if (employee.getDepartment() == null) {
                        String departmentNumber = employee.getDepartmentNumber();
                        if (departmentNumber != null) {
                            Department department = departmentMap.get(departmentNumber);
                            employee.setDepartment(department);
                            department.getEmployees().add(employee);
                        }
                    }

                    String positionNumber = employee.getPositionNumber();
                    if (positionNumber != null) {
                        Position position = positionMap.get(positionNumber);
                        employee.getPositions().add(position);
                        employee.setPositionName(position == null ? null : position.getName());
                        if (position != null) {
                            position.getEmployees().add(employee);
                        }
                    }
                }).collect(Collectors.toList());
    }

    /**
     * 设置部门的idPath，numberPath和namePath。
     */
    private void setPath() {
        departments = departments.parallelStream()
                .peek(department -> {
                    department.getIdPath();
                    department.getNumberPath();
                    department.getNamePath();
                })
                .collect(Collectors.toList());
    }

    /**
     * 返回组织是否存在部门和员工。
     *
     * @return true - 存在，false - 部门或员工不存在
     */
    private boolean isEmpty() {
        if (empty == null) {
            empty = !(departments != null && !departments.isEmpty()
                    && departments.parallelStream().anyMatch(department -> !CollectionUtils.isEmpty(department.getEmployees())));
        }
        return empty;
    }

    /**
     * 返回部门信息表。key - 部门编号，value - 部门信息。
     *
     * @return 部门信息表
     */
    private Map<String, Department> getDepartmentMap() {
        if (!departmentMap.isEmpty()) return departmentMap;
        departments.parallelStream().forEach(department -> departmentMap.put(department.getNumber(), department));
        return departmentMap;
    }

    /**
     * 返回岗位信息表。key - 岗位编号，value - 岗位信息。
     *
     * @return 岗位信息表
     */
    private Map<String, Position> getPositionMap() {
        if (!positionMap.isEmpty()) return positionMap;
        positions.parallelStream().forEach(position -> positionMap.put(position.getNumber(), position));
        return positionMap;
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
}
