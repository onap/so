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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;

@RunWith(MockitoJUnitRunner.class)
public class VnfAaiCheckerTest {

    private static final String VNF_ID = "t5h78w";

    @Mock
    private AaiConnection aaiConnection;

    @InjectMocks
    private VnfAaiChecker checker;

    @Test
    public void testAll() {
        // when
        final boolean response = checker.vnfCheck(NotificationVnfFilterType.ALL, VNF_ID);

        // then
        assertTrue(response);
    }

    @Test
    public void testAaiCheckedPresent() {
        // given
        when(aaiConnection.checkGenericVnfId(eq(VNF_ID))).thenReturn(true);

        // when
        final boolean response = checker.vnfCheck(NotificationVnfFilterType.AAI_CHECKED, VNF_ID);

        // then
        assertTrue(response);
    }

    @Test
    public void testAaiCheckedAbsent() {
        // given
        when(aaiConnection.checkGenericVnfId(eq(VNF_ID))).thenReturn(false);

        // when
        final boolean response = checker.vnfCheck(NotificationVnfFilterType.AAI_CHECKED, VNF_ID);

        // then
        assertFalse(response);
    }

    @Test
    public void testNone() {
        // when
        final boolean response = checker.vnfCheck(NotificationVnfFilterType.NONE, VNF_ID);

        // then
        assertFalse(response);
    }
}
