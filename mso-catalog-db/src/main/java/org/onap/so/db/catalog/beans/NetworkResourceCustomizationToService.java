/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.onap.so.db.catalog.beans;


import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "network_resource_customization_to_service")
public class NetworkResourceCustomizationToService implements Serializable {
    public static final long serialVersionUID = -1322322139926390331L;

    public String getServiceModelUUID() {
        return serviceModelUUID;
    }

    public void setServiceModelUUID(String serviceModelUUID) {
        this.serviceModelUUID = serviceModelUUID;
    }

    public String getResourceModelCustomizationUuid() {
        return resourceModelCustomizationUuid;
    }

    public void setResourceModelCustomizationUuid(String resourceModelCustomizationUuid) {
        this.resourceModelCustomizationUuid = resourceModelCustomizationUuid;
    }

    @Column(name = "SERVICE_MODEL_UUID")
    private String serviceModelUUID;

    @BusinessKey
    @Id
    @Column(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID")
    private String resourceModelCustomizationUuid;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("service_model_uuid", serviceModelUUID)
                .append("resource_model_customization_uuid", resourceModelCustomizationUuid)
                .toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NetworkResourceCustomizationToService)) {
            return false;
        }
        NetworkResourceCustomizationToService castOther = (NetworkResourceCustomizationToService) other;
        return new EqualsBuilder().append(resourceModelCustomizationUuid, castOther.resourceModelCustomizationUuid).isEquals();
    }
}
