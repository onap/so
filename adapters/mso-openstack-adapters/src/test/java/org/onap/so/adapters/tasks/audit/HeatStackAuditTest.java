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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.tasks.audit;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.tasks.audit.AuditVServer;
import org.onap.so.adapters.tasks.audit.HeatStackAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.MsoNeutronUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.quantum.model.Port;


@RunWith(MockitoJUnitRunner.Silent.class)
public class HeatStackAuditTest extends HeatStackAudit {

    @InjectMocks
    private HeatStackAudit heatStackAudit = new HeatStackAudit();

    @Mock
    private MsoHeatUtils msoHeatUtilsMock;

    @Mock
    private MsoNeutronUtils neutronUtilsMock;

    @Mock
    private AuditVServer auditVserver;

    private static final String cloudRegion = "cloudRegion";
    private static final String tenantId = "tenantId";

    private Resources resources = new Resources();

    private ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(Include.NON_NULL);

    private ObjectMapper stackObjectMapper = new ObjectMapper()
            .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true).setSerializationInclusion(Include.NON_NULL);

    private List<Optional<Port>> portList = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        resources = objectMapper.readValue(new File("src/test/resources/GetResources.json"), Resources.class);
        Port neutronPort1 = stackObjectMapper.readValue(new File("src/test/resources/NeutronPort1.json"), Port.class);
        doReturn(Optional.of(neutronPort1)).when(neutronUtilsMock)
                .getNeutronPort("7ee06d9d-3d18-411c-9d3e-aec930f70413", cloudRegion, tenantId);
        Port neutronPort2 = stackObjectMapper.readValue(new File("src/test/resources/NeutronPort2.json"), Port.class);
        doReturn(Optional.of(neutronPort2)).when(neutronUtilsMock)
                .getNeutronPort("27391d94-33af-474a-927d-d409249e8fd3", cloudRegion, tenantId);
        Port neutronPort3 = stackObjectMapper.readValue(new File("src/test/resources/NeutronPort3.json"), Port.class);
        doReturn(Optional.of(neutronPort3)).when(neutronUtilsMock)
                .getNeutronPort("fdeedf37-c01e-4ab0-bdd6-8d5fc4913943", cloudRegion, tenantId);
        Port neutronPort4 = stackObjectMapper.readValue(new File("src/test/resources/NeutronPort4.json"), Port.class);
        doReturn(Optional.of(neutronPort4)).when(neutronUtilsMock)
                .getNeutronPort("8d93f63e-e972-48c7-ad98-b2122da47315", cloudRegion, tenantId);
        Port neutronPort5 = stackObjectMapper.readValue(new File("src/test/resources/NeutronPort5.json"), Port.class);
        doReturn(Optional.of(neutronPort5)).when(neutronUtilsMock)
                .getNeutronPort("0594a2f2-7ea4-42eb-abc2-48ea49677fca", cloudRegion, tenantId);
        Port neutronPort6 = stackObjectMapper.readValue(new File("src/test/resources/NeutronPort6.json"), Port.class);
        doReturn(Optional.of(neutronPort6)).when(neutronUtilsMock)
                .getNeutronPort("00bb8407-650e-48b5-b919-33b88d6f8fe3", cloudRegion, tenantId);


        portList.add(Optional.empty());
        portList.add(Optional.of(neutronPort1));
        portList.add(Optional.of(neutronPort2));
        portList.add(Optional.of(neutronPort3));
        portList.add(Optional.of(neutronPort4));
        portList.add(Optional.of(neutronPort5));
        portList.add(Optional.of(neutronPort6));
    }

    @Test
    public void extract_proper_path_Test() {
        Optional<String> actualResult = extractStackPathFromHref(
                "https://orchestration.com:8004/v1/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81");
        assertEquals("/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81", actualResult.get());
    }

    @Test
    public void extract_proper_resources_path_Test() {
        Optional<String> actualResult = extractResourcePathFromHref(
                "https://orchestration.com:8004/v1/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81");
        assertEquals("/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81/resources", actualResult.get());
    }

    @Test
    public void extract_invalid_uri_Test() {
        Optional<String> actualResult = extractStackPathFromHref(
                "orchestrn.com:8004/v18b44d60a6f94bdcb2738f9e//stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81");
        assertEquals(false, actualResult.isPresent());
    }

    @Test
    public void createVserverSet_Test() throws Exception {
        List<Resource> novaResources = resources.getList().stream().filter(p -> "OS::Nova::Server".equals(p.getType()))
                .collect(Collectors.toList());

        List<Resource> resourceGroups = resources.getList().stream()
                .filter(p -> "OS::Heat::ResourceGroup".equals(p.getType())).collect(Collectors.toList());

        Resources service1QueryResponse = objectMapper
                .readValue(new File("src/test/resources/Service1ResourceGroupResponse.json"), Resources.class);
        doReturn(service1QueryResponse).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz/31d0647a-6043-49a4-81b6-ccab29380672/resources",
                cloudRegion, tenantId, Resources.class);

        Resources service2QueryResponse = objectMapper
                .readValue(new File("src/test/resources/Service2ResourceGroupResponse.json"), Resources.class);
        doReturn(service2QueryResponse).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service2_port_0_subinterfaces-hlzdigtimzst/447a9b41-714e-434b-b1d0-6cce8d9f0f0c/resources",
                cloudRegion, tenantId, Resources.class);


        Stack service2StackQuerySubInt =
                stackObjectMapper.readValue(new File("src/test/resources/Service2SubInterface0.json"), Stack.class);
        doReturn(service2StackQuerySubInt).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service2_port_0_subinterfaces-hlzdigtimzst-0-upfi5nhurk7y/f711be16-2654-4a09-b89d-0511fda20e81",
                cloudRegion, tenantId, Stack.class);
        Resources service2ResourceQuerySubInt = objectMapper
                .readValue(new File("src/test/resources/Service2SubInterface1Resources.json"), Resources.class);
        doReturn(service2ResourceQuerySubInt).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service2_port_0_subinterfaces-hlzdigtimzst-0-upfi5nhurk7y/f711be16-2654-4a09-b89d-0511fda20e81/resources",
                cloudRegion, tenantId, Resources.class);

        Stack service1StackQuerySubInt1 =
                stackObjectMapper.readValue(new File("src/test/resources/Service1SubInterface0.json"), Stack.class);
        doReturn(service1StackQuerySubInt1).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-1-fmn5laetg5cs/0d9cd813-2ae1-46c0-9ebb-48081f6cffbb",
                cloudRegion, tenantId, Stack.class);
        Resources service1ResourceQuerySubInt1 = objectMapper
                .readValue(new File("src/test/resources/Service1SubInterface0Resources.json"), Resources.class);
        doReturn(service1ResourceQuerySubInt1).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-1-fmn5laetg5cs/0d9cd813-2ae1-46c0-9ebb-48081f6cffbb/resources",
                cloudRegion, tenantId, Resources.class);


        Stack service1StackQuerySubInt2 =
                stackObjectMapper.readValue(new File("src/test/resources/Service1SubInterface1.json"), Stack.class);
        doReturn(service1StackQuerySubInt2).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-0-yghihziaf36m/b7019dd0-2ee9-4447-bdef-ac25676b205a",
                cloudRegion, tenantId, Stack.class);
        Resources service1ResourceQuerySubInt2 = objectMapper
                .readValue(new File("src/test/resources/Service1SubInterface1Resources.json"), Resources.class);
        doReturn(service1ResourceQuerySubInt2).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-0-yghihziaf36m/b7019dd0-2ee9-4447-bdef-ac25676b205a/resources",
                cloudRegion, tenantId, Resources.class);

        Stack service1StackQuerySubInt3 =
                stackObjectMapper.readValue(new File("src/test/resources/Service1SubInterface2.json"), Stack.class);
        doReturn(service1StackQuerySubInt3).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-2-y3ndsavmsymv/bd0fc728-cbde-4301-a581-db56f494675c",
                cloudRegion, tenantId, Stack.class);
        Resources service1ResourceQuerySubInt3 = objectMapper
                .readValue(new File("src/test/resources/Service1SubInterface2Resources.json"), Resources.class);
        doReturn(service1ResourceQuerySubInt3).when(msoHeatUtilsMock).executeHeatClientRequest(
                "/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-2-y3ndsavmsymv/bd0fc728-cbde-4301-a581-db56f494675c/resources",
                cloudRegion, tenantId, Resources.class);

        Set<Vserver> vServersToAudit = heatStackAudit.createVserverSet(resources, novaResources, portList);
        Set<Vserver> vserversWithSubInterfaces =
                heatStackAudit.processSubInterfaces(cloudRegion, tenantId, resourceGroups, vServersToAudit);

        String actualValue = objectMapper.writeValueAsString(vserversWithSubInterfaces);
        String expectedValue = getJson("ExpectedVserversToAudit.json");
        JSONAssert.assertEquals(expectedValue, actualValue, false);
    }

    @Test
    public void auditHeatStackNoServers_Test() throws Exception {
        Resources getResource = objectMapper
                .readValue(new File("src/test/resources/Service1ResourceGroupResponse.json"), Resources.class);
        doReturn(getResource).when(msoHeatUtilsMock).queryStackResources(cloudRegion, tenantId, "heatStackName", 3);

        Optional<AAIObjectAuditList> actual =
                heatStackAudit.auditHeatStack(cloudRegion, "cloudOwner", tenantId, "heatStackName");
        assertEquals(true, actual.get().getAuditList().isEmpty());
    }

    @Test
    public void auditHeatStackNestedServers_Test() throws Exception {
        Resources getResource =
                objectMapper.readValue(new File("src/test/resources/GetNestedResources.json"), Resources.class);
        List<Resource> novaResources = getResource.getList().stream()
                .filter(p -> "OS::Nova::Server".equals(p.getType())).collect(Collectors.toList());
        List<Resource> resourceGroups = getResource.getList().stream()
                .filter(p -> "OS::Heat::ResourceGroup".equals(p.getType())).collect(Collectors.toList());

        doReturn(getResource).when(msoHeatUtilsMock).queryStackResources(cloudRegion, tenantId, "heatStackName", 3);
        Set<Vserver> vServersToAudit = heatStackAudit.createVserverSet(resources, novaResources, portList);
        Set<Vserver> vserversWithSubInterfaces =
                heatStackAudit.processSubInterfaces(cloudRegion, tenantId, resourceGroups, vServersToAudit);
        String actualValue = objectMapper.writeValueAsString(vserversWithSubInterfaces);
        String expectedValue = getJson("NestedExpectedValue.json");
        JSONAssert.assertEquals(expectedValue, actualValue, false);
    }


    @Test
    public void findInterfaceInformation_Test() {
        List<Resource> novaResources = resources.getList().stream().filter(p -> "OS::Nova::Server".equals(p.getType()))
                .collect(Collectors.toList());
        Set<Vserver> expectedVservers = new HashSet<>();
        Vserver vServer1 = new Vserver();
        vServer1.setVserverId("92272b67-d23f-42ca-87fa-7b06a9ec81f3");
        LInterfaces vServer1Linterfaces = new LInterfaces();
        vServer1.setLInterfaces(vServer1Linterfaces);

        LInterface ssc_1_trusted_port_0 = new LInterface();
        ssc_1_trusted_port_0.setInterfaceId("7ee06d9d-3d18-411c-9d3e-aec930f70413");
        ssc_1_trusted_port_0.setInterfaceName("ibcx0026v_ibcx0026vm003_untrusted_port");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_trusted_port_0);

        LInterface ssc_1_svc2_port_0 = new LInterface();
        ssc_1_svc2_port_0.setInterfaceId("27391d94-33af-474a-927d-d409249e8fd3");
        ssc_1_svc2_port_0.setInterfaceName("ibcx0026v_ibcx0026vm003_untrusted_port");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_svc2_port_0);

        LInterface ssc_1_mgmt_port_1 = new LInterface();
        ssc_1_mgmt_port_1.setInterfaceId("fdeedf37-c01e-4ab0-bdd6-8d5fc4913943");
        ssc_1_mgmt_port_1.setInterfaceName("ibcx0026v_ibcx0026vm003_untrusted_port");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_1);

        LInterface ssc_1_mgmt_port_0 = new LInterface();
        ssc_1_mgmt_port_0.setInterfaceId("8d93f63e-e972-48c7-ad98-b2122da47315");
        ssc_1_mgmt_port_0.setInterfaceName("ibcx0026v_ibcx0026vm003_untrusted_port");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_0);

        LInterface ssc_1_svc1_port_0 = new LInterface();
        ssc_1_svc1_port_0.setInterfaceId("0594a2f2-7ea4-42eb-abc2-48ea49677fca");
        ssc_1_svc1_port_0.setInterfaceName("ibcx0026v_ibcx0026vm003_untrusted_port");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_svc1_port_0);

        LInterface ssc_1_int_ha_port_0 = new LInterface();
        ssc_1_int_ha_port_0.setInterfaceId("00bb8407-650e-48b5-b919-33b88d6f8fe3");
        ssc_1_int_ha_port_0.setInterfaceName("ibcx0026v_ibcx0026vm003_untrusted_port");
        vServer1.getLInterfaces().getLInterface().add(ssc_1_int_ha_port_0);

        expectedVservers.add(vServer1);

        Set<Vserver> actualVservers = heatStackAudit.createVserverSet(resources, novaResources, portList);

        assertThat(actualVservers, sameBeanAs(expectedVservers));
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + filename)));
    }
}
