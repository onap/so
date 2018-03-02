package org.openecomp.mso.client.appc;

public class ApplicationControllerOrchestratorException extends Exception {

	private final int appcCode;
	
	public ApplicationControllerOrchestratorException(String message, int code) {
		super(message);
		appcCode = code;
	}
	
	public int getAppcCode()
	{
		return appcCode;
	}
}
