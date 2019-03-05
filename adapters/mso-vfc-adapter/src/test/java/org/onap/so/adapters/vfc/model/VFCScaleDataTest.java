/*
 * ============LICENSE_START==========================================
 *  ONAP - SO
 * ===================================================================
 *  Copyright (c) 2019 IBM.
 * ===================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * ============LICENSE_END=============================================
 * ====================================================================
 */
package org.onap.so.adapters.vfc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class VFCScaleDataTest {

    VFCScaleData vFCScaleData;
    List<ScaleNsByStepsData> scaleNsByStepsData;

    @Before
    public void setUp() {
        vFCScaleData = new VFCScaleData();
        scaleNsByStepsData = new ArrayList<>();
    }

    @Test
    public void testGetNsInstanceId() {
        vFCScaleData.setNsInstanceId("NsInstanceId");
        assertEquals("NsInstanceId", vFCScaleData.getNsInstanceId());
    }

    @Test
    public void testGetScaleType() {
        vFCScaleData.setScaleType("scaleType");
        assertEquals("scaleType", vFCScaleData.getScaleType());
    }

    @Test
    public void testGetScaleNsData() {
        vFCScaleData.setScaleNsData(scaleNsByStepsData);
        assertNotNull(vFCScaleData.getScaleNsData());
    }

}
