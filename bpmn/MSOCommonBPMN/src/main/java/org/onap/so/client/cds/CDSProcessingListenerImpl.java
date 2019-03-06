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

import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.so.client.cds.CDSProcessingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Status;

public class CDSProcessingListenerImpl implements CDSProcessingListener {

	private static final Logger logger = LoggerFactory.getLogger(CDSProcessingListenerImpl.class);

	@Override
	public void onMessage(ExecutionServiceOutput message) {
		logger.info("Received notification from CDS: {}", message);
		
		if (message.getStatus().getEventType() == "EVENT-COMPONENT-FAILURE") {
			//failed processing with failure
		} else if (message.getStatus().getEventType() == "EVENT-COMPONENT-PROCESSING") {
			//still processing
		} else if (message.getStatus().getEventType() == "EVENT_COMPONENT_EXECUTED") {
			//done with async processing
		}
	}

	@Override
	public void onError(Throwable t) {
		Status status = Status.fromThrowable(t);
		logger.error("Failed processing blueprint {}", status, t);
	}

}
