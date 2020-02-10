/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.onap.namingservice.model.Element;
import org.onap.namingservice.model.Deleteelement;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.namingservice.NamingClient;
import org.onap.so.client.namingservice.NamingRequestObject;
import org.onap.so.client.namingservice.NamingRequestObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NamingServiceResources {
    private static final String NAMING_TYPE = "instanceGroup";

    @Autowired
    private NamingClient namingClient;

    @Autowired
    private NamingRequestObjectBuilder namingRequestObjectBuilder;

    public String generateInstanceGroupName(InstanceGroup instanceGroup, String policyInstanceName, String nfNamingCode)
            throws BadResponseException, IOException {
        Element element = namingRequestObjectBuilder.elementMapper(instanceGroup.getId(), policyInstanceName,
                NAMING_TYPE, nfNamingCode, instanceGroup.getInstanceGroupName());
        List<Element> elements = new ArrayList<>();
        elements.add(element);
        return (namingClient.postNameGenRequest(namingRequestObjectBuilder.nameGenRequestMapper(elements)));
    }

    public String deleteInstanceGroupName(InstanceGroup instanceGroup) throws BadResponseException, IOException {
        Deleteelement deleteElement = namingRequestObjectBuilder.deleteElementMapper(instanceGroup.getId());
        List<Deleteelement> deleteElements = new ArrayList<>();
        deleteElements.add(deleteElement);
        return (namingClient
                .deleteNameGenRequest(namingRequestObjectBuilder.nameGenDeleteRequestMapper(deleteElements)));
    }

    public String generateServiceInstanceName(NamingRequestObject namingRequestObject)
            throws BadResponseException, IOException {
        HashMap<String, String> nsRequestObject = namingRequestObject.getNamingRequestObjectMap();
        Element element = new Element();
        nsRequestObject.forEach(element::put);
        List<Element> elements = new ArrayList<>();
        elements.add(element);
        return (namingClient.postNameGenRequest(namingRequestObjectBuilder.nameGenRequestMapper(elements)));
    }

    public String deleteServiceInstanceName(NamingRequestObject namingRequestObject)
            throws BadResponseException, IOException {
        HashMap<String, String> nsRequestObject = namingRequestObject.getNamingRequestObjectMap();
        Deleteelement delElement = new Deleteelement();
        nsRequestObject.forEach((k, v) -> delElement.setExternalKey(v));
        List<Deleteelement> delElements = new ArrayList<>();
        delElements.add(delElement);
        return (namingClient.deleteNameGenRequest(namingRequestObjectBuilder.nameGenDeleteRequestMapper(delElements)));
    }

}
