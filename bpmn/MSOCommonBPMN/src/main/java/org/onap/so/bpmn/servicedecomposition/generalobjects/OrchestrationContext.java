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

package org.onap.so.bpmn.servicedecomposition.generalobjects;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("orchestration-context")
public class OrchestrationContext implements Serializable {

    private static final long serialVersionUID = 6843015923244810369L;

    @JsonProperty("is-rollback-enabled")
    private Boolean isRollbackEnabled;

    public Boolean getIsRollbackEnabled() {
        return this.isRollbackEnabled;
    }

    public void setIsRollbackEnabled(Boolean isRollbackEnabled) {
        this.isRollbackEnabled = isRollbackEnabled;
    }
}
