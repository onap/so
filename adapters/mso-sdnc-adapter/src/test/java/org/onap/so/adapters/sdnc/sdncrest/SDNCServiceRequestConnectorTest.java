/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.sdnc.sdncrest;

import org.junit.Test;
import org.onap.so.adapters.sdnc.FileUtil;
import org.onap.so.adapters.sdncrest.SDNCResponseCommon;

import static org.junit.Assert.assertNotNull;

public class SDNCServiceRequestConnectorTest {

    @Test
    public void parseResponseContentTest() throws Exception {

        String content = FileUtil.readResourceFile("SdncServiceResponse.xml");
        SDNCResponseCommon responseCommon = SDNCServiceRequestConnector.parseResponseContent(content);

        assertNotNull(responseCommon);
    }
}
