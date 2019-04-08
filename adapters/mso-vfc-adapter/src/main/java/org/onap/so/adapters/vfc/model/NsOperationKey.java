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

package org.onap.so.adapters.vfc.model;

/**
 * The operation key object for NS <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-09-15
 */
public class NsOperationKey {

    /**
     * The subscriber id
     */
    private String globalSubscriberId;

    /**
     * The serviceType
     */
    private String serviceType;

    /**
     * The service ID
     */
    private String serviceId;

    /**
     * The Operation ID
     */
    private String operationId;

    /**
     * the NS template uuid
     */
    private String nodeTemplateUUID;

    /**
     * @return Returns the globalSubscriberId.
     */
    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    /**
     * @param globalSubscriberId The globalSubscriberId to set.
     */
    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

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
     * <br>
     * 
     * @return
     * @since ONAP Amsterdam Release
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * <br>
     * 
     * @param serviceId
     * @since ONAP Amsterdam Release
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * <br>
     * 
     * @return
     * @since ONAP Amsterdam Release
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * <br>
     * 
     * @param operationId
     * @since ONAP Amsterdam Release
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @return Returns the nodeTemplateUUID.
     */
    public String getNodeTemplateUUID() {
        return nodeTemplateUUID;
    }

    /**
     * @param nodeTemplateUUID The nodeTemplateUUID to set.
     */
    public void setNodeTemplateUUID(String nodeTemplateUUID) {
        this.nodeTemplateUUID = nodeTemplateUUID;
    }

}
