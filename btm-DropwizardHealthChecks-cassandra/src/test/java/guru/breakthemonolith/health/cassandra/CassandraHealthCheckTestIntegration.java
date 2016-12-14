package guru.breakthemonolith.health.cassandra;

import java.net.InetAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.breakthemonolith.docker.DockerCommandUtils;
import guru.breakthemonolith.docker.DockerRunSpecification;

public class CassandraHealthCheckTestIntegration {

	private static Logger logger = LoggerFactory.getLogger(CassandraHealthCheckTestIntegration.class);
	private static String cassandraContainerName;

	private CassandraHealthCheck healthCheck;

	@BeforeClass
	public static void setUpClass() throws Exception {
		InetAddress address = InetAddress.getByName("localhost");

		DockerCommandUtils.dockerPull("cassandra:3.9");
		DockerRunSpecification runSpec = new DockerRunSpecification("cassandra:3.9");

		runSpec.setDetachedWaitTimeMillis(60000);
		runSpec.getPortMap().put(address.getHostAddress() + ":9042", "9042");
		runSpec.getPortMap().put(address.getHostAddress() + ":9160", "9160");

		logger.info("Sleeping for 60 secs for Cassandra to start up");
		cassandraContainerName = DockerCommandUtils.dockerRun(runSpec);

		DockerCommandUtils.dockerContainerListing();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		DockerCommandUtils.dockerKillContainer(cassandraContainerName);
		DockerCommandUtils.dockerContainerListing();
	}

	@Before
	public void setUp() throws Exception {
		healthCheck = new CassandraHealthCheck("localhost");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHappyPath() throws Exception {
		Assert.assertTrue(healthCheck.check().isHealthy());
	}

	@Test
	public void testFail() throws Exception {
		healthCheck = new CassandraHealthCheck("google.com");
		Assert.assertFalse(healthCheck.check().isHealthy());
	}

}
