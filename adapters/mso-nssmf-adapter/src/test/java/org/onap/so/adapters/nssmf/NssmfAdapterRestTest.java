/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.nssmf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.so.adapters.nssmf.entity.TokenResponse;
import org.onap.so.adapters.nssmf.enums.HttpMethod;
import org.onap.so.adapters.nssmf.rest.NssmfAdapterRest;
import org.onap.so.adapters.nssmf.entity.NssmfInfo;
import org.onap.so.adapters.nssmf.rest.NssmfManager;
import org.onap.so.adapters.nssmf.util.RestUtil;
import org.onap.so.beans.nsmf.ActDeActNssi;
import org.onap.so.beans.nsmf.AllocateCnNssi;
import org.onap.so.beans.nsmf.CnSliceProfile;
import org.onap.so.beans.nsmf.DeAllocateNssi;
import org.onap.so.beans.nsmf.EsrInfo;
import org.onap.so.beans.nsmf.JobStatusRequest;
import org.onap.so.beans.nsmf.NsiInfo;
import org.onap.so.beans.nsmf.NssiActDeActRequest;
import org.onap.so.beans.nsmf.NssiAllocateRequest;
import org.onap.so.beans.nsmf.NssiDeAllocateRequest;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.PerfReq;
import org.onap.so.beans.nsmf.PerfReqEmbb;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
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
public class NssmfAdapterRestTest {

    private final static String ALLOCATE = "{\n" + "  \"esrInfo\" : {\n" + "    \"vendor\" : \"huawei\",\n"
            + "    \"networkType\" : \"cn\"\n" + "  },\n" + "  \"allocateCnNssi\" : {\n"
            + "    \"nssiId\" : \"NSST-C-001-HDBNJ-NSSMF-01-A-ZX\",\n" + "    \"nssiName\" : \"eMBB-001\",\n"
            + "    \"sliceProfile\" : {\n" + "      \"snssaiList\" : [ \"001-100001\" ],\n"
            + "      \"sliceProfileId\" : \"ab9af40f13f721b5f13539d87484098\",\n"
            + "      \"plmnIdList\" : [ \"460-00\", \"460-01\" ],\n" + "      \"perfReq\" : {\n"
            + "        \"perfReqEmbbList\" : [ {\n" + "          \"activityFactor\" : 50\n" + "        } ]\n"
            + "      },\n" + "      \"maxNumberofUEs\" : 200,\n"
            + "      \"coverageAreaTAList\" : [ \"1\", \"2\", \"3\" ],\n" + "      \"latency\" : 6,\n"
            + "      \"resourceSharingLevel\" : \"non-shared\"\n" + "    },\n" + "    \"scriptName\" : \"CN1\",\n"
            + "    \"nsiInfo\" : {\n" + "      \"nsiName\" : \"eMBB-001\",\n"
            + "      \"nsiId\" : \"NSI-M-001-HDBNJ-NSMF-01-A-ZX\"\n" + "    }\n" + "  }\n" + "}";

    private NssmfManager nssmfMgr;

    @Mock
    private ResourceOperationStatusRepository rscOperStatusRepo;

    @Mock
    private RestUtil restUtil;

    @Mock
    private NssmfAdapterRest nssmfRest;

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

    @Before
    public void setUp() {
        initMocks(this);
        nssmfMgr = new NssmfManager();
        nssmfMgr.setRestUtil(restUtil);
        nssmfMgr.setRscOperStatusRepo(rscOperStatusRepo);
    }

    private void createCommonMock(int statusCode, NssmfInfo nssmf) throws Exception {
        when(this.restUtil.send(any(String.class), any(HttpMethod.class), any(String.class), any(Header.class)))
                .thenCallRealMethod();
        when(this.restUtil.createResponse(any(Integer.class), any(String.class))).thenCallRealMethod();
        when(nssmfRest.getNssmfMgr()).thenReturn(nssmfMgr);
        // when(nssmfRest.createAllocateNssi(any(NssiAllocateRequest.class))).thenCallRealMethod();
        // when(nssmfRest.deAllocateNssi(any(NssiDeAllocateRequest.class), any(String.class))).thenCallRealMethod();
        // when(nssmfRest.activateNssi(any(NssiActDeActRequest.class), any(String.class))).thenCallRealMethod();
        // when(nssmfRest.deactivateNssi(any(NssiActDeActRequest.class), any(String.class))).thenCallRealMethod();
        //
        // when(nssmfRest.queryJobStatus(any(JobStatusRequest.class), any(String.class))).thenCallRealMethod();
        when(restUtil.sendRequest(any(String.class), any(HttpMethod.class), any(String.class), any(EsrInfo.class)))
                .thenCallRealMethod();
        when(restUtil.getHttpsClient()).thenReturn(httpClient);

        when(statusLine.getStatusCode()).thenReturn(200);
        when(restUtil.getNssmfHost(any(EsrInfo.class))).thenReturn(nssmf);

        when(tokenResponse.getEntity()).thenReturn(tokenEntity);
        when(tokenResponse.getStatusLine()).thenReturn(statusLine);
        when(tokenEntity.getContent()).thenReturn(tokenStream);

        when(commonResponse.getEntity()).thenReturn(commonEntity);
        when(commonResponse.getStatusLine()).thenReturn(statusLine);
        when(commonEntity.getContent()).thenReturn(postStream);

        Answer<HttpResponse> answer = new Answer<HttpResponse>() {

            public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
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
            }
        };
        doAnswer(answer).when(httpClient).execute(any(HttpRequestBase.class));
    }

    // @Test
    // public void testNssiAllocate() throws Exception {
    // NssmfInfo nssmf = new NssmfInfo();
    // nssmf.setUserName("nssmf-user");
    // nssmf.setPassword("nssmf-pass");
    // nssmf.setPort("8080");
    // nssmf.setIpAddress("127.0.0.1");
    //
    // NssiResponse nssiRes = new NssiResponse();
    // nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");
    // nssiRes.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
    //
    // TokenResponse token = new TokenResponse();
    // token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
    // token.setExpires(1800);
    //
    // postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
    // tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));
    //
    // createCommonMock(200, nssmf);
    // // assertEquals(prettyPrint(createAllocateNssi()), ALLOCATE);
    // ResponseEntity res = nssmfRest.createAllocateNssi(createAllocateNssi());
    // assertNotNull(res);
    // assertNotNull(res.getBody());
    // NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
    // assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
    // assertEquals(allRes.getNssiId(), "NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
    // }
    //
    // @Test
    // public void testNssiDeAllocate() throws Exception {
    // NssmfInfo nssmf = new NssmfInfo();
    // nssmf.setUserName("nssmf-user");
    // nssmf.setPassword("nssmf-pass");
    // nssmf.setPort("8080");
    // nssmf.setIpAddress("127.0.0.1");
    //
    // NssiResponse nssiRes = new NssiResponse();
    // nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");
    //
    // TokenResponse token = new TokenResponse();
    // token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
    // token.setExpires(1800);
    //
    // postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
    // tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));
    //
    // createCommonMock(200, nssmf);
    // ResponseEntity res = nssmfRest.deAllocateNssi(deAllocateNssi(), "ab9af40f13f721b5f13539d87484098");
    // assertNotNull(res);
    // assertNotNull(res.getBody());
    // NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
    // assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
    // }
    //
    // @Test
    // public void testNssiActivate() throws Exception {
    // NssmfInfo nssmf = new NssmfInfo();
    // nssmf.setUserName("nssmf-user");
    // nssmf.setPassword("nssmf-pass");
    // nssmf.setPort("8080");
    // nssmf.setIpAddress("127.0.0.1");
    //
    // NssiResponse nssiRes = new NssiResponse();
    // nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");
    //
    // TokenResponse token = new TokenResponse();
    // token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
    // token.setExpires(1800);
    //
    // postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
    // tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));
    //
    // createCommonMock(200, nssmf);
    // ResponseEntity res = nssmfRest.activateNssi(activateNssi(), "001-100001");
    // assertNotNull(res);
    // assertNotNull(res.getBody());
    // NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
    // assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
    // }
    //
    // @Test
    // public void testNssiDeActivate() throws Exception {
    // NssmfInfo nssmf = new NssmfInfo();
    // nssmf.setUserName("nssmf-user");
    // nssmf.setPassword("nssmf-pass");
    // nssmf.setPort("8080");
    // nssmf.setIpAddress("127.0.0.1");
    //
    // NssiResponse nssiRes = new NssiResponse();
    // nssiRes.setJobId("4b45d919816ccaa2b762df5120f72067");
    //
    // TokenResponse token = new TokenResponse();
    // token.setAccessToken("7512eb3feb5249eca5ddd742fedddd39");
    // token.setExpires(1800);
    //
    // postStream = new ByteArrayInputStream(marshal(nssiRes).getBytes(UTF_8));
    // tokenStream = new ByteArrayInputStream(marshal(token).getBytes(UTF_8));
    //
    // createCommonMock(200, nssmf);
    // ResponseEntity res = nssmfRest.deactivateNssi(deActivateNssi(), "001-100001");
    // assertNotNull(res);
    // assertNotNull(res.getBody());
    // NssiResponse allRes = unMarshal(res.getBody().toString(), NssiResponse.class);
    // assertEquals(allRes.getJobId(), "4b45d919816ccaa2b762df5120f72067");
    // }
    //
    @Test
    public void testAllocateJsonSerDeSer() throws Exception {
        assertEquals(marshal(allocateNssi()), ALLOCATE);
        NssiAllocateRequest all = unMarshal(ALLOCATE, NssiAllocateRequest.class);
        assertNotNull(all);
        assertNotNull(all.getAllocateCnNssi());
        assertNotNull(all.getAllocateCnNssi().getSliceProfile());
        assertEquals(all.getAllocateCnNssi().getSliceProfile().getResourceSharingLevel(), NON_SHARED);
        assertNotNull(all.getAllocateCnNssi().getSliceProfile().getPerfReq());
        assertNotNull(all.getAllocateCnNssi().getSliceProfile().getPerfReq().getPerfReqEmbbList());
        PerfReqEmbb embb =
                all.getAllocateCnNssi().getSliceProfile().getPerfReq().getPerfReqEmbbList().iterator().next();
        assertNotNull(embb);
        assertEquals(embb.getActivityFactor(), 50);
    }

    public NssiAllocateRequest allocateNssi() throws Exception {
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
        EsrInfo esrInfo = new EsrInfo();
        esrInfo.setVendor("huawei");
        esrInfo.setNetworkType(CORE);
        NssiAllocateRequest allocate = new NssiAllocateRequest();
        allocate.setAllocateCnNssi(cnNssi);
        allocate.setEsrInfo(esrInfo);
        return allocate;
    }

    //
    // public NssiDeAllocateRequest deAllocateNssi() throws Exception {
    // DeAllocateNssi deAllocateNssi = new DeAllocateNssi();
    // deAllocateNssi.setTerminateNssiOption(0);
    // List<String> snssai = new LinkedList<>();
    // snssai.add("001-100001");
    // deAllocateNssi.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
    // deAllocateNssi.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
    // deAllocateNssi.setScriptName("CN1");
    // deAllocateNssi.setSnssaiList(snssai);
    // EsrInfo esrInfo = new EsrInfo();
    // esrInfo.setVendor("huawei");
    // esrInfo.setNetworkType(CORE);
    // NssiDeAllocateRequest deAllocate = new NssiDeAllocateRequest();
    // deAllocate.setDeAllocateNssi(deAllocateNssi);
    // deAllocate.setEsrInfo(esrInfo);
    // return deAllocate;
    // }
    //
    // public NssiActDeActRequest activateNssi() throws Exception {
    // EsrInfo esrInfo = new EsrInfo();
    // esrInfo.setVendor("huawei");
    // esrInfo.setNetworkType(CORE);
    // ActDeActNssi act = new ActDeActNssi();
    // act.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
    // act.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
    // NssiActDeActRequest actReq = new NssiActDeActRequest();
    // actReq.setActDeActNssi(act);
    // actReq.setEsrInfo(esrInfo);
    // return actReq;
    // }
    //
    // public NssiActDeActRequest deActivateNssi() throws Exception {
    // EsrInfo esrInfo = new EsrInfo();
    // esrInfo.setVendor("huawei");
    // esrInfo.setNetworkType(CORE);
    // ActDeActNssi deAct = new ActDeActNssi();
    // deAct.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
    // deAct.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
    // NssiActDeActRequest deActReq = new NssiActDeActRequest();
    // deActReq.setActDeActNssi(deAct);
    // deActReq.setEsrInfo(esrInfo);
    // return deActReq;
    // }
    //
    public String queryJobStatusNssi() throws Exception {
        EsrInfo esrInfo = new EsrInfo();
        esrInfo.setVendor("huawei");
        esrInfo.setNetworkType(CORE);

        JobStatusRequest jobStatus = new JobStatusRequest();
        jobStatus.setEsrInfo(esrInfo);
        jobStatus.setNsiId("NSI-M-001-HDBNJ-NSMF-01-A-ZX");
        jobStatus.setNssiId("NSSI-C-001-HDBNJ-NSSMF-01-A-ZX");
        return marshal(jobStatus);
    }
}
