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

package org.openecomp.mso.apihandler.camundabeans;

import org.openecomp.mso.apihandler.common.CommonConstants;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JavaBean JSON class for a "gMyServiceInput" which contains the xml payload that
 * will be passed to the Camunda process
 */


public class CamundaBooleanInput {

    @JsonProperty(CommonConstants.CAMUNDA_VALUE)
    private boolean value;
    @JsonProperty(CommonConstants.CAMUNDA_TYPE)
    private static final String type = "Boolean";


    public CamundaBooleanInput() {
    }

    @JsonProperty(CommonConstants.CAMUNDA_VALUE)
    public boolean getValue() {
        return value;
    }

    @JsonProperty(CommonConstants.CAMUNDA_VALUE)
    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CamundaInput [value=" + value + ", type=" + type + "]";
    }


}
