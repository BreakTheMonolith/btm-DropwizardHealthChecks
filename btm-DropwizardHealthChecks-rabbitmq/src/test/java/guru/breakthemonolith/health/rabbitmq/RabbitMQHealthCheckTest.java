package guru.breakthemonolith.health.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.codahale.metrics.health.HealthCheck.Result;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMQHealthCheckTest {

	private static final String TEST_QUEUE = "testQueue";
	private static final Exception TEST_EXCEPTION = new RuntimeException("crap");

	@Mock
	private ConnectionFactory connectionFactoryMock;

	@Mock
	private Connection connectionMock;

	@Mock
	private Channel channelMock;

	@Mock
	private Logger loggerMock;

	private RabbitMQHealthCheck healthCheck;

	@Before
	public void setUp() throws Exception {
		healthCheck = new RabbitMQHealthCheck(connectionFactoryMock, TEST_QUEUE);
		FieldUtils.writeField(healthCheck, "logger", loggerMock, true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void constructorAllArgsProvided() throws Exception {
		String queueName = (String) FieldUtils.readField(healthCheck, "queueName", true);
		ConnectionFactory connectionFactory = (ConnectionFactory) FieldUtils.readField(healthCheck, "connectionFactory",
				true);

		Assert.assertEquals(connectionFactoryMock, connectionFactory);
		Assert.assertEquals(TEST_QUEUE, queueName);
	}

	@Test(expected = NullPointerException.class)
	public void constructorMissingConnectionFactory() throws Exception {
		new RabbitMQHealthCheck(null, TEST_QUEUE);
	}

	@Test(expected = NullPointerException.class)
	public void constructorNullQueueName() throws Exception {
		new RabbitMQHealthCheck(connectionFactoryMock, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorBlankQueueName() throws Exception {
		new RabbitMQHealthCheck(connectionFactoryMock, "");
	}

	@Test
	public void checkHappyPath() throws Exception {
		mockSetup();

		Result result = healthCheck.check();
		Assert.assertTrue(result.isHealthy());

		Mockito.verify(connectionMock).close();
		Mockito.verify(channelMock).close();
		Mockito.verify(channelMock).queueDeclarePassive(TEST_QUEUE);
	}

	private void mockSetup() throws IOException, TimeoutException {
		Mockito.when(connectionFactoryMock.newConnection()).thenReturn(connectionMock);
		Mockito.when(connectionMock.createChannel()).thenReturn(channelMock);
		Mockito.when(connectionMock.isOpen()).thenReturn(true);
		Mockito.when(channelMock.isOpen()).thenReturn(true);
	}

	@Test
	public void checkNonexistentQueue() throws Exception {
		mockSetup();
		Mockito.when(channelMock.queueDeclarePassive(TEST_QUEUE)).thenThrow(TEST_EXCEPTION);

		Result result = healthCheck.check();
		Assert.assertFalse(result.isHealthy());
		Mockito.verify(loggerMock).error(Matchers.anyString(), Matchers.any(ContextedRuntimeException.class));
	}

	@Test
	public void checkChannelCloseError() throws Exception {
		mockSetup();
		Mockito.doThrow(TEST_EXCEPTION).when(channelMock).close();
		healthCheck.check();
		Mockito.verify(loggerMock).warn(Matchers.anyString(), Matchers.any(ContextedRuntimeException.class));
	}

	@Test
	public void checkConnectionCloseError() throws Exception {
		mockSetup();
		Mockito.doThrow(TEST_EXCEPTION).when(connectionMock).close();
		healthCheck.check();
		Mockito.verify(loggerMock).warn(Matchers.anyString(), Matchers.any(ContextedRuntimeException.class));
	}

}
