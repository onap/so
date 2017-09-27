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

package org.openecomp.mso.client.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;

public class DmaapConsumer {

	private final MRConsumer mrConsumer;
	public DmaapConsumer() {
		mrConsumer = null;
	}
	public DmaapConsumer (String filepath) throws FileNotFoundException, IOException {
		
		mrConsumer = MRClientFactory.createConsumer(filepath);
	}
	
	
	public MRConsumer getMRConsumer() {
		return mrConsumer;
	}
	public boolean consume(Consumer consumer) throws Exception {
		boolean accepted = false;
		while (consumer.continuePolling()) {
			for (String message : this.getMRConsumer().fetch()) {
				if (!accepted && consumer.isAccepted(message)) {
					accepted = true;
				} 
				if (accepted) {
					consumer.processMessage(message);
				}
			}
		}
		
		return true;
	}
	
}
