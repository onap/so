/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019, CMCC Technologies Co., Ltd.
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "service_info")
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @BusinessKey
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "SERVICE_INPUT")
    private String serviceInput;

    @Column(name = "SERVICE_PROPERTIES")
    private String serviceProperties;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SERVICE_MODEL_UUID")
    private Service service;

    public Integer getId() {
        return id;
    }

    public void setId(Integer serviceInfoId) {
        this.id = serviceInfoId;
    }

    public String getServiceInput() {
        return serviceInput;
    }

    public void setServiceInput(String serviceInput) {
        this.serviceInput = serviceInput;
    }

    public String getServiceProperties() {
        return serviceProperties;
    }

    public void setServiceProperties(String serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("serviceProperties", serviceProperties)
                .append("serviceInput", serviceInput).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ServiceInfo that = (ServiceInfo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
