package org.openecomp.mso.client.dmaap.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.openecomp.mso.client.dmaap.Publisher;
import org.openecomp.mso.client.policy.RestClient;

public class RestPublisher implements Publisher {

	private final RestClient client;

	public RestPublisher(Properties properties) {
		PropertiesBean bean = new PropertiesBean(properties);
		client = new DMaaPRestClient(this.createURL(bean), bean.getContentType(), bean.getUsername(), bean.getPassword());
	}
	
	private URL createURL(PropertiesBean properties) {
		try {
			return UriBuilder.fromUri("http://" + properties.getHost())
					.path("events").path(properties.getTopic())
					.queryParam("timeout",  properties.getTimeout()).build().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void send(String json) {
		client.post(json);
	}
}
