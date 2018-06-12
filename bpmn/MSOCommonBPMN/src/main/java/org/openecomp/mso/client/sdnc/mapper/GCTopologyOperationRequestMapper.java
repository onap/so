package org.openecomp.mso.client.sdnc.mapper;

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.*;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.springframework.stereotype.Component;

@Component(value = "sdncGCTopologyOperationRequestMapper")
public class GCTopologyOperationRequestMapper {

    private static final GeneralTopologyObjectMapper generalTopologyObjectMapper = new GeneralTopologyObjectMapper();

    public GenericResourceApiGcTopologyOperationInformation assignOrActivateVnrReqMapper(SDNCSvcAction svcAction,
                                                                                         GenericResourceApiRequestActionEnumeration reqAction,
                                                                                         ServiceInstance serviceInstance,
                                                                                         RequestContext requestContext,
                                                                                         Customer customer,
                                                                                         Configuration vnrConfiguration,
                                                                                         GenericVnf voiceVnf) {

        GenericResourceApiGcTopologyOperationInformation req = new GenericResourceApiGcTopologyOperationInformation();
        String sdncReqId = requestContext.getMsoRequestId();
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId);// TODO Set URL
        GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper.buildGenericResourceApiRequestinformationRequestInformation(sdncReqId, reqAction);
        GenericResourceApiServiceinformationServiceInformation serviceInformation = generalTopologyObjectMapper.buildServiceInformation(serviceInstance, requestContext, customer, false);
        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation = generalTopologyObjectMapper.buildConfigurationInformation(vnrConfiguration,true);
        GenericResourceApiGcrequestinputGcRequestInput gcRequestInput = generalTopologyObjectMapper.buildGcRequestInformation(voiceVnf,null);
        req.setRequestInformation(requestInformation);
        req.setSdncRequestHeader(sdncRequestHeader);
        req.setServiceInformation(serviceInformation);
        req.setConfigurationInformation(configurationInformation);
        req.setGcRequestInput(gcRequestInput);

        return req;

    }


    public GenericResourceApiGcTopologyOperationInformation deactivateOrUnassignVnrReqMapper(SDNCSvcAction svcAction,
                                                                                             ServiceInstance serviceInstance,
                                                                                             RequestContext requestContext,
                                                                                             Configuration vnrConfiguration) {

        GenericResourceApiGcTopologyOperationInformation req = new GenericResourceApiGcTopologyOperationInformation();
        String sdncReqId = requestContext.getMsoRequestId();
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader =
                generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId);// TODO Set URL
        GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper
                .buildGenericResourceApiRequestinformationRequestInformation(sdncReqId,
                        GenericResourceApiRequestActionEnumeration.DELETEGENERICCONFIGURATIONINSTANCE);
        GenericResourceApiServiceinformationServiceInformation serviceInformation = new GenericResourceApiServiceinformationServiceInformation();
        serviceInformation.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation =
                new GenericResourceApiConfigurationinformationConfigurationInformation();
        configurationInformation.setConfigurationId(vnrConfiguration.getConfigurationId());
        req.setRequestInformation(requestInformation);
        req.setSdncRequestHeader(sdncRequestHeader);
        req.setServiceInformation(serviceInformation);
        req.setConfigurationInformation(configurationInformation);
        return req;

    }


}
