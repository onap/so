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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinks;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;

@RunWith(MockitoJUnitRunner.class)
public class DmaapConditionalSenderTest {

    private static final String GENERIC_ID = "gener77";
    private static final String INSTANCE_ID = "insta44";
    private static final String HREF = "/href";

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private AaiConnection aaiConnection;

    @Mock
    private DmaapService dmaapService;

    private static VnfLcmOperationOccurrenceNotification createNotification() {
        final VnfLcmOperationOccurrenceNotification notification = new VnfLcmOperationOccurrenceNotification();
        final LcnVnfLcmOperationOccurrenceNotificationLinks links = new LcnVnfLcmOperationOccurrenceNotificationLinks();
        final LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance vnfInstance =
                new LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance();

        notification.setVnfInstanceId(INSTANCE_ID);
        notification.setLinks(links);
        links.setVnfInstance(vnfInstance);
        vnfInstance.setHref(HREF);

        return notification;
    }

    @Test
    public void testSendNone() {
        // given
        when(configProperties.getNotificationVnfFilterType()).thenReturn(NotificationVnfFilterType.NONE);

        final DmaapConditionalSender sender = new DmaapConditionalSender(configProperties, aaiConnection, dmaapService);
        final VnfLcmOperationOccurrenceNotification notification = createNotification();

        // when
        sender.send(notification);

        // then
        verify(aaiConnection, never()).receiveGenericVnfId(any());
        verify(dmaapService, never()).createDmaapEvent(any(), any(), any());
        verify(dmaapService, never()).send(any());
    }

    @Test
    public void testSendAll() {
        // given
        when(configProperties.getNotificationVnfFilterType()).thenReturn(NotificationVnfFilterType.ALL);
        when(aaiConnection.receiveGenericVnfId(eq(HREF))).thenReturn(GENERIC_ID);

        final DmaapConditionalSender sender = new DmaapConditionalSender(configProperties, aaiConnection, dmaapService);
        final VnfLcmOperationOccurrenceNotification notification = createNotification();

        // when
        sender.send(notification);

        // then
        verify(aaiConnection).receiveGenericVnfId(eq(HREF));
        verify(dmaapService).createDmaapEvent(eq(notification), any(), eq(GENERIC_ID));
        verify(dmaapService).send(any());
    }

    @Test
    public void testSendAaiCheckedPresent() {
        // given
        when(configProperties.getNotificationVnfFilterType()).thenReturn(NotificationVnfFilterType.AAI_CHECKED);
        when(aaiConnection.receiveGenericVnfId(eq(HREF))).thenReturn(GENERIC_ID);

        final DmaapConditionalSender sender = new DmaapConditionalSender(configProperties, aaiConnection, dmaapService);
        final VnfLcmOperationOccurrenceNotification notification = createNotification();

        // when
        sender.send(notification);

        // then
        verify(aaiConnection).receiveGenericVnfId(eq(HREF));
        verify(dmaapService).createDmaapEvent(eq(notification), any(), eq(GENERIC_ID));
        verify(dmaapService).send(any());
    }

    @Test
    public void testSendAaiCheckedAbsent() {
        // given
        when(configProperties.getNotificationVnfFilterType()).thenReturn(NotificationVnfFilterType.AAI_CHECKED);
        when(aaiConnection.receiveGenericVnfId(eq(HREF))).thenReturn(null);

        final DmaapConditionalSender sender = new DmaapConditionalSender(configProperties, aaiConnection, dmaapService);
        final VnfLcmOperationOccurrenceNotification notification = createNotification();

        // when
        sender.send(notification);

        // then
        verify(aaiConnection).receiveGenericVnfId(eq(HREF));
        verify(dmaapService, never()).createDmaapEvent(any(), any(), any());
        verify(dmaapService, never()).send(any());
    }
}
