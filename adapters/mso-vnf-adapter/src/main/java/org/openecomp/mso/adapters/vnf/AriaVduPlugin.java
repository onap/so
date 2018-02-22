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
package org.openecomp.mso.adapters.vnf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gigaspaces.aria.rest.client.AriaClient;
import com.gigaspaces.aria.rest.client.AriaClientFactory;
import com.gigaspaces.aria.rest.client.ExecutionDetails;
import com.gigaspaces.aria.rest.client.Input;
import com.gigaspaces.aria.rest.client.InputImpl;
import com.gigaspaces.aria.rest.client.Output;
import com.gigaspaces.aria.rest.client.Service;
import com.gigaspaces.aria.rest.client.ServiceTemplate;
import com.gigaspaces.aria.rest.client.ServiceTemplateImpl;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.vdu.utils.VduBlueprint;
import org.openecomp.mso.vdu.utils.VduInfo;
import org.openecomp.mso.vdu.utils.VduPlugin;
import org.openecomp.mso.vdu.utils.VduStatus;

/**
 * ARIA VDU Plugin.  Pluggable interface for the ARIA REST API to support TOSCA
 * orchestration.
 * 
 * @author DeWayne
 *
 */
public class AriaVduPlugin implements VduPlugin {
	private static final String API_VERSION = "0.1";
	private static final MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private AriaClient client=null;
	private Map<String,Integer> templateIds = new HashMap<>();
	private Map<String,Integer> serviceIds = new HashMap<>();
	private Map<String,Map<String,Object>> inputsCache = new HashMap<>();

	public AriaVduPlugin() {
		super();
	}

	public AriaVduPlugin( String host, int port) {
		try {
			client = new AriaClientFactory().createRestClient("http", host, port, API_VERSION);
		}catch(Exception e) {
			logger.error (MessageEnum.RA_CREATE_VNF_ERR,  "", "", "", "", "aria", MsoLogger.ErrorCode.AvailabilityError, "Connection to ARIA REST API failed", e);
			throw e;
		}
	}

	/**
	 * Instantiate VDU in ARIA. <code>vduInstanceName</code> is used for both service template
	 * name and service name.< 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public VduInfo instantiateVdu(String cloudSiteId, String tenantId, String vduInstanceName,
			VduBlueprint vduBlueprint, Map<String, ? extends Object> inputs, String environmentFile, int timeoutMinutes,
			boolean suppressBackout) throws MsoException {

		VduInfo vinfo = new VduInfo(vduInstanceName);
		byte[] csar = new CSAR(vduBlueprint).create();
		ServiceTemplate template = new ServiceTemplateImpl( vduInstanceName, csar);
		try {
			client.install_service_template(template);
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceName, MsoLogger.ErrorCode.BusinessProcesssError,
					"instantiate vdu via csar failed", e);
			throw new MsoAdapterException(e.getMessage());
		}

		/**
		 * Create a service
		 */

		try {
			int templateId=-1;
			for(ServiceTemplate stemplate:(List<ServiceTemplate>)client.list_service_templates()) {
				if(stemplate.getName().equals(vduInstanceName)) {
					templateId = stemplate.getId();
				}
			}
			List<Input> sinputs = new ArrayList<Input>();
			for(Map.Entry<String, ? extends Object> entry: inputs.entrySet()) {
				Input inp = new InputImpl(entry.getKey(),entry.getValue().toString(),"");
				sinputs.add(inp);
			}
			client.create_service(templateId, vduInstanceName, sinputs);
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceName, MsoLogger.ErrorCode.BusinessProcesssError,
					"aria service creation failed", e);
			throw new MsoAdapterException(e.getMessage());
		}
		
		// Get the service ID and cache it
		int sid = getServiceId(vduInstanceName);
		serviceIds.put(vduInstanceName, sid);

		/**
		 * Run install
		 */

		try {
			client.start_execution( sid, "install", new ExecutionDetails());
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceName, MsoLogger.ErrorCode.BusinessProcesssError,
					"aria install workflow failed", e);
			throw new MsoAdapterException(e.getMessage());
		}

		/**
		 * Get the outputs and return 
		 */

		try {
			Map<String,Object> voutputs = getOutputs(sid);

			VduInfo vi = new VduInfo(vduInstanceName);
			vi.setInputs((Map<String,Object>)inputs);
			inputsCache.put(vduInstanceName,vi.getInputs());
			vi.setOutputs(voutputs);
			vi.setStatus(VduStatus.INSTANTIATED);
			return vi;
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceName, MsoLogger.ErrorCode.BusinessProcesssError,
					"aria service output fetch failed", e);
			throw new MsoAdapterException(e.getMessage());
		}

	}

	/**
	 * Queries ARIA for VDU status.  vduInstanceId used as template and service name in ARIA (by convention).
	 */
	@Override
	public VduInfo queryVdu(String cloudSiteId, String tenantId, String vduInstanceId) throws MsoException {
		if(client == null) {
			throw new MsoAdapterException("Internal error: no ARIA connection found");
		}

		VduInfo vif = new VduInfo(vduInstanceId);
		Integer sid = serviceIds.get(vduInstanceId);
		if(sid == null) {
			// service doesn't exist
			vif.setStatus(VduStatus.NOTFOUND);
			return vif;
		}
		Service service = client.get_service(sid);
		if(service == null) {
			throw new MsoAdapterException(String.format("Internal error: cached service id %s not found in ARIA",sid)); 
		}
		Map<String,Object> voutputs = getOutputs(sid);
		vif.setOutputs(voutputs);
		vif.setInputs(inputsCache.get(vduInstanceId));
		vif.setStatus(VduStatus.INSTANTIATED);
		return vif;
	}

	@Override
	public VduInfo deleteVdu(String cloudSiteId, String tenantId, String vduInstanceId, int timeoutMinutes,
			boolean keepBlueprintLoaded) throws MsoException {
		
		if(client == null) {
			throw new MsoAdapterException("Internal error: no ARIA connection found");
		}
		Integer sid = serviceIds.get(vduInstanceId);
		VduInfo vif = new VduInfo(vduInstanceId);
		if(sid == null) {
			// service doesn't exist
			vif.setStatus(VduStatus.NOTFOUND);
			return vif;
		}
		
		/**
		 * Run uninstall
		 */
		try {
			client.start_execution( sid, "uninstall", new ExecutionDetails());
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceId, MsoLogger.ErrorCode.BusinessProcesssError,
					"aria uninstall workflow failed", e);
			throw new MsoAdapterException(e.getMessage());
		}

		/**
		 * Delete the service
		 */
		try {
			client.delete_service(sid);
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceId, MsoLogger.ErrorCode.BusinessProcesssError,
					String.format("aria service delete failed. Service id: %d",sid), e);
			throw new MsoAdapterException(e.getMessage());
		}
		
		/**
		 * Delete the blueprint
		 */
		try {
			client.delete_service_template(templateIds.get(vduInstanceId));
		}
		catch(Exception e) {
			logger.error(MessageEnum.RA_CREATE_VNF_ERR, "","","","", vduInstanceId, MsoLogger.ErrorCode.BusinessProcesssError,
					"aria template delete failed", e);
			throw new MsoAdapterException(e.getMessage());
		}
		
		vif.setStatus(VduStatus.DELETED);
		return vif;
	}

	/**
	 * Deployment update not possible with ARIA
	 */
	@Override
	public VduInfo updateVdu(String cloudSiteId, String tenantId, String vduInstanceId, VduBlueprint vduBlueprint,
			Map<String, ? extends Object> inputs, String environmentFile, int timeoutMinutes) throws MsoException {
		throw new MsoAdapterException("NOT IMPLEMENTED");
	}

	/**
	 * Nonsensical in the context of ARIA: blueprint lifespan = vdulifespan
	 */
	@Override
	public boolean isBlueprintLoaded(String cloudSiteId, String vduModelId) throws MsoException {
		throw new MsoAdapterException("NOT IMPLEMENTED");
	}

	/**
	 * Nonsensical in the context of ARIA: blueprint lifespan = vdulifespan
	 */
	@Override
	public void uploadBlueprint(String cloudSiteId, VduBlueprint vduBlueprint, boolean failIfExists)
			throws MsoException {
		throw new MsoAdapterException("NOT IMPLEMENTED");
	}

	@Override
	public boolean blueprintUploadSupported() {
		return false;
	}

	/**
	 * Private
	 */

	/**p
	 * Gets and repacks service outputs for internal use
	 * @param sid the service id (ARIA service id)
	 * @return
	 */
	private Map<String,Object> getOutputs(int sid) {
		@SuppressWarnings("unchecked")
		List<Output> outputs=(List<Output>)client.list_service_outputs(sid);
		Map<String,Object> voutputs = new HashMap<>();
		for(Output output: outputs) {
			voutputs.put(output.getName(), output.getValue());
		}
		return voutputs;
	}

	@SuppressWarnings("unchecked")
	private int getServiceId(String service_name) throws MsoAdapterException{
		int sid = -1;
		List<Service> services = (List<Service>)client.list_services();
		for(Service service:services) {
			if(service.getName().equals(service_name)) {
				sid = service.getId();
			}
		}
		if(sid == -1) {
			throw new MsoAdapterException(String.format("Internal error: just created service not found: %s",service_name));
		}
		return sid;
	}

}
