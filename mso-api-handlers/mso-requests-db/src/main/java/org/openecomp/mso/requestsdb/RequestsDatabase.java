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

package org.openecomp.mso.requestsdb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.openecomp.mso.db.HibernateUtils;
import org.openecomp.mso.requestsdb.HibernateUtilsRequestsDb;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;

public class RequestsDatabase {

    protected static HibernateUtils hibernateUtils = new HibernateUtilsRequestsDb ();
    
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
    protected static final String         NETWORK_INSTANCE_ID        = "networkId";
    protected static final String         GLOBAL_SUBSCRIBER_ID       = "globalSubscriberId";
    protected static final String         SERVICE_NAME_VERSION_ID    = "serviceNameVersionId";
    protected static final String         SERVICE_ID                 = "serviceId";
    protected static final String         SERVICE_VERSION            = "serviceVersion";

    protected static final String         REQUEST_ID                 = "requestId";
    protected static MockRequestsDatabase mockDB                     = null;

    /**
     * Avoids creating an instance of this utility class.
     */
    protected RequestsDatabase () {
    }

    public static boolean healthCheck () {
        Session session = hibernateUtils.getSessionFactory ().openSession ();
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


    public static int updateInfraStatus (String requestId, String requestStatus, String lastModifiedBy) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Update infra request record " + requestId + " with status " + requestStatus);
        Session session = hibernateUtils.getSessionFactory ().openSession ();

        int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update InfraActiveRequests set requestStatus = :requestStatus, modifyTime = :modifyTime, lastModifiedBy = :lastModifiedBy where requestId = :requestId ");
            query.setParameter ("requestStatus", requestStatus);
            query.setParameter (REQUEST_ID, requestId);
            query.setParameter ("lastModifiedBy", lastModifiedBy);
            Calendar modifyTime = Calendar.getInstance ();
            Timestamp modifyTimeStamp = new Timestamp (modifyTime.getTimeInMillis ());
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

    public static int updateInfraStatus (String requestId, String requestStatus, long progress, String lastModifiedBy) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Update infra request record " + requestId + " with status " + requestStatus);
        Session session = hibernateUtils.getSessionFactory ().openSession ();

        int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update InfraActiveRequests set requestStatus = :requestStatus, modifyTime = :modifyTime, progress = :progress, lastModifiedBy = :lastModifiedBy where requestId = :requestId ");
            query.setParameter ("requestStatus", requestStatus);
            query.setParameter (REQUEST_ID, requestId);
            query.setParameter ("progress", progress);
            query.setParameter ("lastModifiedBy", lastModifiedBy);
            Calendar modifyTime = Calendar.getInstance ();
            Timestamp modifyTimeStamp = new Timestamp (modifyTime.getTimeInMillis ());
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

    public static int updateInfraFinalStatus (String requestId, String requestStatus, String statusMessage, long progress, String responseBody, String lastModifiedBy) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Update infra request record " + requestId + " with status " + requestStatus);
        Session session = hibernateUtils.getSessionFactory ().openSession ();

        int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update InfraActiveRequests set requestStatus = :requestStatus, statusMessage = :statusMessage, progress = :progress, endTime = :endTime, responseBody = :responseBody, lastModifiedBy = :lastModifiedBy where id.requestId = :requestId ");
            query.setParameter ("requestStatus", requestStatus);
            query.setParameter ("requestId", requestId);
            Calendar endTime = Calendar.getInstance ();
            Timestamp endTimeStamp = new Timestamp (endTime.getTimeInMillis ());
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

    
    private static List<InfraActiveRequests> executeInfraQuery (List <Criterion> criteria, Order order) {

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Execute query on infra active request table");
        
        List <InfraActiveRequests> results = new ArrayList<InfraActiveRequests>();

        Session session = hibernateUtils.getSessionFactory ().openSession ();
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
    
    public static InfraActiveRequests getRequestFromInfraActive (String requestId) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get request " + requestId + " from InfraActiveRequests DB");

        Session session = hibernateUtils.getSessionFactory ().openSession ();
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
    
    public static InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {

        List <Criterion> criteria = new LinkedList <> ();
       
        if(instanceName != null && !instanceName.equals("")) {
        	
        	if(requestScope.equals("service")){
        		criteria.add (Restrictions.eq (SERVICE_INSTANCE_NAME, instanceName));
        	} else if(requestScope.equals("vnf")){
        		criteria.add (Restrictions.eq (VNF_INSTANCE_NAME, instanceName));
        	} else if(requestScope.equals("volumeGroup")){
        		criteria.add (Restrictions.eq (VOLUME_GROUP_INSTANCE_NAME, instanceName));
        	} else if(requestScope.equals("vfModule")){
        		criteria.add (Restrictions.eq (VFMODULE_INSTANCE_NAME, instanceName));
        	} else if(requestScope.equals("network")){
        		criteria.add (Restrictions.eq (NETWORK_INSTANCE_NAME, instanceName));
        	}
        
        } else {
            if(instanceIdMap != null){
            	if(requestScope.equals("service") && instanceIdMap.get("serviceInstanceId") != null){
            		criteria.add (Restrictions.eq (SERVICE_INSTANCE_ID, instanceIdMap.get("serviceInstanceId")));
             	}
            
            	if(requestScope.equals("vnf") && instanceIdMap.get("vnfInstanceId") != null){
            		criteria.add (Restrictions.eq (VNF_INSTANCE_ID, instanceIdMap.get("vnfInstanceId")));
             	}
            
            	if(requestScope.equals("vfModule") && instanceIdMap.get("vfModuleInstanceId") != null){
            		criteria.add (Restrictions.eq (VFMODULE_INSTANCE_ID, instanceIdMap.get("vfModuleInstanceId")));
             	}
            
            	if(requestScope.equals("volumeGroup") && instanceIdMap.get("volumeGroupInstanceId") != null){
            		criteria.add (Restrictions.eq (VOLUME_GROUP_INSTANCE_ID, instanceIdMap.get("volumeGroupInstanceId")));
             	}
            
            	if(requestScope.equals("network") && instanceIdMap.get("networkInstanceId") != null){
            		criteria.add (Restrictions.eq (NETWORK_INSTANCE_ID, instanceIdMap.get("networkInstanceId")));
            	}
            }
        }
        
        criteria.add (Restrictions.in ("requestStatus", new String[] { "PENDING", "IN_PROGRESS", "TIMEOUT" }));
        
        Order order = Order.desc (START_TIME);
        
        List<InfraActiveRequests> dupList = executeInfraQuery(criteria, order);
        
        InfraActiveRequests infraActiveRequests = null;
        
        if(dupList != null && dupList.size() > 0){
        	infraActiveRequests = dupList.get(0);
        }
         	
        return infraActiveRequests; 
    }
      
    public static List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive (Map<String, List<String>> orchestrationMap) {
        
    	
    	List <Criterion> criteria = new LinkedList <> ();
    	for (Map.Entry<String, List<String>> entry : orchestrationMap.entrySet())
    	{
    		String mapKey = entry.getKey();

    	    if(mapKey.equalsIgnoreCase("vnfInstanceId")){
    	    	mapKey = "vnfId";
     	    } else if(mapKey.equalsIgnoreCase("vnfInstanceName")) {
    	    	mapKey = "vnfName";
    	    } else if(mapKey.equalsIgnoreCase("vfModuleInstanceId")) {
    	    	mapKey = "vfModuleId";
    	    } else if(mapKey.equalsIgnoreCase("vfModuleInstanceName")) {
    	    	mapKey = "vfModuleName";
    	    } else if(mapKey.equalsIgnoreCase("volumeGroupInstanceId")) {
    	    	mapKey = "volumeGroupId";
    	    } else if(mapKey.equalsIgnoreCase("volumeGroupInstanceName")) {
    	    	mapKey = "volumeGroupName";
    	    } else if(mapKey.equalsIgnoreCase("networkInstanceId")) {
    	    	mapKey = "networkId";
    	    } else if(mapKey.equalsIgnoreCase("networkInstanceName")) {
    	    	mapKey = "networkName";
    	    } else if(mapKey.equalsIgnoreCase("lcpCloudRegionId")) {
    	    	mapKey = "aicCloudRegion";
    	    } 
    	    
    	    criteria.add(Restrictions.eq(mapKey, entry.getValue().get(1)));  	    
    	    
    	}
    	
         Order order = Order.asc (START_TIME);

        return executeInfraQuery (criteria, order);
    }


    public static List <InfraActiveRequests> getRequestListFromInfraActive (String queryAttributeName,
                                                                            String queryValue,
                                                                            String requestType) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get list of infra requests from DB with " + queryAttributeName + " = " + queryValue);

        Session session = hibernateUtils.getSessionFactory ().openSession ();
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


    public static InfraActiveRequests getRequestFromInfraActive (String requestId, String requestType) {
        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get infra request from DB with id " + requestId);

        Session session = hibernateUtils.getSessionFactory ().openSession ();
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


    public static InfraActiveRequests checkDuplicateByVnfName (String vnfName, String action, String requestType) {

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get infra request from DB for VNF " + vnfName + " and action " + action + " and requestType " + requestType);

        InfraActiveRequests ar = null;
        Session session = hibernateUtils.getSessionFactory ().openSession ();

        try {
            session.beginTransaction ();
            Query query = session.createQuery ("from InfraActiveRequests where vnfName = :vnfName and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT') and requestType = :requestType ORDER BY startTime DESC");
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

    public static InfraActiveRequests checkDuplicateByVnfId (String vnfId, String action, String requestType) {

        long startTime = System.currentTimeMillis ();
        msoLogger.debug ("Get list of infra requests from DB for VNF " + vnfId + " and action " + action);

        InfraActiveRequests ar = null;
        Session session = hibernateUtils.getSessionFactory ().openSession ();

        try {
            session.beginTransaction ();
            Query query = session.createQuery ("from InfraActiveRequests where vnfId = :vnfId and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT') and requestType = :requestType ORDER BY startTime DESC");
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

    public static void setMockDB(MockRequestsDatabase mockDB) {
        RequestsDatabase.mockDB = mockDB;
    }

    /**
     * Fetch a specific SiteStatus by SiteName.
     *
     * @param siteName The unique name of the site
     * @return SiteStatus object or null if none found
     */
    public static SiteStatus getSiteStatus (String siteName) {
        Session session = hibernateUtils.getSessionFactory ().openSession ();

        long startTime = System.currentTimeMillis ();
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
    public static void updateSiteStatus (String siteName, boolean status) {
        Session session = hibernateUtils.getSessionFactory ().openSession ();
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
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "updateSiteStatus", null);
        }
    }

}
