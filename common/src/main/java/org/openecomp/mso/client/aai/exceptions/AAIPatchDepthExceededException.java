package org.openecomp.mso.client.aai.exceptions;

public class AAIPatchDepthExceededException extends RuntimeException {

	private static final long serialVersionUID = -3740429832086738907L;
	
	
	public AAIPatchDepthExceededException(String payload) {
		super("Object exceeds allowed depth for update action: " + payload);
	}
}
