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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.properties.MsoJavaProperties;

public class VnfMsoInfraRequestTest {
    VnfMsoInfraRequest request = new VnfMsoInfraRequest("29919020");

    @Test(expected = Exception.class)
    public void parseTest() throws ValidationException {
        String reqXML = "<vnf-request><request-info> <request-id>29993</request-id><request-status>COMPLETE</request-status></request-info></vnf-request>";
        request.parse(reqXML, "v1", new MsoJavaProperties());
    }

}
