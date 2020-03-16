/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * See the License for the specific language governing perservice2sions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.audit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.skyscreamer.jsonassert.JSONAssert;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuditVServerTest extends AuditVServer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditVServer auditNova = new AuditVServer();

    @Mock
    private AAIResourcesClient aaiResourcesMock;

    private String cloudOwner = "cloudOwner";
    private String cloudRegion = "cloudRegion";
    private String tenantId = "tenantId";

    private AAIResourceUri vserverURI = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion,
            tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db");

    private AAIResourceUri vserverURI2 = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion,
            tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4dz");

    private AAIResourceUri ssc_1_trusted_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE,
            cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_trusted_port_0");

    private AAIResourceUri ssc_1_service1_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE,
            cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_service1_port_0");

    private AAIResourceUri ssc_1_mgmt_port_1_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE,
            cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_mgmt_port_1");

    private AAIResourceUri ssc_1_mgmt_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE,
            cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_mgmt_port_0");

    private AAIResourceUri ssc_1_service2_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE,
            cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_service2_port_0");

    private AAIResourceUri ssc_1_int_ha_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE,
            cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_int_ha_port_0");

    private AAIResourceUri test_port_1_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE, cloudOwner,
            cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4dz", "test_port_1");

    private AAIResourceUri test_port_2_uri = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE, cloudOwner,
            cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4dz", "test_port_2");

    private AAIResourceUri service2_sub_1_uri =
            AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, cloudOwner, cloudRegion, tenantId,
                    "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_service2_port_0", "service2_sub_interface_1");

    private AAIResourceUri service1_sub_0_uri =
            AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, cloudOwner, cloudRegion, tenantId,
                    "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_service1_port_0", "service1_sub_interface_1");

    private AAIResourceUri service1_sub_1_uri =
            AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, cloudOwner, cloudRegion, tenantId,
                    "3a4c2ca5-27b3-4ecc-98c5-06804867c4db", "ssc_1_service1_port_0", "service1_sub_interface_2");



    private Set<Vserver> vserversToAudit = new HashSet<>();

    LInterface test_port_1 = new LInterface();
    LInterface test_port_2 = new LInterface();
    LInterface ssc_1_int_ha_port_0 = new LInterface();
    LInterface service2_sub_interface_1 = new LInterface();
    LInterface ssc_1_service2_port_0 = new LInterface();
    LInterface ssc_1_mgmt_port_0 = new LInterface();
    LInterface ssc_1_mgmt_port_1 = new LInterface();
    LInterface service1_sub_interface_2 = new LInterface();
    LInterface service1_sub_interface_1 = new LInterface();
    LInterface ssc_1_service1_port_0 = new LInterface();
    LInterface ssc_1_trusted_port_0 = new LInterface();



    @Before
    public void setup() {
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        auditNova.setAaiClient(aaiResourcesMock);

        Vserver vServer1 = new Vserver();
        vServer1.setVserverId("3a4c2ca5-27b3-4ecc-98c5-06804867c4db");
        LInterfaces vServer1Linterfaces = new LInterfaces();
        vServer1.setLInterfaces(vServer1Linterfaces);

        ssc_1_trusted_port_0.setInterfaceId("dec8bdc7-5718-41dc-bfbb-561ff6eeb81c");
        ssc_1_trusted_port_0.setInterfaceName("ssc_1_trusted_port_0");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_trusted_port_0);


        ssc_1_service1_port_0.setInterfaceId("1c56a24b-5f03-435a-850d-31cd4252de56");
        ssc_1_service1_port_0.setInterfaceName("ssc_1_service1_port_0");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_service1_port_0);
        ssc_1_service1_port_0.setLInterfaces(new LInterfaces());


        service1_sub_interface_1.setInterfaceId("0d9cd813-2ae1-46c0-9ebb-48081f6cffbb");
        service1_sub_interface_1.setInterfaceName("service1_sub_interface_1");
        ssc_1_service1_port_0.getLInterfaces().getLInterface().add(service1_sub_interface_1);


        service1_sub_interface_2.setInterfaceId("b7019dd0-2ee9-4447-bdef-ac25676b205a");
        service1_sub_interface_2.setInterfaceName("service1_sub_interface_2");
        ssc_1_service1_port_0.getLInterfaces().getLInterface().add(service1_sub_interface_2);


        ssc_1_mgmt_port_1.setInterfaceId("12afcd28-929f-4d80-8a5a-0833bfd5e20b");
        ssc_1_mgmt_port_1.setInterfaceName("ssc_1_mgmt_port_1");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_1);

        ssc_1_mgmt_port_0.setInterfaceId("80baec42-ffae-425f-ad8c-3f7b2c24bfff");
        ssc_1_mgmt_port_0.setInterfaceName("ssc_1_mgmt_port_0");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_0);


        ssc_1_service2_port_0.setLInterfaces(new LInterfaces());
        ssc_1_service2_port_0.setInterfaceId("13eddf95-4cf3-45f2-823a-2d890a6549b4");
        ssc_1_service2_port_0.setInterfaceName("ssc_1_service2_port_0");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_service2_port_0);


        service2_sub_interface_1.setInterfaceId("f711be16-2654-4a09-b89d-0511fda20e81");
        service2_sub_interface_1.setInterfaceName("service2_sub_interface_1");
        ssc_1_service2_port_0.getLInterfaces().getLInterface().add(service2_sub_interface_1);


        ssc_1_int_ha_port_0.setInterfaceId("9cab2903-70f7-44fd-b681-491d6ae2adb8");
        ssc_1_int_ha_port_0.setInterfaceName("ssc_1_int_ha_port_0");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_int_ha_port_0);

        Vserver vServer2 = new Vserver();
        vServer2.setVserverId("3a4c2ca5-27b3-4ecc-98c5-06804867c4dz");
        LInterfaces vServer2Linterfaces = new LInterfaces();
        vServer2.setLInterfaces(vServer2Linterfaces);

        test_port_1.setInterfaceId("9cab2903-70f7-44fd-b681-491d6ae2adz1");
        test_port_1.setInterfaceName("test_port_1");


        test_port_2.setInterfaceId("9cab2903-70f7-44fd-b681-491d6ae2adz2");
        test_port_2.setInterfaceName("test_port_2");

        vServer2.getLInterfaces().getLInterface().add(test_port_1);
        vServer2.getLInterfaces().getLInterface().add(test_port_2);

        vserversToAudit.add(vServer1);
        vserversToAudit.add(vServer2);
    }

    @Test
    public void audit_Vserver_Empty_HashSet() throws JsonParseException, JsonMappingException, IOException {
        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(new HashSet<Vserver>(), tenantId, cloudOwner, cloudRegion);
        assertEquals(Optional.empty(), actual);
    }

    @Test
    public void audit_Vserver_Found_Test() throws JsonParseException, JsonMappingException, IOException {
        doReturn(true).when(aaiResourcesMock).exists(vserverURI);
        doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
        doReturn(Optional.of(ssc_1_trusted_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_trusted_port_0_uri);
        doReturn(Optional.of(ssc_1_service1_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service1_port_0_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_1)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_1_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_0)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_0_uri);
        doReturn(Optional.of(ssc_1_service2_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service2_port_0_uri);
        doReturn(Optional.of(service2_sub_interface_1)).when(aaiResourcesMock).get(LInterface.class,
                service1_sub_1_uri);
        doReturn(Optional.of(ssc_1_int_ha_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_int_ha_port_0_uri);
        doReturn(Optional.of(test_port_1)).when(aaiResourcesMock).get(LInterface.class, test_port_1_uri);
        doReturn(Optional.of(test_port_2)).when(aaiResourcesMock).get(LInterface.class, test_port_2_uri);

        doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);

        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(vserversToAudit, tenantId, cloudOwner, cloudRegion);
        String actualString = objectMapper.writeValueAsString(actual.get());
        String expected = getJson("ExpectedVServerFound.json");
        JSONAssert.assertEquals(expected, actualString, false);
    }

    @Test
    public void audit_Vserver_Found_Test_Network_Not_Found()
            throws JsonParseException, JsonMappingException, IOException {
        doReturn(true).when(aaiResourcesMock).exists(vserverURI);
        doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
        doReturn(Optional.of(ssc_1_trusted_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_trusted_port_0_uri);
        doReturn(Optional.of(ssc_1_service1_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service1_port_0_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_1)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_1_uri);
        doReturn(Optional.empty()).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_0_uri);
        doReturn(Optional.of(ssc_1_service2_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service2_port_0_uri);
        doReturn(Optional.of(ssc_1_int_ha_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_int_ha_port_0_uri);
        doReturn(Optional.of(test_port_1)).when(aaiResourcesMock).get(LInterface.class, test_port_1_uri);
        doReturn(Optional.of(test_port_2)).when(aaiResourcesMock).get(LInterface.class, test_port_2_uri);

        doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);

        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(vserversToAudit, tenantId, cloudOwner, cloudRegion);
        String actualString = objectMapper.writeValueAsString(actual.get());
        String expected = getJson("VServer_Found_network_Not_Found.json");
        JSONAssert.assertEquals(expected, actualString, false);
    }

    @Test
    public void audit_Vserver_Found_Test_Network_Not_Found_Second_Server()
            throws JsonParseException, JsonMappingException, IOException {
        doReturn(true).when(aaiResourcesMock).exists(vserverURI);
        doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
        doReturn(Optional.of(ssc_1_trusted_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_trusted_port_0_uri);
        doReturn(Optional.of(ssc_1_service1_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service1_port_0_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_1)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_1_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_0)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_0_uri);
        doReturn(Optional.of(ssc_1_service2_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service2_port_0_uri);
        doReturn(Optional.of(ssc_1_int_ha_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_int_ha_port_0_uri);
        doReturn(Optional.of(test_port_1)).when(aaiResourcesMock).get(LInterface.class, test_port_1_uri);
        doReturn(Optional.empty()).when(aaiResourcesMock).get(LInterface.class, test_port_2_uri);
        doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);
        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(vserversToAudit, tenantId, cloudOwner, cloudRegion);
        String actualString = objectMapper.writeValueAsString(actual.get());
        String expected = getJson("VServer_Found_Network_Sec_Server_Not_Found.json");
        JSONAssert.assertEquals(expected, actualString, false);
    }

    @Test
    public void audit_Vserver_Not_Found_Test() throws JsonParseException, JsonMappingException, IOException {
        doReturn(false).when(aaiResourcesMock).exists(vserverURI);
        doReturn(false).when(aaiResourcesMock).exists(vserverURI2);
        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(vserversToAudit, tenantId, cloudOwner, cloudRegion);
        String actualString = objectMapper.writeValueAsString(actual.get());
        String expected = getJson("Vservers_Not_Found.json");
        JSONAssert.assertEquals(expected, actualString, false);
    }

    @Test
    public void audit_Vserver_first_Not_Found_Test() throws JsonParseException, JsonMappingException, IOException {
        doReturn(false).when(aaiResourcesMock).exists(vserverURI);
        doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
        doReturn(Optional.of(test_port_1)).when(aaiResourcesMock).get(LInterface.class, test_port_1_uri);
        doReturn(Optional.of(test_port_2)).when(aaiResourcesMock).get(LInterface.class, test_port_2_uri);
        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(vserversToAudit, tenantId, cloudOwner, cloudRegion);
        String actualString = objectMapper.writeValueAsString(actual.get());
        String expected = getJson("Vserver2_Found_VServer1_Not_Found.json");
        JSONAssert.assertEquals(expected, actualString, false);
    }


    @Test
    public void doesSubInterfaceExistinAAI_Test() {
        AAIResourceUri subInterfaceURI = AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, cloudOwner,
                cloudRegion, tenantId, "vserverId", "l-interface", "sub-interface");

        assertEquals(
                "/cloud-infrastructure/cloud-regions/cloud-region/cloudOwner/cloudRegion/tenants/tenant/tenantId/vservers/vserver/vserverId/l-interfaces/l-interface/l-interface/l-interfaces/l-interface/sub-interface",
                subInterfaceURI.build().toString());
    }

    @Test
    public void audit_Vserver_Second_Not_Found_Test() throws JsonParseException, JsonMappingException, IOException {
        doReturn(true).when(aaiResourcesMock).exists(vserverURI);
        doReturn(Optional.of(ssc_1_trusted_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_trusted_port_0_uri);
        doReturn(Optional.of(ssc_1_service1_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service1_port_0_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_1)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_1_uri);
        doReturn(Optional.of(ssc_1_mgmt_port_0)).when(aaiResourcesMock).get(LInterface.class, ssc_1_mgmt_port_0_uri);
        doReturn(Optional.of(ssc_1_service2_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_service2_port_0_uri);
        doReturn(Optional.of(ssc_1_int_ha_port_0)).when(aaiResourcesMock).get(LInterface.class,
                ssc_1_int_ha_port_0_uri);
        doReturn(Optional.empty()).when(aaiResourcesMock).get(LInterface.class, test_port_1_uri);
        doReturn(Optional.empty()).when(aaiResourcesMock).get(LInterface.class, test_port_2_uri);
        doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
        doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);

        doReturn(false).when(aaiResourcesMock).exists(vserverURI2);
        Optional<AAIObjectAuditList> actual =
                auditNova.auditVservers(vserversToAudit, tenantId, cloudOwner, cloudRegion);
        String actualString = objectMapper.writeValueAsString(actual.get());
        String expected = getJson("VServer_Found_Sec_Server_Not_Found2.json");

        JSONAssert.assertEquals(expected, actualString, false);
    }

    @Test
    public void testAuditVserversWithList() {

        AAIObjectAuditList auditList = new AAIObjectAuditList();
        AAIObjectAudit obj1 = new AAIObjectAudit();
        Vserver vserver = new Vserver();
        vserver.setVserverId("testVserverId");
        obj1.setAaiObject(vserver);
        obj1.setResourceURI(AAIUriFactory
                .createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion, tenantId, "testVserverId").build());
        auditList.getAuditList().add(obj1);

        doReturn(false).when(aaiResourcesMock).exists(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner,
                cloudRegion, tenantId, "testVserverId"));

        auditNova.auditVservers(auditList);

        Mockito.verify(aaiResourcesMock).exists(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner,
                cloudRegion, tenantId, "testVserverId"));

        Assert.assertEquals(false, auditList.getAuditList().get(0).isDoesObjectExist());
    }

    @Test
    public void testAuditVserversThroughRelationships() {

        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("id");

        AAIResultWrapper wrapper = new AAIResultWrapper(vfModule);

        doReturn(Optional.of(wrapper)).when(aaiResourcesMock).getFirstWrapper(VfModules.class, VfModule.class,
                AAIUriFactory.createResourceUri(AAIObjectPlurals.VF_MODULE, "genericVnfId").queryParam("vf-module-name",
                        "vfModuleName"));

        Optional<AAIObjectAuditList> auditList =
                auditNova.auditVserversThroughRelationships("genericVnfId", "vfModuleName");

        Assert.assertTrue(auditList.get().getAuditList().isEmpty());
    }

    @Test
    public void testAuditVserversThroughRelationships_exists() throws IOException {

        String vfModule = getJson("vfModule.json");

        AAIResultWrapper wrapper = new AAIResultWrapper(vfModule);
        AAIResultWrapper vserverWrapper = new AAIResultWrapper(new Vserver());

        doReturn(Optional.of(wrapper)).when(aaiResourcesMock).getFirstWrapper(VfModules.class, VfModule.class,
                AAIUriFactory.createResourceUri(AAIObjectPlurals.VF_MODULE, "genericVnfId").queryParam("vf-module-name",
                        "vfModuleName"));

        doReturn(vserverWrapper).when(aaiResourcesMock).get(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER,
                "cloud-owner", "cloud-region-id", "tenant-id", "VUSCHGA1"));

        Optional<AAIObjectAuditList> auditList =
                auditNova.auditVserversThroughRelationships("genericVnfId", "vfModuleName");

        Assert.assertFalse(auditList.get().getAuditList().isEmpty());
    }


    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + filename)));
    }



}
