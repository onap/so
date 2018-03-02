package org.openecomp.mso.client.aai.exceptions;

public class AAIUriComputationException extends RuntimeException {

	private static final long serialVersionUID = 5187931752227522034L;

	public AAIUriComputationException(String s) {
		super(s);
	}
	
	public AAIUriComputationException(Throwable t) {
		super(t);
	}
}
