/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
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

package org.openecomp.mso.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

/**
 * Reusable Mock StubResponses for AAI Endpoints
 *
 */
public class StubResponseAAI {

	public static void setupAllMocks() {

	}


	/**
	 * Tunnel-XConnect Mock Stub Response
	 */
	public static void MockPutTunnelXConnect(String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId, String tunnelId){
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId + "/tunnel-xconnects/tunnel-xconnect/" + tunnelId))
				.willReturn(aResponse()
						.withStatus(200)));
	}


	/**
	 * Allotted Resource Mock StubResponses below
	 */
	public static void MockPutAllottedResource(String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutAllottedResource_500(String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(500)));
	}


	/**
	 * Service Instance Mock StubResponses below
	 */
	public static void MockGetServiceInstance(String globalCustId, String subscriptionType, String serviceInstanceId, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetServiceInstance_404(String customer, String serviceSubscription, String serviceInstanceId){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId))
						.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockGetServiceInstance_500(String customer, String serviceSubscription, String serviceInstanceId){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId))
						.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockGetServiceInstance_500(String customer, String serviceSubscription, String serviceInstanceId, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId))
						.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockNodeQueryServiceInstanceByName(String serviceInstanceName, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance[&]filter=service-instance-name:EQUALS:" + serviceInstanceName))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockNodeQueryServiceInstanceByName_404(String serviceInstanceName){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-name:EQUALS:" + serviceInstanceName))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockNodeQueryServiceInstanceByName_500(String serviceInstanceName){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-name:EQUALS:" + serviceInstanceName))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockNodeQueryServiceInstanceById(String serviceInstanceId, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance[&]filter=service-instance-id:EQUALS:" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockNodeQueryServiceInstanceById_404(String serviceInstanceId){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-id:EQUALS:" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockNodeQueryServiceInstanceById_500(String serviceInstanceId){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-id:EQUALS:" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockDeleteServiceInstance(String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion){
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(204)));
	}
	
	public static void MockGetServiceInstance(String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion, int statusCode){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(statusCode)));
	}
	
	public static void MockGetServiceInstance(String customer, String serviceSubscription, String resourceVersion, int statusCode){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")));
	}
	
	public static void MockDeleteServiceInstance(String customer, String serviceSubscription, String resourceVersion, int statusCode){
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "[?]resource-version=" +1234))
				  .willReturn(aResponse()
				  .withStatus(statusCode)));
	}

	public static void MockDeleteServiceInstance_404(String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion){
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				 .willReturn(aResponse()
				  .withStatus(404)));
	}

	public static void MockDeleteServiceInstance_500(String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion){
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				 .willReturn(aResponse()
				  .withStatus(500)));
	}

	public static void MockPutServiceInstance(String globalCustId, String subscriptionType, String serviceInstanceId, String responseFile) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutServiceInstance_500(String globalCustId, String subscriptionType, String serviceInstanceId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(500)));
	}


	/**
	 * Service-Subscription Mock StubResponses below
	 */
	public static void MockGetServiceSubscription(String globalCustId, String subscriptionType, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteServiceSubscription(String globalCustId, String subscriptionType, int statusCode) {
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockDeleteServiceInstanceId(String globalCustId, String subscriptionType, String serviceInstanceId) {
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutServiceSubscription(String globalCustId, String subscriptionType) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	
	public static void MockGetServiceSubscription(String globalCustId, String subscriptionType, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
				.withStatus(statusCode)));
	}

	/**
	 * Customer Mock StubResponses below
	 */
	public static void MockGetCustomer(String globalCustId, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteCustomer(String globalCustId) {
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutCustomer(String globalCustId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutCustomer_500(String globalCustId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(500)));
	}


	/**
	 * Generic-Vnf Mock StubResponses below
	 */
	public static void MockGetGenericVnfById(String vnfId, String responseFile, int statusCode){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockGetGenericVnfByIdWithPriority(String vnfId, int statusCode, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.atPriority(1)
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile(responseFile)));	
	}
	
	public static void MockGetGenericVnfByIdWithPriority(String vnfId, String vfModuleId, int statusCode, String responseFile, int priority) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.atPriority(priority)
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile(responseFile)));	
	}

	public static void MockGetGenericVnfByIdWithDepth(String vnfId, int depth, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockGetGenericVnfById_404(String vnfId){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockGetGenericVnfById_500(String vnfId){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockGetGenericVnfByName(String vnfName, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=" + vnfName))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockGetGenericVnfByNameWithDepth(String vnfName, int depth, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=" + vnfName + "[&]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfByName_404(String vnfName){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=" + vnfName))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockDeleteGenericVnf(String vnfId, String resourceVersion){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(204)));
	}

	public static void MockDeleteGenericVnf(String vnfId, String resourceVersion, int statusCode){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockDeleteGenericVnf_500(String vnfId, String resourceVersion){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockPutGenericVnf(String vnfId){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	
	public static void MockPutGenericVnf(String vnfId, String requestBodyContaining, int statusCode) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.withRequestBody(containing(requestBodyContaining))
				.willReturn(aResponse()
					.withStatus(statusCode)));
	}

	public static void MockPutGenericVnf(String vnfId, int statusCode) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.willReturn(aResponse()
					.withStatus(statusCode)));
	}
	
	public static void MockPutGenericVnf_Bad(String vnfId, int statusCode){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}


	/**
	 * Vce Mock StubResponses below
	 */
	public static void MockGetVceById(String vnfId, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVceByName(String vnfName, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce[?]vnf-name=" + vnfName))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteVce(String vnfId, String resourceVersion, int statusCode){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/vces/vce/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockPutVce(String vnfId){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/vces/vce/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	
	public static void MockGetGenericVceByNameWithDepth(String vnfName, int depth, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce[?]vnf-name=" + vnfName + "[&]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVceGenericQuery(String serviceInstanceName, int depth, int statusCode, String responseFile){
		stubFor(get(urlMatching("/aai/v[0-9]+/search/generic-query[?]key=service-instance.service-instance-name:" + serviceInstanceName + "[&]start-node-type=service-instance[&]include=vce[&]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	/**
	 * Tenant Mock StubResponses below
	 */
	public static void MockGetTenantGenericQuery(String customer, String serviceType, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/search/generic-query[?]key=customer.global-customer-id:" + customer + "&key=service-subscription.service-type:" + serviceType + "&start-node-type=service-subscription&include=tenant&include=service-subscription&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetTenant(String tenantId, String responseFile) {
		stubFor(get(urlEqualTo("/aai/v2/cloud-infrastructure/tenants/tenant/" + tenantId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	/**
	 * Network Mock StubResponses below
	 */
	public static void MockGetNetwork(String networkId, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutNetwork(String networkId, int statusCode, String responseFile) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockPutNetwork(String networkPolicyId, String responseFile, int statusCode) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/" + networkPolicyId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockGetNetworkName(String networkPolicyName, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network[?]network-name=" + networkPolicyName))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockGetNetworkVpnBinding(String networkBindingId, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/vpn-bindings/vpn-binding/" + networkBindingId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockGetNetworkPolicy(String networkPolicy, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/" + networkPolicy))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockGetNetworkPolicyfqdn(String networkPolicy, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy[?]network-policy-fqdn=" + networkPolicy))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockGetNetworkRouteTable(String networkRouteId, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/route-table-references/route-table-reference/" + networkRouteId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	

	/**
	 * Cloud infrastructure below
	 */
	
	public static void MockGetCloudRegion(String cloudRegionId, int statusCode, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/" + cloudRegionId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	/**
	 * Volume Group StubResponse below
	 */
	public static void MockGetVolumeGroupById(String cloudRegionId, String volumeGroupId, String responseFile) {
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockPutVolumeGroupById(String cloudRegionId, String volumeGroupId, String responseFile, int statusCode) {
		stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockGetVolumeGroupByName(String cloudRegionId, String volumeGroupName, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/" + cloudRegionId + "/volume-groups[?]volume-group-name=" + volumeGroupName))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockDeleteVolumeGroupById(String cloudRegionId, String volumeGroupId, String resourceVersion, int statusCode) {
		stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(statusCode)));
	}

	/**
	 * VF-Module StubResponse below
	 * @param statusCode TODO
	 */
	public static void MockGetVfModuleId(String vnfId, String vfModuleId, String responseFile, int statusCode) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}
	
	public static void MockGetVfModuleIdNoResponse(String vnfId, String requestContaining, String vfModuleId) {
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")));
	}

	public static void MockPutVfModuleIdNoResponse(String vnfId, String requestContaining, String vfModuleId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId +"/vf-modules/vf-module/" +vfModuleId))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
					.withStatus(200)));
	}
	
	public static void MockPutVfModuleId(String vnfId, String vfModuleId) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	
	public static void MockPutVfModuleId(String vnfId, String vfModuleId, int returnCode) {
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(returnCode)));
	}
	
	public static void MockDeleteVfModuleId(String vnfId, String vfModuleId, String resourceVersion, int returnCode) {
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId + "/[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(returnCode)));
	}

	//// Deprecated Stubs below - to be deleted once unit test that reference them are refactored to use common ones above ////
	@Deprecated
	public static void MockGetVceById(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123?depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getVceResponse.xml")));
	}
	@Deprecated
	public static void MockGetVceByName(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getVceByNameResponse.xml")));
	}
	@Deprecated
	public static void MockPutVce(){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	@Deprecated
	public static void MockDeleteVce(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(204)));
	}
	@Deprecated
	public static void MockDeleteVce_404(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	@Deprecated
	public static void MockDeleteServiceSubscription(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET[?]resource-version=1234"))
				  .willReturn(aResponse()
				  .withStatus(204)));
	}
	@Deprecated
	public static void MockGetServiceSubscription(){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("GenericFlows/getServiceSubscription.xml")));
	}
	@Deprecated
	public static void MockGetServiceSubscription_200Empty(){
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET[?]resource-version=1234"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBody(" ")));
	}
	@Deprecated
	public static void MockGetServiceSubscription_404() {
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockGENPSIPutServiceInstance(){
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET/service-instances/service-instance/MIS%252F1604%252F0026%252FSW_INTERNET"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericPutServiceInstance/GenericPutServiceInstance_PutServiceInstance_AAIResponse_Success.xml")));
	}

	@Deprecated
	public static void MockGENPSIPutServiceSubscription(){
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericPutServiceInstance/GenericPutServiceInstance_PutServiceInstance_AAIResponse_Success.xml")));
	}
	@Deprecated
	public static void MockGENPSIPutServiceInstance_get500(){
		stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET/service-instances/service-instance/MIS%252F1604%252F0026%252FSW_INTERNET"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericPutServiceInstance/aaiFault.xml")));
	}

	@Deprecated
	public static void MockGetGenericVnfById(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfByNameResponse.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfById_404(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockGetGenericVnfByName(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfByName_hasRelationships(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse_hasRelationships.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfById_hasRelationships(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse_hasRelationships.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfById_500(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(500)));
	}
	@Deprecated
	public static void MockGetGenericVnfByName_404(){
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockPutGenericVnf(){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	@Deprecated
	public static void MockPutGenericVnf_400(){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(400)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(204)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf_404(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf_500(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(500)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf_412(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[[?]]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(412)));
	}

}
