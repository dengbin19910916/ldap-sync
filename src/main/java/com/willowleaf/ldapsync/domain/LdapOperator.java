package com.willowleaf.ldapsync.domain;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.ldap.core.AttributesMapper;

import javax.annotation.Nonnull;
import javax.naming.directory.Attributes;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * 操作LDAP数据源，提供查询方法。
 *
 * @see DataSource
 */
@Slf4j
@Data
public class LdapOperator {

    protected DataSource dataSource;

    /**
     * <pre>
     * key - Pattern String, value - DateTimeFormatter[].
     * example: midea-birthday : DateTimeFormatter[]
     *
     * 现已被发现的日期格式:
     * 1. yyyyMMddHHmmss'Z'
     * 2. yyyy-MM-dd HH:mm:ss.S
     * 3. yyyy-MM-dd'T'HH:mm
     * </pre>
     */
    private Map<String, DateTimeFormatter[]> fieldFormatters = new HashMap<>();

    public LdapOperator(@Nonnull DataSource dataSource) {
        this.dataSource = dataSource;
        dataSource.getDictionary(Dictionary.Type.EMPLOYEE).getAttributeMaps()
                .stream()
                .filter(attributeMap -> !isEmpty(attributeMap.getPattern()))
                .collect(Collectors.toList())
                .forEach(attributeMap ->
                        fieldFormatters.put(
                                attributeMap.getSourceName(),
                                Stream.of(attributeMap.getPattern().split(","))
                                        .map(DateTimeFormatter::ofPattern)
                                        .toArray(DateTimeFormatter[]::new)
                        )
                );
    }

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
        return getDataSource().getLdapOperations().search(base, filter, SUBTREE_SCOPE,
                attributeMaps.stream().map(AttributeMap::getSourceName).toArray(String[]::new),
                (AttributesMapper<T>) attributes -> {
                    T model;
                    try {
                        model = clazz.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e);  // It won't happen.
                    }
                    BeanWrapper beanWrapper = new BeanWrapperImpl(model);
                    attributeMaps.parallelStream().forEach(attributeMap ->
                            beanWrapper.setPropertyValue(
                                    attributeMap.getTargetName(),
                                    getValue(attributes, attributeMap.getSourceName())
                            ));
                    return model;   // Never be null.
                });
    }

    @SneakyThrows
    private Object getValue(Attributes attributes, String sourceName) {
        Object value = attributes.get(sourceName) == null ? null : attributes.get(sourceName).get();

        if (value != null && fieldFormatters.containsKey(sourceName)) {  // 需要处理日期格式的字段
            DateTimeFormatter[] formatters = fieldFormatters.get(sourceName);
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
}
