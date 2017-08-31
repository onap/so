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


import java.io.IOException;

import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.mso.adapters.vfc.util.RestfulUtil;
import org.openecomp.mso.adapters.vfc.util.ValidateUtil;
import org.openecomp.mso.db.catalog.CatalogDatabase;

import mockit.MockUp;

/**
 * VF-C adapter UT
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Amsterdam Release  2017-08-31
 */
public class VfcAdapterTest {

    @Mock
    private static CatalogDatabase db;

    /**
     * File path
     */
    private static final String FILE_PATH = "src/test/resources/json/";

    @Test
    public void createTest () {
//        // get request
//        mockGetRequestBody(FILE_PATH + "createNfvoNsReq.json");
//        // get service template
//        ServiceTemplate svcTmpl = new ServiceTemplate();
//        svcTmpl.setId("id");
//        svcTmpl.setServiceTemplateId("svcTmplId");
//        new MockUp<CatalogProxyImpl>() {
//            @Mock
//            public ServiceTemplate getSvcTmplByNodeType(String nodeType, String domainHost){
//                return svcTmpl;
//            }
//        };
//        // get response
//        RestfulResponse restRsp = new RestfulResponse();
//        restRsp.setStatus(HttpStatus.SC_OK);
//        restRsp.setResponseJson(getJsonString(FILE_PATH + "createNfvoNsRsp.json"));
//        mockGetRestfulRsp(restRsp);
//        // insert data
//        new MockUp<ServiceSegmentDaoImpl>() {
//            @Mock
//            public void insertSegment(ServiceSegmentModel serviceSegment) {
//                // do nothing
//            }
//            @Mock
//            public void insertSegmentOper(ServiceSegmentOperation svcSegmentOper) {
//                // do nothing
//            }
//        };
//        Response rsp = impl.createNfvoNs(servletReq);
//        JSONObject obj = JSONObject.fromObject(rsp.getEntity());
//        Assert.assertEquals(null, "1", obj.getString("nsInstanceId"));
    }

    @Test
    public void deleteTest () {
        
    }

    @Test
    public void instantiateTest () {

    }

    @Test
    public void terminateTest () {
        
    }

    @Test
    public void queryJobTest () {

    }
    
    /**
     * Mock to get request body.<br/>
     * 
     * @param file json file path.
     * @since GSO 0.5
     */
    private void mockGetRequestBody(final String file) {
        new MockUp<RestfulUtil>() {

//            @Mock
//            public String getRequestBody(HttpServletRequest request) {
//                return getJsonString(file);
//            }
        };
    }
    
    /**
     * Get json string from file.<br/>
     * 
     * @param file the path of file
     * @return json string
     * @throws IOException when fail to read
     * @since GSO 0.5
     */
    private String getJsonString(final String file) {
        if(ValidateUtil.isStrEmpty(file)) {
            return "";
        }

        String json = null;
//        try {
//            FileInputStream fileStream = new FileInputStream(new File(file));
//            json = IOUtils.toString(fileStream);
//        } catch(Exception e) {
//            Assert.fail(e.getMessage());
//        }

        return json;
    }
}
