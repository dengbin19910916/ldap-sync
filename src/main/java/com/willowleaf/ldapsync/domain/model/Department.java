package com.willowleaf.ldapsync.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.willowleaf.ldapsync.domain.DataSource;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * 标准的部门信息模型。
 *
 * @see Employee 员工信息
 */
@Data
@Entity
public class Department {

    /**
     * 部门ID。
     */
    @Id
    @Column(length = 36)
    private String id;
    /**
     * 部门ID路径。
     */
    private String idPath;
    /**
     * 父部门。
     */
    @JsonIgnore
    @Transient
    private Department parent;
    /**
     * 子部门列表。
     */
    @JsonIgnore
    @Transient
    private List<Department> children = new CopyOnWriteArrayList<>();
    /**
     * 部门路径（包含当前部门）。
     */
    @JsonIgnore
    @Transient
    private List<Department> path = new CopyOnWriteArrayList<>();
    /**
     * 部门编号（必须），例如：30003397（LDAP的唯一标识）。
     */
    private String number;
    /**
     * 部门编号全路径，例如：00017661_100000_1038501017_30003397。
     */
    private String numberPath;
    /**
     * 中文名称（必须），例如：顺德工厂。
     */
    private String name;
    /**
     * 名称全路径，例如：美的_美的集团_家用空调事业部_顺德工厂。
     */
    @Column(length = 2000)
    private String namePath;
    /**
     * 英文名称，例如：Shunde Factory。
     */
    private String englishName;
    /**
     * 英文名称全路径，例如：Midea Holding Group_Midea Group_Residential Air Conditioner Division_Shunde Factory。
     */
    @Column(length = 2000)
    private String englishNamePath;
    /**
     * 上级部门编号（必须），例如：1038501017。
     */
    private String parentNumber;
    /**
     * 上级部门名称，例如：美的_美的集团_家用空调事业部。
     */
    private String parentName;
    /**
     * 电子邮件地址，例如：dm30003397@midea.com。
     */
    private String email;
    /**
     * 组织类型。
     */
    private String organizationType;
    /**
     * 负责人，例如：10005951（员工编号）。
     */
    private String personInCharge;
    /**
     * 显示顺序，例如：1。
     */
    private Integer sequence;
    /**
     * 员工列表。
     */
    @OneToMany(mappedBy = "department")
    private List<Employee> employees = new CopyOnWriteArrayList<>();
    /**
     * 数据源。
     */
    @ManyToOne
    private DataSource dataSource;

    /**
     * 返回当前部门的ID全路径。
     *
     * @return 部门ID路径
     */
    @SuppressWarnings("UnusedReturnValue")
    public String getIdPath() {
        if (isEmpty(idPath)) {
            idPath = getPath().stream().map(Department::getId).collect(Collectors.joining("_"));
        }
        return idPath;
    }

    /**
     * 返回当前部门的编号全路径。
     *
     * @return 部门编号路径
     */
    @SuppressWarnings("UnusedReturnValue")
    public String getNumberPath() {
        if (isEmpty(numberPath)) {
            numberPath = getPath().stream().map(Department::getNumber).collect(Collectors.joining("_"));
        }
        return numberPath;
    }

    /**
     * 返回当前部门的名称全路径。
     *
     * @return 部门名称路径
     */
    @SuppressWarnings("UnusedReturnValue")
    public String getNamePath() {
        if (isEmpty(namePath)) {
            namePath = getPath().stream().map(Department::getName).collect(Collectors.joining("_"));
        }
        return namePath;
    }

    /**
     * <pre>
     * 返回当前部门的路径（一个从根部门对象到当前部门对象所组成的链路）。
     * 在此方法被调用之前{@code Organization#buildDepartmentTree(List)}方法至少被调用过一次。
     * </pre>
     *
     * @return 当前部门的路径
     */
    public List<Department> getPath() {
        List<Department> departments = new ArrayList<>();
        Department dept = this;
        departments.add(dept);
        while (dept.parent != null) {
            dept = dept.parent;
            departments.add(dept);
        }
        Collections.reverse(departments);
        path = departments;
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Department{" +
                "id='" + id + '\'' +
                ", idPath='" + idPath + '\'' +
                ", number='" + number + '\'' +
                ", numberPath='" + numberPath + '\'' +
                ", name='" + name + '\'' +
                ", namePath='" + namePath + '\'' +
                ", englishName='" + englishName + '\'' +
                ", englishNamePath='" + englishNamePath + '\'' +
                ", parentNumber='" + parentNumber + '\'' +
                ", parentName='" + parentName + '\'' +
                ", email='" + email + '\'' +
                ", organizationType='" + organizationType + '\'' +
                ", personInCharge='" + personInCharge + '\'' +
                ", sequence=" + sequence +
                '}';
    }
}
