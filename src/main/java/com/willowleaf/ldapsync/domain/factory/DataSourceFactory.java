package com.willowleaf.ldapsync.domain.factory;

import com.willowleaf.ldapsync.data.DataSourceRepository;
import com.willowleaf.ldapsync.domain.DataSource;
import com.willowleaf.ldapsync.domain.Dictionary;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class DataSourceFactory {

    private final DataSourceRepository dataSourceRepository;

    public DataSourceFactory(DataSourceRepository dataSourceRepository) {
        this.dataSourceRepository = dataSourceRepository;
    }

    DataSource getDataSource(@Nonnull Integer dataSourceId) {
        DataSource dataSource = dataSourceRepository.findById(dataSourceId).orElseThrow(() -> new IllegalArgumentException("无效数据源ID"));
        Map<String, DateTimeFormatter[]> fieldFormatters = new HashMap<>();
        dataSource.getDictionary(Dictionary.Type.EMPLOYEE).getAttributeMaps()
                .stream()
                .filter(attributeMap -> !isEmpty(attributeMap.getPattern()))
                .forEach(attributeMap ->
                        fieldFormatters.put(
                                attributeMap.getSourceName(),
                                Stream.of(attributeMap.getPattern().split(","))
                                        .map(DateTimeFormatter::ofPattern)
                                        .toArray(DateTimeFormatter[]::new)
                        )
                );
        dataSource.setDateTimeFormatters(fieldFormatters);
        return dataSource;
    }
}
