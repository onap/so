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

package org.openecomp.mso.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.db.AbstractSessionFactoryManager;

public class OperationalEnvServiceModelStatusDbTest {

	@Mock
	private AbstractSessionFactoryManager sessionFactoryRequest;
	@Mock
	private SessionFactory sessionFactory;
	@Mock
	private Session session;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(sessionFactory.openSession()).thenReturn(session);
		when(sessionFactoryRequest.getSessionFactory()).thenReturn(sessionFactory);

	}

	@Test
	public void insertFailure() {
		OperationalEnvServiceModelStatusDb omsDB = new OperationalEnvServiceModelStatusDb(this.sessionFactoryRequest);
		Query mockQuery = mock(Query.class);
		when(session.createQuery(any(String.class))).thenReturn(mockQuery);
		when(mockQuery.uniqueResult()).thenReturn(null);
		when(session.isOpen()).thenReturn(true);
		when(session.getTransaction()).thenThrow(Exception.class);
		thrown.expect(Exception.class);

		omsDB.insertOperationalEnvServiceModelStatus("myRequestId", "myOperationalEnvId", "myServiceModelVersionId", "myVal", "myRecoveryAction", 1, "myWorkloadContext");
	}

	@Test
	public void updateFailureRetryCount() {
		OperationalEnvServiceModelStatusDb omsDB = new OperationalEnvServiceModelStatusDb(this.sessionFactoryRequest);
		Query mockQuery = mock(Query.class);
		when(session.createQuery(any(String.class))).thenReturn(mockQuery);
		when(session.isOpen()).thenReturn(true);
		when(session.getTransaction()).thenThrow(Exception.class);
		thrown.expect(Exception.class);
		
		omsDB.updateOperationalEnvRetryCountStatus("myOperationalEnvId", "myServiceModelVersionId", "status", 1);
	}
	
	@Test
	public void updateFailureRetryCountPerReqId() {
		OperationalEnvServiceModelStatusDb omsDB = new OperationalEnvServiceModelStatusDb(this.sessionFactoryRequest);
		Query mockQuery = mock(Query.class);
		when(session.createQuery(any(String.class))).thenReturn(mockQuery);
		when(session.isOpen()).thenReturn(true);
		when(session.getTransaction()).thenThrow(Exception.class);
		thrown.expect(Exception.class);
		
		omsDB.updateOperationalEnvRetryCountStatusPerReqId("myOperationalEnvId", "myServiceModelVersionId", "status", 1, "myReqId");
	}
	
	
	@Test
	public void getOperationalEnvIdStatusTest() {
		OperationalEnvServiceModelStatusDb omsDB = new OperationalEnvServiceModelStatusDb(this.sessionFactoryRequest);
		Query mockQuery = mock(Query.class);
		OperationalEnvServiceModelStatus status = new OperationalEnvServiceModelStatus();
		when(session.createQuery(any(String.class))).thenReturn(mockQuery);
		when(mockQuery.list()).thenReturn(Arrays.asList(status));
		when(session.isOpen()).thenReturn(true);
		assertEquals(status, omsDB.getOperationalEnvIdStatus("myEnvId", "myReqId").get(0));
	}
	
	@Test
	public void getOperationalEnvServiceModelStatusTest() {
		OperationalEnvServiceModelStatusDb omsDB = new OperationalEnvServiceModelStatusDb(this.sessionFactoryRequest);
		OperationalEnvServiceModelStatus status = new OperationalEnvServiceModelStatus();
		Query mockQuery = mock(Query.class);
		when(session.createQuery(any(String.class))).thenReturn(mockQuery);
		when(mockQuery.uniqueResult()).thenReturn(status);
		when(session.isOpen()).thenReturn(true);
		assertEquals(status, omsDB.getOperationalEnvServiceModelStatus("myEnvId", "myModelId"));
	}
}
