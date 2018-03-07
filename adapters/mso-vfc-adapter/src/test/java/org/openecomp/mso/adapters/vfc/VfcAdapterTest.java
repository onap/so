/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vfc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.adapters.vfc.constant.CommonConstant;
import org.openecomp.mso.adapters.vfc.constant.HttpCode;
import org.openecomp.mso.adapters.vfc.model.RestfulResponse;
import org.openecomp.mso.adapters.vfc.util.RestfulUtil;
import org.openecomp.mso.adapters.vfc.util.ValidateUtil;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

import mockit.Mock;
import mockit.MockUp;

/**
 * VF-C adapter UT <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-31
 */
public class VfcAdapterTest {

    private VfcAdapterRest vfcAdapter = new VfcAdapterRest();

    /**
     * File path
     */
    private static final String FILE_PATH = "src/test/resources/json/";

    /**
     * mock get request body <br>
     * 
     * @param fileName
     * @return
     * @since ONAP Amsterdam Release
     */
    public String getRequestBody(String fileName) {
        return getJsonString(fileName);
    }

    /**
     * Mock the request body form a file <br>
     * 
     * @since ONAP Amsterdam Release
     */
    private void mockRestfulUtil() {
        new MockUp<RestfulUtil>() {

            /**
             * mock get send method <br>
             * 
             * @param url
             * @param methodType
             * @param content
             * @return
             * @since ONAP Amsterdam Release
             */
            @Mock
            public RestfulResponse send(String url, String methodType, String content) {
                if(url.equals(CommonConstant.NFVO_CREATE_URL) && methodType.equals(CommonConstant.MethodType.POST)) {
                    return getResponse("createNsRsp.json");
                } else if(url.contains("instantiate") && methodType.equals(CommonConstant.MethodType.POST)) {
                    return getResponse("instantiateNsRsp.json");
                } else if(methodType.equals(CommonConstant.MethodType.DELETE)) {
                    return getResponse(null);
                } else if(url.contains("terminate") && methodType.equals(CommonConstant.MethodType.POST)) {
                    return getResponse("terminateNsRsp.json");
                } else if(url.contains("/api/nslcm/v1/jobs") && methodType.equals(CommonConstant.MethodType.GET)) {
                    return getResponse("queryJobRsp.json");
                } else {
                    return null;
                }
            }
        };
    }

    /**
     * Mock the request body form a file <br>
     * 
     * @param fileName
     * @since ONAP Amsterdam Release
     */
    private void mockRequestDatabase() {
        new MockUp<RequestsDatabase>() {

            /**
             * mock get resource operation status <br>
             * 
             * @param request
             * @return
             * @since ONAP Amsterdam Release
             */
            @Mock
            public ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId,
                    String resourceTemplateUUID) {
                ResourceOperationStatus resStatus = new ResourceOperationStatus();
                resStatus.setServiceId("111");
                resStatus.setOperationId("111");
                return resStatus;
            }

            /**
             * Mock update Res Oper Status <br>
             * 
             * @param operStatus
             * @since ONAP Amsterdam Release
             */
            @Mock
            public void updateResOperStatus(ResourceOperationStatus operStatus) {

            }
        };
    }

    /**
     * Before executing UT, start mock requst database <br>
     * 
     * @since ONAP Amsterdam Release
     */
    @Before
    public void start() {
        mockRequestDatabase();
        mockRestfulUtil();
    }

    /**
     * After executing UT, close session<br/>
     * 
     * @since ONAP Amsterdam Release
     */
    @After
    public void stop() {

    }

    @Test
    public void createTest() {
        // get request
        String createReq = getRequestBody(FILE_PATH + "createNsReq.json");
        vfcAdapter.createNfvoNs(createReq);
    }

    @Test
    public void deleteTest() {
        // get request
        String req = getRequestBody(FILE_PATH + "deleteNsReq.json");
        vfcAdapter.deleteNfvoNs(req, "9b9f02c0-298b-458a-bc9c-be3692e4f354");
    }

    @Test
    public void instantiateTest() {
        // get request
        String req = getRequestBody(FILE_PATH + "instantiateNsReq.json");
        vfcAdapter.instantiateNfvoNs(req, "9b9f02c0-298b-458a-bc9c-be3692e4f354");
    }

    @Test
    public void terminateTest() {
        String req = getRequestBody(FILE_PATH + "terminateNsReq.json");
        vfcAdapter.deleteNfvoNs(req, "9b9f02c0-298b-458a-bc9c-be3692e4f354");
    }

    @Test
    public void queryJobTest() {
        String req = getRequestBody(FILE_PATH + "queryJobReq.json");
        vfcAdapter.queryNfvoJobStatus(req, "1");
    }

    /**
     * Get json string from file.<br/>
     * 
     * @param file the path of file
     * @return json string
     * @throws IOException when fail to read
     * @since ONAP Amsterdam Release 2017-9-6
     */
    @SuppressWarnings("deprecation")
    private String getJsonString(final String file) {
        if(ValidateUtil.isStrEmpty(file)) {
            return "";
        }

        String json = null;
        try {
            FileInputStream fileStream = new FileInputStream(new File(file));
            json = IOUtils.toString(fileStream);
        } catch(Exception e) {
            Assert.fail(e.getMessage());
        }
        return json;
    }

    /**
     * get the response from file <br>
     * 
     * @param fileName
     * @return
     * @since ONAP Amsterdam Release
     */
    private RestfulResponse getResponse(String fileName) {
        RestfulResponse responseSuccess = new RestfulResponse();
        responseSuccess.setStatus(HttpCode.RESPOND_OK);
        if(null != fileName) {
            String jsonStr = getJsonString(FILE_PATH + fileName);
            responseSuccess.setResponseContent(jsonStr);
        }
        return responseSuccess;
    }
}
