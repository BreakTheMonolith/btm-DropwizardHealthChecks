package guru.breakthemonolith.health.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
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

@RunWith(MockitoJUnitRunner.class)
public class DataSourceHealthcheckTest {

	@Mock
	private DataSource dataSourceMock;

	@Mock
	private Connection connectionMock;

	@Mock
	private Statement statementMock;

	@Mock
	private ResultSet resultSetMock;

	@Mock
	private Logger loggerMock;

	private DataSourceHealthcheck healthCheck;

	private static final String TEST_QUERY = "select 1";
	private static final Exception TEST_EXCEPTION = new SQLException("crap");

	@Before
	public void setUp() throws Exception {
		healthCheck = new DataSourceHealthcheck(dataSourceMock, TEST_QUERY);
		FieldUtils.writeField(healthCheck, "logger", loggerMock, true);
	}

	@Test
	public void constructorAllArgsProvided() throws Exception {
		DataSource dSource = (DataSource) FieldUtils.readField(healthCheck, "dataSource", true);
		String query = (String) FieldUtils.readField(healthCheck, "testSqlText", true);

		Assert.assertEquals(dataSourceMock, dSource);
		Assert.assertEquals(TEST_QUERY, query);
	}

	@Test(expected = NullPointerException.class)
	public void constructorMissingDataSource() throws Exception {
		new DataSourceHealthcheck(null, TEST_QUERY);
	}

	@Test(expected = NullPointerException.class)
	public void constructorMissingTestSQL() throws Exception {
		new DataSourceHealthcheck(dataSourceMock, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorBlankTestSQL() throws Exception {
		new DataSourceHealthcheck(dataSourceMock, "");
	}

	@Test
	public void checkHappyPath() throws Exception {
		mockSetup();

		Result result = healthCheck.check();
		Assert.assertTrue(result.isHealthy());

		Mockito.verify(connectionMock).close();
		Mockito.verify(statementMock).close();
		Mockito.verify(resultSetMock).close();
	}

	private void mockSetup() throws SQLException {
		Mockito.when(dataSourceMock.getConnection()).thenReturn(connectionMock);
		Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
		Mockito.when(statementMock.executeQuery(TEST_QUERY)).thenReturn(resultSetMock);
	}

	@Test
	public void checkQueryExecutionFailure() throws Exception {

		Mockito.when(dataSourceMock.getConnection()).thenReturn(connectionMock);
		Mockito.when(connectionMock.createStatement()).thenReturn(statementMock);
		Mockito.when(statementMock.executeQuery(TEST_QUERY)).thenThrow(TEST_EXCEPTION);

		Result result = healthCheck.check();
		Assert.assertTrue(!result.isHealthy());
		Assert.assertEquals(TEST_EXCEPTION, result.getError());

		Mockito.verify(loggerMock).error(Matchers.anyString(), Matchers.any(ContextedRuntimeException.class));
		Mockito.verify(connectionMock).close();
		Mockito.verify(statementMock).close();
	}

	@Test
	public void checkCloseFailure() throws Exception {
		mockSetup();
		Mockito.doThrow(TEST_EXCEPTION).when(connectionMock).close();

		Result result = healthCheck.check();
		Assert.assertTrue(result.isHealthy());

		Mockito.verify(loggerMock).warn(Matchers.anyString(), Matchers.any(ContextedRuntimeException.class));
	}

}
