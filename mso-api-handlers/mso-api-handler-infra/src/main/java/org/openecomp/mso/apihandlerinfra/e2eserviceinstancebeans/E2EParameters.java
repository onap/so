/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties({ "additionalProperties" })
public class E2EParameters {

	@JsonProperty("globalSubscriberId")
	private String globalSubscriberId;

	@JsonProperty("subscriberName")
	private String subscriberName;
	
	@JsonProperty("serviceType")
	private String serviceType;
	
	@JsonProperty("templateName")
	private String templateName;
	

	@JsonProperty("resources")
	private List<ResourceRequest> resources;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	
    /**
     * @return Returns the serviceType.
     */
    public String getServiceType() {
        return serviceType;
    }
    
    /**
     * @param serviceType The serviceType to set.
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
   
    /**
     * @return Returns the templateName.
     */
    public String getTemplateName() {
        return templateName;
    }
    
    /**
     * @param templateName The templateName to set.
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getGlobalSubscriberId() {
		return globalSubscriberId;
	}

	public void setGlobalSubscriberId(String globalSubscriberId) {
		this.globalSubscriberId = globalSubscriberId;
	}

	public String getSubscriberName() {
		return subscriberName;
	}

	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}

	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
    
    /**
     * @return Returns the resources.
     */
    public List<ResourceRequest> getResources() {
        return resources;
    }
    
    /**
     * @param resources The resources to set.
     */
    public void setResources(List<ResourceRequest> resources) {
        this.resources = resources;
    }

}
