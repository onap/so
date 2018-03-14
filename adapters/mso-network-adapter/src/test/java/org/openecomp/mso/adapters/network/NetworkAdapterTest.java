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

package org.openecomp.mso.adapters.network;


import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.mso.adapters.network.exceptions.NetworkException;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.NetworkStatus;
import org.openecomp.mso.openstack.beans.RouteTarget;
import org.openecomp.mso.openstack.beans.Subnet;

public class NetworkAdapterTest {

    @Mock
    private static MsoNetworkAdapterImpl adapter;

    @Mock
    private static CatalogDatabase db;

    @BeforeClass
    public static final void prepare () {
        adapter = Mockito.spy (new MsoNetworkAdapterImpl ());
        db = Mockito.mock (CatalogDatabase.class);
        NetworkResource networkResource = new NetworkResource ();
        NetworkResourceCustomization nrc = new NetworkResourceCustomization();
        nrc.setNetworkResource(networkResource);
        nrc.setNetworkType("PROVIDER");
        networkResource.setNeutronNetworkType ("PROVIDER");
        networkResource.setModelUUID("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
        nrc.setNetworkResourceModelUuid(networkResource.getModelUUID());
        networkResource.setOrchestrationMode ("toto");
        Mockito.when (db.getNetworkResource ("PROVIDER")).thenReturn (networkResource);
        Mockito.when (adapter.getCatalogDB ()).thenReturn (db);
    }

    @Test
    public void createTest () {

        List <Integer> vlans = new LinkedList <> ();
        vlans.add (1);
        vlans.add (2);
        List <Subnet> subnets = new LinkedList <> ();
        subnets.add (new Subnet ());
        MsoRequest msoRequest = new MsoRequest ();
        Holder <String> networkId = new Holder <> ();
        Holder <String> neutronNetworkId = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        Holder <NetworkRollback> rollback = new Holder <> ();
        try {
            adapter.createNetwork ("toto",
                                   "tenant",
                                   "PROVIDER",
                                    "modelCustUuid",
                                   "networkName",
                                   "physicalNetworkName",
                                   vlans,
                                   Boolean.TRUE,
                                   Boolean.TRUE,
                                   subnets,
                                   msoRequest,
                                   networkId,
                                   neutronNetworkId,
                                   subnetIdMap,
                                   rollback);
        } catch (NetworkException e) {
            assertTrue (e.getMessage ().contains ("Configuration Error"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }

    @Test
    public void createTest2 () {
        List <Integer> vlans = new LinkedList <> ();
        vlans.add (1);
        vlans.add (2);
        List <Subnet> subnets = new LinkedList <> ();
        List <RouteTarget> routeTargets = new LinkedList <> ();
        subnets.add (new Subnet ());
        List <String> policyFqdns = new LinkedList <> ();
        policyFqdns.add("pfqdn1");
        policyFqdns.add("pfqdn2");
        List <String> routeTableFqdns = new LinkedList <> ();
        routeTableFqdns.add("rtfqdn1");
        routeTableFqdns.add("rtfqdn2");
        MsoRequest msoRequest = new MsoRequest ();
        Holder <String> networkId = new Holder <> ();
        Holder <String> neutronNetworkId = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        Holder <NetworkRollback> rollback = new Holder <> ();
        Holder <String> networkFqdn= new Holder <> ();
        try {
            adapter.createNetworkContrail ("toto",
                                           "tenant",
                                           "PROVIDER",
                                            "modelCustUuid",
                                           "networkName",
                                           routeTargets,
                                           "shared",
                                           "external",
                                           Boolean.TRUE,
                                           Boolean.TRUE,
                                           subnets,
                                           policyFqdns,
                                           routeTableFqdns,
                                           msoRequest,
                                           networkId,
                                           neutronNetworkId,
                                           networkFqdn,
                                           subnetIdMap,
                                           rollback);
        } catch (NetworkException e) {
            assertTrue (e.getMessage ().contains ("Configuration Error"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }

    @Test
    public void updateTest () {
        List <Integer> vlans = new LinkedList <> ();
        vlans.add (1);
        vlans.add (2);
        List <Subnet> subnets = new LinkedList <> ();
        subnets.add (new Subnet ());
        MsoRequest msoRequest = new MsoRequest ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        Holder <NetworkRollback> rollback = new Holder <> ();
        try {
            adapter.updateNetwork ("toto",
                                   "tenant",
                                   "PROVIDER",
                                    "modelCustUuid",
                                   "networkId",
                                   "networkName",
                                   "physicalNetworkName",
                                   vlans,
                                   subnets,
                                   msoRequest,
                                   subnetIdMap,
                                   rollback);
        } catch (NetworkException e) {
            assertTrue (e.getMessage ().contains ("Configuration Error"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }

    @Test
    public void updateTest2 () {
        List <Integer> vlans = new LinkedList <> ();
        vlans.add (1);
        vlans.add (2);
        List <Subnet> subnets = new LinkedList <> ();
        List <RouteTarget> routeTargets = new LinkedList <> ();
        subnets.add (new Subnet ());
        List <String> policyFqdns = new LinkedList <> ();
        policyFqdns.add("pfqdn1");
        List <String> routeTableFqdns = new LinkedList <> ();
        routeTableFqdns.add("rtfqdn1");
        routeTableFqdns.add("rtfqdn2");
        MsoRequest msoRequest = new MsoRequest ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        Holder <NetworkRollback> rollback = new Holder <> ();
        try {
            adapter.updateNetworkContrail ("toto",
                                           "tenant",
                                           "PROVIDER",
                                            "modelCustUuid",
                                           "networkId",
                                           "networkName",
                                           routeTargets,
                                           "shared",
                                           "external",
                                           subnets,
                                           policyFqdns,
                                           routeTableFqdns,
                                           msoRequest,
                                           subnetIdMap,
                                           rollback);
        } catch (NetworkException e) {
            assertTrue (e.getMessage ().contains ("Configuration Error"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }

    @Test
    public void queryTest () {
        Holder <List <Integer>> vlans = new Holder <> ();
        Holder <NetworkStatus> status = new Holder <> ();
        MsoRequest msoRequest = new MsoRequest ();
        Holder <String> networkId = new Holder <> ();
        Holder <Boolean> result = new Holder <> ();
        Holder <String> neutronNetworkId = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        try {
            adapter.queryNetwork (null,
                                  "tenant",
                                  "networkName",
                                  msoRequest,
                                  result,
                                  networkId,
                                  neutronNetworkId,
                                  status,
                                  vlans,
                                  subnetIdMap);
        } catch (NetworkException e) {
            assertTrue (e.getMessage ().contains ("Missing mandatory parameter"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }

    @Test
    public void queryTest2 () {
        Holder <List <RouteTarget>> routeTargets = new Holder <> ();
        Holder <NetworkStatus> status = new Holder <> ();
        MsoRequest msoRequest = new MsoRequest ();
        Holder <String> networkId = new Holder <> ();
        Holder <Boolean> result = new Holder <> ();
        Holder <String> neutronNetworkId = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        try {
            adapter.queryNetworkContrail (null,
                                          "tenant",
                                          "networkName",
                                          msoRequest,
                                          result,
                                          networkId,
                                          neutronNetworkId,
                                          status,
                                          routeTargets,
                                          subnetIdMap);
        } catch (NetworkException e) {
            assertTrue (e.getMessage ().contains ("Missing mandatory parameter"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }

    @Test
    public void deleteTest () {
        Holder <Boolean> networkDeleted = new Holder<> ();
        MsoRequest msoRequest = new MsoRequest ();
        try {
            adapter.deleteNetwork ("toto", "tenant", "PROVIDER", "modelCustUuid", "networkId", msoRequest, networkDeleted);
        } catch (NetworkException e) {
        	e.printStackTrace();
            assertTrue (e.getMessage ().contains ("Cloud Site [toto] not found"));
        } catch (java.lang.NullPointerException npe) {
        	
        }
    }
}
