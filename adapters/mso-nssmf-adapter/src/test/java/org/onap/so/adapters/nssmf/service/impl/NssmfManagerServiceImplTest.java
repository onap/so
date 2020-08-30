/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.nssmf.service.impl;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.onap.so.adapters.nssmf.consts.NssmfAdapterConsts;
import org.onap.so.adapters.nssmf.entity.NssmfInfo;
import org.onap.so.adapters.nssmf.entity.TokenResponse;
import org.onap.so.adapters.nssmf.enums.HttpMethod;
import org.onap.so.adapters.nssmf.util.RestUtil;
import org.onap.so.beans.nsmf.*;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.unMarshal;
import static org.onap.so.beans.nsmf.NetworkType.CORE;
import static org.onap.so.beans.nsmf.ResourceSharingLevel.NON_SHARED;

@RunWith(SpringRunner.class)
public class NssmfManagerServiceImplTest {

    @Mock
    private RestUtil restUtil;


    private NssmfManagerServiceImpl nssiManagerService;

    @Mock
    private HttpResponse tokenResponse;

    @Mock
    private HttpEntity tokenEntity;

    @Mock
    private HttpResponse commonResponse;

    @Mock
    private HttpEntity commonEntity;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpClient httpClient;

    private InputStream postStream;

    private InputStream tokenStream;

    @Mock
    private ResourceOperationStatusRepository repository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        nssiManagerService = new NssmfManagerServiceImpl();

        Field restUtil = nssiManagerService.getClass().getDeclaredField("restUtil");
        restUtil.setAccessible(true);
        restUtil.set(nssiManagerService, this.restUtil);

        Field repository = nssiManagerService.getClass().getDeclaredField("repository");
        repository.setAccessible(true);
        repository.set(nssiManagerService, this.repository);
        // nssiManagerService.setRestUtil(this.restUtil);

        when(this.restUtil.send(any(String.class), any(HttpMethod.class), any(), any(Header.class)))
                .thenCallRealMethod();
        when(this.restUtil.createResponse(any(Integer.class), any(String.class))).thenCallRealMethod();
    }

    private void createCommonMock(int statusCode, NssmfInfo nssmf) throws Exception {
        when(restUtil.getToken(any(NssmfInfo.class))).thenReturn("7512eb3feb5249eca5ddd742fedddd39");
        when(restUtil.getHttpsClient()).thenReturn(httpClient);

        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(restUtil.getNssmfHost(any(EsrInfo.class))).thenReturn(nssmf);

        when(tokenResponse.getEntity()).thenReturn(tokenEntity);
        when(tokenResponse.getStatusLine()).thenReturn(statusLine);
        when(tokenEntity.getContent()).thenReturn(tokenStream);

        when(commonResponse.getEntity()).thenReturn(commonEntity);
        when(commonResponse.getStatusLine()).thenReturn(statusLine);
        when(commonEntity.getContent()).thenReturn(postStream);

        Answer<HttpResponse> answer = invocation -> {
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length == 1 && arguments[0] != null) {

                HttpRequestBase base = (HttpRequestBase) arguments[0];
                if (base.getURI().toString().endsWith("/oauth/token")) {
                    return tokenResponse;
                } else {
                    return commonResponse;
                }
            }
            return commonResponse;
        };

        doAnswer(answer).when(httpClient).execute(any(HttpRequestBase.class));

    }

    @Test
    public void allocateNssi() throws Exception {

        NssmfInfo nssmf = new NssmfInfo();
        nssmf.setUserName("nssmf-user");
        nssmf.setPassword("nssmf-pass");
        nssmf.setPort("8080");
        nssmf.setIpAddress("127.0.0.1");
        nssmf.setUrl("http://127.0.0.1:8080");

        NssiResponse nssiRes = new NssiResponse();
        nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");
        nssiRes.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");

        TokenResponse token = new TokenResponse();
        token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
        token.setExpires(1800);

        postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
        tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));

        createCommonMock(200, nssmf);


        NssmfAdapterNBIRequest nbiRequest = createAllocateNssi();
        assertNotNull(nbiRequest);
        System.out.println(marshal(nbiRequest));
        ResponseEntity res = nssiManagerService.allocateNssi(nbiRequest);
        assertNotNull(res);
        assertNotNull(res.getBody());
        NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
        assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
        assertEquals(allRes.getNssiId(), "NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");

        System.out.println(res);
    }



    private NssmfAdapterNBIRequest createAllocateNssi() {
        CnSliceProfile sP = new CnSliceProfile();
        List<String> sns = new LinkedList<>();
        sns.add("001-100001");
        List<String> plmn = new LinkedList<>();
        plmn.add("460-00");
        plmn.add("460-01");
        PerfReqEmbb embb = new PerfReqEmbb();
        embb.setActivityFactor(50);
        List<PerfReqEmbb> embbList = new LinkedList<>();
        embbList.add(embb);
        PerfReq perfReq = new PerfReq();
        perfReq.setPerfReqEmbbList(embbList);
        List<String> taList = new LinkedList<>();
        taList.add("1");
        taList.add("2");
        taList.add("3");
        sP.setSnssaiList(sns);
        sP.setSliceProfileId("ab9af40f13f721b5f13539d87484098");
        sP.setPlmnIdList(plmn);
        sP.setPerfReq(perfReq);
        sP.setMaxNumberofUEs(200);
        sP.setCoverageAreaTAList(taList);
        sP.setLatency(6);
        sP.setResourceSharingLevel(NON_SHARED);
        NsiInfo nsiInfo = new NsiInfo();
        nsiInfo.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
        nsiInfo.setNsiName("eMBB-001");
        AllocateCnNssi cnNssi = new AllocateCnNssi();
        cnNssi.setNssiId("NSST-C-001-HDBNJ-NSSMF-01-A-ZX");
        cnNssi.setNssiName("eMBB-001");
        cnNssi.setScriptName("CN1");
        cnNssi.setSliceProfile(sP);
        cnNssi.setNsiInfo(nsiInfo);

        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();
        nbiRequest.setAllocateCnNssi(cnNssi);
        return nbiRequest;
    }

    @Test
    public void deAllocateNssi() throws Exception {
        DeAllocateNssi deAllocateNssi = new DeAllocateNssi();
        deAllocateNssi.setTerminateNssiOption(0);
        List<String> snssai = new LinkedList<>();
        snssai.add("001-100001");
        deAllocateNssi.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
        deAllocateNssi.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
        deAllocateNssi.setScriptName("CN1");
        deAllocateNssi.setSnssaiList(snssai);

        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();
        nbiRequest.setDeAllocateNssi(deAllocateNssi);

        NssmfInfo nssmf = new NssmfInfo();
        nssmf.setUserName("nssmf-user");
        nssmf.setPassword("nssmf-pass");
        nssmf.setPort("8080");
        nssmf.setIpAddress("127.0.0.1");

        NssiResponse nssiRes = new NssiResponse();
        nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");

        TokenResponse token = new TokenResponse();
        token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
        token.setExpires(1800);

        postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
        tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));

        createCommonMock(202, nssmf);
        ResponseEntity res = nssiManagerService.deAllocateNssi(nbiRequest, "ab9af40f13f721b5f13539d87484098");
        assertNotNull(res);
        assertNotNull(res.getBody());
        NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
        assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
        assertNotNull(res);
        assertNotNull(res.getBody());
    }

    @Test
    public void activateNssi() throws Exception {
        NssmfInfo nssmf = new NssmfInfo();
        nssmf.setUserName("nssmf-user");
        nssmf.setPassword("nssmf-pass");
        nssmf.setPort("8080");
        nssmf.setIpAddress("127.0.0.1");

        NssiResponse nssiRes = new NssiResponse();
        nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");

        TokenResponse token = new TokenResponse();
        token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
        token.setExpires(1800);

        postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
        tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));

        ActDeActNssi act = new ActDeActNssi();
        act.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
        act.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");

        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();
        nbiRequest.setActDeActNssi(act);

        createCommonMock(200, nssmf);
        ResponseEntity res = nssiManagerService.activateNssi(nbiRequest, "001-100001");
        assertNotNull(res);
        assertNotNull(res.getBody());
        NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
        assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
    }

    @Test
    public void deActivateNssi() throws Exception {
        NssmfInfo nssmf = new NssmfInfo();
        nssmf.setUserName("nssmf-user");
        nssmf.setPassword("nssmf-pass");
        nssmf.setPort("8080");
        nssmf.setIpAddress("127.0.0.1");

        NssiResponse nssiRes = new NssiResponse();
        nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");

        TokenResponse token = new TokenResponse();
        token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
        token.setExpires(1800);

        postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
        tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));

        ActDeActNssi act = new ActDeActNssi();
        act.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
        act.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");

        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();
        nbiRequest.setActDeActNssi(act);

        createCommonMock(200, nssmf);
        ResponseEntity res = nssiManagerService.deActivateNssi(nbiRequest, "001-100001");
        assertNotNull(res);
        assertNotNull(res.getBody());
        NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
        assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
    }

    @Test
    public void queryJobStatus() throws Exception {
        NssmfInfo nssmf = new NssmfInfo();
        nssmf.setUserName("nssmf-user");
        nssmf.setPassword("nssmf-pass");
        nssmf.setPort("8080");
        nssmf.setIpAddress("127.0.0.1");

        JobStatusResponse jobStatusResponse = new JobStatusResponse();
        ResponseDescriptor descriptor = new ResponseDescriptor();
        descriptor.setResponseId("7512eb3feb5249eca5ddd742fedddd39");
        descriptor.setProgress(20);
        descriptor.setStatusDescription("Initiating VNF Instance");
        descriptor.setStatus("processing");
        jobStatusResponse.setResponseDescriptor(descriptor);

        TokenResponse token = new TokenResponse();
        token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
        token.setExpires(1800);

        postStream = new ByteArrayInputStream(marshal(jobStatusResponse).getBytes(UTF_8));
        tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));

        ResourceOperationStatus operationStatus = new ResourceOperationStatus();
        operationStatus.setOperationId("4b45d919816ccaa2b762df5120f72067");
        operationStatus.setResourceTemplateUUID("8ee5926d-720b-4bb2-86f9-d20e921c143b");
        operationStatus.setServiceId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");

        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();
        nbiRequest.setResponseId("7512eb3feb5249eca5ddd742fedddd39");
        Optional<ResourceOperationStatus> optional = Optional.of(operationStatus);

        doAnswer(invocation -> optional).when(repository).findOne(any());

        createCommonMock(200, nssmf);

        ResponseEntity res = nssiManagerService.queryJobStatus(nbiRequest, "4b45d919816ccaa2b762df5120f72067");
        assertNotNull(res);
        assertNotNull(res.getBody());
        JobStatusResponse allRes = unMarshal(res.getBody().toString(), JobStatusResponse.class);
        assertEquals(allRes.getResponseDescriptor().getProgress(), 20);
        assertEquals(allRes.getResponseDescriptor().getStatus(), "processing");
        assertEquals(allRes.getResponseDescriptor().getResponseId(), "7512eb3feb5249eca5ddd742fedddd39");

        System.out.println(res);

    }

    @Test
    public void queryNSSISelectionCapability() throws Exception {

        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();
        ResponseEntity res = nssiManagerService.queryNSSISelectionCapability(nbiRequest);
        assertNotNull(res);
        assertNotNull(res.getBody());
        Map allRes = unMarshal(res.getBody().toString(), Map.class);
        assertEquals(allRes.get("selection"), "NSMF");

        System.out.println(res);

        nbiRequest.getEsrInfo().setVendor(NssmfAdapterConsts.ONAP_INTERNAL_TAG);
        res = nssiManagerService.queryNSSISelectionCapability(nbiRequest);
        assertNotNull(res);
        assertNotNull(res.getBody());
        allRes = unMarshal(res.getBody().toString(), Map.class);
        assertEquals(allRes.get("selection"), "NSSMF");

        System.out.println(res);

        nbiRequest.getEsrInfo().setNetworkType(NetworkType.ACCESS);
        res = nssiManagerService.queryNSSISelectionCapability(nbiRequest);
        assertNotNull(res);
        assertNotNull(res.getBody());
        allRes = unMarshal(res.getBody().toString(), Map.class);
        assertEquals(allRes.get("selection"), "NSSMF");

        System.out.println(res);
    }

    private NssmfAdapterNBIRequest createNbiRequest() {
        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest();
        EsrInfo esrInfo = new EsrInfo();
        esrInfo.setVendor("huawei");
        esrInfo.setNetworkType(CORE);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceUuid("8ee5926d-720b-4bb2-86f9-d20e921c143b");
        serviceInfo.setServiceInvariantUuid("e75698d9-925a-4cdd-a6c0-edacbe6a0b51");
        serviceInfo.setGlobalSubscriberId("5GCustomer");
        serviceInfo.setServiceType("5G");
        serviceInfo.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
        nbiRequest.setEsrInfo(esrInfo);
        nbiRequest.setServiceInfo(serviceInfo);
        return nbiRequest;
    }

    @Test
    public void querySubnetCapability() {
        NssmfAdapterNBIRequest nbiRequest = createNbiRequest();

        String subnetCapabilityQuery = "\"subnetTypes\": [\"TN-FH\",\"TN-MH\",\"TN-BH\"]";
        nbiRequest.setSubnetCapabilityQuery(subnetCapabilityQuery);
        ResponseEntity res = nssiManagerService.queryNSSISelectionCapability(nbiRequest);
        assertNotNull(res);
        assertNotNull(res.getBody());
    }
}
