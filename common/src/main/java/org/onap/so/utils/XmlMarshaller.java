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

package org.onap.so.utils;


import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.onap.so.exceptions.MarshallerException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XmlMarshaller {

    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL, XmlMarshaller.class);

    public static String marshal(Object object) throws MarshallerException {

        StringWriter stringWriter = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(object, stringWriter);
        } catch (JAXBException e) {
            logger.error(MessageEnum.GENERAL_EXCEPTION, "", "", "", MsoLogger.ErrorCode.SchemaError, e.getMessage(), e);
            throw new MarshallerException(e.getMessage(), MsoLogger.ErrorCode.SchemaError.getValue(), e);
        }

        return stringWriter.toString();
    }

    public static Object unMarshal(String input, Object object) throws MarshallerException {

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setNamespaceAware(true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputSource inputSource = new InputSource(new StringReader(input));
            SAXSource source = new SAXSource(xmlReader, inputSource);
            object = jaxbUnmarshaller.unmarshal(source, object.getClass()).getValue();
        } catch (Exception e) {
            logger.error(MessageEnum.GENERAL_EXCEPTION, "", "", "", MsoLogger.ErrorCode.SchemaError, e.getMessage(), e);
            throw new MarshallerException(e.getMessage(), MsoLogger.ErrorCode.SchemaError.getValue(), e);
        }

        return object;
    }

}
