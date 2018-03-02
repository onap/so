package org.openecomp.mso.client.adapter.vnf;

import org.openecomp.mso.client.RestProperties;

public interface AdapterRestProperties extends RestProperties {

	public String getAuth();
	public String getKey();
}
