package org.openecomp.mso.cloudify.base.client;

public interface CloudifyTokenProvider {

	String getToken();

	void expireToken();

}
