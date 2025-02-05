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

package org.onap.so.apihandlerinfra.tenantisolation.process;


import java.util.List;
import java.util.Optional;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.ActivateVnfDBHelper;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.SDCClientHelper;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Distribution;
import org.onap.so.apihandlerinfra.tenantisolationbeans.DistributionStatus;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.requestsdb.RequestsDBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivateVnfStatusOperationalEnvironment {

    private static Logger logger = LoggerFactory.getLogger(ActivateVnfStatusOperationalEnvironment.class);
    private String origRequestId = "";
    private String errorMessage = "";
    private OperationalEnvDistributionStatus queryDistributionDbResponse = null;
    private OperationalEnvServiceModelStatus queryServiceModelResponse = null;
    private boolean isOverallSuccess = false;

    private static final int RETRY_COUNT_ZERO = 0;
    private static final String ERROR_REASON_ABORTED = "ABORTED";
    private static final String RECOVERY_ACTION_RETRY = "RETRY";
    private static final String RECOVERY_ACTION_ABORT = "ABORT";
    private static final String RECOVERY_ACTION_SKIP = "SKIP";
    private static final String DISTRIBUTION_STATUS_OK = DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString();
    private static final String DISTRIBUTION_STATUS_ERROR = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
    private static final String DISTRIBUTION_STATUS_SENT = "SENT";

    @Autowired
    private ActivateVnfDBHelper dbHelper;
    @Autowired
    private RequestsDBHelper requestDb;
    @Autowired
    private SDCClientHelper sdcClientHelper;
    @Autowired
    private RequestsDbClient client;
    @Autowired
    private AAIClientHelper aaiHelper;

    /**
     * The Point-Of-Entry from APIH with activate status from SDC
     * 
     * @param requestId - String
     * @param request - CloudOrchestrationRequest - object
     * @return void - nothing
     */
    public void execute(String requestId, CloudOrchestrationRequest request) throws ApiException {

        try {

            String operationalEnvironmentId = "";

            String sdcDistributionId = request.getDistributionId();
            Distribution sdcStatus = request.getDistribution();

            // Distribution, Query for operationalEnvironmentId, serviceModelVersionId, origRequestId
            this.queryDistributionDbResponse = client.getDistributionStatusById(sdcDistributionId);
            operationalEnvironmentId = this.queryDistributionDbResponse.getOperationalEnvId();
            this.origRequestId = this.queryDistributionDbResponse.getRequestId();

            // ServiceModel, Query for recoveryAction, retryCountString
            this.queryServiceModelResponse =
                    client.findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId(operationalEnvironmentId,
                            queryDistributionDbResponse.getServiceModelVersionId(), this.origRequestId);

            processActivateSDCStatus(sdcDistributionId, sdcStatus, this.queryDistributionDbResponse,
                    this.queryServiceModelResponse);

            // After EVERY status processed, need to query the status of all service modelId
            // to determine the OVERALL status if "COMPLETE" or "FAILURE":
            checkOrUpdateOverallStatus(operationalEnvironmentId, this.origRequestId);

            // Update AAI to ACTIVE if Overall success
            if (isOverallSuccess) {
                String vnfOperationalEnvironmentId = this.queryServiceModelResponse.getVnfOperationalEnvId();
                OperationalEnvironment aaiOpEnv = getAAIOperationalEnvironment(vnfOperationalEnvironmentId);
                if (aaiOpEnv != null) {
                    aaiOpEnv.setOperationalEnvironmentStatus("ACTIVE");
                    aaiHelper.updateAaiOperationalEnvironment(vnfOperationalEnvironmentId, aaiOpEnv);
                } else {
                    requestDb.updateInfraFailureCompletion("Unable to update ACTIVATE status in AAI. ",
                            this.origRequestId, this.queryServiceModelResponse.getVnfOperationalEnvId());
                }
            }

        } catch (Exception e) {
            logger.error("Exception in execute", e);
            requestDb.updateInfraFailureCompletion(e.getMessage(), this.origRequestId,
                    this.queryServiceModelResponse.getVnfOperationalEnvId());
        }

    }

    /**
     * The Method to process the Activation Status from SDC
     * 
     * @param sdcDistributionId - string
     * @param sdcStatus - Distribution object
     * @param queryDistributionDbResponse - OperationalEnvDistributionStatus object
     * @param queryServiceModelResponse - OperationalEnvServiceModelStatus object
     * @return void - nothing
     */
    public void processActivateSDCStatus(String sdcDistributionId, Distribution sdcStatus,
            OperationalEnvDistributionStatus queryDistributionDbResponse,
            OperationalEnvServiceModelStatus queryServiceModelResponse) throws ApiException {

        String sdcStatusValue = sdcStatus.getStatus().toString();
        String recoveryAction = queryServiceModelResponse.getRecoveryAction();
        int retryCount = queryServiceModelResponse.getRetryCount();

        // Validate/process status
        if (sdcStatus.getStatus().toString().equals(DISTRIBUTION_STATUS_OK)) {
            // should update 1 row, update status to "DISTRIBUTION_COMPLETE_OK"
            OperationalEnvDistributionStatus updateDistStatusOk = dbHelper
                    .updateStatusInOperationalEnvDistributionStatus(queryDistributionDbResponse, sdcStatusValue, "");
            client.save(updateDistStatusOk);
            // should update 1 row, update status and retryCount = 0 (ie, serviceModelVersionId is DONE!)
            OperationalEnvServiceModelStatus updateRetryCountZeroAndStatusOk =
                    dbHelper.updateRetryCountAndStatusInOperationalEnvServiceModelStatus(queryServiceModelResponse,
                            sdcStatusValue, RETRY_COUNT_ZERO);
            client.save(updateRetryCountZeroAndStatusOk);
        } else {

            // "DISTRIBUTION_COMPLETE_ERROR", Check if recoveryAction is "RETRY"
            if (recoveryAction.equals(RECOVERY_ACTION_RETRY) && retryCount > RETRY_COUNT_ZERO) {

                // RESEND / RETRY serviceModelVersionId to SDC

                JSONObject jsonResponse =
                        callSDClientForRetry(queryDistributionDbResponse, queryServiceModelResponse, sdcStatus);

            } else { // either RETRY & Count = 0, or 'ABORT', or 'SKIP'

                if (recoveryAction.equals(RECOVERY_ACTION_SKIP) || recoveryAction.equals(RECOVERY_ACTION_ABORT)) {
                    String modifiedStatus = "";
                    String errorReason = "";
                    if (recoveryAction.equals(RECOVERY_ACTION_SKIP)) { // considered SUCCESS
                        modifiedStatus = DISTRIBUTION_STATUS_OK;
                    } else {
                        if (recoveryAction.equals(RECOVERY_ACTION_ABORT)) {
                            modifiedStatus = DISTRIBUTION_STATUS_ERROR; // ABORT, error
                            errorReason = ERROR_REASON_ABORTED;
                        }
                    }

                    sdcStatusValue = modifiedStatus;
                    OperationalEnvServiceModelStatus updateRetryCountZeroAndStatus =
                            dbHelper.updateRetryCountAndStatusInOperationalEnvServiceModelStatus(
                                    queryServiceModelResponse, modifiedStatus, RETRY_COUNT_ZERO);
                    client.save(updateRetryCountZeroAndStatus);
                    OperationalEnvDistributionStatus updateDistStatus =
                            dbHelper.updateStatusInOperationalEnvDistributionStatus(queryDistributionDbResponse,
                                    modifiedStatus, errorReason);
                    client.save(updateDistStatus);
                } else {
                    // RETRY & Count = 0 (do nothing!)
                }
            }
        }
    }

    /**
     * The Method to call SDC for recoveryActioin RETRY
     * 
     * @param queryDistributionDbResponse - OperationalEnvDistributionStatus object
     * @param queryServiceModelResponse - OperationalEnvServiceModelStatus object
     * @param sdcStatus - Distribution object
     * @return JSONObject object
     */
    public JSONObject callSDClientForRetry(OperationalEnvDistributionStatus queryDistributionDbResponse,
            OperationalEnvServiceModelStatus queryServiceModelResponse, Distribution sdcStatus) throws ApiException {

        JSONObject jsonResponse = null;

        String operEnvironmentId = queryDistributionDbResponse.getOperationalEnvId();
        String serviceModelVersionId = queryDistributionDbResponse.getServiceModelVersionId();
        String originalRequestId = queryServiceModelResponse.getRequestId();
        int retryCount = queryServiceModelResponse.getRetryCount();
        String workloadContext = queryServiceModelResponse.getWorkloadContext();


        jsonResponse = sdcClientHelper.postActivateOperationalEnvironment(serviceModelVersionId, operEnvironmentId,
                workloadContext);
        String statusCode = jsonResponse.get("statusCode").toString();
        if (statusCode.equals(String.valueOf(Response.Status.ACCEPTED.getStatusCode()))) {
            String newDistributionId = jsonResponse.get("distributionId").toString();
            // should insert 1 row, NEW distributionId for replacement of the serviceModelServiceId record
            OperationalEnvDistributionStatus insertNewDistributionId =
                    dbHelper.insertRecordToOperationalEnvDistributionStatus(newDistributionId, operEnvironmentId,
                            serviceModelVersionId, originalRequestId, DISTRIBUTION_STATUS_SENT, "");
            client.save(insertNewDistributionId);

            // update retryCount (less 1) for the serviceModelServiceId
            retryCount = retryCount - 1;
            // should update 1 row, original insert
            OperationalEnvServiceModelStatus updateRetryCountAndStatus =
                    dbHelper.updateRetryCountAndStatusInOperationalEnvServiceModelStatus(queryServiceModelResponse,
                            DISTRIBUTION_STATUS_SENT, retryCount);
            client.save(updateRetryCountAndStatus);

            // should update 1 row, OLD distributionId set to status error (ie, old distributionId is DONE!).
            OperationalEnvDistributionStatus updateStatus = dbHelper.updateStatusInOperationalEnvDistributionStatus(
                    queryDistributionDbResponse, DISTRIBUTION_STATUS_ERROR, sdcStatus.getErrorReason());
            client.save(updateStatus);
        } else {
            String dbErrorMessage = "Failure calling SDC: statusCode: " + statusCode + "; messageId: "
                    + jsonResponse.get("messageId") + "; message: " + jsonResponse.get("message");
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.BusinessProcessError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder(dbErrorMessage, HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
            requestDb.updateInfraFailureCompletion(dbErrorMessage, this.origRequestId, operEnvironmentId);
            throw validateException;
        }

        return jsonResponse;

    }


    /**
     * The Method to check the overall status of the Activation for an operationalEnvironmentId
     * 
     * @param operationalEnvironmentId - string
     * @param origRequestId - string
     * @return void - nothing
     */
    public void checkOrUpdateOverallStatus(String operationalEnvironmentId, String origRequestId) throws ApiException {

        List<OperationalEnvServiceModelStatus> queryServiceModelResponseList =
                client.getAllByOperationalEnvIdAndRequestId(operationalEnvironmentId, origRequestId);

        String status = "Waiting";
        int count = 0;
        // loop through the statuses of the service model
        for (OperationalEnvServiceModelStatus querySrvModelResponse : queryServiceModelResponseList) {
            status = querySrvModelResponse.getServiceModelVersionDistrStatus();
            // all should be OK to be completed.
            if ((status.equals(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString())
                    && (querySrvModelResponse.getRetryCount() == 0))) {
                status = "Completed";
                count++;
            }
            // one error with zero retry, means all are failures.
            if ((status.equals(DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString())
                    && (querySrvModelResponse.getRetryCount() == 0))) {
                status = "Failure";
                count = queryServiceModelResponseList.size();
                break;
            }
        }

        if (status.equals("Completed") && queryServiceModelResponseList.size() == count) {
            String messageStatus = "Overall Activation process is complete. " + status;
            isOverallSuccess = true;
            requestDb.updateInfraSuccessCompletion(messageStatus, origRequestId, operationalEnvironmentId);
        } else {
            if (status.equals("Failure") && queryServiceModelResponseList.size() == count) {
                this.errorMessage = "Overall Activation process is a Failure. " + status;
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.BusinessProcessError)
                                .build();
                ValidateException validateException =
                        new ValidateException.Builder(this.errorMessage, HttpStatus.SC_BAD_REQUEST,
                                ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
                requestDb.updateInfraFailureCompletion(this.errorMessage, origRequestId, operationalEnvironmentId);
                throw validateException;
            }

        }
    }

    /**
     * Get OperationalEnvironment object
     * 
     * @param operationalEnvironmentId - String
     * @return operationalEnv - OperationalEnvironment object
     */
    private OperationalEnvironment getAAIOperationalEnvironment(String operationalEnvironmentId) {
        AAIResultWrapper aaiResult = aaiHelper.getAaiOperationalEnvironment(operationalEnvironmentId);
        Optional<OperationalEnvironment> operationalEnvironmentOpt = aaiResult.asBean(OperationalEnvironment.class);
        return operationalEnvironmentOpt.isPresent() ? operationalEnvironmentOpt.get() : null;
    }
}
