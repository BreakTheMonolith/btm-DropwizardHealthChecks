# btm-DropwizardHealthChecks-net
Health check for needed Network / Http(s) resources. A good use for this is to check the 
health of dependent services. 

### Installation ###

Maven users can find dependency information [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22%20AND%20a%3A%22btm-DropwizardHealthChecks-net%22).

To install, simply include btm-DropwizardHealthChecks-net.jar and all parent dependencies described [here](../README.md). 
In addition, you need the following dependent libraries:
* org.apache.httpcomponents / httpclient (version 4x or above)
* commons-validator / commons-validator

### HttpHealthCheck ###

Example usage:
```  
import guru.breakthemonolith.health.net.HttpHealthCheck;

HttpHealthCheck healthCheck = new HttpHealthCheck(url, requestTimeoutMillis, headerMap);
myHealthCheckRegistry.register("myDependentMicroservice", healthCheck);
```  

