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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;

/**
 * Use resourceId in resource class instead
 *
 * @author cb645j
 *
 */
// @JsonIgnoreProperties
// TODO update any existing references then remove this pointless class
@Deprecated
public class ResourceInstance extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private String instanceId;
    private String instanceName;


    public String getInstanceId() {
        return instanceId;
    }

    /**
     * This class and method is deprecated so use resourceId field in resource class instead
     *
     * @author cb645j
     *
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

}
