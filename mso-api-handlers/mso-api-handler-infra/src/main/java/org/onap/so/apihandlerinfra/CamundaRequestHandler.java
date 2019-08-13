package org.onap.so.apihandlerinfra;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.http.HttpStatus;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class CamundaRequestHandler {

    private static Logger logger = LoggerFactory.getLogger(CamundaRequestHandler.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    public ResponseEntity<List<HistoricProcessInstanceEntity>> getCamundaProcessInstanceHistory(String requestId) {
        RetryTemplate retryTemplate = setRetryTemplate();
        String path = env.getProperty("mso.camunda.rest.history.uri") + requestId;
        String targetUrl = env.getProperty("mso.camundaURL") + path;
        HttpHeaders headers =
                setCamundaHeaders(env.getRequiredProperty("mso.camundaAuth"), env.getRequiredProperty("mso.msoKey"));

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        return retryTemplate.execute(context -> {
            if (context.getLastThrowable() != null) {
                logger.error("Retrying: Last call resulted in exception: ", context.getLastThrowable());
            }
            if (context.getRetryCount() == 0) {
                logger.info("Querying Camunda for process-instance history for requestId: {}", requestId);
            } else {
                logger.info("Retry: {} of 3. Querying Camunda for process-instance history for requestId: {}",
                        context.getRetryCount(), requestId);
            }
            return restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
        });
    }

    protected ResponseEntity<List<HistoricActivityInstanceEntity>> getCamundaActivityHistory(String processInstanceId,
            String requestId) throws ContactCamundaException {
        RetryTemplate retryTemplate = setRetryTemplate();
        String path = env.getProperty("mso.camunda.rest.activity.uri") + processInstanceId;
        String targetUrl = env.getProperty("mso.camundaURL") + path;
        HttpHeaders headers =
                setCamundaHeaders(env.getRequiredProperty("mso.camundaAuth"), env.getRequiredProperty("mso.msoKey"));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        try {
            return retryTemplate.execute(context -> {
                if (context.getLastThrowable() != null) {
                    logger.error("Retrying: Last call resulted in exception: ", context.getLastThrowable());
                }
                if (context.getRetryCount() == 0) {
                    logger.info(
                            "Querying Camunda for activity-instance history for processInstanceId: {}, for requestId: {}",
                            processInstanceId, requestId);
                } else {
                    logger.info(
                            "Retry: {} of 3. Querying Camunda for activity-instance history for processInstanceId: {}, for requestId: {}",
                            context.getRetryCount(), processInstanceId, requestId);
                }

                return restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<List<HistoricActivityInstanceEntity>>() {});
            });

        } catch (RestClientException e) {
            logger.error(
                    "Error querying Camunda for activity-instance history for processInstanceId: {}, for requestId: {}, exception: {}",
                    processInstanceId, requestId, e.getMessage());
            throw new ContactCamundaException.Builder("activity-instance", requestId, e.toString(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).build();
        }
    }

    protected String getTaskName(String requestId) throws ContactCamundaException {
        ResponseEntity<List<HistoricProcessInstanceEntity>> response = null;
        ResponseEntity<List<HistoricActivityInstanceEntity>> activityResponse = null;
        String processInstanceId = null;
        try {
            response = getCamundaProcessInstanceHistory(requestId);
        } catch (RestClientException e) {
            logger.error("Error querying Camunda for process-instance history for requestId: {}, exception: {}",
                    requestId, e.getMessage());
            throw new ContactCamundaException.Builder("process-instance", requestId, e.toString(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).build();
        }

        List<HistoricProcessInstanceEntity> historicProcessInstanceList = response.getBody();

        if (historicProcessInstanceList != null && !historicProcessInstanceList.isEmpty()) {
            Collections.reverse(historicProcessInstanceList);
            processInstanceId = historicProcessInstanceList.get(0).getId();
        } else {
            return "No processInstances returned for requestId: " + requestId;
        }

        if (processInstanceId != null) {
            activityResponse = getCamundaActivityHistory(processInstanceId, requestId);
        } else {
            return "No processInstanceId returned for requestId: " + requestId;
        }

        return getActivityName(activityResponse.getBody());
    }

    protected String getActivityName(List<HistoricActivityInstanceEntity> activityInstanceList) {
        String activityName = null;
        HistoricActivityInstanceEntity activityInstance = null;
        String result = null;

        if (activityInstanceList == null || activityInstanceList.isEmpty()) {
            result = "No results returned on activityInstance history lookup.";
        } else {
            activityInstance = activityInstanceList.get(0);
            activityName = activityInstance.getActivityName();

            if (activityName == null) {
                result = "Task name is null.";
            } else {
                result = "Last task executed: " + activityName;
            }
        }

        return result;
    }

    protected HttpHeaders setCamundaHeaders(String auth, String msoKey) {
        HttpHeaders headers = new HttpHeaders();
        List<org.springframework.http.MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        try {
            String userCredentials = CryptoUtils.decrypt(auth, msoKey);
            if (userCredentials != null) {
                headers.add(HttpHeaders.AUTHORIZATION,
                        "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes()));
            }
        } catch (GeneralSecurityException e) {
            logger.error("Security exception", e);
        }
        return headers;
    }

    protected RetryTemplate setRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(ResourceAccessException.class, true);
        SimpleRetryPolicy policy = new SimpleRetryPolicy(4, retryableExceptions);
        retryTemplate.setRetryPolicy(policy);
        return retryTemplate;
    }
}
