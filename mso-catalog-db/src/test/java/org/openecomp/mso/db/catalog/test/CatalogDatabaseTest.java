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

import mockit.Mock;
import mockit.MockUp;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CatalogDatabaseTest {

    CatalogDatabase cd = null;

    @Before
    public void setup(){
        cd = CatalogDatabase.getInstance();
    }


    @Test
    public void getAllHeatTemplatesTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                return Arrays.asList(heatTemplate);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Object get(Class cls, Serializable id) {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("123-uuid");
                return heatTemplate;
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
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

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("1234-uuid");
                return Arrays.asList(heatTemplate);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Object get(Class cls, Serializable id) {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("123-uuid");
                return heatTemplate;
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("123-uuid");
                return heatTemplate;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");
        assertEquals("123-uuid", ht.getAsdcUuid());
    }

    @Test(expected = HibernateException.class)
    public void getHeatTemplateByArtifactUuidHibernateErrorTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");
    }

    @Test(expected = NonUniqueResultException.class)
    public void getHeatTemplateByArtifactUuidNonUniqueResultTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(2);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");
    }

    @Test(expected = Exception.class)
    public void getHeatTemplateByArtifactUuidGenericExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123-uuid");
    }

    @Test
    public void getParametersForHeatTemplateTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                HeatTemplate heatTemplate = new HeatTemplate();
                heatTemplate.setAsdcUuid("1234-uuid");
                return Arrays.asList(heatTemplate);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<HeatTemplateParam> htList = cd.getParametersForHeatTemplate("12l3");
        assertEquals(1, htList.size());
    }

    @Test(expected = HibernateException.class)
    public void getParametersForHeatTemplateHibernateExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<HeatTemplateParam> htList = cd.getParametersForHeatTemplate("12l3");
    }

    @Test(expected = Exception.class)
    public void getParametersForHeatTemplateExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<HeatTemplate> list() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        List<HeatTemplateParam> htList = cd.getParametersForHeatTemplate("12l3");
    }

    @Test
    public void getHeatEnvironmentByArtifactUuidTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                HeatEnvironment heatEnvironment = new HeatEnvironment();
                heatEnvironment.setArtifactUuid("123-uuid");
                return heatEnvironment;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");
        assertEquals("123-uuid", he.getArtifactUuid());
    }

    @Test(expected = HibernateException.class)
    public void getHeatEnvironmentByArtifactUuidHibernateExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");
    }

    @Test(expected = Exception.class)
    public void getHeatEnvironmentByArtifactUuidExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        HeatEnvironment he = cd.getHeatEnvironmentByArtifactUuid("123");
    }

    @Test
    public void getServiceByInvariantUUIDTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<Service> list() {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getService("123");
        assertEquals("123-uuid", service.getModelUUID());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getServiceNoUniqueResultTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getService("123");
    }

    @Test(expected = HibernateException.class)
    public void getServiceHibernateExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getService("123");
    }

    @Test(expected = Exception.class)
    public void getServiceExceptionTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception("generic exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        Service service = cd.getService("123");
    }

    @Test
    public void getServiceByModelUUIDTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<Service> list() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return Arrays.asList(service);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<Service> list() throws Exception {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                Service service = new Service();
                service.setModelUUID("123-uuid");
                return service;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        Service service = cd.getServiceByVersionAndInvariantId("123","tetwe");
        assertEquals("123-uuid", service.getModelUUID());
    }

    @Test(expected = Exception.class)
    public void getServiceByVersionAndInvariantIdNonUniqueResultTest() throws Exception{

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        Service service = cd.getServiceByVersionAndInvariantId("123","tetwe");
    }

    @Test(expected = Exception.class)
    public void getServiceRecipeTestException() throws Exception{
        ServiceRecipe ht = cd.getServiceRecipe("123","tetwe");
    }

    @Test
    public void getServiceRecipeByServiceModelUuidTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() throws Exception {
                ServiceRecipe serviceRecipe = new ServiceRecipe();
                serviceRecipe.setId(1);
                return Arrays.asList(serviceRecipe);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() throws Exception {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() {
                ServiceRecipe serviceRecipe = new ServiceRecipe();
                serviceRecipe.setId(1);
                return Arrays.asList(serviceRecipe);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<ServiceRecipe> list() {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        List<ServiceRecipe> serviceRecipes = cd.getServiceRecipes("123");
        assertEquals(0, serviceRecipes.size());
    }

    @Test(expected = Exception.class)
    public void getVnfComponentTestException() throws Exception{
        VnfComponent ht = cd.getVnfComponent(123,"vnf");
    }

    @Test
    public void getVnfResourceTest() throws Exception{
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResource> list() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return Arrays.asList(vnfResource);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResource> list() {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");
        assertEquals("123-uuid", vnfResource.getModelUuid());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getVnfResourceNURExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");
    }

    @Test(expected = HibernateException.class)
    public void getVnfResourceHibernateExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");
    }

    @Test(expected = Exception.class)
    public void getVnfResourceExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResource("vnf","3992");
    }

    @Test
    public void getVnfResourceByModelCustomizationIdTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
        assertEquals("123-uuid",vnfResource.getModelUuid());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getVnfResourceByModelCustomizationIdNURExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
    }

    @Test(expected = HibernateException.class)
    public void getVnfResourceByModelCustomizationIdHibernateExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByModelCustomizationId("3992");
    }


    @Test(expected = Exception.class)
    public void getServiceRecipeTest2Exception() throws Exception{
        ServiceRecipe ht = cd.getServiceRecipe(1001,"3992");
    }

    @Test
    public void getVnfResourceCustomizationByModelCustomizationNameTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResourceCustomization> list() throws Exception {
                VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
                vnfResourceCustomization.setVnfResourceModelUUID("123-uuid");
                return Arrays.asList(vnfResourceCustomization);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfResourceCustomization> list() throws Exception {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult(){
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");
        assertEquals("123-uuid", vnf.getModelUuid());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getVnfResourceByModelInvariantIdNURExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult(){
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");
    }

    @Test(expected = HibernateException.class)
    public void getVnfResourceByModelInvariantIdHibernateExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult(){
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");
    }

    @Test(expected = Exception.class)
    public void getVnfResourceByModelInvariantIdExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourceByIdTestException(){
        VnfResource vnf = cd.getVnfResourceById(19299);
    }

    @Test
    public void getVfModuleModelName(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModule> list() throws Exception {
                VfModule vfModule = new VfModule();
                vfModule.setModelUUID("123-uuid");
                return Arrays.asList(vfModule);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModule> list() throws Exception {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VfModule vfModule = new VfModule();
                vfModule.setModelUUID("123-uuid");
                return vfModule;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");
        assertEquals("123-uuid", vfModule.getModelUUID());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getVfModuleModelNameNURExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");
    }

    @Test(expected = HibernateException.class)
    public void getVfModuleModelNameHibernateExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");
    }

    @Test(expected = Exception.class)
    public void getVfModuleModelNameGenericExceptionTest() {
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleModelName("tetes","4kidsl");
    }

    @Test
    public void ggetVfModuleCustomizationByModelNameTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModuleCustomization> list() throws Exception {
                VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
                vfModuleCustomization.setVfModuleModelUuid("123-uuid");
                return Arrays.asList(vfModuleCustomization);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VfModuleCustomization> list() throws Exception {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkResource> list() throws Exception {
                NetworkResource networkResource = new NetworkResource();
                networkResource.setModelUUID("123-uuid");
                return Arrays.asList(networkResource);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<NetworkResource> list() throws Exception {
                return Arrays.asList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setVfModuleId("123-id");
                return Arrays.asList(vnfRecipe);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                return Collections.emptyList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setId(1);
                return Arrays.asList(vnfRecipe);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                return Collections.emptyList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                VnfRecipe vnfRecipe = new VnfRecipe();
                vnfRecipe.setId(1);
                return Arrays.asList(vnfRecipe);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
            @Mock
            public List<VnfRecipe> list() throws Exception {
                return Collections.emptyList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfRecipe vnfRecipe = cd.getVnfRecipeByVfModuleId("tetes","4993493","vnf");
        assertEquals(null, vnfRecipe);
    }

    @Test(expected = Exception.class)
    public void getVfModuleTypeTestException(){
        VfModule vnf = cd.getVfModuleType("4993493");
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleType2TestException(){
        VfModule vnf = cd.getVfModuleType("4993493","vnf");
    }
    @Test
    public void getVnfResourceByServiceUuidTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");
        assertEquals("123-uuid", vnfResource.getModelUuid());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getVnfResourceByServiceUuidNURExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");
    }

    @Test(expected = HibernateException.class)
    public void getVnfResourceByServiceUuidHibernateExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");
    }

    @Test(expected = Exception.class)
    public void getVnfResourceByServiceUuidExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };
        VnfResource vnfResource = cd.getVnfResourceByServiceUuid("4993493");
    }

    @Test
    public void getVnfResourceByVnfUuidTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                VnfResource vnfResource = new VnfResource();
                vnfResource.setModelUuid("123-uuid");
                return vnfResource;
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");
        assertEquals("123-uuid", vnfResource.getModelUuid());
    }

    @Test(expected = NonUniqueResultException.class)
    public void getVnfResourceByVnfUuidNURExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new NonUniqueResultException(-1);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");
    }

    @Test(expected = HibernateException.class)
    public void getVnfResourceByVnfUuidHibernateExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() {
                throw new HibernateException("hibernate exception");
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");
    }

    @Test(expected = Exception.class)
    public void getVnfResourceByVnfUuidExceptionTest(){
        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public Object uniqueResult() throws Exception {
                throw new Exception();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VnfResource vnfResource = cd.getVnfResourceByVnfUuid("4993493");
    }

    @Test
    public void getVfModuleByModelInvariantUuidTest(){

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() throws Exception {
                VfModule vfModule = new VfModule();
                vfModule.setModelUUID("123-uuid");
                return Arrays.asList(vfModule);
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
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

        MockUp<Query> mockUpQuery = new MockUp<Query>() {

            @Mock
            public List<VfModule> list() throws Exception {
                return Collections.emptyList();
            }
        };

        MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String hql) {
                return mockUpQuery.getMockInstance();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

        VfModule vfModule = cd.getVfModuleByModelInvariantUuid("4993493");
        assertEquals(null, vfModule);
    }

    @Test(expected = Exception.class)
    public void getVfModuleByModelCustomizationUuidTestException(){
        VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleByModelInvariantUuidAndModelVersionTestException(){
        VfModule vnf = cd.getVfModuleByModelInvariantUuidAndModelVersion("4993493","vnf");
    }
    @Test(expected = Exception.class)
    public void getVfModuleCustomizationByModelCustomizationIdTestException(){
        VfModuleCustomization vnf = cd.getVfModuleCustomizationByModelCustomizationId("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelUuidTestException(){
        VfModule vnf = cd.getVfModuleByModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourceCustomizationByModelCustomizationUuidTestException(){
        VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationByModelVersionIdTestException(){
        VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelVersionId("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleByModelCustomizationIdAndVersionTestException(){
        cd.getVfModuleByModelCustomizationIdAndVersion("4993493","test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleByModelCustomizationIdModelVersionAndModelInvariantIdTestException(){
        cd.getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId("4993493","vnf","test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourceCustomizationByModelInvariantIdTest(){
        cd.getVnfResourceCustomizationByModelInvariantId("4993493","vnf","test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleCustomizationByVnfModuleCustomizationUuidTest(){
        cd.getVfModuleCustomizationByVnfModuleCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionIdTest(){
        cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("4993493","test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllVfModuleCustomizationstest(){
        cd.getAllVfModuleCustomizations("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourceByModelUuidTest(){
        cd.getVnfResourceByModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResCustomToVfModuleTest(){
        cd.getVnfResCustomToVfModule("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getVfModulesForVnfResourceTest(){
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelUuid("48839");
        cd.getVfModulesForVnfResource(vnfResource);
    }
    @Test(expected = Exception.class)
    public void getVfModulesForVnfResource2Test(){
        cd.getVfModulesForVnfResource("4993493");
    }
    @Test(expected = Exception.class)
    public void getServiceByUuidTest(){
        cd.getServiceByUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getNetworkResourceById2Test(){
        cd.getNetworkResourceById(4993493);
    }
    @Test(expected = Exception.class)
    public void getNetworkResourceByIdTest(){
        cd.getVfModuleTypeByUuid("4993493");
    }
    @Test
    public void isEmptyOrNullTest(){
        boolean is = cd.isEmptyOrNull("4993493");
        assertFalse(is);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getSTRTest(){
        cd.getSTR("4993493","test","vnf");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVRCtoVFMCTest(){
        cd.getVRCtoVFMC("4993493","388492");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleTypeByUuidTestException(){
        cd.getVfModuleTypeByUuid("4993493");
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getTempNetworkHeatTemplateLookupTest(){
        cd.getTempNetworkHeatTemplateLookup("4993493");
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllNetworksByServiceModelUuidTest(){
        cd.getAllNetworksByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByServiceModelInvariantUuidTest(){
        cd.getAllNetworksByServiceModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByServiceModelInvariantUuid2Test(){
        cd.getAllNetworksByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByNetworkModelCustomizationUuidTest(){
        cd.getAllNetworksByNetworkModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByNetworkTypeTest(){
        cd.getAllNetworksByNetworkType("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllVfmcForVrcTest(){
        VnfResourceCustomization re = new VnfResourceCustomization();
        re.setModelCustomizationUuid("377483");
        cd.getAllVfmcForVrc(re);
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceModelUuidTest(){
        cd.getAllVnfsByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceModelInvariantUuidTest(){
        cd.getAllVnfsByServiceModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceModelInvariantUuid2Test(){
        cd.getAllVnfsByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceNameTest(){
        cd.getAllVnfsByServiceName("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceName2Test(){
        cd.getAllVnfsByServiceName("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByVnfModelCustomizationUuidTest(){
        cd.getAllVnfsByVnfModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllAllottedResourcesByServiceModelUuidTest(){
        cd.getAllAllottedResourcesByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllAllottedResourcesByServiceModelInvariantUuidTest(){
        cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllAllottedResourcesByServiceModelInvariantUuid2Test(){
        cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllAllottedResourcesByArModelCustomizationUuidTest(){
        cd.getAllAllottedResourcesByArModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllottedResourceByModelUuidTest(){
        cd.getAllottedResourceByModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllResourcesByServiceModelUuidTest(){
        cd.getAllResourcesByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllResourcesByServiceModelInvariantUuidTest(){
        cd.getAllResourcesByServiceModelInvariantUuid("4993493");
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllResourcesByServiceModelInvariantUuid2Test(){
        cd.getAllResourcesByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getSingleNetworkByModelCustomizationUuidTest(){
        cd.getSingleNetworkByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getSingleAllottedResourceByModelCustomizationUuidTest(){
        cd.getSingleAllottedResourceByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleRecipeTest(){
        cd.getVfModuleRecipe("4993493","test","get");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleTest(){
        cd.getVfModule("4993493","test","get","v2","vnf");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipeTest(){
        cd.getVnfComponentsRecipe("4993493","test","v2","vnf","get","3992");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipeByVfModuleTest(){
        List <VfModule> resultList = new ArrayList<>();
        VfModule m = new VfModule();
        resultList.add(m);
        cd.getVnfComponentsRecipeByVfModule(resultList,"4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllVnfResourcesTest(){
        cd.getAllVnfResources();
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourcesByRoleTest(){
        cd.getVnfResourcesByRole("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfResourceCustomizationsByRoleTest(){
        cd.getVnfResourceCustomizationsByRole("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllNetworkResourcesTest(){
        cd.getAllNetworkResources();
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllNetworkResourceCustomizationsTest(){
        cd.getAllNetworkResourceCustomizations();
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllVfModulesTest(){
        cd.getAllVfModules();
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllVfModuleCustomizationsTest(){
        cd.getAllVfModuleCustomizations();
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getAllHeatEnvironmentTest(){
        cd.getAllHeatEnvironment();
    }
    @Test(expected = Exception.class)
    public void getHeatEnvironment2Test(){
        cd.getHeatEnvironment(4993493);
    }
    @Test(expected = Exception.class)
    public void getNestedTemplatesTest(){
        cd.getNestedTemplates(4993493);
    }
    @Test(expected = Exception.class)
    public void getNestedTemplates2Test(){
        cd.getNestedTemplates("4993493");
    }
    @Test(expected = Exception.class)
    public void getHeatFilesTest(){
        cd.getHeatFiles(4993493);
    }
    @Test(expected = Exception.class)
    public void getVfModuleToHeatFilesEntryTest(){
        cd.getVfModuleToHeatFilesEntry("4993493","49959499");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getServiceToResourceCustomization(){
        cd.getServiceToResourceCustomization("4993493","599349","49900");
    }
    @Test(expected = Exception.class)
    public void getHeatFilesForVfModuleTest(){
        cd.getHeatFilesForVfModule("4993493");
    }
    @Test(expected = Exception.class)
    public void getHeatTemplateTest(){
        cd.getHeatTemplate("4993493","test","heat");
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveHeatTemplateTest(){
        HeatTemplate heat = new HeatTemplate();
        Set <HeatTemplateParam> paramSet = new HashSet<>();
        cd.saveHeatTemplate(heat,paramSet);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getHeatEnvironmentTest(){
        cd.getHeatEnvironment("4993493","test","heat");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getHeatEnvironment3Test(){
        cd.getHeatEnvironment("4993493","test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveHeatEnvironmentTest(){
        HeatEnvironment en = new HeatEnvironment();
        cd.saveHeatEnvironment(en);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveHeatTemplate2Test(){
        HeatTemplate heat = new HeatTemplate();
        cd.saveHeatTemplate(heat);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveHeatFileTest(){
        HeatFiles hf = new HeatFiles();
        cd.saveHeatFile(hf);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveVnfRecipeTest(){
        VnfRecipe vr = new VnfRecipe();
        cd.saveVnfRecipe(vr);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveVnfComponentsRecipe(){
        VnfComponentsRecipe vr = new VnfComponentsRecipe();
        cd.saveVnfComponentsRecipe(vr);
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVnfResourceTest(){
        VnfResource vr = new VnfResource();
        cd.saveOrUpdateVnfResource(vr);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveVnfResourceCustomizationTest(){
        VnfResourceCustomization vr = new VnfResourceCustomization();
        cd.saveVnfResourceCustomization(vr);
    }
    @Test(expected = Exception.class)
    public void saveAllottedResourceCustomizationTest(){
        AllottedResourceCustomization arc = new AllottedResourceCustomization();
        cd.saveAllottedResourceCustomization(arc);
    }
    @Test(expected = Exception.class)
    public void saveAllottedResourceTest(){
        AllottedResource ar = new AllottedResource();
        cd.saveAllottedResource(ar);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveNetworkResourceTest() throws RecordNotFoundException {
        NetworkResource nr = new NetworkResource();
        cd.saveNetworkResource(nr);
    }
    @Test(expected = Exception.class)
    public void saveToscaCsarTest()throws RecordNotFoundException {
        ToscaCsar ts = new ToscaCsar();
        cd.saveToscaCsar(ts);
    }
    @Test(expected = Exception.class)
    public void getToscaCsar(){
        cd.getToscaCsar("4993493");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveTempNetworkHeatTemplateLookupTest(){
        TempNetworkHeatTemplateLookup t = new TempNetworkHeatTemplateLookup();
        cd.saveTempNetworkHeatTemplateLookup(t);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveVfModuleToHeatFiles(){
        VfModuleToHeatFiles v = new VfModuleToHeatFiles();
        cd.saveVfModuleToHeatFiles(v);
    }
    @Test(expected = Exception.class)
    public void saveVnfResourceToVfModuleCustomizationTest() throws RecordNotFoundException {
        VnfResourceCustomization v =new VnfResourceCustomization();
        VfModuleCustomization vm = new VfModuleCustomization();
        cd.saveVnfResourceToVfModuleCustomization(v, vm);
    }
    @Test(expected = Exception.class)
    public void saveNetworkResourceCustomizationTest() throws RecordNotFoundException {
        NetworkResourceCustomization nrc = new NetworkResourceCustomization();
        cd.saveNetworkResourceCustomization(nrc);
    }

    @Test(expected = Exception.class)
    public void saveServiceToNetworksTest(){
        AllottedResource ar = new AllottedResource();
        cd.saveAllottedResource(ar);
    }
    @Test(expected = Exception.class)
    public void saveServiceToResourceCustomizationTest(){
        ServiceToResourceCustomization ar = new ServiceToResourceCustomization();
        cd.saveServiceToResourceCustomization(ar);
    }
    @Test(expected = Exception.class)
    public void saveServiceTest(){
        Service ar = new Service();
        cd.saveService(ar);
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVfModuleTest(){
        VfModule ar = new VfModule();
        cd.saveOrUpdateVfModule(ar);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void saveOrUpdateVfModuleCustomizationTest(){
        VfModuleCustomization ar = new VfModuleCustomization();
        cd.saveOrUpdateVfModuleCustomization(ar);
    }

    @Test(expected = Exception.class)
    public void getNestedHeatTemplateTest(){
        cd.getNestedHeatTemplate(101,201);
    }
    @Test(expected = Exception.class)
    public void getNestedHeatTemplate2Test(){
        cd.getNestedHeatTemplate("1002","1002");
    }
    @Test(expected = Exception.class)
    public void saveNestedHeatTemplateTest(){
        HeatTemplate ar = new HeatTemplate();
        cd.saveNestedHeatTemplate("1001",ar,"test");
    }
    @Test(expected = Exception.class)
    public void getHeatFiles2Test(){
        VfModuleCustomization ar = new VfModuleCustomization();
        cd.getHeatFiles(101,"test","1001","v2");
    }
    @Test(expected = Exception.class)
    public void getHeatFiles3Test(){
        VfModuleCustomization ar = new VfModuleCustomization();
        cd.getHeatFiles("200192");
    }
    @Test(expected = Exception.class)
    public void saveHeatFilesTest(){
        HeatFiles ar = new HeatFiles();
        cd.saveHeatFiles(ar);
    }
    @Test(expected = Exception.class)
    public void saveVfModuleToHeatFilesTest(){
        HeatFiles ar = new HeatFiles();
        cd.saveVfModuleToHeatFiles("3772893",ar);
    }
    @Test
    public void getNetworkResourceByModelUuidTest(){

        cd.getNetworkResourceByModelUuid("3899291");
    }
    @Test(expected = Exception.class)
    public void getNetworkRecipeTest(){

        cd.getNetworkRecipe("test","test1","test2");
    }
    @Test(expected = Exception.class)
    public void getNetworkRecipe2Test(){

        cd.getNetworkRecipe("test","test1");
    }
    @Test
    public void getNetworkResourceByModelCustUuidTest(){

        cd.getNetworkResourceByModelCustUuid("test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVnfComponentsRecipe2Test(){

        cd.getVnfComponentsRecipe("test1","test2","test3","test4");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipeByVfModuleModelUUIdTest(){

        cd.getVnfComponentsRecipeByVfModuleModelUUId("test1","test2","test3");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentRecipesTest(){

        cd.getVnfComponentRecipes("test");
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVnfComponentTest(){
        VnfComponent ar = new VnfComponent();
        cd.saveOrUpdateVnfComponent(ar);
    }

    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModule2Test(){

        cd.getVfModule("test");
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void getVfModuleByModelUUIDTest(){

        cd.getVfModuleByModelUUID("test");
    }
    @Test(expected = Exception.class)
    public void getServiceRecipeByModelUUIDTest(){

        cd.getServiceRecipeByModelUUID("test1","test2");
    }
    @Test(expected = Exception.class)
    public void getModelRecipeTest(){

        cd.getModelRecipe("test1","test2","test3");
    }
    @Test(expected = Exception.class)
    public void healthCheck(){

        cd.healthCheck();
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void executeQuerySingleRow(){
        VnfComponent ar = new VnfComponent();
        HashMap<String, String> variables = new HashMap<>();
        cd.executeQuerySingleRow("tets",variables,false);
    }
    @Test(expected = Exception.class)
    @Ignore // 1802 merge
    public void executeQueryMultipleRows(){
        HashMap<String, String> variables = new HashMap<>();
        cd.executeQueryMultipleRows("select",variables,false);
    }
}
