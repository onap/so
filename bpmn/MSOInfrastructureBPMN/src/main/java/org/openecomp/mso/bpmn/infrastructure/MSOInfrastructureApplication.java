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

package org.openecomp.mso.bpmn.infrastructure;

import java.util.List;

/**
 * @since Version 1.0
 *
 */
@ProcessApplication("MSO Infrastructure Application")
public class MSOInfrastructureApplication extends ServletProcessApplication {
	
	private MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	
	@PostDeploy
	public void postDeploy(ProcessEngine processEngineInstance) {
		long startTime = System.currentTimeMillis();
		
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Post deployment complete...");		
	}
	
	@PreUndeploy
	public void cleanup(ProcessEngine processEngine, ProcessApplicationInfo processApplicationInfo, List<ProcessEngine> processEngines) {
		long startTime = System.currentTimeMillis();
		
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Pre Undeploy complete...");	
		
	}
	
}
