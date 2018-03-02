package org.openecomp.mso.requestsdb;

import org.openecomp.mso.logger.MsoLogger;

public class RequestsDBHelper {
		
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);	
	private String className = this.getClass().getSimpleName() +" class\'s ";
	private String methodName = ""; 
	private String classMethodMessage = "";
	
	/**
	 * This util method is to update the InfraRequest table to Complete
	 * @param msg - string, unique message for each caller
	 * @param requestId - string
	 * @param operationalEnvironmentId - string   
	 * @return void - nothing 
	 * @throws Exception 
	 */	
	public void updateInfraSuccessCompletion(String msg, String requestId, String operationalEnvironmentId) {
		methodName = "updateInfraSuccessCompletion() method.";
		classMethodMessage = className + " " + methodName;		
		msoLogger.debug("Begin of " + classMethodMessage);
			
		RequestsDatabase requestDB = RequestsDatabase.getInstance();
		requestDB.updateInfraFinalStatus(requestId, "COMPLETE", "SUCCESSFUL, operationalEnvironmentId - " + operationalEnvironmentId + "; Success Message: " + msg,
										100L, null, "APIH");
		msoLogger.debug("End of " + classMethodMessage);		
		
	}
	
	/**
	 * This util method is to update the InfraRequest table to Failure
	 * @param msg - string, unique message for each caller
	 * @param requestId - string
	 * @param operationalEnvironmentId - string   
	 * @return void - nothing 
	 * @throws Exception 
	 */	
	public void updateInfraFailureCompletion(String msg, String requestId, String operationalEnvironmentId) {
		methodName = "updateInfraFailureCompletion() method.";
		classMethodMessage = className + " " + methodName;		
		msoLogger.debug("Begin of " + classMethodMessage);
		
		RequestsDatabase requestDB = RequestsDatabase.getInstance();
		requestDB.updateInfraFinalStatus(requestId, "FAILED", "FAILURE, operationalEnvironmentId - " + operationalEnvironmentId + "; Error message: " + msg,
											100L, null, "APIH");
		msoLogger.debug("End of " + classMethodMessage);		
		
	}	
}
