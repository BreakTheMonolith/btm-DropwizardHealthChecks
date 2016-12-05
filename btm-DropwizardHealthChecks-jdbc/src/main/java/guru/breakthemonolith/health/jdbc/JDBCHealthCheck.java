package guru.breakthemonolith.health.jdbc;

import java.util.Properties;

/**
 * Tests a connection for a registered JDBC data source. This check assumes that
 * the JDBC driver has already been registered by the application hosting the
 * health check. This class is thread safe.
 * 
 * @author D. Ashmore
 *
 */
public class JDBCHealthCheck extends DataSourceHealthCheck {

	public JDBCHealthCheck(String jdbcConnectionURL, String testSqlText) {
		super(new JDBCDataSource(jdbcConnectionURL), testSqlText);
	}

	public JDBCHealthCheck(String jdbcConnectionURL, Properties connectionProps, String testSqlText) {
		super(new JDBCDataSource(jdbcConnectionURL, connectionProps), testSqlText);
	}

}
