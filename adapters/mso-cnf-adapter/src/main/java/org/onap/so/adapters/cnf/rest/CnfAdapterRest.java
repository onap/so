package org.onap.so.adapters.cnf.rest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.so.adapters.cnf.model.ConfigTemplateEntity;
import org.onap.so.adapters.cnf.model.ConfigurationEntity;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
import org.onap.so.adapters.cnf.model.InstanceEntity;
import org.onap.so.adapters.cnf.model.ProfileEntity;
import org.onap.so.adapters.cnf.model.ResourceBundleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RestController
public class CnfAdapterRest {

    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterRest.class);
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/healthcheck"}, method = RequestMethod.GET,
            produces = "application/json")
    public String healthCheck() throws Exception {

        logger.info("health check called.");

        // TODO
        HttpGet req = new HttpGet("https://localhost:32780/api/multicloud-k8s/v1/healthcheck");
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/rb/definition"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createRB(@RequestBody ResourceBundleEntity rB) throws Exception {

        logger.info("ResourceBundleEntity:" + rB.toString());

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("https://localhost:32780/api/multicloud-k8s/v1/v1/rb/definition");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String requestBody = objectMapper.writeValueAsString(rB);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/rb/definition/{rb-name}/{rb-version}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getRB(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion)
            throws Exception {

        logger.info("get RB called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet(
                "https://localhost:32780/api/multicloud-k8s/v1/v1/rb/definition/" + rbName + "/" + rbVersion);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.POST, produces = "application/json")
    public String createProfile(@RequestBody ProfileEntity fE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion) throws Exception {

        logger.info("create Profile called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://localhost:32780/api/multicloud-k8s/v1/v1/rb/definition/" + rbName + "/"
                + rbVersion + "/profile");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(fE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("pr-name") String prName) throws Exception {

        logger.info("get Profile called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("https://localhost:32780/api/multicloud-k8s/v1/v1/rb/definition/" + rbName + "/"
                + rbVersion + "/profile/" + prName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/instance"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createInstance(@RequestBody InstanceEntity iE) throws Exception {

        logger.info("create Instance called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("https://localhost:32780/api/multicloud-k8s/v1/v1/instance");
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String requestBody = objectMapper.writeValueAsString(iE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/instance/{instID}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getInstance(@PathVariable("instID") String instanceId) throws Exception {

        logger.info("get Instance called.");
        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("https://localhost:32780/api/multicloud-k8s/v1/v1/instance/" + instanceId);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/multicloud-k8s/v1/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName)
            throws Exception {

        logger.info("create Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("https://localhost:32780/api/multicloud-k8s/v1/v1/definition/" + rbName + "/"
                + rbVersion + "/profile/" + prName + "/config");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(cE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {
            "/api/multicloud-k8s/v1/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfiguration(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("profile-name") String prName, @PathVariable("cfg-name") String cfgName) throws Exception {

        logger.info("get Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("https://localhost:32780/api/multicloud-k8s/v1/v1/definition/" + rbName + "/"
                + rbVersion + "/profile/" + prName + "/config/" + cfgName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/connectivity-info"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createConnectivityInfo(@RequestBody ConnectivityInfo cIE) throws Exception {

        logger.info("create ConnectivityInfo called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("https://localhost:32780/api/multicloud-k8s/v1/v1/connectivity-info");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(cIE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/connectivity-info/{connname}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getConnectivityInfo(@PathVariable("connname") String connName) throws Exception {

        logger.info("get Connectivity Info called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("https://localhost:32780/api/multicloud-k8s/v1/v1/connectivity-info/" + connName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/rb/definition/{rb-name}/{rb-version}/config-template"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfigTemplate(@RequestBody ConfigTemplateEntity tE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion) throws Exception {

        logger.info("createConfigTemplate called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://localhost:32780/api/multicloud-k8s/v1/v1/rb/definition/" + rbName + "/"
                + rbVersion + "/config-template");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(tE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/multicloud-k8s/v1/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfigTemplate(@PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("tname") String tName) throws Exception {

        logger.info("getConfigTemplate called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("https://localhost:32780/api/multicloud-k8s/v1/v1/rb/definition/" + rbName + "/"
                + rbVersion + "/config-template/" + tName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

}
