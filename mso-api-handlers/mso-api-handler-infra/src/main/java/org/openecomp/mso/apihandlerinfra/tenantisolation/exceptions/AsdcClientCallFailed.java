package org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions;

public class AsdcClientCallFailed extends Exception {

	public AsdcClientCallFailed(String message, Throwable cause) {
		super(message, cause);
	}

	public AsdcClientCallFailed(String message) {
		super(message);
	}
	
	
}
