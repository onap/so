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

package org.onap.so.adapters.sdnc.impl;

import static org.junit.Assert.assertEquals;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import org.onap.so.adapters.sdnc.SDNCAdapterRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class UtilsTest {

    @Test
    public final void testUnmarshal() {

        String expectedXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><feature-list xmlns=\"com:att:sdnctl:l3api\"><feature-type>FIREWALL-LITE</feature-type><feature-instance-id>mtjnj40evbc0eceb</feature-instance-id><feature-sub-type>SHARED</feature-sub-type><feature-instance-xpath>/restconf/config/Firewall-API:feature-model/feature-list/FIREWALL-LITE/mtjnj40evbc0eceb/</feature-instance-xpath>      </feature-list>";

        try {

            File file = new File("src/test/resources/sdncBpmnAdiodFirewallRequest.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(SDNCAdapterRequest.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SDNCAdapterRequest request = (SDNCAdapterRequest) jaxbUnmarshaller.unmarshal(file);

            RequestTunables rt = new RequestTunables("0460ba40-60c8-4b07-8878-c8e8d87cde04-1527983610512", "",
                    "/L3SDN-API:services/layer3-service-list/MIS%2F1806%2F25057%2FSW_INTERNET/service-data/feature-list/FIREWALL-LITE/",
                    "put");

            Node node = (Node) request.getRequestData();
            Document reqDoc = node.getOwnerDocument();
            String sdncReqBody = Utils.genSdncPutReq(reqDoc, rt);
            assertEquals(sdncReqBody.replaceAll("[\\t\\n\\r]+", ""), expectedXml);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

}
