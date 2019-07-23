/**
 * <pre>
 * 从LDAP数据源拉取数据并将数据持久化至Database和Elasticsearch。
 *
 * DataSource   LDAP数据源抽象
 * LdapPorter   负责拉取数据
 * Storage      持久化数据模型
 * </pre>
 *
 * @see com.willowleaf.ldapsync.domain.DataSource
 * @see com.willowleaf.ldapsync.domain.LdapPorter
 * @see com.willowleaf.ldapsync.domain.Organization
 * @see com.willowleaf.ldapsync.domain.Organization.Storage
 */
package com.willowleaf.ldapsync;
