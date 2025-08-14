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

package org.onap.so.client.grm;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.so.client.grm.beans.ServiceEndPoint;
import org.onap.so.client.grm.beans.ServiceEndPointList;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceEndPointListTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testUnmarshall() throws Exception {
        String endpointsJson = getFileContentsAsString("__files/grm/endpoints.json");
        ServiceEndPointList sel = mapper.readValue(endpointsJson, ServiceEndPointList.class);

        List<ServiceEndPoint> list = sel.getServiceEndPointList();
        ServiceEndPoint se = list.get(0);

        assertEquals(3, list.size());
        assertEquals("dummy.pod.ns.dummy-pod3", se.getName());
        assertEquals(Integer.valueOf(1), Integer.valueOf(se.getVersion().getMajor()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(se.getVersion().getMinor()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(se.getVersion().getPatch()));
        assertEquals("192.168.120.218", se.getHostAddress());
        assertEquals("32004", se.getListenPort());
        assertEquals("37.7022", se.getLatitude());
        assertEquals("121.9358", se.getLongitude());
        assertEquals("/", se.getContextPath());
        assertEquals("edge", se.getOperationalInfo().getCreatedBy());
        assertEquals("edge", se.getOperationalInfo().getUpdatedBy());
        assertEquals("Environment", se.getProperties().get(0).getName());
        assertEquals("DEV", se.getProperties().get(0).getValue());
    }

    @Test
    public void testUnmarshallServiceEndpointListStartsWithUppercase() throws Exception {
        String endpointsJson = getFileContentsAsString("__files/grm/endpoints2.json");
        ServiceEndPointList sel = mapper.readValue(endpointsJson, ServiceEndPointList.class);

        List<ServiceEndPoint> list = sel.getServiceEndPointList();
        ServiceEndPoint se = list.get(0);

        assertEquals(3, list.size());
        assertEquals("dummy.pod.ns.dummy-pod3", se.getName());
        assertEquals(Integer.valueOf(1), Integer.valueOf(se.getVersion().getMajor()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(se.getVersion().getMinor()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(se.getVersion().getPatch()));
        assertEquals("192.168.120.218", se.getHostAddress());
        assertEquals("32004", se.getListenPort());
        assertEquals("37.7022", se.getLatitude());
        assertEquals("121.9358", se.getLongitude());
        assertEquals("/", se.getContextPath());
        assertEquals("edge", se.getOperationalInfo().getCreatedBy());
        assertEquals("edge", se.getOperationalInfo().getUpdatedBy());
        assertEquals("Environment", se.getProperties().get(0).getName());
        assertEquals("DEV", se.getProperties().get(0).getValue());
    }

    protected String getFileContentsAsString(String fileName) {

        String content = "";
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage());
        }
        return content;
    }
}
