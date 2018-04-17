/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.sdnc.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.logger.MsoLogger;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SDNCAdapterRestImpl.class)
public class SDNCAdapterRestImplTest {

    SDNCAdapterRestImpl test;

    @Before
    public void init(){

        test = new SDNCAdapterRestImpl();
    }

   @Test
   public void testSDNCAdapterRestImpl() {

       HealthCheckUtils hCU = PowerMockito.mock(HealthCheckUtils.class);
        try {
            PowerMockito.whenNew(HealthCheckUtils.class).withNoArguments().thenReturn(hCU);
            when(hCU.siteStatusCheck(any(MsoLogger.class))).thenReturn(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

       String reqXML = "<xml>test</xml>";

       Response response = test.MSORequest(reqXML);
       assertEquals(400,response.getStatus());

       response = test.healthcheck("1a25a7c0-4c91-4f74-9a78-8c11b7a57f1a");
       assertEquals(503,response.getStatus());

       response = test.globalHealthcheck(true);
       assertEquals(503,response.getStatus());

       response = test.nodeHealthcheck();
       assertEquals(503,response.getStatus());

   }

}

