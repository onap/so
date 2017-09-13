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
package org.openecomp.mso.adapters.workflowmessage;

import java.io.UnsupportedEncodingException;

import org.springframework.web.util.UriUtils;

/**
 * Utility methods used by WMAdapterRest.
 */
public final class WMAdapterUtils {
	/**
	 * Encodes a URL path segment according to RFC 3986 Section 2.
	 * @param pathSegment the path segment to encode
	 * @return the encoded path segment
	 */
	public static String encodeURLPathSegment(String pathSegment) {
		try {
			return UriUtils.encodePathSegment(pathSegment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 encoding is not supported",e);
		}
	}

	/**
	 * Instantiation is not allowed.
	 */
	private WMAdapterUtils() {
	}
}