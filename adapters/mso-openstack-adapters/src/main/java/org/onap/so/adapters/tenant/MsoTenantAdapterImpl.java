/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.tenant;


import java.util.Map;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import org.onap.so.adapters.tenant.exceptions.TenantAlreadyExists;
import org.onap.so.adapters.tenant.exceptions.TenantException;
import org.onap.so.adapters.tenantrest.TenantRollback;
import org.onap.so.entity.MsoRequest;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoTenantUtils;
import org.onap.so.openstack.utils.MsoTenantUtilsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WebService(serviceName = "TenantAdapter", endpointInterface = "org.onap.so.adapters.tenant.MsoTenantAdapter",
        targetNamespace = "http://org.onap.so/tenant")
@Component
public class MsoTenantAdapterImpl implements MsoTenantAdapter {
    public static final String CREATE_TENANT = "createTenant";
    public static final String OPENSTACK = "OpenStack";
    public static final String QUERY_TENANT = "QueryTenant";
    public static final String DELETE_TENANT = "DeleteTenant";
    public static final String ROLLBACK_TENANT = "RollbackTenant";
    private static final String OPENSTACK_COMMUNICATE_EXCEPTION_MSG =
            "{} {} Exception while communicate with Open Stack ";
    @Resource
    private WebServiceContext wsContext;

    @Autowired
    private MsoTenantUtilsFactory tFactory;
    private static Logger logger = LoggerFactory.getLogger(MsoTenantAdapterImpl.class);

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck() {
        logger.debug("Health check call in Tenant Adapter");
    }

    /**
     * This is the "Create Tenant" web service implementation. It will create a new Tenant in the specified cloud. If
     * the tenant already exists, this can be considered a success or failure, depending on the value of the
     * 'failIfExists' parameter.
     *
     * The method returns the tenantId (the Openstack ID), and a TenantRollback object. This last object can be passed
     * as-is to the rollbackTenant method to undo what (if anything) was created. This is useful if a Tenant is
     * successfully created but the orchestrator fails on a subsequent operation.
     */
    @Override
    public void createTenant(String cloudSiteId, String tenantName, Map<String, String> metadata, Boolean failIfExists,
            Boolean backout, MsoRequest msoRequest, Holder<String> tenantId, Holder<TenantRollback> rollback)
            throws TenantException {

        logger.debug("Call to MSO createTenant adapter. Creating Tenant: {} in {}", tenantName, cloudSiteId);

        // Start building up rollback object
        TenantRollback tenantRollback = new TenantRollback();
        tenantRollback.setCloudId(cloudSiteId);
        tenantRollback.setMsoRequest(msoRequest);

        MsoTenantUtils tUtils;
        try {
            tUtils = tFactory.getTenantUtils(cloudSiteId);
        } catch (MsoCloudSiteNotFound me) {
            logger.error("{} {} no implementation found for {}: ", MessageEnum.RA_CREATE_TENANT_ERR,
                    ErrorCode.DataError.getValue(), cloudSiteId, me);
            throw new TenantException(me);
        }

        MsoTenant newTenant = null;
        String newTenantId;
        try {
            newTenant = tUtils.queryTenantByName(tenantName, cloudSiteId);
        } catch (MsoException me) {
            logger.error(OPENSTACK_COMMUNICATE_EXCEPTION_MSG, MessageEnum.RA_CREATE_TENANT_ERR,
                    ErrorCode.DataError.getValue(), me);
            throw new TenantException(me);
        }
        if (newTenant == null) {
            if (backout == null)
                backout = true;
            try {
                newTenantId = tUtils.createTenant(tenantName, cloudSiteId, metadata, backout.booleanValue());
            } catch (MsoException me) {
                logger.error(OPENSTACK_COMMUNICATE_EXCEPTION_MSG, MessageEnum.RA_CREATE_TENANT_ERR,
                        ErrorCode.DataError.getValue(), me);
                throw new TenantException(me);
            }
            tenantRollback.setTenantId(newTenantId);
            tenantRollback.setTenantCreated(true);
            logger.debug("Tenant {} successfully created with ID {}", tenantName, newTenantId);
        } else {
            if (failIfExists != null && failIfExists) {
                logger.error("{} {} CreateTenant: Tenant {} already exists in {} ", MessageEnum.RA_TENANT_ALREADY_EXIST,
                        ErrorCode.DataError.getValue(), tenantName, cloudSiteId);
                throw new TenantAlreadyExists(tenantName, cloudSiteId, newTenant.getTenantId());
            }

            newTenantId = newTenant.getTenantId();
            tenantRollback.setTenantCreated(false);
            logger.debug("Tenant {} already exists with ID {}", tenantName, newTenantId);
        }


        tenantId.value = newTenantId;
        rollback.value = tenantRollback;
        return;
    }

    @Override
    public void queryTenant(String cloudSiteId, String tenantNameOrId, MsoRequest msoRequest, Holder<String> tenantId,
            Holder<String> tenantName, Holder<Map<String, String>> metadata) throws TenantException {

        logger.debug("Querying Tenant {} in {}", tenantNameOrId, cloudSiteId);

        MsoTenantUtils tUtils;
        try {
            tUtils = tFactory.getTenantUtils(cloudSiteId);
        } catch (MsoCloudSiteNotFound me) {
            logger.error("{} {} no implementation found for {}: ", MessageEnum.RA_CREATE_TENANT_ERR,
                    ErrorCode.DataError.getValue(), cloudSiteId, me);
            throw new TenantException(me);
        }

        MsoTenant qTenant = null;
        try {
            qTenant = tUtils.queryTenant(tenantNameOrId, cloudSiteId);
            if (qTenant == null) {
                // Not found by ID, Try by name.
                qTenant = tUtils.queryTenantByName(tenantNameOrId, cloudSiteId);
            }

            if (qTenant == null) {
                logger.debug("QueryTenant: Tenant {} not found", tenantNameOrId);
                tenantId.value = null;
                tenantName.value = null;
                metadata.value = null;
            } else {
                logger.debug("QueryTenant: Tenant {} found with ID {}", tenantNameOrId, qTenant.getTenantId());
                tenantId.value = qTenant.getTenantId();
                tenantName.value = qTenant.getTenantName();
                metadata.value = qTenant.getMetadata();
            }
        } catch (MsoException me) {
            logger.error("Exception in queryTenant for {}: ", MessageEnum.RA_GENERAL_EXCEPTION,
                    ErrorCode.DataError.getValue(), tenantNameOrId, me);
            throw new TenantException(me);
        }
        return;
    }

    @Override
    public void deleteTenant(String cloudSiteId, String tenantId, MsoRequest msoRequest, Holder<Boolean> tenantDeleted)
            throws TenantException {

        logger.debug("Deleting Tenant {} in {}", tenantId, cloudSiteId);

        // Delete the Tenant.
        try {

            MsoTenantUtils tUtils = tFactory.getTenantUtils(cloudSiteId);
            boolean deleted = tUtils.deleteTenant(tenantId, cloudSiteId);
            tenantDeleted.value = deleted;
        } catch (MsoException me) {
            logger.error("{} {} Exception - DeleteTenant {}: ", MessageEnum.RA_DELETE_TEMAMT_ERR,
                    ErrorCode.DataError.getValue(), tenantId, me);
            throw new TenantException(me);
        }

        // On success, nothing is returned.
        return;
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation. A rollback object is returned to the
     * client in a successful creation response. The client can pass that object as-is back to the rollbackVnf operation
     * to undo the creation.
     *
     * The rollback includes removing the VNF and deleting the tenant if the tenant did not exist prior to the VNF
     * creation.
     */
    @Override
    public void rollbackTenant(TenantRollback rollback) throws TenantException {
        // rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            logger.warn("{} {} rollbackTenant, rollback is null", MessageEnum.RA_ROLLBACK_NULL,
                    ErrorCode.DataError.getValue());
            return;
        }

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudId();
        String tenantId = rollback.getTenantId();

        logger.debug("Rolling Back Tenant {} in {}", rollback.getTenantId(), cloudSiteId);

        if (rollback.getTenantCreated()) {
            try {

                MsoTenantUtils tUtils = tFactory.getTenantUtils(cloudSiteId);
                tUtils.deleteTenant(tenantId, cloudSiteId);
            } catch (MsoException me) {
                me.addContext(ROLLBACK_TENANT);
                // Failed to delete the tenant.
                logger.error("{} {} Exception - rollbackTenant {}: ", MessageEnum.RA_ROLLBACK_TENANT_ERR,
                        ErrorCode.DataError.getValue(), tenantId, me);
                throw new TenantException(me);
            }
        }
        return;
    }
}
