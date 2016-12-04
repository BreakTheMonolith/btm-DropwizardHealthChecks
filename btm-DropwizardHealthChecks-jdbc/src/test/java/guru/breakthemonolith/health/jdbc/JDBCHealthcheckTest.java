package guru.breakthemonolith.health.jdbc;

import java.util.Properties;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;

public class JDBCHealthcheckTest {

	private static final String TEST_JDBC_CONNECTION_URL = "jdbc:h2:mem:testdb";
	private static final Properties TEST_PROPERTIES = new Properties();
	private static final String TEST_QUERY = "select 1";

	private JDBCHealthcheck healthCheck;

	@Test
	public void constructorTwoArgs() throws Exception {
		healthCheck = new JDBCHealthcheck(TEST_JDBC_CONNECTION_URL, TEST_QUERY);
		validateTwoArgs();
	}

	@Test
	public void constructorThreeArgs() throws Exception {
		healthCheck = new JDBCHealthcheck(TEST_JDBC_CONNECTION_URL, TEST_PROPERTIES, TEST_QUERY);
		validateTwoArgs();

		Properties jdbcProps = (Properties) FieldUtils.readField(findJDBCDataSource(), "connectionProps", true);
		Assert.assertEquals(TEST_PROPERTIES, jdbcProps);
	}

	private void validateTwoArgs() throws IllegalAccessException {
		JDBCDataSource jdbcDataSource = findJDBCDataSource();
		String jdbcConnectionURL = (String) FieldUtils.readField(jdbcDataSource, "jdbcConnectionURL", true);
		String query = (String) FieldUtils.readField(healthCheck, "testSqlText", true);

		Assert.assertEquals(TEST_JDBC_CONNECTION_URL, jdbcConnectionURL);
		Assert.assertEquals(TEST_QUERY, query);
	}

	private JDBCDataSource findJDBCDataSource() throws IllegalAccessException {
		return (JDBCDataSource) FieldUtils.readField(healthCheck, "dataSource", true);
	}

}
