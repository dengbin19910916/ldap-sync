/**
 * <pre>
 * 从LDAP数据源拉取数据并将数据持久化至Database和Elasticsearch。
 *
 * DataSource   LDAP数据源抽象
 * LdapOperator 提供操作LDAP的方法
 * LdapPorter   负责拉取数据
 * Persistence  持久化数据模型
 * </pre>
 *
 * @see com.willowleaf.ldapsync.domain.DataSource
 * @see com.willowleaf.ldapsync.domain.LdapOperator
 * @see com.willowleaf.ldapsync.domain.LdapPorter
 * @see com.willowleaf.ldapsync.domain.persistence.Persistence
 */
package com.willowleaf.ldapsync;