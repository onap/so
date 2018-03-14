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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.openecomp.mso.client.ruby.beans.Ruby;

import static org.apache.commons.lang3.StringUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RubyCheckClientTest {
	private final String fileLocation = "src/test/resources/org/openecomp/mso/client/ruby/create-ticket/";
	private static final String REQUEST_ID = "abc123";
	private static final String SOURCE_NAME = "source-name";
	private static final String TIME = "test-time";
	private static final String REASON = "reason";
	private static final String WORK_FLOW_ID = "work-flow-Id";
	private static final String NOTIFICATION = "notification";
	

	
	@Test
	public void verifyRubyCreateTicketRequest() throws IOException, ParseException{
		String content = this.getJson("create-ticket-request.json");
		ObjectMapper mapper = new ObjectMapper();
		Ruby expected = mapper.readValue(content, Ruby.class);
		RubyClient client = new RubyClient();
		RubyClient spy = spy(client);
		when(spy.getTime()).thenReturn(TIME);
		String actual = spy.buildRequest(REQUEST_ID, SOURCE_NAME, REASON, WORK_FLOW_ID, NOTIFICATION);
		assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);
	}
	
	
	@Test
	public void verifyTimeFormat() {
		RubyClient client = new RubyClient();
		String time = client.getTime();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
		formatter.parse(time);
	}
	
	
	@Test
	public void verifyReasonCharLimit() throws IOException{
		final String reasonLong = repeat("*", 256);
		RubyClient client = new RubyClient();
		try{
			client.buildRequest(REQUEST_ID, SOURCE_NAME, reasonLong, WORK_FLOW_ID, NOTIFICATION);
			fail("Should have thrown IllegalArgumentException but did not!");
		}
		catch(final IllegalArgumentException e){
			final String msg = "reason exceeds 255 characters";
			assertEquals(msg, e.getMessage());
		}
	}
	
	@Test
	public void verifyNotificationCharLimit() throws IOException{
		final String notificationLong = repeat("*", 1025);
		RubyClient client = new RubyClient();
		try{
			client.buildRequest(REQUEST_ID, SOURCE_NAME, REASON, WORK_FLOW_ID, notificationLong);
			fail("Should have thrown IllegalArgumentException but did not!");
		}
		catch(final IllegalArgumentException e){
			final String msg = "notification exceeds 1024 characters";
			assertEquals(msg, e.getMessage());
		}
	}
	
	private String getJson(String filename) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
	}
	
}
	
