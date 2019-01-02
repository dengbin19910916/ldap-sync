package com.willowleaf.ldapsync.domain.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willowleaf.ldapsync.domain.model.Department;
import com.willowleaf.ldapsync.domain.model.Employee;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RedisPersistence extends Persistence {

    private final RedisOperations<Object, Object> redisOperations;
    private final ObjectMapper objectMapper;

    public RedisPersistence(RedisOperations<Object, Object> redisOperations, ObjectMapper objectMapper) {
        this.redisOperations = redisOperations;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public void save(@Nonnull final Department department) {
        redisOperations.opsForValue().set(getDeptKey(department.getId()), objectMapper.writeValueAsString(department));

        if (!department.getEmployees().isEmpty()) {
            Map<String, String> employees = new HashMap<>(department.getEmployees().size());
            for (Employee employee : department.getEmployees()) {
                employees.put(getEmpKey(employee.getId()), objectMapper.writeValueAsString(employee));
            }
            redisOperations.opsForValue().multiSet(employees);
        }
    }

    void remove(Department department) {
        List<String> keys = new ArrayList<>(department.getEmployees().size() + 1);
        keys.add(getDeptKey(department.getId()));
        for (Employee employee : department.getEmployees()) {
            keys.add(getEmpKey(employee.getId()));
        }
        redisOperations.delete(keys);
    }

    private String getDeptKey(String id) {
        return getKey("dept", id);
    }

    private String getEmpKey(String id) {
        return getKey("emp", id);
    }

    private String getKey(String type, String id) {
        return "muc:" + type + ":" + id;
    }
}
