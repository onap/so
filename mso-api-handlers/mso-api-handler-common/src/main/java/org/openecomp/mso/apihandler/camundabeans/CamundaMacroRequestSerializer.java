package org.openecomp.mso.apihandler.camundabeans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Class used to create data object and serialize it to JSON which the BPEL macro flow understands.
 */
public class CamundaMacroRequestSerializer  {

    private CamundaMacroRequestSerializer(){}

    public static String getJsonRequest(String requestId, String action, String serviceInstanceId)throws JsonProcessingException{
        CamundaMacroRequest macroRequest = new CamundaMacroRequest();
        macroRequest.setAction(getCamundaInput(action));
        macroRequest.setRequestId(getCamundaInput(requestId));
        macroRequest.setServiceInstanceId(getCamundaInput(serviceInstanceId));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        return mapper.writeValueAsString(macroRequest);
    }

    private static CamundaInput getCamundaInput(String value){
        CamundaInput input = new CamundaInput();
        input.setType("String");
        input.setValue(value);
        return input;
    }
}
