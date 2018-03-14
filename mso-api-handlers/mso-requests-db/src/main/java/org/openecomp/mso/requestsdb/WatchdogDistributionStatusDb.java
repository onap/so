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

import org.hibernate.Query;
import org.hibernate.Session;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class WatchdogDistributionStatusDb {

    protected final AbstractSessionFactoryManager sessionFactoryRequestDB;
    
    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    
    protected static final String         DISTRIBUTION_ID               = "distributionId";
    protected static final String         DISTRIBUTION_ID_STATUS  		= "distributionIdStatus";
    protected static final String         CREATE_TIME                 	= "startTime";
    protected static final String         MODIFY_TIME                 	= "modifyTime";
    

    public static WatchdogDistributionStatusDb getInstance() {
        return new WatchdogDistributionStatusDb(new RequestsDbSessionFactoryManager ());
    }

    protected WatchdogDistributionStatusDb (AbstractSessionFactoryManager sessionFactoryRequest) {
        sessionFactoryRequestDB = sessionFactoryRequest;
    }


    /**
     * Insert into WATCHDOG_DISTRIBUTIONID_STATUS.
     *
     * @param distributionId
     * @return void
     */
	public void insertWatchdogDistributionId(String distributionId ) {
		long startTime = System.currentTimeMillis ();
		Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
		msoLogger.debug ("Insert into WatchdogDistributionStatus - DistributionId: " + distributionId);
		
		if(getWatchdogDistributionId(distributionId) == null){

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		WatchdogDistributionStatus wds = new WatchdogDistributionStatus ();
	
		try {
			session.beginTransaction ();
		
			wds.setDistributionId (distributionId);
			wds.setCreateTime (startTimeStamp);
			Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
			wds.setModifyTime (modifyTimeStamp);
		
			msoLogger.debug ("About to insert a record into WatchdogDistributionStatus ");
		
			session.save (wds);
			session.getTransaction ().commit ();
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in insertWatchdogDistributionId", e);
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "WatchdogDistributionStatusDB", "saveRequest", null);
			if (session != null) {
				session.close ();
			}
			// throw an Exception in the event of a DB insert failure so that the calling routine can exit
			throw e;
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "WatchdogDistributionStatusDB", "insertWatchdogDistributionId", null);
		}
		
		}
		
	}
	
	
    /**
     * Update WATCHDOG_DISTRIBUTIONID_STATUS with new status for a given distributionid.
     *
     * @param distributionId
     * @param distributionStatus
     * @return void
     */
	public void updateWatchdogDistributionIdStatus(String distributionId, String distributionIdStatus ) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Update WatchdogDistributionStatus status with distributionId: " + distributionId + " and distributionStatus: " + distributionIdStatus );

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
	
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("update WatchdogDistributionStatus set distributionIdStatus = :distributionIdStatus where "
					+ "distributionId = :distributionId ");

			query.setParameter ("distributionId", distributionId);
			query.setParameter ("distributionIdStatus", distributionIdStatus);

			//Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
			//query.setParameter ("modifyTime", modifyTimeStamp);
			query.executeUpdate ();
			session.getTransaction ().commit ();			
			
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in updateWatchdogDistributionStatus", e);
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "WatchdogDistributionStatusDB", "saveRequest", null);
			if (session != null) {
				session.close ();
			}
			// throw an Exception in the event of a DB insert failure so that the calling routine can exit
			throw e;
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "WatchdogDistributionStatusDB", "insertWatchdogDistributionStatus", null);
		}
	}
	
    /**
     * Retrieve records from WatchdogDistributionIdStatus.
     *
     * @param distributionId
     * @return WatchdogDistributionIdStatus
     */
	public String getWatchdogDistributionIdStatus(String distributionId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve records from WatchdogDistributionStatus for distributionId : " + distributionId );

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		String distributionStatus = null;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("SELECT distributionIdStatus FROM WatchdogDistributionStatus WHERE distributionId = :distributionId ");
			query.setParameter ("distributionId", distributionId);
			distributionStatus = (String) query.uniqueResult();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "WatchdogDistributionStatusDB", "getWatchdogDistributionIdStatus", null);
		}
		return distributionStatus;
	}
	
    /**
     * Retrieve records from WatchdogDistributionId.
     *
     * @param distributionId
     * @return WatchdogDistributionIdStatus
     */
	public String getWatchdogDistributionId(String distributionId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve distributionId from WatchdogDistributionStatus for distributionId : " + distributionId );

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		String existingDistributionId = null;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("SELECT distributionId FROM WatchdogDistributionStatus WHERE distributionId = :distributionId ");
			query.setParameter ("distributionId", distributionId);
			existingDistributionId = (String) query.uniqueResult();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "WatchdogDistributionStatusDB", "getWatchdogDistributionIdStatus", null);
		}
		return existingDistributionId;
	}
}
