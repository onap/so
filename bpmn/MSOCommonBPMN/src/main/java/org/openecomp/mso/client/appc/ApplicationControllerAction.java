package org.openecomp.mso.client.appc;

import java.util.HashMap;
import java.util.List;

import org.openecomp.mso.client.appc.ApplicationControllerSupport.StatusCategory;
import org.openecomp.mso.bpmn.appc.payload.PayloadClient;
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.client.appc.ApplicationControllerOrchestrator;
import java.util.Optional;
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.Status;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.configuration.EELFLogger.Level;
import java.lang.NoSuchMethodError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ApplicationControllerAction {
	protected ApplicationControllerOrchestrator client = new ApplicationControllerOrchestrator();
	private String errorCode = "1002";
	private String errorMessage = "Unable to reach App C Servers";
	protected final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
	
	public void runAppCCommand(Action action, String msoRequestId, String vnfId, Optional<String> payload, HashMap<String, String> payloadInfo){
		Status appCStatus = null;
		try{
			String vnfName = payloadInfo.getOrDefault("vnfName", "");
			String aicIdentity = payloadInfo.getOrDefault("vnfName","");
			String vnfHostIpAddress = payloadInfo.getOrDefault("vnfHostIpAddress","");
			String vmIdList = payloadInfo.getOrDefault("vmIdList", "");
			String identityUrl = payloadInfo.getOrDefault("identityUrl", "");
			switch(action){
				case ResumeTraffic:
					appCStatus = resumeTrafficAction(msoRequestId, vnfId, vnfName);
					break;
			    case Start:
			    case Stop:
			    	appCStatus = startStopAction(action, msoRequestId, vnfId, aicIdentity);
			    	break;
				case Unlock:
				case Lock:
					appCStatus = client.vnfCommand(action, msoRequestId, vnfId, Optional.empty());
					break;
				case QuiesceTraffic:
					appCStatus = quiesceTrafficAction(msoRequestId, vnfId, payload, vnfName);
					break;
				case HealthCheck:
					appCStatus = healthCheckAction(msoRequestId, vnfId, vnfName, vnfHostIpAddress);
					break;
				case Snapshot:
					String vmIds = JsonUtils.getJsonValue(vmIdList, "vmIds");
					String vmId = "";
					ObjectMapper mapper = new ObjectMapper();
					List<String> vmIdJsonList = mapper.readValue(vmIds, new TypeReference<List<String>>(){});
					int i = 0;
					while(i < vmIdJsonList.size()){
						vmId = vmIdJsonList.get(i);
						appCStatus = snapshotAction(msoRequestId, vnfId, vmId, identityUrl);
						i++;
					}
					break;
				case ConfigModify:
					appCStatus = payloadAction(action, msoRequestId, vnfId, payload);
					break;
				case UpgradePreCheck:
				case UpgradePostCheck:
				case UpgradeSoftware:
				case UpgradeBackup:
					appCStatus = upgradeAction(action,msoRequestId, vnfId, payload, vnfName);
					break;
				default:
					errorMessage = "Unable to idenify Action request for AppCClient";
					break;
			}
			if(appCStatus != null){
				errorCode = Integer.toString(appCStatus.getCode());
				errorMessage = appCStatus.getMessage();
 
			}
			if(ApplicationControllerSupport.getCategoryOf(appCStatus).equals(StatusCategory.NORMAL)){
				errorCode = "0";
			}
		}
		catch(JsonProcessingException e){
			auditLogger.log(Level.ERROR, "Incorrect Payload format for action request" + action.toString(),e, e.getMessage());
			errorMessage = e.getMessage();
		}
		catch(ApplicationControllerOrchestratorException e){
			auditLogger.log(Level.ERROR, "Error building Appc request: ", e, e.getMessage());
			errorCode = "1002";
			errorMessage = e.getMessage();
		}
		catch (NoSuchMethodError e) {
			auditLogger.log(Level.ERROR, "Error building Appc request: ", e, e.getMessage());
			errorMessage = e.getMessage();
		} 
		catch(Exception e){
			auditLogger.log(Level.ERROR, "Error building Appc request: ", e, e.getMessage());
			errorMessage = e.getMessage();
		}
	}
	
	private Status payloadAction(Action action, String msoRequestId, String vnfId, Optional<String> payload) throws JsonProcessingException, Exception{
		if(!(payload.isPresent())){
			throw new IllegalArgumentException("Payload is not present for " + action.toString());
		}
		return client.vnfCommand(action, msoRequestId, vnfId, payload);
	}
	
	private Status quiesceTrafficAction(String msoRequestId, String vnfId, Optional<String> payload, String vnfName) throws JsonProcessingException, Exception{
		if(!(payload.isPresent())){
			throw new IllegalArgumentException("Payload is not present for " + Action.QuiesceTraffic.toString());
		}
		payload = PayloadClient.quiesceTrafficFormat(payload, vnfName);
		return client.vnfCommand(Action.QuiesceTraffic, msoRequestId, vnfId, payload);
	}
	
	private Status upgradeAction(Action action, String msoRequestId, String vnfId, Optional<String> payload, String vnfName) throws JsonProcessingException, Exception{
		if(!(payload.isPresent())){
			throw new IllegalArgumentException("Payload is not present for " + action.toString());
		}
		payload = PayloadClient.upgradeFormat(payload, vnfName);
		return client.vnfCommand(action, msoRequestId, vnfId, payload);
	}
	
	private Status resumeTrafficAction(String msoRequestId, String vnfId, String vnfName)throws JsonProcessingException, Exception{
		Optional<String> payload = PayloadClient.resumeTrafficFormat(vnfName);
		return client.vnfCommand(Action.ResumeTraffic, msoRequestId, vnfId, payload);
	}
	
	private Status startStopAction(Action action, String msoRequestId, String vnfId, String aicIdentity)throws JsonProcessingException, Exception{
		Optional<String> payload = PayloadClient.startStopFormat(aicIdentity);
		return client.vnfCommand(action, msoRequestId, vnfId, payload);
	}
	
	private Status healthCheckAction(String msoRequestId, String vnfId, String vnfName, String vnfHostIpAddress)throws JsonProcessingException, Exception{
		Optional<String> payload = PayloadClient.healthCheckFormat(vnfName, vnfHostIpAddress);
		return client.vnfCommand(Action.HealthCheck, msoRequestId, vnfId, payload);
	}
	
	private Status snapshotAction(String msoRequestId, String vnfId, String vmId, String identityUrl) throws JsonProcessingException, Exception{
		Optional<String> payload = PayloadClient.snapshotFormat(vmId, identityUrl);
		return client.vnfCommand(Action.Snapshot, msoRequestId, vnfId, payload);
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	public String getErrorCode(){
		return errorCode;
	}
}
