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
 * <br>
 * <p>
 * </p>
 * response model of query operation status
 * 
 * @author
 * @version SDNO 0.5 September 3, 2016
 */
public class ResponseDescriptor {

    String status;

    String progress;

    String statusDescription;

    Integer errorCode;

    Integer responseId;

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return Returns the progress.
     */
    public String getProgress() {
        return progress;
    }

    /**
     * @param progress The progress to set.
     */
    public void setProgress(String progress) {
        this.progress = progress;
    }

    /**
     * @return Returns the statusDescription.
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * @param statusDescription The statusDescription to set.
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
     * @return Returns the errorCode.
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode The errorCode to set.
     */
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return Returns the responseId.
     */
    public Integer getResponseId() {
        return responseId;
    }

    /**
     * @param responseId The responseId to set.
     */
    public void setResponseId(Integer responseId) {
        this.responseId = responseId;
    }

}
