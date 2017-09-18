/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import org.openecomp.mso.adapters.sdncrest.SDNCEvent;
import org.openecomp.mso.logger.MsoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.ParseException;

/**
 * SDNCConnector for "agnostic" API services.
 */
public class SDNCEventParser {
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    
 // Instantiation is not allowed.
    private SDNCEventParser() {
    }
    
    /**
	 * Parses SDNC event XML. If the content can be parsed and contains all required
	 * elements, then an object is returned. Otherwise, a ParseException is thrown.
	 * This method performs no logging or alarming.
	 * @throws ParseException on error
	 */
	public static SDNCEvent parse(String content) throws ParseException {
		try {
			// Note: this document builder is not namespace-aware, so namespaces are ignored.
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			InputSource source = new InputSource(new StringReader(content));
			Document doc = documentBuilderFactory.newDocumentBuilder().parse(source);

			// Find the configuration-event child under the root element.
			// The root element is expected to be an "output" element, but we don't really care.

			Element root = doc.getDocumentElement();
			Element configurationEvent = null;

			for (Element child : SDNCAdapterUtils.childElements(root)) {
				if ("configuration-event".equals(child.getNodeName())) {
					configurationEvent = child;
					break;
				}
			}

			if (configurationEvent == null) {
				throw new ParseException("No configuration-event element in SDNC event", 0);
			}

			// Process the children of configuration-event

			String eventType = null;
			String eventCorrelatorType = null;
			String eventCorrelator = null;
			Element eventParameters = null;

			for (Element child : SDNCAdapterUtils.childElements(configurationEvent)) {
				if ("event-type".equals(child.getNodeName())) {
					eventType = child.getTextContent();
				} else if ("event-correlator-type".equals(child.getNodeName())) {
					eventCorrelatorType = child.getTextContent();
				} else if ("event-correlator".equals(child.getNodeName())) {
					eventCorrelator = child.getTextContent();
				} else if ("event-parameters".equals(child.getNodeName())) {
					eventParameters = child;
				}
			}

			// event-type is mandatory.

			if (eventType == null || eventType.isEmpty()) {
				throw new ParseException("No event-type in SDNC event", 0);
			}

			// event-correlator-type is mandatory.

			if (eventCorrelatorType == null || eventCorrelatorType.isEmpty()) {
				throw new ParseException("No event-correlator-type in SDNC event", 0);
			}

			// event-correlator is mandatory.

			if (eventCorrelator == null || eventCorrelator.isEmpty()) {
				throw new ParseException("No event-correlator in SDNC event", 0);
			}

			// Create an event object.

			SDNCEvent event = new SDNCEvent(eventType, eventCorrelatorType, eventCorrelator);

			// event-parameters is an optional container element.  If present,
			// process its children, adding values to the event object.

			if (eventParameters != null) {
				for (Element element : SDNCAdapterUtils.childElements(eventParameters)) {
					if (!"event-parameter".equals(element.getNodeName())) {
						continue;
					}

					String tagName = null;
					String tagValue = null;

					for (Element child : SDNCAdapterUtils.childElements(element)) {
						if ("tag-name".equals(child.getNodeName())) {
							tagName = child.getTextContent();
						} else if ("tag-value".equals(child.getNodeName())) {
							tagValue = child.getTextContent();
						}
					}

					// tag-name is mandatory

					if (tagName == null) {
						throw new ParseException("Missing tag-name in SDNC event parameter", 0);
					}

					// tag-value is optional.  If absent, make it an empty string so we don't
					// end up with null values in the parameter map.

					if (tagValue == null) {
						tagValue = "";
					}

					event.addParam(tagName, tagValue);
				}
			}

			return event;
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
		    LOGGER.debug("Exception:", e);
			throw new ParseException("Failed to parse SDNC event:", 0 );
		}
	}
}