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

package org.onap.so.openstack.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.Set;

import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.PluginAction;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduException;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduPlugin;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.cloud.authentication.AuthenticationMethodFactory;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.onap.so.openstack.beans.HeatCacheEntry;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.MulticloudHeat;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.exceptions.MsoStackAlreadyExists;
import org.onap.so.openstack.exceptions.MsoTenantNotFound;
import org.onap.so.openstack.mappers.StackInfoMapper;
import org.onap.so.client.HttpClient;
import org.onap.so.client.RestClient;
import org.onap.so.utils.CryptoUtils;
import org.onap.so.utils.TargetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.model.CreateStackParam;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.heat.model.Stack.Output;
import com.woorea.openstack.heat.model.Stacks;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.utils.KeystoneUtils;

@Component
public class MsoMulticloudUtils extends MsoHeatUtils implements VduPlugin{

    private static final String TOKEN_AUTH = "TokenAuth";

    private static final String QUERY_ALL_STACKS = "QueryAllStacks";

    private static final String DELETE_STACK = "DeleteStack";

    private static final String HEAT_ERROR = "HeatError";

    private static final String CREATE_STACK = "CreateStack";

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, MsoMulticloudUtils.class);

	// Cache Heat Clients statically. Since there is just one MSO user, there is no
	// benefit to re-authentication on every request (or across different flows). The
	// token will be used until it expires.
	//
	// The cache key is "tenantId:cloudId"
	private static Map <String, HeatCacheEntry> heatClientCache = new HashMap <> ();

    @Autowired
    private Environment environment;

    @Autowired
    private AuthenticationMethodFactory authenticationMethodFactory;

    @Autowired
    private MsoTenantUtilsFactory tenantUtilsFactory;

    /******************************************************************************
     *
     * Methods (and associated utilities) to implement the VduPlugin interface
     *
     *******************************************************************************/

    /**
     * Create a new Stack in the specified cloud location and tenant. The Heat template
     * and parameter map are passed in as arguments, along with the cloud access credentials.
     * It is expected that parameters have been validated and contain at minimum the required
     * parameters for the given template with no extra (undefined) parameters..
     *
     * The Stack name supplied by the caller must be unique in the scope of this tenant.
     * However, it should also be globally unique, as it will be the identifier for the
     * resource going forward in Inventory. This latter is managed by the higher levels
     * invoking this function.
     *
     * The caller may choose to let this function poll Openstack for completion of the
     * stack creation, or may handle polling itself via separate calls to query the status.
     * In either case, a StackInfo object will be returned containing the current status.
     * When polling is enabled, a status of CREATED is expected. When not polling, a
     * status of BUILDING is expected.
     *
     * An error will be thrown if the requested Stack already exists in the specified
     * Tenant and Cloud.
     *
     * For 1510 - add "environment", "files" (nested templates), and "heatFiles" (get_files) as
     * parameters for createStack. If environment is non-null, it will be added to the stack.
     * The nested templates and get_file entries both end up being added to the "files" on the
     * stack. We must combine them before we add them to the stack if they're both non-null.
     *
     * @param cloudSiteId The cloud (may be a region) in which to create the stack.
     * @param tenantId The Openstack ID of the tenant in which to create the Stack
     * @param stackName The name of the stack to create
     * @param heatTemplate The Heat template
     * @param stackInputs A map of key/value inputs
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @param environment An optional yaml-format string to specify environmental parameters
     * @param files a Map<String, Object> that lists the child template IDs (file is the string, object is an int of
     *        Template id)
     * @param heatFiles a Map<String, Object> that lists the get_file entries (fileName, fileBody)
     * @param backout Donot delete stack on create Failure - defaulted to True
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */

    @SuppressWarnings("unchecked")
	@Override
    public StackInfo createStack (String cloudSiteId,
                                  String tenantId,
                                  String stackName,
                                  String heatTemplate,
                                  Map <String, ?> stackInputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes,
                                  String environment,
                                  Map <String, Object> files,
                                  Map <String, Object> heatFiles,
                                  boolean backout) throws MsoException {

        // EWMMC - Get out the directives, if present.
        String oofDirectives = null;
        String sdncDirectives = null;

        String key = "oof_directives";
        if (!stackInputs.isEmpty() && stackInputs.containsKey(key)) {
            oofDirectives = (String) stackInputs.get(key);
            stackInputs.remove(key);
        }
        key = "sdnc_directives";
        if (!stackInputs.isEmpty() && stackInputs.containsKey(key)) {
            sdncDirectives = (String) stackInputs.get(key);
            stackInputs.remove(key);
        }

        // create the multicloud payload
        CreateStackParam stack = createStackParam(stackName, heatTemplate, stackInputs, timeoutMinutes, environment, files, heatFiles);

        MsoMulticloudParam multicloudParam = new MsoMulticloudParam();
        multicloudParam.setGenericVnfId("genericVnfId");   // TBD
        multicloudParam.setVfModuleId("vfModuleId");       // TBD
        multicloudParam.setOofDirectives(oofDirectives);
        multicloudParam.setSdncDirectives(sdncDirectives);
        multicloudParam.setTemplateType("heat");
        multicloudParam.setTemplateData(stack.toString()); // TBD - probably not right

        String multicloudEndpoint = "http://{msbIP}:{msbPort}/api/multicloud/v1/cloud-owner/cloud-region/infra_workload";

        RestClient multicloudClient = getMulticloudClient(multicloudEndpoint);

        if (multicloudClient != null) {
            Response res = multicloudClient.post(multicloudParam);
            LOGGER.debug("Multicloud Post response is: " + res);
        }

        Stack responseStack = new Stack();
        responseStack.setStackStatus(HeatStatus.CREATED.toString());

        return new StackInfoMapper(responseStack).map();
    }

	protected Stack queryHeatStack (MulticloudHeat heatClient, String stackName) throws MsoException {
		if (stackName == null) {
			return null;
		}
		try {
			OpenStackRequest <Stack> request = heatClient.getStacks ().byName (stackName);
			return executeAndRecordOpenstackRequest (request);
		} catch (OpenStackResponseException e) {
			if (e.getStatus () == 404) {
				LOGGER.debug ("queryHeatStack - stack not found: " + stackName);
				return null;
			} else {
				// Convert the OpenStackResponseException to an MsoOpenstackException
				throw heatExceptionToMsoException (e, "QueryStack");
			}
		} catch (OpenStackConnectException e) {
			// Connection to Openstack failed
			throw heatExceptionToMsoException (e, "QueryAllStack");
		}
	}


	public Map<String, Object> queryStackForOutputs(String cloudSiteId,
													String tenantId, String stackName) throws MsoException {
		LOGGER.debug("MsoHeatUtils.queryStackForOutputs)");
		StackInfo heatStack = this.queryStack(cloudSiteId, tenantId, stackName);
		if (heatStack == null || heatStack.getStatus() == HeatStatus.NOTFOUND) {
			return null;
		}
		return heatStack.getOutputs();
	}

	/**
     * Query for a single stack (by Name) in a tenant. This call will always return a
     * StackInfo object. If the stack does not exist, an "empty" StackInfo will be
     * returned - containing only the stack name and a status of NOTFOUND.
     *
     * @param tenantId The Openstack ID of the tenant in which to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query
     * @param stackName The name of the stack to query (may be simple or canonical)
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */
    @Override
    public StackInfo queryStack (String cloudSiteId, String tenantId, String stackName) throws MsoException {
        LOGGER.debug ("Query HEAT stack: " + stackName + " in tenant " + tenantId);

        String multicloudEndpoint = "http://{msbIP}:{msbPort}/api/multicloud/v1/cloud-owner/cloud-region/infra_workload/" + stackName;

        RestClient multicloudClient = getMulticloudClient(multicloudEndpoint);

        if (multicloudClient != null) {
            Response response = multicloudClient.get();
            LOGGER.debug("Multicloud Get response is: " + response);

            return new StackInfo (stackName, HeatStatus.CREATED);
        }

        return new StackInfo (stackName, HeatStatus.NOTFOUND);
    }

	// ---------------------------------------------------------------
	// PRIVATE FUNCTIONS FOR USE WITHIN THIS CLASS

    private RestClient getMulticloudClient(String endpoint) {
        RestClient client = null;
        try {
            client= new HttpClient(UriBuilder.fromUri(endpoint).build().toURL(),
            "application/json", TargetEntity.OPENSTACK_ADAPTER);
        } catch (MalformedURLException e) {
            LOGGER.debug("Encountered malformed URL error getting multicloud rest client " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Encountered illegal argument getting multicloud rest client " + e.getMessage());
        } catch (UriBuilderException e) {
            LOGGER.debug("Encountered URI builder error getting multicloud rest client " + e.getMessage());
        }
        return client;
    }

    /**
     * VduPlugin interface for instantiate function.
     *
     * Translate the VduPlugin parameters to the corresponding 'createStack' parameters,
     * and then invoke the existing function.
     */
    @Override
    public VduInstance instantiateVdu (
            CloudInfo cloudInfo,
            String instanceName,
            Map<String,Object> inputs,
            VduModelInfo vduModel,
            boolean rollbackOnFailure)
    	throws VduException
    {
    	String cloudSiteId = cloudInfo.getCloudSiteId();
    	String tenantId = cloudInfo.getTenantId();

    	// Translate the VDU ModelInformation structure to that which is needed for
    	// creating the Heat stack.  Loop through the artifacts, looking specifically
    	// for MAIN_TEMPLATE and ENVIRONMENT.  Any other artifact will
    	// be attached as a FILE.
    	String heatTemplate = null;
    	Map<String,Object> nestedTemplates = new HashMap<>();
    	Map<String,Object> files = new HashMap<>();
    	String heatEnvironment = null;

    	for (VduArtifact vduArtifact: vduModel.getArtifacts()) {
    		if (vduArtifact.getType() == ArtifactType.MAIN_TEMPLATE) {
    			heatTemplate = new String(vduArtifact.getContent());
    		}
    		else if (vduArtifact.getType() == ArtifactType.NESTED_TEMPLATE) {
    			nestedTemplates.put(vduArtifact.getName(), new String(vduArtifact.getContent()));
    		}
    		else if (vduArtifact.getType() == ArtifactType.ENVIRONMENT) {
    			heatEnvironment = new String(vduArtifact.getContent());
    		}
    	}

    	try {
    	    StackInfo stackInfo = createStack (cloudSiteId,
                    tenantId,
                    instanceName,
                    heatTemplate,
                    inputs,
                    true,    // poll for completion
                    vduModel.getTimeoutMinutes(),
                    heatEnvironment,
                    nestedTemplates,
                    files,
                    rollbackOnFailure);
            // Populate a vduInstance from the StackInfo
            return stackInfoToVduInstance(stackInfo);
        }
        catch (Exception e) {
            throw new VduException ("MsoMulticloudUtils (instantiateVDU): createStack Exception", e);
        }
    }


    /**
     * VduPlugin interface for query function.
     */
    @Override
    public VduInstance queryVdu (CloudInfo cloudInfo, String instanceId)
        throws VduException
    {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Query the Cloudify Deployment object and  populate a VduInstance
            StackInfo stackInfo = queryStack (cloudSiteId, tenantId, instanceId);

            return stackInfoToVduInstance(stackInfo);
        }
        catch (Exception e) {
            throw new VduException ("MsoMulticloudUtils (queryVdu): queryStack Exception ", e);
        }
    }


    /**
     * VduPlugin interface for delete function.
     */
    @Override
    public VduInstance deleteVdu (CloudInfo cloudInfo, String instanceId, int timeoutMinutes)
        throws VduException
    {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Delete the Multicloud stack
            StackInfo stackInfo = deleteStack (tenantId, cloudSiteId, instanceId, true);

            // Populate a VduInstance based on the deleted Cloudify Deployment object
            VduInstance vduInstance = stackInfoToVduInstance(stackInfo);

            // Override return state to DELETED (MulticloudUtils sets to NOTFOUND)
            vduInstance.getStatus().setState(VduStateType.DELETED);

            return vduInstance;
        }
        catch (Exception e) {
            throw new VduException ("Delete VDU Exception", e);
        }
    }


    /**
     * VduPlugin interface for update function.
     *
     * Update is currently not supported in the MsoMulticloudUtils implementation of VduPlugin.
     * Just return a VduException.
     *
     */
    @Override
    public VduInstance updateVdu (
            CloudInfo cloudInfo,
            String instanceId,
            Map<String,Object> inputs,
            VduModelInfo vduModel,
            boolean rollbackOnFailure)
        throws VduException
    {
        throw new VduException ("MsoMulticloudUtils: updateVdu interface not supported");
    }


    /*
     * Convert the local DeploymentInfo object (Cloudify-specific) to a generic VduInstance object
     */
    protected VduInstance stackInfoToVduInstance (StackInfo stackInfo)
    {
        VduInstance vduInstance = new VduInstance();

        // The full canonical name as the instance UUID
        vduInstance.setVduInstanceId(stackInfo.getCanonicalName());
        vduInstance.setVduInstanceName(stackInfo.getName());

        // Copy inputs and outputs
        vduInstance.setInputs(stackInfo.getParameters());
        vduInstance.setOutputs(stackInfo.getOutputs());

        // Translate the status elements
        vduInstance.setStatus(stackStatusToVduStatus (stackInfo));

        return vduInstance;
    }

    private VduStatus stackStatusToVduStatus (StackInfo stackInfo)
    {
        VduStatus vduStatus = new VduStatus();

        // Map the status fields to more generic VduStatus.
        // There are lots of HeatStatus values, so this is a bit long...
        HeatStatus heatStatus = stackInfo.getStatus();
        String statusMessage = stackInfo.getStatusMessage();

        if (heatStatus == HeatStatus.INIT  ||  heatStatus == HeatStatus.BUILDING) {
            vduStatus.setState(VduStateType.INSTANTIATING);
            vduStatus.setLastAction((new PluginAction ("create", "in_progress", statusMessage)));
        }
        else if (heatStatus == HeatStatus.NOTFOUND) {
            vduStatus.setState(VduStateType.NOTFOUND);
        }
        else if (heatStatus == HeatStatus.CREATED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
            vduStatus.setLastAction((new PluginAction ("create", "complete", statusMessage)));
        }
        else if (heatStatus == HeatStatus.UPDATED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
            vduStatus.setLastAction((new PluginAction ("update", "complete", statusMessage)));
        }
        else if (heatStatus == HeatStatus.UPDATING) {
            vduStatus.setState(VduStateType.UPDATING);
            vduStatus.setLastAction((new PluginAction ("update", "in_progress", statusMessage)));
        }
        else if (heatStatus == HeatStatus.DELETING) {
            vduStatus.setState(VduStateType.DELETING);
            vduStatus.setLastAction((new PluginAction ("delete", "in_progress", statusMessage)));
        }
        else if (heatStatus == HeatStatus.FAILED) {
            vduStatus.setState(VduStateType.FAILED);
            vduStatus.setErrorMessage(stackInfo.getStatusMessage());
        } else {
            vduStatus.setState(VduStateType.UNKNOWN);
        }

        return vduStatus;
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.debug ("Thread interrupted while sleeping", e);
            Thread.currentThread().interrupt();
        }
    }
}
