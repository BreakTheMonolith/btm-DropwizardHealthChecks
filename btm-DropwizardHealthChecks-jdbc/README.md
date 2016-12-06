# btm-DropwizardHealthChecks-jdbc
Provides health checks to databases supported by JDBC. 

### Installation ###

Maven users can find dependency information [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22%20AND%20a%3A%22btm-DropwizardHealthChecks-jdbc%22).

To install, simply include btm-DropwizardHealthChecks-jdbc.jar and all parent dependencies described [here](../README.md). No
additional dependencies are needed.

### Test SQL Queries ###
All database checks require a test SQL statement that will ensure that the database is functioning. It
is recommended that that SQL statement be very lightweight.  Recommendations include:

For Oracle
```  
select 1 from dual
```  

For PostgreSQL, MySQL, Microsoft SQL Server and several others:
```  
select 1
```  

### DataSourceHealthCheck ###

Example usage:
```  
import guru.breakthemonolith.health.jdbc.DataSourceHealthCheck;

DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(myDataSource, myTestSQL);
myHealthCheckRegistry.register("database", healthCheck);
```  

### JDBCHealthCheck ###

Example usage:
```  
import guru.breakthemonolith.health.jdbc.JDBCHealthCheck;

JDBCHealthCheck healthCheck = new JDBCHealthCheck(myJDBCConnectionUrl, myTestSQL);
myHealthCheckRegistry.register("database", healthCheck);
```  


