package com.willowleaf.ldapsync.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.willowleaf.ldapsync.domain.factory.LdapPorterFactory;
import com.willowleaf.ldapsync.domain.model.Department;
import com.willowleaf.ldapsync.domain.model.Employee;
import com.willowleaf.ldapsync.data.DictionaryRepository;
import lombok.Data;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 为LDAP数据源提供{@code LdapOperations}对象方便操作，
 * 返回{@code Dictionary}提供LDAP的base和filter信息与属性映射信息。
 * 由{@code LdapOperator}使用{code DataSource}对象操作LDAP数据源。
 * </pre>
 *
 * @see LdapPorterFactory
 */
@Data
@Entity
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String url;

    private String username;

    private String password;

    @Enumerated
    private PullStrategy pullStrategy;

    @OneToMany(mappedBy = "dataSource", fetch = FetchType.EAGER)
    private List<Dictionary> dictionaries;

    @OneToMany(mappedBy = "dataSource")
    private List<Department> departments;

    @OneToMany(mappedBy = "dataSource")
    private List<Employee> employees;

    @JsonIgnore
    @Transient
    private DictionaryRepository dictionaryRepository;

    /**
     * 数据拉取类型。
     * 确定使用何种方式拉取员工信息数据。
     */
    public enum PullStrategy {
        /**
         * 一次拉取所有数据。
         */
        SINGLE,
        /**
         * 循环拉取数据。
         */
        CYCLE,
    }

    @JsonIgnore
    LdapOperations getLdapOperations() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setUserDn(username);
        contextSource.setPassword(password);
        contextSource.setCacheEnvironmentProperties(false);
        Map<String, Object> config = new HashMap<>();
        config.put("java.naming.ldap.attributes.binary", "objectGUID");
        contextSource.setBaseEnvironmentProperties(config);
        contextSource.setPooled(true);
        contextSource.setAuthenticationSource(new AuthenticationSource() {
            @Override
            public String getPrincipal() {
                return username;
            }

            @Override
            public String getCredentials() {
                return password;
            }
        });
        return new LdapTemplate(contextSource);
    }

    @JsonIgnore
    public Dictionary getDictionary(final Dictionary.Type dictionaryType) {
        return dictionaries.stream()
                .filter(dictionary -> dictionary.getType() == dictionaryType)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(dictionaryType + "字典配置缺失。"));
    }

    @Override
    public String toString() {
        return name;
    }
}
