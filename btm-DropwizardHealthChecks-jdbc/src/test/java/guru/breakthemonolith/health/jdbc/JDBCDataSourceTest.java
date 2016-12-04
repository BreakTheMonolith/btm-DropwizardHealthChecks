package guru.breakthemonolith.health.jdbc;

import java.util.Properties;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JDBCDataSourceTest {

	private JDBCDataSource dataSourceNoProps;
	private JDBCDataSource dataSourceWithProps;

	private static final String TEST_JDBC_CONNECTION_URL = "jdbc:h2:mem:testdb";
	private static final Properties TEST_PROPERTIES = new Properties();

	@Before
	public void setUp() throws Exception {
		dataSourceNoProps = new JDBCDataSource(TEST_JDBC_CONNECTION_URL);
		dataSourceWithProps = new JDBCDataSource(TEST_JDBC_CONNECTION_URL, TEST_PROPERTIES);
	}

	@Test
	public void constructorNoPropsAllArgsProvided() throws Exception {
		String jdbcConnectionURL = (String) FieldUtils.readField(dataSourceNoProps, "jdbcConnectionURL", true);
		Assert.assertEquals(TEST_JDBC_CONNECTION_URL, jdbcConnectionURL);
	}

	@Test(expected = NullPointerException.class)
	public void constructorNoPropsMissingJDBCConnectionUrl() throws Exception {
		new JDBCDataSource(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNoPropsBlankJDBCConnectionUrl() throws Exception {
		new JDBCDataSource("");
	}

	@Test
	public void constructorWithPropsAllArgsProvided() throws Exception {
		String jdbcConnectionURL = (String) FieldUtils.readField(dataSourceWithProps, "jdbcConnectionURL", true);
		Properties jdbcProps = (Properties) FieldUtils.readField(dataSourceWithProps, "connectionProps", true);
		Assert.assertEquals(TEST_JDBC_CONNECTION_URL, jdbcConnectionURL);
		Assert.assertEquals(TEST_PROPERTIES, jdbcProps);
	}

	@Test
	public void getConnection() throws Exception {
		Assert.assertNotNull(dataSourceNoProps.getConnection());
	}

	@Test(expected = NullPointerException.class)
	public void constructorWithPropsMissingJDBCConnectionUrl() throws Exception {
		new JDBCDataSource(null, TEST_PROPERTIES);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorWithPropsBlankJDBCConnectionUrl() throws Exception {
		new JDBCDataSource("", TEST_PROPERTIES);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getLogWriter() throws Exception {
		dataSourceNoProps.getLogWriter();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setLogWriter() throws Exception {
		dataSourceNoProps.setLogWriter(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setLoginTimeout() throws Exception {
		dataSourceNoProps.setLoginTimeout(1);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getLoginTimeout() throws Exception {
		dataSourceNoProps.getLoginTimeout();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getParentLogger() throws Exception {
		dataSourceNoProps.getParentLogger();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void unwrap() throws Exception {
		dataSourceNoProps.unwrap(String.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void isWrapperFor() throws Exception {
		dataSourceNoProps.isWrapperFor(String.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getConnectionUnusedOverload() throws Exception {
		dataSourceNoProps.getConnection("fu", "bar");
	}

}
