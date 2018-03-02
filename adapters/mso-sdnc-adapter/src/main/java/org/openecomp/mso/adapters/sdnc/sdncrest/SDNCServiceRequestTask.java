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
package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.openecomp.mso.adapters.sdncrest.SDNCErrorCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceError;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class SDNCServiceRequestTask implements Runnable {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

	private final SDNCServiceRequest request;
	private final String msoRequestId;
	private final String msoServiceInstanceId;
	private final String myUrlSuffix;

	public SDNCServiceRequestTask(SDNCServiceRequest request,
			String msoRequestId, String msoServiceInstanceId,
			String myUrlSuffix) {
		this.request = request;
		this.msoRequestId = msoRequestId;
		this.msoServiceInstanceId = msoServiceInstanceId;
		this.myUrlSuffix = myUrlSuffix;
	}

	@Override
	public void run()
	{
		MsoLogger.setLogContext(msoRequestId, msoServiceInstanceId);
		MsoLogger.setServiceName(getClass().getSimpleName());

		LOGGER.debug(getClass().getSimpleName() + ".run()"
			+ " entered with request: " + request.toJson());

		String sdncRequestId = request.getSDNCRequestId();
		String sdncService = request.getSDNCService();
		String sdncOperation = request.getSDNCOperation();

		TypedRequestTunables rt = new TypedRequestTunables(sdncRequestId, myUrlSuffix);
		rt.setServiceKey(sdncService, sdncOperation);

		if (!rt.setTunables()) {
			// Note that the error was logged and alarmed by setTunables()
			SDNCServiceError error = new SDNCServiceError(request.getSDNCRequestId(),
				String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR), rt.getError(), "Y");
			BPRestCallback callback = new BPRestCallback();
			callback.send(request.getBPNotificationUrl(), error.toJson());
			return;
		}

		String xml = genSdncReq(request, rt);

		long sdncStartTime = System.currentTimeMillis();
		SDNCConnector connector = new SDNCServiceRequestConnector();
		SDNCResponseCommon response = connector.send(xml, rt);

		if (response instanceof SDNCErrorCommon) {
			LOGGER.recordMetricEvent(sdncStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
				"Received success response from SDNC", "SDNC", sdncService + "." + sdncOperation, null);
		} else {
			LOGGER.recordMetricEvent(sdncStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
				"Received error response from SDNC", "SDNC", sdncService + "." + sdncOperation, null);
		}

		long bpStartTime = System.currentTimeMillis();
		BPRestCallback callback = new BPRestCallback();
		boolean callbackSuccess = callback.send(request.getBPNotificationUrl(), response.toJson());

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

			Element requestInformation = addChild(root, "request-information");
			addTextChild(requestInformation, "request-id", request.getRequestInformation().getRequestId());
			if(request.getRequestInformation().getRequestAction()!= null) {
				addTextChild(requestInformation, "request-action",
						request.getRequestInformation().getRequestAction());
			}
			if(request.getRequestInformation().getRequestSubAction()!= null) {
				addTextChild(requestInformation, "request-sub-action",
						request.getRequestInformation().getRequestSubAction());
			}
			addTextChild(requestInformation, "source", request.getRequestInformation().getSource());
			addTextChild(requestInformation, "notification-url", request.getRequestInformation().getNotificationUrl());

			Element serviceInformation = addChild(root, "service-information");
			addTextChild(serviceInformation, "service-type", request.getServiceInformation().getServiceType());
			addTextChild(serviceInformation, "service-instance-id", request.getServiceInformation().getServiceInstanceId());
			addTextChild(serviceInformation, "subscriber-name", request.getServiceInformation().getSubscriberName());
			addTextChild(serviceInformation, "subscriber-global-id", request.getServiceInformation().getSubscriberGlobalId());

			Element agnosticServiceInformation = addChild(root, "agnostic-service-information");
			addTextChild(agnosticServiceInformation, "operation", request.getSDNCOperation());
			addTextChild(agnosticServiceInformation, "service", request.getSDNCService());

			// anydata is mandatory in the SDNC schema, so if the data we got is null,
			// set use an empty string instead to ensure we generate an empty element.

			String anydata = request.getSDNCServiceData();
			if (anydata == null) {
				anydata = "";
			}

			// content-type is also mandatory.

			String contentType = request.getSDNCServiceDataType();

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
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
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

		LOGGER.debug("Formatted SDNC service request XML:\n" + xml);
		return xml;
	}
}
