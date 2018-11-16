package org.onap.so.cloud.authentication;

public class ServiceEndpointNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -5347215451284361397L;

	public ServiceEndpointNotFoundException(String message) {
		super(message);
	}
}
