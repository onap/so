/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
 * ================================================================================
 * Modifications Copyright (C) 2020 Nordix
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

public final class PayloadConstants {

    private PayloadConstants() {

    }

    public static final String REQUEST = "request";
    public static final String PROPERTIES = "properties";
    public static final String SCOPE = "scope";
    public static final String NSSI_SCOPE = "nssi";
    public static final String ACTION = "action";
    public static final String MODE = "mode";
    public static final String SEPARATOR = "-";
    public static final String PNF_SCOPE = "pnf";
    public static final String VNF_SCOPE = "vnf";
    public static final String VF_MODULE_SCOPE = "vfmodule";
    public static final String SERVICE_SCOPE = "service";
    public static final String RESOLUTION_KEY = "resolution-key";
    public static final String CDS_ACTOR = "cds";

    public static final String PRC_BLUEPRINT_NAME = "PRC_blueprintName";
    public static final String PRC_BLUEPRINT_VERSION = "PRC_blueprintVersion";
    public static final String PRC_CUSTOMIZATION_UUID = "PRC_customizationUuid";
    public static final String PRC_TARGET_SOFTWARE_VERSION = "targetSoftwareVersion";

    public final static String PNF_CORRELATION_ID = "pnfCorrelationId";
    public final static String PNF_UUID = "pnfUuid";
    public final static String SERVICE_INSTANCE_ID = "serviceInstanceId";
    public final static String MODEL_UUID = "modelUuid";

    public final static String TIMEOUT_CONTROLLER_MESSAGE = "timeoutControllerMessage";
    public final static String CONTROLLER_ERROR_MESSAGE = "controllerErrorMessage";
    public final static String CONTROLLER_MSG_TIMEOUT_REACHED = "controllerMessageTimeoutReached";
}
