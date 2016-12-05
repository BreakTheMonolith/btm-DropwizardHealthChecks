package guru.breakthemonolith.health.net;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

/**
 * Health check for needed Http(s) resources. A good use for this is to check
 * the health of dependent services. This class is thread safe.
 * 
 * <p>
 * Behavior of this health check is as follows:
 * </p>
 * <li>By default, an HTTP response of between 200-299 is considered healthy.
 * Anything else is unhealthy. Override isStatisCodeValid() if something else is
 * wanted.</li>
 * <li>GET http(s) requests are assumed. Override createRequest() if something
 * else is wanted.</li>
 * 
 * <p>
 * Inputs required for this health check are the following:
 * </p>
 * <li>url - Required. Full url including protocol (e.g.
 * http://www.google.com).</li>
 * <li>headerMap - Optional. String/String map for header settings (e.g.
 * security headers).</li>
 * <li>headerMap - Optional. String/String map for header settings (e.g.
 * security headers).</li>
 * 
 * @author D. Ashmore
 *
 */
public class HttpHealthCheck extends HealthCheck {

	public static final int DEFAULT_REQUEST_TIMEOUT_MILLIS = 30000;
	private static Logger logger = LoggerFactory.getLogger(HttpHealthCheck.class);

	private String checkUrl;
	private Map<String, String> headerMap;
	private int requestTimeoutMillis;
	private UrlValidator validator;

	public HttpHealthCheck(String url) {
		this(url, DEFAULT_REQUEST_TIMEOUT_MILLIS, new HashMap<String, String>());
	}

	public HttpHealthCheck(String url, int requestTimeoutMillis, Map<String, String> headerMap) {
		validator = this.createUrlValidator();

		Validate.notBlank(url, "Null or blank url not allowed");
		Validate.isTrue(validator.isValid(url), "Url provided not well-formed. url=%s", url);
		Validate.isTrue(requestTimeoutMillis == -1 || requestTimeoutMillis > 0,
				"requestTimeoutMillis must be -1 or larger than 0. requestTimeoutMillis=%s", requestTimeoutMillis);
		this.headerMap = headerMap;
		this.checkUrl = url;
		this.requestTimeoutMillis = requestTimeoutMillis;
	}

	@Override
	protected Result check() throws Exception {
		try {
			return localCheck();
		} catch (Exception e) {
			Exception wrappedException = new ContextedRuntimeException(e).addContextValue("checkUrl", checkUrl)
					.addContextValue("requestTimeoutMillis", requestTimeoutMillis).addContextValue("headerMap",
							(headerMap == null) ? headerMap : Arrays.toString(headerMap.entrySet().toArray()));
			logger.error("Healthcheck Failure", wrappedException);
			return Result.unhealthy(wrappedException);
		}
	}

	private Result localCheck() throws IOException, ClientProtocolException {
		HttpRequestBase httpRequest = createRequest(checkUrl);
		if (headerMap != null) {
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				httpRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		try {
			httpclient = createCloseableHttpClient();
			response = httpclient.execute(httpRequest);
			int httpStatusCode = response.getStatusLine().getStatusCode();
			if (isStatusCodeValid(httpStatusCode)) {
				return Result.healthy();
			}
			return Result.unhealthy(String.format("Request: %s\nRequest Headers: %s\nResponse: %s", httpRequest,
					Arrays.toString(httpRequest.getAllHeaders()), response));
		} finally {
			if (response != null) {
				response.close();
			}
			if (httpclient != null) {
				httpclient.close();
			}
		}
	}

	/**
	 * Provides Http request that defaults to an HttpGet.
	 * 
	 * @param url
	 * @return extension of HttpRequestBase (e.g. HttpGet, HttpPut)
	 */
	protected HttpRequestBase createRequest(String url) {
		return new HttpGet(checkUrl);
	}

	/**
	 * Provide configured instance of UrlValidator. This can be overridden if
	 * the defaults aren't suitable.
	 * 
	 * @return UrlValidator from Apache commons validator
	 */
	protected UrlValidator createUrlValidator() {
		return new UrlValidator();
	}

	/**
	 * Provide a configured CloseableHttpClient with the configuration given by
	 * createRequestConfig(). This can be overridden if the defaults aren't
	 * suitable.
	 * 
	 * @return CloseableHttpClient
	 */
	protected CloseableHttpClient createCloseableHttpClient() {
		return HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();
	}

	/**
	 * Provide RequestConfig with the requestTimeoutMillis specified. This can
	 * be overridden if more customization is needed.
	 * 
	 * @return RequestConfig
	 */
	protected RequestConfig createRequestConfig() {
		return RequestConfig.copy(RequestConfig.DEFAULT).setConnectionRequestTimeout(requestTimeoutMillis).build();
	}

	/**
	 * Determines if status code is valid (between 200 and 299 inclusive). This
	 * can be overridden if different criteria are wanted.
	 * 
	 * @param statusCode
	 * @return true if valid, false otherwise
	 */
	protected boolean isStatusCodeValid(int statusCode) {
		return statusCode >= 200 && statusCode <= 299;
	}

}
