package org.onap.so.adapters.cnf.service;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.adapters.cnf.exceptions.ApplicationException;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class CnfAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterService.class);

    @Autowired
    private RestTemplate restTemplate;

    private static final String INSTANCE_CREATE_PATH = "/v1/instance";

    public InstanceResponse createInstance(BpmnInstanceRequest bpmnInstanceRequest) {

        try {

            logger.info("CnfAdapterService createInstance called");

            MulticloudInstanceRequest multicloudInstanceRequest = new MulticloudInstanceRequest();
            InstanceResponse instanceResponse = null;

            if (bpmnInstanceRequest.getK8sRBProfileName() != null) {
                multicloudInstanceRequest.setCloudRegion(bpmnInstanceRequest.getCloudRegionId());
                multicloudInstanceRequest.setLabels(bpmnInstanceRequest.getLabels());
                multicloudInstanceRequest.setOverrideValues(bpmnInstanceRequest.getOverrideValues());
                multicloudInstanceRequest.setProfileName(bpmnInstanceRequest.getK8sRBProfileName());
                multicloudInstanceRequest.setRbName(bpmnInstanceRequest.getModelInvariantId());
                multicloudInstanceRequest.setRbVersion(bpmnInstanceRequest.getModelVersionId());
                multicloudInstanceRequest.setVfModuleUuid(bpmnInstanceRequest.getVfModuleUUID());
            } else {

                logger.error("k8sProfileName should not be null");
                return instanceResponse;
            }
            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String endpoint = UriBuilder.fromUri(uri).path(INSTANCE_CREATE_PATH).build().toString();
            HttpEntity<?> entity = getHttpEntity(multicloudInstanceRequest);
            ResponseEntity<InstanceResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.POST, entity, InstanceResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    protected HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        /*
         * try { String userCredentials = CryptoUtils.decrypt(env.getRequiredProperty("mso.cnf.adapter.auth"),
         * env.getRequiredProperty("mso.msoKey")); if (userCredentials != null) { headers.add(HttpHeaders.AUTHORIZATION,
         * "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes())); } } catch
         * (GeneralSecurityException e) { logger.error("Security exception", e); }
         */
        return headers;
    }

    protected HttpEntity<?> getHttpEntity(MulticloudInstanceRequest request) {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(request, headers);
    }

}
