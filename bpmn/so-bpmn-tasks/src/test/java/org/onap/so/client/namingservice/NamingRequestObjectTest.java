/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.HashMap;
import org.junit.Test;
import org.onap.namingservice.model.Element;

public class NamingRequestObjectTest {

    private String externalKey = "95cbbe59-1017-4c13-b4e8-d824e54def3e";
    private String policyInstanceName = "MSO_Policy.Config_MS_VNFInstanceGroup";
    private String namingType = "Service";
    private String nfNamingCode = "NamingCode";
    private String resourceName = "resourceName";
    private String serviceModelName = "serviceModelName";
    private String modelVersion = "modelVersion";

    @Test
    public void namingRequestObjectTest() {

        Element expected = new Element();
        expected.put(NamingServiceConstants.NS_EXTERNAL_KEY, externalKey);
        expected.put(NamingServiceConstants.NS_POLICY_INSTANCE_NAME, policyInstanceName);
        expected.put(NamingServiceConstants.NS_NAMING_TYPE, namingType);
        expected.put(NamingServiceConstants.NS_RESOURCE_NAME, resourceName);
        expected.put(NamingServiceConstants.NS_NF_NAMING_CODE, nfNamingCode);

        NamingRequestObject namingRequestObject = new NamingRequestObject();
        namingRequestObject.setExternalKeyValue(externalKey);
        namingRequestObject.setPolicyInstanceNameValue(policyInstanceName);
        namingRequestObject.setNamingTypeValue(namingType);
        namingRequestObject.setNfNamingCodeValue(nfNamingCode);
        namingRequestObject.setResourceNameValue(resourceName);

        HashMap<String, String> actual = namingRequestObject.getNamingRequestObjectMap();
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void namingWanTransportRequestObjectTest() {

        Element expected = new Element();
        expected.put(NamingServiceConstants.NS_EXTERNAL_KEY, externalKey);
        expected.put(NamingServiceConstants.NS_POLICY_INSTANCE_NAME, policyInstanceName);
        expected.put(NamingServiceConstants.NS_NAMING_TYPE, namingType);
        expected.put(NamingServiceConstants.NS_RESOURCE_NAME, resourceName);
        expected.put(NamingServiceConstants.NS_SERVICE_MODEL_NAME, serviceModelName);
        expected.put(NamingServiceConstants.NS_MODEL_VERSION, modelVersion);

        NamingRequestObject namingRequestObject = new NamingRequestObject();
        namingRequestObject.setExternalKeyValue(externalKey);
        namingRequestObject.setPolicyInstanceNameValue(policyInstanceName);
        namingRequestObject.setNamingTypeValue(namingType);
        namingRequestObject.setResourceNameValue(resourceName);
        namingRequestObject.setModelVersionValue(modelVersion);
        namingRequestObject.setServiceModelNameValue(serviceModelName);

        HashMap<String, String> actual = namingRequestObject.getNamingRequestObjectMap();
        assertThat(actual, sameBeanAs(expected));
    }
}
