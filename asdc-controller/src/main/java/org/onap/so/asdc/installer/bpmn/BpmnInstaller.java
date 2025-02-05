/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.asdc.installer.bpmn;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class BpmnInstaller {
    protected static final Logger logger = LoggerFactory.getLogger(BpmnInstaller.class);
    private static final String BPMN_SUFFIX = ".bpmn";
    private static final String CAMUNDA_URL = "mso.camundaURL";
    private static final String CREATE_DEPLOYMENT_PATH = "sobpmnengine/deployment/create";

    @Autowired
    private Environment env;

    @Autowired
    private ASDCConfiguration asdcConfig;

    public void installBpmn(String csarFilePath) {
        logger.info("Deploying BPMN files from {}", csarFilePath);
        try (ZipInputStream csarFile =
                new ZipInputStream(new FileInputStream(Paths.get(csarFilePath).normalize().toString()))) {
            ZipEntry entry = csarFile.getNextEntry();

            while (entry != null) {
                String name = entry.getName();
                if (name.endsWith(BPMN_SUFFIX)) {
                    logger.debug("Attempting to deploy BPMN file: {}", name);
                    try {
                        Path p = Paths.get(name);
                        String fileName = p.getFileName().toString();
                        extractBpmnFileFromCsar(csarFile, fileName);
                        HttpResponse response = sendDeploymentRequest(fileName, "");
                        logger.debug("Response status line: {}", response.getStatusLine());
                        logger.debug("Response entity: {}", response.getEntity().toString());
                        if (response.getStatusLine().getStatusCode() != 200) {
                            logger.debug("Failed deploying BPMN {}", name);
                            logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL.toString(),
                                    name, fileName, Integer.toString(response.getStatusLine().getStatusCode()),
                                    ErrorCode.DataError.getValue(), "ASDC BPMN deploy failed");
                        } else {
                            logger.debug("Successfully deployed to Camunda: {}", name);
                        }
                    } catch (Exception e) {
                        logger.debug("Exception :", e);
                        logger.error(LoggingAnchor.FIVE, MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL.toString(), name,
                                e.getMessage(), ErrorCode.DataError.getValue(), "ASDC BPMN deploy failed");
                    }
                }
                entry = csarFile.getNextEntry();
            }
        } catch (IOException ex) {
            logger.debug("Exception :", ex);
            logger.error(LoggingAnchor.FIVE, MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL.toString(), csarFilePath,
                    ex.getMessage(), ErrorCode.DataError.getValue(), "ASDC reading CSAR with workflows failed");
        }
    }

    public boolean containsWorkflows(String csarFilePath) {
        try (ZipFile zipFile = new ZipFile(csarFilePath)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                String fileName = zipEntries.nextElement().getName();
                if (fileName.endsWith(BPMN_SUFFIX)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Exception :", e);
            logger.error(LoggingAnchor.FIVE, MessageEnum.ASDC_ARTIFACT_CHECK_EXC.toString(), csarFilePath,
                    e.getMessage(), ErrorCode.DataError.getValue(), "ASDC Unable to check CSAR entries");
        }
        return false;
    }

    protected HttpResponse sendDeploymentRequest(String bpmnFileName, String version)
            throws IOException, URISyntaxException {
        HttpClient client = HttpClientBuilder.create().build();
        URI deploymentUri = new URI(this.env.getProperty(CAMUNDA_URL) + CREATE_DEPLOYMENT_PATH);
        HttpPost post = new HttpPost(deploymentUri);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000000).setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000).build();
        post.setConfig(requestConfig);
        HttpEntity requestEntity = buildMimeMultipart(bpmnFileName, version);
        post.setEntity(requestEntity);
        return client.execute(post);
    }

    protected HttpEntity buildMimeMultipart(String bpmnFileName, String version) throws IOException {
        FileInputStream bpmnFileStream = new FileInputStream(
                Paths.get(getMsoConfigPath(), "ASDC", version, bpmnFileName).normalize().toString());

        byte[] bytesToSend = IOUtils.toByteArray(bpmnFileStream);
        HttpEntity requestEntity =
                MultipartEntityBuilder.create()
                        .addPart(FormBodyPartBuilder.create().setName("deployment-name")
                                .setBody(new StringBody("MSO Sample 1", ContentType.TEXT_PLAIN))
                                .setField("Content-Disposition",
                                        String.format("form-data; name=\"%s\"", "deployment-name"))
                                .build())
                        .addPart(FormBodyPartBuilder.create().setName("enable-duplicate-filtering")
                                .setBody(new StringBody("false", ContentType.TEXT_PLAIN))
                                .setField("Content-Disposition",
                                        String.format("form-data; name=\"%s\"", "enable-duplicate-filtering"))
                                .build())
                        .addPart(FormBodyPartBuilder.create().setName("deplpy-changed-only")
                                .setBody(new StringBody("false", ContentType.TEXT_PLAIN))
                                .setField("Content-Disposition",
                                        String.format("form-data; name=\"%s\"", "deploy-changed-only"))
                                .build())
                        .addPart(FormBodyPartBuilder.create().setName("deployment-source")
                                .setBody(new StringBody("local", ContentType.TEXT_PLAIN))
                                .setField("Content-Disposition",
                                        String.format("form-data; name=\"%s\"", "deployment-source"))
                                .build())
                        .addPart(
                                FormBodyPartBuilder.create().setName(bpmnFileName)
                                        .setBody(new ByteArrayBody(bytesToSend, ContentType.create("octet"),
                                                bpmnFileName))
                                        .setField("Content-Disposition",
                                                String.format("form-data; name=\"%s\"; filename=\"%s\"; size=%d",
                                                        bpmnFileName, bpmnFileName, bytesToSend.length))
                                        .build())
                        .build();

        IOUtils.closeQuietly(bpmnFileStream);
        return requestEntity;
    }

    protected void extractBpmnFileFromCsar(ZipInputStream zipIn, String fileName) {
        String filePath = Paths.get(System.getProperty("mso.config.path"), "ASDC", fileName).normalize().toString();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, read);
            }
        } catch (IOException e) {
            logger.error("Unable to open file.", e);
        }
    }

    private String getMsoConfigPath() {
        String msoConfigPath = System.getProperty("mso.config.path");
        if (msoConfigPath == null) {
            logger.info("Unable to find the system property mso.config.path, use the default configuration");
            msoConfigPath = StringUtils.defaultString(asdcConfig.getPropertyOrNull("mso.config.defaultpath"));
        }
        logger.info("MSO config path is: {}", msoConfigPath);
        return msoConfigPath;
    }
}
