# btm-DropwizardHealthChecks
A Library of reusable health checks implementing the Dropwizard Health Check Framework. These checks are 
usable with any JVM-based language (e.g. Java, Scala, Groovy, etc.). 

This product is dedicated to making health checks as simple as possible so that you spend less
time dealing with non-functional requirements and get on to more fun coding activities.

Health checks contained within include:
* Any database supporting JDBC including JNDI data sources (e.g. PostGreSQL, MySQL, Oracle, MS SQL Server, DB2, etc.).
* Any service exposing a Http(s) health check (nice for verifying dependent services are up)
* [Rabbit MQ](https://www.rabbitmq.com/)
* [Cassandra](http://cassandra.apache.org/)
* [MongoDB](https://www.mongodb.com/)

This product set utilizes the [Dropwizard Health Check platform](http://metrics.dropwizard.io/3.1.0/manual/healthchecks/). Maven artifacts for all btm-DropwizardHealthChecks are [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22).

We want additional health checks!  It's a time saver for everybody. Information on how you can contribute is found [here](CONTRIBUTING.md). 
Ideas for additional health checks are:
* [Apache Spark](http://spark.apache.org/)
* [Apache Kafka](https://kafka.apache.org/)
* [Apache Hadoop](http://hadoop.apache.org/)
* [Apache ActiveMQ](http://activemq.apache.org/)
* [IBM Websphere MQ](https://www-01.ibm.com/software/integration/wmq/clients/)

Health checks currently available are:
### Relational Database Checks
[Usage and Install documentation](btm-DropwizardHealthChecks-jdbc/README.md)

* DataSourceHealthCheck - Checks applications ability to connect to a JDBC DataSource
* JDBCHealthCheck - Checks applications ability to connect to a database via JDBC

### Network Resource Checks
[Usage and Install documentation](btm-DropwizardHealthChecks-net/README.md)

* HttpHealthCheck - Health check for dependent Http(s) resources or services

### RabbitMQ Resource Checks
[Usage and Install documentation](btm-DropwizardHealthChecks-rabbitmq/README.md)

* RabbitMQHealthCheck - Health check for RabbitMQ queue access.

### Cassandra database Checks
[Usage and Install documentation](btm-DropwizardHealthChecks-cassanrda/README.md)

* CassandraHealthCheck - Health check for Cassanrda database access.

### MongoDB database Checks
[Usage and Install documentation](btm-DropwizardHealthChecks-mongodb/README.md)

* MongoDbHealthCheck - Health check for MongoDB database access.


### System Requirements For All Checks
* Java JDK 1.7 or later
* io.dropwizard.metrics / metrics-healthchecks
* org.apache.commons / commons-lang3
* org.slf4j / slf4j-api
 
