package org.openecomp.mso.apihandlerinfra.tenantisolation;

import java.net.MalformedURLException;
import java.net.URL;

import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.client.aai.AAIProperties;
import org.openecomp.mso.client.aai.AAIVersion;
import org.openecomp.mso.properties.MsoJavaProperties;

public class AaiClientPropertiesImpl implements AAIProperties {

	final MsoJavaProperties props;
	public AaiClientPropertiesImpl() {
		this.props = MsoPropertiesUtils.loadMsoProperties ();
	}

	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.getProperty("aai.endpoint", null));
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}
	
	@Override
	public AAIVersion getDefaultVersion() {
		return AAIVersion.LATEST;
	}
}
