/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.*;
import org.openecomp.mso.db.catalog.utils.RecordNotFoundException;

import mockit.Mock;
import mockit.MockUp;

public class CatalogDatabaseTest {

    CatalogDatabase cd = null;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private MockUp<CatalogDatabase> mockCd = null;
    private MockUp<Session> mockedSession = null;
    private MockUp<Query> mockUpQuery = null;
    private MockUp<Query> mockUpQuery2 = null;
    private MockUp<Query> mockUpQuery3 = null;
    private MockUp<Query> mockUpQuery4 = null;
    @Before
    public void setup(){
        cd = CatalogDatabase.getInstance();
    }
    
  
	 @After
	 public void tearDown() {
		 if (mockCd!=null) { mockCd.tearDown(); mockCd = null; }
		 if (mockedSession!=null) { mockedSession.tearDown(); mockedSession = null; }
		 if (mockUpQuery!=null) { mockUpQuery.tearDown(); mockUpQuery = null; }
		 if (mockUpQuery2!=null) { mockUpQuery2.tearDown(); mockUpQuery2 = null; }
		 if (mockUpQuery3!=null) { mockUpQuery3.tearDown(); mockUpQuery3 = null; }
		 if (mockUpQuery4!=null) { mockUpQuery4.tearDown(); mockUpQuery4 = null; }
    }



    @Test
    public void getAllHeatTemplatesTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List <HeatTemplate> list = cd.getAllHeatTemplates();
        assertEquals(list.size(), 1);


    }

    @Test
    public void getHeatTemplateByIdTest(){

        mockedSession = new MockUp<Session>() {
            @Mock
            public Object get(Class cls, Serializable id) {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("123-uuid");
                return heatTemplate;
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplate(10);
        assertEquals("123-uuid", ht.getAsdcUuid());


    }

    @Test
    public void getHeatTemplateByNameEmptyListTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplate("heat123");
        assertEquals(null, ht);


    }

    @Test
    public void getHeatTemplateByNameTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate1 = new HeatTemplate();
                heatTemplate1.setAsdcUuid("123-uuid");
                heatTemplate1.setVersion("1.2");
                HeatTemplate heatTemplate2 = new HeatTemplate();
                heatTemplate2.setAsdcUuid("456-uuid");
                heatTemplate2.setVersion("1.3");
                return Arrays.asList(heatTemplate1, heatTemplate2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplate("heat123");
        assertEquals("456-uuid", ht.getAsdcUuid());


    }

    @Test
    public void getHeatTemplateByTemplateNameTest() {

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("1234-uuid");
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplate("heat123","v2");
        assertEquals("1234-uuid", ht.getAsdcUuid());


    }

    @Test
    public void getHeatTemplateByTemplateNameEmptyResultTest() {

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplate("heat123","v2");
        assertEquals(null, ht);


    }

    @Test
    public void getHeatTemplateByArtifactUuidException(){

        mockedSession = new MockUp<Session>() {
            @Mock
            public Object get(Class cls, Serializable id) {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("123-uuid");
                return heatTemplate;
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuid("123");
        assertEquals("123-uuid", ht.getAsdcUuid());


    }

    @Test
    public void getHeatTemplateByArtifactUuidTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("123-uuid");
                return heatTemplate;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");
        assertEquals("123-uuid", ht.getAsdcUuid());


    }

    @Test
    public void getHeatTemplateByArtifactUuidNullTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");
        assertNull(ht);


    }

    @Test
    public void getHeatTemplateByArtifactUuidHibernateErrorTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");


    }

    @Test
    public void getHeatTemplateByArtifactUuidNonUniqueResultTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");


    }

    @Test
    public void getHeatTemplateByArtifactUuidGenericExceptionTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");


    }

    @Test
    public void getParametersForHeatTemplateTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("1234-uuid");
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<HeatTemplateParam> htList = cd.getParametersForHeatTemplate("12l3");
        assertEquals(1, htList.size());


    }

    @Test
    public void getParametersForHeatTemplateHibernateExceptionTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        List<HeatTemplateParam> htList = cd.getParametersForHeatTemplate("12l3");
  


    }

    @Test
    public void getParametersForHeatTemplateExceptionTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        List<HeatTemplateParam> htList = cd.getParametersForHeatTemplate("12l3");


    }

    @Test
    public void getHeatEnvironmentByArtifactUuidTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                HeatEnvironment heatEnvironment = new HeatEnvironment();
                heatEnvironment.setArtifactUuid("123-uuid");
                return heatEnvironment;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");
        assertEquals("123-uuid", he.getArtifactUuid());


    }

    @Test
    public void getHeatEnvironmentByArtifactUuidHibernateExceptionTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");


    }

    @Test
    public void getHeatEnvironmentByArtifactUuidExceptionTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");


    }

    @Test
    public void getHeatEnvironmentByArtifactUuidNonUniqueTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new org.hibernate.NonUniqueResultException(2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");
        assertNull(he);

    }

    @Test
    public void getServiceByInvariantUUIDTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getServiceByInvariantUUID("123");
        assertEquals("123-uuid", service.getModelUUID());


    }

    @Test
    public void getServiceByInvariantUUIDEmptyResultTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getServiceByInvariantUUID("123");
        assertEquals(null, service);


    }

    @Test
    public void getServiceTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getService("123");
        assertEquals("123-uuid", service.getModelUUID());


    }

    @Test
    public void getServiceNullTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getService("123");
        assertNull(service);


    }

    @Test
    public void getServiceNoUniqueResultTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        Service service = cd.getService("123");


    }

    @Test
    public void getServiceHibernateExceptionTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        Service service = cd.getService("123");


    }

    @Test
    public void getServiceExceptionTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception("generic exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        Service service = cd.getService("123");


    }

    @Test
    public void getServiceByModelUUIDTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        Service service = cd.getServiceByModelUUID("123");
        assertEquals("123-uuid", service.getModelUUID());


    }

    @Test
    public void getService2Test(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("serviceNameVersionId", "v2");
        Service service = cd.getService(map, "123");

        assertEquals("123-uuid", service.getModelUUID());

        map.remove("serviceNameVersionId");
        service = cd.getService(map, "123");
        assertNotNull(service);
    }

    @Test
    public void getService2NonUniqueTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
            	throw new org.hibernate.NonUniqueResultException(2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("serviceNameVersionId", "v2");

        thrown.expect(org.hibernate.NonUniqueResultException.class);
        Service service = cd.getService(map, "123");
    }

    @Test
    public void getService2HibernateExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
            	throw new org.hibernate.HibernateException("test case");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("serviceNameVersionId", "v2");
        
        thrown.expect(org.hibernate.HibernateException.class);
        Service service = cd.getService(map, "123");
    }

    @Test
    public void getService2ExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

        	@Mock
            public Object uniqueResult() throws Exception {
            	throw new NullPointerException();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("serviceNameVersionId", "v2");
        
        thrown.expect(NullPointerException.class);
        Service service = cd.getService(map, "123");
    }

    @Test
    public void getService2NullTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("serviceNameVersionId", "v2");
        
        Service service = cd.getService(map, "123");
        assertNull(service);
    }

    @Test
    public void getServiceByModelNameTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<Service> list() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getServiceByModelName("123");
        assertEquals("123-uuid", service.getModelUUID());


    }

    @Test
    public void getServiceByModelNameEmptyTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<Service> list() throws Exception {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getServiceByModelName("123");
        assertEquals(null, service);


    }

    @Test
    public void getServiceByVersionAndInvariantIdTest() throws Exception{

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        Service service = cd.getServiceByVersionAndInvariantId("123","tetwe");
        assertEquals("123-uuid", service.getModelUUID());


    }

    @Test
    public void getServiceByVersionAndInvariantIdNullTest() throws Exception{

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        Service service = cd.getServiceByVersionAndInvariantId("123","tetwe");
        assertNull(service);


    }

    @Test
    public void getServiceByVersionAndInvariantIdNonUniqueResultTest() throws Exception{

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        Service service = cd.getServiceByVersionAndInvariantId("123","tetwe");


    }

    @Test
    public void getServiceRecipeTestException() throws Exception{
    	thrown.expect(Exception.class);
        ServiceRecipe ht = cd.getServiceRecipe("123","tetwe");
    }

    @Test
    public void getServiceRecipeByServiceModelUuidTest() {
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() throws Exception {
                ServiceRecipe serviceRecipe = new ServiceRecipe();
                serviceRecipe.setId(1);
                return Arrays.asList(serviceRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        ServiceRecipe serviceRecipe = cd.getServiceRecipeByServiceModelUuid("123","tetwe");
        assertEquals(1, serviceRecipe.getId());

        serviceRecipe = cd.getServiceRecipeByServiceModelUuid("123", null);
        assertEquals(1, serviceRecipe.getId());
    }

    @Test
    public void getServiceRecipeByServiceModelUuidEmptyTest() {
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() throws Exception {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        ServiceRecipe serviceRecipe = cd.getServiceRecipeByServiceModelUuid("123","tetwe");
        assertEquals(null, serviceRecipe);


    }

    @Test
    public void getServiceRecipesTestException() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() {
                ServiceRecipe serviceRecipe = new ServiceRecipe();
                serviceRecipe.setId(1);
                return Arrays.asList(serviceRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<ServiceRecipe> serviceRecipes = cd.getServiceRecipes("123");
        assertEquals(1, serviceRecipes.size());


    }

    @Test
    public void getServiceRecipesEmptyTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<ServiceRecipe> serviceRecipes = cd.getServiceRecipes("123");
        assertEquals(0, serviceRecipes.size());


    }

    @Test
    public void getVnfComponentTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	VnfComponent vnfComponent = new VnfComponent();
            	vnfComponent.setHeatTemplateId(1234);
                return vnfComponent;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfComponent ht = cd.getVnfComponent(123,"vnf");
        assertEquals(new Integer(1234), ht.getHeatTemplateId());
    }

    @Test
    public void getVnfComponentNullTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfComponent ht = cd.getVnfComponent(123,"vnf");
        assertNull(ht);
    }

    @Test
    public void getVnfComponentNonUniqueTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new org.hibernate.NonUniqueResultException(2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(org.hibernate.NonUniqueResultException.class);
        VnfComponent ht = cd.getVnfComponent(123,"vnf");
    }

    @Test
    public void getVnfComponentHibernateExceptionTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new org.hibernate.HibernateException("test case");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(org.hibernate.HibernateException.class);
        VnfComponent ht = cd.getVnfComponent(123,"vnf");
    }

    @Test
    public void getVnfComponentExceptionTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new NullPointerException();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(NullPointerException.class);
        VnfComponent ht = cd.getVnfComponent(123,"vnf");
    }

    @Test
    public void getVnfResourceTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResource> list() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return Arrays.asList(vnfResource);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByType("vnf");
        assertEquals("123-uuid", vnfResource.getModelUuid());


    }

    @Test
    public void getVnfResourceEmptyTest() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResource> list() {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByType("vnf");
        assertEquals(null, vnfResource);


    }

    @Test
    public void getVnfResourceByTypeTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");
        assertEquals("123-uuid", vnfResource.getModelUuid());


    }

    @Test
    public void getVnfResourceNURExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");


    }

    @Test
    public void getVnfResourceHibernateExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");


    }

    @Test
    public void getVnfResourceExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");


    }

    @Test
    public void getVnfResourceByModelCustomizationIdTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
        assertEquals("123-uuid",vnfResource.getModelUuid());


    }

    @Test
    public void getVnfResourceByModelCustomizationIdNullTest() {
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() throws Exception {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
        assertNull(vnfResource);
    }

    @Test
    public void getVnfResourceByModelCustomizationIdNURExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");


    }

    @Test
    public void getVnfResourceByModelCustomizationIdHibernateExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
    }

    @Test
    public void getVnfResourceByModelCustomizationIdExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NullPointerException();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NullPointerException.class);
        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
    }


    @Test
    public void getServiceRecipeTest2() throws Exception{
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List <ServiceRecipe> list() throws Exception {
                ServiceRecipe serviceRecipe = new ServiceRecipe();
                serviceRecipe.setServiceModelUUID("123-uuid");
                return Arrays.asList(serviceRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        ServiceRecipe ht = cd.getServiceRecipe(1001,"3992");
        assertEquals("123-uuid", ht.getServiceModelUUID());
        
        ht = cd.getServiceRecipe(1001, null);
        assertEquals("123-uuid", ht.getServiceModelUUID());
        
    }

    @Test
    public void getServiceRecipeTest2Exception() throws Exception{
    	thrown.expect(Exception.class);
        ServiceRecipe ht = cd.getServiceRecipe(1001,"3992");
    }

    @Test
    public void getVnfResourceCustomizationByModelCustomizationNameTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResourceCustomization> list() throws Exception {
                VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
                vnfResourceCustomization.setVnfResourceModelUUID("123-uuid");
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelCustomizationName("test", "test234");
        assertEquals("123-uuid", vnf.getVnfResourceModelUUID());


    }

    @Test
    public void getVnfResourceCustomizationByModelCustomizationNameEmptyTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResourceCustomization> list() throws Exception {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelCustomizationName("test", "test234");
        assertEquals(null, vnf);


    }

    @Test
    public void getVnfResourceByModelInvariantIdTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult(){
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");
        assertEquals("123-uuid", vnf.getModelUuid());


    }

    @Test
    public void getVnfResourceByModelInvariantIdNURExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult(){
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");


    }

    @Test
    public void getVnfResourceByModelInvariantIdHibernateExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult(){
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");


    }

    @Test
    public void getVnfResourceByModelInvariantIdExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");


    }

    @Test
    public void getVnfResourceByIdTestException(){
        mockUpQuery = new MockUp<Query>() {
        	int id = 0;
        	@Mock
        	public Query setParameter(String name, Object val) {
        		id = (Integer)val;
        		return this.getMockInstance();
        	}
        	
            @Mock
            public List<VnfResource> list() throws Exception {
            	if (id==0) return new ArrayList<VnfResource>();
            	VnfResource vm = new VnfResource();
            	vm.setModelInvariantUuid(Integer.toString(id));
                return Arrays.asList(vm);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnf = cd.getVnfResourceById(19299);
        assertEquals("19299", vnf.getModelInvariantUuid());

        	vnf = cd.getVnfResourceById(0);
        	assertNull(vnf);
    }

    @Test
    public void getVfModuleModelName(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModule> list() throws Exception {
                VfModule vfModule = new VfModule();
                vfModule.setModelUUID("123-uuid");
                return Arrays.asList(vfModule);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VfModule vfModule = cd.getVfModuleModelName("vfmodule");
        assertEquals("123-uuid", vfModule.getModelUUID());


    }

    @Test
    public void getVfModuleModelNameExceptionTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModule> list() throws Exception {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VfModule vfModule = cd.getVfModuleModelName("vfmodule");
        assertEquals(null, vfModule);


    }

    @Test
    public void getVfModuleModelNameTest() {
        mockUpQuery = new MockUp<Query>() {
        	String modelVersion = null;
        	
        	@Mock
        	public Query setParameter(String name, Object val) {
        		if (name.equals("model_version")) modelVersion = (String)val;
        		return this.getMockInstance();
        	}

            @Mock
            public Object uniqueResult() {
            	if (modelVersion==null || modelVersion.equals("nil")) {
            		return null;
            	}
                VfModule vfModule = new VfModule();
                vfModule.setModelUUID("123-uuid");
                return vfModule;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");
        assertEquals("123-uuid", vfModule.getModelUUID());

        vfModule = cd.getVfModuleModelName("tetes","nil");
        assertNull(vfModule);
    }

    @Test
    public void getVfModuleModelNameNURExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");


    }

    @Test
    public void getVfModuleModelNameHibernateExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");


    }

    @Test
    public void getVfModuleModelNameGenericExceptionTest() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");


    }

    @Test
    public void ggetVfModuleCustomizationByModelNameTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModuleCustomization> list() throws Exception {
                VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
                vfModuleCustomization.setVfModuleModelUuid("123-uuid");
                return Arrays.asList(vfModuleCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VfModuleCustomization vfModuleCustomization = cd.getVfModuleCustomizationByModelName("tetes");
        assertEquals("123-uuid", vfModuleCustomization.getVfModuleModelUuid());


    }

    @Test
    public void ggetVfModuleCustomizationByModelNameEmptyTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModuleCustomization> list() throws Exception {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VfModuleCustomization vfModuleCustomization = cd.getVfModuleCustomizationByModelName("tetes");
        assertEquals(null, vfModuleCustomization);


    }

    @Test
    public void getNetworkResourceTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkResource> list() throws Exception {
                NetworkResource networkResource = new NetworkResource();
                networkResource.setModelUUID("123-uuid");
                return Arrays.asList(networkResource);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        NetworkResource networkResource = cd.getNetworkResource("tetes");
        assertEquals("123-uuid", networkResource.getModelUUID());


    }

    @Test
    public void getNetworkResourceTestEmptyException(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkResource> list() throws Exception {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        NetworkResource networkResource = cd.getNetworkResource("tetes");
        assertEquals(null, networkResource);


    }

    @Test
    public void getVnfRecipeTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setVfModuleId("123-id");
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfRecipe vnfRecipe = cd.getVnfRecipe("tetes","ergfedrf","4993493");
        assertEquals("123-id", vnfRecipe.getVfModuleId());


    }

    @Test
    public void getVnfRecipeEmptyTest(){

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                return Collections.emptyList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfRecipe vnfRecipe = cd.getVnfRecipe("tetes","ergfedrf","4993493");
        assertEquals(null, vnfRecipe);


    }

    @Test
    public void getVnfRecipe2Test(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setId(1);
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfRecipe vnfRecipe = cd.getVnfRecipe("tetes","4993493");
        assertEquals(1, vnfRecipe.getId());


    }

    @Test
    public void getVnfRecipe2EmptyTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                return Collections.emptyList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfRecipe vnfRecipe = cd.getVnfRecipe("tetes","4993493");
        assertEquals(null, vnfRecipe);


    }

    @Test
    public void getVnfRecipeByVfModuleIdTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setId(1);
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfRecipe vnfRecipe = cd.getVnfRecipeByVfModuleId("tetes","4993493","vnf");
        assertEquals(1, vnfRecipe.getId());


    }

    @Test
    public void getVnfRecipeByVfModuleIdEmptyTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                return Collections.emptyList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfRecipe vnfRecipe = cd.getVnfRecipeByVfModuleId("tetes","4993493","vnf");
        assertEquals(null, vnfRecipe);


    }

    @Test
    public void getVfModuleTypeTestException(){
        mockUpQuery = new MockUp<Query>() {
        	String type = null;
        	@Mock
        	public Query setParameter(String name, Object val) {
        		type = (String)val;
        		return this.getMockInstance();
        	}

        	@Mock
            public List<VfModule> list() {
        		if ("nil".equals(type)) return new ArrayList<VfModule>();

        		VfModule vm = new VfModule();
        		vm.setModelUUID("123-uuid");
                return Arrays.asList(vm);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vnf = cd.getVfModuleType("4993493");
        assertEquals("123-uuid", vnf.getModelUUID());
        
        vnf = cd.getVfModuleType("nil");
        assertNull(vnf);
    }

    @Test
    public void getVnfResourceByServiceUuidTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");
        assertEquals("123-uuid", vnfResource.getModelUuid());


    }

    @Test
    public void getVnfResourceByServiceUuidNURExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");


    }

    @Test
    public void getVnfResourceByServiceUuidHibernateExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");


    }

    @Test
    public void getVnfResourceByServiceUuidExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");


    }

    @Test
    public void getVnfResourceByVnfUuidTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");
        assertEquals("123-uuid", vnfResource.getModelUuid());


    }

    @Test
    public void getVnfResourceByVnfUuidNURExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(NonUniqueResultException.class);
        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");


    }

    @Test
    public void getVnfResourceByVnfUuidHibernateExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(HibernateException.class);
        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");


    }

    @Test
    public void getVnfResourceByVnfUuidExceptionTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        thrown.expect(Exception.class);
        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");


    }

    @Test
    public void getVfModuleByModelInvariantUuidTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() throws Exception {
                VfModule vfModule = new VfModule();
                vfModule.setModelUUID("123-uuid");
                return Arrays.asList(vfModule);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleByModelInvariantUuid("4993493");
        assertEquals("123-uuid", vfModule.getModelUUID());


    }

    @Test
    public void getVfModuleByModelInvariantUuidEmptyTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() throws Exception {
                return Collections.emptyList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleByModelInvariantUuid("4993493");
        assertEquals(null, vfModule);


    }

    @Test
    public void getVfModuleByModelCustomizationUuidTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	VfModuleCustomization vc = new VfModuleCustomization();
                vc.setModelCustomizationUuid("4993493");
                return vc;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
        assertEquals("4993493", vnf.getModelCustomizationUuid());
    }
    
    @Test
    public void getVfModuleByModelCustomizationUuidNullTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
        assertNull(vnf);
    }
    
    @Test
    public void getVfModuleByModelCustomizationUuidNonUniqueExceptionTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new org.hibernate.NonUniqueResultException(2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(org.hibernate.NonUniqueResultException.class);
        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
    }
    
    @Test
    public void getVfModuleByModelCustomizationUuidHibernateExceptionTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new org.hibernate.HibernateException("test case");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(org.hibernate.HibernateException.class);
        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
    }
    
    @Test
    public void getVfModuleByModelCustomizationUuidExceptionTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
                throw new NullPointerException();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(NullPointerException.class);
        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
    }
    
    @Test
    public void getVfModuleCustomizationByModelCustomizationIdTest(){
        mockUpQuery = new MockUp<Query>() {
    		String modelCustomizationUuid = null;

    		@Mock
    		public Query setParameter(String name, Object val) {
    			if (name.equals("modelCustomizationUuid")) modelCustomizationUuid = (String)val;
    			return this.getMockInstance();
    		}

            @Mock
            public Object uniqueResult() {
    			if ("nil".equals(modelCustomizationUuid)) return null;
    			if ("multi".equals(modelCustomizationUuid)) throw new org.hibernate.NonUniqueResultException(2);
    			if ("he".equals(modelCustomizationUuid)) throw new org.hibernate.HibernateException("test case");
    			if ("npe".equals(modelCustomizationUuid)) throw new NullPointerException();

    			VfModuleCustomization vm = new VfModuleCustomization();
            	vm.setModelCustomizationUuid("4993493");
                return vm;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModuleCustomization vnf = cd.getVfModuleCustomizationByModelCustomizationId("4993493");
        assertEquals("4993493", vnf.getModelCustomizationUuid());
        
        vnf = cd.getVfModuleCustomizationByModelCustomizationId("nil");
        assertNull(vnf);
        

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.NonUniqueResultException.class);
    		vnf = cd.getVfModuleCustomizationByModelCustomizationId("multi");
    	} catch (org.hibernate.NonUniqueResultException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.HibernateException.class);
    		vnf = cd.getVfModuleCustomizationByModelCustomizationId("he");
    	} catch (org.hibernate.HibernateException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(NullPointerException.class);
    		vnf = cd.getVfModuleCustomizationByModelCustomizationId("npe");
    	} catch (NullPointerException e) {
    		// noop
    	}

    }

    @Test
    public void getVfModuleByModelUuidTestException(){
    	mockUpQuery = new MockUp<Query>() {
    		String modelUuidValue = null;

    		@Mock
    		public Query setParameter(String name, Object val) {
    			if (name.equals("modelUuidValue")) modelUuidValue = (String)val;
    			return this.getMockInstance();
    		}

    		@Mock
    		public List<VfModule> list() {
    			if ("nil".equals(modelUuidValue)) return null;
    			if ("multi".equals(modelUuidValue)) throw new org.hibernate.NonUniqueResultException(2);
    			if ("he".equals(modelUuidValue)) throw new org.hibernate.HibernateException("test case");
    			if ("npe".equals(modelUuidValue)) throw new NullPointerException();

    			VfModule vfModule = new VfModule();
    			vfModule.setModelInvariantUuid(modelUuidValue);
    			return Arrays.asList(vfModule);
    		}
    	};

    	mockedSession = new MockUp<Session>() {
    		@Mock
    		public Query createQuery(String hql) {
    			return mockUpQuery.getMockInstance();
    		}
    	};

    	mockCd = new MockUp<CatalogDatabase>() {
    		@Mock
    		private Session getSession() {
    			return mockedSession.getMockInstance();
    		}
    	};

    	VfModule vnf = cd.getVfModuleByModelUuid("4993493");
    	assertEquals("4993493", vnf.getModelInvariantUuid());

    	vnf = cd.getVfModuleByModelUuid("nil");
    	assertNull(vnf);

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.NonUniqueResultException.class);
    		vnf = cd.getVfModuleByModelUuid("multi");
    	} catch (org.hibernate.NonUniqueResultException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.HibernateException.class);
    		vnf = cd.getVfModuleByModelUuid("he");
    	} catch (org.hibernate.HibernateException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(NullPointerException.class);
    		vnf = cd.getVfModuleByModelUuid("npe");
    	} catch (NullPointerException e) {
    		// noop
    	}
    }
    @Test
    public void getVnfResourceCustomizationByModelCustomizationUuidTestException(){
    	thrown.expect(Exception.class);
        VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelCustomizationUuid("4993493");
    }
    @Test
    public void getVnfResourceCustomizationByModelVersionIdTestException(){
    	thrown.expect(Exception.class);
        VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelVersionId("4993493");
    }
    @Test
    public void getVfModuleByModelCustomizationIdAndVersionTestException(){
    	thrown.expect(Exception.class);
        cd.getVfModuleByModelCustomizationIdAndVersion("4993493","test");
    }
    @Test
    public void getVfModuleByModelCustomizationIdModelVersionAndModelInvariantIdTestException(){
    	thrown.expect(Exception.class);
        cd.getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId("4993493","vnf","test");
    }
    @Test
    public void getVnfResourceCustomizationByModelInvariantIdTest(){
    	thrown.expect(Exception.class);
        cd.getVnfResourceCustomizationByModelInvariantId("4993493","vnf","test");
    }
    @Test
    public void getVfModuleCustomizationByVnfModuleCustomizationUuidTest(){
    	mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() throws Exception {
                return Collections.emptyList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertEquals(cd.getVfModuleCustomizationByVnfModuleCustomizationUuid("4993493").size(), 0);
    }
    @Test
    public void getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionIdTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResourceCustomization> list() {
                VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("SELECT vrc FROM VnfResourceCustomization as vrc WHERE vrc.vnfResourceModelUuid IN (SELECT vr.modelUuid FROM VnfResource vr WHERE vr.modelUuid = :modelVersionId)AND vrc.modelInstanceName = :modelCustomizationName"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("modelCustomizationName","modelVersionId"));

        VnfResourceCustomization result = cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("4993493", "test");
        assertNotNull(result);
    }

    @Test
    public void getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId_NullReturnTest(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResourceCustomization> list() {
                return Arrays.asList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("SELECT vrc FROM VnfResourceCustomization as vrc WHERE vrc.vnfResourceModelUuid IN (SELECT vr.modelUuid FROM VnfResource vr WHERE vr.modelUuid = :modelVersionId)AND vrc.modelInstanceName = :modelCustomizationName"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResourceCustomization result = cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("4993493", "test");
        assertNull(result);
    }
    @Test
    public void getAllVfModuleCustomizationstest(){
    	thrown.expect(Exception.class);
        cd.getAllVfModuleCustomizations("4993493");
    }
    @Test
    public void getVnfResourceByModelUuidTest(){
    	thrown.expect(Exception.class);
        cd.getVnfResourceByModelUuid("4993493");
    }
    @Test
    public void getVnfResCustomToVfModuleTest(){
    	thrown.expect(Exception.class);
        cd.getVnfResCustomToVfModule("4993493","test");
    }
    @Test
    public void getVfModulesForVnfResourceTest(){
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelUuid("48839");
        thrown.expect(Exception.class);
        cd.getVfModulesForVnfResource(vnfResource);
    }
    @Test
    public void getVfModulesForVnfResource2Test(){
    	thrown.expect(Exception.class);
        cd.getVfModulesForVnfResource("4993493");
    }
    @Test
    public void getServiceByUuidTest(){
    	thrown.expect(Exception.class);
        cd.getServiceByUuid("4993493");
    }
    @Test
    public void getNetworkResourceById2Test(){
    	thrown.expect(Exception.class);
        cd.getNetworkResourceById(4993493);
    }

    @Test
    public void getNetworkResourceByIdTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	VfModule vm = new VfModule();
            	vm.setModelInvariantUuid("4993493");
            	return vm;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vm = cd.getVfModuleTypeByUuid("4993493");
        assertEquals("4993493", vm.getModelInvariantUuid());
    }

    @Test
    public void getNetworkResourceByIdNullTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vm = cd.getVfModuleTypeByUuid("4993493");
        assertNull(null);
    }

    @Test
    public void getNetworkResourceByIdNonUniqueTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new org.hibernate.NonUniqueResultException(2);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(org.hibernate.NonUniqueResultException.class);
        VfModule vm = cd.getVfModuleTypeByUuid("4993493");
    }

    @Test
    public void getNetworkResourceByIdHibernateExceptionTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new org.hibernate.HibernateException("test case");
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(org.hibernate.HibernateException.class);
        VfModule vm = cd.getVfModuleTypeByUuid("4993493");
    }

    @Test
    public void getNetworkResourceByIdExceptionTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public Object uniqueResult() {
            	throw new NullPointerException();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        thrown.expect(NullPointerException.class);
        VfModule vm = cd.getVfModuleTypeByUuid("4993493");
    }

    @Test
    public void isEmptyOrNullTest(){
        boolean is = cd.isEmptyOrNull("4993493");
        assertFalse(is);
    }
    @Test
    public void getSTRTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<ServiceToResourceCustomization> list() {
                ServiceToResourceCustomization vnfResourceCustomization = new ServiceToResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<ServiceToResourceCustomization> str = cd.getSTR("4993493", "test", "vnf");
        assertFalse(str.isEmpty());

    }
    @Test
    public void getVRCtoVFMCTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResCustomToVfModuleCustom> list() {
                VnfResCustomToVfModuleCustom vnfResourceCustomization = new VnfResCustomToVfModuleCustom();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM VnfResCustomToVfModuleCustom WHERE vnfResourceCustModelCustomizationUuid = :vrc_mcu AND vfModuleCustModelCustomizationUuid = :vfmc_mcu"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<VnfResCustomToVfModuleCustom> vrCtoVFMC = cd.getVRCtoVFMC("4993493", "388492");
        assertFalse(vrCtoVFMC.isEmpty());
    }
    @Test
    public void getTempNetworkHeatTemplateLookupTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<TempNetworkHeatTemplateLookup> list() {
                TempNetworkHeatTemplateLookup vnfResourceCustomization = new TempNetworkHeatTemplateLookup();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<TempNetworkHeatTemplateLookup> tempNetworkHeatTemplateLookup = cd.getTempNetworkHeatTemplateLookup("4993493");
        assertFalse(tempNetworkHeatTemplateLookup.isEmpty());
    }

    @Test
    public void getAllNetworksByServiceModelUuidTest(){
    	mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() throws Exception {
                return Collections.emptyList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertEquals(cd.getAllNetworksByServiceModelUuid("4993493").size(), 0);
    }
    @Test
    public void getAllNetworksByServiceModelInvariantUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllNetworksByServiceModelInvariantUuid("4993493");
    }
    @Test
    public void getAllNetworksByServiceModelInvariantUuid2Test(){
    	thrown.expect(Exception.class);
        cd.getAllNetworksByServiceModelInvariantUuid("4993493","test");
    }
    @Test
    public void getAllNetworksByNetworkModelCustomizationUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllNetworksByNetworkModelCustomizationUuid("4993493");
    }
    @Test
    public void getAllNetworksByNetworkTypeTest(){
    	thrown.expect(Exception.class);
        cd.getAllNetworksByNetworkType("4993493");
    }
    @Test
    public void getAllVfmcForVrcTest(){
        VnfResourceCustomization re = new VnfResourceCustomization();
        re.setModelCustomizationUuid("377483");
        thrown.expect(Exception.class);
        cd.getAllVfmcForVrc(re);
    }
    @Test
    public void getAllVnfsByServiceModelUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllVnfsByServiceModelUuid("4993493");
    }
    @Test
    public void getAllVnfsByServiceModelInvariantUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllVnfsByServiceModelInvariantUuid("4993493");
    }
    @Test
    public void getAllVnfsByServiceModelInvariantUuid2Test(){
    	thrown.expect(Exception.class);
        cd.getAllVnfsByServiceModelInvariantUuid("4993493","test");
    }
    @Test
    public void getAllVnfsByServiceNameTest(){
    	thrown.expect(Exception.class);
        cd.getAllVnfsByServiceName("4993493","test");
    }
    @Test
    public void getAllVnfsByServiceName2Test(){
    	thrown.expect(Exception.class);
        cd.getAllVnfsByServiceName("4993493");
    }
    @Test
    public void getAllVnfsByVnfModelCustomizationUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllVnfsByVnfModelCustomizationUuid("4993493");
    }
    @Test
    public void getAllAllottedResourcesByServiceModelUuidTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<ServiceToResourceCustomization> list() {
                ServiceToResourceCustomization vnfResourceCustomization = new ServiceToResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockUpQuery2 = new MockUp<Query>() {

            @Mock
            public List<AllottedResourceCustomization> list() {
                AllottedResourceCustomization vnfResourceCustomization = new AllottedResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockUpQuery3 = new MockUp<Query>() {

            @Mock
            public List<AllottedResource> list() {
                AllottedResource vnfResourceCustomization = new AllottedResource();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                if(hql.contains("ServiceToResourceCustomization")){
                    return mockUpQuery.getMockInstance();

                }else if(hql.contains("AllottedResource " )){
                    return mockUpQuery3.getMockInstance();

                } else{
                    return mockUpQuery2.getMockInstance();
                }


            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<AllottedResourceCustomization> allAllottedResourcesByServiceModelUuid = cd.getAllAllottedResourcesByServiceModelUuid("4993493");
        assertFalse(allAllottedResourcesByServiceModelUuid.isEmpty());
    }
    @Test
    public void getAllAllottedResourcesByServiceModelInvariantUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493");
    }
    @Test
    public void getAllAllottedResourcesByServiceModelInvariantUuid2Test(){

        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<ServiceToResourceCustomization> list() {
                ServiceToResourceCustomization vnfResourceCustomization = new ServiceToResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockUpQuery2 = new MockUp<Query>() {

            @Mock
            public List<AllottedResourceCustomization> list() {
                AllottedResourceCustomization vnfResourceCustomization = new AllottedResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockUpQuery3 = new MockUp<Query>() {

            @Mock
            public List<AllottedResource> list() {
                AllottedResource vnfResourceCustomization = new AllottedResource();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockUpQuery4 = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service vnfResourceCustomization = new Service();
                return Arrays.asList(vnfResourceCustomization);
            }
        };
        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                if(hql.contains("ServiceToResourceCustomization")){
                    return mockUpQuery.getMockInstance();

                }else if(hql.contains("AllottedResource " )){
                    return mockUpQuery3.getMockInstance();

                } else if(hql.contains(" Service ")){
                    return mockUpQuery4.getMockInstance();
                }else{
                    return mockUpQuery2.getMockInstance();
                }


            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };


        List<AllottedResourceCustomization> allottedResourceCustomizations = cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493", "test");
        assertFalse(allottedResourceCustomizations.isEmpty());
    }
    @Test
    public void getAllAllottedResourcesByArModelCustomizationUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllAllottedResourcesByArModelCustomizationUuid("4993493");
    }
    @Test
    public void getAllottedResourceByModelUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllottedResourceByModelUuid("4993493");
    }
    @Test
    public void getAllResourcesByServiceModelUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllResourcesByServiceModelUuid("4993493");
    }
    @Test
    public void getAllResourcesByServiceModelInvariantUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllResourcesByServiceModelInvariantUuid("4993493");
    }

    @Test
    public void getAllResourcesByServiceModelInvariantUuid2Test(){
    	thrown.expect(Exception.class);
        cd.getAllResourcesByServiceModelInvariantUuid("4993493","test");
    }
    @Test
    public void getSingleNetworkByModelCustomizationUuidTest(){
    	thrown.expect(Exception.class);
        cd.getSingleNetworkByModelCustomizationUuid("4993493");
    }
    @Test
    public void getSingleAllottedResourceByModelCustomizationUuidTest(){
    	thrown.expect(Exception.class);
        cd.getSingleAllottedResourceByModelCustomizationUuid("4993493");
    }
    @Test
    public void getVfModuleRecipeTest(){
    	thrown.expect(Exception.class);
        cd.getVfModuleRecipe("4993493","test","get");
    }
    @Test
    public void getVfModuleTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() {
                VfModule vnfResourceCustomization = new VfModule();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<VfModule> vfModule = cd.getVfModule("4993493", "test", "get", "v2", "vnf");
        assertFalse(vfModule.isEmpty());
    }
    @Test
    public void getVnfComponentsRecipeTest(){
    	thrown.expect(Exception.class);
        cd.getVnfComponentsRecipe("4993493","test","v2","vnf","get","3992");
    }
    @Test
    public void getVnfComponentsRecipeByVfModuleTest(){
        List <VfModule> resultList = new ArrayList<>();
        VfModule m = new VfModule();
        resultList.add(m);
        thrown.expect(Exception.class);
        cd.getVnfComponentsRecipeByVfModule(resultList,"4993493");
    }
    @Test
    public void getAllVnfResourcesTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResource> list() {
                VnfResource vnfResourceCustomization = new VnfResource();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM VnfResource"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<VnfResource> allVnfResources = cd.getAllVnfResources();
        assertFalse(allVnfResources.isEmpty());
    }
    @Test
    public void getVnfResourcesByRoleTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResource> list() {
                VnfResource vnfResourceCustomization = new VnfResource();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM VnfResource WHERE vnfRole = :vnfRole"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<VnfResource> vnfResourcesByRole = cd.getVnfResourcesByRole("4993493");
        assertFalse(vnfResourcesByRole.isEmpty());
    }
    @Test
    public void getVnfResourceCustomizationsByRoleTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResourceCustomization> list() {
                VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM VnfResourceCustomization WHERE nfRole = :vnfRole"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<VnfResourceCustomization> vnfResourceCustomizationsByRole = cd.getVnfResourceCustomizationsByRole("4993493");
        assertFalse(vnfResourceCustomizationsByRole.isEmpty());
    }
    @Test
    public void getAllNetworkResourcesTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<NetworkResource> list() {
                NetworkResource vnfResourceCustomization = new NetworkResource();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM NetworkResource"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<NetworkResource> allNetworkResources = cd.getAllNetworkResources();
        assertFalse(allNetworkResources.isEmpty());
    }
    @Test
    public void getAllNetworkResourceCustomizationsTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<NetworkResourceCustomization> list() {
                NetworkResourceCustomization vnfResourceCustomization = new NetworkResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM NetworkResourceCustomization"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<NetworkResourceCustomization> allNetworkResourceCustomizations = cd.getAllNetworkResourceCustomizations();
        assertFalse(allNetworkResourceCustomizations.isEmpty());
    }
    @Test
    public void getAllVfModulesTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() {
                VfModule vnfResourceCustomization = new VfModule();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM VfModule"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<VfModule> allVfModules = cd.getAllVfModules();
        assertFalse(allVfModules.isEmpty());
    }
    @Test
    public void getAllVfModuleCustomizationsTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModuleCustomization> list() {
                VfModuleCustomization vnfResourceCustomization = new VfModuleCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM VfModuleCustomization"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<VfModuleCustomization> allVfModuleCustomizations = cd.getAllVfModuleCustomizations();
        assertFalse(allVfModuleCustomizations.isEmpty());
    }
    @Test
    public void getAllHeatEnvironmentTest(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<HeatEnvironment> list() {
                HeatEnvironment vnfResourceCustomization = new HeatEnvironment();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                assertTrue(hql.contains("FROM HeatEnvironment"));
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<HeatEnvironment> allHeatEnvironment = cd.getAllHeatEnvironment();
        assertFalse(allHeatEnvironment.isEmpty());
    }
    @Test
    public void getHeatEnvironment2Test(){
    	thrown.expect(Exception.class);
        cd.getHeatEnvironment(4993493);
    }
    @Test
    public void getNestedTemplatesTest(){
    	thrown.expect(Exception.class);
        cd.getNestedTemplates(4993493);
    }
    @Test
    public void getNestedTemplates2Test(){
    	thrown.expect(Exception.class);
        cd.getNestedTemplates("4993493");
    }
    @Test
    public void getHeatFilesTest(){
    	thrown.expect(Exception.class);
        cd.getHeatFiles(4993493);
    }
    @Test
    public void getVfModuleToHeatFilesEntryTest(){
    	thrown.expect(Exception.class);
        cd.getVfModuleToHeatFilesEntry("4993493","49959499");
    }
    @Test
    public void getServiceToResourceCustomization(){
    	thrown.expect(Exception.class);
        cd.getServiceToResourceCustomization("4993493","599349","49900");
    }
    @Test
    public void getHeatFilesForVfModuleTest(){
    	thrown.expect(Exception.class);
        cd.getHeatFilesForVfModule("4993493");
    }
    @Test
    public void getHeatTemplateTest(){
    	thrown.expect(Exception.class);
        cd.getHeatTemplate("4993493","test","heat");
    }

    @Test
    public void saveHeatTemplateTest(){
        HeatTemplate heat = new HeatTemplate();
        Set <HeatTemplateParam> paramSet = new HashSet<>();
        thrown.expect(Exception.class);
        cd.saveHeatTemplate(heat,paramSet);
    }
    @Test
    public void getHeatEnvironmentTest(){
    	
    	mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                return null;
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertEquals(cd.getHeatEnvironment("4993493","test","heat"), null);
    }
    @Test
    public void getHeatEnvironment3Test(){
    	thrown.expect(Exception.class);
        cd.getHeatEnvironment("4993493","test");
    }
    @Test
    public void saveHeatEnvironmentTest(){
        HeatEnvironment en = new HeatEnvironment();
        thrown.expect(Exception.class);
        cd.saveHeatEnvironment(en);
    }
    @Test
    public void saveHeatTemplate2Test(){
        HeatTemplate heat = new HeatTemplate();
        thrown.expect(Exception.class);
        cd.saveHeatTemplate(heat);
    }
    @Test
    public void saveHeatFileTest(){
        HeatFiles hf = new HeatFiles();
        thrown.expect(Exception.class);
        cd.saveHeatFile(hf);
    }
    @Test
    public void saveVnfRecipeTest(){
        VnfRecipe vr = new VnfRecipe();
        thrown.expect(Exception.class);
        cd.saveVnfRecipe(vr);
    }
    @Test
    public void saveVnfComponentsRecipe(){
        VnfComponentsRecipe vr = new VnfComponentsRecipe();
        thrown.expect(Exception.class);
        cd.saveVnfComponentsRecipe(vr);
    }
    @Test
    public void saveOrUpdateVnfResourceTest(){
        VnfResource vr = new VnfResource();
        thrown.expect(Exception.class);
        cd.saveOrUpdateVnfResource(vr);
    }
    @Test
    public void saveVnfResourceCustomizationTest(){
        VnfResourceCustomization vr = new VnfResourceCustomization();
        thrown.expect(Exception.class);
        cd.saveVnfResourceCustomization(vr);
    }
    @Test
    public void saveAllottedResourceCustomizationTest(){
        AllottedResourceCustomization arc = new AllottedResourceCustomization();
        thrown.expect(Exception.class);
        cd.saveAllottedResourceCustomization(arc);
    }
    @Test
    public void saveAllottedResourceTest(){
        AllottedResource ar = new AllottedResource();
        thrown.expect(Exception.class);
        cd.saveAllottedResource(ar);
    }
    @Test
    public void saveNetworkResourceTest() throws RecordNotFoundException {
        NetworkResource nr = new NetworkResource();
        thrown.expect(Exception.class);
        cd.saveNetworkResource(nr);
    }
    @Test
    public void saveToscaCsarTest()throws RecordNotFoundException {
        ToscaCsar ts = new ToscaCsar();
        thrown.expect(Exception.class);
        cd.saveToscaCsar(ts);
    }
    @Test
    public void getToscaCsar(){
    	thrown.expect(Exception.class);
        cd.getToscaCsar("4993493");
    }
    @Test
    public void saveTempNetworkHeatTemplateLookupTest(){
        TempNetworkHeatTemplateLookup t = new TempNetworkHeatTemplateLookup();
        thrown.expect(Exception.class);
        cd.saveTempNetworkHeatTemplateLookup(t);
    }
    @Test
    public void saveVnfResourceToVfModuleCustomizationTest() throws RecordNotFoundException {
        VnfResourceCustomization v =new VnfResourceCustomization();
        VfModuleCustomization vm = new VfModuleCustomization();
        thrown.expect(Exception.class);
        cd.saveVnfResourceToVfModuleCustomization(v, vm);
    }
    @Test
    public void saveNetworkResourceCustomizationTest() throws RecordNotFoundException {
        NetworkResourceCustomization nrc = new NetworkResourceCustomization();
        thrown.expect(Exception.class);
        cd.saveNetworkResourceCustomization(nrc);
    }

    @Test
    public void saveServiceToNetworksTest(){
        AllottedResource ar = new AllottedResource();
        thrown.expect(Exception.class);
        cd.saveAllottedResource(ar);
    }
    @Test
    public void saveServiceToResourceCustomizationTest(){
        ServiceToResourceCustomization ar = new ServiceToResourceCustomization();
        thrown.expect(Exception.class);
        cd.saveServiceToResourceCustomization(ar);
    }
    @Test
    public void saveServiceTest(){
        Service ar = new Service();
        thrown.expect(Exception.class);
        cd.saveService(ar);
    }
    @Test
    public void saveOrUpdateVfModuleTest(){
        VfModule ar = new VfModule();
        thrown.expect(Exception.class);
        cd.saveOrUpdateVfModule(ar);
    }
    @Test
    public void saveOrUpdateVfModuleCustomizationTest(){
        VfModuleCustomization ar = new VfModuleCustomization();
        thrown.expect(Exception.class);
        cd.saveOrUpdateVfModuleCustomization(ar);
    }

    @Test
    public void getNestedHeatTemplateTest(){
    	thrown.expect(Exception.class);
        cd.getNestedHeatTemplate(101,201);
    }
    @Test
    public void getNestedHeatTemplate2Test(){
    	thrown.expect(Exception.class);
        cd.getNestedHeatTemplate("1002","1002");
    }
    @Test
    public void saveNestedHeatTemplateTest(){
        HeatTemplate ar = new HeatTemplate();
        thrown.expect(Exception.class);
        cd.saveNestedHeatTemplate("1001",ar,"test");
    }
    @Test
    public void getHeatFiles2Test(){
        VfModuleCustomization ar = new VfModuleCustomization();
        thrown.expect(Exception.class);
        cd.getHeatFiles(101,"test","1001","v2");
    }
    @Test
    public void getHeatFiles3Test(){
        VfModuleCustomization ar = new VfModuleCustomization();
        thrown.expect(Exception.class);
        cd.getHeatFiles("200192");
    }
    @Test
    public void saveHeatFilesTest(){
        HeatFiles ar = new HeatFiles();
        thrown.expect(Exception.class);
        cd.saveHeatFiles(ar);
    }
    @Test
    public void saveVfModuleToHeatFilesTest(){
        HeatFiles ar = new HeatFiles();
        thrown.expect(Exception.class);
        cd.saveVfModuleToHeatFiles("3772893",ar);
    }
    @Test
    public void getNetworkResourceByModelUuidTest(){

        cd.getNetworkResourceByModelUuid("3899291");
    }
    @Test
    public void getNetworkRecipeTest(){
    	thrown.expect(Exception.class);
        cd.getNetworkRecipe("test","test1","test2");
    }
    @Test
    public void getNetworkRecipe2Test(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkRecipe> list() {
                NetworkRecipe heatTemplate = new NetworkRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        NetworkRecipe networkRecipe = cd.getNetworkRecipe("test","test1");assertNotNull(networkRecipe);
        assertNotNull(networkRecipe);

    }
    @Test
    public void getNetworkResourceByModelCustUuidTest(){

        cd.getNetworkResourceByModelCustUuid("test");
    }

    @Test
    public void getVnfComponentsRecipeByVfModuleModelUUIdTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfComponentsRecipe> list() {
                VnfComponentsRecipe heatTemplate = new VnfComponentsRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfComponentsRecipe vnfComponentsRecipeByVfModuleModelUUId = cd.getVnfComponentsRecipeByVfModuleModelUUId("test1", "test2", "test3");
        assertNotNull(vnfComponentsRecipeByVfModuleModelUUId);
    }
    @Test
    public void getVnfComponentRecipesTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfComponentsRecipe> list() {
                VnfComponentsRecipe heatTemplate = new VnfComponentsRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<VnfComponentsRecipe> test = cd.getVnfComponentRecipes("test");
        assertNotNull(test);
        assertFalse(test.isEmpty());
    }
    @Test
    public void saveOrUpdateVnfComponentTest(){
        VnfComponent ar = new VnfComponent();
        thrown.expect(Exception.class);
        cd.saveOrUpdateVnfComponent(ar);
    }

    @Test
    public void getVfModule2Test(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModule> list() {
                VfModule heatTemplate = new VfModule();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VfModule test = cd.getVfModule("test");
        assertNotNull(test);
    }
    @Test
    public void getVfModuleByModelUUIDTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModule> list() {
                VfModule heatTemplate = new VfModule();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule test = cd.getVfModuleByModelUUID("test");
        assertNotNull(test);
    }
    @Test
    public void getServiceRecipeByModelUUIDTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() {
                ServiceRecipe heatTemplate = new ServiceRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Assert.assertNotNull(cd.getServiceRecipeByModelUUID("test1", "test2"));
    }
    @Test
    public void getModelRecipeTest(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<Object> list() {
                return new ArrayList();
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Assert.assertNull(cd.getModelRecipe("test1", "test2", "test3"));
    }
    @Test
    @Ignore
    public void healthCheck(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createSQLQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }

        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.healthCheck());
    }
    @Test
    public void executeQuerySingleRow(){
        VnfComponent ar = new VnfComponent();
        HashMap<String, String> variables = new HashMap<>();
        thrown.expect(Exception.class);
        cd.executeQuerySingleRow("tets",variables,false);
    }

    @Test
    public void executeQueryMultipleRows(){
        HashMap<String, String> variables = new HashMap<>();

        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };


        List<Object> select = cd.executeQueryMultipleRows("select", variables, false);
        assertFalse(select.isEmpty());
    }

    @Test
    public void getArRecipeByNameVersion(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ArRecipe> list() {
                ArRecipe arRecipe = new ArRecipe();
                return Arrays.asList(arRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.getArRecipeByNameVersion("select","variables","String"));
    }
    @Test
    public void getVnfComponentsRecipe(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfComponentsRecipe> list() {
                VnfComponentsRecipe heatTemplate = new VnfComponentsRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.getVnfComponentsRecipe("vnfType","vnfComponentType","action","serviceType"));
    }
    @Test
    public void getNetworkRecipeByNameVersion(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkRecipe> list() {
                NetworkRecipe heatTemplate = new NetworkRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.getNetworkRecipeByNameVersion("modelName","modelVersion","action"));
    }
    @Test
    public void saveOrUpdateVfModuleCustomization(){
        mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkRecipe> list() {
                NetworkRecipe heatTemplate = new NetworkRecipe();
                return Arrays.asList(heatTemplate);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setHeatEnvironmentArtifactUuid("HeatEnvironmentArtifactUuid");
        vfModuleCustomization.setVolEnvironmentArtifactUuid("VolEnvironmentArtifactUuid");
        vfModuleCustomization.setVfModuleModelUuid("VfModuleModelUuid");
        vfModuleCustomization.setModelCustomizationUuid("ModelCustomizationUuid");
        cd.saveOrUpdateVfModuleCustomization(vfModuleCustomization);
    }
    @Test
    public void saveServiceToNetworks(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        ServiceToNetworks serviceToNetworks = new ServiceToNetworks();
        cd.saveServiceToNetworks(serviceToNetworks);
    }
    @Test
    public void saveVfModuleToHeatFiles() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModuleToHeatFiles vfModuleToHeatFiles = new VfModuleToHeatFiles();

        cd.saveVfModuleToHeatFiles(vfModuleToHeatFiles);
    }
    @Test
    public void saveTempNetworkHeatTemplateLookup() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup = new TempNetworkHeatTemplateLookup();

        cd.saveTempNetworkHeatTemplateLookup(tempNetworkHeatTemplateLookup);
    }
    @Test
    public void getToscaCsarByServiceModelUUID() {
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };


        assertNull(cd.getToscaCsarByServiceModelUUID("uuid-123"));
        cd.close();
    }
    @Test
    public void getVnfRecipeByNameVersion(){
        mockUpQuery = new MockUp<Query>() {
        	String version = null;
        	
        	@Mock
        	public Query setParameter(String name, Object val) {
        		if (name.equals("version")) version = (String)val;
        		return this.getMockInstance();
        	}

            @Mock
            public List<VnfRecipe> list() {
            	if ("nil".equals(version)) return new ArrayList<VnfRecipe>();
            	
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setVersion(version);
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        
        VnfRecipe vf = cd.getVnfRecipeByNameVersion("modelName","modelVersion","action");
        assertEquals("modelVersion", vf.getVersion());
        
        vf = cd.getVnfRecipeByNameVersion("modelName","nil","action");
        assertNull(vf);
    }
    
    @Test
    public void getVnfRecipeByModuleUuid(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfRecipe> list() {
                VnfRecipe vnfRecipe = new VnfRecipe();
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNull(cd.getVnfRecipeByModuleUuid("vnfModelUuid","action"));
    }
    
    @Test
    public void getVfModuleType(){
    	mockUpQuery = new MockUp<Query>() {
    		String version = null;

    		@Mock
    		public Query setParameter(String name, Object val) {
    			if (name.equals("version")) version = (String)val;
    			return this.getMockInstance();
    		}

    		@Mock
    		public Object uniqueResult() {
    			if ("nil".equals(version)) return null;
    			if ("multi".equals(version)) throw new org.hibernate.NonUniqueResultException(2);
    			if ("he".equals(version)) throw new org.hibernate.HibernateException("test case");
    			if ("npe".equals(version)) throw new NullPointerException();

    			VfModule vfModule = new VfModule();
    			vfModule.setVersion(version);
    			return vfModule;
    		}
    	};

    	mockedSession = new MockUp<Session>() {
    		@Mock
    		public Query createQuery(String hql) {
    			return mockUpQuery.getMockInstance();
    		}
    	};

    	mockCd = new MockUp<CatalogDatabase>() {
    		@Mock
    		private Session getSession() {
    			return mockedSession.getMockInstance();
    		}
    	};

    	VfModule vm = cd.getVfModuleType("type","version");
    	assertEquals("version", vm.getVersion());

    	vm = cd.getVfModuleType("type", "nil");
    	assertNull(vm);

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.NonUniqueResultException.class);
    		vm = cd.getVfModuleType("type", "multi");
    	} catch (org.hibernate.NonUniqueResultException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.HibernateException.class);
    		vm = cd.getVfModuleType("type", "he");
    	} catch (org.hibernate.HibernateException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(NullPointerException.class);
    		vm = cd.getVfModuleType("type", "npe");
    	} catch (NullPointerException e) {
    		// noop
    	}

    }
    @Test
    public void getVfModuleByModelInvariantUuidAndModelVersion(){
    	mockUpQuery = new MockUp<Query>() {
    		String version = null;

    		@Mock
    		public Query setParameter(String name, Object val) {
    			if (name.equals("modelVersion")) version = (String)val;
    			return this.getMockInstance();
    		}

    		@Mock
    		public Object uniqueResult() {
    			if ("nil".equals(version)) return null;
    			if ("multi".equals(version)) throw new org.hibernate.NonUniqueResultException(2);
    			if ("he".equals(version)) throw new org.hibernate.HibernateException("test case");
    			if ("npe".equals(version)) throw new NullPointerException();

    			VfModule vfModule = new VfModule();
    			vfModule.setVersion(version);
    			return vfModule;
    		}
    	};

    	mockedSession = new MockUp<Session>() {
    		@Mock
    		public Query createQuery(String hql) {
    			return mockUpQuery.getMockInstance();
    		}
    	};

    	mockCd = new MockUp<CatalogDatabase>() {
    		@Mock
    		private Session getSession() {
    			return mockedSession.getMockInstance();
    		}
    	};

        VfModule vm = cd.getVfModuleByModelInvariantUuidAndModelVersion("modelInvariantUuid","modelVersion");
    	assertEquals("modelVersion", vm.getVersion());

    	vm = cd.getVfModuleByModelInvariantUuidAndModelVersion("modelInvariantUuid","nil");
    	assertNull(vm);

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.NonUniqueResultException.class);
    		vm = cd.getVfModuleByModelInvariantUuidAndModelVersion("modelInvariantUuid","multi");
    	} catch (org.hibernate.NonUniqueResultException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(org.hibernate.HibernateException.class);
    		vm = cd.getVfModuleByModelInvariantUuidAndModelVersion("modelInvariantUuid","he");
    	} catch (org.hibernate.HibernateException e) {
    		// noop
    	}

    	try {
    		thrown = ExpectedException.none();
    		thrown.expect(NullPointerException.class);
    		vm = cd.getVfModuleByModelInvariantUuidAndModelVersion("modelInvariantUuid","npe");
    	} catch (NullPointerException e) {
    		// noop
    	}

    }
    
    @Test
    public void getVnfResourceCustomizationByModelCustomizationUuid(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfRecipe> list() {
                VnfRecipe vnfRecipe = new VnfRecipe();
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNull(cd.getVnfResourceCustomizationByModelCustomizationUuid("modelCustomizationUuid"));
    }
    @Test
    public void getVfModuleByModelCustomizationIdAndVersion(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfRecipe> list() {
                VnfRecipe vnfRecipe = new VnfRecipe();
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNull(cd.getVfModuleByModelCustomizationIdAndVersion("modelCustomizationUuid","modelVersionId"));
    }
    @Test
    public void getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfRecipe> list() {
                VnfRecipe vnfRecipe = new VnfRecipe();
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNull(cd.getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId("modelCustomizationUuid","modelVersion","modelInvariantId"));
    }
    @Test
    public void getVnfResourceCustomizationByModelInvariantId(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfRecipe> list() {
                VnfRecipe vnfRecipe = new VnfRecipe();
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNull(cd.getVnfResourceCustomizationByModelInvariantId("modelInvariantId","modelVersion","modelCustomizationName"));
    }
    @Test
    public void getVfModuleCustomizationByVnfModuleCustomizationUuid(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfRecipe> list() {
                VnfRecipe vnfRecipe = new VnfRecipe();
                return Arrays.asList(vnfRecipe);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.getVfModuleCustomizationByVnfModuleCustomizationUuid("modelCustomizationUuid"));
    }
    @Test
    public void getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId(){
        mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VnfResourceCustomization> list() {
                VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        mockCd = new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        assertNotNull(cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("modelCustomizationName","modelVersionId"));
    }
}
