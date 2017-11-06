package org.openecomp.mso.client.sdno.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.openecomp.mso.client.dmaap.DmaapConsumer;
import org.openecomp.mso.client.exceptions.SDNOException;
import org.openecomp.mso.jsonpath.JsonPathUtil;

public class SDNOHealthCheckDmaapConsumer extends DmaapConsumer {

	private final String uuid;
	private boolean continuePolling = true;
	private final static String healthDiagnosticPath = "body.output.*";

	public SDNOHealthCheckDmaapConsumer() throws FileNotFoundException, IOException {
		this("none");
	}
	
	public SDNOHealthCheckDmaapConsumer(String uuid) throws FileNotFoundException, IOException {
		super();
		this.uuid = uuid;
	}
	
	@Override
	public String getUserName() {
		return msoProperties.get("sdno.health-check.dmaap.username");
	}

	@Override
	public String getPassword() {
		return msoProperties.get("sdno.health-check.dmaap.password");
	}

	@Override
	public String getTopic() {
		return msoProperties.get("sdno.health-check.dmaap.subscriber.topic");
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
					throw new SDNOException("failed with message " + statusMessage.get());
				} else {
					throw new SDNOException("failed with no status message");
				}
			} else {
				auditLogger.info("successful health diagnostic found for request: " + this.getRequestId());
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
	public boolean isFailure(String message) {
		if (isResultInfo(message)) {
			Optional<String> code = isFailure(message, this.getRequestId());
			if (code.isPresent()) {
				if ("500".equals(code.get())) {
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
	
	protected Optional<String> isFailure(String json, String uuid) {
		return JsonPathUtil.getInstance().locateResult(json, String.format("$.result-info[?(@.status=='FAILURE' && @.request-id=='%s')].code", uuid));
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
		return JsonPathUtil.getInstance().locateResult(json, "$." + healthDiagnosticPath + ".error-message");
	}
	
	@Override
	public int getMaximumElapsedTime() {
		return 300000;
	}
}
