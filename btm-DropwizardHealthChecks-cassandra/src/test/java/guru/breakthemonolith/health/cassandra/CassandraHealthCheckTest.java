package guru.breakthemonolith.health.cassandra;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CassandraHealthCheckTest {

	private static final String TEST_QUERY = "testQuery";
	private static final String TEST_SERVER = "testServer";
	private static final String TEST_KEY_SPACE = "testKeyspace";

	@Mock
	private Logger loggerMock;

	private CassandraHealthCheck healthCheck;

	@Before
	public void setUp() throws Exception {
		healthCheck = new CassandraHealthCheck(TEST_SERVER);
	}

	@After
	public void tearDown() throws Exception {
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

}
