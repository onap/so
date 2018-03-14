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


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class WatchdogComponentDistributionStatusDb {

    protected final AbstractSessionFactoryManager sessionFactoryRequestDB;
    
    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    
    protected static final String         DISTRIBUTION_ID				= "distributionId";
    protected static final String         COMPONENT_NAME      			= "componentName";
    protected static final String         COMPONENT_DISTRIBUTION_STATUS = "componentDistributionIdStatus";
    protected static final String         CREATE_TIME         			= "startTime";
    protected static final String         MODIFY_TIME         			= "modifyTime";
    

    public static WatchdogComponentDistributionStatusDb getInstance() {
        return new WatchdogComponentDistributionStatusDb(new RequestsDbSessionFactoryManager ());
    }

    protected WatchdogComponentDistributionStatusDb (AbstractSessionFactoryManager sessionFactoryRequest) {
        sessionFactoryRequestDB = sessionFactoryRequest;
    }


    /**
     * Insert into watchdog_per_component_distribution_status.
     *
     * @param distributionId
     * @param componentName
     * @param componentDistributionStatus
     * @return void
     */
	public void insertWatchdogComponentDistributionStatus(String distributionId, String componentName, String componentDistributionStatus ) {
		long startTime = System.currentTimeMillis ();
		Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
		msoLogger.debug ("Insert into WatchdogPerComponentDistributionStatus for DistributionId: " + distributionId + " ComponentName: " + componentName + " and ComponentDistributionStatus: " + componentDistributionStatus);
		
		List<WatchdogComponentDistributionStatus> componentList = getWatchdogComponentDistributionStatus(distributionId, componentName);
		
		if((componentList == null) || componentList.isEmpty())
		{

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		WatchdogComponentDistributionStatus wdcds = new WatchdogComponentDistributionStatus ();
	
		try {
			session.beginTransaction ();
		
			wdcds.setDistributionId (distributionId);
			wdcds.setComponentName (componentName);
			wdcds.setComponentDistributionStatus (componentDistributionStatus);
			wdcds.setCreateTime (startTimeStamp);
			Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
			wdcds.setModifyTime (modifyTimeStamp);
		
			msoLogger.debug ("About to insert a record into WatchdogPerComponentDistributionStatus");
		
			session.save (wdcds);
			session.getTransaction ().commit ();
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in insertWatchdogComponentDistributionStatus", e);
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "WatchdogComponentDistributionStatusDB", "saveRequest", null);
			if (session != null) {
				session.close ();
			}
			// throw an Exception in the event of a DB insert failure so that the calling routine can exit
			throw e;
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
		}
		msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "WatchdogComponentDistributionStatusDB", "insertWatchdogComponentDistributionStatus", null);
	}
	}
		
  }
	
    /**
     * Retrieve records from WatchdogComponentDistributionStatus.
     *
     * @param distributionId
     * @return WatchdogComponentDistributionStatus
     */
	 @SuppressWarnings("unchecked")
	 public List<WatchdogComponentDistributionStatus> getWatchdogComponentDistributionStatus(String distributionId) {
		 Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
		 session.beginTransaction();

		 List<WatchdogComponentDistributionStatus> results = new ArrayList<WatchdogComponentDistributionStatus>();
		 msoLogger.debug("Request database - getWatchdogComponentDistributionStatus:" + distributionId);
		 try {
			 String hql = "FROM WatchdogComponentDistributionStatus WHERE distributionId = :distributionId";
			 Query query = session.createQuery(hql);
			 query.setParameter("distributionId", distributionId);
			 results = query.list();
		 } finally {
			 if (session != null && session.isOpen ()) {
				 session.close ();
			 }
			 msoLogger.debug ("getWatchdogComponentDistributionStatus - Successfully");
		 }
		 return results;
	 }
	 
	    /**
	     * Retrieve records from WatchdogComponentDistributionStatus.
	     *
	     * @param distributionId
	     * @param componentName
	     * @return WatchdogComponentDistributionStatus
	     */
		 @SuppressWarnings("unchecked")
		 public List<WatchdogComponentDistributionStatus> getWatchdogComponentDistributionStatus(String distributionId, String componentName) {
			 Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
			 session.beginTransaction();

			 List<WatchdogComponentDistributionStatus> results = new ArrayList<WatchdogComponentDistributionStatus>();
			 msoLogger.debug("Request database - getWatchdogComponentDistributionStatus:" + distributionId + " and componentName:" + componentName);
			 try {
				 String hql = "FROM WatchdogComponentDistributionStatus WHERE distributionId = :distributionId AND componentName = :componentName";
				 Query query = session.createQuery(hql);
				 query.setParameter("distributionId", distributionId);
				 query.setParameter("componentName", componentName);
				 results = query.list();
			 } finally {
				 if (session != null && session.isOpen ()) {
					 session.close ();
				 }
				 msoLogger.debug ("getWatchdogComponentDistributionStatus by ComponentName - Successfully");
			 }
			 return results;
		 }
		 
		   /**
		     * Retrieve records from getWatchdogComponentNames.
		     *
		     * @param distributionId
		     * @return String
		     */
		 	@SuppressWarnings("unchecked")
		 	public List<String> getWatchdogComponentNames(String distributionId) {
				 Session session = sessionFactoryRequestDB.getSessionFactory().openSession();
				 session.beginTransaction();

				 List<String> results = new ArrayList<String>();
				 msoLogger.debug("Request database - getWatchdogComponentNames:" + distributionId);
				 try {
					 String hql = "Select componentName FROM WatchdogComponentDistributionStatus WHERE distributionId = :distributionId";
					 Query query = session.createQuery(hql);
					 query.setParameter("distributionId", distributionId);
					 results = query.list();
				 } finally {
					 if (session != null && session.isOpen ()) {
						 session.close ();
					 }
					 msoLogger.debug ("getWatchdogComponentNames - Successfully");
				 }
				 return results;
			 }
	
}
