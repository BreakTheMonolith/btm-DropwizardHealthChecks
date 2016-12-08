package guru.breakthemonolith.health.rabbitmq;

import java.net.InetAddress;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck.Result;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import guru.breakthemonolith.docker.DockerCommandUtils;
import guru.breakthemonolith.docker.DockerRunSpecification;

public class RabbitMQHealthCheckTestIntegration {

	private ConnectionFactory connectionFactory;
	private RabbitMQHealthCheck healthcheck;
	private static final String TEST_QUEUE = "testQueue";
	private static final String RABBITMQ_INSIDE_PORT = "5672";
	private static final String RABBITMQ_OUTSIDE_PORT = "6000";
	private static String rabbitMQContainerName;

	@BeforeClass
	public static void setUpClass() throws Exception {

		InetAddress address = InetAddress.getByName("localhost");

		DockerCommandUtils.dockerPull("rabbitmq:latest");

		DockerRunSpecification runSpec = new DockerRunSpecification("rabbitmq:latest");
		runSpec.getPortMap().put(address.getHostAddress() + ":" + RABBITMQ_OUTSIDE_PORT, RABBITMQ_INSIDE_PORT);
		runSpec.setDetachedWaitTimeMillis(5000);

		rabbitMQContainerName = DockerCommandUtils.dockerRun(runSpec);
		DockerCommandUtils.dockerContainerListing();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		DockerCommandUtils.dockerKillContainer(rabbitMQContainerName);
		DockerCommandUtils.dockerContainerListing();
	}

	@Before
	public void setUp() throws Exception {
		connectionFactory = new ConnectionFactory();
		// connectionFactory.setUri("amqp://guest:guest@localhost:" +
		// RABBITMQ_OUTSIDE_PORT + "/");
		connectionFactory.setHost("localhost");
		connectionFactory.setPort(6000);
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		connectionFactory.setVirtualHost("/");

		Connection conn = null;
		Channel channel = null;

		try {
			conn = connectionFactory.newConnection();
			channel = conn.createChannel();
			channel.queueDeclare(TEST_QUEUE, false, false, false, null);
			channel.exchangeDeclare(TEST_QUEUE, "direct");
			channel.queueBind(TEST_QUEUE, TEST_QUEUE, TEST_QUEUE);
		} catch (Exception e) {
			throw new ContextedRuntimeException(e).addContextValue("queueName", TEST_QUEUE)
					.addContextValue("connectionFactory", ToStringBuilder.reflectionToString(connectionFactory));

		}
	}

	@Test
	public void checkFailure() throws Exception {
		healthcheck = new RabbitMQHealthCheck(connectionFactory, "NonExistentQueue");
		Result result = healthcheck.check();
		Assert.assertFalse(result.isHealthy());
	}

	@Test
	public void checkSuccess() throws Exception {
		healthcheck = new RabbitMQHealthCheck(connectionFactory, TEST_QUEUE);
		Result result = healthcheck.check();
		Assert.assertTrue(result.isHealthy());
	}

}
