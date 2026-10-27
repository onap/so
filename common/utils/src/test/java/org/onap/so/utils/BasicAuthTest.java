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

package org.onap.so.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class BasicAuthTest {

    @Test
    public void thatHeaderEncodesUsernameAndPassword() {
        assertEquals("Basic YnBlbDpwYXNzd29yZDEk", BasicAuth.header("bpel", "password1$"));
    }

    @Test
    public void thatPasswordMayContainColons() {
        assertEquals("Basic dXNlcjpwYTpzcw==", BasicAuth.header("user", "pa:ss"));
    }

    @Test
    public void thatNoHeaderIsCreatedWithoutUsername() {
        assertNull(BasicAuth.header(null, "password"));
        assertNull(BasicAuth.header(" ", "password"));
    }
}
