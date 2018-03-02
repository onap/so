package org.openecomp.mso.apihandlerinfra.tenantisolation;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.client.grm.GRMProperties;
import org.openecomp.mso.properties.MsoJavaProperties;

public class GrmClientPropertiesImpl implements GRMProperties {

	final MsoJavaProperties props;
	
	public GrmClientPropertiesImpl() {
		this.props = MsoPropertiesUtils.loadMsoProperties ();
	}

	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.getProperty("grm.endpoint", null));
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
		return props.getProperty("grm.username", null);
	}

	@Override
	public String getPassword() {
		return props.getProperty("grm.password", null);
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON;
	}

}
