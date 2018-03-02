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

package org.openecomp.mso.cloud;


import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.openstack.utils.MsoKeystoneUtils;
import org.openecomp.mso.openstack.utils.MsoNeutronUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This class returns a cloud Config instances
 *
 *
 */

@Singleton(name = "CloudConfigFactory")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@LocalBean
@Path("/cloud")
public class CloudConfigFactory implements Serializable {

    private static final long serialVersionUID = 2956662716453261085L;

    private static CloudConfig cloudConfigCache = new CloudConfig ();

    protected static String prefixMsoPropertiesPath = System.getProperty ("mso.config.path");

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

    private static int refreshTimer;

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock ();

    static {
        if (prefixMsoPropertiesPath == null) {
            prefixMsoPropertiesPath = "";
        }
    }

    public void initializeCloudConfig (String filePath, int refreshTimer) throws MsoCloudIdentityNotFound {

        rwl.writeLock ().lock ();
        try {
            cloudConfigCache.loadCloudConfig (prefixMsoPropertiesPath + filePath, refreshTimer);
            LOGGER.info (MessageEnum.RA_CONFIG_LOAD, prefixMsoPropertiesPath + filePath, "", "");
        } catch (JsonParseException e) {
            LOGGER.error (MessageEnum.RA_CONFIG_EXC, "Error parsing cloud config file " + filePath, "", "", MsoLogger.ErrorCode.DataError, "Exception - JsonParseException", e);
        } catch (JsonMappingException e) {
            LOGGER.error (MessageEnum.RA_CONFIG_EXC, "Error parsing cloud config file " + filePath, "", "", MsoLogger.ErrorCode.DataError, "Exception - JsonMappingException", e);
        } catch (IOException e) {
            LOGGER.error (MessageEnum.RA_CONFIG_NOT_FOUND, filePath, "", "", MsoLogger.ErrorCode.DataError, "Exception - config not found", e);
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    public void changeMsoPropertiesFilePath (String newMsoPropPath) {
        rwl.writeLock ().lock ();
        try {
            CloudConfigFactory.cloudConfigCache.configFilePath = prefixMsoPropertiesPath + newMsoPropPath;
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    public CloudConfigFactory () {
    }

    public CloudConfig getCloudConfig () {
        rwl.readLock ().lock ();
        try {
            if (cloudConfigCache.isValidCloudConfig()) {
                return cloudConfigCache.clone ();
            } else {
                return new CloudConfig();
            }
        } finally {
            rwl.readLock ().unlock ();
        }
    }

    /**
     * This method is not intended to be called, it's used to refresh the config
     * automatically
     *
     * @return true if Properties have been reloaded, false otherwise
     */
    @Schedule(minute = "*/1", hour = "*", persistent = false)
    public void reloadCloudConfig () {

        try {
            if (!rwl.writeLock ().tryLock () && !rwl.writeLock ().tryLock (30L, TimeUnit.SECONDS)) {
                LOGGER.debug ("Busy write lock on mso cloud config factory, skipping the reloading");
                return;
            }
        } catch (InterruptedException e1) {
            LOGGER.debug ("Interrupted while trying to acquire write lock on cloud config factory, skipping the reloading");
            Thread.currentThread ().interrupt ();
            return;
        }
        try {
            //LOGGER.debug ("Processing a reload of the mso properties file entries");
            try {

                if (refreshTimer <= 1) {
                    CloudConfig oldCloudConfig = null;
                    if (cloudConfigCache.isValidCloudConfig()) {
                        oldCloudConfig = cloudConfigCache.clone();
                    }
                    cloudConfigCache.reloadPropertiesFile ();
                    refreshTimer = cloudConfigCache.refreshTimerInMinutes;
                    if (!cloudConfigCache.equals(oldCloudConfig)) {
                    	LOGGER.info (MessageEnum.RA_CONFIG_LOAD, prefixMsoPropertiesPath + cloudConfigCache.configFilePath, "", "");
                    }

                } else {
                    --refreshTimer;
                }

            } catch (JsonParseException e) {
                LOGGER.error (MessageEnum.RA_CONFIG_EXC,
                              "Error parsing cloud config file " + cloudConfigCache.configFilePath, "", "", MsoLogger.ErrorCode.DataError, "Exception - JsonParseException",
                              e);
            } catch (JsonMappingException e) {
                LOGGER.error (MessageEnum.RA_CONFIG_EXC,
                              "Error parsing cloud config file " + cloudConfigCache.configFilePath, "", "", MsoLogger.ErrorCode.DataError, "Exception - JsonMappingException",
                              e);
            } catch (IOException e) {
                LOGGER.error (MessageEnum.RA_CONFIG_NOT_FOUND, cloudConfigCache.configFilePath, "", "", MsoLogger.ErrorCode.DataError, "Exception - config not found", e);
            }
        } catch (Exception e) {
            LOGGER.error (MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. Global issue while reloading", "", "", MsoLogger.ErrorCode.DataError, "Exception - Global issue while reloading\"", e);
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    @GET
    @Path("/showConfig")
    @Produces("text/plain")
    public Response showCloudConfig () {
        CloudConfig cloudConfig = this.getCloudConfig ();
        if (cloudConfig != null) {
            StringBuffer response = new StringBuffer ();
            response.append ("Cloud Sites:\n");
            for (CloudSite site : cloudConfig.getCloudSites ().values ()) {
                response.append (site.toString () + "\n");
            }
    
            response.append ("\n\nCloud Identity Services:\n");
            for (CloudIdentity identity : cloudConfig.getIdentityServices ().values ()) {
                response.append (identity.toString () + "\n");
            }
    
            return Response.status (200).entity (response).build ();
        } else {
            return Response.status (500).entity ("Cloud Config has not been loaded properly, this could be due to a bad JSON structure (Check the logs for additional details)").build ();
        }
    }

    @GET
    @Path("/resetClientCaches")
    @Produces("text/plain")
    public Response resetClientCaches () {
        // Reset all cached clients/credentials
        MsoKeystoneUtils.adminCacheReset ();
        MsoHeatUtils.heatCacheReset ();
        MsoNeutronUtils.neutronCacheReset ();

        String response = "Client caches reset.  All entries removed.";
        return Response.status (200).entity (response).build ();
    }

    @GET
    @Path("/cleanupClientCaches")
    @Produces("text/plain")
    public Response cleanupClientCaches () {
        // Reset all cached clients/credentials
        MsoKeystoneUtils.adminCacheCleanup ();
        MsoHeatUtils.heatCacheCleanup ();
        MsoNeutronUtils.neutronCacheCleanup ();

        String response = "Client caches cleaned up.  All expired entries removed";
        return Response.status (200).entity (response).build ();
    }

    @GET
    @Path("/encryptPassword/{pwd}")
    @Produces("text/plain")
    public Response encryptPassword (@PathParam("pwd") String pwd) {
        String encryptedPassword = CloudIdentity.encryptPassword (pwd);

        String response = "Encrypted Password = " + encryptedPassword;
        return Response.status (200).entity (response).build ();
    }
}
