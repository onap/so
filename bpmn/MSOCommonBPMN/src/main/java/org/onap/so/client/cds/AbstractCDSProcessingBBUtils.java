/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.cds;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.onap.ccsdk.apps.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.apps.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.client.PreconditionFailedException;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.util.JsonFormat;

/**
 * Util class to support Call to CDS client
 *
 */
@Component
public class AbstractCDSProcessingBBUtils {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCDSProcessingBBUtils.class);

	public static String cdsResponse = "";
	private static final String BLUEPRINT_NAME_PROP = "blueprintName";
	private static final String BLUEPRINT_VERSION_PROP = "blueprintVersion";
	private static final String ACTION_PROP = "actionName";
	private static final String MODE_PROP = "mode";
	private static final String REQUESTOBJECT_PROP = "requestObject";
	private static final String REQUESTID_PROP = "requestId";
	private static final String ORIGINATORID_PROP = "originatorId";
	private static final String SUBREQUESTID_PROP = "subRequestId";

	@Autowired
	private ExceptionBuilder exceptionUtil;

	/**
	 * Extracting data from execution object and building the ExecutionServiceInput
	 * Object
	 * 
	 * @param execution
	 *            DelegateExecution object
	 */
	public void constructExecutionServiceInputObject(DelegateExecution execution) {
		logger.trace("Start AbstractCDSProcessingBBUtils.preProcessRequest ");

		try {
			String originatorId = (String) execution.getVariable(ORIGINATORID_PROP);
			String requestId = (String) execution.getVariable(REQUESTID_PROP);
			String subRequestId = (String) execution.getVariable(SUBREQUESTID_PROP);
			String blueprintName = (String) execution.getVariable(BLUEPRINT_NAME_PROP);
			String blueprintVersion = (String) execution.getVariable(BLUEPRINT_VERSION_PROP);
			String actionName = (String) execution.getVariable(ACTION_PROP);
			String mode = (String) execution.getVariable(MODE_PROP);
			String requestObject = (String) execution.getVariable(REQUESTOBJECT_PROP);

			String payload = requestObject;

			CommonHeader commonHeader = CommonHeader.newBuilder().setOriginatorId(originatorId).setRequestId(requestId)
					.setSubRequestId(subRequestId).build();
			ActionIdentifiers actionIdentifiers = ActionIdentifiers.newBuilder().setBlueprintName(blueprintName)
					.setBlueprintVersion(blueprintVersion).setActionName(actionName).setMode(mode).build();

			Builder struct = Struct.newBuilder();
			try {
				JsonFormat.parser().merge(payload, struct);
			} catch (InvalidProtocolBufferException e) {
				logger.error("Failed to parse received message. blueprint({}:{}) for action({}). {}", blueprintVersion,
						blueprintName, actionName, e);
			}

			ExecutionServiceInput executionServiceInput = ExecutionServiceInput.newBuilder()
					.setCommonHeader(commonHeader).setActionIdentifiers(actionIdentifiers).setPayload(struct).build();

			execution.setVariable("executionServiceInput", executionServiceInput);

		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	/**
	 * get the executionServiceInput object from execution and send a request to CDS
	 * Client and wait for TIMEOUT period
	 * 
	 * @param execution
	 *            DelegateExecution object
	 */
	public void sendRequestToCDSClient(DelegateExecution execution) {

		logger.trace("Start AbstractCDSProcessingBBUtils.sendRequestToCDSClient ");
		try {
			CDSProperties props = RestPropertiesLoader.getInstance().getNewImpl(CDSProperties.class);
			if (props == null) {
				throw new PreconditionFailedException(
						"No RestProperty.CDSProperties implementation found on classpath, can't create client.");
			}

			ExecutionServiceInput executionServiceInput = (ExecutionServiceInput) execution
					.getVariable("executionServiceInput");

			CDSProcessingListener cdsProcessingListener = new CDSProcessingListenerImpl();

			CDSProcessingClient cdsClient = new CDSProcessingClient(cdsProcessingListener);
			CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);

			try {
				countDownLatch.await(props.getTimeout(), TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				logger.error("Caught exception in sendRequestToCDSClient in AbstractCDSProcessingBBUtils : ", ex);
			} finally {
				cdsClient.close();
			}

			if (cdsResponse != null) {
				execution.setVariable("CDSStatus", cdsResponse);
			}

		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	/**
	 * Will construct the payload required to call the CDS client
	 * 
	 * @param parameters Map<String, Object>
	 * @param nfType String value example: VNF/PNF
	 * @param configType String value example: ConfigAssign/ConfigDeploy 
	 * @return String payload
	 */
	public String constructPayload(Map<String, Object> parameters, String nfType, String configType) {

		JSONObject payloadParameters = new JSONObject();
		JSONObject configPropertiesParameters = new JSONObject();
		JSONObject configRequestParameters = new JSONObject();

		if (null != nfType && nfType.equalsIgnoreCase("VNF")) {
			if (null != parameters && null != parameters.get("vnf-id") && null != parameters.get("vnf-name")
					&& null != parameters.get("vnf-customization-uuid")) {
				String vnfId = (String) parameters.get("vnf-id");
				String vnfName = (String) parameters.get("vnf-name");
				String vnfCustomizationUuid = (String) parameters.get("vnf-customization-uuid");

				configPropertiesParameters.put("vnf-id", vnfId);
				configPropertiesParameters.put("vnf-name", vnfName);
				configPropertiesParameters.put("vnf-customization-uuid", vnfCustomizationUuid);
			}
		}
		if (nfType != null && nfType.equalsIgnoreCase("PNF")) {
			if (null != parameters && null != parameters.get("pnf-id") && null != parameters.get("pnf-name")
					&& null != parameters.get("pnf-customization-uuid")) {
				String pnfId = (String) parameters.get("pnf-id");
				String pnfName = (String) parameters.get("pnf-name");
				String pnfCustomizationUuid = (String) parameters.get("pnf-customization-uuid");

				configPropertiesParameters.put("pnf-id", pnfId);
				configPropertiesParameters.put("pnf-name", pnfName);
				configPropertiesParameters.put("pnf-customization-uuid", pnfCustomizationUuid);
			}
		}
		if (null != parameters && null != parameters.get("service-instance-id")) {
			String serviceIntanceId = (String) parameters.get("service-instance-id");
			configPropertiesParameters.put("service-instance-id", serviceIntanceId);
		}
		if (null != parameters && null != parameters.get("service-model-uuid")) {
			String serviceModelUuid = (String) parameters.get("service-model-uuid");
			configPropertiesParameters.put("service-model-uuid", serviceModelUuid);
		}

		if ((null != parameters && null != parameters.get("user_params"))
				&& configType.equalsIgnoreCase("ConfigAssign")) {
			Object instance = parameters.get("user_params");
			if (instance instanceof Map) {
				Map<String, Object> m = (Map<String, Object>) instance;
				for (Map.Entry<String, Object> entry : m.entrySet()) {
					configPropertiesParameters.put(entry.getKey(), entry.getValue());
				}
			}
		}

		if (null != parameters && null != parameters.get("resolution-key")) {
			String resolutionkey = (String) parameters.get("resolution-key");
			configRequestParameters.put("resolution-key", resolutionkey);
		}

		if ((null != configType || !configType.equals("")) && configType.equalsIgnoreCase("ConfigDeploy")) {
			configRequestParameters.put("config-deploy-properties", configPropertiesParameters);
			payloadParameters.put("config-deploy-request", configRequestParameters);

		}
		if ((null != configType || !configType.equals("")) && configType.equalsIgnoreCase("ConfigAssign")) {
			configRequestParameters.put("config-assign-properties", configPropertiesParameters);
			payloadParameters.put("config-assign-request", configRequestParameters);
		}

		return payloadParameters.toString();

	}

}
