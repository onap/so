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
 
package org.openecomp.mso.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;

@Provider
@Priority(Integer.MIN_VALUE)
public class ResponseExceptionMapperImpl extends ResponseExceptionMapper {

	@Override
	public Optional<String> extractMessage(InputStream stream) throws IOException {
		final String input = IOUtils.toString(stream, "UTF-8");
		IOUtils.closeQuietly(stream);
		return Optional.of(input);
	}
	

}
