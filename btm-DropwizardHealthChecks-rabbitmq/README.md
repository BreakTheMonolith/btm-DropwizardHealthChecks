# btm-DropwizardHealthChecks-rabbitmq
Health check for RabbitMQ queue access. 

### Installation ###

Maven users can find dependency information [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22guru.breakthemonolith%22%20AND%20a%3A%22btm-DropwizardHealthChecks-net%22).

To install, simply include btm-DropwizardHealthChecks-net.jar and all parent dependencies described [here](../README.md). 
In addition, you need the following dependent libraries:
* com.rabbitmq / amqp-client (version 4x or above)

### RabbitMQHealthCheck ###

Example usage:
```  
import guru.breakthemonolith.health.rabbitmq.RabbitMQHealthCheck;

RabbitMQHealthCheck healthCheck = new RabbitMQHealthCheck(myConnectionFactory, myQueueName);
myHealthCheckRegistry.register("myRabbitMqQueue", healthCheck);
```  

