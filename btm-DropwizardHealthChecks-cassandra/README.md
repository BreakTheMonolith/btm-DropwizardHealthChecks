# btm-DropwizardHealthChecks-cassandra
Provides health check for a Cassandra database. 

### Installation

Maven users can find dependency information [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22%20AND%20a%3A%22btm-DropwizardHealthChecks-cassandra%22).

To install, simply include btm-DropwizardHealthChecks-cassanrda.jar and all parent dependencies described [here](../README.md). In addition, you need the following dependent libraries:
* com.datastax.cassandra / cassandra-driver-core (version 3x or above)

### CassandraHealthCheck

Default settings are as follows. You've an option to provide your own if needed.
* The default test query is "SELECT now() FROM system.local;".  
* The default keySpace is null.

Example usage for default test query and null keyspace:
```  
import guru.breakthemonolith.health.cassandra.CassandraHealthCheck;

CassandraHealthCheck healthCheck = new CassandraHealthCheck(myCassandraServerName);
myHealthCheckRegistry.register("cassandra", healthCheck);
```  

Example usage specifying test query and keyspace:
```  
import guru.breakthemonolith.health.cassandra.CassandraHealthCheck;

CassandraHealthCheck healthCheck = new CassandraHealthCheck(myTestQuery, myCassandraServerName, myKeySpace);
myHealthCheckRegistry.register("cassandra", healthCheck);
```  
