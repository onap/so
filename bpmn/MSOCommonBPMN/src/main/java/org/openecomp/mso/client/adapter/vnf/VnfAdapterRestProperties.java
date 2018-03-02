package org.openecomp.mso.client.adapter.vnf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.client.adapter.vnf.AdapterRestProperties;

public class VnfAdapterRestProperties implements AdapterRestProperties {

	private final Map<String, String> props;
	
	public VnfAdapterRestProperties() {
		this.props = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
	}
	
	@Override
	public String getAuth() {
		return props.get("mso.adapters.po.auth");
	}
	@Override
	public String getKey() {
		return props.get("mso.msoKey");
	}
	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.get("mso.adapters.vnf.rest.endpoint"));
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}

}
