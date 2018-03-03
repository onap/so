/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.bpmn.common.workflow.service;

import java.util.Optional;

/**
 * Base class for services that must be process-engine aware. The only
 * process engine currently supported is the "default" process engine.
 */
public class ProcessEngineAwareService {
	
	private final String processEngineName = "default";
	private volatile Optional<ProcessEngineServices> pes4junit = Optional.empty();
	
	/**
	 * Gets the process engine name.
	 * @return the process engine name
	 */
	public String getProcessEngineName() {
		return processEngineName;
	}

	/**
	 * Gets process engine services.
	 * @return process engine services
	 */
	public ProcessEngineServices getProcessEngineServices() {
		return pes4junit.orElse(ProcessEngines.getProcessEngine(
				getProcessEngineName()));
	}

	/**
	 * Allows a particular process engine to be specified, overriding the
	 * usual process engine lookup by name.  Intended primarily for the
	 * unit test environment.
	 * @param pes process engine services
	 */
	public void setProcessEngineServices4junit(ProcessEngineServices pes) {
		pes4junit = Optional.ofNullable(pes);
	}
}
