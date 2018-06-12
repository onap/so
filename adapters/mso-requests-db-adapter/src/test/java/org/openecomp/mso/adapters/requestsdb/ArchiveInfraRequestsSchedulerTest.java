package org.openecomp.mso.adapters.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.adapters.requestsdb.application.MSORequestDBApplication;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.data.repository.ArchivedInfraRequestsRepository;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
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
