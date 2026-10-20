/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom. All rights reserved.
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

package org.onap.so.client;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import io.micrometer.observation.ObservationRegistry;

/**
 * Hands the application's {@link ObservationRegistry} to the HTTP clients that are not Spring beans - every
 * {@link RestClient} and the clients the Groovy scripts instantiate - so that their requests are traced and carry the
 * trace context to the called service.
 */
@Component
public class ClientObservationRegistry {

    private static volatile ObservationRegistry registry = ObservationRegistry.NOOP;

    ClientObservationRegistry(ObjectProvider<ObservationRegistry> observationRegistry) {
        setRegistry(observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP));
    }

    public static ObservationRegistry get() {
        return registry;
    }

    static void setRegistry(ObservationRegistry observationRegistry) {
        registry = observationRegistry;
    }
}
