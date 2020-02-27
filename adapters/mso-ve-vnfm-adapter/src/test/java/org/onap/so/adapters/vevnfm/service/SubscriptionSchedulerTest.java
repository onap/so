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

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.EsrSystemInfo;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionSchedulerTest {

    private static final String URL = "url";
    private static final String ID = "id044";

    @Mock
    private SubscriberService subscriberService;

    @InjectMocks
    private SubscriptionScheduler subscriptionScheduler;

    @Test
    public void testFullScenario() throws Exception {
        // given
        final EsrSystemInfo info = new EsrSystemInfo();
        info.setServiceUrl(URL);
        final List<EsrSystemInfo> infos = Collections.singletonList(info);

        when(subscriberService.subscribe(eq(info))).thenReturn(ID);
        when(subscriberService.checkSubscription(eq(info), eq(ID))).thenReturn(false);

        // when
        subscriptionScheduler.setInfos(infos);
        subscriptionScheduler.subscribeTask();
        subscriptionScheduler.checkSubscribeTask();

        // then
        verify(subscriberService).subscribe(info);
        verify(subscriberService).checkSubscription(info, ID);

        assertNull(subscriptionScheduler.getEsrIds().get(0).getId());
    }
}
