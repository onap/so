/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.tenantIsolation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.so.asdc.BaseTest;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.db.catalog.beans.Service;
import org.springframework.beans.factory.annotation.Autowired;

public class WatchdogDistributionTest extends BaseTest {
    @Autowired
    private WatchdogDistribution watchdogDistribution;

    @Mock
    private AAIResourcesClient aaiResourceClient;

    private static final String SUCCESS_TEST = "watchdogTestStatusSuccess";
    private static final String FAILURE_TEST = "watchdogTestStatusFailure";
    private static final String TIMEOUT_TEST = "watchdogTestStatusTimeout";
    private static final String INCOMPLETE_TEST = "watchdogTestStatusIncomplete";
    private static final String EXCEPTION_TEST = "watchdogTestStatusException";
    private static final String NULL_TEST = "watchdogTestStatusNull";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getOverallDistributionStatusTimeoutTest() throws Exception {
        String status = watchdogDistribution.getOverallDistributionStatus(TIMEOUT_TEST);
        assertEquals(DistributionStatus.TIMEOUT.toString(), status);
    }

    @Test
    public void getOverallDistributionStatusSuccessTest() throws Exception {
        String status = watchdogDistribution.getOverallDistributionStatus(SUCCESS_TEST);
        assertEquals(DistributionStatus.SUCCESS.toString(), status);
    }

    @Test
    public void getOverallDistributionStatusFailureTest() throws Exception {
        String status = watchdogDistribution.getOverallDistributionStatus(FAILURE_TEST);
        assertEquals(DistributionStatus.FAILURE.toString(), status);
    }

    @Test
    public void getOverallDistributionStatusIncompleteTest() throws Exception {
        String status = watchdogDistribution.getOverallDistributionStatus(INCOMPLETE_TEST);
        assertEquals(DistributionStatus.INCOMPLETE.toString(), status);
    }

    @Test
    public void getOverallDistributionStatusInvalidComponentExceptionTest() throws Exception {
        expectedException.expect(Exception.class);
        watchdogDistribution.getOverallDistributionStatus(EXCEPTION_TEST);
    }

    @Test
    public void getOverallDistributionStatusNewStatusTest() throws Exception {
        String status = watchdogDistribution.getOverallDistributionStatus("newDistrubutionStatus");
        assertEquals(DistributionStatus.INCOMPLETE.toString(), status);
    }

    @Test
    public void getOverallDistributionStatusExceptionTest() throws Exception {
        expectedException.expect(Exception.class);
        watchdogDistribution.getOverallDistributionStatus(null);
    }

    @Test
    public void executePatchAAITest() throws Exception {
        Service service = new Service();
        service.setModelInvariantUUID("9647dfc4-2083-11e7-93ae-92361f002671");

        doReturn(aaiResourceClient).when(watchdogDistributionSpy).getAaiClient();
        doNothing().when(aaiResourceClient).update(isA(AAIResourceUri.class), isA(HashMap.class));

        watchdogDistribution.executePatchAAI(SUCCESS_TEST, service.getModelInvariantUUID(),
                DistributionStatus.SUCCESS.toString());

        verify(aaiResourceClient, times(1)).update(any(AAIResourceUri.class), any(HashMap.class));
    }

    @Test
    public void executePatchAAINullDistrubutionIdTest() throws Exception {
        expectedException.expect(Exception.class);
        watchdogDistribution.executePatchAAI(null, "", DistributionStatus.SUCCESS.toString());
    }

    @Test
    public void executePatchAAINullServiceTest() throws Exception {
        expectedException.expect(Exception.class);
        watchdogDistribution.executePatchAAI(NULL_TEST, null, DistributionStatus.SUCCESS.toString());
    }

    @Test
    public void getSetAaiClientTest() {
        aaiResourceClient = watchdogDistribution.getAaiClient();
        watchdogDistribution.setAaiClient(aaiResourceClient);
        AAIResourcesClient aaiResourceClient2 = watchdogDistribution.getAaiClient();
        assertEquals(aaiResourceClient, aaiResourceClient2);
    }
}
