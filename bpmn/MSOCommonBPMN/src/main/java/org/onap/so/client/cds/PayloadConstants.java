/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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

    public static String REQUEST = "-request";
    public static String PROPERTIES = "-properties";
    public static String SCOPE = "scope";
    public static String ACTION = "action";
    public static String CONFIG = "config-";
    public static String VNF_SCOPE = "vnf";
    public static String PNF_SCOPE = "pnf";
    public static String VFMODULE_SCOPE = "vf-module";
    public static String RESOLUTION_KEY = "resolution-key";
    public static String SEPARATOR = "-";

    public static final String PRC_BLUEPRINT_NAME = "PRC_blueprintName";
    public static final String PRC_BLUEPRINT_VERSION = "PRC_blueprintVersion";
    public static final String PRC_CUSTOMIZATION_UUID = "PRC_customizationUuid";

    public final static String PNF_CORRELATION_ID = "pnfCorrelationId";
    public final static String PNF_UUID = "pnfUuid";
    public final static String SERVICE_INSTANCE_ID = "serviceInstanceId";
    public final static String MODEL_UUID = "modelUuid";

}
