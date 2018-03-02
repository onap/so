package org.openecomp.mso.client.aai.exceptions;

public class AAIPayloadException extends Exception {

	private static final long serialVersionUID = -5712783905947711065L;
	
	public AAIPayloadException(Throwable t) {
		super(t);
	}
	
	public AAIPayloadException(String s, Throwable t) {
		super(s, t);
	}
	
	public AAIPayloadException(String s) {
		super(s);
	}
	

}
