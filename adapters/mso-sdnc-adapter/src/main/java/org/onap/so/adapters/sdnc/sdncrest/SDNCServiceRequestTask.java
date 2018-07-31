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

package org.onap.so.adapters.sdnc.sdncrest;

import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpStatus;
import org.onap.so.adapters.sdnc.exception.SDNCAdapterException;
import org.onap.so.adapters.sdncrest.RequestInformation;
import org.onap.so.adapters.sdncrest.SDNCErrorCommon;
import org.onap.so.adapters.sdncrest.SDNCResponseCommon;
import org.onap.so.adapters.sdncrest.SDNCServiceError;
import org.onap.so.adapters.sdncrest.SDNCServiceRequest;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
public class SDNCServiceRequestTask {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA,SDNCServiceRequestTask.class);

	@Autowired
	private SDNCServiceRequestConnector connector;
	
	@Autowired
	MapTypedRequestTunablesData mapTunables;
	
	@Autowired
	private BPRestCallback bpRestCallback;

	@Async
	public void runRequest(SDNCServiceRequest request,String msoRequestId,String msoServiceInstanceId,String myUrlSuffix) throws SDNCAdapterException
	{
		MsoLogger.setLogContext(msoRequestId, msoServiceInstanceId);
		MsoLogger.setServiceName(getClass().getSimpleName());

		String sdncRequestId = request.getSdncRequestId();
		String sdncService = request.getSdncService();
		String sdncOperation = request.getSdncOperation();

		TypedRequestTunables rt = new TypedRequestTunables(sdncRequestId, myUrlSuffix);
		rt.setServiceKey(sdncService, sdncOperation);
		TypedRequestTunables mappedTunables = mapTunables.setTunables(rt);
		if (!mappedTunables.getError().isEmpty()) {
			// Note that the error was logged and alarmed by setTunables()
			SDNCServiceError error = new SDNCServiceError(request.getSdncRequestId(),
				String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR), mappedTunables.getError(), "Y");
			bpRestCallback.send(request.getBPNotificationUrl(), error.toJson());
			return;
		}

		String xml = genSdncReq(request, mappedTunables);

		long sdncStartTime = System.currentTimeMillis();		
		SDNCResponseCommon response = connector.send(xml, mappedTunables);

		if (response instanceof SDNCErrorCommon) {
			LOGGER.recordMetricEvent(sdncStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
				"Received success response from SDNC", "SDNC", sdncService + "." + sdncOperation, null);
		} else {
			LOGGER.recordMetricEvent(sdncStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
				"Received error response from SDNC", "SDNC", sdncService + "." + sdncOperation, null);
		}

		long bpStartTime = System.currentTimeMillis();
		boolean callbackSuccess = bpRestCallback.send(request.getBPNotificationUrl(), response.toJson());

		if (callbackSuccess) {
			LOGGER.recordMetricEvent(bpStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
				"Sent notification", "BPMN", request.getBPNotificationUrl(), null);
		} else {
			LOGGER.recordMetricEvent(bpStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
				"Failed to send notification", "BPMN", request.getBPNotificationUrl(), null);
		}
	}

	private Element addChild(Element parent, String tag) {
		Element child = parent.getOwnerDocument().createElement(tag);
		parent.appendChild(child);
		return child;
	}

	private void addTextChild(Element parent, String tag, String text) {
		if (text != null) {
			Element child = parent.getOwnerDocument().createElement(tag);
			child.setTextContent(text);
			parent.appendChild(child);
		}
	}

	private String genSdncReq(SDNCServiceRequest request, TypedRequestTunables rt) {
		Document doc;

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			doc = documentBuilder.newDocument();
			Element root = doc.createElementNS(rt.getNamespace(), "input");
			doc.appendChild(root);

			Element hdr = addChild(root, rt.getHeaderName());
			addTextChild(hdr, "svc-request-id", rt.getReqId());
			addTextChild(hdr, "svc-notification-url", rt.getMyUrl());

			RequestInformation requestInfo = request.getRequestInformation();
			Element requestInformation = addChild(root, "request-information");
			addTextChild(requestInformation, "request-id", requestInfo.getRequestId());
		
			
			if(requestInfo.getRequestAction()!= null) {
				addTextChild(requestInformation, "request-action",
						requestInfo.getRequestAction());
			}
			if(requestInfo.getRequestSubAction()!= null) {
				addTextChild(requestInformation, "request-sub-action",
						requestInfo.getRequestSubAction());
			}
			if(requestInfo.getOrderNumber() != null &&
					!requestInfo.getOrderNumber().isEmpty() ) {
				addTextChild(requestInformation, "order-number",
						requestInfo.getOrderNumber());
			}
			
			if(requestInfo.getOrderVersion() != null &&
					!requestInfo.getOrderVersion().isEmpty() ) {
				addTextChild(requestInformation, "order-version",
						requestInfo.getOrderVersion());
			}
			
			
			addTextChild(requestInformation, "source", requestInfo.getSource());
			addTextChild(requestInformation, "notification-url", requestInfo.getNotificationUrl());

			Element serviceInformation = addChild(root, "service-information");
			addTextChild(serviceInformation, "service-type", request.getServiceInformation().getServiceType());
			addTextChild(serviceInformation, "service-instance-id", request.getServiceInformation().getServiceInstanceId());
			addTextChild(serviceInformation, "subscriber-name", request.getServiceInformation().getSubscriberName());
			addTextChild(serviceInformation, "subscriber-global-id", request.getServiceInformation().getSubscriberGlobalId());

			Element agnosticServiceInformation = addChild(root, "agnostic-service-information");
			addTextChild(agnosticServiceInformation, "operation", request.getSdncOperation());
			addTextChild(agnosticServiceInformation, "service", request.getSdncService());

			// anydata is mandatory in the SDNC schema, so if the data we got is null,
			// set use an empty string instead to ensure we generate an empty element.

			String anydata = request.getSdncServiceData();
			if (anydata == null) {
				anydata = "";
			}

			// content-type is also mandatory.

			String contentType = request.getSdncServiceDataType();

			if (contentType == null || contentType.isEmpty()) {
				if (anydata.isEmpty()) {
					contentType = "XML";
				} else {
					if (anydata.startsWith("<")) {
						contentType = "XML";
					} else {
						contentType = "JSON";
					}
				}
			}

			addTextChild(agnosticServiceInformation, "content-type", contentType);
			addTextChild(agnosticServiceInformation, "anydata", anydata);
		} catch (Exception e) {
			LOGGER.error(MessageEnum.RA_ERROR_CREATE_SDNC_REQUEST, "SDNC", "",
					MsoLogger.ErrorCode.BusinessProcesssError, "Exception in genSdncReq", e);
			return null;
		}

		String xml;

		try {
			StringWriter writer = new StringWriter();
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			xml = writer.toString();
		} catch (Exception e) {
			LOGGER.error(MessageEnum.RA_ERROR_CONVERT_XML2STR, "", "",
				MsoLogger.ErrorCode.DataError, "Exception - domToStr", e);
			return null;
		}

		LOGGER.trace("Formatted SDNC service request XML:\n" + xml);
		return xml;
	}
}
