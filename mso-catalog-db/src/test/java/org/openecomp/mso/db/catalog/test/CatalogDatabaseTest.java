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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VfModuleToHeatFiles;
import org.openecomp.mso.db.catalog.beans.VnfComponent;
import org.openecomp.mso.db.catalog.beans.VnfComponentsRecipe;
import org.openecomp.mso.db.catalog.beans.VnfRecipe;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
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
    @Before
    public void setup(){
        cd = CatalogDatabase.getInstance();
    }
    
  
	 @After
	 public void tearDown() {
		 if (mockCd!=null) { mockCd.tearDown(); mockCd = null; }
		 if (mockedSession!=null) { mockedSession.tearDown(); mockedSession = null; }
		 if (mockUpQuery!=null) { mockUpQuery.tearDown(); mockUpQuery = null; }
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
    @Ignore
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
    public void getVnfComponentTestException() throws Exception{
    	thrown.expect(Exception.class);
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
        VnfResource vnfResource = cd.getVnfResource("vnf");
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
        VnfResource vnfResource = cd.getVnfResource("vnf");
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
    	thrown.expect(Exception.class);
        VnfResource vnf = cd.getVnfResourceById(19299);
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

            @Mock
            public Object uniqueResult() {
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
    	thrown.expect(Exception.class);
        VfModule vnf = cd.getVfModuleType("4993493");
    }

    @Test
    public void getVfModuleType2TestException(){
    	thrown.expect(Exception.class);
        VfModule vnf = cd.getVfModuleType("4993493","vnf");
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
    public void getVfModuleByModelCustomizationUuidTestException(){
    	thrown.expect(Exception.class);
        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
    }
    @Test
    public void getVfModuleByModelInvariantUuidAndModelVersionTestException(){
    	thrown.expect(Exception.class);
        VfModule vnf = cd.getVfModuleByModelInvariantUuidAndModelVersion("4993493","vnf");
    }
    @Test
    public void getVfModuleCustomizationByModelCustomizationIdTestException(){
    	thrown.expect(Exception.class);
        VfModuleCustomization vnf = cd.getVfModuleCustomizationByModelCustomizationId("4993493");
    }
    @Test
    public void getVfModuleByModelUuidTestException(){
    	thrown.expect(Exception.class);
        VfModule vnf = cd.getVfModuleByModelUuid("4993493");
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
    	thrown.expect(Exception.class);
        cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("4993493","test");
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
    	thrown.expect(Exception.class);
        cd.getVfModuleTypeByUuid("4993493");
    }
    @Test
    public void isEmptyOrNullTest(){
        boolean is = cd.isEmptyOrNull("4993493");
        assertFalse(is);
    }
    @Test
    public void getSTRTest(){
    	thrown.expect(Exception.class);
        cd.getSTR("4993493","test","vnf");
    }
    @Test
    public void getVRCtoVFMCTest(){
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
        assertEquals(cd.getVRCtoVFMC("4993493","388492").size(), 0);
    }
    @Test
    public void getVfModuleTypeByUuidTestException(){
    	thrown.expect(Exception.class);
        cd.getVfModuleTypeByUuid("4993493");
    }

    @Test
    public void getTempNetworkHeatTemplateLookupTest(){
    	thrown.expect(Exception.class);
        cd.getTempNetworkHeatTemplateLookup("4993493");
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
    	thrown.expect(Exception.class);
        cd.getAllAllottedResourcesByServiceModelUuid("4993493");
    }
    @Test
    public void getAllAllottedResourcesByServiceModelInvariantUuidTest(){
    	thrown.expect(Exception.class);
        cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493");
    }
    @Test
    public void getAllAllottedResourcesByServiceModelInvariantUuid2Test(){
    	thrown.expect(Exception.class);
        cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493","test");
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
        assertEquals(cd.getVfModule("4993493","test","get","v2","vnf").size(), 0);
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
    	thrown.expect(Exception.class);
        cd.getAllVnfResources();
    }
    @Test
    public void getVnfResourcesByRoleTest(){
    	thrown.expect(Exception.class);
        cd.getVnfResourcesByRole("4993493");
    }
    @Test
    public void getVnfResourceCustomizationsByRoleTest(){
    	thrown.expect(Exception.class);
        cd.getVnfResourceCustomizationsByRole("4993493");
    }
    @Test
    public void getAllNetworkResourcesTest(){
    	thrown.expect(Exception.class);
        cd.getAllNetworkResources();
    }
    @Test
    public void getAllNetworkResourceCustomizationsTest(){
    	thrown.expect(Exception.class);
        cd.getAllNetworkResourceCustomizations();
    }
    @Test
    public void getAllVfModulesTest(){
    	thrown.expect(Exception.class);
        cd.getAllVfModules();
    }
    @Test
    public void getAllVfModuleCustomizationsTest(){
    	thrown.expect(Exception.class);
        cd.getAllVfModuleCustomizations();
    }
    @Test
    public void getAllHeatEnvironmentTest(){
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
        assertEquals(cd.getAllHeatEnvironment().size(), 0);
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
    public void saveVfModuleToHeatFiles(){
        VfModuleToHeatFiles v = new VfModuleToHeatFiles();
        thrown.expect(Exception.class);
        cd.saveVfModuleToHeatFiles(v);
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
    	thrown.expect(Exception.class);
        cd.getNetworkRecipe("test","test1");
    }
    @Test
    public void getNetworkResourceByModelCustUuidTest(){

        cd.getNetworkResourceByModelCustUuid("test");
    }
    @Test
    public void getVnfComponentsRecipe2Test(){
    	thrown.expect(Exception.class);
        cd.getVnfComponentsRecipe("test1","test2","test3","test4");
    }
    @Test
    public void getVnfComponentsRecipeByVfModuleModelUUIdTest(){
    	thrown.expect(Exception.class);
        cd.getVnfComponentsRecipeByVfModuleModelUUId("test1","test2","test3");
    }
    @Test
    public void getVnfComponentRecipesTest(){
    	thrown.expect(Exception.class);
        cd.getVnfComponentRecipes("test");
    }
    @Test
    public void saveOrUpdateVnfComponentTest(){
        VnfComponent ar = new VnfComponent();
        thrown.expect(Exception.class);
        cd.saveOrUpdateVnfComponent(ar);
    }

    @Test
    public void getVfModule2Test(){
    	thrown.expect(Exception.class);
        cd.getVfModule("test");
    }
    @Test
    public void getVfModuleByModelUUIDTest(){
    	thrown.expect(Exception.class);
        cd.getVfModuleByModelUUID("test");
    }
    @Test
    public void getServiceRecipeByModelUUIDTest(){
    	thrown.expect(Exception.class);
        cd.getServiceRecipeByModelUUID("test1","test2");
    }
    @Test
    public void getModelRecipeTest(){
    	thrown.expect(Exception.class);
        cd.getModelRecipe("test1","test2","test3");
    }
    @Test
    public void healthCheck(){
    	thrown.expect(Exception.class);
        cd.healthCheck();
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
        thrown.expect(Exception.class);
        cd.executeQueryMultipleRows("select",variables,false);
    }
}
