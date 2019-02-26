/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.sdnc.sdncrest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods used by SDNCAdapterRest.
 */
public final class SDNCAdapterUtils {
    private static final Logger logger = LoggerFactory.getLogger(SDNCAdapterUtils.class);
    /**
     * Instantiation is not allowed.
     */
    private SDNCAdapterUtils() {
    }
    
    /**
	 * Returns a node's child elements in a list.
	 */
	public static List<Element> childElements(Node node) {
		List<Element> elements = new ArrayList<>();

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				elements.add((Element) child);
			}
		}

		return elements;
	}

	/**
	 * Encodes a URL path segment according to RFC 3986 Section 2.
	 * @param pathSegment the path segment to encode
	 * @return the encoded path segment
	 */
	public static String encodeURLPathSegment(String pathSegment) {		
			return UriUtils.encodePathSegment(pathSegment, "UTF-8");
	}
}
