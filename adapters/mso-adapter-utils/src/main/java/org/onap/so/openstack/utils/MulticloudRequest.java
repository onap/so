/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Intel Corp. All rights reserved.
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

package org.onap.so.openstack.utils;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.woorea.openstack.heat.model.CreateStackParam;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"generic-vnf-id", "vf-module-id", "vf-module-model-invariant-id", "vf-module-model-version-id",
        "vf-module-model-customization-id", "oof_directives", "sdnc_directives", "user_directives", "template_type",
        "template_data"})
public class MulticloudRequest implements Serializable {
    private static final long serialVersionUID = -5215028275577848311L;

    @JsonProperty("generic-vnf-id")
    private String genericVnfId;
    @JsonProperty("vf-module-id")
    private String vfModuleId;
    @JsonProperty("vf-module-model-invariant-id")
    private String vfModuleModelInvariantId;
    @JsonProperty("vf-module-model-version-id")
    private String vfModuleModelVersionId;
    @JsonProperty("vf-module-model-customization-id")
    private String vfModuleModelCustomizationId;
    @JsonProperty("oof_directives")
    private JsonNode oofDirectives;
    @JsonProperty("sdnc_directives")
    private JsonNode sdncDirectives;
    @JsonProperty("user_directives")
    private JsonNode userDirectives;
    @JsonProperty("template_type")
    private String templateType;
    @JsonProperty("template_data")
    private CreateStackParam templateData;


    @JsonProperty("generic-vnf-id")
    public String getGenericVnfId() {
        return genericVnfId;
    }

    @JsonProperty("generic-vnf-id")
    public void setGenericVnfId(String genericVnfId) {
        this.genericVnfId = genericVnfId;
    }

    @JsonProperty("vf-module-id")
    public String getVfModuleId() {
        return vfModuleId;
    }

    @JsonProperty("vf-module-id")
    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    @JsonProperty("vf-module-model-invariant-id")
    public String getVfModuleModelInvariantId() {
        return vfModuleModelInvariantId;
    }

    @JsonProperty("vf-module-model-invariant-id")
    public void setVfModuleModelInvariantId(String vfModuleModelInvariantId) {
        this.vfModuleModelInvariantId = vfModuleModelInvariantId;
    }

    @JsonProperty("vf-module-model-version-id")
    public String getVfModuleModelVersionId() {
        return vfModuleModelVersionId;
    }

    @JsonProperty("vf-module-model-version-id")
    public void setVfModuleModelVersionId(String vfModuleModelVersionId) {
        this.vfModuleModelVersionId = vfModuleModelVersionId;
    }

    @JsonProperty("vf-module-model-customization-id")
    public String getVfModuleModelCustomizationId() {
        return vfModuleModelCustomizationId;
    }

    @JsonProperty("vf-module-model-customization-id")
    public void setVfModuleModelCustomizationId(String vfModuleModelCustomizationId) {
        this.vfModuleModelCustomizationId = vfModuleModelCustomizationId;
    }

    @JsonProperty("oof_directives")
    public JsonNode getOofDirectives() {
        return oofDirectives;
    }

    @JsonProperty("oof_directives")
    public void setOofDirectives(JsonNode oofDirectives) {
        this.oofDirectives = oofDirectives;
    }

    @JsonProperty("sdnc_directives")
    public JsonNode getSdncDirectives() {
        return sdncDirectives;
    }

    @JsonProperty("sdnc_directives")
    public void setSdncDirectives(JsonNode sdncDirectives) {
        this.sdncDirectives = sdncDirectives;
    }

    @JsonProperty("user_directives")
    public JsonNode getUserDirectives() {
        return userDirectives;
    }

    @JsonProperty("user_directives")
    public void setUserDirectives(JsonNode userDirectives) {
        this.userDirectives = userDirectives;
    }

    @JsonProperty("template_type")
    public String getTemplateType() {
        return templateType;
    }

    @JsonProperty("template_type")
    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    @JsonProperty("template_data")
    public CreateStackParam getTemplateData() {
        return templateData;
    }

    @JsonProperty("template_data")
    public void setTemplateData(CreateStackParam templateData) {
        this.templateData = templateData;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("genericVnfId", genericVnfId).append("vfModuleId", vfModuleId)
                .append("vfModuleModelInvariantId", vfModuleModelInvariantId)
                .append("vfModuleModelVersionId", vfModuleModelVersionId)
                .append("vfModuleModelCustomizationId", vfModuleModelCustomizationId)
                .append("oofDirectives", oofDirectives).append("sdncDirectives", sdncDirectives)
                .append("userDirectives", userDirectives).append("templateType", templateType)
                .append("templateData", templateData).toString();
    }

}
