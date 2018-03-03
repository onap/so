/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.db.catalog;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatNestedTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.Model;
import org.openecomp.mso.db.catalog.beans.ModelRecipe;
import org.openecomp.mso.db.catalog.beans.NetworkRecipe;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.ServiceToAllottedResources;
import org.openecomp.mso.db.catalog.beans.ServiceToNetworks;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VfModuleToHeatFiles;
import org.openecomp.mso.db.catalog.beans.VnfComponent;
import org.openecomp.mso.db.catalog.beans.VnfComponentsRecipe;
import org.openecomp.mso.db.catalog.beans.VnfRecipe;
import org.openecomp.mso.db.catalog.beans.VnfResCustomToVfModuleCustom;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.db.catalog.utils.MavenLikeVersioningComparator;
import org.openecomp.mso.db.catalog.utils.RecordNotFoundException;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This class encapsulates all of the objects that can be queried from a Catalog database.
 * Clients must use these methods to retrieve catalog objects. The session is not
 * available for clients to do their own direct queries to the database.
 *
 *
 */
public class CatalogDatabase implements Closeable {

    protected final AbstractSessionFactoryManager sessionFactoryCatalogDB;

    private static final String NETWORK_TYPE = "networkType";
    private static final String ACTION = "action";
    private static final String VNF_TYPE = "vnfType";
    private static final String SERVICE_TYPE = "serviceType";
    private static final String MODEL_UUID= "modelUUID";
    private static final String VNF_COMPONENT_TYPE = "vnfComponentType";
    private static final String MODEL_ID = "modelId";
    private static final String MODEL_NAME = "modelName";
    private static final String TYPE = "type";
    private static final String MODEL_TYPE = "modelType";
    private static final String MODEL_VERSION_ID = "modelVersionId";
    private static final String MODEL_CUSTOMIZATION_UUID = "modelCustomizationUuid";
	private static final String VF_MODULE_MODEL_UUID = "vfModuleModelUUId";

    protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    protected Session session = null;

    protected CatalogDatabase (AbstractSessionFactoryManager sessionFactoryCatalog) {
        sessionFactoryCatalogDB = sessionFactoryCatalog;
    }
    
    public static CatalogDatabase getInstance() {
        return new CatalogDatabase(new CatalogDbSessionFactoryManager ());
    }
    
    private Session getSession () {

             if (session == null) {
            try {
                session = sessionFactoryCatalogDB.getSessionFactory ().openSession ();
                session.beginTransaction ();
            } catch (HibernateException he) {
                LOGGER.error (MessageEnum.GENERAL_EXCEPTION_ARG, "Error creating Hibernate Session: " + he, "", "", MsoLogger.ErrorCode.DataError, "Error creating Hibernate Session: " + he);
                throw he;
            }
        }

        return session;
    }

    /**
     * Close an open Catalog Database session.
     * This method should always be called when a client is finished using a
     * CatalogDatabase instance.
     */
    @Override
    public void close () {
        if (session != null) {
            session.close ();
            session = null;
        }
    }

    /**
     * Commits the current transaction on this session and starts a fresh one.
     */
    public void commit () {
        getSession ().getTransaction ().commit ();
        getSession ().beginTransaction ();
    }

    /**
     * Rolls back current transaction and starts a fresh one.
     */
    public void rollback () {
        getSession ().getTransaction ().rollback ();
        getSession ().beginTransaction ();
    }

    /**
     * Return all Heat Templates in the Catalog DB
     *
     * @return A list of HeatTemplate objects
     */
    @SuppressWarnings("unchecked")
    public List <HeatTemplate> getAllHeatTemplates() {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get all Heat templates");
        String hql = "FROM HeatTemplate";
        Query query = getSession().createQuery(hql);

        List <HeatTemplate> result = query.list();
        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllHeatTemplates", null);
        return result;
    }

    /**
     * Fetch a specific Heat Template by ID.
     *
     * @param templateId template id
     * @return HeatTemplate object or null if none found
     */
    @Deprecated
    public HeatTemplate getHeatTemplate(int templateId) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get Heat template with id " + templateId);

        HeatTemplate template = (HeatTemplate) getSession().get(HeatTemplate.class, templateId);
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return template;
    }

    /**
     * Return the newest version of a specific Heat Template (queried by Name).
     *
     * @param templateName template name
     * @return HeatTemplate object or null if none found
     */
    public HeatTemplate getHeatTemplate(String templateName) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Heat template with name " + templateName);

        String hql = "FROM HeatTemplate WHERE templateName = :template_name";
        Query query = getSession().createQuery (hql);
        query.setParameter("template_name", templateName);

        @SuppressWarnings("unchecked")
        List <HeatTemplate> resultList = query.list();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No template found", "CatalogDB", "getHeatTemplate", null);
            return null;
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return resultList.get(0);
    }

    /**
     * Return a specific version of a specific Heat Template (queried by Name).
     *
     * @param templateName
     * @param version
     * @return HeatTemplate object or null if none found
     */
    public HeatTemplate getHeatTemplate(String templateName, String version) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Heat template with name " + templateName
                                      + " and version "
                                      + version);

        String hql = "FROM HeatTemplate WHERE templateName = :template_name AND version = :version";
        Query query = getSession().createQuery(hql);
        query.setParameter("template_name", templateName);
        query.setParameter("version", version);

        @SuppressWarnings("unchecked")
        List <HeatTemplate> resultList = query.list();

        // See if something came back.
        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No template found.", "CatalogDB", "getHeatTemplate", null);
            return null;
        }
        // Name + Version is unique, so should only be one element
        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return resultList.get(0);
    }

    /**
     * Return a specific Heat Template by ARTIFACT_UUID).
     *
     * @param artifactUuid
     * @return HeatTemplate object or null if none found
     */    
    
    public HeatTemplate getHeatTemplateByArtifactUuid(String artifactUuid) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat template with artifactUuid " + artifactUuid);

        // Will this work if the id is a string? 
        HeatTemplate template = (HeatTemplate) getSession ().get (HeatTemplate.class, artifactUuid);
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return template;
    }
    
    /**
     * Return a specific Heat Template by ARTIFACT_UUID using standard query method. unique record expected.
     *
     * @param artifactUuid
     * @return HeatTemplate object or null if none found
     */
    public HeatTemplate getHeatTemplateByArtifactUuidRegularQuery(String artifactUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Heat template (regular query) with artifactUuid " + artifactUuid);

        String hql = "FROM HeatTemplate WHERE artifactUuid = :artifactUuidValue";
        HashMap<String, String> variables = new HashMap<>();
        variables.put("artifactUuidValue", artifactUuid);
        HeatTemplate template = (HeatTemplate) this.executeQuerySingleRow(hql, variables, true);

        if (template == null) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getHeatTemplateByArtifactUuidRegularQuery", null);
        } else {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplateByArtifactUuidRegularQuery", null);
        }
        return template;
    }
    
    public List<HeatTemplateParam> getParametersForHeatTemplate(String heatTemplateArtifactUuid) {
        LOGGER.debug ("Catalog database - getParametersForHeatTemplate with artifactUuid " + heatTemplateArtifactUuid);

        String hql = "FROM HeatTemplateParams WHERE artifactUuid = :artifactUuidValue";
        Query query = getSession().createQuery(hql);
        query.setParameter ("artifactUuidValue", heatTemplateArtifactUuid);
        List<HeatTemplateParam> resultList = new ArrayList<>();
        try {
        	resultList = query.list ();
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching HeatTemplateParams for: heatTemplateArtifactUuid='" + heatTemplateArtifactUuid + "'" + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching HeatTemplateParams for artifactUuid=" + heatTemplateArtifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for artifactUuid=" + heatTemplateArtifactUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching HeatTemplateParam for: artifactUuid='" + heatTemplateArtifactUuid + "'" + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching HeatTemplate for artifactUuid=" + heatTemplateArtifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for artifactUuid=" + heatTemplateArtifactUuid);
        	throw e;
        }
        
        return resultList;
    	
    }
    
    /**
     * Return a specific Heat Environment by ARTIFACT_UUID using standard query method. unique record expected.
     *
     * @param artifactUuid
     * @return HeatEnvironment object or null if none found
     */    
    public HeatEnvironment getHeatEnvironmentByArtifactUuid(String artifactUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Heat Environment with artifactUuid " + artifactUuid);

        String hql = "FROM HeatEnvironment WHERE artifactUuid = :artifactUuidValue";
        Query query = getSession().createQuery(hql);
        query.setParameter("artifactUuidValue", artifactUuid);
        HeatEnvironment environment = null;
        try {
            environment = (HeatEnvironment) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row for Envt - data integrity error: artifactUuid='" + artifactUuid +"'", nure);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for heatEnvironment artifactUuid=" + artifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for artifactUuid==" + artifactUuid);
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for envt: artifactUuid='" + artifactUuid + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching envt for artifactUuid=" + artifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching envt for artifactUuid=" + artifactUuid);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: artifactUuid='" + artifactUuid + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching envt for artifactUuid=" + artifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching envt for artifactUuid=" + artifactUuid);

        	throw e;
        }

        if (environment == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getHeatEnvironmentByArtifactUuid", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatEnvironmentByArtifactUuid", null);
        }
        return environment;
    }
    
    /**
     * Fetch a Service definition by InvariantUUID
     */
    public Service getServiceByInvariantUUID (String modelInvariantUUID) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get service with Invariant UUID " + modelInvariantUUID);

        String hql = "FROM Service WHERE modelInvariantUUID = :model_invariant_uuid";
        Query query = getSession().createQuery(hql);
        query.setParameter ("model_invariant_uuid", modelInvariantUUID);

        @SuppressWarnings("unchecked")
        List <Service> resultList = query.list ();

        // See if something came back.
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service not found", "CatalogDB", "getServiceByName", null);
            return null;
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceByName", null);
        return resultList.get (0);
    }

    /**
     * Fetch a Service definition
     */
    public Service getService (String modelName) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get service with name " + modelName);

        String hql = "FROM Service WHERE modelName = :MODEL_NAME";
        Query query = getSession().createQuery(hql);
        query.setParameter("MODEL_NAME", modelName);

        Service service = null;
        try {
        	service = (Service) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelName='" + modelName + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for modelName=" + modelName, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelName=" + modelName);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelName='" + modelName + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelName=" + modelName, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelName=" + modelName);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelName='" + modelName + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelName=" + modelName, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelName=" + modelName);
        	throw e;
        }
        if (service == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getService", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getService", null);
        }

        return service;
    }

    public Service getServiceByModelUUID (String modelUUID) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get service with Model UUID " + modelUUID);

        String hql = "FROM Service WHERE modelUUID = :MODEL_UUID";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("MODEL_UUID", modelUUID);

        
        Service service = this.executeQuerySingleRow(hql, parameters, true);

        /*
        try {
        	service = (Service) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelUUID='" + modelUUID + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for modelUUID=" + modelUUID, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelUUID=" + modelUUID);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelUUID='" + modelUUID + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelUUID=" + modelUUID, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelUUID=" + modelUUID);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelUUID='" + modelUUID + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelUUID=" + modelUUID, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelUUID=" + modelUUID);
        	throw e;
        }
        */
        if (service == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getServiceByModelUUID", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceByModelUUID", null);
        }

        return service;
    }

    /**
     * Fetch the Common Service API definition using Http Method + serviceNameVersionId
     */
    public Service getService(Map<String, String> map, String httpMethod) {

        String serviceNameVersionId = map.get("serviceNameVersionId");
        Query query;
        String serviceId = "not_set";
        String serviceVersion = "not_set";

        if(serviceNameVersionId != null && serviceNameVersionId.length() > 0){
        	LOGGER.debug ("Catalog database - get service modelUUID with id " + serviceNameVersionId);

        	String hql = "FROM Service WHERE MODEL_UUID = :MODEL_UUID and http_method = :http_method";
        	query = getSession().createQuery(hql);
            query.setParameter("MODEL_UUID", serviceNameVersionId);
         } else {
        	serviceId = map.get("serviceId");
        	serviceVersion = map.get("serviceVersion");
            LOGGER.debug("Catalog database - get serviceId with id " + serviceId + " and serviceVersion with " + serviceVersion);

            String hql = "FROM Service WHERE service_id = :service_id and service_version = :service_version and http_method = :http_method";
            query = getSession().createQuery(hql);
            query.setParameter("service_id", serviceId);
            query.setParameter("service_version", serviceVersion);
         }

        query.setParameter("http_method", httpMethod);

        long startTime = System.currentTimeMillis();
        Service service = null;
        try {
        	service = (Service) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - data integrity error: service_id='" + serviceId + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for service_id=" + serviceId + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for service_id=" + serviceId);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: service_id='" + serviceId + "', serviceVersion='" + serviceVersion + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for service_id=" + serviceId + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for service_id=" + serviceId);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: service_id='" + serviceId + "', serviceVersion='" + serviceVersion + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for service_id=" + serviceId + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for service_id=" + serviceId);

        	throw e;
        }
        if (service == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getService", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getService", null);
        }
        return service;
    }

    /**
     * Return the newest version of a Service (queried by Name).
     *
     * @param modelName
     * @return Service object or null if none found
     */
    public Service getServiceByModelName(String modelName){

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get service with name " + modelName);

        String hql = "FROM Service WHERE modelName = :MODEL_NAME";
        Query query = getSession().createQuery(hql);
        query.setParameter("MODEL_NAME", modelName);

        @SuppressWarnings("unchecked")
        List <Service> resultList = query.list();

        // See if something came back.
        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service not found", "CatalogDB", "getServiceByModelName", null);
            return null;
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceByModelName", null);
        return resultList.get(0);
    }

    public Service getServiceByVersionAndInvariantId(String modelInvariantId, String modelVersion) throws Exception {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get service with modelInvariantId: " + modelInvariantId + " and modelVersion: " + modelVersion);

        String hql = "FROM Service WHERE modelInvariantUUID = :MODEL_INVARIANT_UUID AND version = :VERSION_STR";
        Query query = getSession().createQuery(hql);
        query.setParameter("MODEL_INVARIANT_UUID", modelInvariantId);
        query.setParameter("VERSION_STR", modelVersion);

        Service result = null;
        try {
            result = (Service) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelInvariantId='" + modelInvariantId + "', modelVersion='" + modelVersion + "'", nure);
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for modelInvariantId=" + modelInvariantId + " and modelVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelInvariantId=" + modelInvariantId);
            throw new Exception("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelInvariantId='" + modelInvariantId + "', modelVersion='" + modelVersion + "'");
        }
        // See if something came back.
        if (result==null) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service not found", "CatalogDB", "getServiceByVersionAndInvariantId", null);
            return null;
        }

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceByVersionAndInvariantId", null);
        return result;
    }

    /**
     * Return a newest version of Service recipe that matches a given SERVICE_ID and ACTION
     *
     * @param serviceModelUUID
     * @param action     * 
     * @return ServiceRecipe object or null if none found
     */
    @Deprecated
    public ServiceRecipe getServiceRecipe (int serviceModelUUID, String action) {
       
        StringBuilder hql;

    	if(action == null){
        	hql = new StringBuilder ("FROM ServiceRecipe WHERE serviceModelUUID = :serviceModelUUID");
        }else {
        	hql = new StringBuilder ("FROM ServiceRecipe WHERE serviceModelUUID = :serviceModelUUID AND action = :action ");
        }

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Service recipe with serviceModelUUID " + Integer.toString(serviceModelUUID)
                                      + " and action "
                                      + action
                                      );

        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter ("serviceModelUUID", serviceModelUUID);
        if(action != null){
        	query.setParameter (ACTION, action);
        }

                        @SuppressWarnings("unchecked")
        List <ServiceRecipe> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service recipe not found", "CatalogDB", "getServiceRecipe", null);
                                return null;
                        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipe", null);
        return resultList.get (0);
    }

    /**
     * Return a newest version of Service recipe that matches a given SERVICE_MODEL_UUID and ACTION
     *
     * @param serviceModelUuid
     * @param action     *
     * @return ServiceRecipe object or null if none found
     */
    public ServiceRecipe getServiceRecipeByServiceModelUuid(String serviceModelUuid, String action) {

        StringBuilder hql;

        if(action == null){
            hql = new StringBuilder("FROM ServiceRecipe WHERE serviceModelUuid = :serviceModelUuid");
        }else {
            hql = new StringBuilder("FROM ServiceRecipe WHERE serviceModelUuid = :serviceModelUuid AND action = :action ");
        }

        long startTime = System.currentTimeMillis ();
        LOGGER.debug("Catalog database - get Service recipe with serviceModelUuid " + serviceModelUuid
                                      + " and action "
                                      + action
                                      );

        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelUuid", serviceModelUuid);
        if(action != null){
            query.setParameter(ACTION, action);
        }

        @SuppressWarnings("unchecked")
        List <ServiceRecipe> resultList = query.list();

        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service recipe not found", "CatalogDB", "getServiceRecipe", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipe", null);
        return resultList.get(0);
    }

    public List<ServiceRecipe> getServiceRecipes(String serviceModelUuid) {

        StringBuilder hql;

        hql = new StringBuilder("FROM ServiceRecipe WHERE serviceModelUUID = :serviceModelUUID");

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Service recipe with serviceModelUUID " + serviceModelUuid);

        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelUUID", serviceModelUuid);

        @SuppressWarnings("unchecked")
        List <ServiceRecipe> resultList = query.list();

        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service recipe not found", "CatalogDB", "getServiceRecipes", null);
            return Collections.EMPTY_LIST;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipes", null);
        return resultList;
    }

    
    /**
     * Return the VNF component data - queried by the VNFs ID and the component type.
     *
     * @param vnfId
     * @param type
     * @return VnfComponent object or null if none found
     */
    @Deprecated
    public VnfComponent getVnfComponent (int vnfId, String type) {

    	long startTime = System.currentTimeMillis();
    	LOGGER.debug ("Catalog database - get VnfComponent where vnfId="+ vnfId+ " AND componentType="+ type);

        String hql = "FROM VnfComponent WHERE vnfId = :vnf_id AND componentType = :type";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("vnf_id", vnfId);
        query.setParameter ("type", type);

       	VnfComponent result = null;
       	try {
       		result = (VnfComponent) query.uniqueResult();
       	} catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: vnf_id='" + vnfId + "', componentType='" + type + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnf_id=" + vnfId + " and componentType=" + type, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for vnf_id=" + vnfId);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: vnf_id='" + vnfId + "', componentType='" + type + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for vnf_id=" + vnfId + " and componentType=" + type, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnf_id=" + vnfId);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: vnf_id='" + vnfId + "', componentType='" + type + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for vnf_id=" + vnfId + " and componentType=" + type, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for vnf_id=" + vnfId);

        	throw e;
        }

       	if (result != null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfComponent", null);
       	} else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No VNFComponent found", "CatalogDB", "getVnfComponent", null);
       	}
        return result;
    }

    /**
     * Return the newest version of a specific VNF resource (queried by Name).
     *
     * @param vnfType
     * @return VnfResource object or null if none found
     */
    public VnfResource getVnfResource (String vnfType) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get vnf resource with model_name " + vnfType);

        String hql = "FROM VnfResource WHERE modelName = :vnf_name";
        Query query = getSession().createQuery(hql);
        query.setParameter("vnf_name", vnfType);

        @SuppressWarnings("unchecked")
        List <VnfResource> resultList = query.list();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF not found", "CatalogDB", "getVnfResource", null);
            return null;
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResource", null);
        return resultList.get(0);
    }

    /**
     * Return the newest version of a specific VNF resource (queried by Name).
     *
     * @param vnfType
     * @param serviceVersion
     * @return VnfResource object or null if none found
     */
    public VnfResource getVnfResource (String vnfType, String serviceVersion) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF resource with model_name " + vnfType + " and version=" + serviceVersion);

        String hql = "FROM VnfResource WHERE modelName = :vnfName and version = :serviceVersion";
        Query query = getSession().createQuery(hql);
        query.setParameter("vnfName", vnfType);
        query.setParameter("serviceVersion", serviceVersion);

        VnfResource resource = null;
        try {
        	resource = (VnfResource) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: vnfType='" + vnfType + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfType=" + vnfType + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for vnfType=" + vnfType);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: vnfType='" + vnfType + "', asdc_service_model_version='" + serviceVersion + "' " + he.getMessage());
        	LOGGER.debug(Arrays.toString(he.getStackTrace()));
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for vnfType=" + vnfType + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnfType=" + vnfType);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: vnfType='" + vnfType + "', serviceVersion='" + serviceVersion + "' " + e.getMessage());
        	LOGGER.debug(Arrays.toString(e.getStackTrace()));
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for vnfType=" + vnfType + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for vnfType=" + vnfType);

        	throw e;
        }
        if (resource == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResource", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResource", null);
        }
        return resource;
    }

    /**
     * Return VnfResource (queried by modelCustomizationId).
     *
     * @param modelCustomizationId
     * @return VnfResource object or null if none found
     */
    public VnfResource getVnfResourceByModelCustomizationId(String modelCustomizationId) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF resource with modelCustomizationId " + modelCustomizationId);

        String hql = "SELECT vr "
					+ "FROM VnfResource as vr JOIN vr.vnfResourceCustomizations as vrc "
					+ "WHERE vrc.modelCustomizationUuid = :modelCustomizationId";
		
        Query query = getSession().createQuery(hql);
        query.setParameter("modelCustomizationId", modelCustomizationId);

        VnfResource resource = null;
        try {
            resource = (VnfResource) query.uniqueResult();
        } catch(org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelCustomizationUuid='" + modelCustomizationId + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for modelCustomizationUuid=" + modelCustomizationId, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelCustomizationId=" + modelCustomizationId);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelCustomizationId='" + modelCustomizationId + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelCustomizationId=" + modelCustomizationId, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationId=" + modelCustomizationId);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelCustomizationId='" + modelCustomizationId + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelCustomizationId=" + modelCustomizationId, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelCustomizationId=" + modelCustomizationId);

        	throw e;
        }
        if (resource == null) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResourceByModelCustomizationId", null);
        } else {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceByModelCustomizationId", null);
        }
        return resource;
    }
    
    
    /**
     * Return the newest version of a specific VNF resource Customization (queried by modelCustomizationName and modelVersionId).
     *
     * @return {@link VnfResourceCustomization} object or null if none found
     */
    public VnfResourceCustomization getVnfResourceCustomizationByModelCustomizationName (String modelCustomizationName, String modelVersionId) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF resource Customization with modelCustomizationName " + modelCustomizationName + " serviceModelUUID " + modelVersionId);

        String hql = "SELECT vrc FROM VnfResourceCustomization as vrc WHERE vrc.modelCustomizationUuid IN "
					+ "(SELECT src.resourceModelCustomizationUUID FROM ServiceToResourceCustomization src "
					+ "WHERE src.serviceModelUUID = :modelVersionId)"
					+ "AND vrc.modelInstanceName = :modelCustomizationName";
		
        Query query = getSession().createQuery(hql);
        query.setParameter("modelCustomizationName", modelCustomizationName);
        query.setParameter("modelVersionId", modelVersionId);

        @SuppressWarnings("unchecked")
        List<VnfResourceCustomization> resultList = query.list();

        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VnfResourceCustomization not found", "CatalogDB", "getVnfResourceCustomizationByModelCustomizationName", null);
            return null;
        }
        
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceCustomizationByModelCustomizationName", null);
        return resultList.get(0);
    }
    
    
    /**
     * Return the newest version of a specific VNF resource (queried by modelInvariantId).
     *
     * @param modelInvariantUuid model invariant ID
     * @param modelVersion model version
     * @return VnfResource object or null if none found
     */
    public VnfResource getVnfResourceByModelInvariantId(String modelInvariantUuid, String modelVersion) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF resource with modelInvariantUuid " + modelInvariantUuid);

        String hql = "FROM VnfResource WHERE modelInvariantUuid = :modelInvariantUuid and version = :serviceVersion";
        Query query = getSession().createQuery(hql);
        query.setParameter("modelInvariantUuid", modelInvariantUuid);
        query.setParameter("serviceVersion", modelVersion);

        VnfResource resource = null;
        try {
        	resource = (VnfResource) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelInvariantUuid='" + modelInvariantUuid + "', serviceVersion='" + modelVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for modelInvariantUuid=" + modelInvariantUuid + " and serviceVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelInvariantUuid=" + modelInvariantUuid);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelInvariantUuid='" + modelInvariantUuid + "', asdc_service_model_version='" + modelVersion + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelInvariantUuid=" + modelInvariantUuid + " and serviceVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelInvariantUuid=" + modelInvariantUuid);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelInvariantUuid='" + modelInvariantUuid + "', serviceVersion='" + modelVersion + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelInvariantUuid=" + modelInvariantUuid + " and serviceVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelInvariantUuid=" + modelInvariantUuid);

        	throw e;
        }
        if (resource == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResource", null);
        } else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResource", null);
        }
        return resource;
    }

    /**
     * Return the newest version of a specific VNF resource (queried by ID).
     *
     * @param id The vnf id
     * @return VnfResource object or null if none found
     */
    @Deprecated
    public VnfResource getVnfResourceById (int id) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VNF resource with id " + id);

        String hql = "FROM VnfResource WHERE id = :id";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("id", id);

        @SuppressWarnings("unchecked")
        List <VnfResource> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VnfResource not found", "CatalogDB", "getVnfResourceById", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceById", null);
        return resultList.get (0);
    }

    /**
     * Return the newest version of a vfModule - 1607
     *
     */
    public VfModule getVfModuleModelName(String modelName) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get vfModuleModelName with name " + modelName);

        String hql = "FROM VfModule WHERE modelName = :model_name";
        Query query = getSession().createQuery(hql);
        query.setParameter("model_name", modelName);

        @SuppressWarnings("unchecked")
        List<VfModule> resultList = query.list();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF not found", "CatalogDB", "getVfModuleModelName", null);
            return null;
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleModelName", null);
        return resultList.get(0);
    }

    public VfModule getVfModuleModelName(String modelName, String model_version) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get vfModuleModelName with type='" + modelName + "' and asdc_service_model_version='" + model_version + "'");

        String hql = "FROM VfModule WHERE Name = :model_name and version = :model_version";
        Query query = getSession().createQuery(hql);
        query.setParameter("modelName", modelName);
        query.setParameter("model_version", model_version);

        VfModule module = null;
        try {
            module = (VfModule) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: type='" + modelName + "', asdc_service_model_version='" + model_version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for type=" + modelName + " and version=" + model_version, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for type=" + modelName);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: type='" + modelName + "', asdc_service_model_version='" + model_version + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for type=" + modelName + " and version=" + model_version, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for type=" + modelName);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: type='" + modelName + "', asdc_service_model_version='" + model_version + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for type=" + modelName + " and version=" + model_version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for type=" + modelName);

        	throw e;
        }
        if (module == null) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleModelName", null);
        } else {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleModelName", null);
        }
        return module;
    }
    
    /**
     * Need this for 1707 DHV. This may be a temporary solution. May
     * change it to get resources using service's model name.
     * 
     *@author cb645j
     *
     */
    public VfModuleCustomization getVfModuleCustomizationByModelName(String modelName) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VfModuleCustomization By VfModule's ModelName: " + modelName);

        String hql = "SELECT VfModuleCustomization FROM VfModuleCustomization as vfmc LEFT OUTER JOIN VfModule as vfm on vfm.modelUUID = vfmc.vfModuleModelUuid where vfm.modelName = :model_name";
        Query query = getSession().createQuery(hql);
        query.setParameter("model_name", modelName);

        @SuppressWarnings("unchecked")
        List<VfModuleCustomization> resultList = query.list();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful query but Vf module NOT found", "CatalogDB", "getVfModuleCustomizationByModelName", null);
            return null;
        }

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful query ", "CatalogDB", "getVfModuleCustomizationByModelName", null);
        return resultList.get(0);
    }


    /**
     * Return the newest version of a specific Network resource (queried by Type).
     *
     * @param networkType
     * @return NetworkResource object or null if none found
     */
    public NetworkResource getNetworkResource(String networkType) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get network resource with type " + networkType);

        String hql = "FROM NetworkResource WHERE model_name = :network_type";
        Query query = getSession().createQuery(hql);
        query.setParameter("network_type", networkType);

        @SuppressWarnings("unchecked")
        List <NetworkResource> resultList = query.list();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Network Resource not found", "CatalogDB", "getNetworkResource", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);
        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkResource", null);
        return resultList.get(0);
    }

    /**
     * Return a VNF recipe that matches a given VNF_TYPE, ACTION, and, if specified, SERVICE_TYPE
     *
     * @param vnfType
     * @param action
     * @param serviceType The service Name, if null or empty is provided, it won't be taken into account
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVnfRecipe(String vnfType, String action, String serviceType) {
        boolean withServiceType = false;

        StringBuilder hql = new StringBuilder("FROM VnfRecipe WHERE vnfType = :vnfType AND action = :action ");

        // If query c
        if (serviceType == null || serviceType.isEmpty()) {
            hql.append("AND serviceType is NULL ");
        } else {
            hql.append("AND serviceType = :serviceType ");
            withServiceType = true;
        }

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF recipe with name " + vnfType
                                      + " and action "
                                      + action
                                      + " and service type "
                                      + serviceType);

        Query query = getSession().createQuery(hql.toString());
        query.setParameter(VNF_TYPE, vnfType);
        query.setParameter(ACTION, action);
        if (withServiceType) {
            query.setParameter(SERVICE_TYPE, serviceType);
        }

        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList = query.list();

        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfRecipe", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfRecipe", null);
        return resultList.get(0);
    }

    /**
     * Return a VNF recipe that matches a given VNF_TYPE and ACTION
     *
     * @param vnfType
     * @param action
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVnfRecipe(String vnfType, String action) {
        StringBuilder hql = new StringBuilder("FROM VnfRecipe WHERE vnfType = :vnfType AND action = :action ");

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF recipe with name " + vnfType
                                      + " and action "
                                      + action);

        Query query = getSession().createQuery(hql.toString());
        query.setParameter(VNF_TYPE, vnfType);
        query.setParameter(ACTION, action);

        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList = query.list();

        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfRecipe", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfRecipe", null);
        return resultList.get(0);
    }

    /**
     * Return a VNF recipe that matches a given VF_MODULE_ID and ACTION
     *
     * @param vfModuleId
     * @param action
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVnfRecipeByVfModuleId(String vnfType, String vfModuleId, String action) {

    	StringBuilder hql = new StringBuilder("FROM VnfRecipe WHERE vfModuleId = :vfModuleId and action = :action  ");

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get VNF Recipe with vfModuleId " + vfModuleId);

        Query query = getSession().createQuery(hql.toString ());
        query.setParameter(VF_MODULE_MODEL_UUID, vfModuleId);
        query.setParameter(ACTION, action);

        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList = query.list();

        if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe Entry not found", "CatalogDB", "getVnfRecipeByVfModuleId", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse(resultList);

        LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF Recipe Entry found", "CatalogDB", "getVnfRecipeByVfModuleId", null);
        return resultList.get(0);
    }

    public VfModule getVfModuleTypeByUuid(String modelCustomizationUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get vfModuleTypeByUuid with uuid=" + modelCustomizationUuid);

        String hql = "FROM VfModule WHERE modelCustomizationUuid = :modelCustomizationUuid";
        Query query = getSession().createQuery(hql);
        query.setParameter("modelCustomizationUuid", modelCustomizationUuid);

        VfModule module = null;
        try {
            module = (VfModule) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelCustomizationUuid='" + modelCustomizationUuid + "'");
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelCustomizationUuid==" + modelCustomizationUuid);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + he.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + e.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid);

        	throw e;
        }
        if (module == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleTypeByUuid", null);
        } else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleTypeByUuid", null);
        }
        return module;
    }

    @Deprecated
    public VfModule getVfModuleType(String type) {
    	long startTime = System.currentTimeMillis();
    	LOGGER.debug("Catalog database - get vfModuleType with type " + type);

    	String hql = "FROM VfModule WHERE type = :type";
    	Query query = getSession().createQuery(hql);
    	query.setParameter("type",  type);

    	@SuppressWarnings("unchecked")
    	List<VfModule> resultList = query.list();
    	if (resultList.isEmpty()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF not found", "CatalogDB", "getVfModuleType", null);
            return null;
    	}
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleType", null);
        return resultList.get (0);
    }

    @Deprecated
    public VfModule getVfModuleType(String type, String version) {

    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get vfModuleType with type " + type + " and model_version " + version);

        String hql = "FROM VfModule WHERE type = :type and version = :version";
        Query query = getSession().createQuery(hql);
        query.setParameter ("type", type);
        query.setParameter ("version", version);
        VfModule module = null;
        try {
        	module = (VfModule) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: type='" + type + "', asdc_service_model_version='" + version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for type=" + type + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for type==" + type);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: type='" + type + "', asdc_service_model_version='" + version + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for type=" + type + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for type=" + type);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: type='" + type + "', asdc_service_model_version='" + version + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for type=" + type + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for type=" + type);

        	throw e;
        }
        if (module == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleType", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleType", null);
        }
        return module;
    }

    public VnfResource getVnfResourceByServiceUuid(String serviceModelInvariantUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get vfModuleType with serviceModelInvariantUuid " + serviceModelInvariantUuid);

        String hql = "FROM VnfResource WHERE serviceModelInvariantUuid = :serviceModelInvariantUuid";
        Query query = getSession().createQuery(hql);
        query.setParameter ("serviceModelInvariantUuid", serviceModelInvariantUuid);
        VnfResource vnfResource = null;
        try {
            vnfResource = (VnfResource) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: serviceModelInvariantUuid='" + serviceModelInvariantUuid);
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for serviceModelInvariantUuid=" + serviceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for serviceModelInvariantUuid==" + serviceModelInvariantUuid);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: serviceModelInvariantUuid='" + serviceModelInvariantUuid + "' " + he.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: serviceModelInvariantUuid='" + serviceModelInvariantUuid + "' " + e.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid);

        	throw e;
        }
        if (vnfResource == null) {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleType", null);
        } else {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleType", null);
        }
        return vnfResource;
    }

    public VnfResource getVnfResourceByVnfUuid(String vnfResourceModelInvariantUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get vfModuleType with vnfResourceModelInvariantUuid " + vnfResourceModelInvariantUuid);

        String hql = "FROM VnfResource WHERE vnfResourceModelInvariantUuid = :vnfResourceModelInvariantUuid";
        Query query = getSession().createQuery(hql);
        query.setParameter("vnfResourceModelInvariantUuid", vnfResourceModelInvariantUuid);
        VnfResource vnfResource = null;
        try {
            vnfResource = (VnfResource) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: vnfResourceModelInvariantUuid='" + vnfResourceModelInvariantUuid);
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfResourceModelInvariantUuid=" + vnfResourceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for vnfResourceModelInvariantUuid==" + vnfResourceModelInvariantUuid);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: vnfResourceModelInvariantUuid='" + vnfResourceModelInvariantUuid + "' " + he.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for vnfResourceModelInvariantUuid=" + vnfResourceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnfResourceModelInvariantUuid=" + vnfResourceModelInvariantUuid);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: vnfResourceModelInvariantUuid='" + vnfResourceModelInvariantUuid + "' " + e.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for vnfResourceModelInvariantUuid=" + vnfResourceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for vnfResourceModelInvariantUuid=" + vnfResourceModelInvariantUuid);

        	throw e;
        }
        if (vnfResource == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleType", null);
        } else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleType", null);
        }
        return vnfResource;
    }

    public VnfResource getVnfResourceByType(String vnfType) {
        return this.getVnfResource(vnfType);
    }

    public VfModule getVfModuleByModelInvariantUuid(String modelInvariantUUID) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get vfModuleTypeByModelInvariantUuid with uuid " + modelInvariantUUID);

        String hql = "FROM VfModule WHERE modelInvariantUUID = :modelInvariantUUID ";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("modelInvariantUUID", modelInvariantUUID);
        List<VfModule> modules = this.executeQueryMultipleRows(hql, parameters, true);
        VfModule module = null;
        
        if (modules != null && ! modules.isEmpty()) {
        	modules.sort(new MavenLikeVersioningComparator());
        	Collections.reverse (modules);
        	module =  modules.get(0);
        }
  
        if (module == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleByModelInvariantUuid", null);
        } else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelInvariantUuid", null);
        }
        return module;
    }

    public VfModuleCustomization getVfModuleByModelCustomizationUuid(String modelCustomizationUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get vfModuleTypeByModelCustomizationUuid with uuid " + modelCustomizationUuid);

        String hql = "FROM VfModuleCustomization WHERE modelCustomizationUuid = :modelCustomizationUuid ";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelCustomizationUuid", modelCustomizationUuid);
        VfModuleCustomization module = null;
        try {
        	module = (VfModuleCustomization) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelCustomizationUuid='" + modelCustomizationUuid + "'");
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vfModuleModelInvariantUuid=" + modelCustomizationUuid , "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelCustomizationUuid==" + modelCustomizationUuid);

        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + he.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid);

        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + e.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid);

        	throw e;
        }
        if (module == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleByModelCustomizationUuid", null);
        } else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelCustomizationUuid", null);
        }
        return module;
    }

    
    public VfModule getVfModuleByModelInvariantUuidAndModelVersion(String modelInvariantUuid, String modelVersion) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVfModuleByModelInvariantUuidAndModelVersion with modelInvariantUuid: " + modelInvariantUuid + ", modelVersion: " + modelVersion);

        String hql = "FROM VfModule WHERE modelInvariantUUID = :modelInvariantUuid and version = :modelVersion";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelInvariantUuid", modelInvariantUuid);
        query.setParameter("modelVersion", modelVersion);
        VfModule module = null;
        try {
        	module = (VfModule) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelInvariantUuid='" + modelInvariantUuid + "', modelVersion='" +modelVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vfModule ModelInvariantUuid=" + modelInvariantUuid + " modelVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for ModelInvariantUuid==" + modelInvariantUuid + " modelVersion==" + modelVersion);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelInvariantUuid='" + modelInvariantUuid + "', modelVersion='" +modelVersion + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelInvariantUuid=" + modelInvariantUuid + " modelVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelInvariantUuid=" + modelInvariantUuid + " modelVersion=" + modelVersion);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelInvariantUuid='" + modelInvariantUuid + "', modelVersion='" +modelVersion + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelInvariantUuid=" + modelInvariantUuid + " modelVersion=" + modelVersion, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelInvariantUuid=" + modelInvariantUuid + " modelVersion=" + modelVersion);
        	throw e;
        }
        if (module == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleByModelInvariantUuidAndModelVersion", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelInvariantUuidAndModelVersion", null);
        }
        return module;
    }
    
    /**
     * Return the VfModuleCustomization object identified by the given modelCustomizationUuid 1707
     *
     * @param modelCustomizationUuid
     * @return VfModuleCustomization or null if not found
     */
    public VfModuleCustomization getVfModuleCustomizationByModelCustomizationId(String modelCustomizationUuid) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVfModuleCustomizationByModelCustomizationId with modelCustomizationUuid: " + modelCustomizationUuid);

        String hql = "FROM VfModuleCustomization WHERE modelCustomizationUuid = :modelCustomizationUuid";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelCustomizationUuid", modelCustomizationUuid);
        VfModuleCustomization VfModuleCustomization = null;
        try {
        	VfModuleCustomization = (VfModuleCustomization) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: modelCustomizationUuid='" + modelCustomizationUuid +"'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vfModuleCustomization modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelCustomizationUuid==" + modelCustomizationUuid);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid);
        	throw e;
        }
        if (VfModuleCustomization != null) {
        	LOGGER.debug("Found VMC of " + VfModuleCustomization.getModelCustomizationUuid() + ", now looking for vfModule=" + VfModuleCustomization.getVfModuleModelUuid());
        	VfModuleCustomization.setVfModule(this.getVfModuleByModelUuid(VfModuleCustomization.getVfModuleModelUuid()));
        }

        if (VfModuleCustomization == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleCustomizationByModelCustomizationId", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleCustomizationByModelCustomizationId", null);
        }
        return VfModuleCustomization;
    }
    
    /**
     * Return the VfModule object identified by the given modelUuid 1707
     * per Mike Z. - this may return more than one row - and even if it does, 
     * the heat template will be the same - so just return any of the rows.
     *
     * @param modelUuid
     * @return VfModule or null if not found
     */
    public VfModule getVfModuleByModelUuid(String modelUuid) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVfModuleByModelUuid with modelUuid: " + modelUuid);

        String hql = "FROM VfModule WHERE modelUUID = :modelUuidValue";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelUuidValue", modelUuid);
        List<VfModule> vfModules = null;
        try {
        	vfModules = query.list ();
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching VfModule for: modelUuid='" + modelUuid + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching VfModule for modelUuid=" + modelUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelUuid=" + modelUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching VfModule for: modelUuid='" + modelUuid + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for modelUuid=" + modelUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelUuid=" + modelUuid);
        	throw e;
        }

        if (vfModules == null || vfModules.isEmpty()) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleByModelUuid", null);
        	return null;
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelUuid", null);
        }
        return vfModules.get(0);
    }
    /**
     * Return the VnfResourceCustomization object identified by the given modelCustomizationUuid 1707
     * Note that the corresponding VnfResource Object will be put in the VnfResourceCustomization bean
     *
     * @param modelCustomizationUuid
     * @return VnfResourceCustomization or null if not found
     */
    public VnfResourceCustomization getVnfResourceCustomizationByModelCustomizationUuid(String modelCustomizationUuid) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVnfResourceByModelCustomizatonUuid with modelCustomizationUuid: " + modelCustomizationUuid);

        String hql = "FROM VnfResourceCustomization WHERE modelCustomizationUuid = :modelCustomizationUuid";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelCustomizationUuid", modelCustomizationUuid);
        VnfResourceCustomization vnfResourceCustomization = null;
        try {
        	vnfResourceCustomization = (VnfResourceCustomization) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row in VRC - data integrity error: modelCustomizationUuid='" + modelCustomizationUuid +"'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfResourceCustomization modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelCustomizationUuid==" + modelCustomizationUuid);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching VRC for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching VRC for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching VRC for: modelCustomizationUuid='" + modelCustomizationUuid + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching VRC for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelCustomizationUuid=" + modelCustomizationUuid);
        	throw e;
        }
        if (vnfResourceCustomization != null) {
        	LOGGER.debug("Found VRC of " + vnfResourceCustomization.getModelCustomizationUuid() + ", now looking for vnfResource=" + vnfResourceCustomization.getVnfResourceModelUuid() );
        	vnfResourceCustomization.setVnfResource(this.getVnfResourceByModelUuid(vnfResourceCustomization.getVnfResourceModelUuid()));
        	LOGGER.debug("Now looking for vfModules for " + vnfResourceCustomization.getModelCustomizationUuid());
        	vnfResourceCustomization.setVfModuleCustomizations(this.getAllVfModuleCustomizations(vnfResourceCustomization.getModelCustomizationUuid()));
        }

        if (vnfResourceCustomization == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResourceCustomizationByModelCustomizationUuid", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceCustomizationByModelCustomizationUuid", null);
        }
        return vnfResourceCustomization;
    }
    
    /**
     * Return the VnfResourceCustomization object identified by the given modelCustomizationUuid 1707
     * Note that the corresponding VnfResource Object will be put in the VnfResourceCustomization bean
     *
     * @param getVnfResourceCustomizationByModelVersionId
     * @return VnfResourceCustomization or null if not found
     */
    public VnfResourceCustomization getVnfResourceCustomizationByModelVersionId(String modelVersionId) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVnfResourceCustomizationByModelVersionId with modelVersionId: " + modelVersionId);

        String hql = "FROM VnfResourceCustomization WHERE vnfResourceModelUuid = :modelVersionId";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelVersionId", modelVersionId);
        VnfResourceCustomization vnfResourceCustomization = null;
        try {
        	vnfResourceCustomization = (VnfResourceCustomization) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row in VRC - data integrity error: modelVersionId='" + modelVersionId +"'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfResourceCustomization modelVersionId=" + modelVersionId, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelVersionId==" + modelVersionId);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching VRC for: modelVersionId='" + modelVersionId + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching VRC for modelVersionId=" + modelVersionId, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelVersionId=" + modelVersionId);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching VRC for: modelVersionId='" + modelVersionId + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching VRC for modelVersionId=" + modelVersionId, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelVersionId=" + modelVersionId);
        	throw e;
        }
        if (vnfResourceCustomization != null) {
        	LOGGER.debug("Found VRC of " + vnfResourceCustomization.getModelCustomizationUuid() + ", now looking for vnfResource=" + vnfResourceCustomization.getVnfResourceModelUuid() );
        	vnfResourceCustomization.setVnfResource(this.getVnfResourceByModelUuid(vnfResourceCustomization.getVnfResourceModelUuid()));
        	LOGGER.debug("Now looking for vfModules for " + vnfResourceCustomization.getModelCustomizationUuid());
        	vnfResourceCustomization.setVfModuleCustomizations(this.getAllVfModuleCustomizations(vnfResourceCustomization.getModelCustomizationUuid()));
        }

        if (vnfResourceCustomization == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResourceCustomizationByModelVersionId", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceCustomizationByModelVersionId", null);
        }
        return vnfResourceCustomization;
    }
    
    /**
     * Return the VfModule object identified by the given modelCustomizationId, modelVersionId 1707
     *
     * @param modelVersionId, modelCustomizationId
     * @return VfModule or null if not found
     */
    public VfModule getVfModuleByModelCustomizationIdAndVersion(String modelCustomizationId, String modelVersionId) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVfModuleByModelCustomizationIdAndVersion with modelVersionId: " + modelVersionId + " modelCustomizationId: " + modelCustomizationId);

//      select * from vf_module vfm where vfm.MODEL_UUID IN (
//      select vfmc.VF_MODULE_MODEL_UUID from vf_module_customization vfmc where vfmc.MODEL_CUSTOMIZATION_UUID='222bd8f2-341d-4419-aa0e-98398fa34050')
//      and vfm.MODEL_UUID = 'fa1c8558-006c-4fb6-82f2-4fc0646d6b06';
        
        String hql = "Select vfm FROM VfModule as vfm WHERE vfm.modelUUID IN ("
        		+ "SELECT vfmc.vfModuleModelUuid FROM VfModuleCustomization as vfmc "
        		+ "WHERE vfmc.modelCustomizationUuid = :modelCustomizationId) "
        		+ "AND vfm.modelUUID = :modelVersionId";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelVersionId", modelVersionId);
        query.setParameter ("modelCustomizationId", modelCustomizationId);
        VfModule vfModule = null;
        try {
        	vfModule = (VfModule) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row in VRC - data integrity error: modelVersionId='" + modelVersionId +"' modelCustomizationId='" + modelCustomizationId + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfResourceCustomization modelVersionId=" + modelVersionId, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelVersionId=" + modelVersionId + " modelCustomizationId=" + modelCustomizationId);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching VRC for: modelVersionId='" + modelVersionId + "' modelCustomizationId='" + modelCustomizationId + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching VRC for modelVersionId=" + modelVersionId, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelVersionId=" + modelVersionId + " modelCustomizationId=" + modelCustomizationId);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching VRC for: modelVersionId='" + modelVersionId + "' modelCustomizationId='" + modelCustomizationId + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching VRC for modelVersionId=" + modelVersionId, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelVersionId=" + modelVersionId + " modelCustomizationId=" + modelCustomizationId);
        	throw e;
        }

        if (vfModule == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleByModelCustomizationIdAndVersion", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelCustomizationIdAndVersion", null);
        }
        return vfModule;
    }
    
    /**
     * Return the VfModule object identified by the given modelCustomizationId, modelVersion, modelInvariantId 1707
     *
     * @param modelCustomizationId, modelVersion, modelInvariantId
     * @return VfModule or null if not found
     */
    public VfModule getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId(String modelCustomizationId, String modelVersion, String modelInvariantId) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId with modelVersionId: " + modelVersion);

        //select * from vf_module vfm left outer join vf_module_customization vfmc on vfmc.VF_MODULE_MODEL_UUID = vfm.MODEL_UUID 
//        where vfmc.MODEL_CUSTOMIZATION_UUID='52643a8e-7953-4e48-8eab-97165b2b3a4b' and vfm.MODEL_UUID = ''
        
        String hql = "Select vfm FROM VfModule as vfm LEFT OUTER JOIN VfModuleCustomization as vfmc on vfmc.vfModuleModelUuid = vfm.modelUUID"
        		+ "WHERE vfmc.modelCustomizationUuid = :modelCustomizationId AND vfm.modelInvariantUUID = :modelInvariantId AND vfm.modelVersion = :modelVersion";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelInvariantId", modelInvariantId);
        query.setParameter ("modelCustomizationId", modelCustomizationId);
        query.setParameter ("modelVersion", modelVersion);
        VfModule vfModule = null;
        try {
        	vfModule = (VfModule) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row in VRC - data integrity error: modelInvariantId='" + modelInvariantId +"' modelVersion='" + modelVersion + "' modelCustomizationId='" + modelCustomizationId +"'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfResourceCustomization modelInvariantId=" + modelInvariantId, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelInvariantId=" + modelInvariantId + " modelVersion=" + modelVersion + " modelCustomizationId=" + modelCustomizationId);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching VRC for: modelInvariantId='" + modelInvariantId + "' modelVersion='" + modelVersion + "' modelCustomizationId='" + modelCustomizationId + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching VRC for modelInvariantId=" + modelInvariantId, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelInvariantId=" + modelInvariantId + " modelVersion=" + modelVersion + " modelCustomizationId=" + modelCustomizationId);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching VRC for: modelInvariantId='" + modelInvariantId + "' modelVersion='" + modelVersion + "' modelCustomizationId='" + modelCustomizationId + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching VRC for modelInvariantId=" + modelInvariantId, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelInvariantId=" + modelInvariantId + " modelVersion=" + modelVersion + " modelCustomizationId=" + modelCustomizationId);
        	throw e;
        }

        if (vfModule == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId", null);
        }
        return vfModule;
    }
    
    /**
     * Return the VnfResourceCustomization object identified by the given modelCustomizationName, modelInvariantId and modelVersion 1707
     *
     * @param modelInvariantId, modelVersion, modelCustomizationName
     * @return VnfResourceCustomization or null if not found
     */
    public VnfResourceCustomization getVnfResourceCustomizationByModelInvariantId(String modelInvariantId, String modelVersion, String modelCustomizationName) {
    	long startTime = System.currentTimeMillis();
    	LOGGER.debug ("Catalog database - get getVnfResourceCustomizationByModelInvariantId with modelInvariantId: " + modelInvariantId + ", modelVersion: " 
						+ modelVersion + ", modelCustomizationName: " + modelCustomizationName);
        
        String hql = "SELECT VnfResourceCustomization FROM VnfResourceCustomization as vrc "
        			+ "LEFT OUTER JOIN VnfResource as vr "
        			+ "on vr.modelUuid =vrc.vnfResourceModelUuid "
        			+ "WHERE vr.modelInvariantUuid = :modelInvariantId AND vr.modelVersion = :modelVersion AND vrc.modelInstanceName = :modelCustomizationName";
        
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelInvariantId", modelInvariantId);
        query.setParameter("modelVersion", modelVersion);
        query.setParameter("modelCustomizationName", modelCustomizationName);
        VnfResourceCustomization vnfResourceCustomization = null;
        try {
        	vnfResourceCustomization = (VnfResourceCustomization) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row in VRC - data integrity error: modelInvariantId='" + modelInvariantId +"' and modelVersion='" + modelVersion + "' modelCustomizationName='" + modelCustomizationName + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfResourceCustomization modelInvariantId=" + modelInvariantId, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for modelInvariantId==" + modelInvariantId+"' and modelVersion='" + modelVersion + "'modelCustomizationName='" + modelCustomizationName + "'");
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching VRC for: modelInvariantId='" + modelInvariantId +"' and modelVersion='" + modelVersion + "'modelCustomizationName='" + modelCustomizationName + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching VRC for modelInvariantId=" + modelInvariantId, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelInvariantId=" + modelInvariantId+"' and modelVersion='" + modelVersion + "'modelCustomizationName='" + modelCustomizationName + "'");
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching VRC for: modelInvariantId='" + modelInvariantId +"' and modelVersion='" + modelVersion + "' " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching VRC for modelInvariantId=" + modelInvariantId, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for modelInvariantId=" + modelInvariantId+"' and modelVersion='" + modelVersion + "'modelCustomizationName='" + modelCustomizationName + "'");
        	throw e;
        }
        if (vnfResourceCustomization != null) {
        	LOGGER.debug("Found VRC of " + vnfResourceCustomization.getModelCustomizationUuid() + ", now looking for vnfResource=" + vnfResourceCustomization.getVnfResourceModelUuid() );
        	vnfResourceCustomization.setVnfResource(this.getVnfResourceByModelUuid(vnfResourceCustomization.getVnfResourceModelUUID()));
        	LOGGER.debug("Now looking for vfModules for " + vnfResourceCustomization.getModelCustomizationUuid());
        	vnfResourceCustomization.setVfModuleCustomizations(this.getAllVfModuleCustomizations(vnfResourceCustomization.getModelCustomizationUuid()));
        }

        if (vnfResourceCustomization == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResourceCustomizationByModelInvariantId", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceCustomizationByModelInvariantId", null);
        }
        return vnfResourceCustomization;
    }
    
    /**
     * Return list of VnfResourceCustomization objects identified by the given modelCustomizationUuid 1707
     *
     * @param modelCustomizationUuid
     * @return List<VfModuleCustomization> or null if not found
     */
    public List<VfModuleCustomization> getVfModuleCustomizationByVnfModuleCustomizationUuid(String modelCustomizationUuid) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get getVfModuleCustomizationByVnfModuleCustomizationUuid with modelCustomizationUuid: " + modelCustomizationUuid);
        
//      select * from vf_module_customization as vfmc where vfmc.MODEL_CUSTOMIZATION_UUID IN(
//      select vrcmc.VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID from vnf_res_custom_to_vf_module_custom as vrcmc
//      where vrcmc.VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID = 'd279139c-4b85-48ff-8ac4-9b83a6fc6da7') 
        
        String hql = "SELECT vfmc FROM VfModuleCustomization as vfmc where vfmc.modelCustomizationUuid "
        			+ "IN(select vrcmc.vfModuleCustModelCustomizationUuid from VnfResCustomToVfModuleCustom as vrcmc "
        					+ "WHERE vrcmc.vnfResourceCustModelCustomizationUuid = :modelCustomizationUuid)";
        
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelCustomizationUuid", modelCustomizationUuid);
        List<VfModuleCustomization> resultList = null;
        try {
        	resultList = query.list();
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - getVfModuleCustomizationByVnfModuleCustomizationUuid - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + " " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception - getVfModuleCustomizationByVnfModuleCustomizationUuid - searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid);
        	throw he;
    	} catch (Exception e) {
        	LOGGER.debug("Exception - getVfModuleCustomizationByVnfModuleCustomizationUuid - while searching for: modelCustomizationUuid='" + modelCustomizationUuid + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception - getVfModuleCustomizationByVnfModuleCustomizationUuid - searching for modelCustomizationUuid=" + modelCustomizationUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for modelCustomizationUuid=" + modelCustomizationUuid);
        	throw e;
    	}

        if (resultList == null) {
    		resultList = new ArrayList<>();
    	}
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleCustomizationByVnfModuleCustomizationUuid", null);
    	return resultList;
    }
    
    /**
     * Return the newest version of a specific VNF resource Customization (queried by modelCustomizationName and modelVersionId).
     *
     * @return {@link VnfResourceCustomization} object or null if none found
     */
    public VnfResourceCustomization getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId (String modelCustomizationName, String modelVersionId) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VNF resource Customization with modelCustomizationName " + modelCustomizationName + " modelUUID " + modelVersionId);

        String hql = "SELECT vrc FROM VnfResourceCustomization as vrc WHERE vrc.vnfResourceModelUuid IN "
					+ "(SELECT vr.modelUuid FROM VnfResource vr "
					+ "WHERE vr.modelUuid = :modelVersionId)"
					+ "AND vrc.modelInstanceName = :modelCustomizationName";
		
        Query query = getSession ().createQuery (hql);
        query.setParameter ("modelCustomizationName", modelCustomizationName);
        query.setParameter ("modelVersionId", modelVersionId);

        @SuppressWarnings("unchecked")
        List <VnfResourceCustomization> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VnfResourceCustomization not found", "CatalogDB", "getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId", null);
            return null;
        }
        
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId", null);
        return resultList.get (0);
    }
    
    public ArrayList<VfModuleCustomization> getAllVfModuleCustomizations(String vnfResourceCustomizationMCU) {
        LOGGER.debug ("Catalog database - getAllVfModuleCustomizations with vnfResourceCustomizationMCU " + vnfResourceCustomizationMCU);
        
        List<VnfResCustomToVfModuleCustom> matches = this.getVRCtoVFMC(vnfResourceCustomizationMCU, null); 
        if (matches == null || matches.isEmpty()) {
        	LOGGER.debug("Found no vf modules for " + vnfResourceCustomizationMCU);
        	return new ArrayList<>();
        }
        ArrayList<VfModuleCustomization> list = new ArrayList<>();
        for (VnfResCustomToVfModuleCustom v : matches) {
        	String m = v.getVfModuleCustModelCustomizationUuid();
        	LOGGER.debug("VfModule to match: " + m);
        	VfModuleCustomization c = this.getVfModuleCustomizationByModelCustomizationId(m);
        	if (c != null) {
        		list.add(c);
        	} else {
        		LOGGER.debug("**UNABLE to find vfModule " + m);
        	}
        }
        return list;
    }
    
    /**
     * Return the VnfResourceCustomization object identified by the given modelCustomizationUuid 1707
     * Note that the corresponding VnfResource Object will be put in the VnfResourceCustomization bean
     *
     * @param modelCustomizationUuid
     * @return VnfResourceCustomization or null if not found
     */
    public VnfResource getVnfResourceByModelUuid(String modelUuid) {
    	long startTime = System.currentTimeMillis();
        LOGGER.debug ("Catalog database - get VnfResource with modelUuid " + modelUuid);

        String hql = "FROM VnfResource WHERE modelUuid = :modelUuid";
        Query query = getSession().createQuery(hql);
        query.setParameter ("modelUuid", modelUuid);
        VnfResource vnfResource = null;
        try {
        	vnfResource = (VnfResource) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique Vnf_Resource row - data integrity error: modelUuid='" + modelUuid);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for Vnf Resource modelUuid=" + modelUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for vnfResource modelUuid==" + modelUuid);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: VnfResource modelUuid='" + modelUuid + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for vnfResource ModelUuid=" + modelUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnfResource modelUuid=" + modelUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: vnfResource ModelUuid='" + modelUuid + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for vnfResource ModelUuid=" + modelUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for vnfResource modelUuid=" + modelUuid);
        	throw e;
        }
        if (vnfResource == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResourceByModelUuid", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceByModelUuid", null);
        }
        return vnfResource;	
    }
    
    public VnfResCustomToVfModuleCustom getVnfResCustomToVfModule(String vnfId, String vfId) {
    	long startTime = System.currentTimeMillis();
    	LOGGER.debug("Catalog database - getVnfResCustomToVfModule - vnfResourceCustModelCustUuid: " + vnfId + ", vfModuleCustModelCustomUuid=" + vfId);
    	StringBuilder hql = new StringBuilder("FROM VnfResCustomToVfModuleCustom where vnfResourceCustModelCustomizationUuid = :vnfIdValue and vfModuleCustModelCustomizationUuid = :vfIdValue"); 	
    	HashMap<String, String> parameters = new HashMap<>();
    	parameters.put("vnfIdValue", vnfId);
    	parameters.put("vfIdValue", vfId);
    	VnfResCustomToVfModuleCustom vrctvmc = this.executeQuerySingleRow(hql.toString(), parameters, true);
        if (vrctvmc == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVnfResCustomToVfModule", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResCustomToVfModule", null);
        }
        return vrctvmc;
    }

    public List<VfModule> getVfModulesForVnfResource(VnfResource vnfResource) {
        if (vnfResource == null)
            return Collections.EMPTY_LIST;
    	String vnfResourceModelUuid = vnfResource.getModelUuid();

        LOGGER.debug("Catalog database - getVfModulesForVnfResource - vnfResource: " + vnfResource.toString());

    	return this.getVfModulesForVnfResource(vnfResourceModelUuid);

    }

    public List<VfModule> getVfModulesForVnfResource(String vnfResourceModelUuid) {
        long startTime = System.currentTimeMillis();
    	LOGGER.debug("Catalog database - getVfModulesForVnfResource - vnfResourceModelUuid: " + vnfResourceModelUuid);
    	StringBuilder hql = new StringBuilder("FROM VfModule where vnfResourceModelUUId = :vnfResourceModelUUId");
        Query query = getSession().createQuery(hql.toString());
    	query.setParameter("vnfResourceModelUUId", vnfResourceModelUuid);
        List<VfModule> resultList = null;
        try {
            resultList = query.list();
            if (resultList != null)
                LOGGER.debug("\tQuery found " + resultList.size() + " records.");
            else
                LOGGER.debug("\tQuery found no records.");
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - getVfModulesForVnfResource - while searching for: vnfResourceModelUUId='" + vnfResourceModelUuid + " " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception - getVfModulesForVnfResource - searching for vnfResourceModelUUId=" + vnfResourceModelUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnfResourceModelUUId=" + vnfResourceModelUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Exception - getVfModulesForVnfResource - while searching for: vnfResourceModelUUId='" + vnfResourceModelUuid + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception - getVfModulesForVnfResource - searching for vnfResourceModelUUId=" + vnfResourceModelUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnfResourceModelUUId=" + vnfResourceModelUuid);
        	throw e;
        }
        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModulesForVnfResource", null);
        return resultList;
    }

    public Service getServiceByUuid (String serviceModelInvariantUuid) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get service with ModelInvariantUuid " + serviceModelInvariantUuid);

        String hql = "FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("serviceModelInvariantUuid", serviceModelInvariantUuid);

        Service service = null;
        try {
            service = (Service) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: serviceModelInvariantUuid='" + serviceModelInvariantUuid + "'");
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for serviceModelInvariantUuid=" + serviceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for serviceModelInvariantUuid=" + serviceModelInvariantUuid);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: serviceName='" + serviceModelInvariantUuid + "' " + he.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: serviceModelInvariantUuid='" + serviceModelInvariantUuid + " " + e.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for serviceModelInvariantUuid=" + serviceModelInvariantUuid);
        	throw e;
        }
        if (service == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getService", null);
        } else {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getService", null);
        }

        return service;
    }

    public NetworkResource getNetworkResourceById(Integer id) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getNetworkResource with id " + id);

        String hql = "FROM NetworkResource WHERE id = :id";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("id", id);

        NetworkResource networkResource = null;
        try {
            networkResource = (NetworkResource) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
            LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: NETWORK_RESOURCE.id='" + id + "'");
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for NETWORK_RESOURCE.id=" + id, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for NETWORK_RESOURCE.id=" + id);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: NETWORK_RESOURCE.id='" + id + "' " + he.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for NETWORK_RESOURCE.id=" + id, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for NETWORK_RESOURCE.id=" + id);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: NETWORK_RESOURCE.id='" + id + " " + e.getMessage());
            LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for NETWORK_RESOURCE.id=" + id, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for NETWORK_RESOURCE.id=" + id);
        	throw e;
        }

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkResourceById", null);
        return networkResource;

    }

    public NetworkResource getNetworkResourceById(String id) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getNetworkResource with model_uuid " + id);

        String hql = "FROM NetworkResource WHERE modelUUID = :model_uuid";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("model_uuid", id);
        
        List<NetworkResource> networkResources = null;
        try {
        	networkResources = query.list ();
    	} catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: NETWORK_RESOURCE.id='" + id + "' " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for NETWORK_RESOURCE.model_uuid=" + id, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for NETWORK_RESOURCE.model_uuid=" + id);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: NETWORK_RESOURCE.id='" + id + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for NETWORK_RESOURCE.model_uuid=" + id, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for NETWORK_RESOURCE.model_uuid=" + id);
        	throw e;
        }
        
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkResourceById", null);
        if (networkResources == null || networkResources.isEmpty())
        	return null;
        else
        	return networkResources.get(0);
    }
    
    // 1707 API Spec
    
    public static boolean isEmptyOrNull(String str) {
    	if (str == null) 
    		return true;
    	if ("null".equals(str))
    		return true;
    	if ("".equals(str))
    		return true;
    	return false;
    }
    
    public List<ServiceToResourceCustomization> getSTR(String serviceModelUuid, String resourceModelCustomizationUuid, String modelType) {
    	LOGGER.debug("Catalog database: getSTR - smu=" + serviceModelUuid + ", rmcu=" + resourceModelCustomizationUuid + ", modelType = " + modelType);
    	
    	if (isEmptyOrNull(serviceModelUuid) && isEmptyOrNull(resourceModelCustomizationUuid) && isEmptyOrNull(modelType)) 
    		return null;
    	
    	StringBuilder hql = new StringBuilder("FROM ServiceToResourceCustomization WHERE ");
    	boolean first = true;
    	if (serviceModelUuid != null && !serviceModelUuid.equals("")) {
    		hql.append("serviceModelUUID = :smu");
    		first = false;
    	}
    	if (resourceModelCustomizationUuid != null && !resourceModelCustomizationUuid.equals("")) {
    		if (!first) {
    			hql.append(" AND ");
    		}
    		hql.append("resourceModelCustomizationUUID = :rmcu");
    		first = false;
    	}
    	if (modelType != null && !modelType.equals("")) {
    		if (!first) {
    			hql.append(" AND ");
    			first = false;
    		}
    		hql.append("modelType = :modelType");
    		first = false;
    	}
    	Query query = getSession().createQuery(hql.toString());
    	if (hql.toString().contains(":smu")) 
    		query.setParameter("smu", serviceModelUuid);
    	if (hql.toString().contains(":rmcu")) 
    		query.setParameter("rmcu", resourceModelCustomizationUuid);
    	if (hql.toString().contains(":modelType")) 
    		query.setParameter("modelType", modelType);
        LOGGER.debug("query - " + hql.toString());
    	
    	@SuppressWarnings("unchecked")
    	List<ServiceToResourceCustomization> resultList = query.list();
        if (resultList == null || resultList.isEmpty()) {
        	LOGGER.debug("Found no matches to the query - " + hql.toString());
        	return new ArrayList<>();
        }
    	return resultList;
    }
    
    public List<VnfResCustomToVfModuleCustom> getVRCtoVFMC (String vrc_mcu, String vfmc_mcu) {
    	LOGGER.debug("Catalog database: getVRCtoVFMC - vrc_mcu=" + vrc_mcu + ", vfmc_mcu=" + vfmc_mcu);
    	
    	if (isEmptyOrNull(vrc_mcu) && isEmptyOrNull(vfmc_mcu))
    		return null;
    	
    	StringBuilder hql = new StringBuilder("FROM VnfResCustomToVfModuleCustom WHERE ");
    	boolean first = true;
    	if (vrc_mcu != null && !vrc_mcu.equals("")) {
    		hql.append("vnfResourceCustModelCustomizationUuid = :vrc_mcu");
    		first = false;
    	}
    	if (vfmc_mcu != null && !vfmc_mcu.equals("")) {
    		if (!first) {
    			hql.append(" AND ");
    		}
    		hql.append("vfModuleCustModelCustomizationUuid = :vfmc_mcu");
    		first = false;
    	}
    	Query query = getSession().createQuery(hql.toString());
    	if (hql.toString().contains(":vrc_mcu")) 
    		query.setParameter("vrc_mcu", vrc_mcu);
    	if (hql.toString().contains(":vfmc_mcu")) 
    		query.setParameter("vfmc_mcu", vfmc_mcu);
    	@SuppressWarnings("unchecked")
    	List<VnfResCustomToVfModuleCustom> resultList = query.list();
        if (resultList == null || resultList.isEmpty()) {
        	LOGGER.debug("Found no matches to the query - " + hql.toString());
        	return new ArrayList<>();
        }
    	return resultList;
    }
    
    @SuppressWarnings("unchecked")
    public List <TempNetworkHeatTemplateLookup> getTempNetworkHeatTemplateLookup (String networkResourceModelName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - GetTempNetworkHeatTemplateLookup for Network Name " + networkResourceModelName);

        String hql = "FROM TempNetworkHeatTemplateLookup where networkResourceModelName = :networkResourceModelName";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("networkResourceModelName", networkResourceModelName);

        List <TempNetworkHeatTemplateLookup> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getTempNetworkHeatTemplateLookup", null);
        return result;
    }
    
    // 1702 API Spec - Query for all networks in a Service:
    public List<NetworkResourceCustomization> getAllNetworksByServiceModelUuid(String serviceModelUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getServiceNetworksByServiceModelUuid - " + serviceModelUuid);

    	List<ServiceToResourceCustomization> strMappings = this.getSTR(serviceModelUuid, null, "network");
    	if (strMappings == null || strMappings.isEmpty()) {
    		LOGGER.debug("Found NO matches for NRC with ServiceModelUuid=" + serviceModelUuid);
            return new ArrayList<>();
        }
        LOGGER.debug("Found " + strMappings.size() + " entries in ServiceToResourceCustomizations.network with smu=" + serviceModelUuid); 

        ArrayList<NetworkResourceCustomization> masterList = new ArrayList<>();
        for (ServiceToResourceCustomization stn : strMappings) {
        	String networkModelCustomizationUuid = stn.getResourceModelCustomizationUUID();
            LOGGER.debug("Now searching for NetworkResourceCustomization for " + networkModelCustomizationUuid);
            List<NetworkResourceCustomization> resultSet = this.getAllNetworksByNetworkModelCustomizationUuid(networkModelCustomizationUuid);
            masterList.addAll(resultSet);
        }
        LOGGER.debug("Returning " + masterList.size() + " NRC records");
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllNetworksByServiceModelUuid", null);
        return masterList;
    }
    public List<NetworkResourceCustomization> getAllNetworksByServiceModelInvariantUuid(String serviceModelInvariantUuid) {
        LOGGER.debug("Catalog database: getServiceNetworksByServiceModelInvariantUuid - " + serviceModelInvariantUuid);

        StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("Could not find Service for " + serviceModelInvariantUuid);
            return new ArrayList<>();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        String serviceNameVersionId = service.getModelUUID();
        LOGGER.debug("The highest version for the Service " + serviceModelInvariantUuid + " is " + serviceNameVersionId);

        // Service.serviceNameVersionId == ServiceToNetworks.serviceModelUuid
        return this.getAllNetworksByServiceModelUuid(serviceNameVersionId);
    }
    public List<NetworkResourceCustomization> getAllNetworksByServiceModelInvariantUuid(String serviceModelInvariantUuid, String serviceModelVersion) {
        LOGGER.debug("Catalog database: getServiceNetworksByServiceModelInvariantUuid - " + serviceModelInvariantUuid + ", version=" + serviceModelVersion);

        StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid and version = :serviceModelVersion");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        query.setParameter("serviceModelVersion", serviceModelVersion);

        //TODO
        //can fix this later - no time - could do a unique query here - but this should work
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("No Service found with smu=" + serviceModelInvariantUuid + " and smv=" + serviceModelVersion);
            return new ArrayList<>();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        String serviceNameVersionId = service.getModelUUID();

        // Service.serviceNameVersionId == ServiceToNetworks.serviceModelUuid
        return this.getAllNetworksByServiceModelUuid(serviceNameVersionId);

    }
    public List<NetworkResourceCustomization> getAllNetworksByNetworkModelCustomizationUuid(String networkModelCustomizationUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllNetworksByNetworkModelCustomizationUuid - " + networkModelCustomizationUuid);

        StringBuilder hql = new StringBuilder("FROM NetworkResourceCustomization WHERE modelCustomizationUuid = :networkModelCustomizationUuid");
    	//Query query = getSession().createQuery(hql.toString());
    	//query.setParameter("networkModelCustomizationUuid", networkModelCustomizationUuid);
    	//LOGGER.debug("QUERY: " + hql.toString() + ", networkModelCustomizationUuid=" + networkModelCustomizationUuid);
    	
    	//@SuppressWarnings("unchecked")
    	//List<NetworkResourceCustomization> resultList = query.list();
    	
    	HashMap<String, String> params = new HashMap<>();
    	params.put("networkModelCustomizationUuid", networkModelCustomizationUuid);

    	List<NetworkResourceCustomization> resultList = this.executeQueryMultipleRows(hql.toString(), params, true);

    	if (resultList.isEmpty()) {
    		LOGGER.debug("Unable to find an NMC with nmcu=" + networkModelCustomizationUuid);
    		return new ArrayList<>();
    	}
    	for (NetworkResourceCustomization nrc : resultList) {
    		nrc.setNetworkResource(this.getNetworkResourceById(nrc.getNetworkResourceModelUuid()));
    	}
     	

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllNetworksByNetworkModelCustomizationUuid", null);
        return resultList;
    }
    
    public List<NetworkResourceCustomization> getAllNetworksByNetworkType(String networkType) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getServiceNetworksByNetworkType - " + networkType);

        NetworkResource nr = this.getNetworkResource(networkType);
        if (nr == null) {
            return new ArrayList<>();
        }
    	String networkResourceId = nr.getModelUUID();

        LOGGER.debug("Now searching for NRC's with networkResourceId = " + networkResourceId);
    	StringBuilder hql = new StringBuilder("FROM NetworkResourceCustomization WHERE networkResourceModelUuid = :networkResourceId");

        Query query = getSession().createQuery(hql.toString());
        query.setParameter("networkResourceId", networkResourceId);

        @SuppressWarnings("unchecked")
        List<NetworkResourceCustomization> resultList = query.list();

        if (resultList != null && ! resultList.isEmpty()) {
            LOGGER.debug("Found " + resultList.size() + " results");
            for (NetworkResourceCustomization nrc : resultList) {
                nrc.setNetworkType(networkType);
                nrc.setNetworkResource(nr);
            }
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllNetworksByNetworkType", null);

        return resultList;
    }
    public ArrayList<VfModuleCustomization> getAllVfmcForVrc(VnfResourceCustomization vrc) {
    	LOGGER.debug("Catalog database: getAllVfmcForVrc - " + vrc.getModelCustomizationUuid());
    	
    	List<VnfResCustomToVfModuleCustom> vfmcs = this.getVRCtoVFMC(vrc.getModelCustomizationUuid(), null);
    	if (vfmcs == null || vfmcs.isEmpty()) {
    		return new ArrayList<>();
    	}
    	ArrayList<VfModuleCustomization> vfModuleCusts = new ArrayList<>();
    	for (VnfResCustomToVfModuleCustom vfmc : vfmcs) {
    		VfModuleCustomization vfmcust = this.getVfModuleCustomizationByModelCustomizationId(vfmc.getVfModuleCustModelCustomizationUuid());
    		if (vfmcust != null) {
    			vfModuleCusts.add(vfmcust);
    		}
    	}
    	return vfModuleCusts;
    }

    //1702 API Spec cont'd - Query for all VnfResources in a Service:
    //1707 modified for db refactoring
    public List<VnfResourceCustomization> getAllVnfsByServiceModelUuid(String serviceModelUuid) {
        LOGGER.debug("Catalog database: getAllVnfsByServiceModelUuid - " + serviceModelUuid);

    	StringBuilder hql = new StringBuilder("FROM Service WHERE modelUUID = :serviceModelUuid");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelUuid", serviceModelUuid);
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
    		LOGGER.debug("Unable to find a service with modelUuid=" + serviceModelUuid);
    		return new ArrayList<>();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);

        // Step 2 - Now query to get the related VnfResourceCustomizations

        List<ServiceToResourceCustomization> strcs = this.getSTR(serviceModelUuid, null, "vnf");

        if (strcs.isEmpty()) {
    		LOGGER.debug("Unable to find any related vnfs to a service with modelUuid=" + serviceModelUuid);
        	return new ArrayList<>();
    }
        
        ArrayList<VnfResourceCustomization> allVrcs = new ArrayList<>();
        for (ServiceToResourceCustomization strc : strcs) {
        	LOGGER.debug("Try to find VRC for mcu=" + strc.getResourceModelCustomizationUUID());
        	VnfResourceCustomization vrc = this.getVnfResourceCustomizationByModelCustomizationUuid(strc.getResourceModelCustomizationUUID());
        	if (vrc != null)
        		allVrcs.add(vrc);
        }
        return allVrcs;
    	
    }
    public List<VnfResourceCustomization> getAllVnfsByServiceModelInvariantUuid(String serviceModelInvariantUuid) {
        LOGGER.debug("Catalog database: getAllVnfsByServiceModelInvariantUuid - " + serviceModelInvariantUuid);

        StringBuilder hqlService = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid");
        Query query = getSession().createQuery(hqlService.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        @SuppressWarnings("unchecked")
        List<Service> resultList = query.list();

        if (resultList.isEmpty()) {
    		return new ArrayList<>();
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);
        Service service = resultList.get(0);
        //now just call the method that takes the version - the service object will have the highest version
    	return this.getAllVnfsByServiceModelUuid(service.getModelUUID());
    }
    public List<VnfResourceCustomization> getAllVnfsByServiceModelInvariantUuid(String serviceModelInvariantUuid, String serviceModelVersion) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllVnfsByServiceModelInvariantUuid - " + serviceModelInvariantUuid + ", version=" + serviceModelVersion);

    	StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid and version = :serviceModelVersion");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        query.setParameter("serviceModelVersion", serviceModelVersion);

        @SuppressWarnings("unchecked")
    	List<Service> resultList = query.list();

        if (resultList.isEmpty()) {
    		return new ArrayList<>();
                }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);
        Service service = resultList.get(0);
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllVnfsByServiceModelInvariantUuid", null);
    	return this.getAllVnfsByServiceModelUuid(service.getModelUUID());
            }

    public List<VnfResourceCustomization> getAllVnfsByServiceName(String serviceName, String serviceVersion)  {
        LOGGER.debug("Catalog database: getAllVnfsByServiceName - " + serviceName + ", version=" + serviceVersion);
        if (serviceVersion == null || serviceVersion.equals("")) {
            return this.getAllVnfsByServiceName(serviceName);
        }

    	StringBuilder hql = new StringBuilder("FROM Service WHERE modelName = :serviceName and version = :serviceVersion");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceName", serviceName);
        query.setParameter("serviceVersion", serviceVersion);

        @SuppressWarnings("unchecked")
        List<Service> resultList = query.list();

        if (resultList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Service service = resultList.get(0);
    	return this.getAllVnfsByServiceModelUuid(service.getModelUUID());
    }
    public List<VnfResourceCustomization> getAllVnfsByServiceName(String serviceName) {
        LOGGER.debug("Catalog database: getAllVnfsByServiceName - " + serviceName);

    	StringBuilder hql = new StringBuilder("FROM Service WHERE modelName = :serviceName");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceName", serviceName);

        @SuppressWarnings("unchecked")
        List<Service> resultList = query.list();

        if (resultList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);
        Service service = resultList.get(0);

    	return this.getAllVnfsByServiceModelUuid(service.getModelUUID());
    }

    public List<VnfResourceCustomization> getAllVnfsByVnfModelCustomizationUuid(String vnfModelCustomizationUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllVnfsByVnfModelCustomizationUuid - " + vnfModelCustomizationUuid);

    	StringBuilder hql1 = new StringBuilder("FROM VnfResourceCustomization WHERE modelCustomizationUuid = :vrcmcu");
    	Query query1 = getSession().createQuery(hql1.toString());
    	query1.setParameter("vrcmcu", vnfModelCustomizationUuid);
        @SuppressWarnings("unchecked")
    	List<VnfResourceCustomization> resultList1 = query1.list();

    	if (resultList1.isEmpty()) {
            LOGGER.debug("Found no records matching " + vnfModelCustomizationUuid);
            return Collections.EMPTY_LIST;
        }
    	
    	for (VnfResourceCustomization vrc : resultList1) {
    		VnfResource vr = this.getVnfResourceByModelUuid(vrc.getVnfResourceModelUuid());
    		vrc.setVnfResource(vr);
    		vrc.setVfModuleCustomizations(this.getAllVfmcForVrc(vrc));
                }

    	LOGGER.debug("Returning " + resultList1.size() + " vnf modules");
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllVnfsByVnfModelCustomizationUuid", null);
    	return resultList1;
    }

    //1702 API Spec cont'd - Query for all allotted resources in a Service

    public List<AllottedResourceCustomization> getAllAllottedResourcesByServiceModelUuid(String serviceModelUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllAllottedResourcesByServiceModelUuid - " + serviceModelUuid);

    	List<ServiceToResourceCustomization> strcs = this.getSTR(serviceModelUuid, null, "allottedResource");
    	if (strcs == null || strcs.isEmpty()) {
    		LOGGER.debug("No AR entries found for " + serviceModelUuid);
            return new ArrayList<>();
        }
        LOGGER.debug("Found " + strcs.size() + " entries in ServiceToResourceCustomizations with smu=" + serviceModelUuid + ", allottedResource"); 

        ArrayList<AllottedResourceCustomization> masterList = new ArrayList<>();
        for (ServiceToResourceCustomization star : strcs) {
        	String arModelCustomizationUuid = star.getResourceModelCustomizationUUID();
            LOGGER.debug("Now searching for AllottedResourceCustomization for " + arModelCustomizationUuid);
            List<AllottedResourceCustomization> resultSet = this.getAllAllottedResourcesByArModelCustomizationUuid(arModelCustomizationUuid);
            masterList.addAll(resultSet);
        }
        LOGGER.debug("Returning " + masterList.size() + " ARC records");
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllAllottedResourcesByServiceModelUuid", null);
        return masterList;
    }

    public List<AllottedResourceCustomization> getAllAllottedResourcesByServiceModelInvariantUuid(String serviceModelInvariantUuid) {
        LOGGER.debug("Catalog database: getAllAllottedResourcesByServiceModelInvariantUuid - " + serviceModelInvariantUuid);

        StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("Could not find Service for " + serviceModelInvariantUuid);
            return new ArrayList<>();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        String serviceModelUuid = service.getModelUUID();
        LOGGER.debug("The highest version for the Service " + serviceModelInvariantUuid + " is " + serviceModelUuid);

        return this.getAllAllottedResourcesByServiceModelUuid(serviceModelUuid);
    }

    public List<AllottedResourceCustomization> getAllAllottedResourcesByServiceModelInvariantUuid(String serviceModelInvariantUuid, String serviceModelVersion) {
        LOGGER.debug("Catalog database: getAllAllottedResourcesByServiceModelInvariantUuid - " + serviceModelInvariantUuid + ", version=" + serviceModelVersion);

        StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid and version = :serviceModelVersion");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        query.setParameter("serviceModelVersion", serviceModelVersion);

        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("No Service found with smu=" + serviceModelInvariantUuid + " and smv=" + serviceModelVersion);
            return new ArrayList<>();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        String serviceModelUuid = service.getModelUUID();

        return this.getAllAllottedResourcesByServiceModelUuid(serviceModelUuid);
    }

    public List<AllottedResourceCustomization> getAllAllottedResourcesByArModelCustomizationUuid(String arModelCustomizationUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllAllottedResourcesByArModelCustomizationUuid - " + arModelCustomizationUuid);

        StringBuilder hql = new StringBuilder("FROM AllottedResourceCustomization WHERE modelCustomizationUuid = :arModelCustomizationUuid");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("arModelCustomizationUuid", arModelCustomizationUuid);

        @SuppressWarnings("unchecked")
        List<AllottedResourceCustomization> resultList = query.list();

    	if (resultList.isEmpty()) {
    		LOGGER.debug("No ARC found with arc_mcu=" + arModelCustomizationUuid);
    		return new ArrayList<>();
    	}
    	// There should only be one - but we'll handle if multiple
    	for (AllottedResourceCustomization arc : resultList) {
    		AllottedResource ar = this.getAllottedResourceByModelUuid(arc.getArModelUuid());
    		arc.setAllottedResource(ar);
    	}
    	
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllAllottedResourcesByArModelCustomizationUuid", null);
        return resultList;
    }

    public AllottedResource getAllottedResourceByModelUuid(String arModelUuid) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Allotted Resource with modelUuid= " + arModelUuid);

        String hql = "FROM AllottedResource WHERE modelUuid = :arModelUuid";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("arModelUuid", arModelUuid);

        @SuppressWarnings("unchecked")
        List <AllottedResource> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. AllottedResource not found", "CatalogDB", "getAllottedResourceByModelUuid", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllottedResourceByModelUuid", null);
        return resultList.get (0);
    	
    }
    
    //1702 API Spec cont'd - Query for all resources in a Service:
    public ServiceMacroHolder getAllResourcesByServiceModelUuid(String serviceModelUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllResourcesByServiceModelUuid - " + serviceModelUuid);

    	StringBuilder hql = new StringBuilder("FROM Service WHERE modelUUID = :serviceModelUuid");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelUuid", serviceModelUuid);
    	LOGGER.debug("Query: " + hql.toString() + ", smu=" + serviceModelUuid);
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("Unable to find a Service with serviceModelUuid=" + serviceModelUuid);
            return new ServiceMacroHolder();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        ServiceMacroHolder smh = new ServiceMacroHolder(service);
        ArrayList<NetworkResourceCustomization> nrcList = (ArrayList<NetworkResourceCustomization>) this.getAllNetworksByServiceModelUuid(serviceModelUuid);
        smh.setNetworkResourceCustomization(nrcList);
        ArrayList<AllottedResourceCustomization> arcList = (ArrayList<AllottedResourceCustomization>) this.getAllAllottedResourcesByServiceModelUuid(serviceModelUuid);
        smh.setAllottedResourceCustomization(arcList);
        ArrayList<VnfResourceCustomization> vnfList = (ArrayList<VnfResourceCustomization>) this.getAllVnfsByServiceModelUuid(serviceModelUuid);
        smh.setVnfResourceCustomizations(vnfList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllResourcesByServiceModelUuid", null);
        return smh;
    }
    public ServiceMacroHolder getAllResourcesByServiceModelInvariantUuid(String serviceModelInvariantUuid) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllResourcesByServiceModelInvariantUuid - " + serviceModelInvariantUuid);

        StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("Unable to find a Service with serviceModelInvariantUuid=" + serviceModelInvariantUuid);
            return new ServiceMacroHolder();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        ServiceMacroHolder smh = new ServiceMacroHolder(service);
        ArrayList<NetworkResourceCustomization> nrcList = (ArrayList<NetworkResourceCustomization>) this.getAllNetworksByServiceModelUuid(service.getModelUUID());
        smh.setNetworkResourceCustomization(nrcList);
        ArrayList<AllottedResourceCustomization> arcList = (ArrayList<AllottedResourceCustomization>) this.getAllAllottedResourcesByServiceModelUuid(service.getModelUUID());
        smh.setAllottedResourceCustomization(arcList);
        ArrayList<VnfResourceCustomization> vnfList = (ArrayList<VnfResourceCustomization>) this.getAllVnfsByServiceModelUuid(service.getModelUUID());
        smh.setVnfResourceCustomizations(vnfList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllResourcesByServiceModelInvariantUuid", null);
        return smh;

    }
    public ServiceMacroHolder getAllResourcesByServiceModelInvariantUuid(String serviceModelInvariantUuid, String serviceModelVersion) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database: getAllResourcesByServiceModelInvariantUuid - " + serviceModelInvariantUuid + ", version=" + serviceModelVersion);

        StringBuilder hql = new StringBuilder("FROM Service WHERE modelInvariantUUID = :serviceModelInvariantUuid and version = :serviceModelVersion");
        Query query = getSession().createQuery(hql.toString());
        query.setParameter("serviceModelInvariantUuid", serviceModelInvariantUuid);
        query.setParameter("serviceModelVersion", serviceModelVersion);
        //TODO make this a unique query
        @SuppressWarnings("unchecked")
        List<Service> serviceList = query.list();

        if (serviceList.isEmpty()) {
            LOGGER.debug("Unable to find a Service with serviceModelInvariantUuid=" + serviceModelInvariantUuid + " and serviceModelVersion=" + serviceModelVersion);
            return new ServiceMacroHolder();
        }

        serviceList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (serviceList);
        Service service = serviceList.get(0);

        ServiceMacroHolder smh = new ServiceMacroHolder(service);
        ArrayList<NetworkResourceCustomization> nrcList = (ArrayList<NetworkResourceCustomization>) this.getAllNetworksByServiceModelUuid(service.getModelUUID());
        smh.setNetworkResourceCustomization(nrcList);
        ArrayList<AllottedResourceCustomization> arcList = (ArrayList<AllottedResourceCustomization>) this.getAllAllottedResourcesByServiceModelUuid(service.getModelUUID());
        smh.setAllottedResourceCustomization(arcList);
        ArrayList<VnfResourceCustomization> vnfList = (ArrayList<VnfResourceCustomization>) this.getAllVnfsByServiceModelUuid(service.getModelUUID());
        smh.setVnfResourceCustomizations(vnfList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllResourcesByServiceModelUuid with version", null);
        return smh;
    }

    // 1707 New API queries
    public NetworkResourceCustomization getSingleNetworkByModelCustomizationUuid(String modelCustomizationUuid) {
        LOGGER.debug("Catalog database; getSingleNetworkByModelCustomizationUuid - " + modelCustomizationUuid);
        List<NetworkResourceCustomization> resultList = this.getAllNetworksByNetworkModelCustomizationUuid(modelCustomizationUuid);
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        return resultList.get(0);
    }
    public AllottedResourceCustomization getSingleAllottedResourceByModelCustomizationUuid(String modelCustomizationUuid) {
        LOGGER.debug("Catalog database; getSingleAllottedResourceByModelCustomizationUuid - " + modelCustomizationUuid);
        List<AllottedResourceCustomization> resultList = this.getAllAllottedResourcesByArModelCustomizationUuid(modelCustomizationUuid);
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        return resultList.get(0);
    }
    @Deprecated
    public VnfResource getSingleVnfResourceByModelCustomizationUuid(String modelCustomizationUuid) {
    	/*
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database; getSingleVnfResourceByModelCustomizationUuid - " + modelCustomizationUuid);
        List<VnfResource> resultList = this.getAllVnfsByVnfModelCustomizationUuid(modelCustomizationUuid);
        if (resultList == null || resultList.size() < 1) {
            return null;
        }
        return resultList.get(0);
    	*/
    	return null;
    }

    private void populateNetworkResourceType(List<NetworkResourceCustomization> resultList) {
        HashMap<String, NetworkResource> networkResources = new HashMap<>();

        for (NetworkResourceCustomization nrc : resultList) {
        	String network_id = nrc.getNetworkResourceModelUuid();
            if (network_id == null) {
                nrc.setNetworkResource(null);
                nrc.setNetworkType("UNKNOWN_NETWORK_ID_NULL");
                continue;
            }
            if (networkResources.containsKey(network_id)) {
                nrc.setNetworkResource(networkResources.get(network_id));
        		nrc.setNetworkType(networkResources.get(network_id).getModelName());
            } else {
                NetworkResource nr = this.getNetworkResourceById(network_id);
                if (nr == null) {
                    nrc.setNetworkType("INVALID_NETWORK_TYPE_ID_NOT_FOUND");
                } else {
        			nrc.setNetworkType(nr.getModelName());
                    nrc.setNetworkResource(nr);
                    networkResources.put(network_id, nr);
                }
            }
        }
    }

    /**
     * Return a VNF recipe that matches a given VNF_TYPE, VF_MODULE_MODEL_NAME, and ACTION
     * first query VF_MODULE table by type, and then use the ID to query
     * VNF_RECIPE by VF_MODULE_ID and ACTION
     *
     * @param vnfType
     * @parm vfModuleModelName
     * @param action
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVfModuleRecipe (String vnfType, String vfModuleModelName, String action) {
    	String vfModuleType = vnfType + "::" + vfModuleModelName;

    	StringBuilder hql = new StringBuilder ("FROM VfModule WHERE type = :type ");

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VF MODULE  with type " + vfModuleType);

        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter (TYPE, vfModuleType);

        @SuppressWarnings("unchecked")
        List <VfModule> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF Module Entry not found", "CatalogDB", "getVfModuleRecipe", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        VfModule vfMod = resultList.get(0);

        String vfModuleId = vfMod.getModelUUID();

        StringBuilder hql1 = new StringBuilder ("FROM VnfRecipe WHERE vfModuleId = :vfModuleId AND action = :action ");

        LOGGER.debug ("Catalog database - get VNF recipe with vf module id " + vfModuleId
                                      + " and action "
                                      + action);

        Query query1 = getSession ().createQuery (hql1.toString ());
        query1.setParameter (VF_MODULE_MODEL_UUID, vfModuleId);
        query1.setParameter (ACTION, action);

        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList1 = query1.list ();

        if (resultList1.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVfModuleRecipe", null);
            return null;
        }

        resultList1.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList1);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe found", "CatalogDB", "getVfModuleRecipe", null);
        return resultList1.get (0);
    }

    /**
     * Return a VNF Module List that matches a given VNF_TYPE, VF_MODULE_MODEL_NAME,
     * ASDC_SERVICE_MODEL_VERSION, MODEL_VERSION, and ACTION
     *
     * @param vnfModuleType
     * @parm modelCustomizationUuid
     * @param asdcServiceModelVersion
     * @param modelVersion
     * @param action
     * @return VfModule list
     */
    public List<VfModule> getVfModule (String vfModuleType, String modelCustomizationUuid, String asdcServiceModelVersion, String modelVersion, String action) {
        StringBuilder hql;
        Query query;
        if(modelCustomizationUuid != null){
            hql = new StringBuilder ("FROM VfModule WHERE modelCustomizationUuid = :modelCustomizationUuid AND version = :version");

            LOGGER.debug ("Catalog database - get VF MODULE  with type " + vfModuleType + ", asdcServiceModelVersion " + asdcServiceModelVersion + ", modelVersion " + modelVersion);

            query = getSession ().createQuery (hql.toString ());
            query.setParameter ("modelCustomizationUuid", modelCustomizationUuid);
            query.setParameter ("version", asdcServiceModelVersion);
        }else{
            hql = new StringBuilder ("FROM VfModule WHERE type = :type AND version = :version AND modelVersion = :modelVersion");

            LOGGER.debug ("Catalog database - get VF MODULE  with type " + vfModuleType + ", asdcServiceModelVersion " + asdcServiceModelVersion + ", modelVersion " + modelVersion);

            query = getSession ().createQuery (hql.toString ());
            query.setParameter (TYPE, vfModuleType);
            query.setParameter ("version", asdcServiceModelVersion);
            query.setParameter ("modelVersion", modelVersion);
        }

        @SuppressWarnings("unchecked")
        List <VfModule> resultList = query.list ();
        return resultList;
    }

    
    /**
     * Return a VNF COMPONENTSrecipe that matches a given VNF_TYPE, VF_MODULE_MODEL_NAME,
     * MODEL_CUSTOMIZATION_UUID, ASDC_SERVICE_MODEL_VERSION, MODEL_VERSION, and ACTION
     * first query VF_MODULE table by type, and then use the ID to query
     * VNF_COMPONENTS_RECIPE by VF_MODULE_ID and ACTION
     *
     * @param vnfType
     * @parm vfModuleModelName
     * @param action
     * @return VnfRecipe object or null if none found
     */
    public VnfComponentsRecipe getVnfComponentsRecipe (String vnfType, String vfModuleModelName, String modelCustomizationUuid, String asdcServiceModelVersion, String modelVersion, String action) {
        String vfModuleType = vnfType + "::" + vfModuleModelName;
        long startTime = System.currentTimeMillis ();
        List <VfModule> resultList = getVfModule(vfModuleType, modelCustomizationUuid,  asdcServiceModelVersion,  modelVersion,  action);

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF Module Entry not found", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        VfModule vfMod = resultList.get(0);

        String vfModuleId = vfMod.getModelUUID();

        StringBuilder hql1 = new StringBuilder ("FROM VnfComponentsRecipe WHERE vfModuleId = :vfModuleId AND action = :action ");

        LOGGER.debug ("Catalog database - get Vnf Components recipe with vf module id " + vfModuleId
                + " and action "
                + action);

        Query query1 = getSession ().createQuery (hql1.toString ());
        query1.setParameter (VF_MODULE_MODEL_UUID, vfModuleId);
        query1.setParameter (ACTION, action);

        @SuppressWarnings("unchecked")
        List <VnfComponentsRecipe> resultList1 = query1.list ();

        if (resultList1.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }

        resultList1.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList1);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe found", "CatalogDB", "getVnfComponentsRecipe", null);
        if (resultList1.size() > 1 && (!resultList1. get (0).getOrchestrationUri().equals(resultList1.get (1).getOrchestrationUri ()))) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Different ORCHESTRATION URIs found for same VERSION and ID. No result returned.", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }
        return resultList1.get (0);
    }

    /**
     * Return a VNF COMPONENTSrecipe that matches a given VNF_TYPE, VF_MODULE_MODEL_NAME,
     * ASDC_SERVICE_MODEL_VERSION, MODEL_VERSION, and ACTION
     * first query VF_MODULE table by type, and then use the ID to query
     * VNF_COMPONENTS_RECIPE by VF_MODULE_ID and ACTION
     *
     * @param vnfType
     * @parm vfModuleModelName
     * @param action
     * @return VnfRecipe object or null if none found
     */
    public VnfComponentsRecipe getVnfComponentsRecipeByVfModule(List <VfModule> resultList,  String action) {
        long startTime = System.currentTimeMillis ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF Module Entry not found", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }

        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        VfModule vfMod = resultList.get(0);

        String vfModuleId = vfMod.getModelName();      

        StringBuilder hql1 = new StringBuilder ("FROM VnfComponentsRecipe WHERE vfModuleId = :vfModuleId AND action = :action ");

        LOGGER.debug ("Catalog database - get Vnf Components recipe with vf module id " + vfModuleId
                                      + " and action "
                                      + action);

        Query query1 = getSession ().createQuery (hql1.toString ());
        query1.setParameter (VF_MODULE_MODEL_UUID, vfModuleId);
        query1.setParameter (ACTION, action);

        @SuppressWarnings("unchecked")
        List <VnfComponentsRecipe> resultList1 = query1.list ();

        if (resultList1.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }

        resultList1.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList1);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe found", "CatalogDB", "getVnfComponentsRecipe", null);
        if (resultList1.size() > 1 && (!resultList1. get (0).getOrchestrationUri().equals(resultList1.get (1).getOrchestrationUri ()))) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Different ORCHESTRATION URIs found for same VERSION and ID. No result returned.", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }
        return resultList1.get (0);
    }


    /**
     * Return all VNF Resources in the Catalog DB
     *
     * @return A list of VnfResource objects
     */
    @SuppressWarnings("unchecked")
    public List <VnfResource> getAllVnfResources () {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all VNF resources");

        String hql = "FROM VnfResource";
        Query query = getSession ().createQuery (hql);

        List <VnfResource> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllVnfResources", null);
        return result;
    }

    /**
     * Return VNF Resources in the Catalog DB that match a given VNF role
     *
     * @return A list of VnfResource objects
     */
    @SuppressWarnings("unchecked")
    @Deprecated // vnfRole is no longer in VnfResource
    public List <VnfResource> getVnfResourcesByRole (String vnfRole) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all VNF resources for role " + vnfRole);

        String hql = "FROM VnfResource WHERE vnfRole = :vnfRole";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("vnfRole", vnfRole);

        List <VnfResource> resources = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourcesByRole", null);
        return resources;
    }

    /**
     * Return VNF Resources in the Catalog DB that match a given VNF role
     *
     * @return A list of VnfResource objects
     */
    @SuppressWarnings("unchecked")
    public List<VnfResourceCustomization> getVnfResourceCustomizationsByRole(String vnfRole) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all VNF resource customizations for role " + vnfRole);

        String hql = "FROM VnfResourceCustomization WHERE nfRole = :vnfRole";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("vnfRole", vnfRole);

        List <VnfResourceCustomization> resources = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResourceCustomizationsByRole", null);
        return resources;
    }

    /**
     * Return all Network Resources in the Catalog DB
     *
     * @return A list of NetworkResource objects
     */
    @SuppressWarnings("unchecked")
    public List <NetworkResource> getAllNetworkResources () {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all network resources");

        String hql = "FROM NetworkResource";
        Query query = getSession ().createQuery (hql);

        List <NetworkResource> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllNetworkResources", null);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<NetworkResourceCustomization> getAllNetworkResourceCustomizations() {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all network resource customizations");

        String hql = "FROM NetworkResourceCustomization";
        Query query = getSession ().createQuery (hql);

        List <NetworkResourceCustomization> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllNetworkResourceCustomizations", null);
        return result;	
    }
    
    /**
     * Return all VF Modules in the Catalog DB
     *
     * @return A list of VfModule objects
     */
    @SuppressWarnings("unchecked")
    public List <VfModule> getAllVfModules () {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all vf modules");

        String hql = "FROM VfModule";
        Query query = getSession ().createQuery (hql);

        List <VfModule> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllVfModules", null);
        return result;
    }

   @SuppressWarnings("unchecked")
   public List <VfModuleCustomization> getAllVfModuleCustomizations () {

       long startTime = System.currentTimeMillis ();
       LOGGER.debug ("Catalog database - get all vf module customizations");

       String hql = "FROM VfModuleCustomization";
       Query query = getSession ().createQuery (hql);

       List <VfModuleCustomization> result = query.list ();
       LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllVfModuleCustomizations", null);
       return result;
   }
    

    /**
     * Return all HeatEnvironment in the Catalog DB
     *
     * @return A list of HeatEnvironment objects
     */
    @SuppressWarnings("unchecked")
    public List <HeatEnvironment> getAllHeatEnvironment () {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all Heat environments");

        String hql = "FROM HeatEnvironment";
        Query query = getSession ().createQuery (hql);

        List <HeatEnvironment> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllHeatEnvironment", null);
        return result;
    }

    /**
     * Fetch the Environment by Environment ID - 1510
     */
    @Deprecated // no longer in heat envt table
    public HeatEnvironment getHeatEnvironment (int id) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat environment with id " + id);

        String hql = "FROM HeatEnvironment WHERE id = :idValue";

        LOGGER.debug ("getHeatEnvironment called with id=" + id);

        Query query = getSession ().createQuery (hql);
        query.setParameter ("idValue", id);

        @SuppressWarnings("unchecked")
        List <HeatEnvironment> resultList = query.list ();

        // See if something came back.
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Heat environment not found", "CatalogDB", "getHeatEnvironment", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatEnvironment", null);
        return resultList.get (0);
    }

    /**
     * Fetch the nested templates - 1510
     */

    @Deprecated
    public Map <String, Object> getNestedTemplates (int templateId) {
        Map <String, Object> nestedTemplates;
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getNestedTemplates called with templateId " + templateId);

        String hql = "FROM HeatNestedTemplate where parent_template_id = :parentIdValue";

        Query query = getSession ().createQuery (hql);
        query.setParameter ("parentIdValue", templateId);

        @SuppressWarnings("unchecked")
        List <HeatNestedTemplate> resultList = query.list ();
        // If nothing comes back, there are no nested templates
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No nestedTemplate found", "CatalogDB", "getNestedTemplates", null);
            LOGGER.debug ("No nestedTemplates found for templateId=" + templateId + ", " + hql);
            return null;
        }
        // Now, for each entry in NESTED_HEAT_TEMPLATES, we need to grab the template body from HEAT_TEMPLATE
        nestedTemplates = new HashMap <> ();
        for (HeatNestedTemplate hnt : resultList) {
            LOGGER.debug ("Querying for " + hnt);
            HeatTemplate ht = this.getHeatTemplate (hnt.getChildTemplateId ());
            if (ht == null) {
                LOGGER.debug ("No template found matching childTemplateId=" + hnt.getChildTemplateId ());
                continue;
            }
            String providerResourceFile = hnt.getProviderResourceFile ();
            String heatTemplateBody = ht.getTemplateBody ();
            if (providerResourceFile != null && heatTemplateBody != null) {
                nestedTemplates.put (providerResourceFile, heatTemplateBody);
            } else {
                LOGGER.debug ("providerResourceFile or heatTemplateBody were null - do not add to HashMap!");
            }
        }
        // Make sure we're not returning an empty map - if so, just return null
        if (nestedTemplates.isEmpty ()) {
            LOGGER.debug ("nestedTemplates is empty - just return null");
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Nested template is empty", "CatalogDB", "getNestedTemplate", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNestedTemplate", null);
        return nestedTemplates;
    }
    /**
     * Return a Map<String, Object> for returning the child templates and their contents
     * 
     * @param parentHeatTemplateId
     * @return Map<String,Object> or null if none found
     */
    public Map <String, Object> getNestedTemplates (String parentHeatTemplateId) {
        Map <String, Object> nestedTemplates;
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getNestedTemplates called with parentTemplateId " + parentHeatTemplateId);

        String hql = "FROM HeatNestedTemplate where parentTemplateId = :parentHeatTemplateId";

        Query query = getSession ().createQuery (hql);
        query.setParameter ("parentHeatTemplateId", parentHeatTemplateId);

        @SuppressWarnings("unchecked")
        List <HeatNestedTemplate> resultList = query.list ();
        // If nothing comes back, there are no nested templates
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No nestedTemplate found", "CatalogDB", "getNestedTemplates", null);
            LOGGER.debug ("No nestedTemplates found for templateId=" + parentHeatTemplateId + ", " + hql);
            return null;
        }
        // Now, for each entry in NESTED_HEAT_TEMPLATES, we need to grab the template body from HEAT_TEMPLATE
        nestedTemplates = new HashMap <> ();
        for (HeatNestedTemplate hnt : resultList) {
            LOGGER.debug ("Querying for " + hnt);
            HeatTemplate ht = this.getHeatTemplateByArtifactUuid (hnt.getChildTemplateId ());
            if (ht == null) {
                LOGGER.debug ("No template found matching childTemplateId=" + hnt.getChildTemplateId ());
                continue;
            }
            String providerResourceFile = hnt.getProviderResourceFile ();
            String heatTemplateBody = ht.getTemplateBody ();
            if (providerResourceFile != null && heatTemplateBody != null) {
                nestedTemplates.put (providerResourceFile, heatTemplateBody);
            } else {
                LOGGER.debug ("providerResourceFile or heatTemplateBody were null - do not add to HashMap!");
            }
        }
        // Make sure we're not returning an empty map - if so, just return null
        if (nestedTemplates.isEmpty ()) {
            LOGGER.debug ("nestedTemplates is empty - just return null");
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Nested template is empty", "CatalogDB", "getNestedTemplate", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNestedTemplate", null);
        return nestedTemplates;
    }

    /*
     * Fetch any files in the HEAT_FILES table 1510
     */
    @Deprecated
    public Map <String, HeatFiles> getHeatFiles (int vnfResourceId) {
       Map <String, HeatFiles> heatFiles;

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getHeatFiles called with vnfResourceId " + vnfResourceId);
        String hql = "FROM HeatFiles where vnf_resource_id = :vnfResourceIdValue";

        Query query = getSession ().createQuery (hql);
        query.setParameter ("vnfResourceIdValue", vnfResourceId);

        @SuppressWarnings("unchecked")
        List <HeatFiles> resultList = query.list ();
        // If nothing comes back, there are no heat files
        if (resultList.isEmpty ()) {
            LOGGER.debug ("No heatFiles found for vnfResourceId=" + vnfResourceId);
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No heat files", "CatalogDB", "getHeatFiles", null);
            return null;
        }
        // Now, we just need to return a HashMap (key=fileName, object=fileBody)
        heatFiles = new HashMap <> ();
        for (HeatFiles hf : resultList) {
            LOGGER.debug ("Adding " + hf.getFileName () + "->" + hf.getFileBody ());
            heatFiles.put (hf.getFileName (), hf);
        }
        // Make sure we're not returning an empty map - if so, just return null
        if (heatFiles.isEmpty ()) {
            LOGGER.debug ("heatFiles is empty - just return null");
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Heat files is empty", "CatalogDB", "getHeatFiles", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatFiles", null);
        return heatFiles;
    }

    // New 1607 - with modularization, use new table to determine which HEAT_FILES entries to attach
    @Deprecated
    public Map <String, HeatFiles> getHeatFilesForVfModule(int vfModuleId) {
    	/*
        Map <String, HeatFiles> heatFiles = null;

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getHeatFilesForVfModule called with vfModuleId " + vfModuleId);
        String hql = "FROM VfModuleToHeatFiles where vf_module_id = :vfModuleIdValue";

        Query query = getSession ().createQuery (hql);
        query.setParameter ("vfModuleIdValue", vfModuleId);

        List<VfModuleToHeatFiles> mapList = query.list();
        if (mapList.isEmpty()) {
            LOGGER.debug ("No heatFiles found for vfModuleId=" + vfModuleId);
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No heatfiles found for vfModule", "CatalogDB", "getHeatFilesForVfModule", null);
            return null;
        }
        //Now the fun part - we have a list of the heat files we need to get - could clean this up with a join
        heatFiles = new HashMap<String, HeatFiles>();
        for (VfModuleToHeatFiles vmthf : mapList) {
        	int heatFilesId = vmthf.getHeatFilesId();
        	hql = "FROM HeatFiles where id = :id_value";
        	query = getSession().createQuery(hql);
        	query.setParameter("id_value", heatFilesId);
        	List<HeatFiles> fileList = query.list();
        	if (fileList.isEmpty()) {
        		// Should this throw an exception??
        		LOGGER.debug("Unable to find a HEAT_FILES entry at " + heatFilesId);
                String errorString = "_ERROR|" + heatFilesId;
        		// The receiving code needs to know to throw an exception for this - or ignore it.
        		heatFiles.put(errorString, null);
        	} else {
        		// Should only ever have 1 result - add it to our Map
        		LOGGER.debug("Retrieved " + fileList.size() + " heat file entry at " + heatFilesId);
        		for (HeatFiles hf : fileList) {
        			LOGGER.debug("Adding " + hf.getFileName() + "->" + hf.getFileBody());
        			heatFiles.put(hf.getFileName(), hf);
        		}
        	}
        }
        if (heatFiles.isEmpty()) {
            LOGGER.debug ("heatFiles is empty - just return null");
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. HeatFiles is empty", "CatalogDB", "getHeatFilesForVfModule", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatFilesForVfModule", null);
        return heatFiles;
        */
    	return null;
    }
    
    /**
     * Return a VfModuleToHeatFiles object 
     * 
     * @param vfModuleModelUuid, heatFilesArtifactUuid
     * @return VfModuleToHeatFiles or null if none found
     */ 
    public VfModuleToHeatFiles getVfModuleToHeatFilesEntry(String vfModuleModelUuid, String heatFilesArtifactUuid) {

        LOGGER.debug ("Catalog database - getVfModuleToHeatFilesEntry with vfModuleModelUuid " + vfModuleModelUuid + ", heatFilesArtifactUuid=" + heatFilesArtifactUuid);
        String hql = "FROM VfModuleToHeatFiles where vfModuleModelUuid = :vfModuleModelUuidValue and heatFilesArtifactUuid = :heatFilesArtifactUuidValue";
        
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("vfModuleModelUuidValue", vfModuleModelUuid);
        parameters.put("heatFilesArtifactUuidValue", heatFilesArtifactUuid);
        
        VfModuleToHeatFiles vmthf = null;
        
        try {
        	vmthf = this.executeQuerySingleRow(hql, parameters, true);
        } catch (Exception e) {
        	throw e;
        }
        return vmthf;
    }

    
    /**
     * Return a ServiceToResourceCustomization object 
     * 
     * @param vfModuleModelUuid, heatFilesArtifactUuid
     * @return VfModuleToHeatFiles or null if none found
     */ 
    public ServiceToResourceCustomization getServiceToResourceCustomization(String serviceModelUuid, String resourceModelCustomizationUuid, String modelType) {

        LOGGER.debug ("Catalog database - getServiceToResourceCustomization with serviceModelUuid=" + serviceModelUuid + ", resourceModelCustomizationUuid=" + resourceModelCustomizationUuid + ", modelType=" + modelType);
        String hql = "FROM ServiceToResourceCustomization where serviceModelUUID = :serviceModelUuidValue and resourceModelCustomizationUUID = :resourceModelCustomizationUuidValue and modelType = :modelTypeValue ";
        
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("serviceModelUuidValue", serviceModelUuid);
        parameters.put("resourceModelCustomizationUuidValue", resourceModelCustomizationUuid);
        parameters.put("modelTypeValue", modelType);
        
        ServiceToResourceCustomization strc = null;
        
        try {
        	strc = this.executeQuerySingleRow(hql, parameters, true);
        } catch (Exception e) {
        	throw e;
        }
        return strc;
    }

    /**
     * Return a Map<String, HeatFiles> for returning the heat files associated with a vfModule 1707
     * 
     * @param parentHeatTemplateId
     * @return Map<String,Object> or null if none found
     */ 
    public Map <String, HeatFiles> getHeatFilesForVfModule(String vfModuleModelUuid) {
        Map <String, HeatFiles> heatFiles;

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getHeatFilesForVfModule called with vfModuleModelUuid " + vfModuleModelUuid);
        String hql = "FROM VfModuleToHeatFiles where vfModuleModelUuid = :vfModuleModelUuidValue";

        Query query = getSession ().createQuery (hql);
        query.setParameter ("vfModuleModelUuidValue", vfModuleModelUuid);
       
        @SuppressWarnings("unchecked")
        List<VfModuleToHeatFiles> mapList = query.list();
        if (mapList.isEmpty()) {
            LOGGER.debug ("No heatFiles found for vfModuleModelUuid=" + vfModuleModelUuid);
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No heatfiles found for vfModule", "CatalogDB", "getHeatFilesForVfModule", null);
            return null;
        }
        //Now the fun part - we have a list of the heat files we need to get - could clean this up with a join
        heatFiles = new HashMap<>();
        for (VfModuleToHeatFiles vmthf : mapList) {
        	String heatFilesUuid = vmthf.getHeatFilesArtifactUuid();
        	hql = "FROM HeatFiles where artifactUuid = :heatFilesUuidValue";
        	query = getSession().createQuery(hql);
        	query.setParameter("heatFilesUuidValue", heatFilesUuid);
        	@SuppressWarnings("unchecked")
        	List<HeatFiles> fileList = query.list();
        	if (fileList.isEmpty()) {
        		// Should this throw an exception??
        		LOGGER.debug("Unable to find a HEAT_FILES entry at " + heatFilesUuid);
                String errorString = "_ERROR|" + heatFilesUuid;
        		// The receiving code needs to know to throw an exception for this - or ignore it.
        		heatFiles.put(errorString, null);
        	} else {
        		// Should only ever have 1 result - add it to our Map
        		LOGGER.debug("Retrieved " + fileList.size() + " heat file entry at " + heatFilesUuid);
        		for (HeatFiles hf : fileList) {
        			LOGGER.debug("Adding " + hf.getFileName() + "->" + hf.getFileBody());
        			heatFiles.put(hf.getFileName(), hf);
        		}
        	}
        }
        if (heatFiles.isEmpty()) {
            LOGGER.debug ("heatFiles is empty - just return null");
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. HeatFiles is empty", "CatalogDB", "getHeatFilesForVfModule", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatFilesForVfModule", null);
        return heatFiles;	
    }

    /**
     * Get the heat template object based on asdc attributes
     *
     * @param templateName The template name, generally the yaml filename. "example.yaml"
     * @param version The version as specified by ASDC. "1.1"
     * @param asdcResourceName The ASDC resource name provided in the ASDC artifact
     *
     * @return The HeatTemplate
     */
    @Deprecated // asdcResourceName is no longer in heatTeamplate
    public HeatTemplate getHeatTemplate (String templateName, String version, String asdcResourceName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getHeatTemplate with name " + templateName
                                      + " and version "
                                      + version
                                      + " and ASDC resource name "
                                      + asdcResourceName);

        String hql = "FROM HeatTemplate WHERE templateName = :template_name AND version = :version AND asdcResourceName = :asdcResourceName";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("template_name", templateName);
        query.setParameter ("version", version);
        query.setParameter ("asdcResourceName", asdcResourceName);

        @SuppressWarnings("unchecked")
        List <HeatTemplate> resultList = query.list ();

        // See if something came back.
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Heat template not found", "CatalogDB", "getHeatTemplate", null);
            return null;
        }
        // Name + Version is unique, so should only be one element
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return resultList.get (0);
    }


    /**
     * Save the Heat Template
     *
     * @param heat The heat template
     * @param paramSet The list of heat template parameters
     */
    public void saveHeatTemplate (HeatTemplate heat, Set <HeatTemplateParam> paramSet) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat Template with name " + heat.getTemplateName() + ", artifactUUID=" + heat.getArtifactUuid());

        heat.setParameters(null);
        try {
            
            HeatTemplate heatTemp = this.getHeatTemplateByArtifactUuidRegularQuery(heat.getArtifactUuid());
            
            if (heatTemp == null) {
                this.getSession ().save (heat);

                if (paramSet != null) {
                	StringBuilder sb = new StringBuilder("Parameters: ");
                    for (HeatTemplateParam param : paramSet) {
                        param.setHeatTemplateArtifactUuid(heat.getArtifactUuid());
                        sb.append(param.getParamName()).append(", ");
                    }
                    LOGGER.debug(sb.toString());
                    heat.setParameters (paramSet);
                    try {
                    	Session session = this.getSession();
                    	if (!(session.isConnected() && session.isOpen())) {
                    		LOGGER.debug("Initial session is not connected or open - get another");
                    		session = this.getSession();
                    	}
                    	session.save(heat);
                    } catch (HibernateException he1) {
                    	LOGGER.debug("Hibernate Exception encountered on first attempt at save(heat) - try again..." + he1.getMessage(), he1);
                    	try {
                    		Session session = this.getSession();
                    		session.save(heat);
                    	} catch (HibernateException he2) {
                    		LOGGER.debug("Hibernate Exception encountered on second attempt at save(heat)" + he2.getMessage());
                    		LOGGER.debug(Arrays.toString(he2.getStackTrace()));
                    		throw he2;
                    	} catch (Exception e2) {
                    		LOGGER.debug("General Exception encountered on second attempt at save(heat)..." + e2.getMessage(),e2);
                    		LOGGER.debug(Arrays.toString(e2.getStackTrace()));
                    		throw e2;
                    	}
                    	
                    } catch (Exception e1) {
                    	LOGGER.debug("General Exception encountered on first attempt at save(heat) - try again..." + e1.getMessage(), e1);
                    	LOGGER.debug(Arrays.toString(e1.getStackTrace()));
                    	try {
                    		Session session = this.getSession();
                    		session.save(heat);
                    	} catch (HibernateException he2) {
                    		LOGGER.debug("General Exception encountered on second attempt at save(heat)" + he2.getMessage(), he2);
                    		LOGGER.debug(Arrays.toString(he2.getStackTrace()));
                    		throw he2;
                    	} catch (Exception e2) {
                    		LOGGER.debug("General Exception encountered on second attempt at save(heat)..." + e2.getMessage(), e2);
                    		LOGGER.debug(Arrays.toString(e2.getStackTrace()));
                    		throw e2;
                    	}
                    }
                }

            } else {
            	heat.setArtifactUuid(heatTemp.getArtifactUuid());
            }
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveHeatTemplate", null);
        }
    }

    /**
     * Retrieves a Heat environment from DB based on its unique key.
     *
     * @param name the environment artifact name
     * @param version the environment resource version
     * @param asdcResourceName the environment resource name
     * @return the heat environment from DB or null if not found
     */
    @Deprecated
    public HeatEnvironment getHeatEnvironment (String name, String version, String asdcResourceName) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat environment with name " + name
                                      + " and version "
                                      + version
                                      + " and ASDC resource name "
                                      + asdcResourceName);

        String hql = "FROM HeatEnvironment WHERE name=:name AND version=:version AND asdcResourceName=:asdcResourceName";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("name", name);
        query.setParameter ("version", version);
        query.setParameter ("asdcResourceName", asdcResourceName);
        HeatEnvironment env = null;
        try {
        	env = (HeatEnvironment) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: envName='" + name + "', version='" + version + "' and asdcResourceName=" + asdcResourceName, nure);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for envName=" + name + " and version=" + version + " and asdcResourceName=" + asdcResourceName, "", "", MsoLogger.ErrorCode.DataError, "non unique result for envName=" + name);
        	env = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: envName='" + name + "', asdc_service_model_version='" + version + "' and asdcResourceName=" + asdcResourceName, he);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for envName=" + name + " and version=" + version + " and asdcResourceName=" + asdcResourceName, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for envName=" + name);
        	env = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: envName='" + name + "', asdc_service_model_version='" + version + "' and asdcResourceName=" + asdcResourceName, e);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for envName=" + name + " and version=" + version + " and asdcResourceName=" + asdcResourceName, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for envName=" + name);
        	env = null;
        }
        if (env == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getHeatTemplate", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        }
        return env;
    }

    /**
     * Retrieves a Heat environment from DB based on its unique key. 1707
     *
     * @param name the environment artifact name
     * @param version the environment resource version
     * @return the heat environment from DB or null if not found
     */
    public HeatEnvironment getHeatEnvironment (String artifactUuid, String version) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat environment with uuid " + artifactUuid
                                      + " and version "
                                      + version);

        String hql = "FROM HeatEnvironment WHERE artifactUuid=:artifactUuid AND version=:version";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("artifactUuid", artifactUuid);
        query.setParameter ("version", version);
        HeatEnvironment env = null;
        try {
        	env = (HeatEnvironment) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: envName='" + artifactUuid + "', version='" + version);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for artifactUUID=" + artifactUuid + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "non unique result for ArtifactUUID=" + artifactUuid);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: artifactUUID='" + artifactUuid + "', asdc_service_model_version='" + version + " " + he.getMessage() );
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for artifactUUID=" + artifactUuid + " and version=" + version , "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for artifactUUID=" + artifactUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: artifactUUID='" + artifactUuid + "', asdc_service_model_version='" + version  + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for artifactUUID=" + artifactUuid + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for artifactUUID=" + artifactUuid);
        	throw e;
        }
        if (env == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getHeatTemplate", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        }
        return env;
    }

    /**
     * Save the HeatEnvironment
     *
     * @param env The Environment
     */
    public void saveHeatEnvironment (HeatEnvironment env) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat environment with name "
                                      + env.getEnvironment() + " and ArtifactUUID " + env.getArtifactUuid());
        try {
            HeatEnvironment dbEnv = getHeatEnvironment (env.getArtifactUuid(), env.getVersion ());
            if (dbEnv == null) {

                this.getSession ().save (env);

            } else {
            	env.setArtifactUuid(dbEnv.getArtifactUuid());
            }

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveHeatTemplate", null);
        }
    }

    /**
     * Save the heatTemplate
     *
     * @param heat The heat template
     */
    public void saveHeatTemplate (HeatTemplate heat) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat template with name " + heat.getTemplateName ());
        try {
            this.getSession ().update (heat);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveHeatTemplate", null);
        }
    }

    public void saveHeatFile (HeatFiles heatFile) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat file with name " + heatFile.getFileName ());
        try {
            this.getSession ().save (heatFile);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveHeatFile", null);
        }
    }

    public void saveVnfRecipe (VnfRecipe vnfRecipe) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VNF recipe with VNF type " + vnfRecipe.getVnfType ());
        try {
            this.getSession ().save (vnfRecipe);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVnfRecipe", null);
        }
    }

    public void saveVnfComponentsRecipe (VnfComponentsRecipe vnfComponentsRecipe) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VNF Component recipe with VNF type " + vnfComponentsRecipe.getVnfType ());
        try {
            this.getSession ().save (vnfComponentsRecipe);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVnfComponentsRecipe", null);
        }
    }


    public void saveOrUpdateVnfResource (VnfResource vnfResource) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VNF Resource with VNF type " + vnfResource.getModelName());
        try {

        	VnfResource existing = this.getVnfResourceByModelUuid(vnfResource.getModelUuid());
        	if (existing == null) {
        		LOGGER.debug("No existing entry found - attempting to save...");
                this.getSession ().save (vnfResource);
        	} else {
        		LOGGER.debug("Existing vnf resource found!");
            }

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateVnfResource", null);
        }
    }

    public boolean saveVnfResourceCustomization (VnfResourceCustomization vnfResourceCustomization) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VNF Resource Customization with Name " + vnfResourceCustomization.getModelInstanceName());
        try {
        	LOGGER.debug(vnfResourceCustomization.toString());
        } catch (Exception e) {
        	LOGGER.debug("Unable to print VRC " + e.getMessage(), e);
        }
        try {
        		 // Check if NetworkResourceCustomzation record already exists.  If so, skip saving it.
        		// List<NetworkResource> networkResourceList = getAllNetworksByNetworkModelCustomizationUuid(networkResourceCustomization.getModelCustomizationUuid());
        		 // Do any matching customization records exist?
        		// if(networkResourceList.size() == 0){
         		        		 
        			// networkResourceCustomization.setNetworkResourceModelUuid(networkResource.getModelUuid());
        //	this.getSession().flush();
        //	this.getSession().clear();
        	
        	VnfResourceCustomization existing = this.getVnfResourceCustomizationByModelCustomizationUuid(vnfResourceCustomization.getModelCustomizationUuid());
        	
        	if (existing == null) {
        		LOGGER.debug("No existing entry found...attempting to save...");
            		this.getSession ().save (vnfResourceCustomization);
        		return true;
        	}else {
        		try {
        			LOGGER.debug("Existing VRC entry found\n" + existing.toString());
        		} catch (Exception e) {
        			LOGGER.debug("Unable to print VRC2 " + e.getMessage(), e);
        		}
        		return false;
            	}
        		         		 
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVnfResourceCustomization", null);
        }
    }
    
    public void saveAllottedResourceCustomization (AllottedResourceCustomization resourceCustomization) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Allotted Resource with Name " + resourceCustomization.getModelInstanceName());
        try {
            List<AllottedResourceCustomization> allottedResourcesList = getAllAllottedResourcesByArModelCustomizationUuid(resourceCustomization.getModelCustomizationUuid());

            if(allottedResourcesList.isEmpty()){
                this.getSession ().save(resourceCustomization);
            }

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateAllottedResourceCustomization", null);
        }
    }

    public void saveAllottedResource (AllottedResource allottedResource) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Allotted Resource with Name " + allottedResource.getModelName());
        try { 
        	AllottedResource existing = this.getAllottedResourceByModelUuid(allottedResource.getModelUuid());
        	if (existing == null) {
        		this.getSession ().save (allottedResource);
        	} else {
        		LOGGER.debug("Found existing allottedResource with this modelUuid - no need to save");
        	}
         
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateAllottedResourceCustomization", null);
        }
    }
    
    public void saveNetworkResource (NetworkResource networkResource) throws RecordNotFoundException {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Network Resource with Network Name " + networkResource.getModelName());
        try {
        		 // Check if NetworkResourceCustomzation record already exists.  If so, skip saving it.
        		// List<NetworkResource> networkResourceList = getAllNetworksByNetworkModelCustomizationUuid(networkResourceCustomization.getModelCustomizationUuid());
        		 // Do any matching customization records exist?
			if(getNetworkResourceByModelUuid(networkResource.getModelUUID()) == null){
        			 this.getSession ().save(networkResource);
			}
  
        
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveNetworkResourceCustomization", null);
        }
    }
    
    public void saveToscaCsar (ToscaCsar toscaCsar) throws RecordNotFoundException {
    	

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Tosca Csar with Name " + toscaCsar.getName());
        try {
        	
        	if(getToscaCsar(toscaCsar.getArtifactChecksum()) == null){
        		this.getSession ().save (toscaCsar);
        	}
        	LOGGER.debug("Temporarily disabling saveToscaCsar pending further investigation 2017-06-02");
        
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveToscaCsar", null);
        }
    }
    

    /**
     * Return the newest version of a specific Tosca CSAR Record resource (queried by Name).
     *
     * @param ToscaCsar
     * @return ToscaCsar object or null if none found
     */
    public ToscaCsar getToscaCsar (String artifactChecksum) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Tosca CSAR record with artifactChecksum " + artifactChecksum);

        String hql = "FROM ToscaCsar WHERE artifactChecksum = :artifactChecksum";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("artifactChecksum", artifactChecksum);

        @SuppressWarnings("unchecked")
        List <ToscaCsar> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Tosca Csar not found", "CatalogDB", "getToscaCsar", null);
            return null;
        }

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getToscaCsar", null);
        return resultList.get (0);
    }
    
    /**
     * Return a specific Tosca CSAR Record resource (queried by atrifact uuid).
     *
     * @param toscaCsarArtifactUUID the artifact uuid of the tosca csar
     * @return ToscaCsar object or null if none found
     */
    public ToscaCsar getToscaCsarByUUID(String toscaCsarArtifactUUID){
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Tosca CSAR record with artifactUUID " + toscaCsarArtifactUUID);

        String hql = "FROM ToscaCsar WHERE artifactUUID = :toscaCsarArtifactUUID";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("toscaCsarArtifactUUID", toscaCsarArtifactUUID);

        @SuppressWarnings("unchecked")
        List <ToscaCsar> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Tosca Csar not found", "CatalogDB", "getToscaCsarByUUID", null);
            return null;
        }

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getToscaCsarByUUID", null);
        return resultList.get (0);
    }

    /**
     * Return a specific Tosca CSAR Record resource (queried by service model uuid).
     * <br>
     * 
     * @param serviceModelUUID the service model uuid
     * @return ToscaCsar object or null if none found
     * @since ONAP Beijing Release
     */
    public ToscaCsar getToscaCsarByServiceModelUUID(String serviceModelUUID){
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Tosca CSAR record with serviceModelUUID " + serviceModelUUID);
        Service service = getServiceByModelUUID(serviceModelUUID);
        if(null == service){
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Service not found", "CatalogDB", "getToscaCsarByServiceModelUUID", null);
            return null;
        }
        ToscaCsar csar = getToscaCsarByUUID(service.getToscaCsarArtifactUUID());
        if(null == csar){
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Tosca csar of the service not found", "CatalogDB", "getToscaCsarByServiceModelUUID", null);
            return null;
        }
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getToscaCsarByServiceModelUUID", null);
        return csar;
    }
    
    public void saveTempNetworkHeatTemplateLookup (TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save TempNetworkHeatTemplateLookup with Network Model Name " + tempNetworkHeatTemplateLookup.getNetworkResourceModelName() +
        		      " and Heat Template Artifact UUID " + tempNetworkHeatTemplateLookup.getHeatTemplateArtifactUuid());
        try {
                 this.getSession ().save (tempNetworkHeatTemplateLookup);
      
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveTempNetworkHeatTemplateLookup", null); 
        }
    }
    
    public void saveVfModuleToHeatFiles (VfModuleToHeatFiles vfModuleToHeatFiles) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VfModuleToHeatFiles with VF Module UUID " + vfModuleToHeatFiles.getVfModuleModelUuid() +
        		      " and Heat Files Artifact UUID " + vfModuleToHeatFiles.getHeatFilesArtifactUuid());
        try {
        	
                this.getSession ().save (vfModuleToHeatFiles);
      
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVFModuleToHeatFiles", null); 
        }
    }
    
    public void saveVnfResourceToVfModuleCustomization(VnfResourceCustomization vnfResourceCustomizationUUID, VfModuleCustomization vfModuleCustomizationUUID) throws RecordNotFoundException {
        long startTime = System.currentTimeMillis ();
        VnfResCustomToVfModuleCustom vnfResCustomToVfModuleCustom = new VnfResCustomToVfModuleCustom();
        
        if(vnfResourceCustomizationUUID != null && vfModuleCustomizationUUID != null){
        	vnfResCustomToVfModuleCustom.setVnfResourceCustModelCustomizationUuid(vnfResourceCustomizationUUID.getModelCustomizationUuid());
        	vnfResCustomToVfModuleCustom.setVfModuleCustModelCustomizationUuid(vfModuleCustomizationUUID.getModelCustomizationUuid());
        	String vnfId = vnfResourceCustomizationUUID.getModelCustomizationUuid();
        	String vfId = vfModuleCustomizationUUID.getModelCustomizationUuid();
        	LOGGER.debug ("Catalog database - save VnfResCustomToVfModuleCustom with vnf=" + vnfId + ", vf=" + vfId);
        	try {
        		VnfResCustomToVfModuleCustom existing = this.getVnfResCustomToVfModule(vnfId, vfId);
        		if (existing == null) {
        			LOGGER.debug("No existing entry found - will now try to save");
        			this.getSession ().save (vnfResCustomToVfModuleCustom);
        		} else {
        			LOGGER.debug("Existing entry already found - no save needed");
        		}
        	} finally {
        		LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVnfResourceToVfModuleCustomization", null);
        	}
        }
    }
    
    public void saveNetworkResourceCustomization (NetworkResourceCustomization networkResourceCustomization) throws RecordNotFoundException {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Network Resource Customization with Network Name " + networkResourceCustomization.getModelInstanceName());
        try {
            // Check if NetworkResourceCustomzation record already exists.  If so, skip saving it.
            List<NetworkResourceCustomization> networkResourceCustomizationList = getAllNetworksByNetworkModelCustomizationUuid(networkResourceCustomization.getModelCustomizationUuid());
            // Do any matching customization records exist?
            if(networkResourceCustomizationList.isEmpty()){

                // Retreive the record from the Network_Resource table associated to the Customization record based on ModelName
        		// ?? is it modelInstanceName with 1707?
        		NetworkResource networkResource = getNetworkResource(networkResourceCustomization.getModelInstanceName());

                if(networkResource == null){
        			throw new RecordNotFoundException("No record found in NETWORK_RESOURCE table for model name " + networkResourceCustomization.getModelInstanceName());
                }

        		networkResourceCustomization.setNetworkResourceModelUuid(networkResource.getModelUUID());

                this.getSession ().save(networkResourceCustomization);
            }


        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveNetworkResourceCustomization", null);
        }
    }

    @Deprecated  // table is gone - mapped to ServiceToResource
    public void saveServiceToNetworks (ServiceToNetworks serviceToNetworks) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save to ServiceToNetworks table with NetworkModelCustomizationUUID of " + serviceToNetworks.getNetworkModelCustomizationUuid() + " and ServiceModelUUID of " + serviceToNetworks.getServiceModelUuid());
        try {
            this.getSession ().save(serviceToNetworks);

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveNetworkResourceCustomization", null);
        }
    }

    public void saveServiceToResourceCustomization(ServiceToResourceCustomization serviceToResource) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save to ServiceToResourceCustomization table with ServiceModelUuid of " + serviceToResource.getServiceModelUUID() + ", ResourceModelUUID of " + serviceToResource.getResourceModelCustomizationUUID() + " and model_type=" + serviceToResource.getModelType());
        ServiceToResourceCustomization strc = this.getServiceToResourceCustomization(serviceToResource.getServiceModelUUID(), serviceToResource.getResourceModelCustomizationUUID(), serviceToResource.getModelType());
        try {
        	if (strc != null) {
        		LOGGER.debug("**This ServiceToResourceCustomization record already exists - no need to save");
        	} else {
        	 this.getSession ().save(serviceToResource);
        	}
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveServiceToResourceCustomization", null);
        }
    }
    
    @Deprecated // table is gone - mapped to ServiceToResourceCustomization
    public void saveServiceToAllottedResources (ServiceToAllottedResources serviceToAllottedResources) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save to serviceToAllottedResources table with ARModelCustomizationUUID of " + serviceToAllottedResources.getArModelCustomizationUuid() + " and ServiceModelUUID of " + serviceToAllottedResources.getServiceModelUuid());
        try {
            this.getSession ().save(serviceToAllottedResources);

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveServiceToAllottedResources", null);
        }
    }

    public void saveService (Service service) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Service with ServiceName/Version/serviceUUID(SERVICE_NAME_VERSION_ID)" + service.getModelName()+"/"+service.getVersion()+"/"+service.getModelUUID());
        try {
            Service serviceInvariantDB = null;
            // Retrieve existing service record by nameVersionId
        	Service serviceDB = this.getServiceByModelUUID(service.getModelUUID());
            if (serviceDB == null) {
                // Check to see if a record with the same modelInvariantId already exists.  This tells us that a previous version exists and we can copy its recipe Record for the new service record.
                serviceInvariantDB = this.getServiceByInvariantUUID(service.getModelInvariantUUID());
                // Save the new Service record
                this.getSession ().save (service);
            }

            if(serviceInvariantDB != null){  // existing modelInvariantId was found.
                // copy the recipe record with the matching invariant id.  We will duplicate this for the new service record
            	List<ServiceRecipe> serviceRecipes = getServiceRecipes(serviceInvariantDB.getModelUUID());

                if(serviceRecipes != null && ! serviceRecipes.isEmpty()){
                    for(ServiceRecipe serviceRecipe : serviceRecipes){
                        if(serviceRecipe != null){
                            // Fetch the service record that we just added.  We do this so we can extract its Id column value, this will be the foreign key we use in the service recipe table.
            				Service newService = this.getServiceByModelUUID(service.getModelUUID());
                            // Create a new ServiceRecipe record based on the existing one we just copied from the DB.
                            ServiceRecipe newServiceRecipe = new ServiceRecipe();
                            newServiceRecipe.setAction(serviceRecipe.getAction());
                            newServiceRecipe.setDescription(serviceRecipe.getDescription());
                            newServiceRecipe.setOrchestrationUri(serviceRecipe.getOrchestrationUri());
                            newServiceRecipe.setRecipeTimeout(serviceRecipe.getRecipeTimeout());
                            newServiceRecipe.setServiceParamXSD(serviceRecipe.getServiceParamXSD());
            				newServiceRecipe.setServiceModelUUID(newService.getModelUUID());
                            newServiceRecipe.setVersion(serviceRecipe.getVersion());
            				// Check recipe does not exist before inserting
            				ServiceRecipe recipe = getServiceRecipeByModelUUID(newServiceRecipe.getServiceModelUUID(), newServiceRecipe.getAction());
                            // Save the new recipe record in the service_recipe table and associate it to the new service record that we just added.
            				if(recipe == null){
                            this.getSession ().save (newServiceRecipe);
                        }
                    }
            	}
              }
            }

               
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateService", null);
        }
    }

    public void saveOrUpdateVfModule (VfModule vfModule) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save or update VF Module with VF Model Name " + vfModule.getModelName());
        VfModule vfModuleInvariantDB = null;
        try {
        	LOGGER.debug("heat template id = " + vfModule.getHeatTemplateArtifactUUId() + ", vol template id = "+ vfModule.getVolHeatTemplateArtifactUUId());
        	LOGGER.debug(vfModule.toString());
        } catch (Exception e) {
        	LOGGER.debug("unable to print vfmodule " + e.getMessage(), e);
        }
        try {
        	VfModule existing = this.getVfModuleByModelUUID(vfModule.getModelUUID());
        	if (existing == null) {
        		// Check to see if a record with the same modelInvariantId already exists.  This tells us that a previous version exists and we can copy its recipe Record for the new service record.
        		vfModuleInvariantDB = this.getVfModuleByModelInvariantUuid(vfModule.getModelInvariantUUID());
        		LOGGER.debug("No existing entry found, attempting to save...");
                this.getSession ().save (vfModule);
        	} else {
        		try {
        			LOGGER.debug("Found an existing vf module!\n" + existing.toString());
        		} catch (Exception e) {
        			LOGGER.debug("unable to print vfmodule2 " + e.getMessage(), e);
            }
        	}
        	
            if(vfModuleInvariantDB != null){  // existing modelInvariantId was found.
                // copy the recipe record with the matching invariant id.  We will duplicate this for the new service record             	
             	List<VnfComponentsRecipe> vfRecipes = getVnfComponentRecipes(vfModuleInvariantDB.getModelUUID());

             	
             	if(vfRecipes != null && ! vfRecipes.isEmpty()){
             		for(VnfComponentsRecipe vfRecipe : vfRecipes){
             			if(vfRecipe != null){
             				// Fetch the service record that we just added.  We do this so we can extract its Id column value, this will be the foreign key we use in the service recipe table.
             				VfModule newRecipe = this.getVfModuleByModelUUID(vfModule.getModelUUID());
             				// Create a new ServiceRecipe record based on the existing one we just copied from the DB.
             				VnfComponentsRecipe newVnfRecipe = new VnfComponentsRecipe();
             				newVnfRecipe.setAction(vfRecipe.getAction());
             				newVnfRecipe.setDescription(vfRecipe.getDescription());
             				newVnfRecipe.setOrchestrationUri(vfRecipe.getOrchestrationUri());
             				newVnfRecipe.setRecipeTimeout(vfRecipe.getRecipeTimeout());
             				newVnfRecipe.setVnfComponentParamXSD(vfRecipe.getVnfComponentParamXSD());
             				newVnfRecipe.setVfModuleModelUUId(newRecipe.getModelUUID());
             				newVnfRecipe.setVersion(vfRecipe.getVersion());
             				newVnfRecipe.setVnfComponentType(vfRecipe.getVnfComponentType());
             				newVnfRecipe.setVnfType(vfRecipe.getVnfType());
             				// Check recipe does not exist before inserting
         //    				VnfComponentsRecipe recipe = getVnfComponentRecipes(newVnfRecipe.getVfModuleModelUUId());
             				List<VnfComponentsRecipe> recipe = getVnfComponentRecipes(newVnfRecipe.getVfModuleModelUUId());
             				// Save the new recipe record in the service_recipe table and associate it to the new service record that we just added.
        //     				if(recipe == null){
            					this.getSession ().save (newVnfRecipe);
        //     				}
             			}
             		}
             	}
  
             }

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateVfModule", null);
        }
    }

    public void saveOrUpdateVfModuleCustomization (VfModuleCustomization vfModuleCustomization) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VF Module Customization with VF Customization Model Name UUID " + vfModuleCustomization.getVfModuleModelUuid());
        try {
        	LOGGER.debug("env id = " + vfModuleCustomization.getHeatEnvironmentArtifactUuid() + ", vol Env=" + vfModuleCustomization.getVolEnvironmentArtifactUuid());
        	LOGGER.debug(vfModuleCustomization.toString());
        } catch (Exception e) {
        	LOGGER.debug("unable to print vfmodulecust " + e.getMessage(), e);
        }
        try {
        	VfModuleCustomization existing = this.getVfModuleCustomizationByModelCustomizationId(vfModuleCustomization.getModelCustomizationUuid());
        	if (existing == null) {
        		LOGGER.debug("No existing entry found, attempting to save...");
                this.getSession ().save (vfModuleCustomization);
        	} else {
        		try {
        			LOGGER.debug("Found an existing vf module customization entry\n" + existing.toString());
        		} catch (Exception e) {
        			LOGGER.debug("unable to print vfmodulecust2 " + e.getMessage(), e);
            	}
        	}
      
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateVfModuleCustomization", null); 
        }
    }

    @Deprecated
    public HeatNestedTemplate getNestedHeatTemplate(int parentTemplateId, int childTemplateId) {
    	  long startTime = System.currentTimeMillis ();
          LOGGER.debug ("Catalog database - get nested Heat template with PerentId-Child Id "
                                        + parentTemplateId +"-"+childTemplateId);
          try {
              HeatNestedTemplate nestedTemplate = new HeatNestedTemplate ();
//              nestedTemplate.setParentTemplateId (parentTemplateId);
//              nestedTemplate.setChildTemplateId (childTemplateId);
              
              return (HeatNestedTemplate)session.get (HeatNestedTemplate.class,nestedTemplate);
          } finally {
              LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNestedHeatTemplate", null);
          }
    }
    
    // 1707 version
    public HeatNestedTemplate getNestedHeatTemplate(String parentTemplateId, String childTemplateId) {
  	  long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get nested Heat template with PerentId="
                                      + parentTemplateId +", ChildId="+childTemplateId);
        try {
            HeatNestedTemplate nestedTemplate = new HeatNestedTemplate ();
              nestedTemplate.setParentTemplateId (parentTemplateId);
              nestedTemplate.setChildTemplateId (childTemplateId);

              return (HeatNestedTemplate)session.get (HeatNestedTemplate.class,nestedTemplate);
          } finally {
              LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNestedHeatTemplate", null);
          }
    }

    @Deprecated
    public void saveNestedHeatTemplate (int parentTemplateId, HeatTemplate childTemplate, String yamlFile) {
    	/*
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save nested Heat template with name "
                                      + childTemplate.getTemplateName ());
        try {

	        saveHeatTemplate(childTemplate, childTemplate.getParameters());
	        if (getNestedHeatTemplate(parentTemplateId,childTemplate.getId()) == null) {
	            HeatNestedTemplate nestedTemplate = new HeatNestedTemplate ();
	            nestedTemplate.setParentTemplateId (parentTemplateId);
	            nestedTemplate.setChildTemplateId (childTemplate.getId ());
	            nestedTemplate.setProviderResourceFile (yamlFile);
	            session.save (nestedTemplate);
        	}
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveNestedHeatTemplate", null);
        }
        */
    }
    
    // 1707
    public void saveNestedHeatTemplate (String parentTemplateId, HeatTemplate childTemplate, String yamlFile) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save nested Heat template with name "
                                      + childTemplate.getTemplateName () + ",parentId=" + parentTemplateId + ",childId=" + childTemplate.getArtifactUuid() + ", providerResourceFile=" + yamlFile);
        try {
      
	        saveHeatTemplate(childTemplate, childTemplate.getParameters());
	        if (getNestedHeatTemplate(parentTemplateId,childTemplate.getArtifactUuid()) == null) { 
	            HeatNestedTemplate nestedTemplate = new HeatNestedTemplate ();
	            nestedTemplate.setParentTemplateId (parentTemplateId);
	            nestedTemplate.setChildTemplateId (childTemplate.getArtifactUuid ());
	            nestedTemplate.setProviderResourceFile (yamlFile);
	            session.flush();
	            session.save (nestedTemplate);
        	}
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveNestedHeatTemplate", null);
        }
    }

    @Deprecated
    public HeatFiles getHeatFiles(int vnfResourceId,String fileName,String asdcResourceName, String version) {
    	  long startTime = System.currentTimeMillis ();
          LOGGER.debug ("Catalog database - getHeatFiles with name " + fileName
                                        + " and vnfResourceID "
                                        + vnfResourceId
//                                        + " and ASDC resource name "
                                        + asdcResourceName
                                        + " and version "
                                        + version);

          String hql = "FROM HeatFiles WHERE fileName = :fileName AND vnfResourceId = :vnfResourceId AND asdcResourceName = :asdcResourceName AND version = :version";
          Query query = getSession ().createQuery (hql);
          query.setParameter ("fileName", fileName);
          query.setParameter ("vnfResourceId", vnfResourceId);
          query.setParameter ("asdcResourceName", asdcResourceName);
          query.setParameter ("version", version);

          @SuppressWarnings("unchecked")

          HeatFiles heatFilesResult = null;
          try {
        	  heatFilesResult = (HeatFiles) query.uniqueResult ();
          } catch (org.hibernate.NonUniqueResultException nure) {
          	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: fileName='" + fileName + "', vnfResourceId='" + vnfResourceId + "' and asdcResourceName=" + asdcResourceName + " and version=" + version);
          	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for fileName=" + fileName + " and vnfResourceId=" + vnfResourceId + " and asdcResourceName=" + asdcResourceName + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for fileName=" + fileName);
          	throw nure;
          } catch (org.hibernate.HibernateException he) {
          	LOGGER.debug("Hibernate Exception - while searching for: fileName='" + fileName + "', vnfResourceId='" + vnfResourceId + "' and asdcResourceName=" + asdcResourceName + " and version=" + version + " " + he.getMessage());
          	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for fileName=" + fileName + " and vnfResourceId=" + vnfResourceId + " and asdcResourceName=" + asdcResourceName + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for fileName=" + fileName);
          	throw he;
          } catch (Exception e) {
          	LOGGER.debug("Generic Exception - while searching for: fileName='" + fileName + "', vnfResourceId='" + vnfResourceId + "' and asdcResourceName=" + asdcResourceName + " and version=" + version + " " + e.getMessage());
          	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for fileName=" + fileName + " and vnfResourceId=" + vnfResourceId + " and asdcResourceName=" + asdcResourceName + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for fileName=" + fileName);
          	throw e;
          }

          // See if something came back.
          if (heatFilesResult == null) {
              LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. HeatFiles not found", "CatalogDB", "getHeatFiles", null);
              return null;
          }
          // Name + Version is unique, so should only be one element
          LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatFiles", null);
          return heatFilesResult;
    }

    public HeatFiles getHeatFiles(String artifactUuid) {
  	  long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - getHeatFiles with artifactUuid " + artifactUuid);

        String hql = "FROM HeatFiles WHERE artifactUuid = :artifactUuid";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("artifactUuid", artifactUuid);

        @SuppressWarnings("unchecked")
      
        HeatFiles heatFilesResult = null;
        try {
      	  heatFilesResult = (HeatFiles) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: artifactUuid='" + artifactUuid );
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for artifactUuid=" + artifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for artifactUuid=" + artifactUuid);
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: artifactUuid='" + artifactUuid + " " + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for artifactUuid=" + artifactUuid, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for artifactUuid=" + artifactUuid);
        	throw he;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: artifactUuid='" + artifactUuid  + " " + e.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for artifactUuid=" + artifactUuid , "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for artifactUuid=" + artifactUuid);
        	throw e;
        } 
        
        // See if something came back.
        if (heatFilesResult == null) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. HeatFiles not found", "CatalogDB", "getHeatFiles", null);
            return null;
        }
        // Name + Version is unique, so should only be one element
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatFiles", null);
        return heatFilesResult;
  }
    
    public void saveHeatFiles (HeatFiles childFile) {
    	 long startTime = System.currentTimeMillis ();
         LOGGER.debug ("Catalog database - save Heat File with name "
                                       + childFile.getFileName());
         try {
//             HeatFiles heatFiles = getHeatFiles (childFile.getVnfResourceId(), childFile.getFileName(), childFile.getAsdcResourceName (),childFile.getVersion());
             HeatFiles heatFiles = getHeatFiles (childFile.getArtifactUuid());
             if (heatFiles == null) {

            	 // asdc_heat_files_save
                 this.getSession ().save (childFile);

             } else {
            	 /* replaced 'heatFiles' by 'childFile'
            	    Based on following comment:
					It must be childFile.setId instead of heatFiles.setId, we must return the ID if it exists in DB.
				 */
            	 childFile.setArtifactUuid(heatFiles.getArtifactUuid());
             }

         } finally {
             LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveHeatFiles", null);
         }
    }

    @Deprecated
    public void saveVfModuleToHeatFiles (int parentVfModuleId, HeatFiles childFile) {
    	/*
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat File to VFmodule link "
                                      + childFile.getFileName());
        try {
            saveHeatFiles (childFile);
            VfModuleToHeatFiles vfModuleToHeatFile = new VfModuleToHeatFiles ();
	        vfModuleToHeatFile.setVfModuleId(parentVfModuleId);
	        vfModuleToHeatFile.setHeatFilesId(childFile.getId());

	        session.save (vfModuleToHeatFile);

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVfModuleToHeatFiles", null);
        }
        */
    }
    
    public void saveVfModuleToHeatFiles (String parentVfModuleId, HeatFiles childFile) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat File to VFmodule link "
                                      + childFile.getFileName());
        try {
            saveHeatFiles (childFile);
            VfModuleToHeatFiles checkExistingEntry = this.getVfModuleToHeatFilesEntry(parentVfModuleId, childFile.getArtifactUuid());
            if (checkExistingEntry == null) {
            	VfModuleToHeatFiles vfModuleToHeatFile = new VfModuleToHeatFiles ();
	        	vfModuleToHeatFile.setVfModuleModelUuid(parentVfModuleId);
	        	vfModuleToHeatFile.setHeatFilesArtifactUuid(childFile.getArtifactUuid());
	        	session.flush();
	        	session.save (vfModuleToHeatFile);
            } else {
            	LOGGER.debug("**Found existing VfModuleToHeatFiles entry for " + checkExistingEntry.toString());
            	LOGGER.debug("No need to save...");
            }
          
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveVfModuleToHeatFiles", null);
        }
    }

    /**
     * Return a Network Resource that matches the Network Customization defined by given MODEL_CUSTOMIZATION_UUID
     *
     * @param networkType
     * @param action
     * @param serviceType
     * @return NetworkRecipe object or null if none found
     */
    public NetworkResource getNetworkResourceByModelUuid(String modelUUID) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get network resource with modelUUID " + modelUUID);

        try {
            String hql =  "FROM NetworkResource WHERE modelUUID=:modelUUID";
            Query query = getSession ().createQuery (hql);
            query.setParameter (MODEL_UUID, modelUUID);

            @SuppressWarnings("unchecked")
            List <NetworkResource> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            
            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);
            
            return resultList.get (0);
        } catch (Exception e) {
        	LOGGER.debug("Error trying to find Network Resource with " + modelUUID +", " + e.getMessage(),e);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkResourceByModelUuid", null);
        }
        return null;
    }


    /**
     * Return a Network recipe that matches a given NETWORK_TYPE, ACTION, and, if specified, SERVICE_TYPE
     *
     * @param networkType
     * @param action
     * @param serviceType
     * @return NetworkRecipe object or null if none found
     */
    public NetworkRecipe getNetworkRecipe (String networkType, String action, String serviceType) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get network recipe with network type " + networkType
                                      + " and action "
                                      + action
                                      + " and service type "
                                      + serviceType);

        try {
            String hql;
            if (serviceType == null) {
                hql = "FROM NetworkRecipe WHERE networkType = :networkType AND action = :action AND serviceType IS NULL ";
            } else {
                hql = "FROM NetworkRecipe WHERE networkType = :networkType AND action = :action AND serviceType = :serviceType ";
            }
            Query query = getSession ().createQuery (hql);
            query.setParameter (NETWORK_TYPE, networkType);
            query.setParameter (ACTION, action);
            if (serviceType != null) {
                query.setParameter ("serviceType", serviceType);
            }

            @SuppressWarnings("unchecked")
            List <NetworkRecipe> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }

            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkRecipe", null);
        }
    }

    
    /**
     * Return a Network recipe that matches a given MODEL_NAME and ACTION
     *
     * @param modelName
     * @param action
     * @return NetworkRecipe object or null if none found
     */
    public NetworkRecipe getNetworkRecipe (String modelName, String action) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get network recipe with network model name " + modelName
                                      + " and action "
                                      + action
                                      );

        try {
            String hql = "FROM NetworkRecipe WHERE modelName = :modelName AND action = :action";

            Query query = getSession ().createQuery (hql);
            query.setParameter (MODEL_NAME, modelName);
            query.setParameter (ACTION, action);

            @SuppressWarnings("unchecked")
            List <NetworkRecipe> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }

            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkRecipe", null);
        }
    }

    /**
     * Return a Network Resource that matches the Network Customization defined by given MODEL_CUSTOMIZATION_UUID
     *
     * @param networkType
     * @param action
     * @param serviceType
     * @return NetworkRecipe object or null if none found
     */
    public NetworkResource getNetworkResourceByModelCustUuid(String modelCustomizationUuid) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get network resource with modelCustomizationUuid " + modelCustomizationUuid);

        try {
            String hql =  "select n FROM NetworkResource n, NetworkResourceCustomization c WHERE n.modelUUID=c.networkResourceModelUuid and c.modelCustomizationUuid = :modelCustomizationUuid";
            Query query = getSession ().createQuery (hql);
            query.setParameter (MODEL_CUSTOMIZATION_UUID, modelCustomizationUuid);

            @SuppressWarnings("unchecked")
            List <NetworkResource> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }

            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } catch (Exception e) {
        	LOGGER.debug("Error trying to find Network Resource with " + modelCustomizationUuid +", " + e.getMessage(),e);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkResourceByModelCustUuid", null);
        }
        return null;
    }

    /**
     * Return a VnfComponents recipe that matches a given VNF_TYPE, VNF_COMPONENT_TYPE, ACTION, and, if specified,
     * SERVICE_TYPE
     *
     * @param vnfType
     * @param vnfComponentType
     * @param action
     * @param serviceType
     * @return VnfComponentsRecipe object or null if none found
     */
    public VnfComponentsRecipe getVnfComponentsRecipe (String vnfType,
                                                       String vnfComponentType,
                                                       String action,
                                                       String serviceType) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Vnf Component recipe with network type " + vnfType
                                      + " and component type "
                                      + vnfComponentType
                                      + " and action "
                                      + action
                                      + " and service type "
                                      + serviceType);

        try {
            String hql;
            if (serviceType == null) {
                hql = "FROM VnfComponentsRecipe WHERE vnfType = :vnfType AND vnfComponentType = :vnfComponentType AND action = :action AND serviceType IS NULL ";
            } else {
                hql = "FROM VnfComponentsRecipe WHERE vnfType = :vnfType AND vnfComponentType = :vnfComponentType AND action = :action AND serviceType = :serviceType ";
            }
            Query query = getSession ().createQuery (hql);
            query.setParameter (VNF_TYPE, vnfType);
            query.setParameter (VNF_COMPONENT_TYPE, vnfComponentType);
            query.setParameter (ACTION, action);
            if (serviceType != null) {
                query.setParameter ("serviceType", serviceType);
            }

            @SuppressWarnings("unchecked")
            List <VnfComponentsRecipe> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfComponentsRecipe", null);
        }
    }

    /**
     * Return a VnfComponents recipe that matches a given VF_MODULE_ID, VNF_COMPONENT_TYPE, ACTION
     *
     * @param vfModuleId
     * @param vnfComponentType
     * @param action
     * @return VnfComponentsRecipe object or null if none found
     */
    public VnfComponentsRecipe getVnfComponentsRecipeByVfModuleModelUUId (String vfModuleModelUUId,
                                                       String vnfComponentType,
                                                       String action) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Vnf Component recipe with vfModuleModelUUId " + vfModuleModelUUId
                                      + " and component type "
                                      + vnfComponentType
                                      + " and action "
                                      + action);

        try {
            String hql;
            hql = "FROM VnfComponentsRecipe WHERE vfModuleModelUUId = :vfModuleModelUUId AND vnfComponentType = :vnfComponentType AND action = :action ";

            Query query = getSession ().createQuery (hql);
            query.setParameter (VF_MODULE_MODEL_UUID, vfModuleModelUUId);
            query.setParameter (VNF_COMPONENT_TYPE, vnfComponentType);
            query.setParameter (ACTION, action);

            @SuppressWarnings("unchecked")
            List <VnfComponentsRecipe> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfComponentsRecipeByVfModuleModelUUId", null);
        }
    }
    
    public List<VnfComponentsRecipe> getVnfComponentRecipes (String vfModuleModelUUId) {
        
        StringBuilder hql = null;
    	
       	hql = new StringBuilder ("FROM VnfComponentsRecipe WHERE vfModuleModelUUId = :vfModuleModelUUId");
    	
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Service recipe with vfModuleModelUUId " + vfModuleModelUUId);

        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter ("vfModuleModelUUId", vfModuleModelUUId);
        
        @SuppressWarnings("unchecked")
        List <VnfComponentsRecipe> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service recipe not found", "CatalogDB", "getVfModuleRecipes", null);
            return Collections.EMPTY_LIST;
        }
        
        resultList.sort(new MavenLikeVersioningComparator());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleRecipes", null);
        return resultList;
    }



    public void saveOrUpdateVnfComponent (VnfComponent vnfComponent) {
        long startTime = System.currentTimeMillis ();

        LOGGER.debug ("Catalog database - save VnfComponent where vnfId="+ vnfComponent.getVnfId()+ " AND componentType="+ vnfComponent.getComponentType());

        VnfComponent vnfComponentDb = this.getVnfComponent(vnfComponent.getVnfId(), vnfComponent.getComponentType());

        try {

                this.getSession ().save (vnfComponent);

        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateVnfComponent", null);
        }
    }

    /**
     * Return a VfModule record that matches a given MODEL_NAME
     *
     * @param modelName
     * @return VfModule object or null if none found
     */
    public VfModule getVfModule (String modelName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get vf module with model name " + modelName);

        try {
            String hql;

            hql = "FROM VfModule WHERE modelName = :modelName";

            Query query = getSession ().createQuery (hql);
            query.setParameter (MODEL_NAME, modelName);

            @SuppressWarnings("unchecked")
            List <VfModule> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModule", null);
        }
    }

    /**
     * Return a VfModule record that matches a given MODEL_NAME
     *
     * @param modelName
     * @return VfModule object or null if none found
     */
    public VfModule getVfModuleByModelUUID (String modelUUID) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get vf module with modelUUID " + modelUUID);

        try {
            String hql;

            hql = "FROM VfModule WHERE modelUUID = :modelUUID";

            Query query = getSession ().createQuery (hql);
            query.setParameter (MODEL_UUID, modelUUID);

            @SuppressWarnings("unchecked")
            List <VfModule> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            resultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleByModelUUID", null);
        }
    }
    
    /**
     * Return a Service recipe that matches a given service ModelUUID and action
     * (modelUUID) and ACTION
     *
     * @param modelUUID
     * @param action    
     * @return ServiceRecipe object or null if none found
     */
    public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID, String action) {                     

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Service recipe with modelUUID=" + modelUUID + " and action=" + action);

        try {
			String hql;
			// based on the new SERVICE_RECIPE schema where SERVICE_MODEL_UUID == MODEL_UUID, a JOIN with the SERVICE table is no longer needed
//			hql = "SELECT new ServiceRecipe(SR.id, SR.serviceModelUUID, SR.action, SR.description, " +
//					"SR.orchestrationUri, SR.serviceParamXSD, case when SR.recipeTimeout is null then 0 else SR.recipeTimeout end, " +
//					"case when SR.serviceTimeoutInterim is null then 0 else SR.serviceTimeoutInterim end, SR.created) " +
//					"FROM Service as S RIGHT OUTER JOIN S.recipes SR " +
//					"WHERE SR.serviceModelUUID = :modelUUID AND SR.action = :action";
			hql = "FROM ServiceRecipe WHERE serviceModelUUID = :modelUUID AND action = :action";
			Query query = getSession().createQuery(hql);
			query.setParameter(MODEL_UUID, modelUUID);
			query.setParameter(ACTION, action);

			@SuppressWarnings("unchecked")
			List<ServiceRecipe> recipeResultList = query.list();
			if (recipeResultList.isEmpty()) {
				LOGGER.debug("Catalog database - recipeResultList is null");
				return null;
			}
			recipeResultList.sort(new MavenLikeVersioningComparator());
			Collections.reverse(recipeResultList);
			LOGGER.debug("Catalog database - recipeResultList contains " + recipeResultList.get(0).toString());

			return recipeResultList.get(0);
        } finally {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipeByModelUUID", null);
        }
    }
    
    /**
     * Return a Service recipe that matches a given SERVICE_NAME_VERSION_ID
     * (MODEL_VERSION_ID) and ACTION
     *
     * @param modelVersionId
     * @param action    
     * @return ServiceRecipe object or null if none found
     */
    @Deprecated
    public ServiceRecipe getServiceRecipe(String modelVersionId,
                                       String action) {                     

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Service recipe with modeVersionId=" + modelVersionId
                                      + " and action=" + action);

        try {
			String hql;
			// Note: Even with the implementation of the HQL JOIN below, the code for the two separate
			//       SELECTs will be retained/commented for now in the event some subsequent JOIN issue arises
			// 1st query to get the Service record for the given SERVICE_NAME_VERSION_ID (MODEL_VERSION_ID)
/*			hql = "FROM Service WHERE serviceNameVersionId = :serviceNameVersionId";
			Query query = getSession().createQuery(hql);
			query.setParameter(SERVICE_NAME_VERSION_ID, modelVersionId);

			@SuppressWarnings("unchecked")
			List<Service> serviceResultList = query.list();
			if (serviceResultList.isEmpty()) {
				LOGGER.debug("Catalog database - serviceResultList is null");
				return null;
			}
			Collections.sort(serviceResultList, new MavenLikeVersioningComparator());
			Collections.reverse(serviceResultList);
			LOGGER.debug("Catalog database - serviceResultList contains " + serviceResultList.get(0).toString());

			// 2nd query to get the ServiceRecipe record corresponding to the Service from the 1st query
			hql = "FROM ServiceRecipe WHERE serviceModelUUID = :serviceModelUUID AND action = :action";
			query = getSession().createQuery(hql);
			// The SERVICE table 'id' field maps to the SERVICE_RECIPE table 'SERVICE_ID' field
			query.setParameter(SERVICE_ID, serviceResultList.get(0).getId());
			query.setParameter(ACTION, action);
*/
			// The following SELECT performs a JOIN across the SERVICE and SERVICE_RECIPE tables. It required a new
			// CTR in the ServiceRecipe Class to populate that object (the other option was to parse the Object[]
			// returned by createQuery() and manually populate the ServiceRecipe object). Two of the 'int' fields in the
			// SERVICE_RECIPE DB schema (the timeouts) permit NULL values which required some additional code in the
			// SELECT to generate a default of 0 (needed by the CTR) in the cases where the value is NULL.
			hql = "SELECT new ServiceRecipe(SR.id, SR.serviceModelUUID, SR.action, SR.description, " +
					"SR.orchestrationUri, SR.serviceParamXSD, case when SR.recipeTimeout is null then 0 else SR.recipeTimeout end, " +
					"case when SR.serviceTimeoutInterim is null then 0 else SR.serviceTimeoutInterim end, SR.created) " +
					"FROM Service as S RIGHT OUTER JOIN S.recipes SR " +
					"WHERE SR.serviceModelUUID = S.id AND S.serviceNameVersionId = :serviceNameVersionId AND SR.action = :action";
			Query query = getSession().createQuery(hql);
			query.setParameter(MODEL_UUID, modelVersionId);
			query.setParameter(ACTION, action);

			@SuppressWarnings("unchecked")
			List<ServiceRecipe> recipeResultList = query.list();
			if (recipeResultList.isEmpty()) {
				LOGGER.debug("Catalog database - recipeResultList is null");
				return null;
			}
			recipeResultList.sort(new MavenLikeVersioningComparator());
			Collections.reverse(recipeResultList);
			LOGGER.debug("Catalog database - recipeResultList contains " + recipeResultList.get(0).toString());

			return recipeResultList.get(0);
        } finally {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipe", null);
        }
    }

    /**
     * Return a Model recipe that matches a given MODEL_TYPE, MODEL_VERSION_ID, ACTION
     * Note: This method is not currently used but was retained in the event the
     *       architecture moves back to a MODEL/MODEL_RECIPE structure.
     *
     * @param modelType
     * @param modelVersionId
     * @param action
     * @return ModelRecipe object or null if none found
     */
    public ModelRecipe getModelRecipe(String modelType,
                                      String modelVersionId,
                                      String action) {

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Model recipe with modelType=" + modelType
                + " and modeVersionId=" + modelVersionId
                + " and action=" + action);

        try {
            String hql;
            // TBD - at some point it would be desirable to figure out how to do a  HQL JOIN across
            //       the MODEL and MODEL_RECIPE tables in HQL instead of 2 separate queries.
            //       There seems to be 2 issues: formatting a hql query that executes successfully
            //       and then being able to generate a result that will fit into the ModelRecipe class.

            // 1st query to get the Model record for the given MODEL_TYPE and MODEL_VERSION_ID
            hql = "FROM Model WHERE modelType = :modelType AND modelVersionId = :modelVersionId";
            Query query = getSession().createQuery(hql);
            query.setParameter(MODEL_TYPE, modelType);
            query.setParameter(MODEL_VERSION_ID, modelVersionId);

            @SuppressWarnings("unchecked")
            List<Model> modelResultList = query.list();
            if (modelResultList.isEmpty()) {
                LOGGER.debug("Catalog database - modelResultList is null");
                return null;
            }
            modelResultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse(modelResultList);
            LOGGER.debug("Catalog database - modelResultList contains " + modelResultList.get(0).toString());

            // 2nd query to get the ModelRecipe record corresponding to the Model from the 1st query
            hql = "FROM ModelRecipe WHERE modelId = :modelId AND action = :action";
            query = getSession().createQuery(hql);
            // The MODEL table 'id' field maps to the MODEL_RECIPE table 'MODEL_ID' field
            query.setParameter(MODEL_ID, modelResultList.get(0).getId());
            query.setParameter(ACTION, action);

            @SuppressWarnings("unchecked")
            List<ModelRecipe> recipeResultList = query.list();
            if (recipeResultList.isEmpty()) {
                LOGGER.debug("Catalog database - recipeResultList is null");
                return null;
            }
            recipeResultList.sort(new MavenLikeVersioningComparator());
            Collections.reverse(recipeResultList);
            LOGGER.debug("Catalog database - recipeResultList contains " + recipeResultList.get(0).toString());

            return recipeResultList.get(0);
        } finally {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getModelRecipe", null);
        }
    }


    /**
     * Verify the health of the DB.
     *
     * @return boolean value indicate whether DB is healthy
     */
    public boolean healthCheck () {
        long startTime = System.currentTimeMillis ();
        Session session = this.getSession ();

        // Query query = session.createQuery (" from ActiveRequests ");
        Query query = session.createSQLQuery (" show tables ");

        List<?> list = query.list();
        LOGGER.debug("healthCheck CatalogDB - Successful");
        return true;
    }
    
    public < E > E executeQuerySingleRow(String hql, HashMap<String, String> variables, boolean retry) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - executeQuery: " + hql + (retry ? ", retry=true" : ", retry=false"));
        Query query = getSession().createQuery(hql);

        StringBuilder sb = new StringBuilder();
        if (variables != null) {
        	for(Map.Entry<String, String> entry : variables.entrySet()){
        		sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        		query.setParameter(entry.getKey(), entry.getValue());
        	}
        }
        LOGGER.debug("Variables:\n" + sb.toString());

        E theObject = null;
        try {
            theObject = (E) query.uniqueResult();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for " + hql, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for " + hql );
        	throw nure;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while performing " + hql + "; he message:" + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception while performing hql=" + hql, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for hql=" + hql);
        	if (retry) {
        		LOGGER.debug("***WILL RETRY***");
        		return this.executeQuerySingleRow(hql, variables, false);
        	} else {
        		throw he;
        	}
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while performing '" + hql + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception performing " + hql, "", "", MsoLogger.ErrorCode.DataError, "Generic exception performing " + hql);
        	if (retry) {
        		LOGGER.debug("***WILL RETRY***");
        		return this.executeQuerySingleRow(hql, variables, false);
        	} else {
        		throw e;
        	}
        }

        if (theObject == null) {
        	LOGGER.debug("Returning null");
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "executeQuerySingleRow", null);
        } else {
        	LOGGER.debug("Returning an Object");
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "executeQuerySingleRow", null);
        }
        return theObject;
    }
    
    public < E > List<E> executeQueryMultipleRows(String hql, HashMap<String, String> variables, boolean retry) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug("Catalog database - executeQuery: " + hql + (retry ? ", retry=true" : ", retry=false"));
        Query query = getSession().createQuery(hql);

        StringBuilder sb = new StringBuilder();
        if (variables != null) {
        	for(Map.Entry<String, String> entry : variables.entrySet()){
        		sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        		query.setParameter(entry.getKey(), entry.getValue());
        	}
        }
        LOGGER.debug("Variables:\n" + sb.toString());

        List<E> theObjects = null;
        try {
        	theObjects = (List<E>) query.list ();
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while performing " + hql + "; he message:" + he.getMessage());
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception while performing hql=" + hql, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for hql=" + hql);
        	if (retry) {
        		LOGGER.debug("***WILL RETRY***");
        		return this.executeQuerySingleRow(hql, variables, false);
        	} else {
        		throw he;
        	}
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while performing '" + hql + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception performing " + hql, "", "", MsoLogger.ErrorCode.DataError, "Generic exception performing " + hql);
        	if (retry) {
        		LOGGER.debug("***WILL RETRY***");
        		return this.executeQuerySingleRow(hql, variables, false);
        	} else {
        		throw e;
        	}
        }

        if (theObjects == null) {
        	LOGGER.debug("Returning null");
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "executeQuerySingleRow", null);
        } else {
        	try {
        		LOGGER.debug("Returning theObjects:" + theObjects.size());
        	} catch (Exception e) {
        		LOGGER.debug("Returning theObjects",e);
        	}
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "executeQuerySingleRow", null);
        }
        return theObjects;
    }
}
