/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.dmaapproperties;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class GlobalDmaapPublisherTest extends BaseTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("mso.global.dmaap.host", "http://test:1234");
    }

    @Autowired
    private GlobalDmaapPublisher globalDmaapPublisher;

    @Test
    public void testGetters() {
        assertEquals(
                "81B7E3533B91A6706830611FB9A8ECE529BBCCE754B1F1520FA7C8698B42F97235BEFA993A387E664D6352C63A6185D68DA7F0B1D360637CBA102CB166E3E62C11EB1F75386D3506BCECE51E54",
                globalDmaapPublisher.getAuth());
        assertEquals("07a7159d3bf51a0e53be7a8f89699be7", globalDmaapPublisher.getKey());
        assertEquals("com.att.mso.asyncStatusUpdate", globalDmaapPublisher.getTopic());
        assertEquals("http://test:1234", globalDmaapPublisher.getHost().get());
    }
}
