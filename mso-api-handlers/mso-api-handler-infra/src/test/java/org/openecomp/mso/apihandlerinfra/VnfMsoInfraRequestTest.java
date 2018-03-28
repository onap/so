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

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.properties.MsoJavaProperties;

@RunWith(JMockit.class)
public class VnfMsoInfraRequestTest {

    VnfMsoInfraRequest request = new VnfMsoInfraRequest("29919020");
    private String reqXML = "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\"><request-info><request-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</request-id><action>CREATE_VF_MODULE</action><source>VID</source><!-- new 1610 field --><service-instance-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</service-instance-id></request-info><vnf-inputs><!-- not in use in 1610 --><vnf-name>vnfName</vnf-name><vnf-type>vnfType</vnf-type><vnf-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</vnf-id><volume-group-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</volume-group-id><vf-module-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</vf-module-id><vf-module-name>vfModuleName</vf-module-name><vf-module-model-name>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</vf-module-model-name><model-customization-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</model-customization-id><asdc-service-model-version>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</asdc-service-model-version><aic-cloud-region>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</aic-cloud-region><tenant-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</tenant-id><service-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</service-id><backout-on-failure>false</backout-on-failure><service-instance-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</service-instance-id></vnf-inputs><vnf-params><vnf-parameter-name>pName</vnf-parameter-name><vnf-parameter-value>pValue</vnf-parameter-value></vnf-params></vnf-request>";

    @Test
    public void parseTest() throws ValidationException {
        request.parse(reqXML, "v3", new MsoJavaProperties());
    }

    @Test
    public void createRequestRecord(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                    @Mocked Session session) throws ValidationException {

        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession(); result = session;
        }};

        request.parse(reqXML, "v3", new MsoJavaProperties());
        request.createRequestRecord(Status.COMPLETE);

    }

}
