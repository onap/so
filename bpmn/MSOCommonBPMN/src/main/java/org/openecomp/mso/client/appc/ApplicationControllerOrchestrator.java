/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.client.appc;

import java.util.Optional;

import org.openecomp.mso.client.appc.ApplicationControllerSupport.StatusCategory;

import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.Payload;
import org.onap.appc.client.lcm.model.Status;

public class ApplicationControllerOrchestrator {

	public Status vnfCommand(Action action, String requestId, String vnfId, Optional<String> request) throws ApplicationControllerOrchestratorException {
		ApplicationControllerClient client = new ApplicationControllerClient();
		Status status;
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		actionIdentifiers.setVnfId(vnfId);
		Payload payload = null;
		if (request.isPresent()) {
			payload = new Payload(request.get());
		}
		status = client.runCommand(action, actionIdentifiers, payload, requestId);
		if (ApplicationControllerSupport.getCategoryOf(status).equals(StatusCategory.ERROR)) {
			throw new ApplicationControllerOrchestratorException(status.getMessage(), status.getCode());
		} else {
			return status;
		}
	}
}
