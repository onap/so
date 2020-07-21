/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.validation;

import java.util.Map;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.serviceinstancebeans.LineOfBusiness;
import org.onap.so.serviceinstancebeans.OwningEntity;
import org.onap.so.serviceinstancebeans.Platform;
import org.onap.so.serviceinstancebeans.Project;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;

public class ValidationInformation {
    ServiceInstancesRequest sir;
    Map<String, String> instanceIdMap;
    Actions action;
    int reqVersion;
    String requestScope;
    Boolean aLaCarteFlag;
    RequestParameters requestParameters;
    RequestInfo requestInfo;
    String serviceInstanceType;
    String vfModuleModelName;
    String vnfType;
    String asdcServiceModelVersion;
    String vfModuleType;
    String networkType;
    Platform platform;
    LineOfBusiness lob;
    Project project;
    OwningEntity owningEntity;
    Service userParams;

    public ValidationInformation(ServiceInstancesRequest sir, Map<String, String> instanceIdMap, Actions action,
            int reqVersion, Boolean aLaCarteFlag, RequestParameters requestParameters) {
        this.sir = sir;
        this.instanceIdMap = instanceIdMap;
        this.action = action;
        this.reqVersion = reqVersion;
        this.aLaCarteFlag = aLaCarteFlag;
        this.requestParameters = requestParameters;
    }

    public ServiceInstancesRequest getSir() {
        return this.sir;
    }

    public void setSir(ServiceInstancesRequest value) {
        this.sir = value;
    }

    public Map<String, String> getInstanceIdMap() {
        return this.instanceIdMap;
    }

    public void setInstanceIdMap(Map<String, String> value) {
        this.instanceIdMap = value;
    }

    public Actions getAction() {
        return this.action;
    }

    public void setAction(Actions value) {
        this.action = value;
    }

    public RequestInfo getRequestInfo() {
        return this.requestInfo;
    }

    public void setRequestInfo(RequestInfo value) {
        this.requestInfo = value;
    }

    public int getReqVersion() {
        return this.reqVersion;
    }

    public void setReqVersion(int value) {
        this.reqVersion = value;
    }

    public String getRequestScope() {
        return this.requestScope;
    }

    public void setRequestScope(String value) {
        this.requestScope = value;
    }

    public Boolean getALaCarteFlag() {
        return this.aLaCarteFlag;
    }

    public void setALaCarteFlag(Boolean value) {
        this.aLaCarteFlag = value;
    }

    public RequestParameters getReqParameters() {
        return this.requestParameters;
    }

    public void setReqParameters(RequestParameters value) {
        this.requestParameters = value;
    }

    public String getServiceInstanceType() {
        return this.serviceInstanceType;
    }

    public void setServiceInstanceType(String value) {
        this.serviceInstanceType = value;
    }

    public String getVfModuleModelName() {
        return this.vfModuleModelName;
    }

    public void setVfModuleModelName(String value) {
        this.vfModuleModelName = value;
    }

    public String getVnfType() {
        return this.vnfType;
    }

    public void setVnfType(String value) {
        this.vnfType = value;
    }

    public String getAsdcServiceModelVersion() {
        return this.asdcServiceModelVersion;
    }

    public void setAsdcServiceModelVersion(String value) {
        this.asdcServiceModelVersion = value;
    }

    public String getVfModuleType() {
        return this.vfModuleType;
    }

    public void setVfModuleType(String value) {
        this.vfModuleType = value;
    }

    public String getNetworkType() {
        return this.networkType;
    }

    public void setNetworkType(String value) {
        this.networkType = value;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public void setPlatform(Platform value) {
        this.platform = value;
    }

    public LineOfBusiness getLOB() {
        return this.lob;
    }

    public void setLOB(LineOfBusiness value) {
        this.lob = value;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project value) {
        this.project = value;
    }

    public OwningEntity getOE() {
        return this.owningEntity;
    }

    public void setOE(OwningEntity value) {
        this.owningEntity = value;
    }

    public Service getUserParams() {
        return this.userParams;
    }

    public void setUserParams(Service value) {
        this.userParams = value;
    }
}
