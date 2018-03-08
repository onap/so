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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequest;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

public class NetworkRequestHandlerTest {

    NetworkRequestHandler handler = null;

    UriInfo uriInfo = null;

    @Before
    public void setup() throws Exception {

        uriInfo = Mockito.mock(UriInfo.class);
        Class<?> clazz = NetworkRequestHandler.class;
        handler = (NetworkRequestHandler) clazz.newInstance();

        Field f1 = handler.getClass().getDeclaredField("uriInfo");

        f1.setAccessible(true);
        f1.set(handler, uriInfo);
    }

    @Test
    public void manageVnfRequestTest() {
        Response resp = handler.manageNetworkRequest("<name>Test</name>", "v2");
        assertTrue(null != resp);
    }

    @Test
    public void manageVnfRequestTestV1() {
        Response resp = handler.manageNetworkRequest("<name>Test</name>", "v1");
        assertTrue(null != resp);
    }

    @Test
    public void manageVnfRequestTestV3() {
        Response resp = handler.manageNetworkRequest("<name>Test</name>", "v3");
        assertTrue(null != resp);
    }

    @Test
    public void manageVnfRequestTestInvalidVersion() {
        Response resp = handler.manageNetworkRequest("<name>Test</name>", "v249");
        assertTrue(null != resp);
    }

    @Test
    public void manageVnfRequest2Test() {
        Mockito.when(uriInfo.getRequestUri())
                .thenReturn(URI.create("http://localhost:8080/test"));

        new MockUp<MsoPropertiesUtils>() {
            @Mock
            public synchronized final boolean getNoPropertiesState() {
                return false;
            }
        };
        Response resp = handler.manageNetworkRequest("<name>Test</name>", "v2");
        assertTrue(null != resp);
    }

    @Test
    public void fillNetworkRequestTestV1() {
        NetworkRequest qr = new NetworkRequest();
        InfraRequests ar = new InfraRequests();
        ar.setVnfId("1003");
        ar.setVnfName("vnf");
        ar.setVnfType("vnt");
        ar.setTenantId("48889690");
        ar.setProvStatus("uuu");
        ar.setVolumeGroupName("volume");
        ar.setVolumeGroupId("38838");
        ar.setServiceType("vnf");
        ar.setAicNodeClli("djerfe");
        ar.setAaiServiceId("599499");
        ar.setAicCloudRegion("south");
        ar.setVfModuleName("m1");
        ar.setVfModuleId("39949");
        ar.setVfModuleModelName("test");
        ar.setAaiServiceId("37728");
        ar.setVnfParams("test");
        handler.fillNetworkRequest(qr, ar, "v1");
        String param = (String) qr.getNetworkParams();
        assertTrue(param.equals("test"));
    }

    @Test
    public void fillNetworkRequestTestV2() {
        NetworkRequest qr = new NetworkRequest();
        InfraRequests ar = new InfraRequests();
        ar.setVnfId("1003");
        ar.setVnfName("vnf");
        ar.setVnfType("vnt");
        ar.setTenantId("48889690");
        ar.setProvStatus("uuu");
        ar.setVolumeGroupName("volume");
        ar.setVolumeGroupId("38838");
        ar.setServiceType("vnf");
        ar.setAicNodeClli("djerfe");
        ar.setAaiServiceId("599499");
        ar.setAicCloudRegion("south");
        ar.setVfModuleName("m1");
        ar.setVfModuleId("39949");
        ar.setVfModuleModelName("test");
        ar.setAaiServiceId("37728");
        ar.setVnfParams("test");
        handler.fillNetworkRequest(qr, ar, "v2");
        String param = (String) qr.getNetworkParams();
        assertTrue(param.equals("test"));
    }

    @Test
    public void fillNetworkRequestTestV3() {
        NetworkRequest qr = new NetworkRequest();
        InfraRequests ar = new InfraRequests();
        ar.setVnfId("1003");
        ar.setVnfName("vnf");
        ar.setVnfType("vnt");
        ar.setTenantId("48889690");
        ar.setProvStatus("uuu");
        ar.setVolumeGroupName("volume");
        ar.setVolumeGroupId("38838");
        ar.setServiceType("vnf");
        ar.setAicNodeClli("djerfe");
        ar.setAaiServiceId("599499");
        ar.setAicCloudRegion("south");
        ar.setVfModuleName("m1");
        ar.setVfModuleId("39949");
        ar.setVfModuleModelName("test");
        ar.setAaiServiceId("37728");
        ar.setVnfParams("test");
        handler.fillNetworkRequest(qr, ar, "v3");
        String param = (String) qr.getNetworkParams();
        assertTrue(param.equals("test"));
    }

    @Test
    public void queryFiltersTest() {
        new MockUp<RequestsDatabase>() {
            @Mock
            public List<InfraActiveRequests> getRequestListFromInfraActive(String queryAttributeName,
                                                                           String queryValue,
                                                                           String requestType) {
                List<InfraActiveRequests> list = new ArrayList<>();
                InfraActiveRequests req = new InfraActiveRequests();
                req.setAaiServiceId("299392");
                req.setAction("CREATE");
                req.setRequestStatus("COMPLETE");
                req.setProgress(10001L);
                req.setSource("test");
                req.setStartTime(new Timestamp(10020100));
                req.setEndTime(new Timestamp(20020100));
                req.setStatusMessage("message");
                list.add(req);
                return list;
            }
        };
        Response resp = handler.queryFilters("networkType", "serviceType", "aicNodeClli", "tenantId", "v1");
        assertTrue(resp.getEntity().toString() != null);
    }

    @Test
    public void getRequestTest() {
        new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests getRequestFromInfraActive(String requestId, String requestType) {
                InfraActiveRequests req = new InfraActiveRequests();
                req.setAaiServiceId("299392");
                req.setAction("CREATE");
                req.setRequestStatus("COMPLETE");
                req.setProgress(10001L);
                req.setSource("test");
                req.setStartTime(new Timestamp(10020100));
                req.setEndTime(new Timestamp(20020100));
                req.setStatusMessage("message");
                return req;
            }
        };
        Response resp = handler.getRequest("388293", "v1");
        assertTrue(resp.getEntity().toString() != null);
    }

}
