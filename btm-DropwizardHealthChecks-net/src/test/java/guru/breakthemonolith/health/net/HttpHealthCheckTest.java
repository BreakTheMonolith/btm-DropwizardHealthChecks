package guru.breakthemonolith.health.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.codahale.metrics.health.HealthCheck;

@RunWith(MockitoJUnitRunner.class)
public class HttpHealthCheckTest {

	private HttpHealthCheck healthCheck;
	private HttpHealthCheck healthCheck3Args;
	private static final String TEST_URL = "http://www.google.com";
	private static final int TEST_TIMEOUT = 10;
	private static final Map<String, String> TEST_HEADERS = new HashMap<String, String>();

	@Mock
	private Logger loggerMock;

	@Before
	public void setUp() throws Exception {
		healthCheck = new HttpHealthCheck(TEST_URL);
		FieldUtils.writeField(healthCheck, "logger", loggerMock, true);

		makeCheck3Args(TEST_URL, TEST_TIMEOUT, TEST_HEADERS);
	}

	private void makeCheck3Args(String url, int requestTimeoutMillis, Map<String, String> headerMap)
			throws IllegalAccessException {
		healthCheck3Args = new HttpHealthCheck(url, requestTimeoutMillis, headerMap);
		FieldUtils.writeField(healthCheck3Args, "logger", loggerMock, true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHttpHealthCheckString1Arg() throws Exception {
		String url = (String) FieldUtils.readField(healthCheck, "checkUrl", true);
		Integer timeout = (Integer) FieldUtils.readField(healthCheck, "requestTimeoutMillis", true);
		Map<String, String> headerMap = (Map<String, String>) FieldUtils.readField(healthCheck, "headerMap", true);
		UrlValidator validator = (UrlValidator) FieldUtils.readField(healthCheck, "validator", true);

		Assert.assertEquals(TEST_URL, url);
		Assert.assertEquals(HttpHealthCheck.DEFAULT_REQUEST_TIMEOUT_MILLIS, timeout.intValue());
		Assert.assertTrue(headerMap != null);
		Assert.assertTrue(validator != null);
	}

	@Test
	public void testHttpHealthCheckString3Args() throws Exception {
		String url = (String) FieldUtils.readField(healthCheck3Args, "checkUrl", true);
		Integer timeout = (Integer) FieldUtils.readField(healthCheck3Args, "requestTimeoutMillis", true);
		Map<String, String> headerMap = (Map<String, String>) FieldUtils.readField(healthCheck3Args, "headerMap", true);
		UrlValidator validator = (UrlValidator) FieldUtils.readField(healthCheck3Args, "validator", true);

		Assert.assertEquals(TEST_URL, url);
		Assert.assertEquals(TEST_TIMEOUT, timeout.intValue());
		Assert.assertEquals(TEST_HEADERS, headerMap);
		Assert.assertTrue(validator != null);
	}

	@Test(expected = NullPointerException.class)
	public void testHttpHealthCheckString3ArgsNullUrl() throws Exception {
		new HttpHealthCheck(null, TEST_TIMEOUT, TEST_HEADERS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpHealthCheckString3ArgsBlankUrl() throws Exception {
		new HttpHealthCheck("", TEST_TIMEOUT, TEST_HEADERS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpHealthCheckString3ArgsInvalidUrl() throws Exception {
		new HttpHealthCheck("crap", TEST_TIMEOUT, TEST_HEADERS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpHealthCheckString3ArgsInvalidTimeoutPredicate1() throws Exception {
		new HttpHealthCheck(TEST_URL, -2, TEST_HEADERS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpHealthCheckString3ArgsInvalidTimeoutPredicate2() throws Exception {
		new HttpHealthCheck(TEST_URL, 0, TEST_HEADERS);
	}

	@Test
	public void testIsStatusCodeValid() throws Exception {
		Assert.assertTrue(healthCheck.isStatusCodeValid(200));
		Assert.assertTrue(healthCheck.isStatusCodeValid(201));
		Assert.assertTrue(healthCheck.isStatusCodeValid(299));
		Assert.assertFalse(healthCheck.isStatusCodeValid(199));
		Assert.assertFalse(healthCheck.isStatusCodeValid(300));
	}

	@Test
	public void testCreateRequestConfig() throws Exception {
		Assert.assertEquals(TEST_TIMEOUT, healthCheck3Args.createRequestConfig().getConnectionRequestTimeout());
		Assert.assertEquals(RequestConfig.DEFAULT.getConnectTimeout(),
				healthCheck3Args.createRequestConfig().getConnectTimeout());
	}

	@Test
	public void testCreateCloseableHttpClient() throws Exception {
		CloseableHttpClient client = healthCheck3Args.createCloseableHttpClient();
		RequestConfig config = (RequestConfig) FieldUtils.readField(client, "defaultConfig", true);
		Assert.assertEquals(TEST_TIMEOUT, config.getConnectionRequestTimeout());

		client.close();
	}

	@Test
	public void testCreateUrlValidator() throws Exception {
		Assert.assertNotNull(healthCheck3Args.createUrlValidator());
	}

	@Test
	public void testCreateRequest() throws Exception {
		HttpGet get = (HttpGet) healthCheck3Args.createRequest(TEST_URL);
		Assert.assertEquals(TEST_URL, get.getURI().toString());
	}

	@Test
	public void testCheckHappyPath() throws Exception {
		Assert.assertTrue(healthCheck.check().isHealthy());
	}

	@Test
	public void testCheckUrlNotFound() throws Exception {
		Map<String, String> headers = new HashMap<String, String>(TEST_HEADERS);
		headers.put("fu", "bar");
		makeCheck3Args(TEST_URL + "/crap.html", TEST_TIMEOUT, headers);

		HealthCheck.Result result = healthCheck3Args.check();
		// System.out.println(result.getMessage());

		Assert.assertFalse(result.isHealthy());
		Assert.assertTrue(result.getMessage().contains("404"));
		Assert.assertTrue(result.getMessage().contains("fu"));
		Assert.assertTrue(result.getMessage().contains("bar"));
	}

	@Test
	public void testCheckUrlNonExistent() throws Exception {
		makeCheck3Args(TEST_URL, TEST_TIMEOUT, TEST_HEADERS);
		FieldUtils.writeField(healthCheck3Args, "checkUrl", "invalidUrl", true);

		HealthCheck.Result result = healthCheck3Args.check();
		// System.out.println(result.getMessage());

		Mockito.verify(loggerMock).error(Matchers.anyString(), Matchers.any(ContextedRuntimeException.class));
		Assert.assertTrue(result.getMessage().contains("invalidUrl"));
		Assert.assertTrue(result.getMessage().contains("10"));
		Assert.assertTrue(result.getMessage().contains("headerMap=[]"));
		Assert.assertFalse(result.isHealthy());
	}

	@Test
	public void testCheckUrlNonExistentNullHeaders() throws Exception {
		makeCheck3Args(TEST_URL, TEST_TIMEOUT, null);
		FieldUtils.writeField(healthCheck3Args, "checkUrl", "invalidUrl", true);

		HealthCheck.Result result = healthCheck3Args.check();
		// System.out.println(result.getMessage());
		Assert.assertTrue(result.getMessage().contains("headerMap=null"));
	}

}
