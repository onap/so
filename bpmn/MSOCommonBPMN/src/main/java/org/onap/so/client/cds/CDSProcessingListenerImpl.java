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

import org.onap.ccsdk.apps.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Status;

/**
 * Response from CDS Client
 *
 */
public class CDSProcessingListenerImpl implements CDSProcessingListener {

	private static final Logger logger = LoggerFactory.getLogger(CDSProcessingListenerImpl.class);

	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String PROCESSING = "Processing";

	/**
	 * Get Response from CDS Client
	 * 
	 */
	@Override
	public void onMessage(ExecutionServiceOutput message) {
		logger.info("Received notification from CDS: {}", message);
		EventType eventType = message.getStatus().getEventType();

		switch (eventType) {

		case EVENT_COMPONENT_FAILURE:
			// failed processing with failure
			AbstractCDSProcessingBBUtils.cdsResponse = FAILED;
			break;
		case EVENT_COMPONENT_PROCESSING:
			// still processing
			AbstractCDSProcessingBBUtils.cdsResponse = PROCESSING;
			break;
		case EVENT_COMPONENT_EXECUTED:
			// done with async processing
			AbstractCDSProcessingBBUtils.cdsResponse = SUCCESS;
			break;
		default:
			AbstractCDSProcessingBBUtils.cdsResponse = FAILED;
			break;
		}
	}

	/**
	 * On error at CDS, log the error
	 */
	@Override
	public void onError(Throwable t) {
		Status status = Status.fromThrowable(t);
		logger.error("Failed processing blueprint {}", status, t);
	}

}
