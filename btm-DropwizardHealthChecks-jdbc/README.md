# btm-DropwizardHealthChecks-jdbc
Provides health checks to databases supported by JDBC. 

### Installation ###

A Maven option is forthcoming.

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

### DataSourceHealthcheck ###

Example usage:
```  
import guru.breakthemonolith.health.jdbc.DataSourceHealthcheck;

DataSourceHealthcheck healthCheck = new DataSourceHealthcheck(myDataSource, myTestSQL);
myHealthCheckRegistry.register("database", healthCheck);
```  

### JDBCHealthcheck ###

Example usage:
```  
import guru.breakthemonolith.health.jdbc.JDBCHealthcheckTest;

JDBCHealthcheck healthCheck = new JDBCHealthcheck(myJDBCConnectionUrl, myTestSQL);
myHealthCheckRegistry.register("database", healthCheck);
```  


