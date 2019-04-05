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

package org.onap.so.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;


/**
 * Reusable Mock StubResponses for AAI Endpoints
 *
 */
public class StubResponseAAI {

	public static void setupAllMocks() {

	}


	/**
	 * Allotted Resource Mock StubResponses below
	 */
	public static void MockGetAllottedResource(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutAllottedResource(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutAllottedResource_500(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockDeleteAllottedResource(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId, String resourceVersion) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(204)));
	}

	public static void MockPatchAllottedResource(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String allottedResourceId) {
		wireMockServer.stubFor(patch(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId + "/allotted-resources/allotted-resource/" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockQueryAllottedResourceById(WireMockServer wireMockServer, String allottedResourceId, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=allotted-resource[&]filter=id:EQUALS:" + allottedResourceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}


	/**
	 * Service Instance Mock StubResponses below
	 */
	public static void MockGetServiceInstance(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")));
	}

	/**
	 * Service Instance Mock StubResponses below
	 */
	public static void MockGetServiceInstance(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetServiceInstance_404(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId))
						.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockGetServiceInstance_500(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId))
						.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockGetServiceInstance_500(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId))
						.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockNodeQueryServiceInstanceByName(WireMockServer wireMockServer, String serviceInstanceName, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance[&]filter=service-instance-name:EQUALS:" + serviceInstanceName))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockNodeQueryServiceInstanceByName_404(WireMockServer wireMockServer, String serviceInstanceName){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-name:EQUALS:" + serviceInstanceName))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockNodeQueryServiceInstanceByName_500(WireMockServer wireMockServer, String serviceInstanceName){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-name:EQUALS:" + serviceInstanceName))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockNodeQueryServiceInstanceById(WireMockServer wireMockServer, String serviceInstanceId, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance[&]filter=service-instance-id:EQUALS:" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockNodeQueryServiceInstanceById_404(WireMockServer wireMockServer, String serviceInstanceId){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-id:EQUALS:" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockNodeQueryServiceInstanceById_500(WireMockServer wireMockServer, String serviceInstanceId){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/nodes-query[?]search-node-type=service-instance&filter=service-instance-id:EQUALS:" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockDeleteServiceInstance(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(204)));
	}

	public static void MockGetServiceInstance(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion, int statusCode){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(statusCode)));
	}

	public static void MockGetServiceInstance(WireMockServer wireMockServer, String customer, String serviceSubscription, int statusCode){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")));
	}

	public static void MockDeleteServiceInstance(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion, int statusCode){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockDeleteServiceInstance(WireMockServer wireMockServer, String customer, String serviceSubscription, String resourceVersion, int statusCode){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "[?]resource-version=" +1234))
				  .willReturn(aResponse()
				  .withStatus(statusCode)));
	}

	public static void MockDeleteServiceInstance_404(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				 .willReturn(aResponse()
				  .withStatus(404)));
	}

	public static void MockDeleteServiceInstance_500(WireMockServer wireMockServer, String customer, String serviceSubscription, String serviceInstanceId, String resourceVersion){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + customer + "/service-subscriptions/service-subscription/" + serviceSubscription + "/service-instances/service-instance/" + serviceInstanceId + "[?]resource-version=" + resourceVersion))
				 .willReturn(aResponse()
				  .withStatus(500)));
	}

	public static void MockPutServiceInstance(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId, String responseFile) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutServiceInstance_500(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	/**
	 * Service-Subscription Mock StubResponses below
	 */
	public static void MockGetServiceSubscription(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteServiceSubscription(WireMockServer wireMockServer, String globalCustId, String subscriptionType, int statusCode) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockDeleteServiceInstanceId(WireMockServer wireMockServer, String globalCustId, String subscriptionType, String serviceInstanceId) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutServiceSubscription(WireMockServer wireMockServer, String globalCustId, String subscriptionType) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockGetServiceSubscription(WireMockServer wireMockServer, String globalCustId, String subscriptionType, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType))
				.willReturn(aResponse()
				.withStatus(statusCode)));
	}

	/**
	 * Customer Mock StubResponses below
	 */
	public static void MockGetCustomer(WireMockServer wireMockServer, String globalCustId, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteCustomer(WireMockServer wireMockServer, String globalCustId) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutCustomer(WireMockServer wireMockServer, String globalCustId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutCustomer_500(WireMockServer wireMockServer, String globalCustId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId))
				.willReturn(aResponse()
						.withStatus(500)));
	}


	/**
	 * Generic-Vnf Mock StubResponses below
	 */

	public static void MockGetGenericVnfById(WireMockServer wireMockServer, String vnfId, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfById(WireMockServer wireMockServer, String vnfId, String responseFile, int statusCode){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfByIdWithPriority(WireMockServer wireMockServer, String vnfId, int statusCode, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.atPriority(1)
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfByIdWithPriority(WireMockServer wireMockServer, String vnfId, String vfModuleId, int statusCode, String responseFile, int priority) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.atPriority(priority)
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfByIdWithDepth(WireMockServer wireMockServer, String vnfId, int depth, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfById_404(WireMockServer wireMockServer, String vnfId){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockGetGenericVnfById_500(WireMockServer wireMockServer, String vnfId){
		wireMockServer.stubFor(get(urlMatching("/aai/v9/network/generic-vnfs/generic-vnf/" + vnfId + "[?]depth=1"))
				.withQueryParam("depth", equalTo("1"))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockGetGenericVnfByName(WireMockServer wireMockServer, String vnfName, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=" + vnfName))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfByNameWithDepth(WireMockServer wireMockServer, String vnfName, int depth, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=" + vnfName + "[&]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfByName_404(WireMockServer wireMockServer, String vnfName){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=" + vnfName))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockDeleteGenericVnf(WireMockServer wireMockServer, String vnfId, String resourceVersion){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(204)));
	}

	public static void MockDeleteGenericVnf(WireMockServer wireMockServer, String vnfId, String resourceVersion, int statusCode){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockDeleteGenericVnf_500(WireMockServer wireMockServer, String vnfId, String resourceVersion){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void MockPutGenericVnf(WireMockServer wireMockServer, String vnfId){
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutGenericVnf(WireMockServer wireMockServer, String vnfId, String requestBodyContaining, int statusCode) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.withRequestBody(containing(requestBodyContaining))
				.willReturn(aResponse()
					.withStatus(statusCode)));
	}

	public static void MockPutGenericVnf(WireMockServer wireMockServer, String vnfId, int statusCode) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf" + vnfId))
				.willReturn(aResponse()
					.withStatus(statusCode)));
	}

	public static void MockPutGenericVnf_Bad(WireMockServer wireMockServer, String vnfId, int statusCode){
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockPatchGenericVnf(WireMockServer wireMockServer, String vnfId){
		wireMockServer.stubFor(patch(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	/**
	 * Vce Mock StubResponses below
	 */
	public static void MockGetVceById(WireMockServer wireMockServer, String vnfId, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVceByName(WireMockServer wireMockServer, String vnfName, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce[?]vnf-name=" + vnfName))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteVce(WireMockServer wireMockServer, String vnfId, String resourceVersion, int statusCode){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/vces/vce/" + vnfId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}

	public static void MockPutVce(WireMockServer wireMockServer, String vnfId){
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/vces/vce/" + vnfId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockGetGenericVceByNameWithDepth(WireMockServer wireMockServer, String vnfName, int depth, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce[?]vnf-name=" + vnfName + "[&]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVceGenericQuery(WireMockServer wireMockServer, String serviceInstanceName, int depth, int statusCode, String responseFile){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/generic-query[?]key=service-instance.service-instance-name:" + serviceInstanceName + "[&]start-node-type=service-instance[&]include=vce[&]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	/**
	 * Tenant Mock StubResponses below
	 */
	public static void MockGetTenantGenericQuery(WireMockServer wireMockServer, String customer, String serviceType, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/search/generic-query[?]key=customer.global-customer-id:" + customer + "&key=service-subscription.service-type:" + serviceType + "&start-node-type=service-subscription&include=tenant&include=service-subscription&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetTenant(WireMockServer wireMockServer, String tenantId, String responseFile) {
		wireMockServer.stubFor(get(urlEqualTo("/aai/v2/cloud-infrastructure/tenants/tenant/" + tenantId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	/**
	 * Network Mock StubResponses below
	 */
	public static void MockGetNetwork(WireMockServer wireMockServer, String networkId, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkByIdWithDepth(WireMockServer wireMockServer, String networkId, String responseFile, String depth) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId + "[?]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkCloudRegion(WireMockServer wireMockServer, String responseFile, String cloudRegion) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/"+cloudRegion))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkByName(WireMockServer wireMockServer, String networkName, String responseFile) {
		   wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network[?]network-name="+networkName))
					.willReturn(aResponse()
							.withStatus(200)
							.withHeader("Content-Type", "text/xml")
							.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkByName_404(WireMockServer wireMockServer, String responseFile, String networkName) {
 	wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network[?]network-name="+networkName))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkCloudRegion_404(WireMockServer wireMockServer, String cloudRegion) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/"+cloudRegion))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	public static void MockPutNetwork(WireMockServer wireMockServer, String networkId, int statusCode, String responseFile) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutNetwork(WireMockServer wireMockServer, String networkPolicyId, String responseFile, int statusCode) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/" + networkPolicyId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

    public static void MockPostNetwork(WireMockServer wireMockServer, String networkId) {
        wireMockServer.stubFor(post(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "text/xml")));
    }

    public static void MockPostNetworkSubnet(WireMockServer wireMockServer, String networkId, String subnetId) {
        wireMockServer.stubFor(post(urlMatching(
            "/aai/v[0-9]+/network/l3-networks/l3-network/" + networkId + "/subnets/subnet/" + subnetId))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "text/xml")));
    }

	public static void MockGetNetworkName(WireMockServer wireMockServer, String networkPolicyName, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network[?]network-name=" + networkPolicyName))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

    public static void MockGetNetworkVpnBinding(WireMockServer wireMockServer, String responseFile, String vpnBinding) {
        MockGetNetworkVpnBindingWithDepth(wireMockServer, responseFile, vpnBinding, "all");
    }

    public static void MockGetNetworkVpnBindingWithDepth(WireMockServer wireMockServer, String responseFile,
        String vpnBinding, String depth) {
        wireMockServer.stubFor(
            get(urlMatching("/aai/v[0-9]+/network/vpn-bindings/vpn-binding/" + vpnBinding + "[?]depth=" + depth))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile(responseFile)));
    }

	public static void MockGetNetworkPolicy(WireMockServer wireMockServer, String responseFile, String policy) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/"+policy + "[?]depth=all"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkVpnBinding(WireMockServer wireMockServer, String networkBindingId, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/vpn-bindings/vpn-binding/" + networkBindingId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void MockGetNetworkPolicy(WireMockServer wireMockServer, String networkPolicy, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/" + networkPolicy))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void MockGetNetworkTableReference(WireMockServer wireMockServer, String responseFile, String tableReference) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/route-table-references/route-table-reference/"+tableReference + "[?]depth=all"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutNetworkIdWithDepth(WireMockServer wireMockServer, String responseFile, String networkId, String depth) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/"+networkId+"[?]depth="+depth ))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetNetworkPolicyfqdn(WireMockServer wireMockServer, String networkPolicy, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy[?]network-policy-fqdn=" + networkPolicy))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void MockGetNetworkRouteTable(WireMockServer wireMockServer, String networkRouteId, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/route-table-references/route-table-reference/" + networkRouteId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void MockPatchVfModuleId(WireMockServer wireMockServer, String vnfId, String vfModuleId) {
		wireMockServer.stubFor(patch(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	/////////////

	public static void MockVNFAdapterRestVfModule(WireMockServer wireMockServer) {
		wireMockServer.stubFor(put(urlEqualTo("/vnfs/v1/vnfs/skask/vf-modules/supercool"))
			.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/xml")));
		wireMockServer.stubFor(post(urlMatching("/vnfs/v1/vnfs/.*/vf-modules"))
				.willReturn(aResponse()
					.withStatus(202)
					.withHeader("Content-Type", "application/xml")));
		wireMockServer.stubFor(post(urlEqualTo("/vnfs/v1/vnfs/skask/vf-modules"))
			.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/xml")));
		wireMockServer.stubFor(put(urlEqualTo("/vnfs/v1/volume-groups/78987"))
			.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/xml")));
	}

	public static void MockDBUpdateVfModule(WireMockServer wireMockServer){
		wireMockServer.stubFor(post(urlEqualTo("/dbadapters/RequestsDbAdapter"))
			.willReturn(aResponse()
				.withStatus(200)
			    .withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/DBUpdateResponse.xml")));
	}

	// start of mocks used locally and by other VF Module unit tests
	public static void MockSDNCAdapterVfModule(WireMockServer wireMockServer) {
		// simplified the implementation to return "success" for all requests
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter"))
//			.withRequestBody(containing("SvcInstanceId><"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));

	}

	// start of mocks used locally and by other VF Module unit tests
	public static void MockAAIVfModule(WireMockServer wireMockServer) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/supercool"))
			.atPriority(1)
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/VfModule-supercool.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/lukewarm"))
			.atPriority(2)
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/VfModule-lukewarm.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/.*"))
			.atPriority(5)
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/VfModule-new.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask[?]depth=1"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/GenericVnf.xml")));
		wireMockServer.stubFor(patch(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/supercool"))
//			.withRequestBody(containing("PCRF"))
			.willReturn(aResponse()
				.withStatus(200)));
		wireMockServer.stubFor(patch(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/.*"))
//				.withRequestBody(containing("PCRF"))
				.willReturn(aResponse()
					.withStatus(200)));
		// HTTP PUT stub still used by CreateAAIvfModuleVolumeGroup
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/.*"))
				.withRequestBody(containing("PCRF"))
				.willReturn(aResponse()
					.withStatus(200)));
		// HTTP PUT stub still used by DoCreateVfModuleTest
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/.*"))
				.withRequestBody(containing("MODULELABEL"))
				.willReturn(aResponse()
					.withStatus(200)));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/volume-groups/volume-group[?]volume-group-id=78987"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/ConfirmVolumeGroupTenantResponse.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/volume-groups/volume-group[?]volume-group-id=78987"))
				.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/ConfirmVolumeGroupTenantResponse.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/MDTWNJ21/volume-groups/volume-group/78987"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/VolumeGroup.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/AAIAIC25/volume-groups/volume-group/78987"))
				.willReturn(aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile("VfModularity/VolumeGroup.xml")));
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/volume-groups/volume-group/78987[?]resource-version=0000020"))
			     .willReturn(aResponse()
			     .withStatus(200)
			     .withHeader("Content-Type", "text/xml")
			     .withBodyFile("DeleteCinderVolumeV1/DeleteVolumeId_AAIResponse_Success.xml")));
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("VfModularity/AddNetworkPolicy_AAIResponse_Success.xml")));
		wireMockServer.stubFor(patch(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/NEWvBNGModuleId"))
				.withRequestBody(containing("NEWvBNGModuleId"))
				.willReturn(aResponse()
					.withStatus(200)));
	}



	//////////////

	/**
	 * Cloud infrastructure below
	 */

	public static void MockGetCloudRegion(WireMockServer wireMockServer, String cloudRegionId, int statusCode, String responseFile) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	/**
	 * Volume Group StubResponse below
	 */
	public static void MockGetVolumeGroupById(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupId, String responseFile) {
		MockGetVolumeGroupById(wireMockServer, cloudRegionId, volumeGroupId, responseFile, 200);
	}

	public static void MockGetVolumeGroupById(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupId, String responseFile, int responseCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId))
				.willReturn(aResponse()
						.withStatus(responseCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockPutVolumeGroupById(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupId, String responseFile, int statusCode) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVolumeGroupByName(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupName, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId + "/volume-groups[?]volume-group-name=" + volumeGroupName))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockDeleteVolumeGroupById(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupId, String resourceVersion, int statusCode) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId + "[?]resource-version=" + resourceVersion))
				  .willReturn(aResponse()
				  .withStatus(statusCode)));
	}

	public static void MockGetVolumeGroupByName_404(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupName) {
		wireMockServer.stubFor(get(urlMatching("/aai/v9/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId + "/volume-groups[?]volume-group-name=" + volumeGroupName))
				.willReturn(aResponse()
				.withStatus(404)));
	}

	public static void MockDeleteVolumeGroup(WireMockServer wireMockServer, String cloudRegionId, String volumeGroupId, String resourceVersion) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/" + cloudRegionId + "/volume-groups/volume-group/" + volumeGroupId + "[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
				.withStatus(200)));
	}

	/**
	 * VF-Module StubResponse below
	 * @param statusCode TODO
	 */
	public static void MockGetVfModuleId(WireMockServer wireMockServer, String vnfId, String vfModuleId, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVfModuleByNameWithDepth(WireMockServer wireMockServer, String vnfId, String vfModuleName, int depth, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module[?]vf-module-name=" + vfModuleName + "[?]depth=" + depth))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVfModuleByName(WireMockServer wireMockServer, String vnfId, String vfModuleName, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module[?]vf-module-name=" + vfModuleName))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile(responseFile)));
	}

	public static void MockGetVfModuleIdNoResponse(WireMockServer wireMockServer, String vnfId, String requestContaining, String vfModuleId) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")));
	}

	public static void MockPutVfModuleIdNoResponse(WireMockServer wireMockServer, String vnfId, String requestContaining, String vfModuleId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId +"/vf-modules/vf-module/" +vfModuleId))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
					.withStatus(200)));
	}

	public static void MockPutVfModuleId(WireMockServer wireMockServer, String vnfId, String vfModuleId) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void MockPutVfModuleId(WireMockServer wireMockServer, String vnfId, String vfModuleId, int returnCode) {
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId))
				.willReturn(aResponse()
						.withStatus(returnCode)));
	}

	public static void MockDeleteVfModuleId(WireMockServer wireMockServer, String vnfId, String vfModuleId, String resourceVersion, int returnCode) {
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId + "/[?]resource-version=" + resourceVersion))
				.willReturn(aResponse()
						.withStatus(returnCode)));
	}

	public static void MockAAIVfModuleBadPatch(WireMockServer wireMockServer, String endpoint, int statusCode) {
		wireMockServer.stubFor(patch(urlMatching(endpoint))
			.willReturn(aResponse()
				.withStatus(statusCode)));
	}

	/* AAI Pserver Queries */
	public static void MockGetPserverByVnfId(WireMockServer wireMockServer, String vnfId, String responseFile, int statusCode) {
		wireMockServer.stubFor(put(urlMatching("/aai/v1[0-9]/query.*"))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "application/json")
						.withBodyFile(responseFile)));
	}

	public static void MockGetGenericVnfsByVnfId(WireMockServer wireMockServer, String vnfId, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v1[0-9]/network/generic-vnfs/.*"))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "application/json; charset=utf-8")
						.withBodyFile(responseFile)));
	}

	public static void MockSetInMaintFlagByVnfId(WireMockServer wireMockServer, String vnfId, int statusCode) {
		wireMockServer.stubFor(patch(urlMatching("/aai/v1[0-9]/network/generic-vnfs/.*"))
				.willReturn(aResponse()
						.withStatus(statusCode)
						));
	}

	public static void MockSetInMaintFlagByVnfId(WireMockServer wireMockServer, String vnfId, String responseFile, int statusCode) {
		wireMockServer.stubFor(post(urlMatching("/aai/v1[0-9]/network/generic-vnfs/.*"))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withBodyFile(responseFile)
						));
	}

	public static void MockGetDefaultCloudRegionByCloudRegionId(WireMockServer wireMockServer, String cloudRegionId, String responseFile, int statusCode) {
		wireMockServer.stubFor(get(urlMatching("/aai/v1[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/"+cloudRegionId + ".*"))
				.willReturn(aResponse()
						.withStatus(statusCode)
						.withHeader("Content-Type", "application/json; charset=utf-8")
						.withBodyFile(responseFile)));
	}

	//// Deprecated Stubs below - to be deleted once unit test that reference them are refactored to use common ones above ////
	@Deprecated
	public static void MockGetVceById(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123?depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getVceResponse.xml")));
	}
	@Deprecated
	public static void MockGetVceByName(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/vces/vce[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getVceByNameResponse.xml")));
	}
	@Deprecated
	public static void MockPutVce(WireMockServer wireMockServer){
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	@Deprecated
	public static void MockDeleteVce(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(204)));
	}
	@Deprecated
	public static void MockDeleteVce_404(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/vces/vce/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	@Deprecated
	public static void MockDeleteServiceSubscription(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET[?]resource-version=1234"))
				  .willReturn(aResponse()
				  .withStatus(204)));
	}
	@Deprecated
	public static void MockGetServiceSubscription(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("GenericFlows/getServiceSubscription.xml")));
	}
	@Deprecated
	public static void MockGetServiceSubscription_200Empty(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET[?]resource-version=1234"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBody(" ")));
	}
	@Deprecated
	public static void MockGetServiceSubscription_404(WireMockServer wireMockServer) {
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET"))
				.willReturn(aResponse()
						.withStatus(404)));
	}

	@Deprecated
	public static void MockGetGenericVnfById(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfByNameResponse.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfById_404(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockGetGenericVnfByName(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfByName_hasRelationships(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse_hasRelationships.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfById_hasRelationships(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse_hasRelationships.xml")));
	}
	@Deprecated
	public static void MockGetGenericVnfById_500(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(500)));
	}
	@Deprecated
	public static void MockGetGenericVnfByName_404(WireMockServer wireMockServer){
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockPutGenericVnf(WireMockServer wireMockServer){
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(200)));
	}
	@Deprecated
	public static void MockPutGenericVnf_400(WireMockServer wireMockServer){
		wireMockServer.stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123"))
				.willReturn(aResponse()
						.withStatus(400)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(204)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf_404(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(404)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf_500(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(500)));
	}
	@Deprecated
	public static void MockDeleteGenericVnf_412(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[[?]]resource-version=testReVer123"))
				.willReturn(aResponse()
						.withStatus(412)));
	}

}
