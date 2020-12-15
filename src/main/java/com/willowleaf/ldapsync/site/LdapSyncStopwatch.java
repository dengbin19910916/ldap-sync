package com.willowleaf.ldapsync.site;

import com.willowleaf.ldapsync.domain.LdapPorter;
import com.willowleaf.ldapsync.domain.factory.LdapPorterFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 记录数据同步消耗的时间。
 */
@Slf4j
@Aspect
@Component
public class LdapSyncStopwatch {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LdapPorterFactory ldapPorterFactory;

    public LdapSyncStopwatch(LdapPorterFactory ldapPorterFactory) {
        this.ldapPorterFactory = ldapPorterFactory;
    }

    @Pointcut("execution(* com.willowleaf.ldapsync.site.LdapSyncService.syncData(..))")
    public void syncSpendTime() {
    }

    @Around("syncSpendTime()")
    public Object syncAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Integer dataSourceId = (Integer) joinPoint.getArgs()[0];
        LdapPorter porter = ldapPorterFactory.getLdapPorter(dataSourceId);

        ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();
        LocalDateTime start = LocalDateTime.now();

        log.info("{}数据同步开始[{}]", porter.getDataSource().getName(), start.format(FORMATTER));
        Object result = joinPoint.proceed();
        log.info("{}数据同步结束[{}], 共耗时: {}s",
                porter.getDataSource().getName(),
                LocalDateTime.now().format(FORMATTER),
                LocalDateTime.now().toEpochSecond(zoneOffset) - start.toEpochSecond(zoneOffset));
        return result;
    }
}
