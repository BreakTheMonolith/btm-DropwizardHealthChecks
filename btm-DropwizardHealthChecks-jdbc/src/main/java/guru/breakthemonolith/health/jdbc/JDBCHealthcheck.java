package guru.breakthemonolith.health.jdbc;

import java.util.Properties;

/**
 * Tests a connection for a registered JDBC data source. This check assumes that
 * the JDBC driver has already been registered by the application hosting the
 * health check.
 * 
 * @author D. Ashmore
 *
 */
public class JDBCHealthcheck extends DataSourceHealthcheck {

	public JDBCHealthcheck(String jdbcConnectionURL, String testSqlText) {
		super(new JDBCDataSource(jdbcConnectionURL), testSqlText);
	}

	public JDBCHealthcheck(String jdbcConnectionURL, Properties connectionProps, String testSqlText) {
		super(new JDBCDataSource(jdbcConnectionURL, connectionProps), testSqlText);
	}

}
