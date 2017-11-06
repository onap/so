package org.openecomp.mso.client.dmaap;

import java.util.Map;

public interface DmaapProperties {

	/**
	 * A map of strings which contains the properties for a dmaap client
	 * @return
	 */
	public Map<String, String> getProperties();
}
