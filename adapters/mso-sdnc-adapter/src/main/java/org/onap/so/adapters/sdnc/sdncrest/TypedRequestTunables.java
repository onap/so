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

package org.onap.so.adapters.sdnc.sdncrest;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.onap.so.adapters.sdnc.impl.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Typed Request Tunables.  Each entry is identified by a TYPE in the property name.
 * Different types can have different keys.
 * <p>
 * General format:
 * <pre>
 * org.onap.so.adapters.sdnc.TYPE.KEY1[.KEY2...]=METHOD|TIMEOUT|URL|HEADER|NAMESPACE
 * </pre>
 * Currently supported type(s): service
 * <pre>
 * org.onap.so.adapters.sdnc.service.SERVICE.OPERATION=METHOD|TIMEOUT|URL|HEADER|NAMESPACE
 * </pre>
 */
public class TypedRequestTunables {	

	private static final Logger logger = LoggerFactory.getLogger(TypedRequestTunables.class);

	private String reqId;
	private String myUrlSuffix;
	private String key = null;
	private String error = "";

	// tunables (all are required)
	private String reqMethod = null;
	private String timeout = null;
	private String sdncUrl = null;
	private String headerName = null;
	private String namespace = null;
	private String myUrl = null;
	
	public TypedRequestTunables(TypedRequestTunables reqTunableOriginal) {
		this.reqId = reqTunableOriginal.getReqId();
		this.myUrlSuffix = reqTunableOriginal.getMyUrlSuffix();
		this.key = reqTunableOriginal.getKey();
		this.error = reqTunableOriginal.getError();
		this.reqMethod = reqTunableOriginal.getReqMethod();
		this.timeout = reqTunableOriginal.getTimeout();
		this.sdncUrl = reqTunableOriginal.getSdncUrl();
		this.headerName = reqTunableOriginal.getHeaderName();
		this.namespace = reqTunableOriginal.getNamespace();
		this.myUrl = reqTunableOriginal.getMyUrl();		
	}

	public TypedRequestTunables(String reqId, String myUrlSuffix) {
		this.reqId = reqId;
		this.myUrlSuffix = myUrlSuffix;
	}

	/**
	 * Sets the key for a service request:
	 * <pre>
	 * org.onap.so.adapters.sdnc.service.SERVICE.OPERATION
	 * </pre>
	 * @param service the sdncService
	 * @param operation the sdncOperation
	 */
	public void setServiceKey(String service, String operation) {
		key = Constants.REQUEST_TUNABLES + ".service." + service + "." + operation;
		logger.debug("Generated {} key: {}", getClass().getSimpleName(), key);
	}

	/**
	 * Gets the SDNC request ID.
	 */
	public String getReqId() {
		return reqId;
	}

	/**
	 * Gets the generated key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the most recent error, or null if there was no error.
	 */
	public String getError() {
		return error;
	}

	public String getReqMethod() {
		return reqMethod;
	}

	public String getTimeout() {
		return timeout;
	}

	public String getSdncUrl() {
		return sdncUrl;
	}

	public String getHeaderName() {
		return headerName;
	}

	public String getNamespace() {
		return namespace;
	}

	/**
	 * Gets the SDNC adapter notification URL, trimmed of trailing '/' characters.
	 */
	public String getMyUrl() {
		return myUrl;
	}	

	public String getMyUrlSuffix() {
		return myUrlSuffix;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setReqMethod(String reqMethod) {
		this.reqMethod = reqMethod;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public void setSdncUrl(String sdncUrl) {
		this.sdncUrl = sdncUrl;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setMyUrl(String myUrl) {
		this.myUrl = myUrl;
	}	

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("reqId", reqId).append("myUrlSuffix", myUrlSuffix).append("key", key)
				.append("error", error).append("reqMethod", reqMethod).append("timeout", timeout)
				.append("sdncUrl", sdncUrl).append("headerName", headerName).append("namespace", namespace)
				.append("myUrl", myUrl).toString();
	}
	
}
