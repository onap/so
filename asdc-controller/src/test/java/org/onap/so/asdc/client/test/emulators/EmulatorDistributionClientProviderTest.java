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

package org.onap.so.asdc.client.test.emulators;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import org.onap.sdc.api.IDistributionClient;

public class EmulatorDistributionClientProviderTest {

    private final EmulatorDistributionClientProvider provider = new EmulatorDistributionClientProvider();

    @Test
    public void queuedClientIsHandedOutOnce() {
        DistributionClientEmulator emulator = new DistributionClientEmulator();
        provider.useNext(emulator);

        assertSame(emulator, provider.create());
        IDistributionClient next = provider.create();
        assertNotNull(next);
        assertNotSame(emulator, next);
    }
}
