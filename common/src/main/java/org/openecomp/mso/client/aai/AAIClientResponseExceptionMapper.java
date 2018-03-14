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

package org.openecomp.mso.client.aai;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Priority;
import javax.ws.rs.ext.Provider;

import org.openecomp.mso.client.ResponseExceptionMapper;
import org.openecomp.mso.client.aai.entities.AAIError;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Priority(Integer.MIN_VALUE)
public class AAIClientResponseExceptionMapper extends ResponseExceptionMapper {

	private final UUID requestId;
	public AAIClientResponseExceptionMapper(UUID requestId) {
		this.requestId = requestId;
	}
	@Override
	public Optional<String> extractMessage(InputStream stream) throws IOException {
		
		String errorString = "Error calling A&AI. Request-Id=" + this.getRequestId() + " ";
		try {
			AAIError error = new ObjectMapper().readValue(stream, AAIError.class);
			AAIErrorFormatter formatter = new AAIErrorFormatter(error);
			return Optional.of(errorString + formatter.getMessage());
		} catch (JsonParseException e) {
			return Optional.of(errorString);
		}
	}
	
	protected UUID getRequestId() {
		return this.requestId;
	}
}
