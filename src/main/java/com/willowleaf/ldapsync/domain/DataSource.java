package com.willowleaf.ldapsync.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.willowleaf.ldapsync.domain.factory.LdapPorterFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.annotation.Nonnull;
import javax.naming.directory.Attributes;
import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

/**
 * <pre>
 * 返回{@code Dictionary}提供LDAP的base和filter信息与属性映射信息，
 * 并提供对数据源的操作方法。
 * </pre>
 *
 * @see LdapPorterFactory
 */
@Data
@Slf4j
@NoArgsConstructor
@Entity
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String url;

    private String username;

    private String password;

    @Transient
    private LdapOperations ldapOperations;

    @Enumerated
    private PullStrategy pullStrategy;

    @OneToMany(mappedBy = "dataSource")
    private List<Dictionary> dictionaries;

    @OneToMany(mappedBy = "dataSource")
    private List<Department> departments;

    @OneToMany(mappedBy = "dataSource")
    private List<Employee> employees;

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

    /**
     * <pre>
     * 日期字段具有多种日期格式。
     * key - Pattern String, value - DateTimeFormatter[].
     * example: midea-birthday : DateTimeFormatter[]
     *
     * 现已被发现的日期格式:
     * 1. yyyyMMddHHmmss'Z'
     * 2. yyyy-MM-dd HH:mm:ss.S
     * 3. yyyy-MM-dd'T'HH:mm
     * </pre>
     */
    @JsonIgnore
    @Transient
    private Map<String, DateTimeFormatter[]> dateTimeFormatters;

    /**
     * 返回数据列表。
     *
     * @param base          LDAP base
     * @param filter        LDAP filter
     * @param attributeMaps 属性映射
     * @param clazz         结果数据类型的Class对象
     * @param <T>           结果数据类型
     * @return 数据列表
     */
    <T> List<T> search(@Nonnull String base, @Nonnull String filter,
                       @Nonnull List<AttributeMap> attributeMaps,
                       @Nonnull Class<T> clazz) {
        return getLdapOperations().search(base, filter, SUBTREE_SCOPE,
                attributeMaps.stream().map(AttributeMap::getSourceName).toArray(String[]::new),
                (AttributesMapper<T>) attributes -> {
                    T model;
                    try {
                        model = clazz.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException
                             | NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e);  // It won't happen.
                    }
                    BeanWrapper beanWrapper = new BeanWrapperImpl(model);
                    for (AttributeMap attributeMap : attributeMaps) {
                        beanWrapper.setPropertyValue(
                                attributeMap.getTargetName(),
                                getValue(attributes, attributeMap.getSourceName())
                        );
                    }
                    return model;   // Never be null.
                });
    }

    @SneakyThrows
    private Object getValue(Attributes attributes, String sourceName) {
        if (attributes.get(sourceName) == null
                || attributes.get(sourceName).get() == null) {
            return null;
        }

        Object value = attributes.get(sourceName).get();
        if (dateTimeFormatters.containsKey(sourceName)) {   // 需要处理日期格式的字段
            DateTimeFormatter[] formatters = dateTimeFormatters.get(sourceName);
            for (int i = 0; i < formatters.length; i++) {
                try {
                    value = LocalDateTime.parse(value.toString(), formatters[i]);
                    break;
                } catch (DateTimeParseException e) {
                    if (i == formatters.length - 1) {   // 未找到合适的日期格式
                        log.error("日期格式不合法[{}]，请添加合适的日期格式配置。", value.toString());
                        throw e;
                    }
                }
            }
        }
        return value;
    }

    @JsonIgnore
    private LdapOperations getLdapOperations() {
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
        if (ldapOperations == null) {
            this.ldapOperations = new LdapTemplate(contextSource);
        }
        return ldapOperations;
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
