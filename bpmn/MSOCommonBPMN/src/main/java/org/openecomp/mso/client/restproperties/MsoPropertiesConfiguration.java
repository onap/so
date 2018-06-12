package org.openecomp.mso.client.restproperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:urn.properties")
public class MsoPropertiesConfiguration {
	
	@Value("${mso.infra.customer.id}")
	private String infraCustomerId;
	
	@Value("${mso.sriov.network.role.portal1}")
	private String sriovNetworkRolePortal1;
	
	@Value("${mso.sriov.network.role.portal2}")
	private String sriovNetworkRolePortal2;
	
	@Value("${mso.sriov.network.role.gateway1}")
	private String sriovNetworkRoleGateway1;
	
	@Value("${mso.sriov.network.role.gateway2}")
	private String sriovNetworkRoleGateway2;
	
	@Value("${mso.oam.network.role.portal}")
	private String oamNetworkRolePortal;
	
	@Value("${mso.oam.network.role.gateway}")
	private String oamNetworkRoleGateway;
	
	@Value("${mso.sniro.auth}")
	private String sniroAuth;
	
	@Value("${mso.sniro.timeout}")
	private String sniroTimeout;
	
	@Value("${mso.portal.service.model.name}")
	private String portalServiceModelName;
	
	@Value("${mso.gateway.service.model.name}")
	private String gatewayServiceModelName;
	
	@Value("${mso.sniro.policies.dhv.2vvig}")
	private String sniroPoliciesDhv2vvig;
	
	@Value("${mso.sniro.policies.dhv.4vvig}")
	private String sniroPoliciesDhv4vvig;
	
	@Value("${mso.service.agnostic.sniro.host}")
	private String serviceAgnosticSniroHost;
	
	@Value("${mso.service.agnostic.sniro.endpoint}")
	private String serviceAgnosticSniroEndpoint;
	
	@Value("${mso.catalog.db.endpoint}")
	private String catalogDbEndpoint;
	
	@Value("${mso.adapters.completemsoprocess.endpoint}")
	private String adaptersCompletemsoprocessEndpoint;
	
	@Value("${mso.adapters.db.endpoint}")
	private String adaptersDbEndpoint;
	
	@Value("${mso.openecomp.adapters.db.endpoint}")
	private String openecompAdaptersDbEndpoint;
	
	@Value("${mso.adapters.openecomp.db.endpoint}")
	private String adaptersOpenecompDbEndpoint;
	
	@Value("${mso.adapters.sdnc.endpoint}")
	private String adaptersSdncEndpoint;
	
	@Value("${mso.adapters.sdnc.rest.endpoint}")
	private String adaptersSdncRestEndpoint;
	
	@Value("${mso.adapters.tenant.endpoint}")
	private String adaptersTenantEndpoint;
	
	@Value("${mso.adapters.workflow.message.endpoint}")
	private String adaptersWorkflowMessageEndpoint;
	
	@Value("${mso.workflow.message.endpoint}")
	private String workflowMessageEndPoint;
	
	@Value("${mso.workflow.sdncadapter.callback}")
	private String workflowSdncadapterCallback;
	
	@Value("${mso.csi.pwd}")
	private String csiPwd;
	
	@Value("${mso.csi.usrname}")
	private String csiUsrname;
	
	@Value("${mso.csi.sendmanagednetworkstatusnotification.version}")
	private String csiSendmanagednetworkstatusnotificationVersion;
	
	@Value("${mso.csi.sendmanagednetworkstatusnotification.applicationname}")
	private String csiSendmanagednetworkstatusnotificationApplicationname;
	
	@Value("${mso.msoKey}")
	private String msoKey;
	
	@Value("${mso.adapters.po.auth}")
	private String adaptersPoAuth;
	
	@Value("${mso.adapters.db.auth}")
	private String adaptersDbAuth;
	
	@Value("${mso.workflow.notification.name}")
	private String workflowNotificationName;
	
	@Value("${mso.sdnc.timeout}")
	private String sdncTimeout;
	
	@Value("${mso.rollback}")
	private String rollback;
	
	@Value("${mso.adapters.network.endpoint}")
	private String adaptersNetworkEndpoint;
	
	@Value("${mso.adapters.network.rest.endpoint}")
	private String adaptersNetworkRestEndpoint;
	
	@Value("${mso.adapters.vnf-async.endpoint}")
	private String adaptersVnfAsyncEndpoint;
	
	@Value("${mso.workflow.vnfadapter.delete.callback}")
	private String workflowVnfadapterDeleteCallback;
	
	@Value("${mso.workflow.vnfadapter.create.callback}")
	private String workflowVnfadapterCreateCallback;
	
	@Value("${mso.adapters.vnf.rest.endpoint}")
	private String adaptersVnfRestEndpoint;
	
	@Value("${mso.adapters.vnf.volume-groups.rest.endpoint}")
	private String adaptersVnfVolumeGroupsRestEndpoint;
	
	@Value("${mso.po.timeout}")
	private String poTimeout;
	
	@Value("${mso.sdnc.firewall.yang.model}")
	private String sdncFirewallYangModel;
	
	@Value("${mso.sdnc.firewall.yang.model.version}")
	private String sdncFirewallYangModelVersion;
	
	@Value("${mso.sdnc.timeout.firewall.minutes}")
	private String sdncTimeoutFirewallMinutes;
	
	@Value("${mso.sdnc.timeout.ucpe.async.hours}")
	private String sdncTimeoutUcpeAsyncHours;
	
	@Value("${mso.sdnc.timeout.ucpe.async.minutes}")
	private String sdncTimeoutUcpeAsyncMinutes;
	
	@Value("${mso.callbackRetryAttempts}")
	private String callbackRetryAttempts;
	
	@Value("${mso.callbackRetrySleepTime}")
	private String callbackRetrySleepTime;
	
	@Value("${mso.use.qualified.host}")
	private String useQualifiedHost;
	
	@Value("${mso.workflow.l3ToHigherLayerAddBonding.model.name}")
	private String workflowL3ToHigherLayerAddBondingModelName;
	
	@Value("${mso.workflow.l3ToHigherLayerAddBonding.model.version}")
	private String workflowL3ToHigherLayerAddBondingModelVersion;
	
	@Value("${mso.sitename}")
	private String sitename;
	
	@Value("${mso.workflow.global.default.aai.namespace}")
	private String workflowGlobalDefaultAaiNamespace;
	
	@Value("${mso.workflow.global.default.aai.version}")
	private String workflowGlobalDefaultAaiVersion;
	
	@Value("${mso.workflow.default.aai.v11.generic-vnf.uri}")
	private String workflowDefaultAaiV11GenericVnfUri;
	
	@Value("${mso.workflow.default.aai.v11.vpn-binding.uri}")
	private String workflowDefaultAaiV11VpnBindingUri;
	
	@Value("${mso.workflow.default.aai.v11.network-policy.uri}")
	private String workflowDefaultAaiV11NetworkPolicyUri;
	
	@Value("${mso.workflow.default.aai.v11.route-table-reference.uri}")
	private String workflowDefaultAaiV11RouteTableReferenceUri;
	
	@Value("${mso.workflow.default.aai.v11.vce.uri}")
	private String workflowDefaultAaiV11VceUri;
	
	@Value("${mso.workflow.default.aai.v11.l3-network.uri}")
	private String workflowDefaultAaiV11L3NetworkUri;
	
	@Value("${mso.workflow.default.aai.v11.customer.uri}")
	private String workflowDefaultAaiV11CustomerUri;
	
	@Value("${mso.workflow.default.aai.v11.tenant.uri}")
	private String workflowDefaultAaiV11TenantUri;
	
	@Value("${mso.workflow.default.aai.v11.generic-query.uri}")
	private String workflowDefaultAaiV11GenericQueryUri;
	
	@Value("${mso.workflow.default.aai.v11.cloud-region.uri}")
	private String workflowDefaultAaiV11CloudRegionUri;
	
	@Value("${mso.workflow.default.aai.v11.nodes-query.uri}")
	private String workflowDefaultAaiV11NodesQueryUri;
	
	@Value("${mso.workflow.default.aai.v11.configuration.uri}")
	private String workflowDefaultAaiV11ConfigurationUri;
	
	@Value("${mso.default.adapter.namespace}")
	private String defaultAdapterNamespace;
	
	@Value("${mso.openecomp.adapter.namespace}")
	private String openecompAdapterNamespace;

	@Value("${mso.adiod.vce.service.model.invariant.uuid}")
	private String adiodVceServiceModelInvariantUuid;

	@Value("${mso.adiod.vpe.service.model.invariant.uuid}")
	private String adiodVpeServiceModelInvariantUuid;

	@Value("${mso.adiod.l3sa.disable.homing}")
	private String adiodL3saDisableHoming;

	@Value("${mso.workflow.default.retry.attempts}")
	private String workflowDefaultRetryAttempts;

	@Value("${mso.bpmn.optimisticlockingexception.retrycount}")
	private String bpmnOptimisticlockingexceptionRetrycount;

	@Value("${mso.bpmn.process.historyTimeToLive}")
	private String bpmnProcessHistoryTimeToLive;

	@Value("${mso.correlation.timeout}")
	private String correlationTimemout;

	public String getInfraCustomerId() {
		return infraCustomerId;
	}

	public String getSriovNetworkRolePortal1() {
		return sriovNetworkRolePortal1;
	}

	public String getSriovNetworkRolePortal2() {
		return sriovNetworkRolePortal2;
	}

	public String getSriovNetworkRoleGateway1() {
		return sriovNetworkRoleGateway1;
	}

	public String getSriovNetworkRoleGateway2() {
		return sriovNetworkRoleGateway2;
	}

	public String getOamNetworkRolePortal() {
		return oamNetworkRolePortal;
	}

	public String getOamNetworkRoleGateway() {
		return oamNetworkRoleGateway;
	}

	public String getSniroAuth() {
		return sniroAuth;
	}

	public String getSniroTimeout() {
		return sniroTimeout;
	}

	public String getPortalServiceModelName() {
		return portalServiceModelName;
	}

	public String getGatewayServiceModelName() {
		return gatewayServiceModelName;
	}

	public String getSniroPoliciesDhv2vvig() {
		return sniroPoliciesDhv2vvig;
	}

	public String getSniroPoliciesDhv4vvig() {
		return sniroPoliciesDhv4vvig;
	}

	public String getServiceAgnosticSniroHost() {
		return serviceAgnosticSniroHost;
	}

	public String getServiceAgnosticSniroEndpoint() {
		return serviceAgnosticSniroEndpoint;
	}

	public String getCatalogDbEndpoint() {
		return catalogDbEndpoint;
	}

	public String getAdaptersCompletemsoprocessEndpoint() {
		return adaptersCompletemsoprocessEndpoint;
	}

	public String getAdaptersDbEndpoint() {
		return adaptersDbEndpoint;
	}

	public String getOpenecompAdaptersDbEndpoint() {
		return openecompAdaptersDbEndpoint;
	}

	public String getAdaptersOpenecompDbEndpoint() {
		return adaptersOpenecompDbEndpoint;
	}

	public String getAdaptersSdncEndpoint() {
		return adaptersSdncEndpoint;
	}

	public String getAdaptersSdncRestEndpoint() {
		return adaptersSdncRestEndpoint;
	}

	public String getAdaptersTenantEndpoint() {
		return adaptersTenantEndpoint;
	}

	public String getAdaptersWorkflowMessageEndpoint() {
		return adaptersWorkflowMessageEndpoint;
	}

	public String getWorkflowMessageEndPoint() {
		return workflowMessageEndPoint;
	}

	public String getWorkflowSdncadapterCallback() {
		return workflowSdncadapterCallback;
	}

	public String getCsiPwd() {
		return csiPwd;
	}

	public String getCsiUsrname() {
		return csiUsrname;
	}

	public String getCsiSendmanagednetworkstatusnotificationVersion() {
		return csiSendmanagednetworkstatusnotificationVersion;
	}

	public String getCsiSendmanagednetworkstatusnotificationApplicationname() {
		return csiSendmanagednetworkstatusnotificationApplicationname;
	}

	public String getMsoKey() {
		return msoKey;
	}

	public String getAdaptersPoAuth() {
		return adaptersPoAuth;
	}

	public String getAdaptersDbAuth() {
		return adaptersDbAuth;
	}

	public String getWorkflowNotificationName() {
		return workflowNotificationName;
	}

	public String getSdncTimeout() {
		return sdncTimeout;
	}

	public String getRollback() {
		return rollback;
	}

	public String getAdaptersNetworkEndpoint() {
		return adaptersNetworkEndpoint;
	}

	public String getAdaptersNetworkRestEndpoint() {
		return adaptersNetworkRestEndpoint;
	}

	public String getAdaptersVnfAsyncEndpoint() {
		return adaptersVnfAsyncEndpoint;
	}

	public String getWorkflowVnfadapterDeleteCallback() {
		return workflowVnfadapterDeleteCallback;
	}

	public String getWorkflowVnfadapterCreateCallback() {
		return workflowVnfadapterCreateCallback;
	}

	public String getAdaptersVnfRestEndpoint() {
		return adaptersVnfRestEndpoint;
	}

	public String getAdaptersVnfVolumeGroupsRestEndpoint() {
		return adaptersVnfVolumeGroupsRestEndpoint;
	}

	public String getPoTimeout() {
		return poTimeout;
	}

	public String getSdncFirewallYangModel() {
		return sdncFirewallYangModel;
	}

	public String getSdncFirewallYangModelVersion() {
		return sdncFirewallYangModelVersion;
	}

	public String getSdncTimeoutFirewallMinutes() {
		return sdncTimeoutFirewallMinutes;
	}

	public String getSdncTimeoutUcpeAsyncHours() {
		return sdncTimeoutUcpeAsyncHours;
	}

	public String getSdncTimeoutUcpeAsyncMinutes() {
		return sdncTimeoutUcpeAsyncMinutes;
	}

	public String getCallbackRetryAttempts() {
		return callbackRetryAttempts;
	}

	public String getCallbackRetrySleepTime() {
		return callbackRetrySleepTime;
	}

	public String getUseQualifiedHost() {
		return useQualifiedHost;
	}

	public String getWorkflowL3ToHigherLayerAddBondingModelName() {
		return workflowL3ToHigherLayerAddBondingModelName;
	}

	public String getWorkflowL3ToHigherLayerAddBondingModelVersion() {
		return workflowL3ToHigherLayerAddBondingModelVersion;
	}

	public String getSitename() {
		return sitename;
	}

	public String getWorkflowGlobalDefaultAaiNamespace() {
		return workflowGlobalDefaultAaiNamespace;
	}

	public String getWorkflowGlobalDefaultAaiVersion() {
		return workflowGlobalDefaultAaiVersion;
	}

	public String getWorkflowDefaultAaiV11GenericVnfUri() {
		return workflowDefaultAaiV11GenericVnfUri;
	}

	public String getWorkflowDefaultAaiV11VpnBindingUri() {
		return workflowDefaultAaiV11VpnBindingUri;
	}

	public String getWorkflowDefaultAaiV11NetworkPolicyUri() {
		return workflowDefaultAaiV11NetworkPolicyUri;
	}

	public String getWorkflowDefaultAaiV11RouteTableReferenceUri() {
		return workflowDefaultAaiV11RouteTableReferenceUri;
	}

	public String getWorkflowDefaultAaiV11VceUri() {
		return workflowDefaultAaiV11VceUri;
	}

	public String getWorkflowDefaultAaiV11L3NetworkUri() {
		return workflowDefaultAaiV11L3NetworkUri;
	}

	public String getWorkflowDefaultAaiV11CustomerUri() {
		return workflowDefaultAaiV11CustomerUri;
	}

	public String getWorkflowDefaultAaiV11TenantUri() {
		return workflowDefaultAaiV11TenantUri;
	}

	public String getWorkflowDefaultAaiV11GenericQueryUri() {
		return workflowDefaultAaiV11GenericQueryUri;
	}

	public String getWorkflowDefaultAaiV11CloudRegionUri() {
		return workflowDefaultAaiV11CloudRegionUri;
	}

	public String getWorkflowDefaultAaiV11NodesQueryUri() {
		return workflowDefaultAaiV11NodesQueryUri;
	}

	public String getWorkflowDefaultAaiV11ConfigurationUri() {
		return workflowDefaultAaiV11ConfigurationUri;
	}

	public String getDefaultAdapterNamespace() {
		return defaultAdapterNamespace;
	}

	public String getOpenecompAdapterNamespace() {
		return openecompAdapterNamespace;
	}

	public String getAdiodVceServiceModelInvariantUuid() {
		return adiodVceServiceModelInvariantUuid;
	}

	public String getAdiodVpeServiceModelInvariantUuid() {
		return adiodVpeServiceModelInvariantUuid;
	}

	public String getAdiodL3saDisableHoming() {
		return adiodL3saDisableHoming;
	}

	public String getWorkflowDefaultRetryAttempts() {
		return workflowDefaultRetryAttempts;
	}

	public String getBpmnOptimisticlockingexceptionRetrycount() {
		return bpmnOptimisticlockingexceptionRetrycount;
	}

	public String getBpmnProcessHistoryTimeToLive() {
		return bpmnProcessHistoryTimeToLive;
	}

	public String getCorrelationTimemout() {
		return correlationTimemout;
	}


}
