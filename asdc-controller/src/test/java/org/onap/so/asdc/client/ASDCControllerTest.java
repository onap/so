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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IConfiguration;
import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.consumer.IStatusCallback;
import org.onap.sdc.api.results.DistributionActionResultEnum;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.EmulatorDistributionClientProvider;
import org.springframework.beans.factory.annotation.Autowired;

public class ASDCControllerTest extends BaseTest {
    @Autowired
    private ASDCController asdcController;
    @Autowired
    private EmulatorDistributionClientProvider clientProvider;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void initASDCExceptionTest() throws Exception {
        expectedException.expect(ASDCControllerException.class);

        asdcController.changeControllerStatus(ASDCControllerStatus.IDLE);

        try {
            asdcController.initASDC();
        } finally {
            asdcController.closeASDC();
        }
    }

    @Test
    public void initASDCUsesTheClientFromTheProvider() throws Exception {
        DistributionClientEmulator client = new DistributionClientEmulator();
        clientProvider.useNext(client);
        try {
            asdcController.initASDC();
            assertSame(client, asdcController.getDistributionClient());
        } finally {
            asdcController.closeASDC();
        }
    }

    @Test
    public void closeASDCReleasesTheClient() throws Exception {
        clientProvider.useNext(new DistributionClientEmulator());
        asdcController.initASDC();
        asdcController.closeASDC();
        assertNull(asdcController.getDistributionClient());
    }

    @Test
    public void failedInitReusesTheClientOnTheNextAttempt() throws Exception {
        IDistributionClient client = mock(IDistributionClient.class);
        IDistributionClientResult failure =
                new DistributionClientResultImpl(DistributionActionResultEnum.FAIL, "SDC unavailable");
        IDistributionClientResult success =
                new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS, "SUCCESS");
        when(client.init(any(IConfiguration.class), any(INotificationCallback.class), any(IStatusCallback.class)))
                .thenReturn(failure, success);
        when(client.start()).thenReturn(success);
        when(client.stop()).thenReturn(success);
        clientProvider.useNext(client);

        assertThrows(ASDCControllerException.class, () -> asdcController.initASDC());
        try {
            asdcController.initASDC();
            assertSame(client, asdcController.getDistributionClient());
        } finally {
            asdcController.closeASDC();
        }
    }
}
