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

package org.openecomp.mso.adapters.sdnc;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class ObjectFactoryTest {

    private Marshaller jaxbMarshaller;
    private Unmarshaller jaxbUnmarshaller;

    /**
     * Test method for {@link org.openecomp.mso.adapters.sdnc.ObjectFactory#createRequestHeader()}.
     */
    @Test
    public final void testCreateRequestHeader() {
        ObjectFactory of = new ObjectFactory();
        RequestHeader rh = of.createRequestHeader();
        rh.setCallbackUrl("callback");
        rh.setMsoAction("action");
        rh.setRequestId("reqid");
        rh.setSvcAction("svcAction");
        rh.setSvcInstanceId("svcId");
        rh.setSvcOperation("op");

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RequestHeader.class);
            jaxbMarshaller = jaxbContext.createMarshaller();

            JAXBContext jaxbContext2 = JAXBContext.newInstance(RequestHeader.class);
            jaxbUnmarshaller = jaxbContext2.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
            return;
        }

        StringWriter writer = new StringWriter();
        try {
            jaxbMarshaller.marshal(rh, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
        }
        String marshalled = writer.toString();
        assert (marshalled.contains("<RequestId>reqid</RequestId>"));

        InputStream inputStream = new ByteArrayInputStream(marshalled.getBytes(Charset.forName("UTF-8")));
        try {
            RequestHeader res2 = (RequestHeader) jaxbUnmarshaller.unmarshal(inputStream);
            assert (res2.getCallbackUrl().equals("callback"));
            assert (res2.getMsoAction().equals("action"));
            assert (res2.getSvcOperation().equals("op"));
        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test method for {@link org.openecomp.mso.adapters.sdnc.ObjectFactory#createSDNCAdapterResponse()}.
     */
    @Test
    public final void testCreateSDNCAdapterResponse() {
        ObjectFactory of = new ObjectFactory();
        SDNCAdapterResponse ar = of.createSDNCAdapterResponse();
        assert (ar != null);
    }
}
