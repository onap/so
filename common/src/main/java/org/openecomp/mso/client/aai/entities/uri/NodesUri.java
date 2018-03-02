package org.openecomp.mso.client.aai.entities.uri;

import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;

public class NodesUri extends SimpleUri {

	protected NodesUri(AAIObjectType type, Object... values) {
		super(type, values);
	}
	
	
	@Override
	protected String getTemplate(AAIObjectType type) {
		return "/nodes" + type.partialUri();
	}
	
	@Override
	protected String getTemplate(AAIObjectPlurals type) {
		return "/nodes" + type.partialUri();
	}
}
