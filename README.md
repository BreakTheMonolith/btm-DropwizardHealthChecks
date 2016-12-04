# DropwizardHealthChecks
** Under Construction** -- A Library of reusable health checks implementing the Dropwizard Health Check Framework 

This product is dedicated to making health checks as simple as possible so that you spend less
time dealing with non-functional requirements and get on to more fun coding activities.

Health checks currently available are:
### Relational Database Checks ###
[Usage and Install documentation](btm-DropwizardHealthChecks-jdbc/README.md)

* DataSourceHealthcheck - Checks applications ability to connect to a JDBC DataSource
* JDBCHealthcheck - Checks applications ability to connect to a database via JDBC

### System Requirements For All Checks ###
* Java JDK 1.7 or later
* io.dropwizard.metrics / metrics-healthchecks
* org.apache.commons / commons-lang3
* org.slf4j / slf4j-api
 
