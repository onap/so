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

package org.onap.so.client.orchestration;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.namingservice.model.Deleteelement;
import org.onap.namingservice.model.Element;
import org.onap.namingservice.model.NameGenDeleteRequest;
import org.onap.namingservice.model.NameGenDeleteResponse;
import org.onap.namingservice.model.NameGenRequest;
import org.onap.namingservice.model.NameGenResponse;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.client.namingservice.NamingClient;
import org.onap.so.client.namingservice.NamingRequestObjectBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NamingServiceResourcesTest extends TestDataSetup {
    @InjectMocks
    private NamingServiceResources namingServiceResources = new NamingServiceResources();

    private InstanceGroup instanceGroup;

    @Mock
    protected NamingRequestObjectBuilder MOCK_namingRequestObjectBuilder;

    @Mock
    protected NamingClient MOCK_namingClient;

    @Before
    public void before() {
        instanceGroup = buildInstanceGroup();
    }

    @Test
    public void generateInstanceGroupNameTest() throws Exception {
        NameGenResponse name = new NameGenResponse();
        ResponseEntity<NameGenResponse> resp = new ResponseEntity<>(name, null, HttpStatus.OK);
        Element element = new Element();

        NameGenRequest req = new NameGenRequest();
        doReturn(element).when(MOCK_namingRequestObjectBuilder).elementMapper(isA(String.class), isA(String.class),
                isA(String.class), isA(String.class), isA(String.class));
        doReturn("generatedInstanceGroupName").when(MOCK_namingClient).postNameGenRequest(isA(NameGenRequest.class));
        doReturn(req).when(MOCK_namingRequestObjectBuilder).nameGenRequestMapper(isA(List.class));

        String generatedName =
                namingServiceResources.generateInstanceGroupName(instanceGroup, "policyInstanceName", "nfNamingCode");

        verify(MOCK_namingClient, times(1)).postNameGenRequest(any(NameGenRequest.class));
        assertEquals(generatedName, "generatedInstanceGroupName");
    }

    @Test
    public void deleteInstanceGroupNameTest() throws Exception {
        NameGenDeleteResponse name = new NameGenDeleteResponse();
        ResponseEntity<NameGenDeleteResponse> resp = new ResponseEntity<>(name, null, HttpStatus.OK);
        Deleteelement deleteElement = new Deleteelement();
        deleteElement.setExternalKey(instanceGroup.getId());
        NameGenDeleteRequest req = new NameGenDeleteRequest();
        doReturn(deleteElement).when(MOCK_namingRequestObjectBuilder).deleteElementMapper(isA(String.class));
        doReturn("").when(MOCK_namingClient).deleteNameGenRequest(isA(NameGenDeleteRequest.class));
        doReturn(req).when(MOCK_namingRequestObjectBuilder).nameGenDeleteRequestMapper(isA(List.class));

        namingServiceResources.deleteInstanceGroupName(instanceGroup);

        verify(MOCK_namingClient, times(1)).deleteNameGenRequest(any(NameGenDeleteRequest.class));

    }


}
