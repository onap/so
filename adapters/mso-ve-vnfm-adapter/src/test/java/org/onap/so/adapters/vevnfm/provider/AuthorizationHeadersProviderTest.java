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

package org.onap.so.adapters.vevnfm.provider;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AuthorizationHeadersProviderTest {

    private final AuthorizationHeadersProvider provider = new AuthorizationHeadersProvider();

    @Test
    public void testSuccess() {
        final int size = provider.getHttpHeaders().size();

        provider.addAuthorization("authorization");
        assertEquals(size + 1, provider.getHttpHeaders().size());

        provider.removeAuthorization();
        assertEquals(size, provider.getHttpHeaders().size());
    }
}
