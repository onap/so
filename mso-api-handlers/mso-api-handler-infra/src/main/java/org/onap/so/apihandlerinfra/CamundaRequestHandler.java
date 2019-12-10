package org.onap.so.apihandlerinfra;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
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
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class CamundaRequestHandler {

    private static Logger logger = LoggerFactory.getLogger(CamundaRequestHandler.class);
    private static final String TIMEOUT = "30000";
    private static final String RETRY_TIMEOUT = "15000";
    private static final String TIMEOUT_PROPERTY = "mso.camunda.request.timeout";
    private static final String RETRY_TIMEOUT_PROPERTY = "mso.camunda.request.timeout.retry";

    @Autowired
    private Environment env;

    public ResponseEntity<List<HistoricProcessInstanceEntity>> getCamundaProcessInstanceHistory(String requestId,
            boolean retry) {
        String path = env.getProperty("mso.camunda.rest.history.uri") + requestId;
        String targetUrl = env.getProperty("mso.camundaURL") + path;
        HttpHeaders headers =
                setCamundaHeaders(env.getRequiredProperty("mso.camundaAuth"), env.getRequiredProperty("mso.msoKey"));

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        if (retry) {
            RestTemplate restTemplate = getRestTemplate(retry);
            RetryTemplate retryTemplate = getRetryTemplate();
            return retryTemplate.execute(context -> {
                if (context.getLastThrowable() != null) {
                    logger.error("Retrying: Last call resulted in exception: ", context.getLastThrowable());
                }
                if (context.getRetryCount() == 0) {
                    logger.info("Querying Camunda for process-instance history for requestId: {}", requestId);
                } else {
                    logger.info(
                            "Retry: Querying Camunda for process-instance history for retryCount: {} and requestId: {}",
                            context.getRetryCount(), requestId);
                }
                return restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
            });
        } else {
            RestTemplate restTemplate = getRestTemplate(retry);
            return restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
        }
    }

    protected ResponseEntity<List<HistoricActivityInstanceEntity>> getCamundaActivityHistory(String processInstanceId) {
        RestTemplate restTemplate = getRestTemplate(false);
        String path = env.getProperty("mso.camunda.rest.activity.uri") + processInstanceId;
        String targetUrl = env.getProperty("mso.camundaURL") + path;
        HttpHeaders headers =
                setCamundaHeaders(env.getRequiredProperty("mso.camundaAuth"), env.getRequiredProperty("mso.msoKey"));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricActivityInstanceEntity>>() {});
    }

    protected String getTaskName(String requestId) {
        ResponseEntity<List<HistoricProcessInstanceEntity>> response = null;

        String taskInformation = null;
        try {
            response = getCamundaProcessInstanceHistory(requestId, false);
        } catch (RestClientException e) {
            logger.warn("Error querying Camunda for process-instance history for requestId: {}, exception: {}",
                    requestId, e.getMessage());
        }

        if (response != null) {
            taskInformation = getTaskInformation(response, requestId);
        }
        return taskInformation;
    }

    protected String getTaskInformation(ResponseEntity<List<HistoricProcessInstanceEntity>> response,
            String requestId) {
        List<HistoricProcessInstanceEntity> historicProcessInstanceList = response.getBody();
        ResponseEntity<List<HistoricActivityInstanceEntity>> activityResponse = null;
        String processInstanceId = null;
        String taskInformation = null;

        if (historicProcessInstanceList != null && !historicProcessInstanceList.isEmpty()) {
            Collections.reverse(historicProcessInstanceList);
            processInstanceId = historicProcessInstanceList.get(0).getId();
        } else {
            logger.warn("No processInstances returned for requestId: {} to get TaskInformation", requestId);
        }

        if (processInstanceId != null) {
            try {
                activityResponse = getCamundaActivityHistory(processInstanceId);
            } catch (RestClientException e) {
                logger.warn(
                        "Error querying Camunda for activity-instance history for processInstanceId: {}, for requestId: {}, exception: {}",
                        processInstanceId, requestId, e.getMessage());
            }
        } else {
            logger.warn("No processInstanceId returned for requestId: {} to get TaskInformation", requestId);
        }

        if (activityResponse != null) {
            taskInformation = getActivityName(activityResponse.getBody());
        } else {
            logger.warn("No activity history information returned for requestId: {} to get TaskInformation", requestId);
        }
        return taskInformation;
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

    protected RetryTemplate getRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(ResourceAccessException.class, true);
        SimpleRetryPolicy policy = new SimpleRetryPolicy(2, retryableExceptions);
        retryTemplate.setRetryPolicy(policy);
        return retryTemplate;
    }

    protected RestTemplate getRestTemplate(boolean retry) {
        int timeout;
        if (retry) {
            timeout = Integer.parseInt(env.getProperty(RETRY_TIMEOUT_PROPERTY, RETRY_TIMEOUT));
        } else {
            timeout = Integer.parseInt(env.getProperty(TIMEOUT_PROPERTY, TIMEOUT));
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(timeout);
        factory.setReadTimeout(timeout);
        factory.setConnectTimeout(timeout);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }
}
