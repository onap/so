/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.svnfm.simulator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@Entity
@Table(name = "VNF_INSTANCE")
public class VnfInstance {
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    private String vnfInstanceName;
    private String vnfInstanceDescription;
    private String vnfdId;
    private String vnfProvider;
    private String vnfProductName;
    private String vnfSoftwareVersion;
    private String vnfdVersion;
    private String vnfPkgId;
    private String vnfConfigurableProperties;
    private String vimConnectionInfo;
    private String vnfInstantiationState;
    private String instantiatedVnfInfo;
    private String metadata;
    private String extensions;
    private String links;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(final String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getVnfInstanceDescription() {
        return vnfInstanceDescription;
    }

    public void setVnfInstanceDescription(final String vnfInstanceDescription) {
        this.vnfInstanceDescription = vnfInstanceDescription;
    }

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public String getVnfProvider() {
        return vnfProvider;
    }

    public void setVnfProvider(final String vnfProvider) {
        this.vnfProvider = vnfProvider;
    }

    public String getVnfProductName() {
        return vnfProductName;
    }

    public void setVnfProductName(final String vnfProductName) {
        this.vnfProductName = vnfProductName;
    }

    public String getVnfSoftwareVersion() {
        return vnfSoftwareVersion;
    }

    public void setVnfSoftwareVersion(final String vnfSoftwareVersion) {
        this.vnfSoftwareVersion = vnfSoftwareVersion;
    }

    public String getVnfdVersion() {
        return vnfdVersion;
    }

    public void setVnfdVersion(final String vnfdVersion) {
        this.vnfdVersion = vnfdVersion;
    }

    public String getVnfPkgId() {
        return vnfPkgId;
    }

    public void setVnfPkgId(final String vnfPkgId) {
        this.vnfPkgId = vnfPkgId;
    }

    public String getVnfConfigurableProperties() {
        return vnfConfigurableProperties;
    }

    public void setVnfConfigurableProperties(final String vnfConfigurableProperties) {
        this.vnfConfigurableProperties = vnfConfigurableProperties;
    }

    public String getVimConnectionInfo() {
        return vimConnectionInfo;
    }

    public void setVimConnectionInfo(final String vimConnectionInfo) {
        this.vimConnectionInfo = vimConnectionInfo;
    }

    public String getInstantiationState() {
        return vnfInstantiationState;
    }

    public void setVnfInstantiationState(final String vnfInstantiationState) {
        this.vnfInstantiationState = vnfInstantiationState;
    }

    public String getVnfInstantiationState() {
        return instantiatedVnfInfo;
    }

    public void setInstantiatedVnfInfo(final String instantiatedVnfInfo) {
        this.instantiatedVnfInfo = instantiatedVnfInfo;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public String getExtensions() {
        return extensions;
    }

    public void setExtensions(final String extensions) {
        this.extensions = extensions;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(final String links) {
        this.links = links;
    }
}
