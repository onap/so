package org.openecomp.mso.client.restproperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.client.aai.AAIProperties;
import org.openecomp.mso.client.aai.AAIVersion;

public class AAIPropertiesImpl implements AAIProperties {

	final Map<String, String> props;

	public AAIPropertiesImpl() {
		this.props = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");

	}

	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.get("aai.endpoint"));
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
