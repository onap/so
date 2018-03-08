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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.NetworkResource;

public class NetworkTypesHandlerTest {

    NetworkTypesHandler handler = new NetworkTypesHandler();

    @Test
    public void getNetworkTypesTest() {
        Response resp = handler.getNetworkTypes("v2");
        assertTrue(resp.getEntity().toString() != null);
    }

    @Test
    public void getNetworkTypesTest2() {
        new MockUp<CatalogDatabase>() {
            @Mock
            public List<NetworkResource> getAllNetworkResources() {
                return null;
            }
        };
        Response resp = handler.getNetworkTypes("v2");
        assertTrue(resp.getEntity().toString() != null);
    }

    @Test
    public void getNetworkTypesTest3() {
        List<NetworkResource> netList = new ArrayList<>();
        new MockUp<CatalogDatabase>() {
            @Mock
            public List<NetworkResource> getAllNetworkResources() {
                NetworkResource ns = new NetworkResource();
                netList.add(ns);
                return netList;
            }
        };
        Response resp = handler.getNetworkTypes("v2");
        assertTrue(resp.getEntity().toString() != null);
    }
}
