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

package org.onap.so.client.policy.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "bbid", "workstep", "treatments"})
public class DictionaryData {

    @JsonProperty("id")
    private Id id;
    @JsonProperty("bbid")
    private Bbid bbid;
    @JsonProperty("workstep")
    private Workstep workstep;
    @JsonProperty("treatments")
    private Treatments treatments;

    @JsonProperty("id")
    public Id getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Id id) {
        this.id = id;
    }

    public DictionaryData withId(Id id) {
        this.id = id;
        return this;
    }

    @JsonProperty("bbid")
    public Bbid getBbid() {
        return bbid;
    }

    @JsonProperty("bbid")
    public void setBbid(Bbid bbid) {
        this.bbid = bbid;
    }

    public DictionaryData withBbid(Bbid bbid) {
        this.bbid = bbid;
        return this;
    }

    @JsonProperty("workstep")
    public Workstep getWorkstep() {
        return workstep;
    }

    @JsonProperty("workstep")
    public void setWorkstep(Workstep workstep) {
        this.workstep = workstep;
    }

    public DictionaryData withWorkstep(Workstep workstep) {
        this.workstep = workstep;
        return this;
    }

    @JsonProperty("treatments")
    public Treatments getTreatments() {
        return treatments;
    }

    @JsonProperty("treatments")
    public void setTreatments(Treatments treatments) {
        this.treatments = treatments;
    }

    public DictionaryData withTreatments(Treatments treatments) {
        this.treatments = treatments;
        return this;
    }

}
