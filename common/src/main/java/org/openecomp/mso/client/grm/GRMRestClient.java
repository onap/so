package org.openecomp.mso.client.grm;


import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.ClientResponseFilter;

import org.openecomp.mso.client.ResponseExceptionMapperImpl;
import org.openecomp.mso.client.RestProperties;
import org.openecomp.mso.client.policy.RestClient;

public class GRMRestClient extends RestClient {

	private final String username;
	private final String password;
	
	public GRMRestClient(RestProperties props, URI path, String username, String password) {
		super(props, UUID.randomUUID(), Optional.of(path));
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
	public RestClient addRequestId(UUID requestId) {
		return this;
	}
}
