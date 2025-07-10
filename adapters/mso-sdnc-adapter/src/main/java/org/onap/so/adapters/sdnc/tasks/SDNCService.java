package org.onap.so.adapters.sdnc.tasks;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.DatatypeConverter;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.adapters.sdnc.impl.Constants;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.utils.CryptoUtils;
import org.onap.so.utils.ExternalTaskUtils;
import org.onap.so.utils.RetrySequenceLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SDNCService extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(SDNCService.class);

    @Autowired
    private AuditMDCSetup mdcSetup;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    private static final ObjectMapper objMapper = new ObjectMapper();

    public SDNCService() {
        super(RetrySequenceLevel.SHORT);
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    // TODO maybe make a new sdnc client
    public void executePostTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        logger.debug("Executing External Task SDNC Post Service");
        Map<String, Object> variables = new HashMap<>();
        boolean success = false;
        String errorMessage = "";
        try {
            Object request = externalTask.getVariable("sdncRequest");
            String jsonRequest = buildJsonRequest(request);

            UriBuilder url = UriBuilder.fromUri(env.getProperty(Constants.SDNC_HOST));
            url.path((String) externalTask.getVariable("sdncUri"));

            HttpEntity<String> requestEntity = new HttpEntity<String>(jsonRequest, getHttpHeader());
            ResponseEntity<Object> responseEntity =
                    restTemplate.exchange(url.build(), HttpMethod.POST, requestEntity, Object.class);

            if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                success = true;
            } else {
                errorMessage = "SDNC returned a " + responseEntity.getStatusCode().value();
            }
        } catch (Exception e) {
            logger.error("Error during External Task SDNC Post Service", e);
            errorMessage = "Error during External Task SDNC Post Service: " + e.getMessage();
        }

        if (success) {
            externalTaskService.complete(externalTask, variables);
            logger.debug("The External Task {} was Successful", externalTask.getId());
        } else {
            if (externalTask.getRetries() == null) {
                logger.debug("The External Task {} Failed, Setting Retries to Default Start Value {}",
                        externalTask.getId(), getRetrySequence().length);
                externalTaskService.handleFailure(externalTask, errorMessage, "errorDetails", getRetrySequence().length,
                        10000);
            } else if (externalTask.getRetries() != null && externalTask.getRetries() - 1 == 0) {
                logger.debug("The External Task {} Failed, All Retries Exhausted", externalTask.getId());
                variables.put("errorMessage", errorMessage);
                externalTaskService.handleBpmnError(externalTask, "SDNCWorkflowException", null, variables);
            } else {
                logger.debug("The External Task {} Failed, Decrementing Retries to {} with Retry Delay {}",
                        externalTask.getId(), externalTask.getRetries() - 1,
                        calculateRetryDelay(externalTask.getRetries()));
                externalTaskService.handleFailure(externalTask, errorMessage, "errorDetails",
                        externalTask.getRetries() - 1, calculateRetryDelay(externalTask.getRetries()));
            }
        }
    }

    public void executeGetTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        logger.debug("Executing External Task SDNC Get Service");

    }

    private String buildJsonRequest(Object request) throws JsonProcessingException {
        String jsonRequest = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
        return jsonRequest;
    }

    private HttpHeaders getHttpHeader() throws GeneralSecurityException {
        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.set("Authorization", getAuth());
        httpHeader.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptMediaTypes = new ArrayList<>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        acceptMediaTypes.add(MediaType.TEXT_PLAIN);
        httpHeader.setAccept(acceptMediaTypes);
        return httpHeader;
    }

    protected String getAuth() throws GeneralSecurityException {
        String auth = CryptoUtils.decrypt(env.getProperty(Constants.SDNC_AUTH_PROP),
                env.getProperty(Constants.ENCRYPTION_KEY_PROP));
        return "Basic " + DatatypeConverter.printBase64Binary(auth.getBytes());
    }

}
