package org.openecomp.mso.client.grm;

import java.net.URI;
import java.util.Base64;

import javax.ws.rs.core.UriBuilder;
import org.openecomp.mso.client.RestPropertiesLoader;
import org.openecomp.mso.client.policy.RestClient;

public class GRMRestInvoker {
	
	private final RestClient client;
	private final GRMProperties properties;
	
	public GRMRestInvoker(GRMAction action) {
		GRMProperties props = GRMPropertiesLoader.getInstance().getImpl();
		if (props == null) {
			props = new GRMDefaultPropertiesImpl();
		}
		this.properties = props;
		this.client = new GRMRestClient(this.properties, this.createURI(action), this.properties.getUsername(), this.decode(this.properties.getPassword()));
	}
	
	private URI createURI(GRMAction action) {
		return UriBuilder.fromUri("/GRMLWPService")
				.path(this.properties.getDefaultVersion())
				.path("serviceEndPoint")
				.path(action.getAction())
				.build();
	}
	
	private String decode(String cred) {
		try {
			return new String(Base64.getDecoder().decode(cred.getBytes()));
		} 
		catch(IllegalArgumentException iae) {
			return cred;
		}
	}
	
	private RestClient getClient() {
		return this.client;
	}
	
	public void post(Object obj) {
		getClient().post(obj);
	}
	
	public <T> T post(Object obj, Class<T> resultClass) {
		return getClient().post(obj, resultClass);
	}
	
}
