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

package org.onap.so.serviceinstancebeans;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "owningEntity")
@JsonInclude(Include.NON_DEFAULT)
public class OwningEntity implements Serializable {

    private static final long serialVersionUID = -3907033130633428448L;
    @JsonProperty("owningEntityId")
    private String owningEntityId;
    @JsonProperty("owningEntityName")
    private String owningEntityName;

    public String getOwningEntityId() {
        return owningEntityId;
    }

    public void setOwningEntityId(String value) {
        this.owningEntityId = value;
    }

    public String getOwningEntityName() {
        return owningEntityName;
    }

    public void setOwningEntityName(String value) {
        this.owningEntityName = value;
    }

    @Override
    public String toString() {
        return "OwningEntity [owningEntityId=" + owningEntityId + ", owningEntityName=" + owningEntityName + "]";
    }
}
