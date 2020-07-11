/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.sdnc.impl;


import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    private Utils() {}

    public static String genSdncReq(Document reqDoc, RequestTunables rt) {
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // NewDoc for output
            // Root
            Document newdoc = db.newDocument();
            Element root = newdoc.createElementNS(rt.getNamespace(), "input");
            newdoc.appendChild(root);

            // Header
            Element hdr = newdoc.createElement(rt.getHeaderName());
            root.appendChild(hdr);

            String elemData = rt.getReqId();
            Element hdrChild;
            if (elemData != null && elemData.length() > 0) {
                hdrChild = newdoc.createElement("svc-request-id");
                hdrChild.appendChild(newdoc.createTextNode(elemData));
                hdr.appendChild(hdrChild);
            }

            elemData = rt.getAction();
            if (elemData != null && elemData.length() > 0) {
                hdrChild = newdoc.createElement("svc-action");
                hdrChild.appendChild(newdoc.createTextNode(elemData));
                hdr.appendChild(hdrChild);
            }

            elemData = rt.getSdncaNotificationUrl();
            if (elemData != null && elemData.length() > 0) {
                hdrChild = newdoc.createElement("svc-notification-url");
                hdrChild.appendChild(newdoc.createTextNode(elemData));
                hdr.appendChild(hdrChild);
            }

            // RequestData
            NodeList nodes = reqDoc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                Node newNode = newdoc.importNode(n, true);
                root.appendChild(newNode);
            }

            String s = domToStr(newdoc);
            logger.debug("Formatted SdncReq:\n", s);
            return s;

        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_ERROR_CREATE_SDNC_REQUEST.toString(), "SDNC",
                    ErrorCode.BusinessProcessError.getValue(), "Exception in genSdncReq", e);
        }
        return null;
    }

    public static String genSdncPutReq(Document reqDoc, RequestTunables rt) {
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // NewDoc for output
            // Root
            Document newdoc = db.newDocument();

            // RequestData
            NodeList nodes = reqDoc.getDocumentElement().getChildNodes();

            Element root = newdoc.createElementNS(nodes.item(0).getNamespaceURI(), nodes.item(0).getNodeName());
            newdoc.appendChild(root);

            NodeList childNodes = nodes.item(0).getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node n = childNodes.item(i);
                Node newNode = newdoc.importNode(n, true);
                root.appendChild(newNode);
            }

            String s = domToStr(newdoc);
            logger.debug("Formatted SdncPutReq:\n {}", s);
            return s;

        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_ERROR_CREATE_SDNC_REQUEST.toString(), "SDNC",
                    ErrorCode.DataError.getValue(), "Exception in genSdncPutReq", e);
        }
        return null;
    }

    public static String genMsoFailResp(SDNCResponse resp) {
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // NewDoc for output
            // Root
            Document newdoc = db.newDocument();
            Element root = newdoc.createElement("output");
            newdoc.appendChild(root);

            Element elem1 = newdoc.createElement("svc-request-id");
            elem1.appendChild(newdoc.createTextNode(resp.getReqId()));
            root.appendChild(elem1);

            Element elem2 = newdoc.createElement("response-code");
            elem2.appendChild(newdoc.createTextNode(String.valueOf(resp.getRespCode())));
            root.appendChild(elem2);

            Element elem3 = newdoc.createElement("response-message");
            elem3.appendChild(newdoc.createTextNode(String.valueOf(resp.getRespMsg())));
            root.appendChild(elem3);

            String s = domToStr(newdoc);
            logger.debug("Formatted SdncReq: {}", s);
            return s;

        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_ERROR_CREATE_SDNC_RESPONSE.toString(), "SDNC",
                    ErrorCode.DataError.getValue(), "Exception in genMsoFailResp", e);
        }
        return null;
    }


    public static String domToStr(Document doc) {
        if (doc != null) {
            try {
                DOMSource ds = new DOMSource(doc);
                StringWriter sw = new StringWriter();
                StreamResult sr = new StreamResult(sw);
                TransformerFactory tf = TransformerFactory.newInstance();
                tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                Transformer t = tf.newTransformer();
                // t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");//<?xml version="1.0" encoding="UTF-8"?>
                t.transform(ds, sr);
                String s = sw.toString();

                // This is an awful fix for now but we don't want that xmlns="" to be generated
                s = s.replaceAll("xmlns=\"\"", "");
                return s;
            } catch (Exception e) {
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_ERROR_CONVERT_XML2STR.toString(),
                        ErrorCode.DataError.getValue(), "Exception - domToStr", e);
            }
        }
        return null;
    }
}
