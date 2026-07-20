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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.regex.Pattern;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.aai.AAIObjectType;

public class AAIPrefixPatternTest {

    private AAIResourceUri newUri() {
        return new AAISimpleUri(AAIObjectType.UNKNOWN,
                UriBuilder.fromUri("/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053").build());
    }

    @Test
    public void prefixPatternStillMatchesAaiVersionPrefix() {
        Pattern pattern = newUri().getPrefixPattern();
        assertTrue(pattern.matcher("/aai/v9").matches(), "matches /aai/vN");
        assertTrue(pattern.matcher("/aai/v38").matches(), "matches multi-digit version");
    }

    @Test
    public void prefixPatternIsCompiledOnceAndReused() {
        AAIResourceUri uri = newUri();

        // Same instance must not recompile the constant regex on each call
        assertSame(uri.getPrefixPattern(), uri.getPrefixPattern(), "same instance reuses compiled Pattern");

        // Every AAI URI shares the one compiled prefix Pattern
        assertSame(newUri().getPrefixPattern(), newUri().getPrefixPattern(),
                "prefix Pattern is shared across AAI URIs");
    }
}
