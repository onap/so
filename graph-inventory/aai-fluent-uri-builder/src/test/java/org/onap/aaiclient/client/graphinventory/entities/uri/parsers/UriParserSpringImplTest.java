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

package org.onap.aaiclient.client.graphinventory.entities.uri.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UriParserSpringImplTest {

    private static final String TEMPLATE = "/network/generic-vnfs/generic-vnf/{vnf-id}";

    @Test
    public void forTemplateReusesInstanceForSameTemplate() {
        UriParser first = UriParserSpringImpl.forTemplate(TEMPLATE);
        UriParser second = UriParserSpringImpl.forTemplate(TEMPLATE);

        assertSame(first, second, "same template string returns the cached parser");
    }

    @Test
    public void forTemplateReturnsDistinctInstancesForDistinctTemplates() {
        UriParser generic = UriParserSpringImpl.forTemplate(TEMPLATE);
        UriParser pserver = UriParserSpringImpl.forTemplate("/cloud-infrastructure/pservers/pserver/{hostname}");

        assertNotSame(generic, pserver, "different templates are cached separately");
    }

    @Test
    public void cachedParserStillParsesAndExposesVariables() {
        UriParser parser = UriParserSpringImpl.forTemplate(TEMPLATE);

        assertEquals("[vnf-id]", parser.getVariables().toString(), "variables preserved");

        Map<String, String> keys = parser.parse("/network/generic-vnfs/generic-vnf/myVnf");
        assertEquals("myVnf", keys.get("vnf-id"), "parsing preserved");
    }
}
