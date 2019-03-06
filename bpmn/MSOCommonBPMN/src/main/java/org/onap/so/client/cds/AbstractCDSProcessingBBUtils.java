/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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

import org.onap.ccsdk.apps.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.apps.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.client.cds.CDSProcessingClient;
import org.onap.so.client.cds.CDSProcessingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.util.JsonFormat;

@Component
public class AbstractCDSProcessingBBUtils {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCDSProcessingBBUtils.class);

	private static final int TIMEOUT = 30;
	private static final String BLUEPRINT_NAME_PROP = "blueprintName";
	private static final String BLUEPRINT_VERSION_PROP = "blueprintVersion";
	private static final String ACTION_PROP = "actionName";
	private static final String MODE_PROP = "mode";
	private static final String REQUESTOBJECT_PROP = "requestObject";
	private static final String REQUESTID_PROP = "requestId";
	private static final String ORIGINATORID_PROP = "originatorId";
	private static final String SUBREQUESTID_PROP = "subRequestId";

	@Autowired
	private InjectionHelper injectionHelper;

	public void sendRequestToCDSClient(Map<String, String> parameters) {

		final String originatorId = parameters.get(ORIGINATORID_PROP);
		final String requestId = parameters.get(REQUESTID_PROP);
		final String subRequestId = parameters.get(SUBREQUESTID_PROP);
		final String blueprintName = parameters.get(BLUEPRINT_NAME_PROP);
		final String blueprintVersion = parameters.get(BLUEPRINT_VERSION_PROP);
		final String actionName = parameters.get(ACTION_PROP);
		final String mode = parameters.get(MODE_PROP);
		final String requestObject = parameters.get(REQUESTOBJECT_PROP);

		CommonHeader commonHeader = CommonHeader.newBuilder().setOriginatorId(originatorId).setRequestId(requestId)
				.setSubRequestId(subRequestId).build();
		ActionIdentifiers actionIdentifiers = ActionIdentifiers.newBuilder().setBlueprintName(blueprintName)
				.setBlueprintVersion(blueprintVersion).setActionName(actionName).setMode(mode).build();

		Builder struct = Struct.newBuilder();
		try {
			JsonFormat.parser().merge(requestObject, struct);
		} catch (InvalidProtocolBufferException e) {
			logger.error("Failed to parse received message. blueprint({}:{}) for action({}). {}", blueprintVersion,
					blueprintName, actionName, e);
		}

		CDSProcessingListener cdsProcessingListener = new CDSProcessingListenerImpl();

		ExecutionServiceInput executionServiceInput = ExecutionServiceInput.newBuilder().setCommonHeader(commonHeader)
				.setActionIdentifiers(actionIdentifiers).setPayload(struct).build();

		CDSProcessingClient cdsClient = injectionHelper.getCdsClient(cdsProcessingListener);

		CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);

		try {
			countDownLatch.await(TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			logger.error(
					"Caught exception in sendRequestToCDSClient in AbstractCDSProcessingBBUtils" + ex.getMessage());
		} finally {
			cdsClient.close();
		}
		
	}

}
