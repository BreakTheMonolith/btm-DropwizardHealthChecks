package guru.breakthemonolith.health.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;

/**
 * Simple JDBC query test.
 * 
 * <p>
 * Inputs required for this health check are the following:
 * </p>
 * <li>jdbcConnectionURL - Required. Pre-configured and able to provide JDBC
 * connections (See DriverManager link below).</li>
 * <li>connectionProps - Optional (default none). Connection properties if
 * needed by the database JDBC Driver you're using.</li>
 * 
 * @author D. Ashmore
 * @see <a href=
 *      "https://docs.oracle.com/javase/7/docs/api/java/sql/DriverManager.html">DriverManager</a>
 *
 */
class JDBCDataSource implements DataSource {

	private String jdbcConnectionURL;
	private Properties connectionProps;

	public JDBCDataSource(String jdbcConnectionURL) {
		this(jdbcConnectionURL, null);
	}

	public JDBCDataSource(String jdbcConnectionURL, Properties connectionProps) {
		Validate.notBlank(jdbcConnectionURL, "Null or blank jdbcConnectionURL is not allowed.");
		this.jdbcConnectionURL = jdbcConnectionURL;
		this.connectionProps = connectionProps;
	}

	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");

	}

	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");

	}

	public int getLoginTimeout() throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new UnsupportedOperationException("Feature not implemented");
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcConnectionURL, connectionProps);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException("Feature not implemented");
	}

}
