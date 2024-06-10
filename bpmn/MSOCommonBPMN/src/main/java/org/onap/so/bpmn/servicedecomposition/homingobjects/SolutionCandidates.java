/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.homingobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SolutionCandidates implements Serializable {

    private static final long serialVersionUID = 2188754118148819627L;

    @JsonProperty("requiredCandidates")
    private List<Candidate> requiredCandidates = new ArrayList<Candidate>();
    @JsonProperty("excludedCandidates")
    private List<Candidate> excludedCandidates = new ArrayList<Candidate>();
    @JsonProperty("existingCandidates")
    private List<Candidate> existingCandidates = new ArrayList<Candidate>();
    @JsonProperty("filteringAttributes")
    private List<Candidate> filteringAttributes = new ArrayList<Candidate>();


    public List<Candidate> getRequiredCandidates() {
        return requiredCandidates;
    }

    public void addRequiredCandidates(Candidate requiredCandidate) {
        this.requiredCandidates.add(requiredCandidate);
    }

    public List<Candidate> getExcludedCandidates() {
        return excludedCandidates;
    }

    public void addExcludedCandidates(Candidate excludedCandidate) {
        this.excludedCandidates.add(excludedCandidate);
    }

    public List<Candidate> getExistingCandidates() {
        return existingCandidates;
    }

    public List<Candidate> getFilteringAttributes() {
        return filteringAttributes;
    }

}
