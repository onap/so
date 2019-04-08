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

public class ModelInfoCollection implements Serializable {

    private static final long serialVersionUID = 8380534468706675508L;

    @JsonProperty("model-customization-uuid")
    private String modelCustomizationUUID;
    @JsonProperty("model-version-id")
    private String modelVersionId;
    @JsonProperty("model-invariant-uuid")
    private String modelInvariantUUID;
    @JsonProperty("collection-function")
    private String collectionFunction;
    @JsonProperty("collection-role")
    private String collectionRole;
    @JsonProperty("collection-type")
    private String collectionType;
    @JsonProperty("description")
    private String description;
    @JsonProperty("quantity")
    private Integer quantity;

    public String getModelCustomizationUUID() {
        return modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getModelInvariantUUID() {
        return this.modelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        this.modelInvariantUUID = modelInvariantUUID;
    }

    public String getCollectionFunction() {
        return collectionFunction;
    }

    public void setCollectionFunction(String collectionFunction) {
        this.collectionFunction = collectionFunction;
    }

    public String getCollectionRole() {
        return collectionRole;
    }

    public void setCollectionRole(String collectionRole) {
        this.collectionRole = collectionRole;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }



}
