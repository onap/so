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

package org.onap.so.client.exception;

import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;

public class BBObjectNotFoundException extends Exception {
    private ResourceKey resourceKey;
    private String resourceValue;

    public BBObjectNotFoundException() {}

    public BBObjectNotFoundException(ResourceKey resourceKey, String resourceValue) {
        super("BBObject" + resourceKey + " was not found in gBBInput using reference value: " + resourceValue);
        this.resourceKey = resourceKey;
        this.resourceValue = resourceValue;
    }

    public ResourceKey getResourceKey() {
        return resourceKey;
    }

    public String getResourceValue() {
        return resourceValue;
    }
}
