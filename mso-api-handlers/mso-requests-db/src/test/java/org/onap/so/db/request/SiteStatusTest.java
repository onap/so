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

package org.onap.so.db.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.db.request.beans.SiteStatus;
import org.onap.so.db.request.data.repository.SiteStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SiteStatusTest {
	
	@Autowired
	private SiteStatusRepository repository;
	
	@Test	
	@Transactional
	public void timeStampCreated() throws InterruptedException {
		SiteStatus found = repository.findOne("test name4");		
		assertNotNull(found.getCreated());
		assertEquals("test name4", found.getSiteName());		
	}
	
	@Test
	public void sortByCreated() {
		
		final PageRequest page1 = new PageRequest(
				  0, 20, Direction.DESC, "created"
				);
		
		SiteStatus example = new SiteStatus();
		example.setStatus(true);
		Page<SiteStatus> found = repository.findAll(Example.of(example), page1);
		
		assertEquals("test name4", found.getContent().get(0).getSiteName());
		
	}
	
	@Test
	public void updateStatus() {
		
		SiteStatus status = repository.findOne("test name update");
		status.setStatus(false);
		
		repository.saveAndFlush(status);

		assertEquals(false, repository.findOne("test name update").getStatus());
		
	}
	
}
