package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.request.information.RequestInformation;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.sdnc.request.header.SdncRequestHeader;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkRpcInputEntityBuilder implements AbstractBuilder<Map<String, String>, NetworkRpcInputEntity> {

    public static final String SVC_REQUEST_ID = "MSO";
    public static final String SDC_ACTION = "SDC_ACTION";
    public static final RequestInformation.RequestAction REQUEST_ACTION = RequestInformation.RequestAction.CreateNetworkInstance;

    protected NetworkRpcInputEntity getSdncEntityInput(Map<String, String> inputs) {
        NetworkRpcInputEntity networkRpcInputEntity = new NetworkRpcInputEntity();
        InputEntity inputEntity = new InputEntity();
        {
            loadSdncRequestHeaderEntity(inputs, inputEntity);
            loadRequestInformationEntity(inputEntity);

            ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
            String serviceId = inputs.get("serviceId");
            serviceInformationEntity.setServiceId(serviceId);


            loadNetwrokRequestInputEntity(inputs, inputEntity);
        }
        networkRpcInputEntity.setInput(inputEntity);
        return networkRpcInputEntity;
    }

    private void loadNetwrokRequestInputEntity(Map<String, String> inputs, InputEntity inputEntity) {
        NetworkRequestInputEntity networkRequestInputEntity = new NetworkRequestInputEntity();
        {
            NetworkInputPaarametersEntity networkInputPaarametersEntity = new NetworkInputPaarametersEntity();
            {
                List<ParamEntity> paramEntityList = getParamEntities(inputs);
                networkInputPaarametersEntity.setParamList(paramEntityList);
            }

        }
        inputEntity.setNetworkRequestInput(networkRequestInputEntity);
    }

    private List<ParamEntity> getParamEntities(Map<String, String> inputs) {
        List<ParamEntity> paramEntityList = new ArrayList<>();
        if (inputs != null && !inputs.isEmpty()) {
            inputs.keySet().forEach(key -> {
                ParamEntity paramEntity = new ParamEntity();
                paramEntity.setName(key);
                paramEntity.setValue(inputs.get(key));
                paramEntityList.add(paramEntity);
            });
        }
        return paramEntityList;
    }

    private void loadRequestInformationEntity(InputEntity inputEntity) {
        RequestInformationEntity requestInformationEntity = new RequestInformationEntity();
        {
            requestInformationEntity.setRequestId(SVC_REQUEST_ID);
            requestInformationEntity.setRequestAction(REQUEST_ACTION.name());
        }
        inputEntity.setRequestInformation(requestInformationEntity);
    }

    private void loadSdncRequestHeaderEntity(Map<String, String> inputs, InputEntity inputEntity) {
        SdncRequestHeaderEntity sdncRequestHeaderEntity = new SdncRequestHeaderEntity();
        {
            sdncRequestHeaderEntity.setSvcRequestId(SVC_REQUEST_ID);
            String action = inputs.get(SDC_ACTION);
            if (!StringUtils.isBlank(action)) {
                if (action.toLowerCase().contains("delete")) {
                    action = SdncRequestHeader.SvcAction.Delete.name();
                } else if (action.toLowerCase().contains("create")) {
                    action = SdncRequestHeader.SvcAction.Create.name();
                }
            }
            sdncRequestHeaderEntity.setSvcAction(action);
        }
        inputEntity.setSdncRequestHeader(sdncRequestHeaderEntity);
    }

    @Override
    public NetworkRpcInputEntity build(Map<String, String> input) {
        return null;
    }
}
