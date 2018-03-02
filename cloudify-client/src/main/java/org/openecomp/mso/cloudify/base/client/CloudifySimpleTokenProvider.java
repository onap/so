package org.openecomp.mso.cloudify.base.client;

public class CloudifySimpleTokenProvider implements CloudifyTokenProvider {

	String token;

	public CloudifySimpleTokenProvider(String token) {
		this.token = token;
	}

	@Override
	public String getToken() {
		return this.token;
	}

	@Override
	public void expireToken() {
	}

}
