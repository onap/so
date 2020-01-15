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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StartupServiceTest {

    @Mock
    private AaiConnectionService aaiService;

    @Mock
    private SubscriberService subscriberService;

    @InjectMocks
    private StartupService startupService;

    @Test
    public void testSuccess() throws Exception {
        // given
        final Map vnfm = new LinkedHashMap();
        vnfm.put(startupService.URL, "lh");

        when(aaiService.receiveVnfm()).thenReturn(vnfm);
        when(subscriberService.subscribe(vnfm)).thenReturn(true);

        // when
        startupService.run();

        // then
        verify(aaiService, times(1)).receiveVnfm();
        verify(subscriberService, times(1)).subscribe(vnfm);
    }

    @Test(expected = VeVnfmException.class)
    public void testFailureAai() throws Exception {
        // given
        final Map vnfm = new LinkedHashMap();

        when(aaiService.receiveVnfm()).thenReturn(vnfm);

        // when
        startupService.run();
    }

    @Test(expected = VeVnfmException.class)
    public void testFailureSubscriber() throws Exception {
        // given
        final Map vnfm = new LinkedHashMap();
        vnfm.put(startupService.URL, "lh");

        when(aaiService.receiveVnfm()).thenReturn(vnfm);
        when(subscriberService.subscribe(vnfm)).thenReturn(false);

        // when
        startupService.run();
    }
}
