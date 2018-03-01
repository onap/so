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

package org.openecomp.mso.vdu.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VduInfoTest {

    private VduInfo vduInfo;
    private VduStatus status = VduStatus.NOTFOUND;
    private Map<String, Object> outputs;
    private Map<String, Object> inputs;

    @Before
    public void setUp() {
        vduInfo = new VduInfo();
    }

    @Test
    public void testGetVnfInstanceId() {
        String vnfInstanceId = "vnfInstanceId";
        vduInfo.setVnfInstanceId(vnfInstanceId);
        Assert.assertNotNull(vduInfo.getVnfInstanceId());
        Assert.assertEquals(vduInfo.getVnfInstanceId(), "vnfInstanceId");
    }

    @Test
    public void testGetVnfInstanceName() {
        String vnfInstanceName = "vnfInstanceName";
        vduInfo.setVnfInstanceName(vnfInstanceName);
        Assert.assertNotNull(vduInfo.getVnfInstanceName());
        Assert.assertEquals(vduInfo.getVnfInstanceName(), "vnfInstanceName");
    }

    @Test
    public void testGetStatus() {
        vduInfo.setStatus(status);
        Assert.assertNotNull(vduInfo.getStatus());
        Assert.assertEquals(status, vduInfo.getStatus());
    }

    @Test
    public void testGetOutputs() {
        Object obj = new Object();
        String str = "some text";
        outputs = new HashMap<String, Object>();
        outputs.put(str, obj);
        vduInfo.setOutputs(outputs);
        Assert.assertNotNull(vduInfo.getOutputs());
        Assert.assertTrue(vduInfo.getOutputs().containsKey(str));
        Assert.assertTrue(vduInfo.getOutputs().containsValue(obj));
    }

    @Test
    public void testGetInputs() {
        Object obj = new Object();
        String str = "some text";
        inputs = new HashMap<String, Object>();
        inputs.put(str, obj);
        vduInfo.setInputs(inputs);
        Assert.assertNotNull(vduInfo.getInputs());
        Assert.assertTrue(vduInfo.getInputs().containsKey(str));
        Assert.assertTrue(vduInfo.getInputs().containsValue(obj));
    }

}
