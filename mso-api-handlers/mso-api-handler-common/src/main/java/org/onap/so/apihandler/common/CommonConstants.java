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

package org.onap.so.apihandler.common;


public final class CommonConstants {

    public static final String DEFAULT_BPEL_AUTH = "admin:admin";
    public static final String ENCRYPTION_KEY_PROP = "org.onap.so.adapters.network.encryptionKey";

    public static final String REQUEST_ID_HEADER = "mso-request-id";
    public static final String REQUEST_TIMEOUT_HEADER = "mso-service-request-timeout";
    public static final String SCHEMA_VERSION_HEADER = "mso-schema-version";
    public static final String SERVICE_INSTANCE_ID_HEADER = "mso-service-instance-id";
    public static final String ACTION_HEADER = "mso-action";

    public static final String G_REQUEST_ID = "mso-request-id";
    public static final String G_SERVICEINSTANCEID = "serviceInstanceId";
    public static final String G_ACTION = "gAction";

    public static final String CAMUNDA_SERVICE_INPUT = "bpmnRequest";
    public static final String CAMUNDA_ROOT_INPUT = "variables";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CAMUNDA_TYPE = "type";
    public static final String CAMUNDA_VALUE = "value";
    public static final String CAMUNDA_URL = "mso.camundaURL";
    public static final String CAMUNDA_AUTH = "mso.camundaAuth";
    public static final String BPEL_SEARCH_STR = "active-bpel";
    public static final String TASK_SEARCH_STR = "task";
    public static final int BPEL = 0;
    public static final int CAMUNDA = 1;
    public static final int CAMUNDATASK = 2;
    public static final String CAMUNDA_HOST = "host";
    public static final String REQUEST_ID_VARIABLE = "requestId";
    public static final String IS_BASE_VF_MODULE_VARIABLE = "isBaseVfModule";
    public static final String RECIPE_TIMEOUT_VARIABLE = "recipeTimeout";
    public static final String REQUEST_ACTION_VARIABLE = "requestAction";
    public static final String SERVICE_INSTANCE_ID_VARIABLE = "serviceInstanceId";
    public static final String OPERATION_TYPE = "operationType";
    public static final String PNF_CORRELATION_ID = "pnfCorrelationId";
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
    public static final String ALACARTE_TEST_API = "mso.infra.default.alacarte.testApi";
    public static final String MACRO_TEST_API = "mso.infra.service.macro.default.testApi";
    public static final String ALACARTE = "aLaCarte";
    public static final String API_VERSION = "apiVersion";
    public static final String REQUEST_URI = "requestUri";
    public static final String API_MINOR_VERSION = "mso.infra.default.versions.apiMinorVersion";
    public static final String API_PATCH_VERSION = "mso.infra.default.versions.apiPatchVersion";
    public static final String X_TRANSACTION_ID = "X-TransactionID";
    public static final String X_MINOR_VERSION = "X-MinorVersion";
    public static final String X_PATCH_VERSION = "X-PatchVersion";
    public static final String X_LATEST_VERSION = "X-LatestVersion";
    public static final String INSTANCE_GROUP_ID = "instanceGroupId";
    public static final String INSTANCE_GROUP_INSTANCE_ID = "instanceGroupInstanceId";
    public static final String GENERATE_IDS = "generateIdsOnly";

    private CommonConstants() {
        // prevent creating an instance of this class
    }
}
