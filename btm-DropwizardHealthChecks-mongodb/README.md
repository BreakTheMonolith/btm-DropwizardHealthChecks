# btm-DropwizardHealthChecks-mongodb
Provides health check for a Mongo database. 

### Installation

Maven users can find dependency information [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22%20AND%20a%3A%22btm-DropwizardHealthChecks-mongodb%22).

To install, simply include btm-DropwizardHealthChecks-mongodb.jar and all parent dependencies described [here](../README.md). In addition, you need the following dependent libraries:
* org.mongodb / mongodb-driver (version 3x or above)

### MongoDBHealthCheck

Example usage for default test query and null keyspace:
```  
import guru.breakthemonolith.health.mongodb.MongoDbHealthCheck;

MongoDbHealthCheck healthCheck = new MongoDbHealthCheck(myMongoConnectionUrl, myMongoDatabaseName);;
myHealthCheckRegistry.register("mongoDB", healthCheck);
```  
