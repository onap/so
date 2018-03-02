package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.DmaapOperationalEnvClient;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;


public class CreateEcompOperationalEnvironment extends OperationalEnvironmentProcess {
	
	private static final String SERVICE_NAME = "CreateEcompOperationalEnvironment";
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	
	
	public CreateEcompOperationalEnvironment(CloudOrchestrationRequest request, String requestId) {
		super(request, requestId);
        MsoLogger.setServiceName (getRequestId());
        MsoLogger.setLogContext(getRequestId(), getRequest().getOperationalEnvironmentId());
	}

	
	protected DmaapOperationalEnvClient getDmaapClient() {
		return new DmaapOperationalEnvClient();
	}


	@Override
	public void execute() {
		try {
			msoLogger.debug("Begin of execute method in " + SERVICE_NAME);
			msoLogger.debug("CloudOrchestrationRequest: " + request.toString());
			
 			//Create ECOMP Managing Environment object in A&AI
			getAaiHelper().createOperationalEnvironment(getAaiClientObjectBuilder().buildAAIOperationalEnvironment("ACTIVE"));
			msoLogger.debug("ECOMP operational environment created in A&AI.");
					
			// Call client to publish to DMaap	
			getDmaapClient().dmaapPublishOperationalEnvRequest(getRequest().getOperationalEnvironmentId(), 
					getRequest().getRequestDetails().getRequestInfo().getInstanceName(),
					getRequest().getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString(),
					getRequest().getRequestDetails().getRequestParameters().getTenantContext(),
					getRequest().getRequestDetails().getRequestParameters().getWorkloadContext(),
					"Create");
			msoLogger.debug("ECOMP operational environment published in Dmaap/ASDC.");
		
			//Update request database
			getRequestDb().updateInfraSuccessCompletion("SUCCESSFULLY Created ECOMP OperationalEnvironment.", getRequestId(), getRequest().getOperationalEnvironmentId()); 
		} 
		catch (Exception e) {
			e.printStackTrace();
			msoLogger.error(MessageEnum.APIH_GENERAL_EXCEPTION, "", "", "", MsoLogger.ErrorCode.UnknownError, e.getMessage());
			getRequestDb().updateInfraFailureCompletion(e.getMessage(), getRequestId(), getRequest().getOperationalEnvironmentId());
		}	
	}


	@Override
	protected String getServiceName() {
		return CreateEcompOperationalEnvironment.SERVICE_NAME;
	}
	
}