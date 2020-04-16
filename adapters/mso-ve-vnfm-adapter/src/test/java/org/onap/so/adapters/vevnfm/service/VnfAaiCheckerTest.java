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
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;
import org.onap.so.adapters.vevnfm.util.StringUsage;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VnfAaiCheckerTest {

    private static final String VNF_ID = "t5h78w";
    private static final String GEN_ID = "ZZ8473UV";

    @Mock
    private AaiConnection aaiConnection;

    @InjectMocks
    private VnfAaiChecker checker;

    @Test
    public void testAllNull() {
        // given
        when(aaiConnection.checkGenericVnfId(eq(VNF_ID))).thenReturn(StringUsage.empty());

        // when
        final StringUsage response = checker.vnfCheck(NotificationVnfFilterType.ALL, VNF_ID);

        // then
        assertTrue(response.isPresent());
        assertNull(response.get());
    }

    @Test
    public void testAllNotNull() {
        // given
        when(aaiConnection.checkGenericVnfId(eq(VNF_ID))).thenReturn(StringUsage.of(GEN_ID));

        // when
        final StringUsage response = checker.vnfCheck(NotificationVnfFilterType.ALL, VNF_ID);

        // then
        assertTrue(response.isPresent());
        assertEquals(GEN_ID, response.get());
    }

    @Test
    public void testAaiCheckedNull() {
        // given
        when(aaiConnection.checkGenericVnfId(eq(VNF_ID))).thenReturn(StringUsage.empty());

        // when
        final StringUsage response = checker.vnfCheck(NotificationVnfFilterType.AAI_CHECKED, VNF_ID);

        // then
        assertFalse(response.isPresent());
        assertNull(response.get());
    }

    @Test
    public void testAaiCheckedNotNull() {
        // given
        when(aaiConnection.checkGenericVnfId(eq(VNF_ID))).thenReturn(StringUsage.of(GEN_ID));

        // when
        final StringUsage response = checker.vnfCheck(NotificationVnfFilterType.AAI_CHECKED, VNF_ID);

        // then
        assertTrue(response.isPresent());
        assertEquals(GEN_ID, response.get());
    }

    @Test
    public void testNone() {
        // when
        final StringUsage response = checker.vnfCheck(NotificationVnfFilterType.NONE, VNF_ID);

        // then
        assertFalse(response.isPresent());
        assertNull(response.get());
    }
}
