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
package org.openecomp.mso.aria;

import com.gigaspaces.aria.rest.client.AriaClient;
import com.gigaspaces.aria.rest.client.AriaClientFactory;
import com.gigaspaces.aria.rest.client.ExecutionDetails;
import com.gigaspaces.aria.rest.client.Input;
import com.gigaspaces.aria.rest.client.InputImpl;
import com.gigaspaces.aria.rest.client.Output;
import com.gigaspaces.aria.rest.client.Service;
import com.gigaspaces.aria.rest.client.ServiceTemplate;
import com.gigaspaces.aria.rest.client.ServiceTemplateImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.mso.adapters.vdu.CloudInfo;
import org.openecomp.mso.adapters.vdu.VduException;
import org.openecomp.mso.adapters.vdu.VduInstance;
import org.openecomp.mso.adapters.vdu.VduModelInfo;
import org.openecomp.mso.adapters.vdu.VduPlugin;
import org.openecomp.mso.adapters.vdu.VduStateType;
import org.openecomp.mso.adapters.vdu.VduStatus;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * ARIA VDU Plugin. Pluggable interface for the ARIA REST API to support TOSCA orchestration.
 *
 * @author DeWayne
 */
public class AriaVduPlugin implements VduPlugin {
    private static final String API_VERSION = "0.1";
    private static final MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
    private AriaClient client = null;
    private Map<String, Integer> templateIds = new HashMap<>();
    private Map<String, Integer> serviceIds = new HashMap<>();
    private Map<String, Map<String, Object>> inputsCache = new HashMap<>();

    public AriaVduPlugin() {
        super();
    }

    public AriaVduPlugin(String host, int port) {
        try {
            client = new AriaClientFactory().createRestClient("http", host, port, API_VERSION);
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    "aria",
                    MsoLogger.ErrorCode.AvailabilityError,
                    "Connection to ARIA REST API failed",
                    e);
            throw e;
        }
    }

    /**
     * Instantiate VDU in ARIA. <code>instanceName</code> is used for both service template name and
     * service name.
     */
    @SuppressWarnings("unchecked")
    @Override
    public VduInstance instantiateVdu(
            CloudInfo cloudInfo,
            String instanceName,
            Map<String, Object> inputs,
            VduModelInfo vduModel,
            boolean rollbackOnFailure)
            throws VduException {

        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        // Currently only support simple CSAR with single main template
        byte[] csar = new CSAR(vduModel).create();

        ServiceTemplate template = new ServiceTemplateImpl(instanceName, csar);
        try {
            client.install_service_template(template);
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceName,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    "instantiate vdu via csar failed",
                    e);
            throw new VduException(e.getMessage());
        }

        /** Create a service */
        try {
            int templateId = -1;
            for (ServiceTemplate stemplate :
                    (List<ServiceTemplate>) client.list_service_templates()) {
                if (stemplate.getName().equals(instanceName)) {
                    templateId = stemplate.getId();
                }
            }
            List<Input> sinputs = new ArrayList<Input>();
            for (Map.Entry<String, ? extends Object> entry : inputs.entrySet()) {
                Input inp = new InputImpl(entry.getKey(), entry.getValue().toString(), "");
                sinputs.add(inp);
            }
            client.create_service(templateId, instanceName, sinputs);
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceName,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    "aria service creation failed",
                    e);
            throw new VduException(e.getMessage());
        }

        // Get the service ID and cache it
        int sid = getServiceId(instanceName);
        serviceIds.put(instanceName, sid);

        /** Run install */
        try {
            client.start_execution(sid, "install", new ExecutionDetails());
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceName,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    "aria install workflow failed",
                    e);
            throw new VduException(e.getMessage());
        }

        /** Get the outputs and return */
        try {
            Map<String, Object> voutputs = getOutputs(sid);

            VduInstance vi = new VduInstance();
            vi.setVduInstanceName(instanceName);
            vi.setInputs((Map<String, Object>) inputs);
            inputsCache.put(instanceName, vi.getInputs());
            vi.setOutputs(voutputs);
            vi.setStatus(new VduStatus(VduStateType.INSTANTIATED));
            return vi;
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceName,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    "aria service output fetch failed",
                    e);
            throw new VduException(e.getMessage());
        }
    }

    /**
     * Queries ARIA for VDU status. instanceId used as template and service name in ARIA (by
     * convention).
     */
    @Override
    public VduInstance queryVdu(CloudInfo cloudInfo, String instanceId) throws VduException {
        if (client == null) {
            throw new VduException("Internal error: no ARIA connection found");
        }

        VduInstance vif = new VduInstance();
        vif.setVduInstanceId(instanceId);
        Integer sid = serviceIds.get(instanceId);
        if (sid == null) {
            // service doesn't exist
            vif.setStatus(new VduStatus(VduStateType.NOTFOUND));
            return vif;
        }
        Service service = client.get_service(sid);
        if (service == null) {
            throw new VduException(
                    String.format("Internal error: cached service id %s not found in ARIA", sid));
        }
        Map<String, Object> voutputs = getOutputs(sid);
        vif.setOutputs(voutputs);
        vif.setInputs(inputsCache.get(instanceId));
        vif.setStatus(new VduStatus(VduStateType.INSTANTIATED));
        return vif;
    }

    @Override
    public VduInstance deleteVdu(CloudInfo cloudInfo, String instanceId, int timeoutMinutes)
            throws VduException {
        VduInstance vif = new VduInstance();
        vif.setVduInstanceId(instanceId);

        if (client == null) {
            throw new VduException("Internal error: no ARIA connection found");
        }
        Integer sid = serviceIds.get(instanceId);
        if (sid == null) {
            // service doesn't exist
            vif.setStatus(new VduStatus(VduStateType.NOTFOUND));
            return vif;
        }

        /** Run uninstall */
        try {
            client.start_execution(sid, "uninstall", new ExecutionDetails());
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceId,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    "aria uninstall workflow failed",
                    e);
            throw new VduException(e.getMessage());
        }

        /** Delete the service */
        try {
            client.delete_service(sid);
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceId,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    String.format("aria service delete failed. Service id: %d", sid),
                    e);
            throw new VduException(e.getMessage());
        }

        /** Delete the blueprint */
        try {
            client.delete_service_template(templateIds.get(instanceId));
        } catch (Exception e) {
            logger.error(
                    MessageEnum.RA_CREATE_VNF_ERR,
                    "",
                    "",
                    "",
                    "",
                    instanceId,
                    MsoLogger.ErrorCode.BusinessProcesssError,
                    "aria template delete failed",
                    e);
            throw new VduException(e.getMessage());
        }

        vif.setStatus(new VduStatus(VduStateType.DELETED));
        return vif;
    }

    /** Deployment update not possible with ARIA */
    @Override
    public VduInstance updateVdu(
            CloudInfo cloudInfo,
            String instanceId,
            Map<String, Object> inputs,
            VduModelInfo vduModel,
            boolean rollbackOnFailure)
            throws VduException {
        throw new VduException("NOT IMPLEMENTED");
    }

    /** Private */

    /**
     * p Gets and repacks service outputs for internal use
     *
     * @param sid the service id (ARIA service id)
     * @return
     */
    private Map<String, Object> getOutputs(int sid) {
        @SuppressWarnings("unchecked")
        List<Output> outputs = (List<Output>) client.list_service_outputs(sid);
        Map<String, Object> voutputs = new HashMap<>();
        for (Output output : outputs) {
            voutputs.put(output.getName(), output.getValue());
        }
        return voutputs;
    }

    @SuppressWarnings("unchecked")
    private int getServiceId(String service_name) throws VduException {
        int sid = -1;
        List<Service> services = (List<Service>) client.list_services();
        for (Service service : services) {
            if (service.getName().equals(service_name)) {
                sid = service.getId();
            }
        }
        if (sid == -1) {
            throw new VduException(
                    String.format(
                            "Internal error: just created service not found: %s", service_name));
        }
        return sid;
    }
}
