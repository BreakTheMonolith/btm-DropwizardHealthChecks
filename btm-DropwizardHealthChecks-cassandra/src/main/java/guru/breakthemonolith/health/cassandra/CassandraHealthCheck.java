package guru.breakthemonolith.health.cassandra;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraHealthCheck extends HealthCheck {

	public static final String DEFAULT_TEST_QUERY = "SELECT now() FROM system.local;";
	private static Logger logger = LoggerFactory.getLogger(CassandraHealthCheck.class);
	private String serverName;
	private String keySpace;
	private String query;

	public CassandraHealthCheck(String cassandraServerName) {
		this(DEFAULT_TEST_QUERY, cassandraServerName, null);
	}

	public CassandraHealthCheck(String testQuery, String cassandraServerName, String cassandraKeySpace) {
		Validate.notBlank(cassandraServerName, "Null or blank cassandraServerName not allowed.");
		Validate.notBlank(testQuery, "Null or blank testQuery not allowed.");
		serverName = cassandraServerName;
		keySpace = cassandraKeySpace;
		query = testQuery;
	}

	@Override
	protected Result check() throws Exception {
		Cluster cassandraClient = null;
		Session cassandraSession = null;

		try {
			cassandraClient = createCassandraClient();
			cassandraSession = cassandraClient.connect(keySpace);
			cassandraSession.execute(query);
		} catch (Exception e) {
			Exception wrappedException = wrapException(e);
			logger.error("Cassandra Healthcheck Failure", wrappedException);
			return Result.unhealthy(wrappedException);
		} finally {
			closeSessionQuietly(cassandraSession);
			closeClusterQuietly(cassandraClient);
		}

		return Result.healthy();
	}

	/**
	 * Protected for unit testing
	 * 
	 * @return
	 */
	protected Cluster createCassandraClient() {
		return Cluster.builder().addContactPoint(serverName).build();
	}

	private Exception wrapException(Exception e) {
		return new ContextedRuntimeException(e)
				.addContextValue("serverName", serverName)
				.addContextValue("keySpace", keySpace)
				.addContextValue("query", query);
	}

	private void closeClusterQuietly(Cluster cassandraCluster) {
		if (cassandraCluster == null)
			return;
		try {
			cassandraCluster.close();
		} catch (Exception e) {
			Exception wrappedException = wrapException(e);
			logger.warn("Error closing Cassandra cluster", wrappedException);
		}
	}

	private void closeSessionQuietly(Session cassandraSession) {
		if (cassandraSession == null)
			return;
		try {
			cassandraSession.close();
		} catch (Exception e) {
			Exception wrappedException = wrapException(e);
			logger.warn("Error closing Cassandra session", wrappedException);
		}
	}

}
