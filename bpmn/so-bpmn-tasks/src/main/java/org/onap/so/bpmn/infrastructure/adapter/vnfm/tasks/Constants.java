/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

/**
 * @author waqas.ikram@est.tech
 */
public class Constants {

    public static final String CREATE_VNF_REQUEST_PARAM_NAME = "createVnfRequest";
    public static final String CREATE_VNF_RESPONSE_PARAM_NAME = "createVnfResponse";

    public static final String DOT = ".";
    public static final String UNDERSCORE = "_";
    public static final String SPACE = "\\s+";

    public static final String VNFM_ADAPTER_DEFAULT_URL = "http://so-vnfm-adapter.onap:9092/so/vnfm-adapter/v1/";
    public static final String VNFM_ADAPTER_DEFAULT_AUTH = "Basic dm5mbTpwYXNzd29yZDEk";


    private Constants() {}
}
