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
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class OperationalEnvServiceModelStatusDb {

    protected final AbstractSessionFactoryManager sessionFactoryRequestDB;
    
    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    
    protected static final String			REQUEST_ID               		= "requestId";
    protected static final String			OPERATIONAL_ENV_ID  		 	= "operationalEnvId";
    protected static final String			SERVICE_MODEL_VERSION_ID      	= "serviceModelVersionId";
    protected static final String			SERVICE_MOD_VER_DISTR_STATUS  	= "serviceModelVersionDistrStatus";
    protected static final String			RECOVERY_ACTION         		= "recoveryAction";
    protected static final int				RETRY_COUNT_LEFT          		= 0;
    protected static final String			CREATE_TIME                 	= "startTime";
    protected static final String 			MODIFY_TIME                 	= "modifyTime";
    

    public static OperationalEnvServiceModelStatusDb getInstance() {
        return new OperationalEnvServiceModelStatusDb(new RequestsDbSessionFactoryManager ());
    }

    protected OperationalEnvServiceModelStatusDb (AbstractSessionFactoryManager sessionFactoryRequest) {
        sessionFactoryRequestDB = sessionFactoryRequest;
    }
    

    /**
     * Retrieve OperationalEnvServiceModelStatus from given OperationalEnvironmentId and serviceModelVersionId
     * @param operationalEnvId
     * @param serviceModelVersionId
     * @return
     */
	public OperationalEnvServiceModelStatus getOperationalEnvServiceModelStatus(String operationalEnvId, String serviceModelVersionId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve OperationalEnvironmentServiceModel with operationalEnvironmentId: " + operationalEnvId + ", serviceModelVersionId: " + serviceModelVersionId);

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		OperationalEnvServiceModelStatus request = null;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("FROM OperationalEnvServiceModelStatus WHERE operationalEnvId = :operationalEnvId AND serviceModelVersionId = :serviceModelVersionId");
			query.setParameter ("operationalEnvId", operationalEnvId);
			query.setParameter ("serviceModelVersionId", serviceModelVersionId);
			request = (OperationalEnvServiceModelStatus) query.uniqueResult ();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "OperationalEnvServiceModelStatus", "getOperationalEnvServiceModelStatus", null);
		}
		return request;
	}
	
	
    /**
     * Retrieve OperationalEnvServiceModelStatus from given OperationalEnvironmentId and serviceModelVersionId
     * @param operationalEnvId
     * @param serviceModelVersionId
     * @return
     */
	@SuppressWarnings("unchecked")
	public List<OperationalEnvServiceModelStatus> getOperationalEnvIdStatus(String operationalEnvId, String requestId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Retrieve OperationalEnvironmentServiceModel with operationalEnvironmentId: " + operationalEnvId + ", requestId: " + requestId);

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		List<OperationalEnvServiceModelStatus> requests = new ArrayList<OperationalEnvServiceModelStatus>();
		
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("FROM OperationalEnvServiceModelStatus WHERE operationalEnvId = :operationalEnvId AND requestId = :requestId");
			query.setParameter ("operationalEnvId", operationalEnvId);
			query.setParameter ("requestId", requestId);
			requests = query.list();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
										"Successfully", "OperationalEnvServiceModelStatus", "getOperationalEnvIdStatus", null);
		}
		return requests;
	}
	
	
    /**
     * Update OperationalEnvServiceModelStatus serviceModelVersionDistrStatus with asdcStatus and retryCount for given operationalEnvId, serviceModelVersionId
     * @param operationalEnvId
     * @param serviceModelVersionId
     * @param asdcStatus
     * @param retryCount
     */
	public int updateOperationalEnvRetryCountStatus(String operationalEnvId, String serviceModelVersionId, String asdcStatus, int retryCount) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Update OperationalEnvServiceModelStatus retryCount: " + retryCount + " and serviceModelVersionDistrStatus :" + asdcStatus);

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		int result = 0;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("update OperationalEnvServiceModelStatus set serviceModelVersionDistrStatus = :serviceModelVersionDistrStatus, retryCount = :retryCount, modifyTime = :modifyTime where "
					+ "operationalEnvId = :operationalEnvId and serviceModelVersionId = :serviceModelVersionId ");
			query.setParameter ("retryCount", retryCount);
			query.setParameter (SERVICE_MOD_VER_DISTR_STATUS, asdcStatus);
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
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "OperationalEnvServiceModelDB", "updateOperationalEnvRetryCountStatus", null);
		}
		return result;
	}
	
    /**
     * Update OperationalEnvServiceModelStatus serviceModelVersionDistrStatus with asdcStatus and retryCount for given operationalEnvId, serviceModelVersionId, requestId
     * @param operationalEnvId
     * @param serviceModelVersionId
     * @param asdcStatus
     * @param retryCount
     * @param requestId
     */
	public int updateOperationalEnvRetryCountStatusPerReqId(String operationalEnvId, String serviceModelVersionId, String asdcStatus, int retryCount, String requestId) {
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("Update OperationalEnvServiceModelStatus retryCount: " + retryCount + " and serviceModelVersionDistrStatus :" + asdcStatus);

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		int result = 0;
		try {
			session.beginTransaction ();
			Query query = session.createQuery ("update OperationalEnvServiceModelStatus set serviceModelVersionDistrStatus = :asdcStatus, retryCount = :retryCount, modifyTime = :modifyTime where "
					+ "operationalEnvId = :operationalEnvId and serviceModelVersionId = :serviceModelVersionId and requestId = :requestId ");
			query.setParameter ("retryCount", retryCount);
			query.setParameter (SERVICE_MOD_VER_DISTR_STATUS, asdcStatus);
			query.setParameter ("operationalEnvId", operationalEnvId);
			query.setParameter ("serviceModelVersionId", serviceModelVersionId);
			query.setParameter ("requestId", requestId);
			Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
			query.setParameter ("modifyTime", modifyTimeStamp);
			result = query.executeUpdate ();
			session.getTransaction ().commit ();
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
			}
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "OperationalEnvServiceModelDB", "updateOperationalEnvRetryCountStatusPerReqId", null);
		}
		return result;
	}
	
	
    /**
     * Insert into OperationalEnvServiceModelStatus with operationalEnvId, serviceModelVersionId, distributionIdStatus, recoveryAction, retryCount 
     * @param operationalEnvId
     * @param serviceModelVersionId
     * @param distributionIdStatus
     * @param distributionId
     * @param recoveryAction
     * @param retryCount
     */
	public void insertOperationalEnvServiceModelStatus(String requestId, String operationalEnvId, String serviceModelVersionId, 
			String distributionIdStatus, String recoveryAction, int retryCount, String workloadContext) {
		long startTime = System.currentTimeMillis ();
		Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
		msoLogger.debug ("Insert into OperationalEnvServiceModelStatus " );

		Session session = sessionFactoryRequestDB.getSessionFactory ().openSession ();
		OperationalEnvServiceModelStatus oesm = new OperationalEnvServiceModelStatus ();
	
		try {
			session.beginTransaction ();
		
			oesm.setRequestId (requestId);
			oesm.setOperationalEnvId (operationalEnvId);
			oesm.setServiceModelVersionId (serviceModelVersionId);
			oesm.setServiceModelVersionDistrStatus (distributionIdStatus);
			oesm.setRecoveryAction (recoveryAction);
			oesm.setRetryCount (retryCount);
			oesm.setWorkloadContext(workloadContext);
			oesm.setCreateTime (startTimeStamp);
			Timestamp modifyTimeStamp = new Timestamp (System.currentTimeMillis());
			oesm.setModifyTime (modifyTimeStamp);
		
			msoLogger.debug ("About to insert a record into OperationalEnvServiceModelStatus");
		
			session.save (oesm);
			session.getTransaction ().commit ();
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in insertOperationalEnvServiceModelStatus", e);
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "OperationalEnvServiceModelStatusDB", "saveRequest", null);
			if (session != null) {
				session.close ();
			}
			// throw an Exception in the event of a DB insert failure so that the calling routine can exit
			throw e;
		} finally {
			if (session != null && session.isOpen ()) {
				session.close ();
		}
		msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "OperationalEnvServiceModelStatusDB", "insertOperationalEnvServiceModelStatus", null);
	}
		
  }

}
