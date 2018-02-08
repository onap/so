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

package org.openecomp.mso.client.aai;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Pservers;
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.client.aai.entities.CustomQuery;
import org.openecomp.mso.client.aai.entities.Results;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AAIRestClientImpl implements AAIRestClient {

	private final WebTarget webTarget;

	private static final String ENDPOINT_VERSION = "v10";
	private static final String ENDPOINT_GET_ALL = ENDPOINT_VERSION + "/cloud-infrastructure/pservers";
	private static final String ENDPOINT_GET_ALL_VNFS = ENDPOINT_VERSION + "/network/generic-vnfs";
	private static final String ENDPOINT_CUSTOM_QUERY = ENDPOINT_VERSION + "/query";
	private static final String PSERVER_VNF_QUERY = "pservers-fromVnf";
	private static final String GENERIC_VNF_PATH = ENDPOINT_VERSION + "/network/generic-vnfs/generic-vnf";
	private static final String SERVICE_TOPOLOGY_BY_SERVICE_INSTANCE_ID = "store(‘x’).union(__.in(‘subscribesTo’).has(‘aai-node-type’,’customer’).store(‘x’),__.out(‘uses’).has(‘aai-node-type’,’allotted-resource’).store(‘x’),__.in(‘hasInstance’).has(‘aai-node-type’,’generic-vnf’).store(‘x’).union("
			+ ".out(‘has’).has(‘aai-node-type’,’vf-module’).store(‘x’),out(‘uses’).has(‘aai-node-type’,’volume-group’).store(‘x’),"
			+ ".out(‘hasLInterface’).has(‘aai-node-type’,’l-interface’).union("
			+ ".out(‘hasIpAddress’).has(‘aai-node-type’,’l3-interface-ipv4-address’).store(‘x’).out(‘isMemberOf’).has(‘aai-node-type’,’l3-network’).store(‘x’),"
			+ ".out(‘hasIpAddress’).has(‘aai-node-type’,’l3-interface-ipv6-address’).store(‘x’).out(‘isMemberOf’).has(‘aai-node-type’,’l3-network’).store(‘x’)"
			+ ")," + ".out(‘runsOnVserver’).has(‘aai-node-type’,’vserver’).store(‘x’).union("
			+ ".in(‘owns’).has(‘aai-node-type’,’tenant’).store(‘x’).in(‘has’).has(‘aai-node-type’,’cloud-region’).store(‘x’),"
			+ ".out(‘runsOnPserver’).has(‘aai-node-type’,’pserver’).store(‘x’),"
			+ ".out(‘hasLInterface’).has(‘aai-node-type’,’l-interface’).union("
			+ ".out(‘hasIpAddress’).has(‘aai-node-type’,’l3-interface-ipv4-address’).store(‘x’).out(‘isMemberOf’).has(‘aai-node-type’,’l3-network’).store(‘x’),"
			+ ".out(‘hasIpAddress’).has(‘aai-node-type’,’l3-interface-ipv6-address’).store(‘x’).out(‘isMemberOf’).has(‘aai-node-type’,’l3-network’).store(‘x’)"
			+ ")" + ")" + ")" + ").cap(‘x’).unfold().dedup()";

	public AAIRestClientImpl() throws NoSuchAlgorithmException {

		Logger logger = Logger.getLogger(getClass().getName());
		Map<String, String> properties = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
		Client client = this.getSSLClient();
		webTarget = client.register(logger).register(new AAIClientResponseExceptionMapper())
				.target(properties.get("aai.endpoint") + "/aai");
	}

	public AAIRestClientImpl(final String host) throws NoSuchAlgorithmException {
		Logger logger = Logger.getLogger(getClass().getName());
		Client client = this.getSSLClient();
		webTarget = client.register(logger).register(new AAIClientResponseExceptionMapper()).target(host + "/aai");
	}

	@Override
	public Pservers getPhysicalServers(String hostName, String uuid) {
		return webTarget.register(AAIResourcesObjectMapperProvider.class).path(ENDPOINT_GET_ALL).request()
				.header("X-FromAppId", "MSO").header("X-TransactionId", uuid)
				.header("Content-Type", MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get()
				.readEntity(Pservers.class);
	}

	@Override
	public List<Pserver> getPhysicalServerByVnfId(String vnfId, String transactionLoggingUuid)
			throws JsonParseException, JsonMappingException, IOException {
		List<String> startNodes = new ArrayList<>();
		startNodes.add("network/generic-vnfs/generic-vnf/" + vnfId);
		String jsonInput = webTarget.register(AAIQueryObjectMapperProvider.class).path(ENDPOINT_CUSTOM_QUERY)
				.queryParam("format", "resource").request().header("X-FromAppId", "MSO")
				.header("X-TransactionId", transactionLoggingUuid)
				.header("Content-Type", MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.entity(new CustomQuery(startNodes, PSERVER_VNF_QUERY), MediaType.APPLICATION_JSON))
				.readEntity(String.class);

		
		return this.getListOfPservers(jsonInput);
	}

	protected List<Pserver> getListOfPservers(String jsonInput) throws JsonParseException, JsonMappingException, IOException
	{
		ObjectMapper mapper = new AAIQueryObjectMapperProvider().getContext(Object.class);
		Results<Map<String, Pserver>> resultsFromJson = mapper.readValue(jsonInput,
				new TypeReference<Results<Map<String, Pserver>>>() {
				});
		List<Pserver> results = new ArrayList<>();
		for (Map<String, Pserver> m : resultsFromJson.getResult()) {
			results.add(m.get("pserver"));
		}
		return results;
	}
	
	protected List<Pserver> getListOfPservers(File jsonInput) throws JsonParseException, JsonMappingException, IOException
	{
		ObjectMapper mapper = new AAIQueryObjectMapperProvider().getContext(Object.class);
		Results<Map<String, Pserver>> resultsFromJson = mapper.readValue(jsonInput,
				new TypeReference<Results<Map<String, Pserver>>>() {
				});
		List<Pserver> results = new ArrayList<>();
		for (Map<String, Pserver> m : resultsFromJson.getResult()) {
			results.add(m.get("pserver"));
		}
		return results;
	}
	
	@Override
	public void updateMaintenceFlag(String vnfName, boolean inMaint, String transactionLoggingUuid) throws JsonParseException, JsonMappingException, IOException {
		GenericVnfs genericVnfs = webTarget.register(AAIResourcesObjectMapperProvider.class).path(ENDPOINT_GET_ALL_VNFS)
				.queryParam("vnf-name", vnfName).request().header("X-FromAppId", "MSO")
				.header("X-TransactionId", transactionLoggingUuid).header("Content-Type", "application/json")
				.accept(MediaType.APPLICATION_JSON_TYPE).get().readEntity(GenericVnfs.class);

		if (genericVnfs.getGenericVnf().size() > 1)
			throw new IndexOutOfBoundsException ("Multiple Generic Vnfs Returned");

		GenericVnf genericVnf = genericVnfs.getGenericVnf().get(0);
		updateMaintenceFlagVnfId(genericVnf.getVnfId(), inMaint, transactionLoggingUuid);
	}

	@Override
	public void updateMaintenceFlagVnfId(String vnfId, boolean inMaint, String transactionLoggingUuid)
			throws JsonParseException, JsonMappingException, IOException {
		GenericVnf genericVnf = new GenericVnf();
		genericVnf.setInMaint(inMaint);
		webTarget.register(AAIResourcesObjectMapperProvider.class).path(GENERIC_VNF_PATH + "/" + vnfId).request()
				.header("X-FromAppId", "MSO").header("X-TransactionId", transactionLoggingUuid)
				.header("Content-Type", "application/merge-patch+json")
				.header("Accept", MediaType.APPLICATION_JSON_TYPE).header("X-HTTP-Method-Override", "PATCH")
				.put(Entity.entity(genericVnf, MediaType.valueOf("application/merge-patch+json")));
	}

	@Override
	public GenericVnf getVnfByName(String vnfId, String transactionLoggingUuid) throws JsonParseException, JsonMappingException, IOException {
		return webTarget.register(AAIResourcesObjectMapperProvider.class).path(GENERIC_VNF_PATH + "/" + vnfId).request()
				.header("X-FromAppId", "MSO").header("X-TransactionId", transactionLoggingUuid)
				.header("Content-Type", "application/json").accept(MediaType.APPLICATION_JSON_TYPE).get()
				.readEntity(GenericVnf.class);
	}

	protected Client getSSLClient() throws NoSuchAlgorithmException {
		return ClientBuilder.newBuilder().sslContext(SSLContext.getDefault()).build();
	}

}
