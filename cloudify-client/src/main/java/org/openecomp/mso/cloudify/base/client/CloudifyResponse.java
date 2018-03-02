package org.openecomp.mso.cloudify.base.client;

import java.io.InputStream;
import java.util.Map;

public interface CloudifyResponse {

	public <T> T getEntity(Class<T> returnType);

	public <T> T getErrorEntity(Class<T> returnType);

	public InputStream getInputStream();

	public String getHeader(String name);
	
	public Map<String, String> headers();
	
}
