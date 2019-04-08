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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestDetails")
@JsonInclude(Include.NON_DEFAULT)
public class RequestDetails implements Serializable {

    private static final long serialVersionUID = -73080684945860609L;
    @JsonProperty("modelInfo")
    protected ModelInfo modelInfo;
    @JsonProperty("requestInfo")
    protected RequestInfo requestInfo;
    @JsonProperty("relatedInstanceList")
    protected RelatedInstanceList[] relatedInstanceList;
    @JsonProperty("subscriberInfo")
    protected SubscriberInfo subscriberInfo;
    @JsonProperty("cloudConfiguration")
    protected CloudConfiguration cloudConfiguration;
    @JsonProperty("requestParameters")
    protected RequestParameters requestParameters;
    @JsonProperty("project")
    protected Project project;
    @JsonProperty("owningEntity")
    protected OwningEntity owningEntity;
    @JsonProperty("platform")
    protected Platform platform;
    @JsonProperty("lineOfBusiness")
    protected LineOfBusiness lineOfBusiness;
    @JsonProperty("instanceName")
    private List<Map<String, String>> instanceName = new ArrayList<>();
    @JsonProperty("configurationParameters")
    protected List<Map<String, String>> configurationParameters = new ArrayList<>();


    /**
     * Gets the value of the serviceInfo property.
     *
     * @return possible object is {@link ModelInfo }
     *
     */
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    /**
     * Sets the value of the serviceInfo property.
     *
     * @param value allowed object is {@link ModelInfo }
     *
     */
    public void setModelInfo(ModelInfo value) {
        this.modelInfo = value;
    }

    /**
     * Gets the value of the requestInfo property.
     *
     * @return possible object is {@link RequestInfo }
     *
     */
    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    /**
     * Sets the value of the requestInfo property.
     *
     * @param value allowed object is {@link RequestInfo }
     *
     */
    public void setRequestInfo(RequestInfo value) {
        this.requestInfo = value;
    }

    /**
     * Gets the value of the subscriberInfo property.
     *
     * @return possible object is {@link SubscriberInfo }
     *
     */
    public SubscriberInfo getSubscriberInfo() {
        return subscriberInfo;
    }

    /**
     * Sets the value of the subscriberInfo property.
     *
     * @param value allowed object is {@link SubscriberInfo }
     *
     */
    public void setSubscriberInfo(SubscriberInfo value) {
        this.subscriberInfo = value;
    }

    /**
     * Gets the value of the cloudConfiguration property.
     *
     * @return possible object is {@link CloudConfiguration }
     *
     */
    public CloudConfiguration getCloudConfiguration() {
        return cloudConfiguration;
    }

    /**
     * Sets the value of the cloudConfiguration property.
     *
     * @param value allowed object is {@link CloudConfiguration }
     *
     */
    public void setCloudConfiguration(CloudConfiguration value) {
        this.cloudConfiguration = value;
    }

    /**
     * Gets the value of the requestParameters property.
     *
     * @return possible object is {@link RequestParameters }
     *
     */
    public RequestParameters getRequestParameters() {
        return requestParameters;
    }

    /**
     * Sets the value of the requestParameters property.
     *
     * @param value allowed object is {@link RequestParameters }
     *
     */
    public void setRequestParameters(RequestParameters value) {
        this.requestParameters = value;
    }

    public RelatedInstanceList[] getRelatedInstanceList() {
        return relatedInstanceList;
    }

    public void setRelatedInstanceList(RelatedInstanceList[] relatedInstanceList) {
        this.relatedInstanceList = relatedInstanceList;
    }

    /**
     * Gets the value of the project property.
     *
     * @return possible object is {@link Project }
     *
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the value of the project property.
     *
     * @param value allowed object is {@link Project }
     *
     */
    public void setProject(Project value) {
        this.project = value;
    }

    /**
     * Gets the value of the owningEntity property.
     *
     * @return possible object is {@link OwningEntity }
     *
     */
    public OwningEntity getOwningEntity() {
        return owningEntity;
    }

    /**
     * Sets the value of the owningEntity property.
     *
     * @param value allowed object is {@link OwningEntity }
     *
     */
    public void setOwningEntity(OwningEntity value) {
        this.owningEntity = value;
    }

    /**
     * Gets the value of the platform property.
     *
     * @return possible object is {@link Platform }
     *
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Sets the value of the platform property.
     *
     * @param value allowed object is {@link Platform }
     *
     */
    public void setPlatform(Platform value) {
        this.platform = value;
    }

    /**
     * Gets the value of the lineOfBusiness property.
     *
     * @return possible object is {@link LineOfBusiness }
     *
     */
    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    /**
     * Sets the value of the lineOfBusiness property.
     *
     * @param value allowed object is {@link LineOfBusiness }
     *
     */
    public void setLineOfBusiness(LineOfBusiness value) {
        this.lineOfBusiness = value;
    }

    /**
     * Gets the value of the instanceName property.
     */
    public List<Map<String, String>> getInstanceName() {
        return instanceName;
    }

    /**
     * Sets the value of the instanceName property.
     *
     * @param value
     *
     */
    public void setInstanceName(List<Map<String, String>> instanceName) {
        this.instanceName = instanceName;
    }

    public List<Map<String, String>> getConfigurationParameters() {
        return configurationParameters;
    }

    public void setConfigurationParameters(List<Map<String, String>> configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @Override
    public String toString() {
        return "RequestDetails [modelInfo=" + modelInfo + ", requestInfo=" + requestInfo + ", relatedInstanceList="
                + Arrays.toString(relatedInstanceList) + ", subscriberInfo=" + subscriberInfo + ", cloudConfiguration="
                + cloudConfiguration + ", requestParameters=" + requestParameters + ", project=" + project
                + ", owningEntity=" + owningEntity + ", platform=" + platform + ", lineOfBusiness=" + lineOfBusiness
                + ", instanceName=" + instanceName + ", configurationParameters=" + configurationParameters + "]";
    }
}
