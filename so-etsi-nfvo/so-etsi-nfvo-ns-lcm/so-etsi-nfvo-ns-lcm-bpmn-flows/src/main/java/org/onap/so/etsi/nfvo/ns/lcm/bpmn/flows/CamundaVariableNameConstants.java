/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class CamundaVariableNameConstants {

    public static final String JOB_ID_PARAM_NAME = "jobId";
    public static final String JOB_BUSINESS_KEY_PARAM_NAME = "jobBusinessKey";
    public static final String CREATE_NS_REQUEST_PARAM_NAME = "createNsRequest";
    public static final String GLOBAL_CUSTOMER_ID_PARAM_NAME = "globalCustomerId";
    public static final String SERVICE_TYPE_PARAM_NAME = "serviceType";


    public static final String NS_PACKAGE_MODEL_PARAM_NAME = "NSPackageModel";
    public static final String CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME =
            "CreateNsWorkflowProcessingException";
    public static final String CREATE_NS_RESPONSE_PARAM_NAME = "createNsResponse";

    public static final String INSTANTIATE_NS_REQUEST_PARAM_NAME = "instantiateNsRequest";
    public static final String OCC_ID_PARAM_NAME = "occId";
    public static final String NS_INSTANCE_ID_PARAM_NAME = "NsInstanceId";
    public static final String NETWORK_SERVICE_DESCRIPTOR_PARAM_NAME = "NetworkServiceDescriptor";
    public static final String VNF_CREATE_INSTANTIATE_REQUESTS = "vnfCreateInstantiateRequests";

    public static final String NF_INST_ID_PARAM_NAME = "NF_INST_ID";
    public static final String CREATE_VNF_RESPONSE_PARAM_NAME = "createVnfResponse";
    public static final String OPERATION_STATUS_PARAM_NAME = "operationStatus";

    private CamundaVariableNameConstants() {}

}
