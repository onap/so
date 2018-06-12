package org.openecomp.mso.client.dmaapproperties;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.bpmn.common.SpringContextHelper;
import org.openecomp.mso.client.dmaap.DmaapProperties;
import org.springframework.context.ApplicationContext;

public class GlobalDmaapPropertiesImpl implements DmaapProperties {

	private final Map<String, String> props  = new HashMap<>();
	
	private static final String[] propertyNames = {
			"mso.global.dmaap.username",
			"mso.global.dmaap.password",
			"mso.global.dmaap.host",
			"mso.global.dmaap.publisher.topic"
	};
	
	public GlobalDmaapPropertiesImpl () {
		ApplicationContext context = SpringContextHelper.getAppContext();
		for (String name : propertyNames) {
			this.props.put(name, context.getEnvironment().getProperty(name));
		}
	}
	
	@Override
	public Map<String, String> getProperties() {
		return this.props;
	}
}
