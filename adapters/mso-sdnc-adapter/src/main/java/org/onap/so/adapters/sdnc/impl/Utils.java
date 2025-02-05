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
import java.time.Instant;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.XML;
import org.json.JSONObject;

public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);
    private static int MSOJsonIndentFactor = 3;

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
            logger.debug("Formatted SdncReq:\n{}", s);
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
            logger.debug("Formatted SdncPutReq:\n{}", s);
            return s;

        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_ERROR_CREATE_SDNC_REQUEST.toString(), "SDNC",
                    ErrorCode.DataError.getValue(), "Exception in genSdncPutReq", e);
        }
        return null;
    }

    public static Element genLcmCommonHeader(Document doc, String requestId) {
        Element commonHeader = doc.createElement("common-header");

        Element hdrChild;

        hdrChild = doc.createElement("api-ver");
        hdrChild.appendChild(doc.createTextNode(Constants.LCM_API_VER));
        commonHeader.appendChild(hdrChild);

        hdrChild = doc.createElement("flags");

        Element flagChild;

        flagChild = doc.createElement("force");
        flagChild.appendChild(doc.createTextNode("FALSE"));
        hdrChild.appendChild(flagChild);

        flagChild = doc.createElement("mode");
        flagChild.appendChild(doc.createTextNode("NORMAL"));
        hdrChild.appendChild(flagChild);

        flagChild = doc.createElement("ttl");
        flagChild.appendChild(doc.createTextNode(String.valueOf(Constants.LCM_FLAGS_TTL)));
        hdrChild.appendChild(flagChild);

        commonHeader.appendChild(hdrChild);

        hdrChild = doc.createElement("originator-id");
        hdrChild.appendChild(doc.createTextNode(Constants.LCM_ORIGINATOR_ID));
        commonHeader.appendChild(hdrChild);

        hdrChild = doc.createElement("request-id");
        hdrChild.appendChild(doc.createTextNode(requestId));
        commonHeader.appendChild(hdrChild);

        hdrChild = doc.createElement("sub-request-id");
        hdrChild.appendChild(doc.createTextNode(UUID.randomUUID().toString()));
        commonHeader.appendChild(hdrChild);

        hdrChild = doc.createElement("timestamp");
        hdrChild.appendChild(doc.createTextNode(Instant.now().toString()));
        commonHeader.appendChild(hdrChild);

        return commonHeader;
    }

    public static String genSdncLcmReq(Document reqDoc, RequestTunables rt) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document newdoc = db.newDocument();
            Element root = newdoc.createElementNS(rt.getNamespace(), "input");
            newdoc.appendChild(root);

            String elemData = rt.getReqId();
            if (elemData == null || elemData.length() == 0) {
                elemData = UUID.randomUUID().toString();
            }

            Element hdrChild;
            hdrChild = genLcmCommonHeader(newdoc, elemData);
            root.appendChild(hdrChild);

            elemData = rt.getAction();
            if (elemData != null && elemData.length() > 0) {
                hdrChild = newdoc.createElement("action");
                hdrChild.appendChild(newdoc.createTextNode(elemData));
                root.appendChild(hdrChild);
            }

            // RequestData
            NodeList nodes = reqDoc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                Node newNode = newdoc.importNode(n, true);
                root.appendChild(newNode);
            }

            String s = domToStr(newdoc);
            logger.debug("Formatted SdncLcmReq:\n{}", s);
            return s;

        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_ERROR_CREATE_SDNC_REQUEST.toString(), "SDNC",
                    ErrorCode.BusinessProcessError.getValue(), "Exception in genSdncLcmReq", e);
        }
        return null;
    }

    public static String genOpticalSdncReq(Document reqDoc, RequestTunables rt) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // NewDoc for output
            // Root
            Document newdoc = db.newDocument();
            Element root = newdoc.createElementNS(rt.getNamespace(), "input");
            newdoc.appendChild(root);
            // RequestData
            NodeList nodes = reqDoc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                Node newNode = newdoc.importNode(n, true);
                root.appendChild(newNode);
            }
            String req = domToStr(newdoc);
            String s = xml2json(req, true);
            logger.debug("Formatted SdncReq:\n", s);
            return s;

        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_ERROR_CREATE_SDNC_REQUEST.toString(), "SDNC",
                    ErrorCode.BusinessProcessError.getValue(), "Exception in genOpticalSdncReq", e);
        }
        return null;
    }

    /**
     * Uses the JSONObject static method to convert a XML doc to JSON.
     *
     * @param xml String containing the XML doc
     * @param pretty flag to determine if the output should be formatted
     * @return String containing the JSON translation
     */
    public static String xml2json(String xml, boolean pretty) {
        try {
            // name spaces cause problems, so just remove them
            JSONObject jsonObj = XML.toJSONObject(removeNamespaces(xml));
            if (!pretty) {
                return jsonObj.toString();
            } else {
                // add an indent to make it 'pretty'
                return jsonObj.toString(MSOJsonIndentFactor);
            }
        } catch (Exception e) {
            logger.debug("xml2json(): unable to parse xml and convert to json. Exception was: {}", e.toString(), e);
            return null;
        }
    }

    /**
     * Removes namespaces and namespace declarations from an XML document.
     *
     * @param xml the XML document
     * @return a possibly modified document
     */
    public static String removeNamespaces(Object xml) {
        if (xml == null) {
            logger.debug("removeNamespaces input object is null , returning null");
            return null;
        }

        String text = String.valueOf(xml);

        // remove xmlns declaration
        text = text.replaceAll("xmlns.*?(\"|\').*?(\"|\')", "");
        // remove opening tag prefix
        text = text.replaceAll("(<)(\\w+:)(.*?>)", "$1$3");
        // remove closing tags prefix
        text = text.replaceAll("(</)(\\w+:)(.*?>)", "$1$3");
        // remove extra spaces left when xmlns declarations are removed
        text = text.replaceAll("\\s+>", ">");

        return text;
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
            logger.debug("Formatted MsoFailResp:\n{}", s);
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
