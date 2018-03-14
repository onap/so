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

package org.openecomp.mso.serviceinstancebeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "cloudConfiguration")
@JsonInclude(Include.NON_DEFAULT)
public class CloudConfiguration implements Serializable {

	private static final long serialVersionUID = 6260165690180745471L;
	@JsonProperty("aicNodeClli")
    protected String aicNodeClli;
	@JsonProperty("tenantId")
    protected String tenantId;
	@JsonProperty("lcpCloudRegionId")
    protected String lcpCloudRegionId;

    /**
     * Gets the value of the aicNodeClli property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAicNodeClli() {
        return aicNodeClli;
    }

    /**
     * Sets the value of the aicNodeClli property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAicNodeClli(String value) {
        this.aicNodeClli = value;
    }

    /**
     * Gets the value of the tenantId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Sets the value of the tenantId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTenantId(String value) {
        this.tenantId = value;
    }


	public String getLcpCloudRegionId() {
		return lcpCloudRegionId;
	}

	public void setLcpCloudRegionId(String lcpCloudRegionId) {
		this.lcpCloudRegionId = lcpCloudRegionId;
	}

	@Override
	public String toString() {
		return "CloudConfiguration [aicNodeClli=" + aicNodeClli + ", tenantId="
				+ tenantId + ", lcpCloudRegionId=" + lcpCloudRegionId + "]";
	}


}
