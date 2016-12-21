package guru.breakthemonolith.health.mongodb;

import java.net.InetAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import guru.breakthemonolith.docker.DockerCommandUtils;
import guru.breakthemonolith.docker.DockerRunSpecification;

public class MongoDbHealthCheckTestIntegration {

	private static final String MONGO_PORT = "27017";
	private static final String MONGO_IMAGE = "mongo:3.4.0";
	private static String mongoDbContainerName;
	private static String hostWithPort;
	private static String mongoConnectionUrl;

	private MongoDbHealthCheck healthCheck;

	@BeforeClass
	public static void setUpClass() throws Exception {
		InetAddress address = InetAddress.getByName("localhost");
		hostWithPort = address.getHostAddress() + ":" + MONGO_PORT;
		mongoConnectionUrl = "mongodb://" + hostWithPort;

		DockerCommandUtils.dockerPull(MONGO_IMAGE);
		DockerRunSpecification runSpec = new DockerRunSpecification(MONGO_IMAGE);
		runSpec.getPortMap().put(hostWithPort, MONGO_PORT);

		mongoDbContainerName = DockerCommandUtils.dockerRun(runSpec);
		DockerCommandUtils.dockerLogContainer(mongoDbContainerName);
		DockerCommandUtils.dockerContainerListing();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		DockerCommandUtils.dockerKillContainer(mongoDbContainerName);
		DockerCommandUtils.dockerContainerListing();
	}

	@Before
	public void setUp() throws Exception {
		healthCheck = new MongoDbHealthCheck("localhost", "admin");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHappyPath() throws Exception {
		Assert.assertTrue(healthCheck.check().isHealthy());
	}

	@Test
	public void testSadPath() throws Exception {
		healthCheck = new MongoDbHealthCheck("localhost", "crap");
		Assert.assertFalse(healthCheck.check().isHealthy());
	}

}
