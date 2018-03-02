package org.openecomp.mso.client.grm;

import org.openecomp.mso.client.RestProperties;

public interface GRMProperties extends RestProperties {
	public String getDefaultVersion();
	public String getUsername();
	public String getPassword();
	public String getContentType();
}
