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
package org.onap.so.client.dmaapproperties;

import java.io.IOException;
import java.util.Optional;

import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.dmaap.DmaapPublisher;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class GlobalDmaapPublisher extends DmaapPublisher {

	
	public GlobalDmaapPublisher() throws IOException {
		super();
	}
	
	@Override
	public String getUserName() {

		return UrnPropertiesReader.getVariable("mso.global.dmaap.username");
	}



	@Override
	public String getPassword() {

		return UrnPropertiesReader.getVariable("mso.global.dmaap.password");
	}

	@Override
	public String getTopic() {
		
		return UrnPropertiesReader.getVariable("mso.global.dmaap.publisher.topic");
	}

	@Override
	public Optional<String> getHost() {
		return Optional.ofNullable(UrnPropertiesReader.getVariable("mso.global.dmaap.host"));
	}
}
