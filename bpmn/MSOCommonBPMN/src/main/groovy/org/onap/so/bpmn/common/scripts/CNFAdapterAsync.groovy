package org.onap.so.bpmn.common.scripts

import jakarta.ws.rs.core.Response

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.ObjectMapper

import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.logging.filter.base.ONAPComponents
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory

import static org.onap.so.bpmn.common.scripts.GenericUtils.isBlank

public class CNFAdapterAsync extends AbstractServiceTaskProcessor {
	private static final Logger logger = LoggerFactory.getLogger(CNFAdapterAsync.class)

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		logger.debug("Start preProcessRequest");

		String apiPath = execution.getVariable("apiPath")
		if (isBlank(apiPath)) {
			String msg = "Cannot process CNF adapter call : API PATH is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		//Object cnfRequestPayloadFromExecutionVariable = execution.getVariable("cnfRequestPayload")

		String cnfRequestPayload = execution.getVariable("cnfRequestPayload")
		if (isBlank(cnfRequestPayload)) {
			String msg = "Cannot process CNF adapter call : cnfRequestPayload is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String correlator = execution.getVariable("correlator")
		if (isBlank(correlator)) {
			String msg = "Cannot process CNF adapter call : correlator is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String messageType = execution.getVariable("messageType")
		if (isBlank(messageType)) {
			String msg = "Cannot process CNF adapter call : messageType is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String timeout = UrnPropertiesReader.getVariable("mso.adapters.cnf.timeout", execution);
		if (isBlank(timeout)) {
			logger.debug("Setting CNF Adapter timeout to default : PT30M")
			timeout = "PT30M"
		}

		execution.setVariable("timeout",timeout)

		logger.debug("Enter preProcessRequest: {}",execution.getVariable("messageType"));
	}

	void callCnfAdapter(DelegateExecution execution) {
		logger.debug("Start callCnfAdapter")
		String cnfAdapterEndpoint = execution.getVariable("apiPath")
		URL requestUrl = new URL(cnfAdapterEndpoint)
		String cnfRequest = execution.getVariable("cnfRequestPayload")
		logger.debug("cnfRequest : " + cnfRequest)
		HttpClient httpClient = new HttpClientFactory().newJsonClient(requestUrl, ONAPComponents.EXTERNAL)
		Response httpResponse = httpClient.post(cnfRequest)
		int responseCode = httpResponse.getStatus()
		logger.debug("CNF sync response code is: " + responseCode)
		if(responseCode < 200 || responseCode >= 300){
			exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from CNF.")
		}
		logger.debug("End callCnfAdapter")
	}
}
