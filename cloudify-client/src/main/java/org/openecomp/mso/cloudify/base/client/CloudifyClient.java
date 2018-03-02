package org.openecomp.mso.cloudify.base.client;

import java.util.Properties;

import org.openecomp.mso.cloudify.connector.http.HttpClientConnector;

public class CloudifyClient {
	
	protected String managerEndpoint;
	protected String tenant = "default_tenant";	// Note - only default_tenant supported in community edition
	
	protected CloudifyTokenProvider tokenProvider;

	protected static int AUTHENTICATION_RETRIES = 1;

	protected CloudifyClientConnector connector;
	
	protected Properties properties = new Properties();

	public CloudifyClient(String managerEndpoint) {
		this.managerEndpoint = managerEndpoint;
		this.connector = new HttpClientConnector();
	}

	public CloudifyClient(String managerEndpoint, String tenant) {
		this.managerEndpoint = managerEndpoint;
		this.tenant = tenant;
		this.connector = new HttpClientConnector();
	}

	public CloudifyClient(String managerEndpoint, CloudifyClientConnector connector) {
		this.managerEndpoint = managerEndpoint;
		this.connector = connector;
	}

	/**
	 * Execute a Cloudify request by making the REST API call.  Return the
	 * complete CloudifyResponse structure, which includes the complete
	 * HTTP response.
	 * @param request a CloudifyRequest object
	 * @return a CloudifyResponse object
	 */
	public <T> CloudifyResponse request(CloudifyRequest<T> request) {
		CloudifyResponseException authException = null;

		for (int i = 0; i <= AUTHENTICATION_RETRIES; i++) {
			request.endpoint(managerEndpoint);
			request.header("Tenant", tenant);
			if (tokenProvider != null)
				request.header("Authentication-Token", tokenProvider.getToken());

			try {
				return connector.request(request);
			} catch (CloudifyResponseException e) {
				if (e.getStatus() != CloudifyResponseStatus.NOT_AUTHORIZED
						|| tokenProvider == null) {
					throw e;
				}
				authException = e;
				tokenProvider.expireToken();
			}
		}

		throw authException;
	}

	/**
	 * Execute a CloudifyRequest by sending the REST API call to the Cloudify
	 * Manager endpoint.  The return type is a JSON POJO object containing the
	 * response body entity.
	 * @param request
	 * @return a JSON POJO object specific to the request type
	 */
	public <T> T execute(CloudifyRequest<T> request) {
		CloudifyResponse response =  request(request);
		return (request.returnType() != null && request.returnType() != Void.class) ? response.getEntity(request.returnType()) : null;
	}

	public void property(String property, String value) {
		properties.put(property, value);
	}

	/**
	 * Set a Token Provider.  This class should be able to produce an
	 * authentication token on-demand.
	 * @param tokenProvider
	 */
	public void setTokenProvider(CloudifyTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}
	
	/**
	 * Manually set the authentication token to use for this client.
	 * @param token
	 */
	public void setToken(String token) {
		setTokenProvider(new CloudifySimpleTokenProvider(token));
	}
	
	/**
	 * Perform a simple GET request with no request message body
	 * @param path
	 * @param returnType
	 * @return An object of Class <R>
	 */
	public <R> CloudifyRequest<R> get(String path, Class<R> returnType) {
		return new CloudifyRequest<R>(this, HttpMethod.GET, path, null, returnType);
	}
	
}
