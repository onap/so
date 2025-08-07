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
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.namingservice.model.Deleteelement;
import org.onap.namingservice.model.Element;
import org.onap.namingservice.model.NameGenDeleteRequest;
import org.onap.namingservice.model.NameGenRequest;

public class NamingRequestObjectBuilderTest {

    private NamingRequestObjectBuilder mapper = new NamingRequestObjectBuilder();
    private String instanceGroupId = "95cbbe59-1017-4c13-b4e8-d824e54def3e";
    private String policyInstanceName = "MSO_Policy.Config_MS_VNFInstanceGroup";
    private String namingType = "InstanceGroup";
    private String nfNamingCode = "NamingCode";
    private String instanceGroupName = "InstanceGroupName";

    @Test
    public void elementMapperTest() {
        // Expected element
        Element expected = new Element();
        expected.put("external-key", instanceGroupId);
        expected.put("policy-instance-name", policyInstanceName);
        expected.put("naming-type", namingType);
        expected.put("resource-name", instanceGroupName);
        expected.put("nf-naming-code", nfNamingCode);

        // Actual element
        Element actual =
                mapper.elementMapper(instanceGroupId, policyInstanceName, namingType, nfNamingCode, instanceGroupName);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void deleteElementMapperTest() {
        // Expected Deleteelement
        Deleteelement expected = new Deleteelement();
        expected.setExternalKey(instanceGroupId);

        // Actual Deleteelement
        Deleteelement actual = mapper.deleteElementMapper(instanceGroupId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void nameGenRequestMapper() {
        // Expected NameGenRequest
        NameGenRequest expected = new NameGenRequest();
        List<Element> elements = new ArrayList<>();
        Element element = new Element();
        element.setExternalKey(instanceGroupId);
        element.setPolicyInstanceName(policyInstanceName);
        element.setNamingType(namingType);
        element.setResourceName(instanceGroupName);
        element.setNamingIngredientsZeroOrMore(nfNamingCode);
        elements.add(element);
        expected.setElements(elements);

        // Actual NameGenRequest
        NameGenRequest actual = mapper.nameGenRequestMapper(elements);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void nameGenDeleteRequestMapper() {
        // Expected NameGenDeleteRequest
        NameGenDeleteRequest expected = new NameGenDeleteRequest();
        List<Deleteelement> deleteElements = new ArrayList<>();
        Deleteelement deleteElement = new Deleteelement();
        deleteElement.setExternalKey(instanceGroupId);
        deleteElements.add(deleteElement);
        expected.setElements(deleteElements);

        // Actual NameGenDeleteRequest
        NameGenDeleteRequest actual = mapper.nameGenDeleteRequestMapper(deleteElements);

        assertThat(actual, sameBeanAs(expected));
    }
}
