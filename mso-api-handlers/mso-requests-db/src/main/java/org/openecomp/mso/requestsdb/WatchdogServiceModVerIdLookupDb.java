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

public class WatchdogServiceModVerIdLookupDb {

    protected final AbstractSessionFactoryManager sessionFactoryRequestDB;
    
    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    protected static final String         DISTRIBUTION_ID               = "distributionId";
    protected static final String         SERVICE_MODEL_VERSION_ID  	= "serviceModelVersionId";
    protected static final String         CREATE_TIME                 	= "startTime";
    

    public static WatchdogServiceModVerIdLookupDb getInstance() {
        return new WatchdogServiceModVerIdLookupDb(new RequestsDbSessionFactoryManager ());
    }

    protected WatchdogServiceModVerIdLookupDb (AbstractSessionFactoryManager sessionFactoryRequest) {
        sessionFactoryRequestDB = sessionFactoryRequest;
    }


    /**
     * Insert into WATCHDOG_SERVICE_MOD_VER_ID_LOOKUP.
     *
     * @param distributionId
     * @param serviceModelVersionId
     * @return void
     */
	public void insertWatchdogServiceModVerIdLookup(String distributionId, String serviceModelVersionId ) {
		long startTime = System.currentTimeMillis ();
		Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
		msoLogger.debug ("Insert into WatchdogServiceModVerIdLookup for DistributionId: " + distributionId + " and ServiceModelVersionId: " + serviceModelVersionId  );
		
		if(getWatchdogServiceModVerId(distributionId) == null){

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		WatchdogServiceModVerIdLookup wdsm = new WatchdogServiceModVerIdLookup ();
	
		try {
			session.beginTransaction ();
		
			wdsm.setDistributionId (distributionId);
			wdsm.setServiceModelVersionId (serviceModelVersionId);
			wdsm.setCreateTime (startTimeStamp);
		
			msoLogger.debug ("About to insert a record into WatchdogServiceModVerIdLookup");
		
			session.save (wdsm);
			session.getTransaction ().commit ();
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in insertWatchdogServiceModVerIdLookup", e);
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "WatchdogServiceModVerIdLookupDB", "saveRequest", null);
			if (session != null) {
				session.close ();
			}
			// throw an Exception in the event of a DB insert failure so that the calling routine can exit
			throw e;
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
		}
		msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "WatchdogServiceModVerIdLookupDB", "insertWatchdogServiceModVerIdLookup", null);
		}
	}
		
  }
	
    /**
     * Retrieve from WATCHDOG_SERVICE_MOD_VER_ID_LOOKUP.
     *
     * @param distributionId
     * @return WatchdogServiceModVerIdLookup
     */
	public String getWatchdogServiceModVerId(String distributionId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve WatchdogServiceModVerIdLookup with distributionId: " + distributionId );

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		String serviceModelVersionId = null;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("Select serviceModelVersionId FROM WatchdogServiceModVerIdLookup WHERE distributionId = :distributionId ");
			query.setParameter ("distributionId", distributionId);
			serviceModelVersionId = (String) query.uniqueResult();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "WatchdogServiceModVerIdLookupDB", "getWatchdogServiceModVerId", null);
		}
		return serviceModelVersionId;
	}
}
