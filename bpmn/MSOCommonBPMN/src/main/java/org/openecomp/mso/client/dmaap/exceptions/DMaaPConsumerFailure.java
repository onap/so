package org.openecomp.mso.client.dmaap.exceptions;

public class DMaaPConsumerFailure extends Exception {

	private static final long serialVersionUID = 2499229901897110362L;

	public DMaaPConsumerFailure() {
		super();
	}
	
	public DMaaPConsumerFailure(String message) {
		super(message);
	}
}
