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

package org.openecomp.mso.apihandler.common;


public final class CommonConstants {

    public static final String DEFAULT_BPEL_AUTH = "admin:admin";
    public static final String ENCRYPTION_KEY = "aa3871669d893c7fb8abbcda31b88b4f";

    public static final String REQUEST_ID_HEADER = "mso-request-id";
    public static final String REQUEST_TIMEOUT_HEADER = "mso-service-request-timeout";
    public static final String SCHEMA_VERSION_HEADER = "mso-schema-version";
    public static final String SERVICE_INSTANCE_ID_HEADER = "mso-service-instance-id";
    public static final String ACTION_HEADER = "mso-action";

    public static final String CAMUNDA_SERVICE_INPUT = "bpmnRequest";
	public static final String CAMUNDA_ROOT_INPUT = "variables";
	public static final String CONTENT_TYPE_JSON= "application/json";
	public static final String CAMUNDA_TYPE = "type";
	public static final String CAMUNDA_VALUE = "value";
	public static final String CAMUNDA_URL = "camundaURL";
	public static final String CAMUNDA_AUTH = "camundaAuth";
	public static final String BPEL_SEARCH_STR = "active-bpel";
	public static final String TASK_SEARCH_STR = "task";
	public static final String BPEL_URL = "bpelURL";
	public static final String BPEL_AUTH = "bpelAuth";
	public static final int BPEL = 0;
	public static final int CAMUNDA = 1;
	public static final int CAMUNDATASK = 2;
	public static final String CAMUNDA_HOST = "host";
	public static final String SDNC_UUID_HEADER = "mso-sdnc-request-id";
	
	public static final String REQUEST_ID_VARIABLE = "requestId";
	public static final String IS_BASE_VF_MODULE_VARIABLE = "isBaseVfModule";
	public static final String RECIPE_TIMEOUT_VARIABLE = "recipeTimeout";
	public static final String REQUEST_ACTION_VARIABLE = "requestAction";
	public static final String SERVICE_INSTANCE_ID_VARIABLE = "serviceInstanceId";
	public static final String VNF_ID_VARIABLE = "vnfId";
	public static final String VF_MODULE_ID_VARIABLE = "vfModuleId";
	public static final String VOLUME_GROUP_ID_VARIABLE = "volumeGroupId";
	public static final String NETWORK_ID_VARIABLE = "networkId";
	public static final String CONFIGURATION_ID_VARIABLE = "configurationId";
	public static final String SERVICE_TYPE_VARIABLE = "serviceType";
	public static final String VNF_TYPE_VARIABLE = "vnfType";
	public static final String VF_MODULE_TYPE_VARIABLE = "vfModuleType";
	public static final String NETWORK_TYPE_VARIABLE = "networkType";
	public static final String REQUEST_DETAILS_VARIABLE = "requestDetails";	
	public static final String ALACARTE_ORCHESTRATION = "mso.infra.default.alacarte.orchestrationUri";
	public static final String ALACARTE_RECIPE_TIMEOUT = "mso.infra.default.alacarte.recipeTimeout";
	public static final String RECIPE_PARAMS = "recipeParams";
	
	private CommonConstants () {
	    // prevent creating an instance of this class
	}
}
