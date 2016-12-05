# btm-DropwizardHealthChecks-net
Health check for needed Network / Http(s) resources. A good use for this is to check the 
health of dependent services. 

### Installation ###

A Maven option is forthcoming.

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

