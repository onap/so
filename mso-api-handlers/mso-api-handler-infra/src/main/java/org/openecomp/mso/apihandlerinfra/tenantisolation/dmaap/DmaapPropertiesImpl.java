package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import java.util.Map;

import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.client.dmaap.DmaapProperties;
import org.openecomp.mso.properties.MsoJavaProperties;

public class DmaapPropertiesImpl implements DmaapProperties {

	private final Map<String, String> props;
	
	public DmaapPropertiesImpl () {
		
		MsoJavaProperties properties = MsoPropertiesUtils.loadMsoProperties();
		this.props = properties.asMap();
	}
	
	@Override
	public Map<String, String> getProperties() {
		
		return this.props;
	}

}
