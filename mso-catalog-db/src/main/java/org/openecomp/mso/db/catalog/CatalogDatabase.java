/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.db.catalog;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openecomp.mso.db.catalog.beans.*;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioningComparator;
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

    protected static final String NETWORK_TYPE = "networkType";
    protected static final String ACTION = "action";
    protected static final String VNF_TYPE = "vnfType";
    protected static final String SERVICE_TYPE = "serviceType";
    protected static final String VNF_COMPONENT_TYPE = "vnfComponentType";
    protected static final String MODEL_NAME = "modelName";
    protected static final String TYPE = "type";
    protected static final String VF_MODULE_ID = "vfModuleId";
    protected static boolean initialized = false;
    protected static SessionFactory sessionFactory;
    protected static ServiceRegistry serviceRegistry;
    protected static final String SERVICE_NAME_VERSION_ID= "serviceNameVersionId";
    
    protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    protected Session session = null;

    public CatalogDatabase () {
    }


    private Session getSession () {
        if (!initialized) {
            LOGGER.debug ("Initializing Catalog Database in Hibernate");
            Configuration configuration = null;
            try {
            		if ("MYSQL".equals (System.getProperty ("mso.db"))
                            || "MARIADB".equals (System.getProperty ("mso.db"))) {
                        configuration = new Configuration ().configure ("hibernate-catalog-mysql.cfg.xml");

                        serviceRegistry = new ServiceRegistryBuilder ().applySettings (configuration.getProperties ()).buildServiceRegistry ();

                        sessionFactory = configuration.buildSessionFactory (serviceRegistry);
                    } else {
                    	LOGGER.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "DB Connection not specified to the JVM,choose either:-Dmso.db=MARIADB, -Dmso.db=MYSQL or -Dmso.container=AJSC", "", "", MsoLogger.ErrorCode.DataError, "DB Connection not specified to the JVM,choose either:-Dmso.db=MARIADB, -Dmso.db=MYSQL or -Dmso.container=AJSC");
                    }
            } catch (Exception e) {
                LOGGER.error (MessageEnum.GENERAL_EXCEPTION_ARG,
                              "Catalog DB initialization issue: " + e.getMessage (), "", "", MsoLogger.ErrorCode.DataError, "Catalog DB initialization issue: " + e.getMessage (), e);
                throw e;
            }
            initialized = true;

            LOGGER.debug ("Catalog Database initialization complete");
        }

        if (session == null) {
            try {
                session = sessionFactory.openSession ();
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
    public List <HeatTemplate> getAllHeatTemplates () {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get all Heat templates");
        String hql = "FROM HeatTemplate";
        Query query = getSession ().createQuery (hql);

        List <HeatTemplate> result = query.list ();
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getAllHeatTemplates", null);
        return result;
    }

    /**
     * Fetch a specific Heat Template by ID.
     *
     * @param templateId
     * @return HeatTemplate object or null if none found
     */
    public HeatTemplate getHeatTemplate (int templateId) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat template with id " + templateId);

        HeatTemplate template = (HeatTemplate) getSession ().get (HeatTemplate.class, templateId);
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return template;
    }

    /**
     * Return the newest version of a specific Heat Template (queried by Name).
     *
     * @param templateName
     * @return HeatTemplate object or null if none found
     */
    public HeatTemplate getHeatTemplate (String templateName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat template with name " + templateName);

        String hql = "FROM HeatTemplate WHERE templateName = :template_name";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("template_name", templateName);

        @SuppressWarnings("unchecked")
        List <HeatTemplate> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No template found", "CatalogDB", "getHeatTemplate", null);
            return null;
        }
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return resultList.get (0);
    }

    /**
     * Return a specific version of a specific Heat Template (queried by Name).
     *
     * @param templateName
     * @param version
     * @return HeatTemplate object or null if none found
     */
    public HeatTemplate getHeatTemplate (String templateName, String version) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Heat template with name " + templateName
                                      + " and version "
                                      + version);

        String hql = "FROM HeatTemplate WHERE templateName = :template_name AND version = :version";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("template_name", templateName);
        query.setParameter ("version", version);

        @SuppressWarnings("unchecked")
        List <HeatTemplate> resultList = query.list ();

        // See if something came back.
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. No template found.", "CatalogDB", "getHeatTemplate", null);
            return null;
        }
        // Name + Version is unique, so should only be one element
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getHeatTemplate", null);
        return resultList.get (0);
    }

    /**
     * Fetch a Service definition
     */
    public Service getService (String serviceName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get service with name " + serviceName);

        String hql = "FROM Service WHERE serviceName = :service_name";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("service_name", serviceName);

        Service service = null;
        try {
        	service = (Service) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: serviceName='" + serviceName + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for serviceName=" + serviceName, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for serviceName=" + serviceName);
        	return null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: serviceName='" + serviceName + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for serviceName=" + serviceName, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for serviceName=" + serviceName);
        	return null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: serviceName='" + serviceName);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for serviceName=" + serviceName, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for serviceName=" + serviceName);
        	return null;
        }
        if (service == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getService", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getService", null);
        }

        return service;
    }
    
    /**
     * Fetch a Service definition
     */
    public Service getServiceByUUID (String serviceNameVersionId) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get service with UUID " + serviceNameVersionId);

        String hql = "FROM Service WHERE serviceNameVersionId = :service_id";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("service_id", serviceNameVersionId);

        Service service = null;
        try {
        	service = (Service) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: serviceNameVersionId='" + serviceNameVersionId + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for serviceNameVersionId=" + serviceNameVersionId, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for serviceNameVersionId=" + serviceNameVersionId);
        	return null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: serviceName='" + serviceNameVersionId + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for serviceNameVersionId=" + serviceNameVersionId, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for serviceNameVersionId=" + serviceNameVersionId);
        	return null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: serviceName='" + serviceNameVersionId);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for serviceNameVersionId=" + serviceNameVersionId, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for serviceNameVersionId=" + serviceNameVersionId);
        	return null;
        }
        if (service == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getService", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getService", null);
        }

        return service;
    }
    
    /**
     * Fetch the Common Service API definition using Http Method + serviceNameVersionId
     */
    public Service getService(HashMap<String, String> map, String httpMethod) {
     
        String serviceNameVersionId = map.get("serviceNameVersionId");
        Query query;
        String serviceId = "not_set";
        String serviceVersion = "not_set";
        
        if(serviceNameVersionId != null && serviceNameVersionId.length() > 0){
        	LOGGER.debug ("Catalog database - get serviceNameVersionId with id " + serviceNameVersionId);

        	String hql = "FROM Service WHERE service_name_version_id = :service_name_version_id and http_method = :http_method";
        	query = getSession ().createQuery (hql);
            query.setParameter ("service_name_version_id", serviceNameVersionId);
         } else {
        	serviceId = map.get("serviceId");
        	serviceVersion = map.get("serviceVersion");
            LOGGER.debug ("Catalog database - get serviceId with id " + serviceId + " and serviceVersion with " + serviceVersion);

            String hql = "FROM Service WHERE service_id = :service_id and service_version = :service_version and http_method = :http_method";
            query = getSession ().createQuery (hql);
            query.setParameter ("service_id", serviceId);
            query.setParameter ("service_version", serviceVersion);
         }
        
        query.setParameter ("http_method", httpMethod);

        long startTime = System.currentTimeMillis ();
        Service service = null;
        try {
        	service = (Service) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - data integrity error: service_id='" + serviceId + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for service_id=" + serviceId + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for service_id=" + serviceId);
        	service = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: service_id='" + serviceId + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for service_id=" + serviceId + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for service_id=" + serviceId);
        	service = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: service_id='" + serviceId + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for service_id=" + serviceId + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for service_id=" + serviceId);
        	service = null;
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
     * @param serviceName
     * @return Service object or null if none found
     */
    public Service getServiceByName (String serviceName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get service with name " + serviceName);

        String hql = "FROM Service WHERE serviceName = :service_name";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("service_name", serviceName);

        @SuppressWarnings("unchecked")
        List <Service> resultList = query.list ();

        // See if something came back. 
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service not found", "CatalogDB", "getServiceByName", null);
            return null;
        }
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceByName", null);
        return resultList.get (0);
    }

    /**
     * Return a Service recipe that matches a given SERVICE_NAME_VERSION_ID
     * (MODEL_VERSION_ID) and ACTION
     *
     * @param modelVersionId
     * @param action    
     * @return ServiceRecipe object or null if none found
     */
    public ServiceRecipe getServiceRecipe(String modelVersionId,
                                       String action) {                     

        long startTime = System.currentTimeMillis();
        LOGGER.debug("Catalog database - get Service recipe with modeVersionId=" + modelVersionId
                                      + " and action=" + action);

        try {
                        String hql;

                        hql = "SELECT new ServiceRecipe(SR.id, SR.serviceId, SR.action, SR.description, " +
                                        "SR.orchestrationUri, SR.serviceParamXSD, case when SR.recipeTimeout is null then 0 else SR.recipeTimeout end, " +
                                        "case when SR.serviceTimeoutInterim is null then 0 else SR.serviceTimeoutInterim end, SR.created) " +
                                        "FROM Service as S RIGHT OUTER JOIN S.recipes SR " +
                                        "WHERE SR.serviceId = S.id AND S.serviceNameVersionId = :serviceNameVersionId AND SR.action = :action";
                        Query query = getSession().createQuery(hql);
                        query.setParameter(SERVICE_NAME_VERSION_ID, modelVersionId);
                        query.setParameter(ACTION, action);

                        @SuppressWarnings("unchecked")
                        List<ServiceRecipe> recipeResultList = query.list();
                        if (recipeResultList.isEmpty()) {
                                LOGGER.debug("Catalog database - recipeResultList is null");
                                return null;
                        }
                        Collections.sort(recipeResultList, new MavenLikeVersioningComparator());
                        Collections.reverse(recipeResultList);
                        LOGGER.debug("Catalog database - recipeResultList contains " + recipeResultList.get(0).toString());

                        return recipeResultList.get(0);
        } finally {
            LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipe", null);
        }
    }
    
    /**
     * Return a newest version of Service recipe that matches a given SERVICE_ID and ACTION
     *
     * @param serviceId
     * @param action     * 
     * @return ServiceRecipe object or null if none found
     */
    public ServiceRecipe getServiceRecipe (int serviceId, String action) {
       
        StringBuilder hql = new StringBuilder ("FROM ServiceRecipe WHERE serviceId = :serviceId AND action = :action ");
    
   
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Service recipe with serviceId " + Integer.toString(serviceId)
                                      + " and action "
                                      + action
                                      );

        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter ("serviceId", serviceId);
        query.setParameter (ACTION, action);
        
        @SuppressWarnings("unchecked")
        List <ServiceRecipe> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Service recipe not found", "CatalogDB", "getServiceRecipe", null);
            return null;
        }
        
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getServiceRecipe", null);
        return resultList.get (0);
    }

    
    /**
     * Return the VNF component data - queried by the VNFs ID and the component type.
     *
     * @param vnfId
     * @param type
     * @return VnfComponent object or null if none found
     */
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
        	result = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: vnf_id='" + vnfId + "', componentType='" + type + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for vnf_id=" + vnfId + " and componentType=" + type, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnf_id=" + vnfId);
        	result = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: vnf_id='" + vnfId + "', componentType='" + type + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for vnf_id=" + vnfId + " and componentType=" + type, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for vnf_id=" + vnfId);
        	result = null;
        }

    	//LOGGER.debug("Found VNF Component: " + result.toString());
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

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get vnf resource with name " + vnfType);

        String hql = "FROM VnfResource WHERE vnfType = :vnf_name";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("vnf_name", vnfType);

        @SuppressWarnings("unchecked")
        List <VnfResource> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF not found", "CatalogDB", "getVnfResource", null);
            return null;
        }
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfResource", null);
        return resultList.get (0);
    }

    /**
     * Return the newest version of a specific VNF resource (queried by Name).
     *
     * @param vnfType
     * @param version
     * @return VnfResource object or null if none found
     */
    public VnfResource getVnfResource (String vnfType, String serviceVersion) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VNF resource with name " + vnfType);

        String hql = "FROM VnfResource WHERE vnfType = :vnfName and version = :serviceVersion";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("vnfName", vnfType);
        query.setParameter ("serviceVersion", serviceVersion);

        VnfResource resource = null;
        try {
        	resource = (VnfResource) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: vnfType='" + vnfType + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for vnfType=" + vnfType + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for vnfType=" + vnfType);
        	resource = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: vnfType='" + vnfType + "', asdc_service_model_version='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for vnfType=" + vnfType + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for vnfType=" + vnfType);
        	resource = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: vnfType='" + vnfType + "', serviceVersion='" + serviceVersion + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for vnfType=" + vnfType + " and serviceVersion=" + serviceVersion, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for vnfType=" + vnfType);
        	resource = null;
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
    public VfModule getVfModuleModelName (String modelName) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get vfModuleModelName with name " + modelName);

        String hql = "FROM VfModule WHERE model_name = :model_name";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("model_name", modelName);

        @SuppressWarnings("unchecked")
        List <VfModule> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF not found", "CatalogDB", "getVfModuleModelName", null);
            return null;
        }
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleModelName", null);
        return resultList.get (0);
    }
    
    public VfModule getVfModuleModelName (String modelName, String model_version) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get vfModuleModelName with type='" + modelName + "' and asdc_service_model_version='" + model_version + "'");

        String hql = "FROM VfModule WHERE model_name = :model_name and version = :model_version";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("model_name", modelName);
        query.setParameter ("model_version", model_version);
        
        VfModule module = null;
        try {
        	module = (VfModule) query.uniqueResult ();
        } catch (org.hibernate.NonUniqueResultException nure) {
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: type='" + modelName + "', asdc_service_model_version='" + model_version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for type=" + modelName + " and version=" + model_version, "", "", MsoLogger.ErrorCode.DataError, "Non unique result for type=" + modelName);
        	module = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: type='" + modelName + "', asdc_service_model_version='" + model_version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for type=" + modelName + " and version=" + model_version, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for type=" + modelName);
        	module = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: type='" + modelName + "', asdc_service_model_version='" + model_version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for type=" + modelName + " and version=" + model_version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for type=" + modelName);
        	module = null;
        }
        if (module == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleModelName", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleModelName", null);
        }
        return module;
    }
   

    /**
     * Return the newest version of a specific Network resource (queried by Type).
     *
     * @param networkType
     * @return NetworkResource object or null if none found
     */
    public NetworkResource getNetworkResource (String networkType) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get network resource with type " + networkType);

        String hql = "FROM NetworkResource WHERE networkType = :network_type";
        Query query = getSession ().createQuery (hql);
        query.setParameter ("network_type", networkType);

        @SuppressWarnings("unchecked")
        List <NetworkResource> resultList = query.list ();

        // See if something came back. Name is unique, so
        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. Network Resource not found", "CatalogDB", "getNetworkResource", null);
            return null;
        }

        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);
        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkResource", null);
        return resultList.get (0);
    }

    /**
     * Return a VNF recipe that matches a given VNF_TYPE, ACTION, and, if specified, SERVICE_TYPE
     *
     * @param vnfType
     * @param action
     * @param serviceType The service Name, if null or empty is provided, it won't be taken into account
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVnfRecipe (String vnfType, String action, String serviceType) {
        boolean withServiceType = false;

        StringBuilder hql = new StringBuilder ("FROM VnfRecipe WHERE vnfType = :vnfType AND action = :action ");

        // If query c
        if (serviceType == null || serviceType.isEmpty ()) {
            hql.append ("AND serviceType is NULL ");
        } else {
            hql.append ("AND serviceType = :serviceType ");
            withServiceType = true;
        }
   
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VNF recipe with name " + vnfType
                                      + " and action "
                                      + action
                                      + " and service type "
                                      + serviceType);

        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter (VNF_TYPE, vnfType);
        query.setParameter (ACTION, action);
        if (withServiceType) {
            query.setParameter (SERVICE_TYPE, serviceType);
        }

        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfRecipe", null);
            return null;
        }
        
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfRecipe", null);
        return resultList.get (0);
    }

    /**
     * Return a VNF recipe that matches a given VNF_TYPE and ACTION
     *
     * @param vnfType
     * @param action
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVnfRecipe (String vnfType, String action) {
        StringBuilder hql = new StringBuilder ("FROM VnfRecipe WHERE vnfType = :vnfType AND action = :action ");

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VNF recipe with name " + vnfType
                                      + " and action "
                                      + action);

        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter (VNF_TYPE, vnfType);
        query.setParameter (ACTION, action);

        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfRecipe", null);
            return null;
        }

        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfRecipe", null);
        return resultList.get (0);
    }
    
    /**
     * Return a VNF recipe that matches a given VF_MODULE_ID and ACTION
     *
     * @param vfModuleId
     * @param action     
     * @return VnfRecipe object or null if none found
     */
    public VnfRecipe getVnfRecipeByVfModuleId (String vnfType, String vfModuleId, String action) {
    	
    	StringBuilder hql = new StringBuilder ("FROM VnfRecipe WHERE vfModuleId = :vfModuleId and action = :action  ");
        
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VNF Recipe with vfModuleId " + vfModuleId);
           
        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter (VF_MODULE_ID, vfModuleId);
        query.setParameter (ACTION, action);
        
        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe Entry not found", "CatalogDB", "getVnfRecipeByVfModuleId", null);
            return null;
        }
        
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF Recipe Entry found", "CatalogDB", "getVnfRecipeByVfModuleId", null);
        return resultList.get (0);
    }
    
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
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleType", null);
        return resultList.get (0);
    }
    
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
        	module = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: type='" + type + "', asdc_service_model_version='" + version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for type=" + type + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for type=" + type);
        	module = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: type='" + type + "', asdc_service_model_version='" + version + "'");
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for type=" + type + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for type=" + type);
        	module = null;
        }
        if (module == null) {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "NotFound", "CatalogDB", "getVfModuleType", null);
        } else {
        	LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModuleType", null);
        }
        return module;
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
        
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        VfModule vfMod = resultList.get(0);
        
        int id = vfMod.getId();
        String vfModuleId = Integer.toString(id);        
        
        StringBuilder hql1 = new StringBuilder ("FROM VnfRecipe WHERE vfModuleId = :vfModuleId AND action = :action ");
          
        LOGGER.debug ("Catalog database - get VNF recipe with vf module id " + vfModuleId
                                      + " and action "
                                      + action);

        Query query1 = getSession ().createQuery (hql1.toString ());
        query1.setParameter (VF_MODULE_ID, vfModuleId);
        query1.setParameter (ACTION, action);
       
        @SuppressWarnings("unchecked")
        List <VnfRecipe> resultList1 = query1.list ();

        if (resultList1.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVfModuleRecipe", null);
            return null;
        }
        
        Collections.sort (resultList1, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList1);

        LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe found", "CatalogDB", "getVfModuleRecipe", null);
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
    public VnfComponentsRecipe getVnfComponentsRecipe (String vnfType, String vfModuleModelName, String asdcServiceModelVersion, String modelVersion, String action) {
    	String vfModuleType = vnfType + "::" + vfModuleModelName;
    	
    	StringBuilder hql = new StringBuilder ("FROM VfModule WHERE type = :type AND version = :version AND modelVersion = :modelVersion");
        
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get VF MODULE  with type " + vfModuleType + ", asdcServiceModelVersion " + asdcServiceModelVersion + ", modelVersion " + modelVersion);
           
        Query query = getSession ().createQuery (hql.toString ());
        query.setParameter (TYPE, vfModuleType);
        query.setParameter ("version", asdcServiceModelVersion);
        query.setParameter ("modelVersion", modelVersion);
        
        @SuppressWarnings("unchecked")
        List <VfModule> resultList = query.list ();

        if (resultList.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VF Module Entry not found", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }
        
        Collections.sort (resultList, new MavenLikeVersioningComparator ());
        Collections.reverse (resultList);

        VfModule vfMod = resultList.get(0);
        
        int id = vfMod.getId();
        String vfModuleId = Integer.toString(id);        
        
        StringBuilder hql1 = new StringBuilder ("FROM VnfComponentsRecipe WHERE vfModuleId = :vfModuleId AND action = :action ");
          
        LOGGER.debug ("Catalog database - get Vnf Components recipe with vf module id " + vfModuleId
                                      + " and action "
                                      + action);

        Query query1 = getSession ().createQuery (hql1.toString ());
        query1.setParameter (VF_MODULE_ID, vfModuleId);
        query1.setParameter (ACTION, action);
       
        @SuppressWarnings("unchecked")
        List <VnfComponentsRecipe> resultList1 = query1.list ();

        if (resultList1.isEmpty ()) {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully. VNF recipe not found", "CatalogDB", "getVnfComponentsRecipe", null);
            return null;
        }
        
        Collections.sort (resultList1, new MavenLikeVersioningComparator ());
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

    public Map <String, Object> getNestedTemplates (int templateId) {
        Map <String, Object> nestedTemplates = null;
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
        nestedTemplates = new HashMap <String, Object> ();
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

    /*
     * Fetch any files in the HEAT_FILES table 1510
     */
    public Map <String, HeatFiles> getHeatFiles (int vnfResourceId) {
       Map <String, HeatFiles> heatFiles = null;

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
        heatFiles = new HashMap <String, HeatFiles> ();
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
    
    public Map <String, HeatFiles> getHeatFilesForVfModule(int vfModuleId) {
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
        //TODO - convert this all with one join - brute force for now due to time
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
        LOGGER.debug ("Catalog database - save Heat Template with name " + heat.getTemplateName());

        heat.setParameters(null);
        try {
            HeatTemplate heatTemp = this.getHeatTemplate (heat.getTemplateName (),
                                                          heat.getVersion (),
                                                          heat.getAsdcResourceName ());
            if (heatTemp == null) {
                this.getSession ().save (heat);

                if (paramSet != null) {
                    for (HeatTemplateParam param : paramSet) {
                        param.setHeatTemplateId (heat.getId ());
                    }
                    heat.setParameters (paramSet);
                    this.getSession ().merge (heat);
                }

            } else {
            	heat.setId(heatTemp.getId());
            }
        } finally {
        	heat.setParameters(paramSet);
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
        	LOGGER.debug("Non Unique Result Exception - the Catalog Database does not match a unique row - data integrity error: envName='" + name + "', version='" + version + "' and asdcResourceName=" + asdcResourceName);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " non unique result for envName=" + name + " and version=" + version + " and asdcResourceName=" + asdcResourceName, "", "", MsoLogger.ErrorCode.DataError, "non unique result for envName=" + name);
        	env = null;
        } catch (org.hibernate.HibernateException he) {
        	LOGGER.debug("Hibernate Exception - while searching for: envName='" + name + "', asdc_service_model_version='" + version + "' and asdcResourceName=" + asdcResourceName);
        	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for envName=" + name + " and version=" + version + " and asdcResourceName=" + asdcResourceName, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for envName=" + name);
        	env = null;
        } catch (Exception e) {
        	LOGGER.debug("Generic Exception - while searching for: envName='" + name + "', asdc_service_model_version='" + version + "' and asdcResourceName=" + asdcResourceName);
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
     * Save the HeatEnvironment
     *
     * @param env The Environment
     */
    public void saveHeatEnvironment (HeatEnvironment env) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Heat environment with name "
                                      + env.getEnvironment());
        try {
            HeatEnvironment dbEnv = getHeatEnvironment (env.getName (), env.getVersion (), env.getAsdcResourceName ());
            if (dbEnv == null) {

                this.getSession ().save (env);
               
            } else {
            	env.setId(dbEnv.getId());
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
        LOGGER.debug ("Catalog database - save VNF Resource with VNF type " + vnfResource.getVnfType ());
        try {

            if (vnfResource.getId() != 0) {
                this.getSession ().merge (vnfResource);
            } else {
                this.getSession ().save (vnfResource);
            }
      
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateVnfResource", null);
        }
    }
    
    public void saveService (Service service) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save Service with ServiceName/Version/serviceUUID(SERVICE_NAME_VERSION_ID)" + service.getServiceName()+"/"+service.getServiceVersion()+"/"+service.getServiceNameVersionId());
        try {
        	Service serviceDB = this.getServiceByUUID(service.getServiceNameVersionId());
            if (serviceDB == null) {
            	this.getSession ().save (service);
            }
      
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateService", null);
        }
    }
    
    public void saveOrUpdateVfModule (VfModule vfModule) {
        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - save VNF Module with VF Model Name " + vfModule.getModelName());
        try {

            if (vfModule.getId() != 0) {
                this.getSession ().merge (vfModule);
            } else {
                this.getSession ().save (vfModule);
            }
      
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveOrUpdateVfModule", null); 
        }
    }

    public HeatNestedTemplate getNestedHeatTemplate(int parentTemplateId, int childTemplateId) {
    	  long startTime = System.currentTimeMillis ();
          LOGGER.debug ("Catalog database - get nested Heat template with PerentId-Child Id "
                                        + parentTemplateId +"-"+childTemplateId);
          try {
              HeatNestedTemplate nestedTemplate = new HeatNestedTemplate ();
              nestedTemplate.setParentTemplateId (parentTemplateId);
              nestedTemplate.setChildTemplateId (childTemplateId);
              
              return (HeatNestedTemplate)session.get (HeatNestedTemplate.class,nestedTemplate);
          } finally {
              LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNestedHeatTemplate", null);
          }
    }
      
    public void saveNestedHeatTemplate (int parentTemplateId, HeatTemplate childTemplate, String yamlFile) {
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
    }
    
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
          	heatFilesResult = null;
          } catch (org.hibernate.HibernateException he) {
          	LOGGER.debug("Hibernate Exception - while searching for: fileName='" + fileName + "', vnfResourceId='" + vnfResourceId + "' and asdcResourceName=" + asdcResourceName + " and version=" + version);
          	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Hibernate exception searching for fileName=" + fileName + " and vnfResourceId=" + vnfResourceId + " and asdcResourceName=" + asdcResourceName + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Hibernate exception searching for fileName=" + fileName);
          	heatFilesResult = null;
          } catch (Exception e) {
          	LOGGER.debug("Generic Exception - while searching for: fileName='" + fileName + "', vnfResourceId='" + vnfResourceId + "' and asdcResourceName=" + asdcResourceName + " and version=" + version);
          	LOGGER.error(MessageEnum.GENERAL_EXCEPTION, " Generic exception searching for fileName=" + fileName + " and vnfResourceId=" + vnfResourceId + " and asdcResourceName=" + asdcResourceName + " and version=" + version, "", "", MsoLogger.ErrorCode.DataError, "Generic exception searching for fileName=" + fileName);
          	heatFilesResult = null;
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
             HeatFiles heatFiles = getHeatFiles (childFile.getVnfResourceId(), childFile.getFileName(), childFile.getAsdcResourceName (),childFile.getVersion());
             if (heatFiles == null) {

            	 // asdc_heat_files_save
                 this.getSession ().save (childFile);
                 
             } else {
            	 /* replaced 'heatFiles' by 'childFile'
            	    Based on following comment:
					It must be childFile.setId instead of heatFiles.setId, we must return the ID if it exists in DB.
				 */
            	 childFile.setId(heatFiles.getId());
             }

         } finally {
             LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "saveHeatFiles", null);
         }
    }
    
    public void saveVfModuleToHeatFiles (int parentVfModuleId, HeatFiles childFile) {
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
            
            Collections.sort (resultList, new MavenLikeVersioningComparator ());
            Collections.reverse (resultList);
            
            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkRecipe", null);
        }
    }
    
    /**
     * Return a Network recipe that matches a given NETWORK_TYPE and ACTION
     *
     * @param networkType
     * @param action 
     * @return NetworkRecipe object or null if none found
     */
    public NetworkRecipe getNetworkRecipe (String networkType, String action) {

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get network recipe with network type " + networkType
                                      + " and action "
                                      + action
                                      );

        try {
            String hql = "FROM NetworkRecipe WHERE networkType = :networkType AND action = :action";
            
            Query query = getSession ().createQuery (hql);
            query.setParameter (NETWORK_TYPE, networkType);
            query.setParameter (ACTION, action);
            
            @SuppressWarnings("unchecked")
            List <NetworkRecipe> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            
            Collections.sort (resultList, new MavenLikeVersioningComparator ());
            Collections.reverse (resultList);
            
            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getNetworkRecipe", null);
        }
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
            Collections.sort (resultList, new MavenLikeVersioningComparator ());
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
    public VnfComponentsRecipe getVnfComponentsRecipeByVfModuleId (String vfModuleId,
                                                       String vnfComponentType,
                                                       String action) {                     

        long startTime = System.currentTimeMillis ();
        LOGGER.debug ("Catalog database - get Vnf Component recipe with vfModuleId " + vfModuleId
                                      + " and component type "
                                      + vnfComponentType
                                      + " and action "
                                      + action);

        try {
            String hql;
            hql = "FROM VnfComponentsRecipe WHERE vfModuleId = :vfModuleId AND vnfComponentType = :vnfComponentType AND action = :action ";
            
            Query query = getSession ().createQuery (hql);
            query.setParameter (VF_MODULE_ID, vfModuleId);
            query.setParameter (VNF_COMPONENT_TYPE, vnfComponentType);
            query.setParameter (ACTION, action);
            
            @SuppressWarnings("unchecked")
            List <VnfComponentsRecipe> resultList = query.list ();

            if (resultList.isEmpty ()) {
                return null;
            }
            Collections.sort (resultList, new MavenLikeVersioningComparator ());
            Collections.reverse (resultList);
            
            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVnfComponentsRecipeByVfModuleId", null);
        }
    }



    public void saveOrUpdateVnfComponent (VnfComponent vnfComponent) {
        long startTime = System.currentTimeMillis ();

        LOGGER.debug ("Catalog database - save VnfComponent where vnfId="+ vnfComponent.getVnfId()+ " AND componentType="+ vnfComponent.getComponentType());

        VnfComponent vnfComponentDb = this.getVnfComponent(vnfComponent.getVnfId(), vnfComponent.getComponentType());

        try {

            if (vnfComponentDb != null) {
                this.getSession ().merge (vnfComponent);
            } else {
                this.getSession ().save (vnfComponent);
            }

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
            Collections.sort (resultList, new MavenLikeVersioningComparator ());
            Collections.reverse (resultList);

            return resultList.get (0);
        } finally {
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "CatalogDB", "getVfModule", null);
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

        Query query = session.createSQLQuery (" show tables ");

        List<?> list = query.list();
        LOGGER.debug("healthCheck CatalogDB - Successful");
        return true;
    }
}
