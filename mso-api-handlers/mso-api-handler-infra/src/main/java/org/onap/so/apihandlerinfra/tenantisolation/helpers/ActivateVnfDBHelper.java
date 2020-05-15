/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.springframework.stereotype.Component;

@Component
public class ActivateVnfDBHelper {



    /**
     * Insert record to OperationalEnvServiceModelStatus table
     * 
     * @param requestId - String
     * @param operationalEnvironmentId - String
     * @param serviceModelVersionId - String
     * @param status - String
     * @param recoveryAction - String
     * @param retryCount - int
     * @param workloadContext - String
     * @return serviceModelStatus - OperationalEnvServiceModelStatus object
     */
    public OperationalEnvServiceModelStatus insertRecordToOperationalEnvServiceModelStatus(String requestId,
            String operationalEnvironmentId, String serviceModelVersionId, String status, String recoveryAction,
            int retryCount, String workloadContext, String vnfOperationalEnvironmentId) {
        OperationalEnvServiceModelStatus serviceModelStatus = new OperationalEnvServiceModelStatus();
        serviceModelStatus.setRequestId(requestId);
        serviceModelStatus.setOperationalEnvId(operationalEnvironmentId);
        serviceModelStatus.setServiceModelVersionId(serviceModelVersionId);
        serviceModelStatus.setServiceModelVersionDistrStatus(status);
        serviceModelStatus.setRecoveryAction(recoveryAction);
        serviceModelStatus.setRetryCount(new Integer(retryCount));
        serviceModelStatus.setWorkloadContext(workloadContext);
        serviceModelStatus.setVnfOperationalEnvId(vnfOperationalEnvironmentId);
        return serviceModelStatus;
    }

    /**
     * Update RetryCount & Status in OperationalEnvServiceModelStatus table
     * 
     * @param serviceModelStatusResponse - OperationalEnvServiceModelStatus object
     * @param status - String
     * @param retryCount - int
     * @return serviceModelStatusResponse - OperationalEnvServiceModelStatus object
     */
    public OperationalEnvServiceModelStatus updateRetryCountAndStatusInOperationalEnvServiceModelStatus(
            OperationalEnvServiceModelStatus serviceModelStatusResponse, String status, int retryCount) {
        serviceModelStatusResponse.setServiceModelVersionDistrStatus(status);
        serviceModelStatusResponse.setRetryCount(new Integer(retryCount));
        return serviceModelStatusResponse;
    }

    /**
     * Insert record to OperationalEnvDistributionStatus table
     * 
     * @param distributionId - String
     * @param operationalEnvironmentId - String
     * @param serviceModelVersionId - String
     * @param requestId - String
     * @param status - String
     * @param distributionIdErrorReason - String
     * @return distStatus - OperationalEnvDistributionStatus object
     */
    public OperationalEnvDistributionStatus insertRecordToOperationalEnvDistributionStatus(String distributionId,
            String operationalEnvironmentId, String serviceModelVersionId, String requestId, String status,
            String distributionIdErrorReason) {
        OperationalEnvDistributionStatus distStatus = new OperationalEnvDistributionStatus();
        distStatus.setDistributionId(distributionId);
        distStatus.setOperationalEnvId(operationalEnvironmentId);
        distStatus.setServiceModelVersionId(serviceModelVersionId);
        distStatus.setRequestId(requestId);
        distStatus.setDistributionIdStatus(status);
        distStatus.setDistributionIdErrorReason(distributionIdErrorReason);
        return distStatus;
    }

    /**
     * Update Status in OperationalEnvDistributionStatus table
     * 
     * @param distributionStatusResponse - OperationalEnvDistributionStatus object
     * @param status - String
     * @param distributionIdErrorReason - String
     * @return distributionStatusResponse - OperationalEnvDistributionStatus object
     */
    public OperationalEnvDistributionStatus updateStatusInOperationalEnvDistributionStatus(
            OperationalEnvDistributionStatus distributionStatusResponse, String status,
            String distributionIdErrorReason) {
        distributionStatusResponse.setDistributionIdStatus(status);
        distributionStatusResponse.setDistributionIdErrorReason(distributionIdErrorReason);
        return distributionStatusResponse;
    }

}
