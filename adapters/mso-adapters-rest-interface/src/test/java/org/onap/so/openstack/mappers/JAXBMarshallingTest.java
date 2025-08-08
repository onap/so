/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.openstack.mappers;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;


public class JAXBMarshallingTest {


    @Test
    public void xmlUnMarshalTest() throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(CreateVfModuleRequest.class);

        CreateVfModuleRequest request = (CreateVfModuleRequest) context.createUnmarshaller().unmarshal(
                Files.newBufferedReader(Paths.get("src/test/resources/createVfModuleRequest-with-params.xml")));

        assertEquals("ubuntu-16-04-cloud-amd64", request.getVfModuleParams().get("vcpe_image_name"));
        assertEquals("10.2.0.0/24", request.getVfModuleParams().get("cpe_public_net_cidr"));
        assertEquals("", request.getVfModuleParams().get("workload_context"));
        assertEquals("[\"a\",\"b\",\"c\"]", request.getVfModuleParams().get("raw-json-param"));
    }

    @Test
    public void xmlMarshalTest() throws IOException {

        CreateVfModuleRequest request = new CreateVfModuleRequest();
        request.getVfModuleParams().put("test-null", null);
        request.getVfModuleParams().put("vcpe_image_name", "ubuntu-16-04-cloud-amd64");
        request.getVfModuleParams().put("test-empty", "");
        request.getVfModuleParams().put("test array", Arrays.asList("a", "b", "c"));
        request.getVfModuleParams().put("test map", Collections.singletonMap("d", "e"));
        request.getVfModuleParams().put("marshalling error", new ArrayList());

        assertEquals("documents should be equal",
                new String(Files
                        .readAllBytes(Paths.get("src/test/resources/VfRequest-marshalled-with-complex-object.xml")))
                                .replaceAll("\\R", "\n"),
                request.toXmlString());

    }

}
