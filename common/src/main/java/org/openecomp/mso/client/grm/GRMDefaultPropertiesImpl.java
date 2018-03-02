package org.openecomp.mso.client.grm;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

public class GRMDefaultPropertiesImpl implements GRMProperties {
	
	public GRMDefaultPropertiesImpl() {
	}

	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL("http://localhost:28090");
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}

	@Override
	public String getDefaultVersion() {
		return "v1";
	}

	@Override
	public String getUsername() {
		return "gmruser";
	}

	@Override
	public String getPassword() {
		return "cGFzc3dvcmQ=";
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON;
	}

}
