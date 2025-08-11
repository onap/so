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

package org.onap.so.asdc.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.client.test.emulators.JsonStatusData;
import org.springframework.beans.factory.annotation.Autowired;

public class ASDCStatusCallBackTest extends BaseTest {
    @Autowired
    private ASDCStatusCallBack statusCallback;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void activateCallbackTest() throws Exception {
        JsonStatusData statusData = new JsonStatusData();

        doNothing().when(toscaInstaller).installTheComponentStatus(isA(JsonStatusData.class));

        statusCallback.activateCallback(statusData);

        verify(toscaInstaller, times(1)).installTheComponentStatus(statusData);
    }

    @Test
    public void activateCallbackDoneErrorStatusTest() throws Exception {
        IStatusData statusData = mock(IStatusData.class);

        doReturn("distributionId").when(statusData).getDistributionID();
        doReturn("componentName").when(statusData).getComponentName();
        doReturn(DistributionStatusEnum.COMPONENT_DONE_ERROR).when(statusData).getStatus();
        doNothing().when(toscaInstaller).installTheComponentStatus(isA(IStatusData.class));

        statusCallback.activateCallback(statusData);

        verify(toscaInstaller, times(1)).installTheComponentStatus(statusData);
    }

    @Test
    public void activateCallbackExceptionTest() throws Exception {
        IStatusData statusData = mock(IStatusData.class);

        doReturn("distributionId").when(statusData).getDistributionID();
        doReturn("componentName").when(statusData).getComponentName();
        doReturn(DistributionStatusEnum.COMPONENT_DONE_OK).when(statusData).getStatus();
        doThrow(ArtifactInstallerException.class).when(toscaInstaller)
                .installTheComponentStatus(isA(IStatusData.class));

        assertDoesNotThrow(() -> statusCallback.activateCallback(statusData));
    }
}
