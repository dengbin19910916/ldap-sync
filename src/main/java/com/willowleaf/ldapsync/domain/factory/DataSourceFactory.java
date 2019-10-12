package com.willowleaf.ldapsync.domain.factory;

import com.willowleaf.ldapsync.data.DataSourceRepository;
import com.willowleaf.ldapsync.domain.DataSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.willowleaf.ldapsync.domain.Dictionary.Type.EMPLOYEE;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class DataSourceFactory {

    private final DataSourceRepository dataSourceRepository;

    public DataSourceFactory(DataSourceRepository dataSourceRepository) {
        this.dataSourceRepository = dataSourceRepository;
    }

    DataSource getDataSource(@Nonnull Integer dataSourceId) {
        DataSource dataSource = dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new IllegalArgumentException("无效数据源ID"));
        Map<String, DateTimeFormatter[]> dateTimeFormatters = new HashMap<>();
        dataSource.getDictionary(EMPLOYEE).getAttributeMaps()
                .stream()
                .filter(attributeMap -> !isEmpty(attributeMap.getPattern()))
                .forEach(attributeMap ->
                        dateTimeFormatters.put(
                                attributeMap.getSourceName(),
                                Stream.of(attributeMap.getPattern().split(","))
                                        .map(DateTimeFormatter::ofPattern)
                                        .toArray(DateTimeFormatter[]::new)
                        )
                );
        dataSource.setDateTimeFormatters(dateTimeFormatters);
        return dataSource;
    }
}
