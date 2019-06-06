/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.List;
import org.onap.so.bpmn.servicedecomposition.homingobjects.CandidateType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Candidate implements Serializable {

    private static final long serialVersionUID = -5474502255533410907L;

    @JsonProperty("identifierType")
    private CandidateType identifierType;
    @JsonProperty("identifiers")
    private List<String> identifiers;
    @JsonProperty("cloudOwner")
    private String cloudOwner;

    public Candidate() {}

    public Candidate(CandidateType identifierType, List<String> identifiers, String cloudOwner) {
        this.identifierType = identifierType;
        this.identifiers = identifiers;
        this.cloudOwner = cloudOwner;
    }

    public CandidateType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(CandidateType identifierType) {
        this.identifierType = identifierType;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

}
