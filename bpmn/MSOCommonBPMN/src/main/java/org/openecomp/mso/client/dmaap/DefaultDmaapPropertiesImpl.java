package org.openecomp.mso.client.dmaap;

import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;

public class DefaultDmaapPropertiesImpl implements DmaapProperties {

	private final Map<String, String> properties;
	public DefaultDmaapPropertiesImpl() {
		this.properties = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
	}
	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	} 

}
