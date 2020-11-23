/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Intel Corp.  All rights reserved.
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

package org.onap.so.client.adapter.vnf.mapper;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AttributeNameValue implements Serializable {
    private static final long serialVersionUID = -5215028275587848311L;

    @JsonProperty("attribute_name")
    private String attributeName;
    @JsonProperty("attribute_value")
    private transient Object attributeValue;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{\"attribute_name\": \"").append(attributeName)
                .append("\", \"attribute_value\": \"").append(attributeValue.toString()).append("\"}").toString();
    }
}
