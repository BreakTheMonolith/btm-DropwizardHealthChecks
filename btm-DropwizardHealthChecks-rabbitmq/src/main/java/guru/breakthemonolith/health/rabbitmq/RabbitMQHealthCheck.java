package guru.breakthemonolith.health.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Health check for RabbitMQ queue access.
 * 
 * @author D. Ashmore
 *
 */
public class RabbitMQHealthCheck extends HealthCheck {

	private static Logger logger = LoggerFactory.getLogger(RabbitMQHealthCheck.class);

	private ConnectionFactory connectionFactory;
	private String queueName;

	public RabbitMQHealthCheck(ConnectionFactory connectionFactory, String queueName) {
		Validate.notBlank(queueName, "Null or blank queueName not allowed");
		Validate.notNull(connectionFactory, "Null connectionFactory not allowed");

		this.queueName = queueName;
		this.connectionFactory = connectionFactory;
	}

	@Override
	protected Result check() throws Exception {
		Connection conn = null;
		Channel channel = null;

		try {
			conn = connectionFactory.newConnection();
			channel = conn.createChannel();
			channel.queueDeclarePassive(queueName);
			return Result.healthy();
		} catch (Exception e) {
			Exception wrappedException = new ContextedRuntimeException(e).addContextValue("queueName", queueName)
					.addContextValue("connectionFactory", ToStringBuilder.reflectionToString(connectionFactory));
			logger.error("Healthcheck Failure", wrappedException);
			return Result.unhealthy(wrappedException);
		} finally {
			closeChannel(channel);
			closeConnection(conn);
		}
	}

	private void closeConnection(Connection conn) throws IOException {
		try {
			if (conn != null && conn.isOpen()) {
				conn.close();
			}
		} catch (Exception e) {
			logger.warn("RabbitMQ connection erred on close",
					new ContextedRuntimeException(e)
							.addContextValue("connectionFactory",
							ToStringBuilder.reflectionToString(connectionFactory)));
		}
	}

	private void closeChannel(Channel channel) throws IOException, TimeoutException {
		try {
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
		} catch (Exception e) {
			logger.warn("RabbitMQ channel erred on close",
					new ContextedRuntimeException(e)
							.addContextValue("queueName", queueName)
							.addContextValue("connectionFactory",
							ToStringBuilder.reflectionToString(connectionFactory)));
		}
	}

}
