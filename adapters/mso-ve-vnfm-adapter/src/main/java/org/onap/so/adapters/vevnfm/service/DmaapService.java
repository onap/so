package org.onap.so.adapters.vevnfm.service;

import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DmaapService {

    private static final Logger logger = LoggerFactory.getLogger(DmaapService.class);

    @Value("${dmaap.endpoint}")
    private String endpoint;

    @Value("${dmaap.topic}")
    private String topic;

    @Autowired
    private HttpRestServiceProvider restProvider;

    public void send(final VnfLcmOperationOccurrenceNotification notification) {
        try {
            final ResponseEntity<String> response = restProvider.postHttpRequest(notification, getUrl(), String.class);
            final HttpStatus statusCode = response.getStatusCode();
            final String body = response.getBody();

            logger.info("The DMaaP replied with the code {} and the body {}", statusCode, body);
        } catch (Exception e) {
            logger.warn("An issue connecting to DMaaP", e);
        }
    }

    private String getUrl() {
        return endpoint + topic;
    }
}
