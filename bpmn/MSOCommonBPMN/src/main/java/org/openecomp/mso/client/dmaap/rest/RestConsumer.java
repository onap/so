package org.openecomp.mso.client.dmaap.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.dmaap.Consumer;
import org.openecomp.mso.client.policy.RestClient;

public class RestConsumer implements Consumer {

	private final RestClient client;
	public RestConsumer(Properties properties) {
		PropertiesBean bean = new PropertiesBean(properties);
		client = new DMaaPRestClient(this.createURL(bean), bean.getContentType(), bean.getUsername(), bean.getPassword());
	}
	
	private URL createURL(PropertiesBean properties) {
		try {
			return UriBuilder.fromUri("http://" + properties.getHost())
					.path("events").path(properties.getTopic())
					.path(properties.getPartition())
					.path("consumer1")
					.queryParam("timeout",  properties.getTimeout()).build().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Iterable<String> fetch() {
		
		return client.get(new GenericType<List<String>>() {});
	}

}
