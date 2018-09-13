/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.requestsdb.application.MSORequestDBApplication;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.ArchivedInfraRequestsRepository;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSORequestDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class ArchiveInfraRequestsSchedulerTest {
	
	@Autowired
	private ArchiveInfraRequestsScheduler scheduler;
	
	@Autowired
	private InfraActiveRequestsRepository iarRepo;
	
	@Autowired
	private ArchivedInfraRequestsRepository archivedRepo;
	
	@Value("${mso.infra-requests.archived.period}")
	private int archivedPeriod;
	
	@Test 
	@Transactional
	public void testArchiveInfraRequests() {
		String requestId1 = "requestId1";
		String requestId2 = "requestId2";
		
		InfraActiveRequests iar1 = new InfraActiveRequests();
		iar1.setRequestId(requestId1);
		iar1.setAction("action1");
		
		InfraActiveRequests iar2 = new InfraActiveRequests();
		iar2.setRequestId(requestId2);
		iar2.setAction("action2");
		
		List<InfraActiveRequests> requests = new ArrayList<>();
		requests.add(iar1);
		requests.add(iar2);
		iarRepo.save(requests);
		
		scheduler.archiveInfraRequests(requests);
		
		assertEquals(2, archivedRepo.count());
		assertEquals(requestId1, archivedRepo.findOne(requestId1).getRequestId());
		assertEquals(requestId2, archivedRepo.findOne(requestId2).getRequestId());
	}

	@Test	
	@Ignore
	public void testInfraRequestsScheduledTask() {
		Date currentDate= new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DATE, -archivedPeriod);
		Date archivingDate = calendar.getTime();
		
		List<InfraActiveRequests> requests = iarRepo.findByEndTimeLessThan(archivingDate, new PageRequest(0, 100));
		List<InfraActiveRequests> requests2 = iarRepo.findByStartTimeLessThanAndEndTime(archivingDate, null, new PageRequest(0, 100));
		
		int total = requests.size() + requests2.size();
		
		scheduler.infraRequestsScheduledTask();
		
		assertTrue(archivedRepo.count() >= total);
		assertTrue(iarRepo.count() < total);
	}
}
