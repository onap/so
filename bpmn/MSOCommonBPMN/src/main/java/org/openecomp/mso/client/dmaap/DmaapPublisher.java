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
import java.util.concurrent.TimeUnit;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.att.nsa.mr.client.MRClientFactory;

public class DmaapPublisher {
	
	private final long seconds;
	private final MRBatchingPublisher publisher;
	
	public DmaapPublisher(String filepath) throws FileNotFoundException, IOException {
		this.seconds = 20;
		this.publisher = MRClientFactory.createBatchingPublisher(filepath);
	}
	
	public DmaapPublisher(String filepath, long seconds) throws FileNotFoundException, IOException {
		this.seconds = seconds;
		this.publisher = MRClientFactory.createBatchingPublisher(filepath);
	}
	
	public void send(String json) throws IOException, InterruptedException {
		publisher.send(json);
		publisher.close(seconds, TimeUnit.SECONDS);
	}

}
