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
package org.openecomp.mso.adapters.vfc.model;

/**
 * The operation key object for NS
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Amsterdam Release  2017-09-15
 */
public class NsOperationKey {
    
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
    private String nodeTemplateId;

    /**
     * 
     * <br>
     * 
     * @return
     * @since ONAP Amsterdam Release
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     *     
     * <br>
     * 
     * @param serviceId
     * @since ONAP Amsterdam Release
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 
     * <br>
     * 
     * @return
     * @since ONAP Amsterdam Release
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * 
     * <br>
     * 
     * @param operationId
     * @since ONAP Amsterdam Release
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * 
     * <br>
     * 
     * @return
     * @since ONAP Amsterdam Release
     */
    public String getNodeTemplateId() {
        return nodeTemplateId;
    }

    /**
     * 
     * <br>
     * 
     * @param nodeTemplateId
     * @since ONAP Amsterdam Release
     */
    public void setNodeTemplateId(String nodeTemplateId) {
        this.nodeTemplateId = nodeTemplateId;
    }
    
}
