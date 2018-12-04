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

package org.onap.so.client;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.*;

import org.onap.so.utils.TargetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient extends RestClient {

	protected final Logger log = LoggerFactory.getLogger(HttpClient.class);
    private TargetEntity targetEntity;
	public HttpClient(URL host, String contentType, TargetEntity targetEntity) {
		super(host, contentType);
		this.targetEntity = targetEntity;

	}

    @Override
    public TargetEntity getTargetEntity(){
        return targetEntity;
    }

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
	}

	@Override
	protected Optional<ResponseExceptionMapper> addResponseExceptionMapper() {
		return Optional.empty();
	}

	/**
	 * Adds a basic authentication header to the request.
	 * @param auth the encrypted credentials
	 * @param key the key for decrypting the credentials
	 */
	@Override
	public void addBasicAuthHeader(String auth, String key) {
		if(isNotBlank(auth) && isNotBlank(key)){
			super.addBasicAuthHeader(auth, key);
		}else{
			log.warn("Not adding basic auth to headers.");
		}
	}

	/**
	 * Adds an additional header to the header map
	 * @param encoded basic auth value
	 */
	public void addAdditionalHeader(String name, String value) {
		try {
			if(isNotBlank(name) && isNotBlank(value)){
				headerMap.put(name, value);
			}else{
				log.warn("Not adding " + name + " to headers.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
