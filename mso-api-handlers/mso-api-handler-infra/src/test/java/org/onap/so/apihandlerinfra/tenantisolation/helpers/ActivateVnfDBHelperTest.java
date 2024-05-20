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

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.apihandlerinfra.ApiHandlerApplication;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.DistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;


public class ActivateVnfDBHelperTest extends BaseTest {

    String operationalEnvironmentId = "TEST_operationalEnvironmentId";
    String vnfOperationalEnvironmentId = "VNF_operationalEnvironmentId";
    String requestId = "TEST_requestId";
    String origRequestId = "TEST_requestId";

    String workloadContext1 = "TEST_workloadContext1";
    String serviceModelVersionId1 = "TEST_serviceModelVersionId1";
    String distributionId1 = "TEST_distributionId1";
    String errorReason = "ABORTED";
    int retryCountThree = 3;
    int retryCountZero = 0;
    String recoveryActionRetry = "RETRY";
    String statusOk = DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString();
    String statusError = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
    String statusSent = "SENT";

    OperationalEnvDistributionStatus expectedDistStatus;
    OperationalEnvServiceModelStatus expectedServiceModelStatus;

    @Autowired
    private ActivateVnfDBHelper dbHelper;

    @Before
    public void testSetUp() {
        // prepare expected OperationalEnvDistributionStatus object
        expectedDistStatus = new OperationalEnvDistributionStatus();
        expectedDistStatus.setDistributionId(distributionId1);
        expectedDistStatus.setOperationalEnvId(operationalEnvironmentId);
        expectedDistStatus.setServiceModelVersionId(serviceModelVersionId1);
        expectedDistStatus.setDistributionIdStatus(statusOk);
        expectedDistStatus.setRequestId(requestId);
        expectedDistStatus.setDistributionIdErrorReason("");

        // prepare expected OperationalEnvServiceModelStatus object
        expectedServiceModelStatus = new OperationalEnvServiceModelStatus();
        expectedServiceModelStatus.setRequestId(requestId);
        expectedServiceModelStatus.setOperationalEnvId(operationalEnvironmentId);
        expectedServiceModelStatus.setServiceModelVersionId(serviceModelVersionId1);
        expectedServiceModelStatus.setServiceModelVersionDistrStatus(statusOk);
        expectedServiceModelStatus.setRecoveryAction(recoveryActionRetry);
        expectedServiceModelStatus.setRetryCount(new Integer(retryCountThree));
        expectedServiceModelStatus.setWorkloadContext(workloadContext1);
        expectedServiceModelStatus.setVnfOperationalEnvId(vnfOperationalEnvironmentId);
    }

    @Test
    public void testOperationalEnvDistributionStatusDbMethods() throws Exception {

        // test insert method
        OperationalEnvDistributionStatus distStatus1 = dbHelper.insertRecordToOperationalEnvDistributionStatus(
                distributionId1, operationalEnvironmentId, serviceModelVersionId1, requestId, statusOk, "");
        assertThat(distStatus1, sameBeanAs(expectedDistStatus));

        // prepare updated expected object
        OperationalEnvDistributionStatus expectedUpdatedDistStatus = expectedDistStatus;
        expectedUpdatedDistStatus.setDistributionIdStatus(statusError);
        expectedUpdatedDistStatus.setDistributionIdErrorReason(errorReason);

        // test update method - statusOk to statusError
        OperationalEnvDistributionStatus distStatus2 =
                dbHelper.updateStatusInOperationalEnvDistributionStatus(distStatus1, statusError, errorReason);
        assertThat(distStatus2, sameBeanAs(expectedUpdatedDistStatus));

    }

    @Test
    public void testOperationalEnvServiceModelStatusDbMethods() throws Exception {

        // test insert method
        OperationalEnvServiceModelStatus serviceModelStatus1 = dbHelper.insertRecordToOperationalEnvServiceModelStatus(
                requestId, operationalEnvironmentId, serviceModelVersionId1, statusOk, recoveryActionRetry,
                retryCountThree, workloadContext1, vnfOperationalEnvironmentId);
        assertThat(serviceModelStatus1, sameBeanAs(expectedServiceModelStatus));

        // prepare updated expected object
        OperationalEnvServiceModelStatus expectedUpdatedServiceModelStatus = serviceModelStatus1;
        expectedUpdatedServiceModelStatus.setServiceModelVersionDistrStatus(statusError);
        Integer integer = Integer.valueOf(retryCountZero);
        expectedUpdatedServiceModelStatus.setRetryCount(integer);
        // expectedUpdatedServiceModelStatus.setRetryCount(new Integer(retryCountZero));

        // test update method - update statusOk to statusError & retryCountThree to retryCountZero
        OperationalEnvServiceModelStatus serviceModelStatus2 =
                dbHelper.updateRetryCountAndStatusInOperationalEnvServiceModelStatus(serviceModelStatus1, statusError,
                        retryCountZero);
        assertThat(serviceModelStatus2, sameBeanAs(expectedUpdatedServiceModelStatus));

    }

}
