package org.openecomp.mso.cloudify.base.client;


public interface CloudifyClientConnector {

	public <T> CloudifyResponse request(CloudifyRequest<T> request);

}
