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

public class OperationalEnvDistributionStatusDb {

    protected final AbstractSessionFactoryManager sessionFactoryRequestDB;
    
    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    
    protected static final String         DISTRIBUTION_ID               = "distributionId";
    protected static final String		  OPERATIONAL_ENV_ID  		 	= "operationalEnvId";
    protected static final String         REQUEST_ID               		= "requestId";
    protected static final String         SERVICE_MODEL_VERSION_ID      = "serviceModelVersionId";
    protected static final String         DISTRIBUTION_ID_STATUS  		= "distributionIdStatus";
    protected static final String         CREATE_TIME                 	= "startTime";
    protected static final String         MODIFY_TIME                 	= "modifyTime";
    

    public static OperationalEnvDistributionStatusDb getInstance() {
        return new OperationalEnvDistributionStatusDb(new RequestsDbSessionFactoryManager ());
    }

    protected OperationalEnvDistributionStatusDb (AbstractSessionFactoryManager sessionFactoryRequest) {
        sessionFactoryRequestDB = sessionFactoryRequest;
    }


    /**
     * Retrieve OperationalEnvDistributionStatus from getSecgiven distributionId
     * @param distributionId
     * @return
     */
	public OperationalEnvDistributionStatus getOperationalEnvDistributionStatus(String distributionId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve Operational Environment Distribution Status with distributionId: " + distributionId);

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		OperationalEnvDistributionStatus request = null;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("from OperationalEnvDistributionStatus where distributionId = :distributionId");
			query.setParameter (DISTRIBUTION_ID, distributionId);
			request = (OperationalEnvDistributionStatus) query.uniqueResult ();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "OperationalEnvDistributionStatus", "getOperationalEnvDistributionStatus", null);
		}
		return request;
	}
	
    /**
     * Retrieve OperationalEnvDistributionStatus from given distributionId and requestId
     * @param distributionId
     * @param requestId
     * @return OperationalEnvDistributionStatus
     */
	public OperationalEnvDistributionStatus getOperationalEnvDistributionStatusPerReqId(String distributionId, String requestId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve Operational Environment Distribution Status with distributionId: " + distributionId + ", requestId: " + requestId);

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		OperationalEnvDistributionStatus request = null;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("from OperationalEnvDistributionStatus where distributionId = :distributionId AND requestId = :requestId");
			query.setParameter (DISTRIBUTION_ID, distributionId);
			query.setParameter (REQUEST_ID, requestId);
			request = (OperationalEnvDistributionStatus) query.uniqueResult ();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "OperationalEnvDistributionStatus", "getOperationalEnvDistributionStatusPerReqId", null);
		}
		return request;
	}
	
    /**
     * Update OperationalEnvDistributionStatus with distributionIdStatus for given distributionId, serviceModelVersionId, serviceModelVersionId
     * @param asdcStatus
     * @param distributionId
     * @param operationalEnvId
     * @param serviceModelVersionId
     */
	public int updateOperationalEnvDistributionStatus(String asdcStatus, String distributionId, 
											String operationalEnvId, String serviceModelVersionId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Update OperationalEnvDistributionStatus DISTRIBUTION_ID_STATUS with asdcStatus: " + asdcStatus);
		 
		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		int result = 0;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("update OperationalEnvDistributionStatus set distributionIdStatus = :distributionIdStatus, modifyTime = :modifyTime where distributionId = :distributionId and "
            		+ "operationalEnvId = :operationalEnvId and serviceModelVersionId = :serviceModelVersionId ");
            query.setParameter (DISTRIBUTION_ID_STATUS, asdcStatus);
            query.setParameter ("distributionId", distributionId);
            query.setParameter ("operationalEnvId", operationalEnvId);
            query.setParameter ("serviceModelVersionId", serviceModelVersionId);
            Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
            query.setParameter ("modifyTime", modifyTimeStamp);
            result = query.executeUpdate ();
            session.getTransaction ().commit ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "updateOperationalEnvDistributionStatus", null);
        }
		return result;
	}
	
    /**
     * Insert into OperationalEnvDistributionStatus with distributionId, operationalEnvId, serviceModelVersionId, distributionIdStatus
     * @param distributionId
     * @param operationalEnvId
     * @param serviceModelVersionId
     * @param distributionIdStatus
     */
	public void insertOperationalEnvDistributionStatus(String distributionId, String operationalEnvId, String serviceModelVersionId, 
				String distributionIdStatus, String requestId) {
		long startTime = System.currentTimeMillis ();
		Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
		msoLogger.debug ("Insert into OperationalEnvDistributionStatus " );

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		OperationalEnvDistributionStatus oed = new OperationalEnvDistributionStatus ();
		
		try {
			session.beginTransaction ();
			
			oed.setDistributionId (distributionId);
			oed.setOperationalEnvId (operationalEnvId);
			oed.setServiceModelVersionId (serviceModelVersionId);
			oed.setDistributionIdStatus (distributionIdStatus);
			oed.setRequestId(requestId);
			oed.setCreateTime (startTimeStamp);
			Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
			oed.setModifyTime (modifyTimeStamp);
			
			msoLogger.debug ("About to insert a record into OperationalEnvDistributionStatus");
			
            session.save (oed);
			session.getTransaction ().commit ();
		} 		catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in insertOperationalEnvDistributionStatus", e);
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "OperationalEnvDistributionStatusDB", "saveRequest", null);
            if (session != null) {
                session.close ();
            }
            // throw an Exception in the event of a DB insert failure so that the calling routine can exit
            throw e;
        }
		finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "OperationalEnvDistributionStatusDB", "insertOperationalEnvDistributionStatus", null);
		}
	}
	
	
}
