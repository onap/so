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

package org.onap.so.client.ruby.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.onap.so.client.dmaap.DmaapPublisher;

public class RubyCreateTicketRequestPublisher extends DmaapPublisher{
	public RubyCreateTicketRequestPublisher() throws FileNotFoundException, IOException {
		super();
	}
	
	@Override
	public String getAuth() {
		return msoProperties.get("ruby.create-ticket-request.dmaap.auth");
	}

	@Override
	public String getKey() {
		return msoProperties.get("mso.msoKey");
	}

	@Override
	public String getTopic() {
		return msoProperties.get("ruby.create-ticket-request.publisher.topic");
	}

	@Override
	public Optional<String> getHost() {
		return Optional.ofNullable(msoProperties.get("ruby.create-ticket-request.publisher.host"));
	}
	
}


