package org.onap.so.adapters.appc.orchestrator.service;

import java.util.Optional;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerCallback;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerClient;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerOrchestratorException;
import org.onap.so.adapters.appc.orchestrator.client.beans.ConfigurationParameters;
import org.onap.so.adapters.appc.orchestrator.client.beans.Identity;
import org.onap.so.adapters.appc.orchestrator.client.beans.Parameters;
import org.onap.so.adapters.appc.orchestrator.client.beans.RequestParameters;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerVm;
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class ApplicationControllerTaskImpl {

    @Autowired
    private ApplicationControllerClient appcClient;

    public Status execute(String msoRequestId, ApplicationControllerTaskRequest request,
            ApplicationControllerCallback listener)
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        Status status = null;
        GraphInventoryCommonObjectMapperProvider mapper = new GraphInventoryCommonObjectMapperProvider();
        Optional<String> payload = Optional.empty();
        String vmId = null;
        Optional<String> vserverId = Optional.empty();
        Parameters parameters = new Parameters();
        ConfigurationParameters configParams = new ConfigurationParameters();
        RequestParameters requestParams = new RequestParameters();

        switch (request.getAction()) {
            case HealthCheck:
                requestParams.setHostIpAddress(request.getApplicationControllerVnf().getVnfHostIpAddress());
                parameters.setRequestParameters(requestParams);
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case ResumeTraffic:
                configParams.setVnfName(request.getApplicationControllerVnf().getVnfName());
                parameters.setConfigurationParameters(configParams);
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case Start:
            case Stop:
                Identity identity = new Identity();
                identity.setIdentityUrl(request.getIdentityUrl());
                payload = Optional.of((mapper.getMapper().writeValueAsString(identity)));
                break;
            case Unlock:
            case Lock:
                break;
            case QuiesceTraffic:
                parameters.setOperationsTimeout(request.getOperationsTimeout());
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case DistributeTraffic:
                configParams.setBookName(request.getBookName());
                configParams.setNodeList(request.getNodeList());
                configParams.setFileParameterContent(request.getFileParameters());
                configParams.setVnfName(request.getApplicationControllerVnf().getVnfName());
                parameters.setConfigurationParameters(configParams);
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case DistributeTrafficCheck:
                configParams.setBookName(request.getBookName());
                configParams.setNodeList(request.getNodeList());
                configParams.setFileParameterContent(request.getFileParameters());
                configParams.setVnfName(request.getApplicationControllerVnf().getVnfName());
                parameters.setConfigurationParameters(configParams);
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case ConfigModify:
                requestParams.setHostIpAddress(request.getApplicationControllerVnf().getVnfHostIpAddress());
                configParams.setAdditionalProperties(request.getConfigParams());
                parameters.setRequestParameters(requestParams);
                parameters.setConfigurationParameters(configParams);
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case ConfigScaleOut:
                break;
            case UpgradePreCheck:
            case UpgradePostCheck:
            case UpgradeSoftware:
            case UpgradeBackup:
                parameters.setExistingSoftwareVersion(request.getExistingSoftwareVersion());
                parameters.setNewSoftwareVersion(request.getNewSoftwareVersion());
                payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                break;
            case ActionStatus:
                break;
            case Snapshot:
                ApplicationControllerVm applicationControllerVm =
                        request.getApplicationControllerVnf().getApplicationControllerVm();
                if (applicationControllerVm != null) {
                    vmId = request.getApplicationControllerVnf().getApplicationControllerVm().getVmId();
                    parameters.setVmId(vmId);
                    payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
                    vserverId = Optional
                            .of(request.getApplicationControllerVnf().getApplicationControllerVm().getVserverId());
                }
                break;
            default:
                // errorMessage = "Unable to idenify Action request for AppCClient";
                break;
        }

        status = appcClient.vnfCommand(request.getAction(), msoRequestId,
                request.getApplicationControllerVnf().getVnfId(), vserverId, payload, request.getControllerType(),
                listener);

        return status;
    }

}
