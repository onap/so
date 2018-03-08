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

package org.openecomp.mso.requestsdb;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.requestsdb.RequestsDbConstant.Status;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.logger.MsoLogger;

public class RequestsDatabase {

    protected final AbstractSessionFactoryManager sessionFactoryRequestDB;
    
    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);
    
    protected static final String         SOURCE                     = "source";
    protected static final String         START_TIME                 = "startTime";
    protected static final String         REQUEST_TYPE               = "requestType";
    protected static final String         SERVICE_INSTANCE_ID        = "serviceInstanceId";
    protected static final String         SERVICE_INSTANCE_NAME      = "serviceInstanceName";
    protected static final String         VNF_INSTANCE_NAME          = "vnfName";
    protected static final String         VNF_INSTANCE_ID            = "vnfId";
    protected static final String         VOLUME_GROUP_INSTANCE_NAME = "volumeGroupName";
    protected static final String         VOLUME_GROUP_INSTANCE_ID   = "volumeGroupId";
    protected static final String         VFMODULE_INSTANCE_NAME     = "vfModuleName";
    protected static final String         VFMODULE_INSTANCE_ID       = "vfModuleId";
    protected static final String         NETWORK_INSTANCE_NAME      = "networkName";
    protected static final String		  CONFIGURATION_INSTANCE_ID  = "configurationId";
    protected static final String		  CONFIGURATION_INSTANCE_NAME= "configurationName";
    protected static final String		  OPERATIONAL_ENV_ID  		 = "operationalEnvId";
    protected static final String		  OPERATIONAL_ENV_NAME		 = "operationalEnvName";
    protected static final String         NETWORK_INSTANCE_ID        = "networkId";
    protected static final String         GLOBAL_SUBSCRIBER_ID       = "globalSubscriberId";
    protected static final String         SERVICE_NAME_VERSION_ID    = "serviceNameVersionId";
    protected static final String         SERVICE_ID                 = "serviceId";
    protected static final String         SERVICE_VERSION            = "serviceVersion";
    protected static final String         REQUEST_ID                 = "requestId";
    protected static final String         REQUESTOR_ID               = "requestorId";
    
    protected static MockRequestsDatabase mockDB                     = null;

    public static RequestsDatabase getInstance() {
        return new RequestsDatabase(new RequestsDbSessionFactoryManager ());
    }

    protected RequestsDatabase (AbstractSessionFactoryManager sessionFactoryRequest) {
        sessionFactoryRequestDB = sessionFactoryRequest;
    }

    public boolean healthCheck () {
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
        try {
            Query query = session.createSQLQuery (" show tables ");

            List<?> list = query.list();

        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
        }
        return true;
    }


    public int updateInfraStatus (String requestId, String requestStatus, String lastModifiedBy) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Update infra request record " + requestId + " with status " + requestStatus);
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

        int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update InfraActiveRequests set requestStatus = :requestStatus, modifyTime = :modifyTime, lastModifiedBy = :lastModifiedBy where requestId = :requestId ");
            query.setParameter ("requestStatus", requestStatus);
            query.setParameter (REQUEST_ID, requestId);
            query.setParameter ("lastModifiedBy", lastModifiedBy);
            Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
            query.setParameter ("modifyTime", modifyTimeStamp);
            result = query.executeUpdate ();
            session.getTransaction ().commit ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "updateInfraStatus", null);
        }
        return result;
    }

    public int updateInfraStatus (String requestId, String requestStatus, long progress, String lastModifiedBy) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Update infra request record " + requestId + " with status " + requestStatus);
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

        int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update InfraActiveRequests set requestStatus = :requestStatus, modifyTime = :modifyTime, progress = :progress, lastModifiedBy = :lastModifiedBy where requestId = :requestId ");
            query.setParameter ("requestStatus", requestStatus);
            query.setParameter (REQUEST_ID, requestId);
            query.setParameter ("progress", progress);
            query.setParameter ("lastModifiedBy", lastModifiedBy);
            Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
            query.setParameter ("modifyTime", modifyTimeStamp);
            result = query.executeUpdate ();
            session.getTransaction ().commit ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "updateInfraStatus", null);
        }
        return result;
    }

    public int updateInfraFinalStatus (String requestId, String requestStatus, String statusMessage, long progress, String responseBody, String lastModifiedBy) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Update infra request record " + requestId + " with status " + requestStatus);
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

        int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update InfraActiveRequests set requestStatus = :requestStatus, statusMessage = :statusMessage, progress = :progress, endTime = :endTime, responseBody = :responseBody, lastModifiedBy = :lastModifiedBy where id.requestId = :requestId ");
            query.setParameter ("requestStatus", requestStatus);
            query.setParameter ("requestId", requestId);
            Timestamp endTimeStamp = new Timestamp (System.currentTimeMillis());
            query.setParameter ("endTime", endTimeStamp);
            query.setParameter ("statusMessage", statusMessage);
            query.setParameter ("progress", progress);
            query.setParameter ("responseBody", responseBody);
            query.setParameter ("lastModifiedBy", lastModifiedBy);
            result = query.executeUpdate ();
            session.getTransaction ().commit ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "updateInfraFinalStatus", null);
        }
        return result;
    }

    
    private List<InfraActiveRequests> executeInfraQuery (List <Criterion> criteria, Order order) {

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Execute query on infra active request table");
        
        List <InfraActiveRequests> results = new ArrayList<>();

        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
        try {
            session.beginTransaction ();
            Criteria crit = session.createCriteria (InfraActiveRequests.class);
            for (Criterion criterion : criteria) {
                crit.add (criterion);
            }
            crit.addOrder (order);

           // @SuppressWarnings("unchecked")
            results = crit.list ();

        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "getInfraActiveRequest", null);
        }
        return results;
    }
    
    public InfraActiveRequests getRequestFromInfraActive (String requestId) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get request " + requestId + " from InfraActiveRequests DB");

        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
        InfraActiveRequests ar = null;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("from InfraActiveRequests where requestId = :requestId OR clientRequestId = :requestId");
            query.setParameter (REQUEST_ID, requestId);
            ar = (InfraActiveRequests) query.uniqueResult ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "InfraRequestDB", "getRequestFromInfraActive", null);
        }
        return ar;
    }
    
    public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {

        List <Criterion> criteria = new LinkedList <> ();
       
        if(instanceName != null && !instanceName.equals("")) {
        	
        	if("service".equals(requestScope)){
        		criteria.add (Restrictions.eq (SERVICE_INSTANCE_NAME, instanceName));
        	} else if("vnf".equals(requestScope)){
        		criteria.add (Restrictions.eq (VNF_INSTANCE_NAME, instanceName));
        	} else if("volumeGroup".equals(requestScope)){
        		criteria.add (Restrictions.eq (VOLUME_GROUP_INSTANCE_NAME, instanceName));
        	} else if("vfModule".equals(requestScope)){
        		criteria.add (Restrictions.eq (VFMODULE_INSTANCE_NAME, instanceName));
        	} else if("network".equals(requestScope)){
        		criteria.add (Restrictions.eq (NETWORK_INSTANCE_NAME, instanceName));
        	} else if(requestScope.equals("configuration")) {
        		criteria.add (Restrictions.eq (CONFIGURATION_INSTANCE_NAME, instanceName));
        	} else if(requestScope.equals("operationalEnvironment")) {
        		criteria.add (Restrictions.eq (OPERATIONAL_ENV_NAME, instanceName));
        	}
        
        } else {
            if(instanceIdMap != null){
            	if("service".equals(requestScope) && instanceIdMap.get("serviceInstanceId") != null){
            		criteria.add (Restrictions.eq (SERVICE_INSTANCE_ID, instanceIdMap.get("serviceInstanceId")));
             	}
            
            	if("vnf".equals(requestScope) && instanceIdMap.get("vnfInstanceId") != null){
            		criteria.add (Restrictions.eq (VNF_INSTANCE_ID, instanceIdMap.get("vnfInstanceId")));
             	}
            
            	if("vfModule".equals(requestScope) && instanceIdMap.get("vfModuleInstanceId") != null){
            		criteria.add (Restrictions.eq (VFMODULE_INSTANCE_ID, instanceIdMap.get("vfModuleInstanceId")));
             	}
            
            	if("volumeGroup".equals(requestScope) && instanceIdMap.get("volumeGroupInstanceId") != null){
            		criteria.add (Restrictions.eq (VOLUME_GROUP_INSTANCE_ID, instanceIdMap.get("volumeGroupInstanceId")));
             	}
            
            	if("network".equals(requestScope) && instanceIdMap.get("networkInstanceId") != null){
            		criteria.add (Restrictions.eq (NETWORK_INSTANCE_ID, instanceIdMap.get("networkInstanceId")));
            	}
            	
            	if(requestScope.equals("configuration") && instanceIdMap.get("configurationInstanceId") != null){
            		criteria.add (Restrictions.eq (CONFIGURATION_INSTANCE_ID, instanceIdMap.get("configurationInstanceId")));
            	}
            	
            	if(requestScope.equals("operationalEnvironment") && instanceIdMap.get("operationalEnvironmentId") != null) {
            		criteria.add (Restrictions.eq (OPERATIONAL_ENV_ID, instanceIdMap.get("operationalEnvironmentId")));
            	}
            }
        }
        
        criteria.add (Restrictions.in ("requestStatus", new String[] { "PENDING", "IN_PROGRESS", "TIMEOUT", "PENDING_MANUAL_TASK" }));
        
        Order order = Order.desc (START_TIME);
        
        List<InfraActiveRequests> dupList = executeInfraQuery(criteria, order);
        
        InfraActiveRequests infraActiveRequests = null;
        
        if(dupList != null && !dupList.isEmpty()){
        	infraActiveRequests = dupList.get(0);
        }
         	
        return infraActiveRequests; 
    }
      
    public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive (Map<String, List<String>> orchestrationMap) {
        
    	
    	List <Criterion> criteria = new LinkedList <> ();
    	for (Map.Entry<String, List<String>> entry : orchestrationMap.entrySet())
    	{
    		String mapKey = entry.getKey();
    		if("serviceInstanceId".equalsIgnoreCase(mapKey)) {
    			mapKey = "serviceInstanceId";
    		} else if("serviceInstanceName".equalsIgnoreCase(mapKey)) {
    			mapKey = "serviceInstanceName";
    		} else if("vnfInstanceId".equalsIgnoreCase(mapKey)){
    	    	mapKey = "vnfId";
     	    } else if("vnfInstanceName".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "vnfName";
    	    } else if("vfModuleInstanceId".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "vfModuleId";
    	    } else if("vfModuleInstanceName".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "vfModuleName";
    	    } else if("volumeGroupInstanceId".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "volumeGroupId";
    	    } else if("volumeGroupInstanceName".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "volumeGroupName";
    	    } else if("networkInstanceId".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "networkId";
    	    } else if("networkInstanceName".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "networkName";
    	    } else if(mapKey.equalsIgnoreCase("configurationInstanceId")) {    	    	
    	    	mapKey = "configurationId";
    	    } else if(mapKey.equalsIgnoreCase("configurationInstanceName")) {    	    	
    	    	mapKey = "configurationName";
    	    } else if("lcpCloudRegionId".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "aicCloudRegion";
    	    } else if("tenantId".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "tenantId";
    	    } else if("modelType".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "requestScope";
    	    } else if("requestorId".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "requestorId";
    	    } else if("requestExecutionDate".equalsIgnoreCase(mapKey)) {
    	    	mapKey = "startTime";
    	    }
    	    
    		String propertyValue = entry.getValue().get(1);
    		if ("startTime".equals(mapKey)) {
    			SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");    	        
    			try {
	    	        Date thisDate = format.parse(propertyValue);
	    	        Timestamp minTime = new Timestamp(thisDate.getTime());	    	        
	    	        Timestamp maxTime = new Timestamp(thisDate.getTime() + TimeUnit.DAYS.toMillis(1));
	    	        
	    	        if("DOES_NOT_EQUAL".equalsIgnoreCase(entry.getValue().get(0))) {
	    	        	criteria.add(Restrictions.or(Restrictions.lt(mapKey, minTime),
	    	        			Restrictions.ge(mapKey, maxTime)));	        			
	        		} else {	        			
	        			criteria.add(Restrictions.between(mapKey, minTime, maxTime));        		       			
	        		}    
    			}
    			catch (Exception e){
    				msoLogger.debug("Exception in getOrchestrationFiltersFromInfraActive(): + " + e.getMessage(), e);
    				return null;
    			}
    		}
    		else if("DOES_NOT_EQUAL".equalsIgnoreCase(entry.getValue().get(0))) {
    			criteria.add(Restrictions.ne(mapKey, propertyValue));
    		} else {
    			criteria.add(Restrictions.eq(mapKey, propertyValue));
    		}
    	    
    	}
    	
         Order order = Order.asc (START_TIME);

        return executeInfraQuery (criteria, order);
    }

    // Added this method for Tenant Isolation project ( 1802-295491a) to query the mso_requests DB 
    // (infra_active_requests table) for operationalEnvId and OperationalEnvName
    public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive (Map<String, String> orchestrationMap) {
    	List <Criterion> criteria = new LinkedList <> ();
    	
    	// Add criteria on OperationalEnvironment RequestScope when requestorId is only specified in the filter
    	// as the same requestorId can also match on different API methods
    	String resourceType = orchestrationMap.get("resourceType");
    	if(resourceType == null) {
    		criteria.add(Restrictions.eq("requestScope", "operationalEnvironment"));
    	}
    	
    	for (Map.Entry<String, String> entry : orchestrationMap.entrySet()) {
    		String mapKey = entry.getKey();
    		if(mapKey.equalsIgnoreCase("requestorId")) {
    	    	mapKey = "requestorId";
    	    } else if(mapKey.equalsIgnoreCase("requestExecutionDate")) {    	    	
    	    	mapKey = "startTime";
    	    } else if(mapKey.equalsIgnoreCase("operationalEnvironmentId")) {    	    	
    	    	mapKey = "operationalEnvId";
    	    } else if(mapKey.equalsIgnoreCase("operationalEnvironmentName")) {    	    	
    	    	mapKey = "operationalEnvName";
    	    } else if(mapKey.equalsIgnoreCase("resourceType")) {    	    	
    	    	mapKey = "requestScope";
    	    }
    	    
    		String propertyValue = entry.getValue();
    		if (mapKey.equals("startTime")) {    			
    			SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");    	        
    			try {
	    	        Date thisDate = format.parse(propertyValue);
	    	        Timestamp minTime = new Timestamp(thisDate.getTime());	    	        
	    	        Timestamp maxTime = new Timestamp(thisDate.getTime() + TimeUnit.DAYS.toMillis(1));
	    	        
	        		criteria.add(Restrictions.between(mapKey, minTime, maxTime));        		       			
    			}
    			catch (Exception e){
    				msoLogger.debug("Exception in getCloudOrchestrationFiltersFromInfraActive(): + " + e.getMessage());
    				return null;
    			}
    		} else {
    			criteria.add(Restrictions.eq(mapKey, propertyValue));
    		}
    	}
    	
         Order order = Order.asc (START_TIME);
         return executeInfraQuery (criteria, order);
    }

    public List <InfraActiveRequests> getRequestListFromInfraActive (String queryAttributeName,
                                                                            String queryValue,
                                                                            String requestType) {
        msoLogger.debug ("Get list of infra requests from DB with " + queryAttributeName + " = " + queryValue);

        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
        try {
            session.beginTransaction ();
            Criteria crit = session.createCriteria (InfraActiveRequests.class)
                    .add (Restrictions.eq (queryAttributeName, queryValue));
            crit.add (Restrictions.eqOrIsNull (REQUEST_TYPE, requestType));
            crit.addOrder (Order.desc (START_TIME));
            crit.addOrder (Order.asc (SOURCE));

            @SuppressWarnings("unchecked")
            List <InfraActiveRequests> arList = crit.list ();
            if (arList != null && !arList.isEmpty ()) {
                return arList;
            }
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
           // msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "getRequestListFromInfraActive", null);
        }
        return null;
    }


    public InfraActiveRequests getRequestFromInfraActive (String requestId, String requestType) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get infra request from DB with id " + requestId);

        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
        InfraActiveRequests ar = null;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("from InfraActiveRequests where (requestId = :requestId OR clientRequestId = :requestId) and requestType = :requestType");
            query.setParameter (REQUEST_ID, requestId);
            query.setParameter (REQUEST_TYPE, requestType);
            ar = (InfraActiveRequests) query.uniqueResult ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "getRequestFromInfraActive", null);
        }
        return ar;
    }


    public InfraActiveRequests checkDuplicateByVnfName (String vnfName, String action, String requestType) {

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get infra request from DB for VNF " + vnfName + " and action " + action + " and requestType " + requestType);

        InfraActiveRequests ar = null;
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

        try {
            session.beginTransaction ();
            Query query = session.createQuery ("from InfraActiveRequests where vnfName = :vnfName and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT' or requestStatus = 'PENDING_MANUAL_TASK') and requestType = :requestType ORDER BY startTime DESC");
            query.setParameter ("vnfName", vnfName);
            query.setParameter ("action", action);
            query.setParameter (REQUEST_TYPE, requestType);
            @SuppressWarnings("unchecked")
            List <InfraActiveRequests> results = query.list ();
            if (!results.isEmpty ()) {
                ar = results.get (0);
            }
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "checkDuplicateByVnfName", null);
        }

        return ar;
    }

    public InfraActiveRequests checkDuplicateByVnfId (String vnfId, String action, String requestType) {

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get list of infra requests from DB for VNF " + vnfId + " and action " + action);

        InfraActiveRequests ar = null;
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

        try {
            session.beginTransaction ();
            Query query = session.createQuery ("from InfraActiveRequests where vnfId = :vnfId and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT' or requestStatus = 'PENDING_MANUAL_TASK') and requestType = :requestType ORDER BY startTime DESC");
            query.setParameter ("vnfId", vnfId);
            query.setParameter ("action", action);
            query.setParameter (REQUEST_TYPE, requestType);
            @SuppressWarnings("unchecked")
            List <InfraActiveRequests> results = query.list ();
            if (!results.isEmpty ()) {
                ar = results.get (0);
            }
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "checkDuplicateByVnfId", null);
        }

        return ar;
    }

    public void setMockDB(MockRequestsDatabase mockDB) {
        RequestsDatabase.mockDB = mockDB;
    }

    /**
     * Fetch a specific SiteStatus by SiteName.
     *
     * @param siteName The unique name of the site
     * @return SiteStatus object or null if none found
     */
    public SiteStatus getSiteStatus (String siteName) {
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

        SiteStatus siteStatus = null;
        msoLogger.debug ("Request database - get Site Status with Site name:" + siteName);
        try {
            String hql = "FROM SiteStatus WHERE siteName = :site_name";
            Query query = session.createQuery (hql);
            query.setParameter ("site_name", siteName);

            siteStatus = (SiteStatus) query.uniqueResult ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.debug ("getSiteStatus - Successfully: " + siteStatus);
        }
        return siteStatus;
    }

    /**
     * Fetch a specific SiteStatus by SiteName.
     *
     * @param siteName The unique name of the site
     * @param status The updated status of the Site
     */
    public void updateSiteStatus (String siteName, boolean status) {
        Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
        session.beginTransaction ();

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Request database - save Site Status with Site name:" + siteName);
        try {
            String hql = "FROM SiteStatus WHERE siteName = :site_name";
            Query query = session.createQuery (hql);
            query.setParameter ("site_name", siteName);

            SiteStatus siteStatus = (SiteStatus) query.uniqueResult ();
            if (siteStatus == null) {
                siteStatus = new SiteStatus ();
                siteStatus.setSiteName (siteName);
                siteStatus.setStatus (status);
                //siteStatus.setCreated(new Timestamp(new Date().getTime()));
                session.save (siteStatus);
            } else {
                siteStatus.setStatus(status);
                //siteStatus.setCreated(new Timestamp(new Date().getTime()));
                session.merge (siteStatus);
            }
            session.getTransaction ().commit ();
        } finally {
            session.close ();
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "updateSiteStatus", null);
        }
    }

    /**
     * get the operation progress
     * <br>
     * 
     * @param serviceId the serviceId
     * @param operationId the operation id 
     * @return current progress of the operation
     * @since ONAP Amsterdam Release
     */
    public OperationStatus getOperationStatus(String serviceId, String operationId) {

        long startTime = System.currentTimeMillis();
        msoLogger.debug("Execute query on infra active request table");

        OperationStatus operStatus = null;
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            String hql = "FROM OperationStatus WHERE SERVICE_ID = :service_id and OPERATION_ID = :operation_id";
            Query query = session.createQuery(hql);
            query.setParameter("service_id", serviceId);
            query.setParameter("operation_id", operationId);
            operStatus = (OperationStatus)query.uniqueResult();

        } finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "getOperationStatus", null);
        }
        return operStatus;
    }
    
    /**
     * get the operation progress
     * <br>
     * 
     * @param serviceId the serviceId
     * @return current progress of the operation
     * @since ONAP Amsterdam Release
     */
    public OperationStatus getOperationStatusByServiceId(String serviceId) {

        long startTime = System.currentTimeMillis();
        msoLogger.debug("Execute query on infra active request table");

        OperationStatus operStatus = null;
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            String hql = "FROM OperationStatus WHERE SERVICE_ID = :service_id";
            Query query = session.createQuery(hql);
            query.setParameter("service_id", serviceId);
            operStatus = (OperationStatus)query.uniqueResult();

        } finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "getOperationStatus", null);
        }
        return operStatus;
    }
    
    /**
     * get the operation progress
     * <br>
     * 
     * @param serviceName the serviceName
     * @return current progress of the operation
     * @since ONAP Amsterdam Release
     */
    public OperationStatus getOperationStatusByServiceName(String serviceName) {

        long startTime = System.currentTimeMillis();
        msoLogger.debug("Execute query on infra active request table");

        OperationStatus operStatus = null;
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            String hql = "FROM OperationStatus WHERE SERVICE_NAME = :service_name";
            Query query = session.createQuery(hql);
            query.setParameter("service_name", serviceName);
            operStatus = (OperationStatus)query.uniqueResult();

        } finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "getOperationStatus", null);
        }
        return operStatus;
    }
    
    /**
     * update the operation status
     * <br>
     * 
     * @param operstatus the operation object
     * @since ONAP Amsterdam Release
     */
    public void updateOperationStatus(OperationStatus operstatus) {
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        session.beginTransaction();

        long startTime = System.currentTimeMillis();
        msoLogger.debug("Request database - save Operation Status with service Id:" + operstatus.getServiceId()
                + ", operationId:" + operstatus.getOperationId());
        try {
            String hql =
                    "FROM OperationStatus WHERE SERVICE_ID = :service_id and OPERATION_ID = :operation_id";
            Query query = session.createQuery(hql);
            query.setParameter("service_id", operstatus.getServiceId());
            query.setParameter("operation_id", operstatus.getOperationId());
            OperationStatus exsitingStatus = (OperationStatus)query.uniqueResult();
            if(exsitingStatus == null) {
                session.save(operstatus);
            } else {
                session.merge(operstatus);
            }
            session.getTransaction().commit();
        } finally {
            session.close();
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "updateOperationStatus", null);
        }
    }

    /**
     * get a operation status of a resource
     * <br>
     * 
     * @param serviceId the service Id
     * @param operationId the operation id
     * @param resourceTemplateUUID the resource template uuid
     * @return the progress status of a resource
     * @since ONAP Amsterdam Release
     */
    public ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId,
            String resourceTemplateUUID) {
        long startTime = System.currentTimeMillis();
        msoLogger.debug("Execute query on infra active request table");

        ResourceOperationStatus operStatus = null;
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            String hql =
                    "FROM ResourceOperationStatus WHERE serviceId = :service_id and operationId = :operation_id and resourceTemplateUUID= :uuid";
            Query query = session.createQuery(hql);
            query.setParameter("service_id", serviceId);
            query.setParameter("operation_id", operationId);
            query.setParameter("uuid", resourceTemplateUUID);
            operStatus = (ResourceOperationStatus)query.uniqueResult();

        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "getOperationStatus", null);
        }
        return operStatus;
    }

    /**
     * update the resource operation
     * <br>
     * 
     * @param operStatus the resource operation object
     * @since ONAP Amsterdam Release
     */
    public void updateResOperStatus(ResourceOperationStatus operStatus) {
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        session.beginTransaction();

        long startTime = System.currentTimeMillis();
        msoLogger.debug("Request database - save Resource Operation Status with service Id:" + operStatus.getServiceId()
                + ", operationId:" + operStatus.getOperationId() + ", resourceUUId:"
                + operStatus.getResourceTemplateUUID());
        try {
            String hql =
                    "FROM ResourceOperationStatus WHERE SERVICE_ID = :service_id and OPERATION_ID = :operation_id and RESOURCE_TEMPLATE_UUID = :res_uuid";
            Query query = session.createQuery(hql);
            query.setParameter("service_id", operStatus.getServiceId());
            query.setParameter("operation_id", operStatus.getOperationId());
            query.setParameter("res_uuid", operStatus.getResourceTemplateUUID());
            ResourceOperationStatus exsitingStatus = (ResourceOperationStatus)query.uniqueResult();
            if(exsitingStatus == null) {
                session.save(operStatus);
            } else {
                session.merge(operStatus);
            }
            session.getTransaction().commit();
        } finally {
            session.close();
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "updateResOperStatus", null);
        }
        updateOperationStatusBasedOnResourceStatus(operStatus);
    }

    /**
     * update service operation status when a operation resource status updated
     * <br>
     * 
     * @param operStatus the resource operation status
     * @since ONAP Amsterdam Release
     */
    private void updateOperationStatusBasedOnResourceStatus(ResourceOperationStatus operStatus) {
        Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
        session.beginTransaction();

        long startTime = System.currentTimeMillis();
        msoLogger.debug("Request database - query Resource Operation Status with service Id:"
                + operStatus.getServiceId() + ", operationId:" + operStatus.getOperationId());
        try {
            // query all resources of the service
            String hql = "FROM ResourceOperationStatus WHERE SERVICE_ID = :service_id and OPERATION_ID = :operation_id";
            Query query = session.createQuery(hql);
            query.setParameter("service_id", operStatus.getServiceId());
            query.setParameter("operation_id", operStatus.getOperationId());
            @SuppressWarnings("unchecked")
            List<ResourceOperationStatus> lstResourceStatus = (List<ResourceOperationStatus>)query.list();
            // count the total progress
            int resourceCount = lstResourceStatus.size();
            int progress = 0;
            boolean isFinished = true;
            for (ResourceOperationStatus lstResourceStatu : lstResourceStatus) {
                progress = progress + Integer.valueOf(lstResourceStatu.getProgress()) / resourceCount;
                if (Status.PROCESSING.equals(lstResourceStatu.getStatus())) {
                    isFinished = false;
                }
            }
            OperationStatus serviceOperStatus =
                    getOperationStatus(operStatus.getServiceId(), operStatus.getOperationId());
            progress = progress > 100 ? 100 : progress;
            serviceOperStatus.setProgress(String.valueOf(progress));
            serviceOperStatus.setOperationContent(operStatus.getStatusDescription());
            // if current resource failed. service failed.
            if(RequestsDbConstant.Status.ERROR.equals(operStatus.getStatus())) {
                serviceOperStatus.setResult(RequestsDbConstant.Status.ERROR);
                serviceOperStatus.setReason(operStatus.getStatusDescription());
            } else if(isFinished) {
                // if finished
                serviceOperStatus.setResult(RequestsDbConstant.Status.FINISHED);
                serviceOperStatus.setProgress(RequestsDbConstant.Progress.ONE_HUNDRED);
            }
            updateOperationStatus(serviceOperStatus);
        } finally {
            session.close();
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "updateResOperStatus", null);
        }
    }

	public InfraActiveRequests checkVnfIdStatus(String operationalEnvironmentId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Get Infra request from DB for OperationalEnvironmentId " + operationalEnvironmentId);

		InfraActiveRequests ar = null;
		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();

		try {
			session.beginTransaction ();
			Query query = session.createQuery ("FROM InfraActiveRequests WHERE operationalEnvId = :operationalEnvId AND requestStatus != 'COMPLETE' AND action = 'create' ORDER BY startTime DESC");
			query.setParameter ("operationalEnvId", operationalEnvironmentId);
	            
			@SuppressWarnings("unchecked")
			List <InfraActiveRequests> results = query.list ();
			if (!results.isEmpty ()) {
				ar = results.get (0);
			}
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "checkDuplicateByVnfName", null);
		}

		return ar;
	}
}
