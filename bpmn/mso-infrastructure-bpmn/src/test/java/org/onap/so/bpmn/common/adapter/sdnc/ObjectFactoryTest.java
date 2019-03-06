/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 IBM.
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
package org.onap.so.bpmn.common.adapter.sdnc;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ObjectFactoryTest {

    ObjectFactory objectFactory;

    @Before
    public void setUp() {
        objectFactory = new ObjectFactory();
    }

    @Test
    public void testCreateRequestHeader() {
        assertEquals(RequestHeader.class.getName(), objectFactory.createRequestHeader().getClass().getName());
    }

    @Test
    public void testCreateSDNCAdapterResponse() {
        assertEquals(SDNCAdapterResponse.class.getName(),
                objectFactory.createSDNCAdapterResponse().getClass().getName());
    }

    @Test
    public void testSDNCAdapterCallbackRequest() {
        assertEquals(SDNCAdapterCallbackRequest.class.getName(),
                objectFactory.createSDNCAdapterCallbackRequest().getClass().getName());
    }

    @Test
    public void testCreateCallbackHeader() {
        assertEquals(CallbackHeader.class.getName(), objectFactory.createCallbackHeader().getClass().getName());
    }

    @Test
    public void testCreateSDNCAdapterRequest() {
        assertEquals(SDNCAdapterRequest.class.getName(), objectFactory.createSDNCAdapterRequest().getClass().getName());
    }

}
