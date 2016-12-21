package guru.breakthemonolith.health.mongodb;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.CommandResult;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoIterable;

/**
 * Checks the health of a MongoDB database. This class is thread safe.
 * 
 * <p>
 * Inputs required for this health check are the following:
 * </p>
 * <li>dbConnectionUrl - Required. Full mongoDB url. Example is
 * "mongodb://localhost:27017"</li>
 * <li>dbName - Required. MongoDB database name containing one or more
 * collections.</li>
 * 
 * @author D. Ashmore
 *
 */
public class MongoDbHealthCheck extends HealthCheck {

	private static Logger logger = LoggerFactory.getLogger(MongoDbHealthCheck.class);
	private String connectionUrl;
	private String databaseName;

	public MongoDbHealthCheck(String dbConnectionUrl, String dbName) {
		Validate.notBlank(dbConnectionUrl, "Null or blank dbConnectionUrl not allowed.");
		Validate.notBlank(dbName, "Null or blank dbName not allowed.");
		connectionUrl = dbConnectionUrl;
		databaseName = dbName;
	}

	@Override
	protected Result check() throws Exception {
		MongoClient mongoClient = null;
		String databaseList = null;
		String databaseStats = null;

		try {
			mongoClient = createMongoClient();
			MongoIterable<String> dbList = mongoClient.listDatabaseNames();
			databaseList = StringUtils.join(dbList, ',');

			CommandResult resultSet = mongoClient.getDB(databaseName).getStats();
			databaseStats = resultSet.toString();
			logger.debug("connectionUrl={} databaseList={} stats={}", connectionUrl, databaseList, databaseStats);

			Integer nbrCollections = (Integer) resultSet.get("collections");
			if (nbrCollections == 0) {
				throw new RuntimeException("Database has nothing in it.");
			}
		} catch (Exception e) {
			ContextedRuntimeException wrappedException = wrapException(e);
			wrappedException.addContextValue("databaseList", databaseList);
			wrappedException.addContextValue("databaseStats", databaseStats);

			logger.error("MongoDB Healthcheck Failure", wrappedException);
			return Result.unhealthy(wrappedException);
		} finally {
			closeQuietly(mongoClient);
		}

		return Result.healthy();
	}

	protected MongoClient createMongoClient() {
		return new MongoClient(connectionUrl);
	}

	private void closeQuietly(MongoClient mongoClient) {
		if (mongoClient != null) {
			try {
				mongoClient.close();
			} catch (Exception e) {
				Exception wrappedException = wrapException(e);
				logger.warn("Error closing MongoDB client", wrappedException);
			}
		}
	}

	private ContextedRuntimeException wrapException(Exception e) {
		return new ContextedRuntimeException(e)
				.addContextValue("connectionUrl", connectionUrl)
				.addContextValue("databaseName", databaseName);
	}

}
