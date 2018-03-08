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


import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.db.AbstractSessionFactoryManager;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class RequestsDatabaseTest {

    RequestsDatabase requestsDatabase = RequestsDatabase.getInstance();

    @Test
    public void getInstanceTest() throws Exception {
        RequestsDatabase instance = RequestsDatabase.getInstance();
        assertEquals(RequestsDatabase.class, instance.getClass());
    }

    @Test
    public void healthCheckTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                @Mocked Session session,
                                @Mocked SQLQuery query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createSQLQuery(" show tables ");
            result = query;
            query.list();
            result = Arrays.asList("table1", "table2");
        }};

        assertTrue(requestsDatabase.healthCheck());
    }

    @Test
    public void updateInfraStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                      @Mocked Session session,
                                      @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            query.executeUpdate();
            result = 1;
        }};
        assertEquals(1, requestsDatabase.updateInfraStatus("123", "unknown", "unknown"));
    }

    @Test
    public void updateInfraStatus1Test(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                       @Mocked Session session,
                                       @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            query.executeUpdate();
            result = 1;
        }};
        assertEquals(1, requestsDatabase.updateInfraStatus("123", "unknown", 0, "unknown"));
    }

    @Test
    public void updateInfraFinalStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                           @Mocked Session session,
                                           @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            query.executeUpdate();
            result = 1;
        }};
        assertEquals(1, requestsDatabase.updateInfraFinalStatus("123",
                "unknown",
                "statusMessage",
                0,
                "responsebody",
                "lastmodifiedby"));
    }

    @Test
    public void getRequestFromInfraActiveTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                              @Mocked Session session,
                                              @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            query.uniqueResult();
            result = new InfraActiveRequests("123", "action");
        }};
        assertEquals("123",
                requestsDatabase.getRequestFromInfraActive("123").getRequestId());
    }

    @Test
    public void getOrchestrationFiltersFromInfraActiveTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                           @Mocked Session session,
                                                           @Mocked Criteria criteria) throws Exception {

        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createCriteria(InfraActiveRequests.class);
            result = criteria;
            criteria.list();
            result = Arrays.asList(new InfraActiveRequests("123", "action"));
        }};
        assertEquals(1,
                requestsDatabase.getRequestListFromInfraActive("queryattr",
                        "queryvalue",
                        "type").size());
    }

    @Test
    public void getRequestListFromInfraActiveTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                  @Mocked Session session,
                                                  @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("from InfraActiveRequests where (requestId = :requestId OR clientRequestId = :requestId) and requestType = :requestType");
            result = query;
            query.uniqueResult();
            result = new InfraActiveRequests("123", "action");
        }};
        assertEquals("123",
                requestsDatabase.getRequestFromInfraActive("123", "requestType").getRequestId());
    }

    @Test
    public void getRequestFromInfraActive1Test(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                               @Mocked Session session,
                                               @Mocked Criteria criteria) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createCriteria(InfraActiveRequests.class);
            result = criteria;
            criteria.list();
            result = Arrays.asList(new InfraActiveRequests());
        }};
        assertEquals(1,
                requestsDatabase.getRequestListFromInfraActive("queryAttr",
                        "queryvalue",
                        "type").size());
    }

    @Test
    public void checkDuplicateByVnfNameTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                            @Mocked Session session,
                                            @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("from InfraActiveRequests where vnfName = :vnfName and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT') and requestType = :requestType ORDER BY startTime DESC");
            result = query;
            query.list();
            result = Arrays.asList(new InfraActiveRequests("123", "action"));
        }};
        assertEquals("123",
                requestsDatabase.checkDuplicateByVnfName("vnfname",
                        "action",
                        "requesttype").getRequestId());
    }

    @Test
    public void checkDuplicateByVnfIdTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                          @Mocked Session session,
                                          @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("from InfraActiveRequests where vnfId = :vnfId and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT') and requestType = :requestType ORDER BY startTime DESC");
            result = query;
            query.list();
            result = Arrays.asList(new InfraActiveRequests("123", "action"));
        }};
        assertEquals("123",
                requestsDatabase.checkDuplicateByVnfId("vnfname",
                        "action",
                        "requesttype").getRequestId());
    }

    @Test
    public void setMockDBTest() throws Exception {
        requestsDatabase.setMockDB(null);
    }

    @Test
    public void getSiteStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                  @Mocked Session session,
                                  @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM SiteStatus WHERE siteName = :site_name");
            result = query;
            query.uniqueResult();
            result = new SiteStatus();
        }};
        assertEquals(SiteStatus.class,
                requestsDatabase.getSiteStatus("site").getClass());
    }

    @Test
    public void updateSiteStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                     @Mocked Session session,
                                     @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM SiteStatus WHERE siteName = :site_name");
            result = query;
            query.uniqueResult();
            result = new SiteStatus();
        }};
        requestsDatabase.updateSiteStatus("site", true);
    }

    @Test
    public void getOperationStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                       @Mocked Session session,
                                       @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM OperationStatus WHERE SERVICE_ID = :service_id and OPERATION_ID = :operation_id");
            result = query;
            query.uniqueResult();
            result = new OperationStatus();
        }};
        assertEquals(OperationStatus.class,
                requestsDatabase.getOperationStatus("123",
                        "Unknown").getClass());
    }

    @Test
    public void getOperationStatusByServiceIdTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                  @Mocked Session session,
                                                  @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM OperationStatus WHERE SERVICE_ID = :service_id");
            result = query;
            query.uniqueResult();
            result = new OperationStatus();
        }};
        assertEquals(OperationStatus.class,
                requestsDatabase.getOperationStatusByServiceId("123").getClass());
    }

    @Test
    public void getOperationStatusByServiceNameTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                    @Mocked Session session,
                                                    @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM OperationStatus WHERE SERVICE_NAME = :service_name");
            result = query;
            query.uniqueResult();
            result = new OperationStatus();
        }};
        assertEquals(OperationStatus.class,
                requestsDatabase.getOperationStatusByServiceName("servicename").getClass());
    }

    @Test
    public void updateOperationStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                          @Mocked Session session,
                                          @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM OperationStatus WHERE SERVICE_ID = :service_id and OPERATION_ID = :operation_id");
            result = query;
            query.uniqueResult();
            result = new OperationStatus();
        }};
        requestsDatabase.updateOperationStatus(new OperationStatus());
    }

    @Test
    public void getResourceOperationStatusTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                               @Mocked Session session,
                                               @Mocked Query query) throws Exception {
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
            session.createQuery("FROM ResourceOperationStatus WHERE serviceId = :service_id and operationId = :operation_id and resourceTemplateUUID= :uuid");
            result = query;
            query.uniqueResult();
            result = new ResourceOperationStatus();
        }};
        assertEquals(ResourceOperationStatus.class,
                requestsDatabase.getResourceOperationStatus("serviceId",
                        "operationid",
                        "123-uuid").getClass());
    }
}
