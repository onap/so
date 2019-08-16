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

package org.onap.so.rest.catalog.beans;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(Include.NON_DEFAULT)
public class Cvnfc {

    private String modelCustomizationId;
    private String modelInstanceName;
    private String modelVersionId;
    private String modelInvariantId;
    private String modelVersion;
    private String modelName;
    private String description;
    private String nfcFunction;
    private String nfcNamingCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date created;

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    public String getModelInstanceName() {
        return modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNfcFunction() {
        return nfcFunction;
    }

    public void setNfcFunction(String nfcFunction) {
        this.nfcFunction = nfcFunction;
    }

    public String getNfcNamingCode() {
        return nfcNamingCode;
    }

    public void setNfcNamingCode(String nfcNamingCode) {
        this.nfcNamingCode = nfcNamingCode;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationId", modelCustomizationId)
                .append("modelInstanceName", modelInstanceName).append("modelVersionId", modelVersionId)
                .append("modelInvariantId", modelInvariantId).append("modelVersion", modelVersion)
                .append("modelName", modelName).append("description", description).append("nfcFunction", nfcFunction)
                .append("nfcNamingCode", nfcNamingCode).append("created", created).toString();
    }

}
