/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.so.adapters.vfc.exceptions.ApplicationException;
import org.onap.so.adapters.vfc.model.NSResourceInputParameter;
import org.onap.so.adapters.vfc.model.RestfulResponse;
import org.onap.so.adapters.vfc.util.JsonUtil;
import org.onap.so.adapters.vfc.util.RestfulUtil;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.InstanceNfvoMappingRepository;
import org.onap.so.db.request.data.repository.OperationStatusRepository;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class VfcManagerSol005Test {
    @InjectMocks
    VfcManagerSol005 vfcManagerSol005;

    @Mock
    InstanceNfvoMappingRepository instanceNfvoMappingRepository;

    @Mock
    ResourceOperationStatusRepository resourceOperationStatusRepository;

    @Mock
    OperationStatusRepository operationStatusRepository;

    @Mock
    RestfulUtil restfulUtil;

    OperationStatus operationStatus = new OperationStatus();

    InstanceNfvoMapping instanceNfvoMapping = new InstanceNfvoMapping();
    RestfulResponse restfulResponse = new RestfulResponse();
    RestfulResponse vfcrestfulResponse = new RestfulResponse();
    NSResourceInputParameter nsResourceInputParameter = new NSResourceInputParameter();
    ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus();

    @Test
    public void createNs() throws ApplicationException, Exception {
        restfulResponse.setStatus(200);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        File file = new File(classLoader.getResource("json/createNsReq.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        nsResourceInputParameter = JsonUtil.unMarshal(content, NSResourceInputParameter.class);
        file = new File(classLoader.getResource("json/aainfvoResponse.json").getFile());
        content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        restfulResponse.setResponseContent(content);
        // restfulResponse.setResponseContent("{\"nfvoId\":\"6ee79fe2-9579-475a-9bb9-20cf4358a19e\",\"name\":\"external_nfvo\",\"api-root\":\"xyz\",\"vendor\":\"vz\",\"version\":\"v1.0\",\"url\":\"http://sample.com/\",\"userName\":\"admin\",\"password\":\"sacjnasnc\"}");
        file = new File(classLoader.getResource("json/createNsSol005Response.json").getFile());
        content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        vfcrestfulResponse.setStatus(202);
        vfcrestfulResponse.setResponseContent(content);
        // vfcrestfulResponse.setResponseContent("{\"_links\": {\"heal\": {\"href\": \"\"}, \"instantiate\": {\"href\":
        // \"\"}, \"nestedNsInstances\": {\"href\": \"\"}, \"scale\": {\"href\": \"\"}, \"self\": {\"href\": \"\"},
        // \"terminate\": {\"href\": \"\"}, \"update\": {\"href\": \"\"}}, \"additionalAffinityOrAntiAffiniityRule\":
        // [{\"Scope\": \"\", \"affinityOrAntiAffiinty\": \"\", \"vnfInstanceId\": [], \"vnfProfileId\": [], \"vnfdId\":
        // []}], \"flavourId\": \"\", \"id\": \"c9f0a95e-dea0-4698-96e5-5a79bc5a233d\", \"nestedNsInstanceId\": [],
        // \"nsInstanceDescription\": \"\", \"nsInstanceName\": \"\", \"nsScaleStatus\": [{\"nsScaleLevelId\": \"\",
        // \"nsScalingAspectId\": \"\"}], \"nsState\": \"\", \"nsdId\": \"\", \"nsdInfoId\": \"\", \"pnfInfo\":
        // [{\"cpInfo\": [{\"cpInstanceId\": \"\", \"cpProtocolData\": {\"ipOverEthernet\": {\"ipAddresses\":
        // {\"addressRange\": {\"maxAddress\": \"\", \"minAddress\": \"\"}, \"fixedAddresses\": \"\",
        // \"numDynamicAddresses\": 1, \"subnetId\": \"\", \"type\": \"\"}, \"macAddress\": {}}, \"layerProtocol\":
        // \"IP_OVER_ETHERNET\"}, \"cpdId\": \"\"}], \"pnfId\": \"\", \"pnfName\": \"\", \"pnfProfileId\": \"\",
        // \"pnfdId\": \"\", \"pnfdInfoId\": \"\"}], \"sapInfo\": [{\"description\": \"\", \"id\": \"\", \"sapName\":
        // \"\", \"sapProtocolInfo\": {\"ipOverEthernet\": {\"ipAddresses\": {\"addressRange\": {\"maxAddress\": \"\",
        // \"minAddress\": \"\"}, \"fixedAddresses\": \"\", \"numDynamicAddresses\": 1, \"subnetId\": \"\", \"type\":
        // \"\"}, \"macAddress\": \"\"}, \"layerProtocol\": \"IP_OVER_ETHERNET\"}, \"sapdId\": \"\"}],
        // \"virtualLinkInfo\": [{\"id\": \"\", \"linkPort\": [{\"id\": \"\", \"resourceHandle\": {\"resourceId\": \"\",
        // \"resourceProviderId\": \"\", \"vimId\": \"\", \"vimLevelResourceType\": \"\"}}], \"nsVirtualLinkDescId\":
        // \"\", \"resourceHandle\": [{\"resourceId\": \"\", \"resourceProviderId\": \"\", \"vimId\": \"\",
        // \"vimLevelResourceType\": \"\"}]}], \"vnfInstance\": [{\"vnfInstanceId\": \"\", \"vnfProfileId\": \"\"}],
        // \"vnffgInfo\": [{\"id\": \"\", \"nfpInfo\": [{\"description\": \"\", \"id\": \"\", \"nfpName\": \"\",
        // \"nfpRule\": {\"destinationIpAddressPrefix\": \"\", \"destinationPortRange\": \"\", \"dscp\": \"\",
        // \"etherDestinationAddress\": \"\", \"etherSourceAddress\": \"\", \"etherType\": \"\", \"extendedCriteria\":
        // [{\"length\": 1, \"startingPoint\": 1, \"value\": \"\"}], \"protocol\": \"\", \"sourceIpAddressPrefix\":
        // \"\", \"sourcePortRange\": \"\", \"vlanTag\": []}, \"nfpState\": \"\", \"nfpdId\": \"\", \"nscpHandle\":
        // [{\"nsInstanceId\": \"\", \"nsSapInstanceId\": \"\", \"pnfExtCpInstanceId\": \"\", \"pnfInfoId\": \"\",
        // \"vnfExtCpInstanceId\": \"\", \"vnfInstanceId\": \"\"}], \"totalCp\": 1}], \"nsCpHandle\":
        // [{\"nsInstanceId\": \"\", \"nsSapInstanceId\": \"\", \"pnfExtCpInstanceId\": \"\", \"pnfInfoId\": \"\",
        // \"vnfExtCpInstanceId\": \"\", \"vnfInstanceId\": \"\"}], \"nsVirtualLinkInfoId\": [], \"pnfInfoId\": [],
        // \"vnfInstanceId\": [], \"vnffgdId\": \"\"}]}");

        resourceOperationStatus.setStatus("processing");
        resourceOperationStatus.setOperationId(nsResourceInputParameter.getNsOperationKey().getOperationId());
        resourceOperationStatus.setServiceId(nsResourceInputParameter.getNsOperationKey().getServiceId());
        resourceOperationStatus
                .setResourceTemplateUUID(nsResourceInputParameter.getNsOperationKey().getNodeTemplateUUID());
        when(instanceNfvoMappingRepository.save(instanceNfvoMapping)).thenReturn(instanceNfvoMapping);
        when(restfulUtil.getNfvoFromAAI("b1bb0ce7-2222-4fa7-95ed-4840d70a1101")).thenReturn(restfulResponse);
        when(restfulUtil.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
                .thenReturn(vfcrestfulResponse);
        when(resourceOperationStatusRepository.save(resourceOperationStatus)).thenReturn(resourceOperationStatus);
        vfcManagerSol005.createNs(nsResourceInputParameter);
        assertNotNull(file);

    }

    @Test
    public void terminateNs() throws Exception {
        instanceNfvoMapping.setInstanceId("b1bb0ce7-2222-4fa7-95ed-4840d70a1101");
        instanceNfvoMapping.setPassword("sacjnasnc");
        instanceNfvoMapping.setUsername("admin");
        instanceNfvoMapping.setNfvoName("external_nfvo");
        instanceNfvoMapping.setEndpoint("http://sample.com/");
        instanceNfvoMapping.setApiRoot("xyz");
        String nsInstanceId = "c9f0a95e-dea0-4698-96e5-5a79bc5a233d";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("json/createNsReq.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        nsResourceInputParameter = JsonUtil.unMarshal(content, NSResourceInputParameter.class);
        Map<String, String> header = new HashMap<>();
        header.put("Location", "http://192.168.10.57:5000/ns_lcm_op_ops/12204a12-7da2-4ddf-8c2f-992a1a1acebf");
        vfcrestfulResponse.setStatus(202);
        vfcrestfulResponse.setResponseContent(null);
        vfcrestfulResponse.setRespHeaderMap(header);
        when(instanceNfvoMappingRepository.findOneByInstanceId(nsInstanceId)).thenReturn(instanceNfvoMapping);
        when(restfulUtil.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(vfcrestfulResponse);
        vfcManagerSol005.terminateNs(nsResourceInputParameter.getNsOperationKey(), nsInstanceId);
        assertNotNull(content);
    }

    @Test
    public void getNsProgress() throws Exception {
        String jobId = "12204a12-7da2-4ddf-8c2f-992a1a1acebf";
        instanceNfvoMapping.setInstanceId("b1bb0ce7-2222-4fa7-95ed-4840d70a1101");
        instanceNfvoMapping.setPassword("sacjnasnc");
        instanceNfvoMapping.setUsername("admin");
        instanceNfvoMapping.setNfvoName("external_nfvo");
        instanceNfvoMapping.setEndpoint("http://sample.com/");
        instanceNfvoMapping.setApiRoot("xyz");
        instanceNfvoMapping.setJobId(jobId);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("json/createNsReq.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        nsResourceInputParameter = JsonUtil.unMarshal(content, NSResourceInputParameter.class);
        operationStatus.setProgress("40");
        operationStatus.setServiceId(nsResourceInputParameter.getNsOperationKey().getServiceId());
        operationStatus.setOperationId(nsResourceInputParameter.getNsOperationKey().getOperationId());
        ResourceOperationStatus resourceOperationStatus =
                new ResourceOperationStatus(nsResourceInputParameter.getNsOperationKey().getServiceId(),
                        nsResourceInputParameter.getNsOperationKey().getOperationId(),
                        nsResourceInputParameter.getNsOperationKey().getNodeTemplateUUID());
        file = new File(classLoader.getResource("json/lcmOperRsp.json").getFile());
        content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        vfcrestfulResponse.setStatus(202);
        vfcrestfulResponse.setResponseContent(content);
        List<ResourceOperationStatus> resourceOperationStatuses = new ArrayList<>();
        resourceOperationStatuses.add(resourceOperationStatus);
        when(instanceNfvoMappingRepository.findOneByJobId(jobId)).thenReturn(instanceNfvoMapping);
        when(restfulUtil.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(vfcrestfulResponse);
        when(operationStatusRepository.findOneByServiceIdAndOperationId(
                nsResourceInputParameter.getNsOperationKey().getServiceId(),
                nsResourceInputParameter.getNsOperationKey().getOperationId())).thenReturn(operationStatus);
        when(resourceOperationStatusRepository.findByServiceIdAndOperationId(
                nsResourceInputParameter.getNsOperationKey().getServiceId(),
                nsResourceInputParameter.getNsOperationKey().getOperationId())).thenReturn(resourceOperationStatuses);
        when(operationStatusRepository.save(operationStatus)).thenReturn(operationStatus);
        vfcManagerSol005.getNsProgress(nsResourceInputParameter.getNsOperationKey(), jobId);
        assertNotNull(jobId);

    }

    @Test
    public void instantiateNs() throws Exception {
        String nsInstanceId = "c9f0a95e-dea0-4698-96e5-5a79bc5a233d";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("json/createNsReq.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath())).replace("\n", "");
        nsResourceInputParameter = JsonUtil.unMarshal(content, NSResourceInputParameter.class);
        instanceNfvoMapping.setInstanceId("b1bb0ce7-2222-4fa7-95ed-4840d70a1101");
        instanceNfvoMapping.setPassword("sacjnasnc");
        instanceNfvoMapping.setUsername("admin");
        instanceNfvoMapping.setNfvoName("external_nfvo");
        instanceNfvoMapping.setEndpoint("http://sample.com/");
        instanceNfvoMapping.setApiRoot("xyz");
        resourceOperationStatus.setStatus("processing");
        resourceOperationStatus.setOperationId(nsResourceInputParameter.getNsOperationKey().getOperationId());
        resourceOperationStatus.setServiceId(nsResourceInputParameter.getNsOperationKey().getServiceId());
        resourceOperationStatus
                .setResourceTemplateUUID(nsResourceInputParameter.getNsOperationKey().getNodeTemplateUUID());
        Map<String, String> header = new HashMap<>();
        header.put("Location", "http://192.168.10.57:5000/ns_lcm_op_ops/12204a12-7da2-4ddf-8c2f-992a1a1acebf");
        vfcrestfulResponse.setStatus(202);
        vfcrestfulResponse.setResponseContent(null);
        vfcrestfulResponse.setRespHeaderMap(header);
        when(instanceNfvoMappingRepository.findOneByInstanceId(nsInstanceId)).thenReturn(instanceNfvoMapping);
        when(restfulUtil.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(vfcrestfulResponse);
        when(resourceOperationStatusRepository.save(resourceOperationStatus)).thenReturn(resourceOperationStatus);
        when(instanceNfvoMappingRepository.save(instanceNfvoMapping)).thenReturn(instanceNfvoMapping);
        vfcManagerSol005.instantiateNs(nsInstanceId, nsResourceInputParameter);
        assertNotNull(nsInstanceId);

    }
}
