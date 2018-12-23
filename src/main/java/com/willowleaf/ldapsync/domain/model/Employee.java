package com.willowleaf.ldapsync.domain.model;

import com.willowleaf.ldapsync.domain.DataSource;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 标准的员工信息模型。
 *
 * @see Position 岗位信息
 */
@Data
@Entity
public class Employee {

    /**
     * 员工ID。
     */
    @Id
    @Column(length = 36)
    private String id;
    /**
     * 员工ID，例如：dengbin（LDAP的唯一标识）。
     */
    private String uid;
    /**
     * 员工编号。
     */
    private String number;
    /**
     * 名称。
     */
    private String name;
    /**
     * 英文名称。
     */
    private String englishName;
    /**
     * 生日。
     */
    private LocalDateTime birthday;
    /**
     * 国籍。
     */
    private String nation;
    /**
     * 电子邮件地址。
     */
    private String email;
    /**
     * 名称拼音。
     */
    private String pinyin;
    /**
     * 性别。
     */
    private String gender;
    /**
     * 密码，例如：{SSHA}dseJhVJQpt/7Gv0qNNTflB11gYbpB98tdD0Dcw==。
     */
    private byte[] password;
    /**
     * 公司编码。
     */
    private String companyNumber;
    /**
     * 部门编号。
     */
    private String departmentNumber;
    /**
     * 公司名称。
     */
    private String companyName;
    /**
     * 兼职部门编号
     */
    private String partTimeDepartmentId;
    /**
     * 兼职部门名称。
     */
    private String partTimePosition;
    /**
     * 状态。
     */
    private String status;
    /**
     * 岗位编号。
     */
    private String positionNumber;
    /**
     * 岗位名称。
     */
    private String positionName;
    /**
     * 职级。
     */
    @Column
    private String level;
    /**
     * 移动电话。
     */
    private String mobile;
    /**
     * 移动电话短号码。
     */
    private String mobileShort;
    /**
     * 固定电话。
     */
    private String telephoneNumber;
    /**
     * 固定电话短号码。
     */
    private String telephoneShort;
    /**
     * 通讯地址。
     */
    private String address;
    /**
     * 序号。
     */
    private Integer sequence;
    /**
     * 员工所属部门。
     */
    @ManyToOne
    private Department department;
    /**
     * 员工的岗位列表。
     */
    @Transient
    private List<Position> positions = new CopyOnWriteArrayList<>();
    /**
     * 数据源。
     */
    @ManyToOne
    private DataSource dataSource;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(uid, employee.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", englishName='" + englishName + '\'' +
                ", birthday=" + birthday +
                ", nation='" + nation + '\'' +
                ", email='" + email + '\'' +
                ", pinyin='" + pinyin + '\'' +
                ", gender='" + gender + '\'' +
                ", password=" + Arrays.toString(password) +
                ", companyNumber='" + companyNumber + '\'' +
                ", departmentNumber='" + departmentNumber + '\'' +
                ", companyName='" + companyName + '\'' +
                ", partTimeDepartmentId='" + partTimeDepartmentId + '\'' +
                ", partTimePosition='" + partTimePosition + '\'' +
                ", status='" + status + '\'' +
                ", positionNumber='" + positionNumber + '\'' +
                ", positionName='" + positionName + '\'' +
                ", level='" + level + '\'' +
                ", mobile='" + mobile + '\'' +
                ", mobileShort='" + mobileShort + '\'' +
                ", telephoneNumber='" + telephoneNumber + '\'' +
                ", telephoneShort='" + telephoneShort + '\'' +
                ", address='" + address + '\'' +
                ", sequence=" + sequence +
                '}';
    }
}
