package org.onap.so.client.namingservice;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.onap.namingservice.model.NameGenDeleteResponse;
import org.onap.namingservice.model.NameGenResponse;
import org.onap.namingservice.model.NameGenResponseError;
import org.onap.namingservice.model.Respelement;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class NamingClientResponseValidator {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NamingClientResponseValidator.class);
	private static final String INSTANCE_GROUP_NAME = "instance-group-name";
	private static final String NO_RESPONSE_FROM_NAMING_SERVICE = "Error did not receive a response from Naming Service.";
	private static final String NULL_RESPONSE_FROM_NAMING_SERVICE = "Error received a null response from Naming Service.";
	private static final String NAMING_SERVICE_ERROR = "Error from Naming Service: %s";
	
	public String validateNameGenResponse(ResponseEntity<NameGenResponse> response) throws BadResponseException {
		if (response == null) {
			msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, NO_RESPONSE_FROM_NAMING_SERVICE, "BPMN",
					MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NO_RESPONSE_FROM_NAMING_SERVICE);
			throw new BadResponseException(NO_RESPONSE_FROM_NAMING_SERVICE);
		}
		       
        int responseCode = response.getStatusCodeValue();
        String generatedName = "";
        NameGenResponse responseBody = response.getBody();
        if (responseBody == null) {
        	msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, NULL_RESPONSE_FROM_NAMING_SERVICE, "BPMN",
					MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NULL_RESPONSE_FROM_NAMING_SERVICE);
			throw new BadResponseException(NULL_RESPONSE_FROM_NAMING_SERVICE);
		}             
		
		if (isHttpCodeSuccess(responseCode)) {
			msoLogger.info("Successful Response from Naming Service");			
			List<Respelement> respList = responseBody.getElements();
			
			if (respList != null) {
				for (int i=0; i < respList.size(); i++) {
					Respelement respElement = respList.get(i);
					if (respElement != null) {
						String resourceName = respElement.getResourceName();
						if (INSTANCE_GROUP_NAME.equals(resourceName)) {
							generatedName = respElement.getResourceValue();
							break;
						}
					}
				}
			}			
			return generatedName;
		} else {
			NameGenResponseError error = responseBody.getError();
			String errorMessageString = NAMING_SERVICE_ERROR;
			if (error != null) {
				errorMessageString = error.getMessage();
			}
			String errorMessage = String.format(NAMING_SERVICE_ERROR, errorMessageString);
			msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, errorMessage, "BPMN", MsoLogger.getServiceName(),
					MsoLogger.ErrorCode.DataError, errorMessage);
			throw new BadResponseException(errorMessage);
		}		
	}
	
	public String validateNameGenDeleteResponse(ResponseEntity<NameGenDeleteResponse> response) throws BadResponseException {
		if (response == null) {
			msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, NO_RESPONSE_FROM_NAMING_SERVICE, "BPMN",
					MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NO_RESPONSE_FROM_NAMING_SERVICE);
			throw new BadResponseException(NO_RESPONSE_FROM_NAMING_SERVICE);
		}
		       
        int responseCode = response.getStatusCodeValue();
        String responseMessage = "";
        NameGenDeleteResponse responseBody = response.getBody();
        if (responseBody == null) {
        	msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, NULL_RESPONSE_FROM_NAMING_SERVICE, "BPMN",
					MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NULL_RESPONSE_FROM_NAMING_SERVICE);
			throw new BadResponseException(NULL_RESPONSE_FROM_NAMING_SERVICE);
		}             
		
		if (isHttpCodeSuccess(responseCode)) {
			msoLogger.info("Successful Response from Naming Service");
			return responseMessage;
		} else {
			String errorMessageString = NAMING_SERVICE_ERROR;
			
			String errorMessage = String.format(NAMING_SERVICE_ERROR, errorMessageString);
			msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, errorMessage, "BPMN", MsoLogger.getServiceName(),
					MsoLogger.ErrorCode.DataError, errorMessage);
			throw new BadResponseException(errorMessage);
		}		
	}
	
	private boolean isHttpCodeSuccess(int code) {
        return code >= HttpStatus.SC_OK && code < HttpStatus.SC_MULTIPLE_CHOICES || code == 0;
    }
	
	protected String formatError(HttpStatusCodeException e) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		NameGenResponse errorResponse = mapper.readValue(e.getResponseBodyAsString(), NameGenResponse.class);
		NameGenResponseError error = errorResponse.getError();
		
		String errorMessageString = null;
		if (error != null) {
			errorMessageString = error.getMessage();
		}
		String errorMessage = String.format(NAMING_SERVICE_ERROR, errorMessageString);
		msoLogger.error(MessageEnum.RA_GENERAL_EXCEPTION, errorMessage, "BPMN", MsoLogger.getServiceName(),
				MsoLogger.ErrorCode.DataError, errorMessage);
		return errorMessage;
	}

}
