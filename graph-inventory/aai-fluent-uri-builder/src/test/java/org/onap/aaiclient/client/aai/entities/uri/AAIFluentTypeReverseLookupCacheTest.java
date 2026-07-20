/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.aai.entities.uri;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.aai.entities.uri.AAIFluentTypeReverseLookup.ResolvedType;

public class AAIFluentTypeReverseLookupCacheTest {

    @Test
    public void resolveTypeIsCachedPerName() {
        Optional<ResolvedType> first = AAIFluentTypeReverseLookup.resolveType("cloud-region");
        Optional<ResolvedType> second = AAIFluentTypeReverseLookup.resolveType("cloud-region");

        assertTrue(first.isPresent(), "cloud-region resolves");
        assertSame(first.get(), second.get(), "resolution reused for the same name (no repeated reflection)");
    }

    @Test
    public void unknownNameCachesNegativeResult() {
        Optional<ResolvedType> first = AAIFluentTypeReverseLookup.resolveType("not-a-real-type-xyz");
        Optional<ResolvedType> second = AAIFluentTypeReverseLookup.resolveType("not-a-real-type-xyz");

        assertFalse(first.isPresent(), "unknown name does not resolve");
        assertSame(first, second, "negative result cached (no repeated ClassNotFoundException)");
    }
}
