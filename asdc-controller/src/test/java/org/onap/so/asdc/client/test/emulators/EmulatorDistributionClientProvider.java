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

import java.util.concurrent.atomic.AtomicReference;
import org.onap.sdc.api.IDistributionClient;
import org.onap.so.asdc.client.DistributionClientProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("test")
public class EmulatorDistributionClientProvider extends DistributionClientProvider {

    private final AtomicReference<IDistributionClient> next = new AtomicReference<>();

    public void useNext(final IDistributionClient client) {
        next.set(client);
    }

    @Override
    public IDistributionClient create() {
        final IDistributionClient queued = next.getAndSet(null);
        return queued != null ? queued : super.create();
    }
}
