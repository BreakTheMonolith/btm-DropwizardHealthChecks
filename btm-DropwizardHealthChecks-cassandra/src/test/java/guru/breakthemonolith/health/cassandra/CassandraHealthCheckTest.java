package guru.breakthemonolith.health.cassandra;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
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
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Initializer;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.Session;

@RunWith(MockitoJUnitRunner.class)
public class CassandraHealthCheckTest {

	private static final String TEST_QUERY = "testQuery";
	private static final String TEST_SERVER = "testServer";
	private static final String TEST_KEY_SPACE = "testKeyspace";

	@Mock
	private Logger loggerMock;

	@Mock
	private TestCluster testCluster;

	@Mock
	private Initializer clusterInitializer;

	@Mock
	private Session session;

	private CassandraHealthCheck healthCheck;

	@Before
	public void setUp() throws Exception {
		healthCheck = new CassandraHealthCheck(TEST_SERVER);
		FieldUtils.writeField(healthCheck, "logger", loggerMock, true);

		List<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>();
		addressList.add(new InetSocketAddress("localhost", 16666));
		Mockito.when(clusterInitializer.getContactPoints()).thenReturn(addressList);
		testCluster = new TestCluster(clusterInitializer);
		testCluster.session = session;
	}

	@Test
	public void testCassandraHealthCheck3Args() throws Exception {
		healthCheck = new CassandraHealthCheck(TEST_QUERY, TEST_SERVER, TEST_KEY_SPACE);
		Assert.assertEquals(TEST_QUERY, FieldUtils.readField(healthCheck, "query", true));
		Assert.assertEquals(TEST_SERVER, FieldUtils.readField(healthCheck, "serverName", true));
		Assert.assertEquals(TEST_KEY_SPACE, FieldUtils.readField(healthCheck, "keySpace", true));
	}

	@Test(expected = NullPointerException.class)
	public void testCassandraHealthCheck3ArgsNullQuery() throws Exception {
		new CassandraHealthCheck(null, TEST_SERVER, TEST_KEY_SPACE);
	}

	@Test(expected = NullPointerException.class)
	public void testCassandraHealthCheck3ArgsNullServerName() throws Exception {
		new CassandraHealthCheck(TEST_QUERY, null, TEST_KEY_SPACE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCassandraHealthCheck3ArgsBlankQuery() throws Exception {
		new CassandraHealthCheck("", TEST_SERVER, TEST_KEY_SPACE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCassandraHealthCheck3ArgsBlankServerName() throws Exception {
		new CassandraHealthCheck(TEST_QUERY, "", TEST_KEY_SPACE);
	}

	@Test
	public void testCassandraHealthCheck1Arg() throws Exception {
		Assert.assertEquals(CassandraHealthCheck.DEFAULT_TEST_QUERY, FieldUtils.readField(healthCheck, "query", true));
		Assert.assertEquals(TEST_SERVER, FieldUtils.readField(healthCheck, "serverName", true));
		Assert.assertNull(FieldUtils.readField(healthCheck, "keySpace", true));
	}

	@Test
	public void testCloseClusterQuietly() throws Exception {
		MethodUtils.invokeMethod(healthCheck, true, "closeClusterQuietly", testCluster);
		Assert.assertEquals(1, testCluster.nbrTimesCloseCalled);
	}

	@Test
	public void testCloseQuietlyClusterNull() throws Exception {
		MethodUtils.invokeMethod(healthCheck, true, "closeClusterQuietly", (Cluster) null);
		Assert.assertEquals(0, testCluster.nbrTimesCloseCalled);
	}

	@Test
	public void testCloseClusterQuietlyClusterException() throws Exception {
		testCluster.exceptionToThrow = new RuntimeException("crap");
		MethodUtils.invokeMethod(healthCheck, true, "closeClusterQuietly", testCluster);
		Assert.assertEquals(1, testCluster.nbrTimesCloseCalled);

		ArgumentCaptor<ContextedRuntimeException> exCaptor = ArgumentCaptor.forClass(ContextedRuntimeException.class);
		Mockito.verify(loggerMock).warn(Matchers.anyString(), exCaptor.capture());
		Assert.assertEquals(3, exCaptor.getValue().getContextLabels().size());
	}

	@Test
	public void testCloseSessionQuietly() throws Exception {
		MethodUtils.invokeMethod(healthCheck, true, "closeSessionQuietly", session);
		Mockito.verify(session).close();
	}

	@Test
	public void testCloseSessionQuietlyNull() throws Exception {
		MethodUtils.invokeMethod(healthCheck, true, "closeSessionQuietly", (Session) null);
		Mockito.verify(session, Mockito.never()).close();
	}

	@Test
	public void testCloseSessionQuietlyException() throws Exception {
		Mockito.doThrow(new RuntimeException("crap")).when(session).close();
		MethodUtils.invokeMethod(healthCheck, true, "closeSessionQuietly", session);
		Mockito.verify(session).close();

		ArgumentCaptor<ContextedRuntimeException> exCaptor = ArgumentCaptor.forClass(ContextedRuntimeException.class);
		Mockito.verify(loggerMock).warn(Matchers.anyString(), exCaptor.capture());
		Assert.assertEquals(3, exCaptor.getValue().getContextLabels().size());
	}

	@Test
	public void testCheck() throws Exception {
		TestCassandraHealthCheck testCassandraHealthCheck = new TestCassandraHealthCheck(TEST_SERVER);
		testCassandraHealthCheck.cluster = testCluster;
		Result result = testCassandraHealthCheck.check();

		Assert.assertTrue(result.isHealthy());
		Mockito.verify(session).close();
		Mockito.verify(session).execute(Matchers.anyString());
		Assert.assertEquals(1, testCluster.nbrTimesCloseCalled);
	}

	@Test
	public void testCheckException() throws Exception {
		TestCassandraHealthCheck testCassandraHealthCheck = new TestCassandraHealthCheck(TEST_SERVER);
		testCassandraHealthCheck.cluster = testCluster;
		FieldUtils.writeField(testCassandraHealthCheck, "logger", loggerMock, true);
		Mockito.when(session.execute(Matchers.anyString())).thenThrow(new RuntimeException("crap"));
		Result result = testCassandraHealthCheck.check();

		Assert.assertFalse(result.isHealthy());
		Mockito.verify(session).close();
		Mockito.verify(session).execute(Matchers.anyString());
		Assert.assertEquals(1, testCluster.nbrTimesCloseCalled);

		ArgumentCaptor<ContextedRuntimeException> exCaptor = ArgumentCaptor.forClass(ContextedRuntimeException.class);
		Mockito.verify(loggerMock).error(Matchers.anyString(), exCaptor.capture());
		Assert.assertEquals(3, exCaptor.getValue().getContextLabels().size());
		Assert.assertEquals(result.getError(), exCaptor.getValue());
	}

	static class TestCluster extends Cluster {

		RuntimeException exceptionToThrow;
		int nbrTimesCloseCalled = 0;
		Session session;

		public TestCluster(Initializer initializer) {
			super(initializer);
		}

		public TestCluster(String name, List<InetSocketAddress> contactPoints, Configuration configuration) {
			super(name, contactPoints, configuration);
		}

		@Override
		public void close() {
			nbrTimesCloseCalled++;
			if (exceptionToThrow != null)
				throw exceptionToThrow;
		}

		@Override
		public Session connect(String keyspace) {
			return session;
		}

	}

	static class TestCassandraHealthCheck extends CassandraHealthCheck {

		TestCluster cluster;

		public TestCassandraHealthCheck(String testQuery, String cassandraServerName, String cassandraKeySpace) {
			super(testQuery, cassandraServerName, cassandraKeySpace);
		}

		public TestCassandraHealthCheck(String cassandraServerName) {
			super(cassandraServerName);
		}

		@Override
		protected Cluster createCassandraClient() {
			return cluster;
		}

	}

}
