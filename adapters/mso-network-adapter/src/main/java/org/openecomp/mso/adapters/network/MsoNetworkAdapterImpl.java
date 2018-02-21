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

package org.openecomp.mso.adapters.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import org.openecomp.mso.adapters.network.exceptions.NetworkException;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.NetworkInfo;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.NetworkStatus;
import org.openecomp.mso.openstack.beans.Pool;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.Subnet;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.openstack.utils.MsoHeatUtilsWithUpdate;
import org.openecomp.mso.openstack.utils.MsoNeutronUtils;
import org.openecomp.mso.openstack.utils.MsoNeutronUtils.NetworkType;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import static org.openecomp.mso.openstack.utils.MsoCommonUtils.isNullOrEmpty;

@WebService(serviceName = "NetworkAdapter", endpointInterface = "org.openecomp.mso.adapters.network.MsoNetworkAdapter", targetNamespace = "http://org.openecomp.mso/network")
public class MsoNetworkAdapterImpl implements MsoNetworkAdapter {

	MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();

	CloudConfigFactory cloudConfigFactory=new CloudConfigFactory();

	private static final String AIC3_NW_PROPERTY= "org.openecomp.mso.adapters.network.aic3nw";
	private static final String AIC3_NW="OS::ContrailV2::VirtualNetwork";
	public static final String MSO_PROP_NETWORK_ADAPTER="MSO_PROP_NETWORK_ADAPTER";
    private static final String VLANS = "vlans";
    private static final String PHYSICAL_NETWORK = "physical_network";
    private static final String UPDATE_NETWORK_CONTEXT = "UpdateNetwork";
    private static final String NETWORK_ID = "network_id";
    private static final String NETWORK_FQDN = "network_fqdn";
    private static final String CREATE_NETWORK_CONTEXT = "CreateNetwork";
    private static final String MSO_CONFIGURATION_ERROR = "MsoConfigurationError";
    private static final String NEUTRON_MODE = "NEUTRON";
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    protected CloudConfig cloudConfig;

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck () {
        LOGGER.debug ("Health check call in Network Adapter");
    }

    /**
     * Do not use this constructor or the msoPropertiesFactory will be NULL.
     *
  	 * @see MsoNetworkAdapterImpl#MsoNetworkAdapterImpl(MsoPropertiesFactory)
     */
    public MsoNetworkAdapterImpl() {
    }

    /**
     * This constructor MUST be used if this class if called with the new operator.
     * @param msoPropFactory

     */
    public MsoNetworkAdapterImpl(MsoPropertiesFactory msoPropFactory,CloudConfigFactory cloudConfigFact) {
    	this.msoPropertiesFactory = msoPropFactory;
    	this.cloudConfigFactory=cloudConfigFact;
    	cloudConfig = cloudConfigFactory.getCloudConfig ();
    }

    @Override
    public void createNetwork (String cloudSiteId,
                               String tenantId,
                               String networkType,
                               String modelCustomizationUuid,
                               String networkName,
                               String physicalNetworkName,
                               List <Integer> vlans,
                               Boolean failIfExists,
                               Boolean backout,
                               List <Subnet> subnets,
                               MsoRequest msoRequest,
                               Holder <String> networkId,
                               Holder <String> neutronNetworkId,
                               Holder <Map <String, String>> subnetIdMap,
                               Holder <NetworkRollback> rollback) throws NetworkException {
    	Holder <String> networkFqdn = new Holder <> ();
        createNetwork (cloudSiteId,
                       tenantId,
                       networkType,
                       modelCustomizationUuid,
                       networkName,
                       physicalNetworkName,
                       vlans,
                       null,
                       null,
                       null,
                       failIfExists,
                       backout,
                       subnets,
                       null,
                       null,
                       msoRequest,
                       networkId,
                       neutronNetworkId,
                       networkFqdn,
                       subnetIdMap,
                       rollback);
    }

    @Override
    public void createNetworkContrail (String cloudSiteId,
                                       String tenantId,
                                       String networkType,
                                       String modelCustomizationUuid,
                                       String networkName,
                                       List <String> routeTargets,
                                       String shared,
                                       String external,
                                       Boolean failIfExists,
                                       Boolean backout,
                                       List <Subnet> subnets,
                                       List <String> policyFqdns,
                                       List<String> routeTableFqdns,
                                       MsoRequest msoRequest,
                                       Holder <String> networkId,
                                       Holder <String> neutronNetworkId,
                                       Holder <String> networkFqdn,
                                       Holder <Map <String, String>> subnetIdMap,
                                       Holder <NetworkRollback> rollback) throws NetworkException {
        createNetwork (cloudSiteId,
                       tenantId,
                       networkType,
                       modelCustomizationUuid,
                       networkName,
                       null,
                       null,
                       routeTargets,
                       shared,
                       external,
                       failIfExists,
                       backout,
                       subnets,
                       policyFqdns,
                       routeTableFqdns,
                       msoRequest,
                       networkId,
                       neutronNetworkId,
                       networkFqdn,
                       subnetIdMap,
                       rollback);
    }

    /**
     * This is the "Create Network" web service implementation.
     * It will create a new Network of the requested type in the specified cloud
     * and tenant. The tenant must exist at the time this service is called.
     *
     * If a network with the same name already exists, this can be considered a
     * success or failure, depending on the value of the 'failIfExists' parameter.
     *
     * There will be a pre-defined set of network types defined in the MSO Catalog.
     * All such networks will have a similar configuration, based on the allowable
     * Openstack networking definitions. This includes basic networks, provider
     * networks (with a single VLAN), and multi-provider networks (one or more VLANs)
     *
     * Initially, all provider networks must be "vlan" type, and multiple segments in
     * a multi-provider network must be multiple VLANs on the same physical network.
     *
     * This service supports two modes of Network creation/update:
     * - via Heat Templates
     * - via Neutron API
     * The network orchestration mode for each network type is declared in its
     * catalog definition. All Heat-based templates must support some subset of
     * the same input parameters: network_name, physical_network, vlan(s).
     *
     * The method returns the network ID and a NetworkRollback object. This latter
     * object can be passed as-is to the rollbackNetwork operation to undo everything
     * that was created. This is useful if a network is successfully created but
     * the orchestration fails on a subsequent operation.
     */

    private void createNetwork (String cloudSiteId,
                               String tenantId,
                               String networkType,
                               String modelCustomizationUuid,
                               String networkName,
                               String physicalNetworkName,
                               List <Integer> vlans,
                               List <String> routeTargets,
                               String shared,
                               String external,
                               Boolean failIfExists,
                               Boolean backout,
                               List <Subnet> subnets,
                               List <String> policyFqdns,
                               List <String> routeTableFqdns,
                               MsoRequest msoRequest,
                               Holder <String> networkId,
                               Holder <String> neutronNetworkId,
                               Holder <String> networkFqdn,
                               Holder <Map <String, String>> subnetIdMap,
                               Holder <NetworkRollback> rollback) throws NetworkException {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("CreateNetwork");

        LOGGER.debug ("*** CREATE Network: " + networkName
                      + " of type "
                      + networkType
                      + " in "
                      + cloudSiteId
                      + "/"
                      + tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Build a default rollback object (no actions performed)
        NetworkRollback networkRollback = new NetworkRollback ();
        networkRollback.setCloudId (cloudSiteId);
        networkRollback.setTenantId (tenantId);
        networkRollback.setMsoRequest (msoRequest);
        networkRollback.setModelCustomizationUuid(modelCustomizationUuid);

        // tenant query is not required here.
        // If the tenant doesn’t exist, the Heat calls will fail anyway (when the HeatUtils try to obtain a token).
        // So this is just catching that error in a bit more obvious way up front.

        cloudConfig = cloudConfigFactory.getCloudConfig ();
        Optional<CloudSite> cloudSiteOpt = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOpt.isPresent())
        {
        	String error = "Configuration Error. Stack " + networkName + " in "
        			+ cloudSiteId
        			+ "/"
        			+ tenantId
        			+ ": "
        			+ " CloudSite does not exist in MSO Configuration";
        	LOGGER.error (MessageEnum.RA_CONFIG_EXC, error, "", "", MsoLogger.ErrorCode.DataError, "Configuration Error");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
        	// Set the detailed error as the Exception 'message'
        	throw new NetworkException (error, MsoExceptionCategory.USERDATA);
        }

        // Get a handle to the Catalog Database
        CatalogDatabase db = getCatalogDB ();

        // Make sure DB connection is always closed
        try {
            NetworkResource networkResource = networkCheck (db,
                                                            startTime,
                                                            networkType,
                                                            modelCustomizationUuid,
                                                            networkName,
                                                            physicalNetworkName,
                                                            vlans,
                                                            routeTargets,
                                                            cloudSiteOpt.get());
            String mode = networkResource.getOrchestrationMode ();
            NetworkType neutronNetworkType = NetworkType.valueOf (networkResource.getNeutronNetworkType ());

            if (NEUTRON_MODE.equals (mode)) {

                // Use an MsoNeutronUtils for all neutron commands
                MsoNeutronUtils neutron = new MsoNeutronUtils (MSO_PROP_NETWORK_ADAPTER, cloudConfigFactory);

                // See if the Network already exists (by name)
                NetworkInfo netInfo = null;
                long queryNetworkStarttime = System.currentTimeMillis ();
                try {
                    netInfo = neutron.queryNetwork (networkName, tenantId, cloudSiteId);
                    LOGGER.recordMetricEvent (queryNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Response successfully received from OpenStack", "OpenStack", "QueryNetwork", null);
                } catch (MsoException me) {
                    LOGGER.recordMetricEvent (queryNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while querying network from OpenStack", "OpenStack", "QueryNetwork", null);
                    LOGGER.error (MessageEnum.RA_QUERY_NETWORK_EXC, networkName, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception while querying network from OpenStack", me);
                    me.addContext (CREATE_NETWORK_CONTEXT);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while querying network from OpenStack");
                    throw new NetworkException (me);
                }

                if (netInfo != null) {
                    // Exists. If that's OK, return success with the network ID.
                    // Otherwise, return an exception.
                    if (failIfExists != null && failIfExists) {
                        String error = "Create Nework: Network " + networkName
                                       + " already exists in "
                                       + cloudSiteId
                                       + "/"
                                       + tenantId
                                       + " with ID " + netInfo.getId();
                        LOGGER.error (MessageEnum.RA_NETWORK_ALREADY_EXIST, networkName, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Network already exists");
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                        throw new NetworkException(error, MsoExceptionCategory.USERDATA);
                    } else {
                        // Populate the outputs from the existing network.
                        networkId.value = netInfo.getId ();
                        neutronNetworkId.value = netInfo.getId ();
                        rollback.value = networkRollback; // Default rollback - no updates performed
                        String msg = "Found Existing network, status=" + netInfo.getStatus () + " for Neutron mode";
                        LOGGER.warn (MessageEnum.RA_NETWORK_ALREADY_EXIST, networkName, cloudSiteId, tenantId, "", "", MsoLogger.ErrorCode.DataError, "Found Existing network, status=" + netInfo.getStatus ());
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, msg);
                    }
                    return;
                }

                long createNetworkStarttime = System.currentTimeMillis ();
                try {
                    netInfo = neutron.createNetwork (cloudSiteId,
                                                     tenantId,
                                                     neutronNetworkType,
                                                     networkName,
                                                     physicalNetworkName,
                                                     vlans);
                    LOGGER.recordMetricEvent (createNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Response successfully received from OpenStack", "OpenStack", "CreateNetwork", null);
                } catch (MsoException me) {
                	me.addContext (CREATE_NETWORK_CONTEXT);
                    LOGGER.recordMetricEvent (createNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with OpenStack", "OpenStack", "CreateNetwork", null);
                	String error = "Create Network: type " + neutronNetworkType
                                   + " in "
                                   + cloudSiteId
                                   + "/"
                                   + tenantId
                                   + ": "
                                   + me;
                    LOGGER.error (MessageEnum.RA_CREATE_NETWORK_EXC, networkName, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception while communicate with OpenStack", me);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);

                    throw new NetworkException (me);
                }

                // Note: ignoring MsoNetworkAlreadyExists because we already checked.

                // If reach this point, network creation is successful.
                // Since directly created via Neutron, networkId tracked by MSO is the same
                // as the neutron network ID.
                networkId.value = netInfo.getId ();
                neutronNetworkId.value = netInfo.getId ();

                networkRollback.setNetworkCreated (true);
                networkRollback.setNetworkId (netInfo.getId ());
                networkRollback.setNeutronNetworkId (netInfo.getId ());
                networkRollback.setNetworkType (networkType);

                LOGGER.debug ("Network " + networkName + " created, id = " + netInfo.getId ());
            } else if ("HEAT".equals (mode)) {

                // Use an MsoHeatUtils for all Heat commands
                MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_NETWORK_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

                //HeatTemplate heatTemplate = db.getHeatTemplate (networkResource.getTemplateId ());
                HeatTemplate heatTemplate = db.getHeatTemplateByArtifactUuidRegularQuery (networkResource.getHeatTemplateArtifactUUID());
                if (heatTemplate == null) {
                    String error = "Network error - undefined Heat Template. Network Type = " + networkType;
                    LOGGER.error (MessageEnum.RA_PARAM_NOT_FOUND, "Heat Template", "Network Type", networkType, "Openstack", "", MsoLogger.ErrorCode.DataError, "Network error - undefined Heat Template. Network Type = " + networkType);
                    alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error); // Alarm on this
                                                                                                     // error,
                                                                                                     // configuration
                                                                                                     // must be fixed
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                    throw new NetworkException (error, MsoExceptionCategory.INTERNAL);
                }

                LOGGER.debug ("Got HEAT Template from DB: " + heatTemplate.toString ());

                // "Fix" the template if it has CR/LF (getting this from Oracle)
                String template = heatTemplate.getHeatTemplate ();
                template = template.replaceAll ("\r\n", "\n");

                boolean aic3template=false;
                String aic3nw = AIC3_NW;
                try {
                	aic3nw = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_NETWORK_ADAPTER).getProperty(AIC3_NW_PROPERTY, AIC3_NW);
        		} catch (MsoPropertiesException e) {
        			String error = "Unable to get properties:" + MSO_PROP_NETWORK_ADAPTER;
        			LOGGER.error (MessageEnum.RA_CONFIG_EXC, error, "", "", MsoLogger.ErrorCode.DataError, "Exception - Unable to get properties", e);
        		}

                if (template.contains(aic3nw))
                	aic3template = true;

                // First, look up to see if the Network already exists (by name).
                // For HEAT orchestration of networks, the stack name will always match the network name
                StackInfo heatStack = null;
                long queryNetworkStarttime = System.currentTimeMillis ();
                try {
                    heatStack = heat.queryStack (cloudSiteId, tenantId, networkName);
                    LOGGER.recordMetricEvent (queryNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Response successfully received from OpenStack", "OpenStack", "QueryNetwork", null);
                } catch (MsoException me) {
                    me.addContext (CREATE_NETWORK_CONTEXT);
                    LOGGER.recordMetricEvent (queryNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while querying stack from OpenStack", "OpenStack", "QueryNetwork", null);
                	String error = "Create Network (heat): query network " + networkName
                                   + " in "
                                   + cloudSiteId
                                   + "/"
                                   + tenantId
                                   + ": "
                                   + me;
                    LOGGER.error (MessageEnum.RA_QUERY_NETWORK_EXC, networkName, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception while querying stack from OpenStack", me);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException (me);
                }

                if (heatStack != null && (heatStack.getStatus () != HeatStatus.NOTFOUND)) {
                    // Stack exists. Return success or error depending on input directive
                    if (failIfExists != null && failIfExists) {
                        String error = "CreateNetwork: Stack " + networkName
                                       + " already exists in "
                                       + cloudSiteId
                                       + "/"
                                       + tenantId
                                       + " as " + heatStack.getCanonicalName();
                        LOGGER.error (MessageEnum.RA_NETWORK_ALREADY_EXIST, networkName, cloudSiteId, tenantId, "", "", MsoLogger.ErrorCode.DataError, "Network already exists");
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                        throw new NetworkException(error, MsoExceptionCategory.USERDATA);
                    } else {
                        // Populate the outputs from the existing stack.
                        networkId.value = heatStack.getCanonicalName ();
                        neutronNetworkId.value = (String) heatStack.getOutputs ().get (NETWORK_ID);
                        rollback.value = networkRollback; // Default rollback - no updates performed
                        if (aic3template)
                        {
                        	networkFqdn.value = (String) heatStack.getOutputs().get(NETWORK_FQDN);
                        }
                        Map <String, Object> outputs = heatStack.getOutputs ();
                        Map <String, String> sMap = new HashMap <> ();
                        if (outputs != null) {
                        	for (String key : outputs.keySet ()) {
                        		if (key != null && key.startsWith ("subnet")) {
                        			if (aic3template) //one subnet_id output
                        			{
                        				 Map <String, String> map = getSubnetUUId(key, outputs, subnets);
                        				 sMap.putAll(map);
                        			}
                        			else //multiples subnet_%aaid% outputs
                        			{
                        				String subnetUUId = (String) outputs.get(key);
                        				sMap.put (key.substring("subnet_id_".length()), subnetUUId);
                        			}
                        		}
                        	}
                        }
                        subnetIdMap.value = sMap;
                        LOGGER.warn (MessageEnum.RA_NETWORK_ALREADY_EXIST, networkName, cloudSiteId, tenantId, "", "", MsoLogger.ErrorCode.DataError, "Found Existing network stack, status=" + heatStack.getStatus ());
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Suc, "Found Existing network stack");
                    }
                    return;
                }

                // Ready to deploy the new Network
                // Build the common set of HEAT template parameters
                Map <String, Object> stackParams = populateNetworkParams (neutronNetworkType,
                                                                          networkName,
                                                                          physicalNetworkName,
                                                                          vlans,
                                                                          routeTargets,
                                                                          shared,
                                                                          external,
                                                                          aic3template);

                // Validate (and update) the input parameters against the DB definition
                // Shouldn't happen unless DB config is wrong, since all networks use same inputs
                // and inputs were already validated.
                try {
                    stackParams = heat.validateStackParams (stackParams, heatTemplate);
                } catch (IllegalArgumentException e) {
                    String error = "Create Network: Configuration Error: " + e.getMessage ();
                    LOGGER.error (MessageEnum.RA_CONFIG_EXC, e.getMessage(), "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception - Create Network, Configuration Error", e);
                    alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error); // Alarm on this
                                                                                                     // error,
                                                                                                     // configuration
                                                                                                     // must be fixed
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
                    // Input parameters were not valid
                    throw new NetworkException (error, MsoExceptionCategory.INTERNAL);
                }

                if (subnets != null) {
                	try {
                		if (aic3template)
                		{
                			template = mergeSubnetsAIC3 (template, subnets, stackParams);
                		}
                		else
                		{
                			template = mergeSubnets (template, subnets);
                		}
                	} catch (MsoException me) {
                		me.addContext (CREATE_NETWORK_CONTEXT);
                		String error = "Create Network (heat): type " + neutronNetworkType
                				+ " in "
                				+ cloudSiteId
                				+ "/"
                				+ tenantId
                				+ ": "
                				+ me;
                		LOGGER.error (MessageEnum.RA_CREATE_NETWORK_EXC, neutronNetworkType.toString(), cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception Create Network, merging subnets", me);
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
                		throw new NetworkException (me);
                	}
                }

                if (policyFqdns != null && !policyFqdns.isEmpty() && aic3template) {
                    try {
                        mergePolicyRefs (policyFqdns, stackParams);
                    } catch (MsoException me) {
                        me.addContext (CREATE_NETWORK_CONTEXT);
                    	String error = "Create Network (heat) mergePolicyRefs type " + neutronNetworkType
                                       + " in "
                                       + cloudSiteId
                                       + "/"
                                       + tenantId
                                       + ": "
                                       + me;
                        LOGGER.error (MessageEnum.RA_CREATE_NETWORK_EXC, neutronNetworkType.toString(), cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception Create Network, merging policyRefs", me);
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
                        throw new NetworkException (me);
                    }
                }

                if (routeTableFqdns != null && !routeTableFqdns.isEmpty() && aic3template) {
                    try {
                        mergeRouteTableRefs (routeTableFqdns, stackParams);
                    } catch (MsoException me) {
                        me.addContext (CREATE_NETWORK_CONTEXT);
                    	String error = "Create Network (heat) mergeRouteTableRefs type " + neutronNetworkType
                                       + " in "
                                       + cloudSiteId
                                       + "/"
                                       + tenantId
                                       + ": "
                                       + me;
                        LOGGER.error (MessageEnum.RA_CREATE_NETWORK_EXC, neutronNetworkType.toString(), cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception Create Network, merging routeTableRefs", me);
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
                        throw new NetworkException (me);
                    }
                }

                // Deploy the network stack
                // Ignore MsoStackAlreadyExists exception because we already checked.
                try {
                	if (backout == null)
                		backout = true;
                    heatStack = heat.createStack (cloudSiteId,
                                                  tenantId,
                                                  networkName,
                                                  template,
                                                  stackParams,
                                                  true,
                                                  heatTemplate.getTimeoutMinutes (),
                                                  null,
                                                  null,
                                                  null,
                                                  backout.booleanValue());
                } catch (MsoException me) {
                    me.addContext (CREATE_NETWORK_CONTEXT);
                	String error = "Create Network (heat): type " + neutronNetworkType
                                   + " in "
                                   + cloudSiteId
                                   + "/"
                                   + tenantId
                                   + ": "
                                   + me;
                    LOGGER.error (MessageEnum.RA_CREATE_NETWORK_EXC, networkName, cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception creating network", me);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException (me);
                }

                // Reach this point if createStack is successful.

                // For Heat-based orchestration, the MSO-tracked network ID is the heat stack,
                // and the neutronNetworkId is the network UUID returned in stack outputs.
                networkId.value = heatStack.getCanonicalName ();
                neutronNetworkId.value = (String) heatStack.getOutputs ().get (NETWORK_ID);
                if (aic3template)
                {
                	networkFqdn.value = (String) heatStack.getOutputs().get(NETWORK_FQDN);
                }
                Map <String, Object> outputs = heatStack.getOutputs ();
                Map <String, String> sMap = new HashMap <> ();
                if (outputs != null) {
                    for (String key : outputs.keySet ()) {
                        if (key != null && key.startsWith ("subnet")) {
                        	if (aic3template) //one subnet output expected
                			{
                				 Map <String, String> map = getSubnetUUId(key, outputs, subnets);
                				 sMap.putAll(map);
                			}
                			else //multiples subnet_%aaid% outputs allowed
                			{
                				String subnetUUId = (String) outputs.get(key);
                				sMap.put (key.substring("subnet_id_".length()), subnetUUId);
                			}
                        }
                    }
                }
                subnetIdMap.value = sMap;

                rollback.value = networkRollback;
                // Populate remaining rollback info and response parameters.
                networkRollback.setNetworkStackId (heatStack.getCanonicalName ());
                networkRollback.setNeutronNetworkId ((String) heatStack.getOutputs ().get (NETWORK_ID));
                networkRollback.setNetworkCreated (true);
                networkRollback.setNetworkType (networkType);

                LOGGER.debug ("Network " + networkName + " successfully created via HEAT");
            }
        } finally {
            db.close ();
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Suc, "Successfully created network");
        return;
    }

    @Override
    public void updateNetwork (String cloudSiteId,
                               String tenantId,
                               String networkType,
                               String modelCustomizationUuid,
                               String networkId,
                               String networkName,
                               String physicalNetworkName,
                               List <Integer> vlans,
                               List <Subnet> subnets,
                               MsoRequest msoRequest,
                               Holder <Map <String, String>> subnetIdMap,
                               Holder <NetworkRollback> rollback) throws NetworkException {
        updateNetwork (cloudSiteId,
                       tenantId,
                       networkType,
                       modelCustomizationUuid,
                       networkId,
                       networkName,
                       physicalNetworkName,
                       vlans,
                       null,
                       null,
                       null,
                       subnets,
                       null,
                       null,
                       msoRequest,
                       subnetIdMap,
                       rollback);

    }

    @Override
    public void updateNetworkContrail (String cloudSiteId,
                                       String tenantId,
                                       String networkType,
                                       String modelCustomizationUuid,
                                       String networkId,
                                       String networkName,
                                       List <String> routeTargets,
                                       String shared,
                                       String external,
                                       List <Subnet> subnets,
                                       List <String> policyFqdns,
                                       List<String> routeTableFqdns,
                                       MsoRequest msoRequest,
                                       Holder <Map <String, String>> subnetIdMap,
                                       Holder <NetworkRollback> rollback) throws NetworkException {
        updateNetwork (cloudSiteId,
                       tenantId,
                       networkType,
                       modelCustomizationUuid,
                       networkId,
                       networkName,
                       null,
                       null,
                       routeTargets,
                       shared,
                       external,
                       subnets,
                       policyFqdns,
                       routeTableFqdns,
                       msoRequest,
                       subnetIdMap,
                       rollback);
    }

    /**
     * This is the "Update Network" web service implementation.
     * It will update an existing Network of the requested type in the specified cloud
     * and tenant. The typical use will be to replace the VLANs with the supplied
     * list (to add or remove a VLAN), but other properties may be updated as well.
     *
     * There will be a pre-defined set of network types defined in the MSO Catalog.
     * All such networks will have a similar configuration, based on the allowable
     * Openstack networking definitions. This includes basic networks, provider
     * networks (with a single VLAN), and multi-provider networks (one or more VLANs).
     *
     * Initially, all provider networks must currently be "vlan" type, and multi-provider
     * networks must be multiple VLANs on the same physical network.
     *
     * This service supports two modes of Network update:
     * - via Heat Templates
     * - via Neutron API
     * The network orchestration mode for each network type is declared in its
     * catalog definition. All Heat-based templates must support some subset of
     * the same input parameters: network_name, physical_network, vlan, segments.
     *
     * The method returns a NetworkRollback object. This object can be passed
     * as-is to the rollbackNetwork operation to undo everything that was updated.
     * This is useful if a network is successfully updated but orchestration
     * fails on a subsequent operation.
     */
    private void updateNetwork (String cloudSiteId,
                               String tenantId,
                               String networkType,
                               String modelCustomizationUuid,
                               String networkId,
                               String networkName,
                               String physicalNetworkName,
                               List <Integer> vlans,
                               List <String> routeTargets,
                               String shared,
                               String external,
                               List <Subnet> subnets,
                               List <String> policyFqdns,
                               List<String> routeTableFqdns,
                               MsoRequest msoRequest,
                               Holder <Map <String, String>> subnetIdMap,
                               Holder <NetworkRollback> rollback) throws NetworkException {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("UpdateNetwork");
        LOGGER.debug ("***UPDATE Network adapter with Network: " + networkName
                + " of type "
                + networkType
                + " in "
                + cloudSiteId
                + "/"
                + tenantId);


        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Build a default rollback object (no actions performed)
        NetworkRollback networkRollback = new NetworkRollback ();
        networkRollback.setCloudId (cloudSiteId);
        networkRollback.setTenantId (tenantId);
        networkRollback.setMsoRequest (msoRequest);

        cloudConfig = cloudConfigFactory.getCloudConfig ();
        Optional<CloudSite> cloudSiteOpt = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSiteOpt.isPresent()) {
        	   String error = "UpdateNetwork: Configuration Error. Stack " + networkName + " in "
                       + cloudSiteId
                       + "/"
                       + tenantId
                       + ": "
                       + " CloudSite does not exist in MSO Configuration";
        	   LOGGER.error (MessageEnum.RA_CONFIG_EXC, error, "Openstack", "", MsoLogger.ErrorCode.DataError, "CloudSite does not exist in MSO Configuration");
               LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
        	   // Set the detailed error as the Exception 'message'
        	   throw new NetworkException (error, MsoExceptionCategory.USERDATA);
        }

        // Get a handle to the Catalog Database
        CatalogDatabase db = getCatalogDB ();

        // Make sure DB connection is always closed
        try {
            NetworkResource networkResource = networkCheck(db,
                    startTime,
                    networkType,
                    modelCustomizationUuid,
                    networkName,
                    physicalNetworkName,
                    vlans,
                    routeTargets,
                    cloudSiteOpt.get());
            String mode = networkResource.getOrchestrationMode();
            NetworkType neutronNetworkType = NetworkType.valueOf(networkResource.getNeutronNetworkType());

            // Use an MsoNeutronUtils for all Neutron commands
            MsoNeutronUtils neutron = new MsoNeutronUtils(MSO_PROP_NETWORK_ADAPTER, cloudConfigFactory);

            if (NEUTRON_MODE.equals(mode)) {

                // Verify that the Network exists
                // For Neutron-based orchestration, the networkId is the Neutron Network UUID.
                NetworkInfo netInfo = null;
                long queryNetworkStarttime = System.currentTimeMillis();
                try {
                    netInfo = neutron.queryNetwork(networkId, tenantId, cloudSiteId);
                    LOGGER.recordMetricEvent(queryNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryNetwork", null);
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    String error = "Update Network (neutron): query " + networkId
                            + " in "
                            + cloudSiteId
                            + "/"
                            + tenantId
                            + ": "
                            + me;
                    LOGGER.recordMetricEvent(queryNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryNetwork", null);
                    LOGGER.error(MessageEnum.RA_QUERY_NETWORK_EXC, networkId, cloudSiteId, tenantId, "OpenStack", "QueryNetwork", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryNetwork", me);
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException(me);
                }

                if (netInfo == null) {
                    String error = "Update Nework: Network " + networkId
                            + " does not exist in "
                            + cloudSiteId
                            + "/"
                            + tenantId;
                    LOGGER.error(MessageEnum.RA_NETWORK_NOT_FOUND, networkId, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Network not found");
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
                    // Does not exist. Throw an exception (can't update a non-existent network)
                    throw new NetworkException(error, MsoExceptionCategory.USERDATA);
                }
                long updateNetworkStarttime = System.currentTimeMillis();
                try {
                    netInfo = neutron.updateNetwork(cloudSiteId,
                            tenantId,
                            networkId,
                            neutronNetworkType,
                            physicalNetworkName,
                            vlans);
                    LOGGER.recordMetricEvent(updateNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "UpdateNetwork", null);
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    String error = "Update Network (neutron): " + networkId
                            + " in "
                            + cloudSiteId
                            + "/"
                            + tenantId
                            + ": "
                            + me;
                    LOGGER.error(MessageEnum.RA_UPDATE_NETWORK_ERR, networkId, cloudSiteId, tenantId, "Openstack", "updateNetwork", MsoLogger.ErrorCode.DataError, "Exception - updateNetwork", me);
                    LOGGER.recordMetricEvent(updateNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "UpdateNetwork", null);
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException(me);
                }

                // Add the network ID and previously queried vlans to the rollback object
                networkRollback.setNetworkId(netInfo.getId());
                networkRollback.setNeutronNetworkId(netInfo.getId());
                networkRollback.setNetworkType(networkType);
                // Save previous parameters
                networkRollback.setNetworkName(netInfo.getName());
                networkRollback.setPhysicalNetwork(netInfo.getProvider());
                networkRollback.setVlans(netInfo.getVlans());

                LOGGER.debug("Network " + networkId + " updated, id = " + netInfo.getId());
            } else if ("HEAT".equals(mode)) {

                // Use an MsoHeatUtils for all Heat commands
                MsoHeatUtilsWithUpdate heat = new MsoHeatUtilsWithUpdate(MSO_PROP_NETWORK_ADAPTER, msoPropertiesFactory, cloudConfigFactory);

                // First, look up to see that the Network already exists.
                // For Heat-based orchestration, the networkId is the network Stack ID.
                StackInfo heatStack = null;
                long queryStackStarttime = System.currentTimeMillis();
                try {
                    heatStack = heat.queryStack(cloudSiteId, tenantId, networkName);
                    LOGGER.recordMetricEvent(queryStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", null);
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    String error = "UpdateNetwork (heat): query " + networkName
                            + " in "
                            + cloudSiteId
                            + "/"
                            + tenantId
                            + ": "
                            + me;
                    LOGGER.recordMetricEvent(queryStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", null);
                    LOGGER.error(MessageEnum.RA_QUERY_NETWORK_EXC, networkId, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Exception - QueryStack", me);
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException(me);
                }

                if (heatStack == null || (heatStack.getStatus() == HeatStatus.NOTFOUND)) {
                    String error = "UpdateNetwork: Stack " + networkName
                            + " does not exist in "
                            + cloudSiteId
                            + "/"
                            + tenantId;
                    LOGGER.error(MessageEnum.RA_NETWORK_NOT_FOUND, networkId, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Network not found");
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
                    // Network stack does not exist. Return an error
                    throw new NetworkException(error, MsoExceptionCategory.USERDATA);
                }

                // Get the previous parameters for rollback
                Map<String, Object> heatParams = heatStack.getParameters();

                String previousNetworkName = (String) heatParams.get("network_name");
                String previousPhysicalNetwork = (String) heatParams.get(PHYSICAL_NETWORK);

                List<Integer> previousVlans = new ArrayList<>();
                String vlansParam = (String) heatParams.get(VLANS);
                if (vlansParam != null) {
                    for (String vlan : vlansParam.split(",")) {
                        try {
                            previousVlans.add(Integer.parseInt(vlan));
                        } catch (NumberFormatException e) {
                            LOGGER.warn(MessageEnum.RA_VLAN_PARSE, networkId, vlansParam, "", "", MsoLogger.ErrorCode.DataError, "Exception - VLAN parse", e);
                        }
                    }
                }
                LOGGER.debug("Update Stack:  Previous VLANS: " + previousVlans);

                // Ready to deploy the updated Network via Heat

                //HeatTemplate heatTemplate = db.getHeatTemplate (networkResource.getTemplateId ());
                HeatTemplate heatTemplate = db.getHeatTemplateByArtifactUuidRegularQuery (networkResource.getHeatTemplateArtifactUUID());
                if (heatTemplate == null) {
                    String error = "Network error - undefined Heat Template. Network Type=" + networkType;
                    LOGGER.error(MessageEnum.RA_PARAM_NOT_FOUND, "Heat Template", "Network Type", networkType, "OpenStack", "getHeatTemplate", MsoLogger.ErrorCode.DataError, "Network error - undefined Heat Template. Network Type=" + networkType);
                    alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
                    throw new NetworkException(error, MsoExceptionCategory.INTERNAL);
                }

                LOGGER.debug("Got HEAT Template from DB: " + heatTemplate.toString());

                // "Fix" the template if it has CR/LF (getting this from Oracle)
                String template = heatTemplate.getHeatTemplate();
                template = template.replaceAll("\r\n", "\n");

                boolean aic3template = false;
                String aic3nw = AIC3_NW;
                try {
                    aic3nw = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_NETWORK_ADAPTER).getProperty(AIC3_NW_PROPERTY, AIC3_NW);
                } catch (MsoPropertiesException e) {
                    String error = "Unable to get properties:" + MSO_PROP_NETWORK_ADAPTER;
                    LOGGER.error(MessageEnum.RA_CONFIG_EXC, error, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - Unable to get properties", e);
                }
                if (template.contains(aic3nw))
                    aic3template = true;

                // Build the common set of HEAT template parameters
                Map<String, Object> stackParams = populateNetworkParams(neutronNetworkType,
                        networkName,
                        physicalNetworkName,
                        vlans,
                        routeTargets,
                        shared,
                        external,
                        aic3template);

                // Validate (and update) the input parameters against the DB definition
                // Shouldn't happen unless DB config is wrong, since all networks use same inputs
                try {
                    stackParams = heat.validateStackParams(stackParams, heatTemplate);
                } catch (IllegalArgumentException e) {
                    String error = "UpdateNetwork: Configuration Error: Network Type=" + networkType;
                    LOGGER.error(MessageEnum.RA_CONFIG_EXC, "Network Type=" + networkType, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - UpdateNetwork: Configuration Error");
                    alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, error);
                    throw new NetworkException(error, MsoExceptionCategory.INTERNAL, e);
                }

                if (subnets != null) {
                    try {
                        if (aic3template) {
                            template = mergeSubnetsAIC3(template, subnets, stackParams);
                        } else {
                            template = mergeSubnets(template, subnets);
                        }
                    } catch (MsoException me) {
                        me.addContext(UPDATE_NETWORK_CONTEXT);
                        String error = "Update Network (heat): type " + neutronNetworkType
                                + " in "
                                + cloudSiteId
                                + "/"
                                + tenantId
                                + ": "
                                + me;
                        LOGGER.error(MessageEnum.RA_UPDATE_NETWORK_ERR, neutronNetworkType.toString(), cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - UpdateNetwork mergeSubnets ", me);
                        LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
                        throw new NetworkException(me);
                    }
                }

                if (policyFqdns != null && aic3template) {
                    try {
                        mergePolicyRefs(policyFqdns, stackParams);
                    } catch (MsoException me) {
                        me.addContext(UPDATE_NETWORK_CONTEXT);
                        String error = "UpdateNetwork (heat) mergePolicyRefs type " + neutronNetworkType
                                + " in "
                                + cloudSiteId
                                + "/"
                                + tenantId
                                + ": "
                                + me;
                        LOGGER.error(MessageEnum.RA_UPDATE_NETWORK_ERR, neutronNetworkType.toString(), cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - UpdateNetwork mergePolicyRefs", me);
                        LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
                        throw new NetworkException(me);
                    }
                }

                if (routeTableFqdns != null && !routeTableFqdns.isEmpty() && aic3template) {
                    try {
                        mergeRouteTableRefs(routeTableFqdns, stackParams);
                    } catch (MsoException me) {
                        me.addContext(UPDATE_NETWORK_CONTEXT);
                        String error = "UpdateNetwork (heat) mergeRouteTableRefs type " + neutronNetworkType
                                + " in "
                                + cloudSiteId
                                + "/"
                                + tenantId
                                + ": "
                                + me;
                        LOGGER.error(MessageEnum.RA_UPDATE_NETWORK_ERR, neutronNetworkType.toString(), cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception - UpdateNetwork mergeRouteTableRefs", me);
                        LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
                        throw new NetworkException(me);
                    }
                }

                // Update the network stack
                // Ignore MsoStackNotFound exception because we already checked.
                long updateStackStarttime = System.currentTimeMillis();
                try {
                    heatStack = heat.updateStack(cloudSiteId,
                            tenantId,
                            networkId,
                            template,
                            stackParams,
                            true,
                            heatTemplate.getTimeoutMinutes());
                    LOGGER.recordMetricEvent(updateStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "UpdateStack", null);
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    String error = "Update Network: " + networkId + " in " + cloudSiteId + "/" + tenantId + ": " + me;
                    LOGGER.recordMetricEvent(updateStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "UpdateStack", null);
                    LOGGER.error(MessageEnum.RA_UPDATE_NETWORK_ERR, networkId, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - update network", me);
                    LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException(me);
                }

                Map<String, Object> outputs = heatStack.getOutputs();
                Map<String, String> sMap = new HashMap<>();
                if (outputs != null) {
                    for (String key : outputs.keySet()) {
                        if (key != null && key.startsWith("subnet")) {
                            if (aic3template) //one subnet output expected
                            {
                                Map<String, String> map = getSubnetUUId(key, outputs, subnets);
                                sMap.putAll(map);
                            } else //multiples subnet_%aaid% outputs allowed
                            {
                                String subnetUUId = (String) outputs.get(key);
                                sMap.put(key.substring("subnet_id_".length()), subnetUUId);
                            }
                        }
                    }
                }
                subnetIdMap.value = sMap;

                // Reach this point if createStack is successful.
                // Populate remaining rollback info and response parameters.
                networkRollback.setNetworkStackId(heatStack.getCanonicalName());
                if(null != outputs) {
                    networkRollback.setNeutronNetworkId((String) outputs.get(NETWORK_ID));
                }
                else {
                    LOGGER.debug("outputs is NULL");
                }
                networkRollback.setNetworkType(networkType);
                // Save previous parameters
                networkRollback.setNetworkName(previousNetworkName);
                networkRollback.setPhysicalNetwork(previousPhysicalNetwork);
                networkRollback.setVlans(previousVlans);

                rollback.value = networkRollback;

                LOGGER.debug("Network " + networkId + " successfully updated via HEAT");
            }
        } finally {
            db.close ();
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully updated network");
        return;
    }

    private NetworkResource networkCheck (CatalogDatabase db,
                                          long startTime,
                                          String networkType,
                                          String modelCustomizationUuid,
                                          String networkName,
                                          String physicalNetworkName,
                                          List <Integer> vlans,
                                          List <String> routeTargets,
                                          CloudSite cloudSite) throws NetworkException {
        // Retrieve the Network Resource definition
        NetworkResource networkResource = null;
		try {
			if (isNullOrEmpty(modelCustomizationUuid)) {
				networkResource = db.getNetworkResource(networkType);
			} else {
				networkResource = db
						.getNetworkResourceByModelCustUuid(modelCustomizationUuid);
			}
			if (networkResource == null) {
				String error = "Create/UpdateNetwork: Unable to get network resource with NetworkType:"
						+ networkType
						+ " or ModelCustomizationUUID:"
						+ modelCustomizationUuid;
				LOGGER.error(MessageEnum.RA_UNKOWN_PARAM,
						"NetworkType/ModelCustomizationUUID", networkType + "/"
								+ modelCustomizationUuid, "OpenStack", "",
						MsoLogger.ErrorCode.DataError,
						"Create/UpdateNetwork: Unknown NetworkType/ModelCustomizationUUID");

				throw new NetworkException(error, MsoExceptionCategory.USERDATA);
			}
			LOGGER.debug("Got Network definition from Catalog: "
					+ networkResource.toString());

			String mode = networkResource.getOrchestrationMode();
			NetworkType neutronNetworkType = NetworkType
					.valueOf(networkResource.getNeutronNetworkType());

			// All Networks are orchestrated via HEAT or Neutron
			if (!("HEAT".equals(mode) || NEUTRON_MODE.equals(mode))) {
				String error = "CreateNetwork: Configuration Error: Network Type = "
						+ networkType;
				LOGGER.error(MessageEnum.RA_NETWORK_ORCHE_MODE_NOT_SUPPORT,
						mode, "OpenStack", "", MsoLogger.ErrorCode.DataError,
						"CreateNetwork: Configuration Error");
				// Alarm on this error, configuration must be fixed
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR,
						MsoAlarmLogger.CRITICAL, error);

				throw new NetworkException(error, MsoExceptionCategory.INTERNAL);
			}

			MavenLikeVersioning aicV = new MavenLikeVersioning();
			aicV.setVersion(cloudSite.getAic_version());
			if ((aicV.isMoreRecentThan(networkResource.getAicVersionMin()) || aicV
					.isTheSameVersion(networkResource.getAicVersionMin())) // aic
																			// >=
																			// min
					&& (aicV.isTheSameVersion(networkResource
							.getAicVersionMax()) || !(aicV
							.isMoreRecentThan(networkResource
									.getAicVersionMax())))) // aic <= max
			{
				LOGGER.debug("Network Type:" + networkType + " VersionMin:"
						+ networkResource.getAicVersionMin() + " VersionMax:"
						+ networkResource.getAicVersionMax()
						+ " supported on Cloud:" + cloudSite.getId()
						+ " with AIC_Version:" + cloudSite.getAic_version());
			} else {
				String error = "Network Type:" + networkType + " Version_Min:"
						+ networkResource.getAicVersionMin() + " Version_Max:"
						+ networkResource.getAicVersionMax()
						+ " not supported on Cloud:" + cloudSite.getId()
						+ " with AIC_Version:" + cloudSite.getAic_version();
				LOGGER.error(MessageEnum.RA_CONFIG_EXC, error, "OpenStack", "",
						MsoLogger.ErrorCode.DataError,
						"Network Type not supported on Cloud");
				LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
						MsoLogger.ResponseCode.DataError, error);
				throw new NetworkException(error, MsoExceptionCategory.USERDATA);
			}

			// Validate the Network parameters.
			String missing = validateNetworkParams(neutronNetworkType,
					networkName, physicalNetworkName, vlans, routeTargets);
			if (!missing.isEmpty()) {
				String error = "Create Network: Missing parameters: " + missing;
				LOGGER.error(MessageEnum.RA_MISSING_PARAM, missing,
						"OpenStack", "", MsoLogger.ErrorCode.DataError,
						"Create Network: Missing parameters");

				throw new NetworkException(error, MsoExceptionCategory.USERDATA);
			}
		} finally {
			db.close();
		}
        return networkResource;
    }

    @Override
    public void queryNetwork (String cloudSiteId,
                              String tenantId,
                              String networkNameOrId,
                              MsoRequest msoRequest,
                              Holder <Boolean> networkExists,
                              Holder <String> networkId,
                              Holder <String> neutronNetworkId,
                              Holder <NetworkStatus> status,
                              Holder <List <Integer>> vlans,
                              Holder <Map <String, String>> subnetIdMap) throws NetworkException {
        queryNetworkInfo(cloudSiteId,
                      tenantId,
                      networkNameOrId,
                      msoRequest,
                      networkExists,
                      networkId,
                      neutronNetworkId,
                      status,
                      vlans,
                      subnetIdMap);
    }

    @Override
    public void queryNetworkContrail (String cloudSiteId,
                                      String tenantId,
                                      String networkNameOrId,
                                      MsoRequest msoRequest,
                                      Holder <Boolean> networkExists,
                                      Holder <String> networkId,
                                      Holder <String> neutronNetworkId,
                                      Holder <NetworkStatus> status,
                                      Holder <List <String>> routeTargets,
                                      Holder <Map <String, String>> subnetIdMap) throws NetworkException {
        queryNetworkInfo(cloudSiteId,
                      tenantId,
                      networkNameOrId,
                      msoRequest,
                      networkExists,
                      networkId,
                      neutronNetworkId,
                      status,
                      null,
                      subnetIdMap);
    }

    /**
     * This is the queryNetworkInfo method. It returns the existence and status of
     * the specified network, along with its Neutron UUID and list of VLANs.
     * This method attempts to find the network using both Heat and Neutron.
     * Heat stacks are first searched based on the provided network name/id.
     * If none is found, the Neutron is directly queried.
     */
    private void queryNetworkInfo(String cloudSiteId,
                              String tenantId,
                              String networkNameOrId,
                              MsoRequest msoRequest,
                              Holder <Boolean> networkExists,
                              Holder <String> networkId,
                              Holder <String> neutronNetworkId,
                              Holder <NetworkStatus> status,
                              Holder <List <Integer>> vlans,
                              Holder <Map <String, String>> subnetIdMap) throws NetworkException {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("QueryNetwork");
        LOGGER.debug ("*** QUERY Network with Network: " + networkNameOrId
                + " in "
                + cloudSiteId
                + "/"
                + tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        if (isNullOrEmpty (cloudSiteId)
            || isNullOrEmpty(tenantId)
            || isNullOrEmpty(networkNameOrId)) {

            String error = "Missing mandatory parameter cloudSiteId, tenantId or networkId";
            LOGGER.error (MessageEnum.RA_MISSING_PARAM, "cloudSiteId or tenantId or networkNameOrId", "OpenStack", "", MsoLogger.ErrorCode.DataError, "Missing mandatory parameter cloudSiteId, tenantId or networkId");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
            throw new NetworkException (error, MsoExceptionCategory.USERDATA);
        }

        cloudConfig = cloudConfigFactory.getCloudConfig();
        Optional<CloudSite> cloudSiteOpt = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOpt.isPresent())
        {
        	String error = "Configuration Error. Stack " + networkNameOrId + " in "
        			+ cloudSiteId
        			+ "/"
        			+ tenantId
        			+ ": "
        			+ " CloudSite does not exist in MSO Configuration";
        	LOGGER.error (MessageEnum.RA_CONFIG_EXC, error, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Configuration Error");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
        	// Set the detailed error as the Exception 'message'
        	throw new NetworkException (error, MsoExceptionCategory.USERDATA);
        }

        // Use MsoNeutronUtils for all NEUTRON commands
        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_NETWORK_ADAPTER,msoPropertiesFactory,cloudConfigFactory);
        MsoNeutronUtils neutron = new MsoNeutronUtils (MSO_PROP_NETWORK_ADAPTER, cloudConfigFactory);

        String mode;
        String neutronId;
        // Try Heat first, since networks may be named the same as the Heat stack
        StackInfo heatStack = null;
        long queryStackStarttime = System.currentTimeMillis ();
        try {
            heatStack = heat.queryStack (cloudSiteId, tenantId, networkNameOrId);
            LOGGER.recordMetricEvent (queryStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", null);
        } catch (MsoException me) {
        	me.addContext ("QueryNetwork");
            String error = "Query Network (heat): " + networkNameOrId
                           + " in "
                           + cloudSiteId
                           + "/"
                           + tenantId
                           + ": "
                           + me;
            LOGGER.recordMetricEvent (queryStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "BPMN", "QueryStack", null);
            LOGGER.error (MessageEnum.RA_QUERY_NETWORK_EXC, networkNameOrId, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Exception - Query Network (heat)", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new NetworkException (me);
        }

        // Populate the outputs based on the returned Stack information
        if (heatStack != null && heatStack.getStatus () != HeatStatus.NOTFOUND) {
            // Found it. Get the neutronNetworkId for further query
            Map <String, Object> outputs = heatStack.getOutputs ();
            neutronId = (String) outputs.get (NETWORK_ID);
            mode = "HEAT";

            Map <String, String> sMap = new HashMap <> ();
            if (outputs != null) {
            	for (String key : outputs.keySet ()) {
            		if (key != null && key.startsWith ("subnet_id_")) //multiples subnet_%aaid% outputs
            		{
            			String subnetUUId = (String) outputs.get(key);
            			sMap.put (key.substring("subnet_id_".length()), subnetUUId);
            		}
            		else if (key != null && key.startsWith ("subnet")) //one subnet output expected
            		{
            			Map <String, String> map = getSubnetUUId(key, outputs, null);
            			sMap.putAll(map);
            		}

            	}
            }
            subnetIdMap.value = sMap;
        } else {
            // Input ID was not a Heat stack ID. Try it directly in Neutron
            neutronId = networkNameOrId;
            mode = NEUTRON_MODE;
        }

        // Query directly against the Neutron Network for the details
        // no RouteTargets available for ContrailV2 in neutron net-show
        // networkId is heatStackId
        long queryNetworkStarttime = System.currentTimeMillis ();
        try {
            NetworkInfo netInfo = neutron.queryNetwork (neutronId, tenantId, cloudSiteId);
            LOGGER.recordMetricEvent (queryNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryNetwork", null);
            if (netInfo != null) {
                // Found. Populate the output elements
                networkExists.value = Boolean.TRUE;
                if ("HEAT".equals (mode)) {
                    networkId.value = heatStack.getCanonicalName ();
                } else {
                    networkId.value = netInfo.getId ();
                }
                neutronNetworkId.value = netInfo.getId ();
                status.value = netInfo.getStatus ();
                if (vlans != null)
                	vlans.value = netInfo.getVlans ();

                LOGGER.debug ("Network " + networkNameOrId
                              + " found ("
                              + mode
                              + "), ID = "
                              + networkId.value
                              + ("HEAT".equals (mode) ? ",NeutronId = " + neutronNetworkId.value : ""));
            } else {
                // Not found. Populate the status fields, leave the rest null
                networkExists.value = Boolean.FALSE;
                status.value = NetworkStatus.NOTFOUND;
                neutronNetworkId.value = null;
                if (vlans != null)
                	vlans.value = new ArrayList<>();

                LOGGER.debug ("Network " + networkNameOrId + " not found");
            }
        } catch (MsoException me) {
            me.addContext ("QueryNetwork");
            String error = "Query Network (neutron): " + networkNameOrId
                           + " in "
                           + cloudSiteId
                           + "/"
                           + tenantId
                           + ": "
                           + me;
            LOGGER.recordMetricEvent (queryNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryNetwork", null);
            LOGGER.error (MessageEnum.RA_QUERY_NETWORK_EXC, networkNameOrId, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - Query Network (neutron)", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new NetworkException (me);
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully queried network");
        return;
    }

    /**
     * This is the "Delete Network" web service implementation.
     * It will delete a Network in the specified cloud and tenant.
     *
     * If the network is not found, it is treated as a success.
     *
     * This service supports two modes of Network creation/update/delete:
     * - via Heat Templates
     * - via Neutron API
     * The network orchestration mode for each network type is declared in its
     * catalog definition.
     *
     * For Heat-based orchestration, the networkId should be the stack ID.
     * For Neutron-based orchestration, the networkId should be the Neutron network UUID.
     *
     * The method returns nothing on success. Rollback is not possible for delete
     * commands, so any failure on delete will require manual fallout in the client.
     */
    @Override
    public void deleteNetwork (String cloudSiteId,
                               String tenantId,
                               String networkType,
                               String modelCustomizationUuid,
                               String networkId,
                               MsoRequest msoRequest,
                               Holder <Boolean> networkDeleted) throws NetworkException {
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("DeleteNetwork");
        LOGGER.debug ("*** DELETE Network adapter with Network: " + networkId
                                      + " in "
                                      + cloudSiteId
                                      + "/"
                                      + tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Get a handle to the Catalog Database
        CatalogDatabase db = getCatalogDB ();

        // Make sure DB connection is always closed
        try {
            if (isNullOrEmpty (cloudSiteId)
                            || isNullOrEmpty(tenantId)
                            || isNullOrEmpty(networkId)) {
                String error = "Missing mandatory parameter cloudSiteId, tenantId or networkId";
                LOGGER.error (MessageEnum.RA_MISSING_PARAM, "cloudSiteId or tenantId or networkId", "Openstack", "", MsoLogger.ErrorCode.DataError, "Missing mandatory parameter cloudSiteId, tenantId or networkId");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
                throw new NetworkException (error, MsoExceptionCategory.USERDATA);
            }

            // Retrieve the Network Resource definition
            NetworkResource networkResource = null;
            if (isNullOrEmpty(modelCustomizationUuid)) {
                networkResource = db.getNetworkResource (networkType);
            }
            else if (!isNullOrEmpty(networkType))
            {
                networkResource = db.getNetworkResourceByModelCustUuid(modelCustomizationUuid);
            }
            String mode = "";
            if (networkResource != null) {
                LOGGER.debug ("Got Network definition from Catalog: " + networkResource.toString ());

                mode = networkResource.getOrchestrationMode ();
            }

            if (NEUTRON_MODE.equals (mode)) {

                // Use MsoNeutronUtils for all NEUTRON commands
                MsoNeutronUtils neutron = new MsoNeutronUtils (MSO_PROP_NETWORK_ADAPTER, cloudConfigFactory);
                long deleteNetworkStarttime = System.currentTimeMillis ();
                try {
                    // The deleteNetwork function in MsoNeutronUtils returns success if the network
                    // was not found. So don't bother to query first.
                    boolean deleted = neutron.deleteNetwork (networkId, tenantId, cloudSiteId);
                    LOGGER.recordMetricEvent (deleteNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteNetwork", null);
                    networkDeleted.value = deleted;
                } catch (MsoException me) {
                    me.addContext ("DeleteNetwork");
                	String error = "Delete Network (neutron): " + networkId
                                   + " in "
                                   + cloudSiteId
                                   + "/"
                                   + tenantId
                                   + ": "
                                   + me;
                    LOGGER.recordMetricEvent (deleteNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteNetwork", null);
                    LOGGER.error (MessageEnum.RA_DELETE_NETWORK_EXC, networkId, cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Delete Network (neutron)", me);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException (me);
                }
            } else { // DEFAULT to ("HEAT".equals (mode))
                long deleteStackStarttime = System.currentTimeMillis ();
                // Use MsoHeatUtils for all HEAT commands
                MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_NETWORK_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

                try {
                    // The deleteStack function in MsoHeatUtils returns NOTFOUND if the stack was not found or if the stack was deleted.
                    //  So query first to report back if stack WAS deleted or just NOTOFUND
                	StackInfo heatStack = null;
                	heatStack = heat.queryStack(cloudSiteId, tenantId, networkId);
                	if (heatStack != null && heatStack.getStatus() != HeatStatus.NOTFOUND)
                	{
                		heat.deleteStack (tenantId, cloudSiteId, networkId, true);
                		LOGGER.recordMetricEvent (deleteStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteStack", null);
                		networkDeleted.value = true;
                	}
                	else
                	{
                		networkDeleted.value = false;
                	}
                } catch (MsoException me) {
                    me.addContext ("DeleteNetwork");
                	String error = "Delete Network (heat): " + networkId
                                   + " in "
                                   + cloudSiteId
                                   + "/"
                                   + tenantId
                                   + ": "
                                   + me;
                    LOGGER.recordMetricEvent (deleteStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteStack", null);
                    LOGGER.error (MessageEnum.RA_DELETE_NETWORK_EXC, networkId, cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Delete Network (heat)", me);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                    throw new NetworkException (me);
                }
            }
        } finally {
            db.close ();
        }

        // On success, nothing is returned.
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully deleted network");
        return;
    }

    public CatalogDatabase getCatalogDB() {
        return CatalogDatabase.getInstance();
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
    public void rollbackNetwork (NetworkRollback rollback) throws NetworkException {
        MsoLogger.setServiceName ("RollbackNetwork");
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        if (rollback == null) {
        	LOGGER.error (MessageEnum.RA_ROLLBACK_NULL, "Openstack", "", MsoLogger.ErrorCode.DataError, "rollback is null");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "No action to perform");
            return;
        }

        MsoLogger.setLogContext (rollback.getMsoRequest());

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudId ();
        String tenantId = rollback.getTenantId ();
        String networkId = rollback.getNetworkStackId ();
        String networkType = rollback.getNetworkType ();
        String modelCustomizationUuid = rollback.getModelCustomizationUuid();

        LOGGER.debug ("*** ROLLBACK Network " + networkId + " in " + cloudSiteId + "/" + tenantId);

        // rollback may be null (e.g. if network already existed when Create was called)
        // Get a handle to the Catalog Database
        CatalogDatabase db = getCatalogDB ();

        // Make sure DB connection is always closed
        try {

            // Retrieve the Network Resource definition
            NetworkResource networkResource = null;
            if (isNullOrEmpty(modelCustomizationUuid)) {
                networkResource = db.getNetworkResource (networkType);
            }
            else
            {
                networkResource = db.getNetworkResourceByModelCustUuid(modelCustomizationUuid);
            }
            String mode = "";
            if (networkResource != null) {

                LOGGER.debug ("Got Network definition from Catalog: " + networkResource.toString ());

                mode = networkResource.getOrchestrationMode ();
            }

            if (rollback.getNetworkCreated ()) {
                // Rolling back a newly created network, so delete it.
                if (NEUTRON_MODE.equals (mode)) {
                    // Use MsoNeutronUtils for all NEUTRON commands
                    MsoNeutronUtils neutron = new MsoNeutronUtils (MSO_PROP_NETWORK_ADAPTER, cloudConfigFactory);
                    long deleteNetworkStarttime = System.currentTimeMillis ();
                    try {
                        // The deleteNetwork function in MsoNeutronUtils returns success if the network
                        // was not found. So don't bother to query first.
                        neutron.deleteNetwork (networkId, tenantId, cloudSiteId);
                        LOGGER.recordMetricEvent (deleteNetworkStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteNetwork", null);
                    } catch (MsoException me) {
                        me.addContext ("RollbackNetwork");
                        String error = "Rollback Network (neutron): " + networkId
                                       + " in "
                                       + cloudSiteId
                                       + "/"
                                       + tenantId
                                       + ": "
                                       + me;
                        LOGGER.recordMetricEvent (deleteNetworkStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteNetwork", null);
                        LOGGER.error (MessageEnum.RA_DELETE_NETWORK_EXC, networkId, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - Rollback Network (neutron)", me);
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                        throw new NetworkException (me);
                    }
                } else { // DEFAULT to if ("HEAT".equals (mode))
                    // Use MsoHeatUtils for all HEAT commands
                    MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_NETWORK_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
                    long deleteStackStarttime = System.currentTimeMillis ();
                    try {
                        // The deleteStack function in MsoHeatUtils returns success if the stack
                        // was not found. So don't bother to query first.
                        heat.deleteStack (tenantId, cloudSiteId, networkId, true);
                        LOGGER.recordMetricEvent (deleteStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteStack", null);
                    } catch (MsoException me) {
                        me.addContext ("RollbackNetwork");
                        String error = "Rollback Network (heat): " + networkId
                                       + " in "
                                       + cloudSiteId
                                       + "/"
                                       + tenantId
                                       + ": "
                                       + me;
                        LOGGER.recordMetricEvent (deleteStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteStack", null);
                        LOGGER.error (MessageEnum.RA_DELETE_NETWORK_EXC, networkId, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - Rollback Network (heat)", me);
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                        throw new NetworkException (me);
                    }
                }
            }
        } finally {
            db.close ();
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully rolled back network");
        return;
    }

    private String validateNetworkParams (NetworkType neutronNetworkType,
                                          String networkName,
                                          String physicalNetwork,
                                          List <Integer> vlans,
                                          List <String> routeTargets) {
        String sep = "";
        StringBuilder missing = new StringBuilder ();
        if (isNullOrEmpty(networkName)) {
            missing.append ("networkName");
            sep = ",";
        }

        if (neutronNetworkType == NetworkType.PROVIDER || neutronNetworkType == NetworkType.MULTI_PROVIDER) {
            if (isNullOrEmpty(physicalNetwork)) {
                missing.append (sep).append ("physicalNetworkName");
                sep = ",";
            }
            if (vlans == null || vlans.isEmpty ()) {
                missing.append (sep).append (VLANS);
            }
        }

        return missing.toString ();
    }

    private Map <String, Object> populateNetworkParams (NetworkType neutronNetworkType,
                                                        String networkName,
                                                        String physicalNetwork,
                                                        List <Integer> vlans,
                                                        List <String> routeTargets,
                                                        String shared,
                                                        String external,
                                                        boolean aic3template) {
        // Build the common set of HEAT template parameters
        Map <String, Object> stackParams = new HashMap <> ();
        stackParams.put ("network_name", networkName);

        if (neutronNetworkType == NetworkType.PROVIDER) {
            // For Provider type
            stackParams.put (PHYSICAL_NETWORK, physicalNetwork);
            stackParams.put ("vlan", vlans.get (0).toString ());
        } else if (neutronNetworkType == NetworkType.MULTI_PROVIDER) {
            // For Multi-provider, PO supports a custom resource extension of ProviderNet.
            // It supports all ProviderNet properties except segmentation_id, and adds a
            // comma-separated-list of VLANs as a "segments" property.
            // Note that this does not match the Neutron definition of Multi-Provider network,
            // which contains a list of 'segments', each having physical_network, network_type,
            // and segmentation_id.
            StringBuilder buf = new StringBuilder ();
            String sep = "";
            for (Integer vlan : vlans) {
                buf.append (sep).append (vlan.toString ());
                sep = ",";
            }
            String csl = buf.toString ();

            stackParams.put (PHYSICAL_NETWORK, physicalNetwork);
            stackParams.put (VLANS, csl);
        }
        if (routeTargets != null && !routeTargets.isEmpty()) {
            StringBuilder buf = new StringBuilder ();
            String sep = "";
            for (String rt : routeTargets) {
            	if (!isNullOrEmpty(rt))
            	{
            		if (aic3template)
            			buf.append (sep).append ("target:" + rt);
            		else
            			buf.append (sep).append (rt);

            		sep = ",";
            	}
            }
            String csl = buf.toString ();

            stackParams.put ("route_targets", csl);
        }
        if (isNullOrEmpty(shared)) {
            stackParams.put ("shared", "False");
        } else {
            stackParams.put ("shared", shared);
        }
        if (isNullOrEmpty(external)) {
            stackParams.put ("external", "False");
        } else {
            stackParams.put ("external", external);
        }
        return stackParams;
    }



    /** policyRef_list structure in stackParams
    [
     {
         "network_policy_refs_data_sequence": {
             "network_policy_refs_data_sequence_major": "1",
             "network_policy_refs_data_sequence_minor": "0"
         }
     },
     {
         "network_policy_refs_data_sequence": {
             "network_policy_refs_data_sequence_major": "2",
             "network_policy_refs_data_sequence_minor": "0"
         }
     }
 	]
    **/
    private void mergePolicyRefs(List <String> pFqdns, Map <String, Object> stackParams) throws MsoException {
		//Resource Property
		List<ContrailPolicyRef> prlist =  new ArrayList <> ();
		int index = 1;
		for (String pf : pFqdns) {
			if (!isNullOrEmpty(pf))
			{
				ContrailPolicyRef pr = new ContrailPolicyRef();
				pr.populate(String.valueOf(index), "0");
				index++;
				LOGGER.debug("Contrail PolicyRefs Data:" + pr.toString());
				prlist.add(pr);
			}
		}

		JsonNode node = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.convertValue(prlist, JsonNode.class);
			String jsonString = mapper.writeValueAsString(prlist);
			LOGGER.debug("Json PolicyRefs Data:" + jsonString);
		}
		catch (Exception e)
		{
			String error = "Error creating JsonNode for policyRefs Data";
			LOGGER.error (MessageEnum.RA_MARSHING_ERROR, error, "Openstack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception creating JsonNode for policyRefs Data", e);
			throw new MsoAdapterException (error);
		}
		//update parameters
		if (pFqdns != null && node != null)
		{
			StringBuilder buf = new StringBuilder ();
			String sep = "";
			for (String pf : pFqdns) {
				if (!isNullOrEmpty(pf))
				{
					buf.append (sep).append (pf);
					sep = ",";
				}
			}
			String csl = buf.toString ();
			stackParams.put ("policy_refs", csl);
			stackParams.put ("policy_refsdata", node);
		}

		LOGGER.debug ("StackParams updated with policy refs");
		return;
    }

    private void mergeRouteTableRefs(List <String> rtFqdns, Map <String, Object> stackParams) throws MsoException {

		//update parameters
		if (rtFqdns != null)
		{
			StringBuilder buf = new StringBuilder ();
			String sep = "";
			for (String rtf : rtFqdns) {
				if (!isNullOrEmpty(rtf))
				{
					buf.append (sep).append (rtf);
					sep = ",";
				}
			}
			String csl = buf.toString ();
			stackParams.put ("route_table_refs", csl);
		}

		LOGGER.debug ("StackParams updated with route_table refs");
		return;
    }


    /*** Subnet Output structure from Juniper
     {
    "ipam_subnets": [
        {
            "subnet": {
                "ip_prefix": "10.100.1.0",
                "ip_prefix_len": 28
            },
            "addr_from_start": null,
            "enable_dhcp": false,
            "default_gateway": "10.100.1.1",
            "dns_nameservers": [],
            "dhcp_option_list": null,
            "subnet_uuid": "10391fbf-6b9c-4160-825d-2d018b7649cf",
            "allocation_pools": [
                {
                    "start": "10.100.1.3",
                    "end": "10.100.1.5"
                },
                {
                    "start": "10.100.1.6",
                    "end": "10.100.1.9"
                }
            ],
            "host_routes": null,
            "dns_server_address": "10.100.1.13",
            "subnet_name": "subnet_MsoNW1_692c9032-e1a2-4d64-828c-7b9a4fcc05b0"
        },
        {
            "subnet": {
                "ip_prefix": "10.100.2.16",
                "ip_prefix_len": 28
            },
            "addr_from_start": null,
            "enable_dhcp": true,
            "default_gateway": "10.100.2.17",
            "dns_nameservers": [],
            "dhcp_option_list": null,
            "subnet_uuid": "c7aac5ea-66fe-443a-85f9-9c38a608c0f6",
            "allocation_pools": [
                {
                    "start": "10.100.2.18",
                    "end": "10.100.2.20"
                }
            ],
            "host_routes": null,
            "dns_server_address": "10.100.2.29",
            "subnet_name": "subnet_MsoNW1_692c9032-e1a2-4d64-828c-7b9a4fcc05b1"
        }
    ],
    "host_routes": null
	}
    ***/
    private String mergeSubnetsAIC3 (String heatTemplate, List <Subnet> subnets, Map <String, Object> stackParams) throws MsoException {

		//Resource Property
		List<ContrailSubnet> cslist =  new ArrayList <> ();
		for (Subnet subnet : subnets) {
			ContrailSubnet cs = new ContrailSubnet();
			LOGGER.debug("Input Subnet:" + subnet.toString());
			cs.populateWith(subnet);
			LOGGER.debug("Contrail Subnet:" + cs.toString());
			cslist.add(cs);
		}

		JsonNode node = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.convertValue(cslist, JsonNode.class);
			String jsonString = mapper.writeValueAsString(cslist);
			LOGGER.debug("Json Subnet List:" + jsonString);
		}
		catch (Exception e)
		{
			String error = "Error creating JsonNode from input subnets";
			LOGGER.error (MessageEnum.RA_MARSHING_ERROR, error, "", "", MsoLogger.ErrorCode.DataError, "Exception creating JsonNode from input subnets", e);
			throw new MsoAdapterException (error);
		}
		//update parameters
		if (node != null)
		{
			stackParams.put ("subnet_list", node);
		}
		//Outputs - All subnets are in one ipam_subnets structure
		String outputTempl = "  subnet:\n" + "    description: Openstack subnet identifier\n"
				+ "    value: { get_attr: [network, network_ipam_refs, 0, attr]}\n";

		// append outputs in heatTemplate
		int outputsIdx = heatTemplate.indexOf ("outputs:");
		heatTemplate = insertStr (heatTemplate, outputTempl, outputsIdx + 8);
		LOGGER.debug ("Template updated with all AIC3.0 subnets:" + heatTemplate);
		return heatTemplate;
    }


    private String mergeSubnets (String heatTemplate, List <Subnet> subnets) throws MsoException {

    		String resourceTempl = "  subnet_%subnetId%:\n" + "    type: OS::Neutron::Subnet\n"
    				+ "    properties:\n"
    				+ "      name: %name%\n"
    				+ "      network_id: { get_resource: network }\n"
    				+ "      cidr: %cidr%\n";

    		/* make these optional
                               + "      ip_version: %ipversion%\n"
                               + "      enable_dhcp: %enabledhcp%\n"
                               + "      gateway_ip: %gatewayip%\n"
                               + "      allocation_pools:\n"
                               + "       - start: %poolstart%\n"
                               + "         end: %poolend%\n";

    		 */

    		String outputTempl = "  subnet_id_%subnetId%:\n" + "    description: Openstack subnet identifier\n"
    				+ "    value: {get_resource: subnet_%subnetId%}\n";

    		StringBuilder curR;
    		String curO;
    		StringBuilder resourcesBuf = new StringBuilder ();
    		StringBuilder outputsBuf = new StringBuilder ();
    		for (Subnet subnet : subnets) {

    			// build template for each subnet
    			curR = new StringBuilder(resourceTempl);
    			if (subnet.getSubnetId () != null) {
    				curR = new StringBuilder(curR.toString().replace("%subnetId%", subnet.getSubnetId()));
    			} else {
    				String error = "Missing Required AAI SubnetId for subnet in HEAT Template";
    				LOGGER.error (MessageEnum.RA_MISSING_PARAM, error, "Openstack", "", MsoLogger.ErrorCode.DataError, "Missing Required AAI ID  for subnet in HEAT Template");
    				throw new MsoAdapterException (error);
    			}

    			if (subnet.getSubnetName () != null) {
    				curR = new StringBuilder(curR.toString().replace("%name%", subnet.getSubnetName()));
    			} else {
    				curR = new StringBuilder(curR.toString().replace("%name%", subnet.getSubnetId()));
    			}

    			if (subnet.getCidr () != null) {
    				curR = new StringBuilder(curR.toString().replace("%cidr%", subnet.getCidr()));
    			} else {
    				String error = "Missing Required cidr for subnet in HEAT Template";
    				LOGGER.error (MessageEnum.RA_MISSING_PARAM, error, "Openstack", "", MsoLogger.ErrorCode.DataError, "Missing Required cidr for subnet in HEAT Template");
    				throw new MsoAdapterException (error);
    			}

    			if (subnet.getIpVersion () != null) {
    				curR.append("      ip_version: " + subnet.getIpVersion() + "\n");
    			}
    			if (subnet.getEnableDHCP () != null) {
    				curR.append("      enable_dhcp: ").append(Boolean.toString(subnet.getEnableDHCP())).append("\n");
    			}
    			if (subnet.getGatewayIp () != null && !subnet.getGatewayIp ().isEmpty() ) {
    				curR.append("      gateway_ip: " + subnet.getGatewayIp() + "\n");
    			}

    			if (subnet.getAllocationPools() != null) {
    				curR.append("      allocation_pools:\n");
    				for (Pool pool : subnet.getAllocationPools())
    				{
    					if (!isNullOrEmpty(pool.getStart()) && !isNullOrEmpty(pool.getEnd()))
    					{
    						curR.append("       - start: " + pool.getStart() + "\n");
    						curR.append("         end: " + pool.getEnd() + "\n");
    					}
    				}
    			}

    			resourcesBuf.append (curR);

    			curO = outputTempl;
    			curO = curO.replace ("%subnetId%", subnet.getSubnetId ());

    			outputsBuf.append (curO);

    		}
    		// append resources and outputs in heatTemplate
    		LOGGER.debug ("Tempate initial:" + heatTemplate);
    		int outputsIdx = heatTemplate.indexOf ("outputs:");
    		heatTemplate = insertStr (heatTemplate, outputsBuf.toString (), outputsIdx + 8);
    		int resourcesIdx = heatTemplate.indexOf ("resources:");
    		heatTemplate = insertStr (heatTemplate, resourcesBuf.toString (), resourcesIdx + 10);

    		LOGGER.debug ("Template updated with all subnets:" + heatTemplate);
    		return heatTemplate;
    }

    private Map <String, String> getSubnetUUId(String key,  Map <String, Object> outputs, List <Subnet> subnets) {

    	Map <String, String> sMap = new HashMap <> ();

    	try{
    		Object obj = outputs.get(key);
    		ObjectMapper mapper = new ObjectMapper();
    		String jStr = mapper.writeValueAsString(obj);
    		LOGGER.debug ("Subnet_Ipam Output JSON String:" + obj.getClass() + " " + jStr);

    		JsonNode rootNode = mapper.readTree(jStr);
    		for (JsonNode sNode : rootNode.path("ipam_subnets"))
    		{
    			LOGGER.debug("Output Subnet Node" + sNode.toString());
    			String name = sNode.path("subnet_name").getTextValue();
    			String uuid = sNode.path("subnet_uuid").getTextValue();
    			String aaiId = name; // default
    			// try to find aaiId for name in input subnetList
    			if (subnets != null)
    			{
    				for (Subnet subnet : subnets)
    				{
    					if ( subnet !=  null && !isNullOrEmpty(subnet.getSubnetName()))
    					{
    						if (subnet.getSubnetName().equals(name))
    						{
    							aaiId = subnet.getSubnetId();
    							break;
    						}
    					}
    				}
    			}
    			sMap.put(aaiId, uuid); //bpmn needs aaid to uuid map
    		}
    	}
    	catch (Exception e)
    	{
    		LOGGER.error (MessageEnum.RA_MARSHING_ERROR, "error getting subnet-uuids", "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception getting subnet-uuids", e);
    	}

    	LOGGER.debug ("Return sMap" + sMap.toString());
    	return sMap;
    }

    private static String insertStr (String template, String snippet, int index) {

        String updatedTemplate;

        LOGGER.debug ("Index:" + index + " Snippet:" + snippet);

        String templateBeg = template.substring (0, index);
        String templateEnd = template.substring (index);

        updatedTemplate = templateBeg + "\n" + snippet + templateEnd;

        LOGGER.debug ("Template updated with a subnet:" + updatedTemplate);
        return updatedTemplate;
    }

}
