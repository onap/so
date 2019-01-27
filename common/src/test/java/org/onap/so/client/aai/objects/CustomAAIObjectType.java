package org.onap.so.client.aai.objects;

import org.onap.so.client.aai.AAINamespaceConstants;
import org.onap.so.client.aai.AAIObjectType;

public class CustomAAIObjectType extends AAIObjectType {
	
	private static final long serialVersionUID = 1919729212831978098L;
	
	public static final AAIObjectType CUSTOM = new CustomAAIObjectType(AAINamespaceConstants.NETWORK, "my-url", "my-custom-name");
	
	/* Default constructor automatically called by AAIObjectType */
	public CustomAAIObjectType() {
		super();
	}
	protected CustomAAIObjectType(String parent, String uri, String name) {
		super(parent, uri, name);
	}

}
