# btm-DropwizardHealthChecks
A Library of reusable health checks implementing the Dropwizard Health Check Framework.

This product is dedicated to making health checks as simple as possible so that you spend less
time dealing with non-functional requirements and get on to more fun coding activities.

This product set utilizes the [Dropwizard Health Check platform](http://metrics.dropwizard.io/3.1.0/manual/healthchecks/)

Maven artifacts for all btm-DropwizardHealthChecks are [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22).

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

### System Requirements For All Checks
* Java JDK 1.7 or later
* io.dropwizard.metrics / metrics-healthchecks
* org.apache.commons / commons-lang3
* org.slf4j / slf4j-api
 
