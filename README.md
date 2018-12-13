# LDAP数据源同步
###### 为了优雅的代码

## 1. 背景

为了解决当下拉取LDAP数据的代码混乱的问题，构建一个优雅的拉取数据的模型，
并能通过合理的方式进行扩展。

## 2. 设计
使用领域驱动设计，参考《领域驱动设计》和 [领域驱动设计，盒马技术团队这么做](https://www.toutiao.com/a6592013320461484547/)，

为什么使用DDD（领域驱动设计）呢？当下我们的代码都是面向过程的代码，将一堆的过程代码包裹在一个对象中而已，
这种代码的可阅读性不高，可维护性差，很难扩展。当我们的功能设计出现根本性错误时，修改成本很高，我们更加难以从根本上解决。<br/>

鉴于原来代码的维护成本高，为此我们重新思考了关于LDAP的组织架构数据同步的问题，建立了一套对象模型
来完成数据同步。模型对象分为DataSource，LdapPorter和Persistence。

1. DataSource对象返回操作目标LDAP源的对象。
2. LdapPorter对象将数据从LDAP源上拉取下来并将数据根据Dictionary对象提供的AttributeMap将数据转换为标准模型Organization（由DataSource, Department, Employee和Position构建而成）。
3. Persistence对象将标准模型数据持久化到目标位置。

传统的Service对象将负责处理什么事情呢？它们当然还是继续处理业务逻辑，只是现在的业务逻辑的具体实现代码不是直接写在Service里面了，那是使用面向过程的思想写代码，根据面向对象的思想，我们需要将业务逻辑封装在合理的对象之中，然后由Service对象进行组合调用完成业务。

### 3. 启动
环境需求：JDK 1.8+，Elasticsearch 6+，MySQL 5+，Maven 3.3+

1. 拉取代码

	```git
	git pull https://github.com/dengbin19910916/ldap-sync.git
	```

2. 编译

	```cmd
	mvn clean package
	```

3. 运行

	```cmd
	java [-Dspring.profiles.active=dev] -jar ldap-sync-0.0.1-SNAPSHOT.jar
	```