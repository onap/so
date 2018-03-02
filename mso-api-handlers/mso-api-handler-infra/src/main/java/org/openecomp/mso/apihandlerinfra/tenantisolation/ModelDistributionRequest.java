package org.openecomp.mso.apihandlerinfra.tenantisolation;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.MsoException;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Distribution;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Status;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.serviceinstancebeans.RequestError;
import org.openecomp.mso.serviceinstancebeans.ServiceException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.jaxrs.PATCH;

@Path("/modelDistributions")
@Api(value="/modelDistributions",description="API Requests for Model Distributions")
public class ModelDistributionRequest {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private TenantIsolationRunnable tenantIsolation = null;
	
	@PATCH
	@Path("/{version:[vV][1]}/distributions/{distributionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Update model distribution status",response=Response.class)
	public Response updateModelDistributionStatus(String requestJSON, @PathParam("version") String version, @PathParam("distributionId") String distributionId) {
		long startTime = System.currentTimeMillis ();
		Distribution distributionRequest = null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			distributionRequest = mapper.readValue(requestJSON, Distribution.class);
		} catch(Exception e) {
			msoLogger.debug ("Mapping of request to JSON object failed : ", e);
			Response response = buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, 
														MsoException.ServiceException,
														"Mapping of request to JSON object failed.  " + e.getMessage(),
														ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Mapping of request to JSON object failed");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		try {
			parse(distributionRequest);
		} catch(Exception e) {
			msoLogger.debug ("Validation failed: ", e);
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
			Response response = buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, 
														MsoException.ServiceException,
														"Error parsing request.  " + e.getMessage(),
														ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		CloudOrchestrationRequest cor = new CloudOrchestrationRequest();
		cor.setDistribution(distributionRequest);
		cor.setDistributionId(distributionId);
		
		TenantIsolationRunnable runnable = getThread();
		runnable.setAction(Action.distributionStatus);
		runnable.setCor(cor);
		runnable.setOperationalEnvType(null);
		runnable.setRequestId(null);
		
		Thread thread = new Thread(runnable);
		thread.start();
		
		return Response.ok().build();
	}

	private void parse(Distribution distributionRequest) throws ValidationException {
		if(distributionRequest.getStatus() == null) {
			throw new ValidationException("status");
		}
		
		if(StringUtils.isBlank(distributionRequest.getErrorReason()) && Status.DISTRIBUTION_COMPLETE_ERROR.equals(distributionRequest.getStatus())) {
			throw new ValidationException("errorReason");
		}
	}
	
    private Response buildServiceErrorResponse (int httpResponseCode, MsoException exceptionType, String text,
            									String messageId, List<String> variables) {
    	RequestError re = new RequestError();
    	ServiceException se = new ServiceException();
    	se.setMessageId(messageId);
    	se.setText(text);
    	if(variables != null){
        	if(variables != null){
        		for(String variable: variables){
        			se.getVariables().add(variable);
       			}
       		}
    	}
    	re.setServiceException(se);

        String requestErrorStr = null;
        try{
        	ObjectMapper mapper = new ObjectMapper();
        	mapper.setSerializationInclusion(Include.NON_DEFAULT);
        	requestErrorStr = mapper.writeValueAsString(re);
        }catch(Exception e){
        	msoLogger.error (MessageEnum.APIH_VALIDATION_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in buildServiceErrorResponse writing exceptionType to string ", e);
        }

        return Response.status (httpResponseCode).entity(requestErrorStr).build ();
    }
    
	public TenantIsolationRunnable getThread() {
		if(tenantIsolation == null) {
			tenantIsolation = new TenantIsolationRunnable();
		}
		return tenantIsolation;
	}

	public void setThread(TenantIsolationRunnable thread) {
		this.tenantIsolation = thread;
	}
}
