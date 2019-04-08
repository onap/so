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

package org.onap.so.bpmn.servicedecomposition.modelinfo;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInfoAllottedResource extends ModelInfoMetadata implements Serializable {

    private static final long serialVersionUID = -5240932898637922018L;

    @JsonProperty("max-instances")
    private String MaxInstances;
    @JsonProperty("min-instances")
    private String MinInstances;
    @JsonProperty("nf-naming-code")
    private String NfNamingCode;
    @JsonProperty("nf-role")
    private String NfRole;
    @JsonProperty("nf-type")
    private String NfType;
    @JsonProperty("nf-function")
    private String NfFunction;
    @JsonProperty("target-network-role")
    private String TarNetworkRole;
    @JsonProperty("providing-service-model-invariant-uuid")
    private String ProvidingServiceModelInvariantUUID;
    @JsonProperty("providing-service-model-name")
    private String ProvidingServiceModelName;
    @JsonProperty("providing-service-model-uuid")
    private String ProvidingServiceModelUUID;
    @JsonProperty("description")
    private String Description;
    @JsonProperty("created")
    private String Created;
    @JsonProperty("tosca-node-type")
    private String ToscaNodeType;
    @JsonProperty("subcategory")
    private String Subcategory;
    // private ModelInfoMetadata modelInfoMetadata;


    public String getMaxInstances() {
        return MaxInstances;
    }

    public void setMaxInstances(String maxInstances) {
        MaxInstances = maxInstances;
    }

    public String getMinInstances() {
        return MinInstances;
    }

    public void setMinInstances(String minInstances) {
        MinInstances = minInstances;
    }

    public String getNfNamingCode() {
        return NfNamingCode;
    }

    public void setNfNamingCode(String nfNamingCode) {
        NfNamingCode = nfNamingCode;
    }

    public String getNfRole() {
        return NfRole;
    }

    public void setNfRole(String nfRole) {
        NfRole = nfRole;
    }

    public String getNfType() {
        return NfType;
    }

    public void setNfType(String nfType) {
        NfType = nfType;
    }

    public String getNfFunction() {
        return NfFunction;
    }

    public void setNfFunction(String nfFunction) {
        NfFunction = nfFunction;
    }

    public String getTarNetworkRole() {
        return TarNetworkRole;
    }

    public void setTarNetworkRole(String tarNetworkRole) {
        TarNetworkRole = tarNetworkRole;
    }

    public String getProvidingServiceModelInvariantUUID() {
        return ProvidingServiceModelInvariantUUID;
    }

    public void setProvidingServiceModelInvariantUUID(String providingServiceModelInvariantUUID) {
        ProvidingServiceModelInvariantUUID = providingServiceModelInvariantUUID;
    }

    public String getProvidingServiceModelName() {
        return ProvidingServiceModelName;
    }

    public void setProvidingServiceModelName(String providingServiceModelName) {
        ProvidingServiceModelName = providingServiceModelName;
    }

    public String getProvidingServiceModelUUID() {
        return ProvidingServiceModelUUID;
    }

    public void setProvidingServiceModelUUID(String providingServiceModelUUID) {
        ProvidingServiceModelUUID = providingServiceModelUUID;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String created) {
        Created = created;
    }

    public String getToscaNodeType() {
        return ToscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        ToscaNodeType = toscaNodeType;
    }

    public String getSubcategory() {
        return Subcategory;
    }

    public void setSubcategory(String subcategory) {
        Subcategory = subcategory;
    }
}
