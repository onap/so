/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;

public class TracingTest extends CatalogDbAdapterBaseTest {

    @Autowired
    ObservationRegistry observationRegistry;

    @Autowired
    Tracer tracer;

    @Test
    public void thatTracingBeansAreAvailable() {
        assertNotNull("ObservationRegistry bean should be present", observationRegistry);
        assertNotNull("Tracer bean should be present", tracer);
    }

}
