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

package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.domain.yang.Pserver;

public class AAIPServerTest {

    @Test
    public void pserverTest() throws IOException {
        AAIRestClientImpl client = new AAIRestClientImpl();
        String json = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aai/pserver.json")));
        List<Pserver> list = client.getListOfPservers(json);

        assertEquals("", list.get(0).getHostname(), "test");
        assertEquals("", list.size(), 2);
    }

    @Test
    @Ignore // IGNORED FOR 1710 MERGE TO ONAP
    public void pserverActualTest() throws IOException {
        AAIRestClientImpl client = new AAIRestClientImpl();
        List<Pserver> list = client.getPhysicalServerByVnfId("d946afed-8ebe-4c5d-9665-54fcc043b8e7");
        assertEquals("", list.size(), 0);
    }

}
