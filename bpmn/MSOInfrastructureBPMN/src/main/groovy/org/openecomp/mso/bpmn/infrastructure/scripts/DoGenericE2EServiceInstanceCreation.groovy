/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
 * ================================================================================
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
package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>DoGenericE2EServiceInstanceCreation.bpmn</class> process.
 *
 */
public class DoGenericE2EServiceInstanceCreation extends AbstractServiceTaskProcessor {

	String Prefix="DCRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (Execution execution) {
		
	}
	
	public void queryCatalogDB (Execution execution) {
		
	}
	
	public void queryCASRFromSDC (Execution execution) {
		
	}
	
	public void parserTemplate (Execution execution) {
		
	}
	
	public void decomposeService (Execution execution) {
		
	}
	
	public void preparesPutServiceInsToAAI (Execution execution) {
		
	}
	
	public void putServiceInsToAAI (Execution execution) {
		
	}
	
	public void postPutServiceInsToAAI (Execution execution) {
		
	}
	
	public void updateRequestDB (Execution execution) {
		
	}

}
