package org.openecomp.mso.client;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultProperties implements RestProperties {

	private final URL url;
	public DefaultProperties(URL url) {
		this.url = url;
	}
	@Override
	public URL getEndpoint() throws MalformedURLException {
		return this.url;
	}

	@Override
	public String getSystemName() {
		return RestClient.ECOMP_COMPONENT_NAME;
	}

}
