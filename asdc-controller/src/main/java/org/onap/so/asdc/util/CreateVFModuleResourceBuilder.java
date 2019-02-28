/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
package org.onap.so.asdc.util;

import java.util.Set;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfcCustomization;

public class CreateVFModuleResourceBuilder {

    private Group group;
    private NodeTemplate vfTemplate;
    private ToscaResourceStructure toscaResourceStructure;
    private VfResourceStructure vfResourceStructure;
    private IVfModuleData vfModuleData;
    private VnfResourceCustomization vnfResource;
    private Service service;
    private Set<CvnfcCustomization> existingCvnfcSet;
    private Set<VnfcCustomization> existingVnfcSet;


    public Group getGroup() {
        return group;
    }

    public CreateVFModuleResourceBuilder setGroup(Group group) {
        this.group = group;
        return this;
    }

    public NodeTemplate getVfTemplate() {
        return vfTemplate;
    }

    public CreateVFModuleResourceBuilder setVfTemplate(NodeTemplate vfTemplate) {
        this.vfTemplate = vfTemplate;
        return this;
    }

    public ToscaResourceStructure getToscaResourceStructure() {
        return toscaResourceStructure;
    }

    public CreateVFModuleResourceBuilder setToscaResourceStructure(ToscaResourceStructure toscaResourceStructure) {
        this.toscaResourceStructure = toscaResourceStructure;
        return this;
    }

    public VfResourceStructure getVfResourceStructure() {
        return vfResourceStructure;
    }

    public CreateVFModuleResourceBuilder setVfResourceStructure(VfResourceStructure vfResourceStructure) {
        this.vfResourceStructure = vfResourceStructure;
        return this;
    }

    public IVfModuleData getVfModuleData() {
        return vfModuleData;
    }

    public CreateVFModuleResourceBuilder setVfModuleData(IVfModuleData vfModuleData) {
        this.vfModuleData = vfModuleData;
        return this;
    }

    public VnfResourceCustomization getVnfResource() {
        return vnfResource;
    }

    public CreateVFModuleResourceBuilder setVnfResource(VnfResourceCustomization vnfResource) {
        this.vnfResource = vnfResource;
        return this;
    }

    public Service getService() {
        return service;
    }

    public CreateVFModuleResourceBuilder setService(Service service) {
        this.service = service;
        return this;
    }

    public Set<CvnfcCustomization> getExistingCvnfcSet() {
        return existingCvnfcSet;
    }

    public CreateVFModuleResourceBuilder setExistingCvnfcSet(Set<CvnfcCustomization> existingCvnfcSet) {
        this.existingCvnfcSet = existingCvnfcSet;
        return this;
    }

    public Set<VnfcCustomization> getExistingVnfcSet() {
        return existingVnfcSet;
    }

    public CreateVFModuleResourceBuilder setExistingVnfcSet(Set<VnfcCustomization> existingVnfcSet) {
        this.existingVnfcSet = existingVnfcSet;
        return this;
    }


}
