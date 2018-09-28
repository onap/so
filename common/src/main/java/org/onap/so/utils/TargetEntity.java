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

package org.onap.so.utils;

import java.util.EnumSet;

public enum TargetEntity {
    OPENSTACK_ADAPTER, BPMN, GRM ,AAI, DMAAP, POLICY, CATALOG_DB, REQUEST_DB,
    VNF_ADAPTER, SDNC_ADAPTER, NARAD, MULTICLOUD;

    private static final String PREFIX = "SO";
    
    public static EnumSet<TargetEntity> getSOInternalComponents() {
        return EnumSet.of(OPENSTACK_ADAPTER, BPMN,CATALOG_DB,REQUEST_DB,VNF_ADAPTER,SDNC_ADAPTER);
    }
    
    @Override
    public String toString(){
        if(getSOInternalComponents().contains(this))
            return TargetEntity.PREFIX + "." + this.name();
        else
            return this.name();
    }
}