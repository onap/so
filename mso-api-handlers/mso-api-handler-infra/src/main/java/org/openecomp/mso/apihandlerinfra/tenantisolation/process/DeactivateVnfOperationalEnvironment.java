package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.TenantIsolationException;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class DeactivateVnfOperationalEnvironment extends OperationalEnvironmentProcess {

	private static final String SERVICE_NAME = "DeactivateVnfOperationalEnvironment"; 
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	private String className = this.getClass().getName();
	
	public DeactivateVnfOperationalEnvironment(CloudOrchestrationRequest request, String requestId) {
		super(request, requestId);
		MsoLogger.setServiceName (getRequestId());
        MsoLogger.setLogContext(getRequestId(), getRequest().getOperationalEnvironmentId());
	}

	@Override
	public void execute() {
		String methodName = "deactivateOperationalEnvironment() method.";
		String classMethodMessage = className + " " + methodName;
		
		msoLogger.debug("Begin of execute method in " + SERVICE_NAME);		
		
		String operationalEnvironmentId = getRequest().getOperationalEnvironmentId();
		msoLogger.debug("Deactivate OperationalEnvironment on " + operationalEnvironmentId);
		try {
			msoLogger.debug("Start of AA&I Get client call in " + classMethodMessage);
			
			AAIResultWrapper aaiResult = getAaiHelper().getAaiOperationalEnvironment(operationalEnvironmentId);
			AAIOperationalEnvironment aaiOpEnv = aaiResult.asBean(AAIOperationalEnvironment.class).get();
			String operationalEnvironmentStatus = aaiOpEnv.getOperationalEnvironmentStatus();

			msoLogger.debug("OperationalEnvironmentStatus is :" + operationalEnvironmentStatus);
			msoLogger.debug(" End of AA&I Get client call in " + classMethodMessage);
			
			if(operationalEnvironmentStatus == null) {
				String error = "OperationalEnvironmentStatus is null on OperationalEnvironmentId: " + operationalEnvironmentId;
				throw new TenantIsolationException(error);
			}
			
			if(operationalEnvironmentStatus.equalsIgnoreCase("ACTIVE")) {
				msoLogger.debug("Start of AA&I UPDATE client call in " + classMethodMessage);
				
				aaiOpEnv.setOperationalEnvironmentStatus("INACTIVE");
				getAaiHelper().updateAaiOperationalEnvironment(operationalEnvironmentId, aaiOpEnv);
				
				msoLogger.debug(" End of AA&I UPDATE client call in " + classMethodMessage);
			} else if(!operationalEnvironmentStatus.equalsIgnoreCase("INACTIVE")) {
				String error = "Invalid OperationalEnvironmentStatus on OperationalEnvironmentId: " + operationalEnvironmentId;
				throw new TenantIsolationException(error);
			}
			
			getRequestDb().updateInfraSuccessCompletion("SUCCESSFULLY Deactivated OperationalEnvironment", requestId, operationalEnvironmentId);
			
		} catch(Exception e) {
			msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, "", "", "", MsoLogger.ErrorCode.DataError, e.getMessage());
			getRequestDb().updateInfraFailureCompletion(e.getMessage(), requestId, operationalEnvironmentId);
		}
		
		msoLogger.debug("End of " + classMethodMessage);		
	}
	
	@Override
	protected String getServiceName() {
		return DeactivateVnfOperationalEnvironment.SERVICE_NAME;
	}
}