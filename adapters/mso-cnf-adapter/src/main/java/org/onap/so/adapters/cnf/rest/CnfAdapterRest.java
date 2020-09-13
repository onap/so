/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.cnf.rest;

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.ConfigTemplateEntity;
import org.onap.so.adapters.cnf.model.ConfigurationEntity;
import org.onap.so.adapters.cnf.model.ConfigurationRollbackEntity;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
import org.onap.so.adapters.cnf.model.InstanceMiniResponseList;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.ProfileEntity;
import org.onap.so.adapters.cnf.model.ResourceBundleEntity;
import org.onap.so.adapters.cnf.model.Tag;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RestController
public class CnfAdapterRest {

    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterRest.class);
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Autowired
    private CnfAdapterService cnfAdapterService;

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/healthcheck"}, method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<String> healthCheck() throws Exception {

        logger.info("healthCheck called.");
        return cnfAdapterService.healthCheck();

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance"}, method = RequestMethod.POST,
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<InstanceResponse> createInstance(@RequestBody BpmnInstanceRequest bpmnInstanceRequest)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("createInstance called.");
        return cnfAdapterService.createInstance(bpmnInstanceRequest);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instID}"}, method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<InstanceResponse> getInstanceByInstanceId(@PathVariable("instID") String instanceId)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("getInstanceByInstanceId called.");

        return cnfAdapterService.getInstanceByInstanceId(instanceId);

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instID}/status"}, method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<InstanceStatusResponse> getInstanceStatusByInstanceId(
            @PathVariable("instID") String instanceId) throws JsonParseException, JsonMappingException, IOException {

        logger.info("getInstanceStatusByInstanceId called.");

        return cnfAdapterService.getInstanceStatusByInstanceId(instanceId);

    }

    @RequestMapping(value = {"/api/cnf-adapter/v1/instance"}, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<InstanceMiniResponseList> getInstanceByRBNameOrRBVersionOrProfileName(
            @RequestParam(value = "rb-name", required = false) String rbName,
            @RequestParam(value = "rb-version", required = false) String rbVersion,
            @RequestParam(value = "profile-name", required = false) String profileName)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("getInstanceByRBNameOrRBVersionOrProfileName called.");
        return cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instID}"}, method = RequestMethod.DELETE,
            produces = "application/json")
    public ResponseEntity<String> deleteInstanceByInstanceId(@PathVariable("instID") String instanceID)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("deleteInstanceByInstanceId called.");
        return cnfAdapterService.deleteInstanceByInstanceId(instanceID);

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createRB(@RequestBody ResourceBundleEntity rB) throws Exception {

        logger.info("ResourceBundleEntity:" + rB.toString());

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/rb/definition");
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
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getRB(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion)
            throws Exception {

        logger.info("get RB called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}"}, method = RequestMethod.DELETE,
            produces = "application/json")
    public String deleteRB(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion)
            throws Exception {

        logger.info("delete RB called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpDelete req = new HttpDelete("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getListOfRB(@PathVariable("rb-name") String rbName) throws Exception {

        logger.info("getListOfRB called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("http://multicloud-k8s:9015/v1/rb/definition/" + rbName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getListOfRBWithoutUsingRBName() throws Exception {

        logger.info("getListOfRBWithoutUsingRBName called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("http://multicloud-k8s:9015/v1/rb/definition");

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/content"},
            method = RequestMethod.POST, produces = "multipart/form-data")
    public String uploadArtifactForRB(@RequestParam("file") MultipartFile file, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion) throws Exception {

        logger.info("Upload  Artifact For RB called.");

        File convFile = new File(file.getOriginalFilename());
        file.transferTo(convFile);
        FileBody fileBody = new FileBody(convFile, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post =
                new HttpPost("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion + "/content");
        post.setHeader("Content-Type", "multipart/form-data");
        logger.info(String.valueOf(post));
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.POST, produces = "application/json")
    public String createProfile(@RequestBody ProfileEntity fE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion) throws Exception {

        logger.info("create Profile called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post =
                new HttpPost("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile");
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
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("pr-name") String prName) throws Exception {

        logger.info("get Profile called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet(
                "http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.GET, produces = "application/json")
    public String getListOfProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion)
            throws Exception {

        logger.info("getListOfProfile called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req =
                new HttpGet("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile");

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("pr-name") String prName) throws Exception {

        logger.info("delete Profile called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpDelete req = new HttpDelete(
                "http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}/content"},
            method = RequestMethod.POST, produces = "multipart/form-data")
    public String uploadArtifactForProfile(@RequestParam("file") MultipartFile file,
            @PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("pr-name") String prName) throws Exception {

        logger.info("Upload  Artifact For Profile called.");

        File convFile = new File(file.getOriginalFilename());
        file.transferTo(convFile);
        FileBody fileBody = new FileBody(convFile, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/content");
        post.setHeader("Content-Type", "multipart/form-data");

        logger.info(String.valueOf(post));
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName)
            throws Exception {

        logger.info("create Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config");
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
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfiguration(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("profile-name") String prName, @PathVariable("cfg-name") String cfgName) throws Exception {

        logger.info("get Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("http://multicloud-k8s:9015/v1/definition/" + rbName + "/" + rbVersion + "/profile/"
                + prName + "/config/" + cfgName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteConfiguration(@PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName,
            @PathVariable("cfg-name") String cfgName) throws Exception {

        logger.info("delete Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpDelete req = new HttpDelete("http://multicloud-k8s:9015/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config/" + cfgName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.PUT, produces = "application/json")
    public String updateConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName,
            @PathVariable("cfg-name") String cfgName) throws Exception {

        logger.info("update Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPut post = new HttpPut("http://multicloud-k8s:9015/v1/definition/" + rbName + "/" + rbVersion + "/profile/"
                + prName + "/config/" + cfgName);
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
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/tagit"},
            method = RequestMethod.POST, produces = "application/json")
    public String tagConfigurationValue(@RequestBody Tag tag, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("pr-name") String prName) throws Exception {
        logger.info("Tag Configuration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config/tagit");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(tag);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createConnectivityInfo(@RequestBody ConnectivityInfo cIE) throws Exception {

        logger.info("create ConnectivityInfo called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/connectivity-info");
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
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info/{connname}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getConnectivityInfo(@PathVariable("connname") String connName) throws Exception {

        logger.info("get Connectivity Info called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("http://multicloud-k8s:9015/v1/connectivity-info/" + connName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info/{connname}"}, method = RequestMethod.DELETE,
            produces = "application/json")
    public String deleteConnectivityInfo(@PathVariable("connname") String connName) throws Exception {

        logger.info("delete Connectivity Info called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpDelete req = new HttpDelete("http://multicloud-k8s:9015/v1/connectivity-info/" + connName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfigTemplate(@RequestBody ConfigTemplateEntity tE, @PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion) throws Exception {

        logger.info("createConfigTemplate called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost(
                "http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion + "/config-template");
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
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfigTemplate(@PathVariable("rb-name") String rbName,
            @PathVariable("rb-version") String rbVersion, @PathVariable("tname") String tName) throws Exception {

        logger.info("getConfigTemplate called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpGet req = new HttpGet("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion
                + "/config-template/" + tName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteTemplate(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("tname") String tName) throws Exception {

        logger.info("deleteTemplate called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpDelete req = new HttpDelete("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion
                + "/config-template/" + tName);

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }

    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}/content"},
            method = RequestMethod.POST, produces = "multipart/form-data")
    public String uploadTarFileForTemplate(@RequestParam("file") MultipartFile file,
            @PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
            @PathVariable("tname") String tName) throws Exception {

        logger.info("uploadTarFileForTemplate called.");

        File convFile = new File(file.getOriginalFilename());
        file.transferTo(convFile);
        FileBody fileBody = new FileBody(convFile, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/rb/definition/" + rbName + "/" + rbVersion
                + "/config-template/" + tName + "/content");
        post.setHeader("Content-Type", "multipart/form-data");

        logger.info(String.valueOf(post));
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rbName}/{rbVersion}/profile/{prName}/config/rollback"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String rollbackConfiguration(@RequestBody ConfigurationRollbackEntity rE,
            @PathVariable("rbName") String rbName, @PathVariable("rbVersion") String rbVersion,
            @PathVariable("prName") String prName) throws Exception {
        logger.info("rollbackConfiguration called.");

        // TODO
        // Below URL should be changed as appropriate multicloud URL.
        HttpPost post = new HttpPost("http://multicloud-k8s:9015/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config/rollback");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(rE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

}
