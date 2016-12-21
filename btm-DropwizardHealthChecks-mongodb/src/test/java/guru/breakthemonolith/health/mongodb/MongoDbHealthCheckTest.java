package guru.breakthemonolith.health.mongodb;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.codahale.metrics.health.HealthCheck.Result;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

@RunWith(MockitoJUnitRunner.class)
public class MongoDbHealthCheckTest {

	private static final String TEST_CONNECT_URL = "testConnectUrl";
	private static final String TEST_DATABASE = "testDatabase";
	private static final RuntimeException TEST_EXCEPTION = new RuntimeException("crap");

	@Mock
	private MongoClient mongoClientMock;

	@Mock
	private CommandResult commandResult;

	@Mock
	private DB db;

	@Mock
	private Logger loggerMock;

	@Mock
	private MongoIterable<String> mongoIterableMock;

	@Mock
	private MongoCursor cursorMock;

	private MongoDbHealthCheck healthCheck;

	@Before
	public void setUp() throws Exception {
		healthCheck = new MongoDbHealthCheck(TEST_CONNECT_URL, TEST_DATABASE);
		FieldUtils.writeField(healthCheck, "logger", loggerMock, true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructor() throws Exception {
		Assert.assertEquals(TEST_CONNECT_URL, FieldUtils.readField(healthCheck, "connectionUrl", true));
		Assert.assertEquals(TEST_DATABASE, FieldUtils.readField(healthCheck, "databaseName", true));
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorNullUrl() throws Exception {
		new MongoDbHealthCheck(null, TEST_DATABASE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorBlankUrl() throws Exception {
		new MongoDbHealthCheck("", TEST_DATABASE);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorNullDatabase() throws Exception {
		new MongoDbHealthCheck(TEST_CONNECT_URL, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorBlankDatabase() throws Exception {
		new MongoDbHealthCheck(TEST_CONNECT_URL, "");
	}

	@Test
	public void testCheck() throws Exception {
		TestMongoDbHealthCheck healthCheck = setTpcheckMocks();
		Mockito.when(commandResult.get(Matchers.anyString())).thenReturn(Integer.valueOf(1));
		Result result = healthCheck.check();

		Mockito.verify(loggerMock).debug("connectionUrl={} databaseList={} stats={}", TEST_CONNECT_URL, "",
				"commandResult");
		Mockito.verify(mongoClientMock).close();
		Assert.assertTrue(result.isHealthy());
	}

	@Test
	public void testCheckInvalidDatabase() throws Exception {
		TestMongoDbHealthCheck healthCheck = setTpcheckMocks();
		Mockito.when(commandResult.get(Matchers.anyString())).thenReturn(Integer.valueOf(0));
		Result result = healthCheck.check();

		Mockito.verify(loggerMock).debug("connectionUrl={} databaseList={} stats={}", TEST_CONNECT_URL, "",
				"commandResult");
		Mockito.verify(mongoClientMock).close();
		Assert.assertFalse(result.isHealthy());

		ArgumentCaptor<ContextedRuntimeException> exCaptor = ArgumentCaptor.forClass(ContextedRuntimeException.class);
		Mockito.verify(loggerMock).error(Matchers.anyString(), exCaptor.capture());
		Assert.assertEquals(4, exCaptor.getValue().getContextLabels().size());
		Assert.assertEquals("Database has nothing in it.", exCaptor.getValue().getCause().getMessage());
	}

	private TestMongoDbHealthCheck setTpcheckMocks() throws IllegalAccessException {
		TestMongoDbHealthCheck healthCheck = new TestMongoDbHealthCheck(TEST_CONNECT_URL, TEST_DATABASE);
		healthCheck.mongoClient = mongoClientMock;
		FieldUtils.writeField(healthCheck, "logger", loggerMock, true);

		Mockito.when(mongoClientMock.getDB(Matchers.anyString())).thenReturn(db);
		Mockito.when(mongoClientMock.listDatabaseNames()).thenReturn(mongoIterableMock);
		Mockito.when(mongoIterableMock.iterator()).thenReturn(cursorMock);
		Mockito.when(db.getStats()).thenReturn(commandResult);
		return healthCheck;
	}

	@Test
	public void testCreateMongoClient() throws Exception {
		Assert.assertNotNull(healthCheck.createMongoClient());
	}

	@Test
	public void testCloseQuietly() throws Exception {
		TestMongoClient mongoClient = new TestMongoClient();
		MethodUtils.invokeMethod(healthCheck, true, "closeQuietly", mongoClient);
		Assert.assertEquals(1, mongoClient.nbrTimesClose);
	}

	@Test
	public void testCloseQuietlyNull() throws Exception {
		MethodUtils.invokeMethod(healthCheck, true, "closeQuietly", (MongoClient) null);
	}

	@Test
	public void testCloseQuietlyExcepting() throws Exception {
		TestMongoClient mongoClient = new TestMongoClient();
		mongoClient.exceptionToThrow = TEST_EXCEPTION;
		MethodUtils.invokeMethod(healthCheck, true, "closeQuietly", mongoClient);

		ArgumentCaptor<ContextedRuntimeException> exCaptor = ArgumentCaptor.forClass(ContextedRuntimeException.class);
		Mockito.verify(loggerMock).warn(Matchers.anyString(), exCaptor.capture());
		Assert.assertEquals(2, exCaptor.getValue().getContextLabels().size());
		Assert.assertEquals(mongoClient.exceptionToThrow, exCaptor.getValue().getCause());
	}

	@Test
	public void testWrapException() throws Exception {
		ContextedRuntimeException wrapped = (ContextedRuntimeException) MethodUtils.invokeMethod(healthCheck, true,
				"wrapException", TEST_EXCEPTION);
		Assert.assertEquals(TEST_EXCEPTION, wrapped.getCause());
		Assert.assertEquals(2, wrapped.getContextEntries().size());
		Assert.assertEquals(TEST_CONNECT_URL, wrapped.getContextValues("connectionUrl").get(0));
		Assert.assertEquals(TEST_DATABASE, wrapped.getContextValues("databaseName").get(0));
	}

	public static class TestMongoClient extends MongoClient {

		private int nbrTimesClose = 0;
		private RuntimeException exceptionToThrow = null;
		private MongoIterable<String> mongoIterable = null;

		@Override
		public void close() {
			nbrTimesClose++;
			if (exceptionToThrow != null) {
				throw exceptionToThrow;
			}
		}

		@Override
		public MongoIterable<String> listDatabaseNames() {
			return mongoIterable;
		}

	}

	public static class TestMongoDbHealthCheck extends MongoDbHealthCheck {

		private MongoClient mongoClient = null;

		public TestMongoDbHealthCheck(String dbConnectionUrl, String dbName) {
			super(dbConnectionUrl, dbName);
		}

		@Override
		protected MongoClient createMongoClient() {
			return mongoClient;
		}

	}

}
