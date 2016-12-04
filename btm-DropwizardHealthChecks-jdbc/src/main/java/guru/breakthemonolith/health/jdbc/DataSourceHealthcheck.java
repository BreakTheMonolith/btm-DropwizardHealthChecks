package guru.breakthemonolith.health.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

/**
 * Checks the health of a JDBC DataSource
 * 
 * <p>
 * Inputs required for this health check are the following:
 * </p>
 * <li>dataSource - Required. Pre-configured and able to provide JDBC
 * connections</li>
 * <li>testSqlText - Required. Lightweight query that can safely be executed
 * without performance impact to your application (see below)</li>
 * 
 * <p>
 * Example Test SQL Queries for popular databases
 * </p>
 * <li>MySQL - <code>SELECT 1</code></li>
 * <li>PostgreSQL - <code>SELECT 1</code></li>
 * <li>Microsoft SQL Server - <code>SELECT 1</code></li>
 * <li>Oracle - <code>SELECT 1 FROM DUAL</code></li>
 * <li>H2 - <code>SELECT 1</code></li>
 * 
 * @author D. Ashmore
 * @see <a href=
 *      "https://docs.oracle.com/javase/7/docs/api/javax/sql/DataSource.html">DataSource</a>
 * @see <a href=
 *      "https://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html">Connection</a>
 */
public class DataSourceHealthcheck extends HealthCheck {

	// Not 'final' so it can be injected for testing
	private static Logger logger = LoggerFactory.getLogger(DataSourceHealthcheck.class);

	private DataSource dataSource;
	private String testSqlText;

	public DataSourceHealthcheck(DataSource dataSource, String testSqlText) {
		Validate.notNull(dataSource, "Null dataSource is not allowed.");
		Validate.notBlank(testSqlText, "Null or blank testSqlText is not allowed.");
		this.dataSource = dataSource;
		this.testSqlText = testSqlText;
	}

	@Override
	protected Result check() throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			rSet = stmt.executeQuery(testSqlText);
			safeClose(rSet);
		} catch (Exception e) {
			logger.error("Healthcheck Failure",
					new ContextedRuntimeException(e).addContextValue("datasource", dataSource));
			return Result.unhealthy(e);

		} finally {
			safeClose(stmt);
			safeClose(conn);
		}
		return Result.healthy();
	}

	private void safeClose(AutoCloseable statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (Exception e) {
			logger.warn("JDBC Statement erred on close",
					new ContextedRuntimeException(e).addContextValue("datasource", dataSource));
		}
	}

}
