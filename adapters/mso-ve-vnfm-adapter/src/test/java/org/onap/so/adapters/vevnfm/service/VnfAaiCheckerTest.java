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
import org.onap.so.adapters.vevnfm.constant.VnfNotificationFilterType;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VnfAaiCheckerTest {

    private static final String VNF_ID = "t5h78w";

    private final VnfAaiChecker checker = new VnfAaiChecker();

    @Test
    public void testAll() {
        // when
        final boolean response = checker.vnfCheck(VnfNotificationFilterType.ALL, VNF_ID);

        // then
        assertTrue(response);
    }

    @Test
    public void testNone() {
        // when
        final boolean response = checker.vnfCheck(VnfNotificationFilterType.NONE, VNF_ID);

        // then
        assertFalse(response);
    }
}
