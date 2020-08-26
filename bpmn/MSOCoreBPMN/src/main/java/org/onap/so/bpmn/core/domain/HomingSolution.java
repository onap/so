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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Stores resources placement and licensing information
 *
 */
@JsonRootName("homingSolution")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomingSolution extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private InventoryType inventoryType;
    private boolean isRehome;
    private String serviceInstanceId; // TODO should start using si object instead
    private String allottedResourceId;
    private String cloudOwner;
    private String cloudRegionId;
    private String aicClli;
    private String aicVersion;
    private String tenant;
    private VnfResource vnf;
    private String oofDirectives;
    private License license = new License();


    /**
     * @return the inventoryType which indicates the solution type
     */
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public boolean isRehome() {
        return isRehome;
    }

    public void setRehome(boolean isRehome) {
        this.isRehome = isRehome;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getAllottedResourceId() {
        return allottedResourceId;
    }

    public void setAllottedResourceId(String allottedResourceId) {
        this.allottedResourceId = allottedResourceId;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public String getCloudRegionId() {
        return cloudRegionId;
    }

    public void setCloudRegionId(String cloudRegionId) {
        this.cloudRegionId = cloudRegionId;
    }

    /**
     * @return the aicClli (aka aic site, physical location id)
     */
    public String getAicClli() {
        return aicClli;
    }

    public void setAicClli(String aicClli) {
        this.aicClli = aicClli;
    }

    public String getAicVersion() {
        return aicVersion;
    }

    public void setAicVersion(String aicVersion) {
        this.aicVersion = aicVersion;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    /**
     * @return the vnf that the resource was homed too.
     */
    public VnfResource getVnf() {
        return vnf;
    }

    public void setVnf(VnfResource vnf) {
        this.vnf = vnf;
    }

    /**
     * @return a map<string, string> key is label name, value is any flavor
     */
    public String getOofDirectives() {
        return oofDirectives;
    }

    public void setOofDirectives(String oofDirectives) {
        this.oofDirectives = oofDirectives;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }


    public static long getSerialversionuid() {
        return serialVersionUID;
    }


}
