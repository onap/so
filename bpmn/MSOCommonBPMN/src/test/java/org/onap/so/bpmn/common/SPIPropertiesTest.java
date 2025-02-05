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

package org.onap.so.bpmn.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.so.client.dmaap.DmaapProperties;
import org.onap.so.client.dmaap.DmaapPropertiesLoader;


public class SPIPropertiesTest {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("mso.config.path", "src/test/resources");
    }

    @Test
    public void notEqual() {
        DmaapProperties one = DmaapPropertiesLoader.getInstance().getNewImpl();
        DmaapProperties two = DmaapPropertiesLoader.getInstance().getNewImpl();
        assertNotEquals(one, two);
    }

    @Test
    public void equal() {
        DmaapProperties one = DmaapPropertiesLoader.getInstance().getImpl();
        DmaapProperties two = DmaapPropertiesLoader.getInstance().getImpl();
        assertEquals(one, two);
    }

    @Test
    public void restNotEqual() {
        AAIProperties one = RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
        AAIProperties two = RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
        assertNotEquals(one, two);
    }

    @Test
    public void restEqual() {
        AAIProperties one = RestPropertiesLoader.getInstance().getImpl(AAIProperties.class);
        AAIProperties two = RestPropertiesLoader.getInstance().getImpl(AAIProperties.class);
        assertEquals(one, two);
    }

}
