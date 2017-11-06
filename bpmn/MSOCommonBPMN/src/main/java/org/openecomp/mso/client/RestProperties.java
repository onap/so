package org.openecomp.mso.client;

import java.net.MalformedURLException;
import java.net.URL;

public interface RestProperties {

	public URL getEndpoint() throws MalformedURLException;
	public String getSystemName();
}
