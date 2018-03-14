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

package org.openecomp.mso.apihandlerinfra.tenantisolation;

import org.openecomp.mso.apihandlerinfra.tenantisolation.process.ActivateVnfOperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolation.process.ActivateVnfStatusOperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolation.process.CreateEcompOperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolation.process.CreateVnfOperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolation.process.DeactivateVnfOperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolation.process.OperationalEnvironmentProcess;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;

public class OperationalEnvironmentProcessFactory {

	public OperationalEnvironmentProcess getOperationalEnvironmentProcess(Action action, String operationalEnvType, CloudOrchestrationRequest cor, String requestId) throws Exception{

		if(Action.create.equals(action)) {
			if(OperationalEnvironment.ECOMP.name().equalsIgnoreCase(operationalEnvType)) {
				return new CreateEcompOperationalEnvironment(cor, requestId);
			} else if(OperationalEnvironment.VNF.name().equalsIgnoreCase(operationalEnvType)) {
				return new CreateVnfOperationalEnvironment(cor, requestId);
			} else {
				throw new Exception("Invalid OperationalEnvironment Type specified for Create Action");
			}
		} else if(Action.activate.equals(action)) {
			return new ActivateVnfOperationalEnvironment(cor, requestId);
		} else if(Action.deactivate.equals(action)) {
			return new DeactivateVnfOperationalEnvironment(cor, requestId);
		} else if(Action.distributionStatus.equals(action)) {
			return new ActivateVnfStatusOperationalEnvironment(cor, requestId);
		} else {
			throw new Exception("Invalid Action specified: " + action);
		}
	}
}
