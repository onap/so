/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.service;

import static org.mockito.Mockito.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;

@RunWith(MockitoJUnitRunner.class)
public class StartupServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private AaiConnection aaiConnection;

    @Mock
    private SubscriberService subscriberService;

    @InjectMocks
    private StartupService startupService;

    @Test
    public void testSuccess() throws Exception {
        // given
        final EsrSystemInfo info = new EsrSystemInfo();
        info.setServiceUrl("lh");
        when(aaiConnection.receiveVnfm()).thenReturn(info);
        when(subscriberService.subscribe(info)).thenReturn(true);

        // when
        final EsrSystemInfo systemInfo = startupService.receiveVnfm();
        startupService.subscribe(systemInfo);

        // then
        verify(aaiConnection, times(1)).receiveVnfm();
        verify(subscriberService, times(1)).subscribe(info);
    }

    @Test
    public void testFailureAai() throws Exception {
        // given
        final EsrSystemInfo info = new EsrSystemInfo();
        when(aaiConnection.receiveVnfm()).thenReturn(info);

        thrown.expect(VeVnfmException.class);

        // when
        final EsrSystemInfo systemInfo = startupService.receiveVnfm();
        startupService.subscribe(systemInfo);
    }

    @Test
    public void testFailureSubscriber() throws Exception {
        // given
        final EsrSystemInfo info = new EsrSystemInfo();
        info.setServiceUrl("lh");
        when(aaiConnection.receiveVnfm()).thenReturn(info);
        when(subscriberService.subscribe(info)).thenReturn(false);

        thrown.expect(VeVnfmException.class);

        // when
        final EsrSystemInfo systemInfo = startupService.receiveVnfm();
        startupService.subscribe(systemInfo);
    }
}
