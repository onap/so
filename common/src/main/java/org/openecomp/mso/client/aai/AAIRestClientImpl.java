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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Pservers;
import org.openecomp.mso.client.aai.entities.CustomQuery;
import org.openecomp.mso.client.aai.entities.Results;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.springframework.stereotype.Service;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class AAIRestClientImpl implements AAIRestClientI {
	
	private static final EELFLogger logger = EELFManager.getInstance().getMetricsLogger();
	private static final AAIVersion ENDPOINT_VERSION = AAIVersion.V10;
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

	public AAIRestClientImpl() {		
	
		
	}

	public AAIRestClientImpl(final String host) {	
		
	
	}

	@Override
	public Pservers getPhysicalServers(String hostName, String uuid) {
		UUID requestId;
		try {
			requestId = UUID.fromString(uuid);
		} catch (IllegalArgumentException e) {
			logger.warn("could not parse uuid: " + uuid + " creating valid uuid automatically");
			requestId = UUID.randomUUID();
		}
		return new AAIResourcesClient(ENDPOINT_VERSION, requestId).get(Pservers.class, AAIUriFactory.createResourceUri(AAIObjectPlurals.PSERVER));
	}

	@Override
	public List<Pserver> getPhysicalServerByVnfId(String vnfId, String transactionLoggingUuid)
			throws JsonParseException, JsonMappingException, IOException {
		UUID requestId;
		try {
			requestId = UUID.fromString(transactionLoggingUuid);
		} catch (IllegalArgumentException e) {
			logger.warn("could not parse uuid: " + transactionLoggingUuid + " creating valid uuid automatically");
			requestId = UUID.randomUUID();
		}
		List<AAIResourceUri> startNodes = new ArrayList<>();
		startNodes.add(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId));
		String jsonInput = new AAIQueryClient(ENDPOINT_VERSION, requestId).query(Format.RESOURCE, new CustomQuery(startNodes,PSERVER_VNF_QUERY));

		return this.getListOfPservers(jsonInput);
		
	}

	protected List<Pserver> getListOfPservers(String jsonInput) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new AAICommonObjectMapperProvider().getContext(Object.class);
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
		UUID requestId;
		try {
			requestId = UUID.fromString(transactionLoggingUuid);
		} catch (IllegalArgumentException e) {
			logger.warn("could not parse uuid: " + transactionLoggingUuid + " creating valid uuid automatically");
			requestId = UUID.randomUUID();
		}
		GenericVnfs genericVnfs = new AAIResourcesClient(ENDPOINT_VERSION, requestId).get(GenericVnfs.class, AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName));
		if(genericVnfs.getGenericVnf().size() > 1)
			throw new IndexOutOfBoundsException("Multiple Generic Vnfs Returned");
		
		GenericVnf genericVnf = genericVnfs.getGenericVnf().get(0);
		updateMaintenceFlagVnfId(genericVnf.getVnfId(), inMaint, transactionLoggingUuid);
	}

	@Override
	public void updateMaintenceFlagVnfId(String vnfId, boolean inMaint, String transactionLoggingUuid) throws JsonParseException, JsonMappingException, IOException {
		UUID requestId;
		try {
			requestId = UUID.fromString(transactionLoggingUuid);
		} catch (IllegalArgumentException e) {
			logger.warn("could not parse uuid: " + transactionLoggingUuid + " creating valid uuid automatically");
			requestId = UUID.randomUUID();
		}
		GenericVnf genericVnf = new GenericVnf();
		genericVnf.setInMaint(inMaint);
		new AAIResourcesClient(ENDPOINT_VERSION, requestId).update(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId), genericVnf);
		
	}

	@Override
	public GenericVnf getVnfByName(String vnfId, String transactionLoggingUuid) throws JsonParseException, JsonMappingException, IOException {
		UUID requestId;
		try {
			requestId = UUID.fromString(transactionLoggingUuid);
		} catch (IllegalArgumentException e) {
			logger.warn("could not parse uuid: " + transactionLoggingUuid + " creating valid uuid automatically");
			requestId = UUID.randomUUID();
		}
		return new AAIResourcesClient(ENDPOINT_VERSION, requestId).get(GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId));
	}

}
