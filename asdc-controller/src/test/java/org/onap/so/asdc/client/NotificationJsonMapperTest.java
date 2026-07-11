/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;

public class NotificationJsonMapperTest {

    private final NotificationJsonMapper mapper = new NotificationJsonMapper();

    @Test
    public void toJson_validNotification_returnsPresentJson() {
        NotificationDataImpl notif = new NotificationDataImpl();
        notif.setDistributionID("dist-1");
        notif.setServiceUUID("svc-1");
        notif.setResources(Collections.emptyList());
        Optional<String> json = mapper.toJson(notif);
        assertTrue(json.isPresent());
        assertTrue(json.get().contains("dist-1"));
    }
}
