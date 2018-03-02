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

package org.openecomp.mso.client.ruby;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.openecomp.mso.client.dmaap.DmaapPublisher;
import org.openecomp.mso.client.ruby.beans.Event;
import org.openecomp.mso.client.ruby.beans.MsoRequest;
import org.openecomp.mso.client.ruby.beans.Ruby;
import org.openecomp.mso.client.ruby.dmaap.RubyCreateTicketRequestPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

 
public class RubyClient {
	
	private static final String REQUEST_CLIENT_NAME = "MSO";
	private static final String ACTION = "Create Ticket";
	
	protected String buildRequest(String requestId, String sourceName, String reason, String workflowId, String notification) throws JsonProcessingException {
		final MsoRequest request = new MsoRequest();
		request.withRequestClientName(REQUEST_CLIENT_NAME)
	    	   .withRequestId(requestId)		
			   .withSourceName(sourceName)
			   .withWorkflowId(workflowId)
			   .withAction(ACTION);
		 
		request.withRequestTime(this.getTime());
		
		if(reason.length() <= 255){
			request.withReason(reason);
		} else {
			throw new IllegalArgumentException("reason exceeds 255 characters");
		}
		if(notification.length() <= 1024){
			request.withNotification(notification);
		} else {
			throw new IllegalArgumentException("notification exceeds 1024 characters");
		}
		final Event event = new Event();
		event.setMsoRequest(request);
		final Ruby ruby = new Ruby();	
		ruby.setEvent(event);	
		return this.getJson(ruby);
	}
	
	protected String getJson(Ruby obj) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(obj);
	}
	
	protected DmaapPublisher getPublisher() throws IOException {
		return new RubyCreateTicketRequestPublisher();
	}	
	
	protected String getTime() {
		final ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
		return currentDateTime.format(format);
	}
	
	public void rubyCreateTicketCheckRequest(String requestId, String sourceName, String reason, String workflowId, String notification) throws Exception {
		String request = this.buildRequest(requestId, sourceName, reason, workflowId, notification);
		final DmaapPublisher publisher = this.getPublisher();
		publisher.send(request);
	}
}