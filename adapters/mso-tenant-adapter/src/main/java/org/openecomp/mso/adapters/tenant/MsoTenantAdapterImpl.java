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

package org.openecomp.mso.adapters.tenant;


import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.adapters.tenant.exceptions.TenantAlreadyExists;
import org.openecomp.mso.adapters.tenant.exceptions.TenantException;
import org.openecomp.mso.adapters.tenantrest.TenantRollback;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoTenantUtils;
import org.openecomp.mso.openstack.utils.MsoTenantUtilsFactory;

@WebService(serviceName = "TenantAdapter", endpointInterface = "org.openecomp.mso.adapters.tenant.MsoTenantAdapter", targetNamespace = "http://org.openecomp.mso/tenant")
public class MsoTenantAdapterImpl implements MsoTenantAdapter {

	MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	MsoTenantUtilsFactory tFactory = new MsoTenantUtilsFactory(MSO_PROP_TENANT_ADAPTER);
	
	public static final String MSO_PROP_TENANT_ADAPTER="MSO_PROP_TENANT_ADAPTER";
	
    @Resource
    WebServiceContext wsContext;

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck () {
        LOGGER.debug ("Health check call in Tenant Adapter");
    }

    /**
     * This is the "Create Tenant" web service implementation. It will create
     * a new Tenant in the specified cloud. If the tenant already exists, this
     * can be considered a success or failure, depending on the value of the
     * 'failIfExists' parameter.
     *
     * The method returns the tenantId (the Openstack ID), and a TenantRollback
     * object. This last object can be passed as-is to the rollbackTenant method
     * to undo what (if anything) was created. This is useful if a Tenant is
     * successfully created but the orchestrator fails on a subsequent operation.
     */
    @Override
    public void createTenant (String cloudSiteId,
                              String tenantName,
                              Map <String, String> metadata,
                              Boolean failIfExists,
                              Boolean backout,
                              MsoRequest msoRequest,
                              Holder <String> tenantId,
                              Holder <TenantRollback> rollback) throws TenantException, TenantAlreadyExists {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("CreateTenant");

        LOGGER.debug ("Call to MSO createTenant adapter. Creating Tenant: " + tenantName
                                      + "in "
                                      + cloudSiteId);

        // Will capture total time for metrics
        long startTime = System.currentTimeMillis ();

        // Start building up rollback object
        TenantRollback tenantRollback = new TenantRollback ();
        tenantRollback.setCloudId (cloudSiteId);
        tenantRollback.setMsoRequest (msoRequest);
        
        MsoTenantUtils tUtils = tFactory.getTenantUtils (cloudSiteId);

        MsoTenant newTenant = null;
        String newTenantId = null;
        long queryTenantStartTime = System.currentTimeMillis ();
        try {
            newTenant = tUtils.queryTenantByName (tenantName, cloudSiteId);
            LOGGER.recordMetricEvent (queryTenantStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryTenant", null);
        } catch (MsoException me) {
            LOGGER.recordMetricEvent (queryTenantStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with Open Stack", "OpenStack", "QueryTenant", null);
            String error = "Create Tenant " + tenantName + ": " + me;
            LOGGER.error (MessageEnum.RA_CREATE_TENANT_ERR, me.getMessage(), "OpenStack", "createTenant", MsoLogger.ErrorCode.DataError, "Exception while communicate with Open Stack", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new TenantException (me);
        }
        if (newTenant == null) {
            if (backout == null)
                backout = true;
            long createTenantStartTime = System.currentTimeMillis ();
            try {
                newTenantId = tUtils.createTenant (tenantName, cloudSiteId, metadata, backout.booleanValue ());
                LOGGER.recordMetricEvent (createTenantStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "CreateTenant", null);
            } catch (MsoException me) {
                LOGGER.recordMetricEvent (createTenantStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with Open Stack", "OpenStack", "CreateTenant", null);
                String error = "Create Tenant " + tenantName + ": " + me;
                LOGGER.error (MessageEnum.RA_CREATE_TENANT_ERR, me.getMessage(), "OpenStack", "createTenant", MsoLogger.ErrorCode.DataError, "Exception while communicate with Open Stack", me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new TenantException (me);
            }
            tenantRollback.setTenantId (newTenantId);
            tenantRollback.setTenantCreated (true);
            LOGGER.debug ("Tenant " + tenantName + " successfully created with ID " + newTenantId);
        } else {
            if (failIfExists != null && failIfExists) {
                String error = "CreateTenant: Tenant " + tenantName + " already exists in " + cloudSiteId;
                LOGGER.error (MessageEnum.RA_TENANT_ALREADY_EXIST, tenantName, cloudSiteId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "CreateTenant, Tenant already exists");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
                throw new TenantAlreadyExists (tenantName, cloudSiteId, newTenant.getTenantId ());
            }

            newTenantId = newTenant.getTenantId ();
            tenantRollback.setTenantCreated (false);
            LOGGER.debug ("Tenant " + tenantName + " already exists with ID " + newTenantId);
        }


        tenantId.value = newTenantId;
        rollback.value = tenantRollback;
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create tenant");
        return;
    }

    @Override
    public void queryTenant (String cloudSiteId,
                             String tenantNameOrId,
                             MsoRequest msoRequest,
                             Holder <String> tenantId,
                             Holder <String> tenantName,
                             Holder <Map <String, String>> metadata) throws TenantException {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("QueryTenant");
        LOGGER.debug ("Querying Tenant " + tenantNameOrId + " in " + cloudSiteId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        MsoTenantUtils tUtils = tFactory.getTenantUtils (cloudSiteId);
        
        MsoTenant qTenant = null;
        long subStartTime = System.currentTimeMillis ();
        try {
            qTenant = tUtils.queryTenant (tenantNameOrId, cloudSiteId);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryTenant", null);
            if (qTenant == null) {
                // Not found by ID, Try by name.
                qTenant = tUtils.queryTenantByName (tenantNameOrId, cloudSiteId);
            }

            if (qTenant == null) {
                LOGGER.debug ("QueryTenant: Tenant " + tenantNameOrId + " not found");
                tenantId.value = null;
                tenantName.value = null;
                metadata.value = null;
            } else {
                LOGGER.debug ("QueryTenant: Tenant " + tenantNameOrId + " found with ID " + qTenant.getTenantId ());
                tenantId.value = qTenant.getTenantId ();
                tenantName.value = qTenant.getTenantName ();
                metadata.value = qTenant.getMetadata ();
            }
        } catch (MsoException me) {
            String error = "Query Tenant " + tenantNameOrId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryTenant", null);
            LOGGER.error (MessageEnum.RA_GENERAL_EXCEPTION, me.getMessage(), "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception in queryTenant", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new TenantException (me);
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully query tenant");
        return;
    }

    @Override
    public void deleteTenant (String cloudSiteId,
                              String tenantId,
                              MsoRequest msoRequest,
                              Holder <Boolean> tenantDeleted) throws TenantException {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("DeleteTenant");

        LOGGER.debug ("Deleting Tenant " + tenantId + " in " + cloudSiteId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Delete the Tenant.
        long subStartTime = System.currentTimeMillis ();
        try {
        	
        	MsoTenantUtils tUtils = tFactory.getTenantUtils (cloudSiteId);
            boolean deleted = tUtils.deleteTenant (tenantId, cloudSiteId);
            tenantDeleted.value = deleted;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully communicate with Open Stack", "OpenStack", "DeleteTenant", null);
        } catch (MsoException me) {
            String error = "Delete Tenant " + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteTenant", null);
            LOGGER.error (MessageEnum.RA_DELETE_TEMAMT_ERR, me.getMessage(), "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - DeleteTenant", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new TenantException (me);
        }

        // On success, nothing is returned.
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully delete tenant");
        return;
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation.
     * A rollback object is returned to the client in a successful creation
     * response. The client can pass that object as-is back to the rollbackVnf
     * operation to undo the creation.
     *
     * The rollback includes removing the VNF and deleting the tenant if the
     * tenant did not exist prior to the VNF creation.
     */
    @Override
    public void rollbackTenant (TenantRollback rollback) throws TenantException {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("RollbackTenant");
        // rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            LOGGER.warn (MessageEnum.RA_ROLLBACK_NULL, "OpenStack", "rollbackTenant", MsoLogger.ErrorCode.DataError, "rollbackTenant, rollback is null");
            return;
        }

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudId ();
        String tenantId = rollback.getTenantId ();

        MsoLogger.setLogContext (rollback.getMsoRequest ());
        LOGGER.debug ("Rolling Back Tenant " + rollback.getTenantId () + " in " + cloudSiteId);

        long subStartTime = System.currentTimeMillis ();
        if (rollback.getTenantCreated ()) {
            try {
            	 
            	MsoTenantUtils tUtils = tFactory.getTenantUtils (cloudSiteId);
                tUtils.deleteTenant (tenantId, cloudSiteId);
                LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully communicate with Open Stack", "OpenStack", "RollbackTenant", null);
            } catch (MsoException me) {
                me.addContext ("RollbackTenant");
                // Failed to delete the tenant.
                String error = "Rollback Tenant " + tenantId + ": " + me;
                LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "RollbackTenant", null);
                LOGGER.error (MessageEnum.RA_ROLLBACK_TENANT_ERR, me.getMessage(), "OpenStack", "rollbackTenant", MsoLogger.ErrorCode.DataError, "Exception - rollbackTenant", me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new TenantException (me);
            }
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully roll back tenant");
        return;
    }
}
