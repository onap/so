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

package org.openecomp.mso.apihandlerinfra.utils;

import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequest;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfRequest;
import org.openecomp.mso.apihandlerinfra.volumebeans.VolumeRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtils {

    private static final String PRETTY_PRINTING = Marshaller.JAXB_FORMATTED_OUTPUT;

    private XmlUtils() {
    }

    public static String domToString(Document doc) throws TransformerException {
        if (doc == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.STANDALONE, "yes");
        NodeList nl = doc.getDocumentElement().getChildNodes();
        DOMSource source = null;
        for (int x = 0; x < nl.getLength(); x++) {
            Node e = nl.item(x);
            if (e instanceof Element) {
                source = new DOMSource(e);
                break;
            }
        }
        if (source != null) {
            t.transform(source, sr);

            return sw.toString();
        }
        return null;
    }

    public static String marshallToString(Object request) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(request.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(PRETTY_PRINTING, true);
        jaxbMarshaller.marshal(request, stringWriter);

        return stringWriter.toString();
    }

    public static VnfRequest unmarshallVnfRequest(String request) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(VnfRequest.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputSource inputSource = new InputSource(new StringReader(request));
        SAXSource source = new SAXSource(inputSource);

        return unmarshaller.unmarshal(source, VnfRequest.class).getValue();
    }

    public static VolumeRequest unmarshallVolumeRequest(String request) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(VnfRequest.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputSource inputSource = new InputSource(new StringReader(request));
        SAXSource source = new SAXSource(inputSource);

        return unmarshaller.unmarshal(source, VolumeRequest.class).getValue();
    }

    public static NetworkRequest unmarshallNetworkRequest(String request) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(NetworkRequest.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputSource inputSource = new InputSource(new StringReader(request));
        SAXSource source = new SAXSource(inputSource);

        return unmarshaller.unmarshal(source, NetworkRequest.class).getValue();
    }
}
