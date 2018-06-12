package org.openecomp.mso.client.sdn.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.client.sdnc.SDNCClient;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;

@Component
public class SdnCommonTasks {

    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCClient.class);
    private static final String RESPONSE_CODE = "response-code";
    private static final String RESPONSE_MESSAGE = "response-message";
    private static final String NO_RESPONSE_FROM_SDNC = "Error did not receive a response from SDNC.";
    private static final String SDNC_CODE_NOT_0_OR_IN_200_299 = "Error from SDNC: Code is not 200-299 or 0. %s";
    private static final String COULD_NOT_CONVERT_SDNC_POJO_TO_JSON = "ERROR: Could not convert SDNC pojo to json string.";

    /***
     * 
     * @param request
     * @return
     * @throws MapperException
     */
    public String buildJsonRequest(Object request) throws MapperException {
        String jsonRequest;
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            jsonRequest = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            msoLogger.error(MessageEnum.JAXB_EXCEPTION, COULD_NOT_CONVERT_SDNC_POJO_TO_JSON, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, e.getMessage());
            throw new MapperException(COULD_NOT_CONVERT_SDNC_POJO_TO_JSON);
        }
        jsonRequest = "{\"input\":" + jsonRequest + "}";
        msoLogger.info(jsonRequest);
        return jsonRequest;
    }

    /***
     * 
     * @param auth
     * @return
     */
    public HttpHeaders getHttpHeaders(String auth) {
        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.set("Authorization", auth);
        httpHeader.setContentType(MediaType.APPLICATION_JSON);
        return httpHeader;
    }

    /***
     * 
     * @param output
     * @return
     * @throws BadResponseException
     */
    public String validateSDNResponse(LinkedHashMap<?, ?> output) throws BadResponseException {
        if (CollectionUtils.isEmpty(output)) {
            msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, NO_RESPONSE_FROM_SDNC, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NO_RESPONSE_FROM_SDNC);
            throw new BadResponseException(NO_RESPONSE_FROM_SDNC);
        }
        String responseCode = (String) output.get(RESPONSE_CODE);
        String responseMessage = (String) output.get(RESPONSE_MESSAGE);
        msoLogger.info("ResponseCode: " + responseCode + " ResponseMessage: " + responseMessage);
        int code = StringUtils.isNotEmpty(responseCode) ? Integer.parseInt(responseCode) : 0;
        if (isHttpCodeSuccess(code)) {
            msoLogger.info("Successful Response from SDNC");
            return responseMessage;
        } else {
            String errorMessage = String.format(SDNC_CODE_NOT_0_OR_IN_200_299, responseMessage);
            msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, errorMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, errorMessage);
            throw new BadResponseException(errorMessage);
        }
    }

    private boolean isHttpCodeSuccess(int code) {
        return code >= HttpStatus.SC_OK && code < HttpStatus.SC_MULTIPLE_CHOICES || code == 0;
    }
}
