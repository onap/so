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

package org.onap.so.apihandlerinfra;


public class Constants {

    public static final String REQUEST_ID_PATH = "/{request-id}";

    public static final String STATUS_SUCCESS = "SUCCESS";

    public static final String MODIFIED_BY_APIHANDLER = "APIH";

    public static final long PROGRESS_REQUEST_COMPLETED = 100L;
    public static final long PROGRESS_REQUEST_RECEIVED = 0L;
    public static final long PROGRESS_REQUEST_IN_PROGRESS = 20L;

    public static final String VNF_TYPE_WILDCARD = "*";

    public static final String VOLUME_GROUP_COMPONENT_TYPE = "VOLUME_GROUP";

    public static final String VALID_INSTANCE_NAME_FORMAT = "^[a-zA-Z][a-zA-Z0-9._-]*$";

    public static final String A_LA_CARTE = "aLaCarte";

    public static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    public static final String VNF_REQUEST_SCOPE = "vnf";
    public static final String SERVICE_INSTANCE_PATH = "/serviceInstances";
    public static final String SERVICE_INSTANTIATION_PATH = "/serviceInstantiation";
    public static final String ORCHESTRATION_REQUESTS_PATH = "/orchestrationRequests";

    public static final String ORCHESTRATION_PATH = "/onap/so/infra";

    private Constants() {}

}
