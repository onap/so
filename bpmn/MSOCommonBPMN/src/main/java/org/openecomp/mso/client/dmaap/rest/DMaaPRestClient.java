package org.openecomp.mso.client.dmaap.rest;

import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.ClientResponseFilter;

import org.openecomp.mso.client.ResponseExceptionMapperImpl;
import org.openecomp.mso.client.policy.RestClient;

public class DMaaPRestClient  extends RestClient {

	private final String username;
	private final String password;
	public DMaaPRestClient(URL url, String contentType, String username, String password) {
		super(url, contentType);
		this.username = username;
		this.password = password;
	}

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes()));
	}

	@Override
	protected Optional<ClientResponseFilter> addResponseFilter() {
		return Optional.of(new ResponseExceptionMapperImpl());
	}

	@Override
	public RestClient addRequestId(String requestId) {
		return null;
	}
}
