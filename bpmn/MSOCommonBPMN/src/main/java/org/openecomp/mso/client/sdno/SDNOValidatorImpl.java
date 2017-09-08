package org.openecomp.mso.client.sdno;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.openecomp.mso.client.dmaap.Consumer;
import org.openecomp.mso.client.dmaap.DmaapConsumer;
import org.openecomp.mso.client.dmaap.DmaapPublisher;
import org.openecomp.mso.client.exceptions.SDNOException;
import org.openecomp.mso.jsonpath.JsonPathUtil;

public class SDNOValidatorImpl implements SDNOValidator, Consumer {

	private final static String aafUserName = "something";
	private final static String clientName = "MSO";
	private final static String healthDiagnosticPath = "body.output.response-healthdiagnostic";
	private final static String producerFilePath = "";
	private String uuid;
	private boolean continuePolling = true;
	@Override
	public void healthDiagnostic(String vnfName, String uuid) {
		//Query A&AI data
		// setup SDNO Entity
		//Call SDNO for Health Diagnostic
		//create producer file for MRClient https://wiki.web.att.com/display/MessageRouter/DMaaP_MR_JavaReferenceClient
		//  final MRBatchingPublisher pub = MRClientFactory.createBatchingPublisher(producerFilePath);
		//	pub.send("Mypartitionkey",JSON.toString(object));
		//create consumer file for MRClient https://wiki.web.att.com/display/MessageRouter/DMaaP_MR_JavaReferenceClient
		//check for error in subscription feed filter via jsonpath 
		//block and continue to poll waiting for response
	}

	protected SDNO buildRequestDiagnostic(String vnfName, String uuid, String oamIp) {
		
		Input input = new Input();
		SDNO parentRequest = new SDNO();
		Body body = new Body();
		parentRequest.setBody(body);
		parentRequest.setNodeType("vPE");
		parentRequest.setOperation("health-diagnostic");
		
		body.setInput(input);
		
		RequestHealthDiagnostic request = new RequestHealthDiagnostic();
		request.setRequestClientName(clientName);
		request.setRequestNodeName(vnfName);
		request.setRequestNodeIp(oamIp); //generic-vnf oam ip
		request.setRequestUserId(aafUserName); //mech id?
		request.setRequestId(uuid); //something to identify this request by for polling
		
		input.setRequestHealthDiagnostic(request);
		
		return parentRequest;
	}
	protected void submitRequest(String json) throws FileNotFoundException, IOException, InterruptedException {
		DmaapPublisher publisher = new DmaapPublisher(this.producerFilePath);
		publisher.send(json);
	}
	protected boolean pollForResponse(DmaapConsumer consumer, String uuid) throws Exception {
		this.uuid = uuid;
		return consumer.consume(this);
	}
	
	@Override
	public boolean continuePolling() {
		return continuePolling;
	}
	
	@Override
	public void stopProcessingMessages() {
		continuePolling = false;
	}
	@Override
	public void processMessage(String message) throws Exception {
		if (isHealthDiagnostic(message, this.getRequestId())) {
			if (!healthDiagnosticSuccessful(message)) {
				Optional<String> statusMessage = this.getStatusMessage(message);
				if (statusMessage.isPresent()) {
					throw new SDNOException(statusMessage.get());
				} else {
					throw new SDNOException();
				}
			} else {
				stopProcessingMessages();
			}
		}
	}
	
	@Override
	public boolean isAccepted(String message) {
		if (isResultInfo(message)) {
			Optional<String> code = isAccepted(message, this.getRequestId());
			if (code.isPresent()) {
				if ("202".equals(code.get())) {
					return true;
				} else {
					//TODO check other statuses 400 and 500
				}
			} else {
				//TODO throw error
			}
		}
		
		return false;
	}
	
	@Override
	public String getRequestId() {
		return uuid;
	}
	
	protected Optional<String> isAccepted(String json, String uuid) {
		return JsonPathUtil.getInstance().locateResult(json, String.format("$.result-info[?(@.status=='ACCEPTED' && @.request-id=='%s')].code", uuid));
	}
	
	protected boolean isResultInfo(String json) {
		return JsonPathUtil.getInstance().pathExists(json, "$[?(@.result-info)]");
	}
	
	protected boolean isHealthDiagnostic(String json, String uuid) {
		return JsonPathUtil.getInstance().pathExists(json, String.format("$[?(@.result-info.request-id=='%s')].%s", uuid, healthDiagnosticPath));
	}
	
	protected boolean healthDiagnosticSuccessful(String json) {
		return JsonPathUtil.getInstance().pathExists(json, "$." + healthDiagnosticPath + "[?(@.response-status=='Success')]");
	}
	
	protected Optional<String> getStatusMessage(String json) {
		return JsonPathUtil.getInstance().locateResult(json, "$." + healthDiagnosticPath + ".response-details-json");
	}
	
}
