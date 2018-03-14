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

package org.openecomp.mso.client.sdno;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.onap.aai.domain.yang.GenericVnf;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.AAIVersion;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.dmaap.DmaapConsumer;
import org.openecomp.mso.client.dmaap.DmaapPublisher;
import org.openecomp.mso.client.sdno.beans.Body;
import org.openecomp.mso.client.sdno.beans.Input;
import org.openecomp.mso.client.sdno.beans.RequestHealthDiagnostic;
import org.openecomp.mso.client.sdno.beans.SDNO;
import org.openecomp.mso.client.sdno.dmaap.SDNOHealthCheckDmaapConsumer;
import org.openecomp.mso.client.sdno.dmaap.SDNOHealthCheckDmaapPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;


public class SDNOValidatorImpl implements SDNOValidator {

	private final static String clientName = "MSO";

	@Override
	public void healthDiagnostic(String vnfId, UUID uuid, String requestingUserId) throws IOException, Exception {
		
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
		AAIResourcesClient client = new AAIResourcesClient(AAIVersion.V10, uuid);
		GenericVnf vnf = client.get(GenericVnf.class, uri);
		
		SDNO requestDiagnostic = buildRequestDiagnostic(vnf, uuid, requestingUserId);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(requestDiagnostic);
		this.submitRequest(json);
		this.pollForResponse(uuid.toString());
		
	}

	protected SDNO buildRequestDiagnostic(GenericVnf vnf, UUID uuid, String requestingUserId) {
		
		Optional<String> vnfType;
		if (vnf.getVnfType() == null) {
			vnfType = Optional.empty();
		} else {
			vnfType = Optional.of(vnf.getVnfType());
		}
		Input input = new Input();
		SDNO parentRequest = new SDNO();
		Body body = new Body();
		parentRequest.setBody(body);
		parentRequest.setNodeType(vnfType.orElse("NONE").toUpperCase());
		parentRequest.setOperation("health-diagnostic");
		
		body.setInput(input);
		
		RequestHealthDiagnostic request = new RequestHealthDiagnostic();
		request.setRequestClientName(clientName);
		request.setRequestNodeName(vnf.getVnfName());
		request.setRequestNodeIp(vnf.getIpv4OamAddress()); //generic-vnf oam ip
		request.setRequestUserId(requestingUserId); //mech id?
		request.setRequestId(uuid.toString()); //something to identify this request by for polling
		
		input.setRequestHealthDiagnostic(request);
		
		return parentRequest;
	}
	protected void submitRequest(String json) throws FileNotFoundException, IOException, InterruptedException {
		
		DmaapPublisher publisher = new SDNOHealthCheckDmaapPublisher();
		publisher.send(json);
	}
	protected boolean pollForResponse(String uuid) throws Exception {
		DmaapConsumer consumer = this.getConsumer(uuid);
		return consumer.consume();
	}
	

	
	protected DmaapConsumer getConsumer(String uuid) throws FileNotFoundException, IOException {
		return new SDNOHealthCheckDmaapConsumer(uuid);
	}
	

	
}
