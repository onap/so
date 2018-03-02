package org.openecomp.mso.cloudify.base.client;

/**
 * Custom RuntimeException to report connection errors to Openstack endpoints.
 * Must be a RuntimeException to conform with OpenstackClient interface, which
 * does not declare specific Exceptions.
 */
public class CloudifyConnectException extends CloudifyBaseException {

	private static final long serialVersionUID = 7294957362769575271L;

	public CloudifyConnectException(String message) {
		super(message);
	}

	public CloudifyConnectException(String message, Throwable cause) {
		super(message, cause);
	}
}
