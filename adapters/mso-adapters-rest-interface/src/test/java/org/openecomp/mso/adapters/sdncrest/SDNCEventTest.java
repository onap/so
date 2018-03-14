/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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
 * limitations under the License
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.sdncrest;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SDNCEventTest {

    private SDNCEvent sdncEvent;
    private Map<String, String> param;
    private String name = "name";
    private String value = "value";

    @Before
    public void setUp() {
        sdncEvent = new SDNCEvent();
    }

    @Test
    public void testGetEventType() {
        sdncEvent.setEventType("eventType");
        Assert.assertNotNull(sdncEvent.getEventType());
        Assert.assertEquals(sdncEvent.getEventType(), "eventType");
    }

    @Test
    public void testGetEventCorrelatorType() {
        sdncEvent.setEventCorrelatorType("eventCorrelatorType");
        Assert.assertNotNull(sdncEvent.getEventCorrelatorType());
        Assert.assertEquals(sdncEvent.getEventCorrelatorType(), "eventCorrelatorType");
    }

    @Test
    public void testGetEventCorrelator() {
        sdncEvent.setEventCorrelator("eventCorrelator");
        Assert.assertNotNull(sdncEvent.getEventCorrelator());
        Assert.assertEquals(sdncEvent.getEventCorrelator(), "eventCorrelator");
    }

    @Test
    public void testGetParams() {
        param = new HashMap<>();
        param.put("paramKey", "paramValue");
        sdncEvent.setParams(param);
        Assert.assertNotNull(sdncEvent.getParams());
        Assert.assertTrue(sdncEvent.getParams().containsKey("paramKey"));
        Assert.assertTrue(sdncEvent.getParams().containsValue("paramValue"));
    }

    @Test
    public void testAddParam() {
        sdncEvent.addParam("name", "value");

    }

}
