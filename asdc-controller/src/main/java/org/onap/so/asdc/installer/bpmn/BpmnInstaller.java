/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.*;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;

import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class BpmnInstaller {
	protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,BpmnInstaller.class);
	private static final String BPMN_SUFFIX = ".bpmn";
	private static final String CAMUNDA_URL = "mso.camundaURL";
	private static final String CREATE_DEPLOYMENT_PATH = "/sobpmnengine/deployment/create";
	
	@Autowired
	private Environment env;	
	
	public void installBpmn(String csarFilePath) {
		LOGGER.info("Deploying BPMN files from " + csarFilePath);		
		try {			
			ZipInputStream csarFile = new ZipInputStream(new FileInputStream(csarFilePath));
			ZipEntry entry = csarFile.getNextEntry();		
	 
			while (entry != null) {				
				String name = entry.getName();
				if (name.endsWith(BPMN_SUFFIX)) {
					LOGGER.debug("Attempting to deploy BPMN file: " + name);
					try {
						Path p = Paths.get(name);
						String fileName = p.getFileName().toString();
						extractBpmnFileFromCsar(csarFile, fileName);
						HttpResponse response = sendDeploymentRequest(fileName);
						LOGGER.debug("Response status line: " + response.getStatusLine());
						LOGGER.debug("Response entity: " + response.getEntity().toString());
						if (response.getStatusLine().getStatusCode() != 200) {
							LOGGER.debug("Failed deploying BPMN " + name);
			                LOGGER.error(MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL,
			        				name,
			        				fileName,
			        				"",
			        				Integer.toString(response.getStatusLine().getStatusCode()), "", "", MsoLogger.ErrorCode.DataError, "ASDC BPMN deploy failed"); 	
						}						
						else {
							LOGGER.debug("Successfully deployed to Camunda: " + name);
						}
					}
					catch (Exception e) {
						LOGGER.debug("Exception :",e);
		                LOGGER.error(MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL,
		        				name,
		        				"",
		        				"",
		        				e.getMessage(), "", "", MsoLogger.ErrorCode.DataError, "ASDC BPMN deploy failed"); 					
					}							
				}
				entry = csarFile.getNextEntry();
	        }
			csarFile.close();
		} catch (IOException ex) {
			LOGGER.debug("Exception :",ex);
            LOGGER.error(MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL,
    				csarFilePath,
    				"",
    				"",
    				ex.getMessage(), "", "", MsoLogger.ErrorCode.DataError, "ASDC reading CSAR with workflows failed"); 		
		}
		return;
	}	
	
	protected HttpResponse sendDeploymentRequest(String bpmnFileName) throws Exception {					
		HttpClient client = HttpClientBuilder.create().build();	
		String deploymentUri = this.env.getProperty(CAMUNDA_URL) + CREATE_DEPLOYMENT_PATH;
		HttpPost post = new HttpPost(deploymentUri);
		RequestConfig requestConfig =
				RequestConfig.custom().setSocketTimeout(1000000).setConnectTimeout(1000).setConnectionRequestTimeout(1000).build();
		post.setConfig(requestConfig);        
		HttpEntity requestEntity = buildMimeMultipart(bpmnFileName);        
		post.setEntity(requestEntity);
		return client.execute(post);
	}
	
	protected HttpEntity buildMimeMultipart(String bpmnFileName) throws Exception {
		FileInputStream bpmnFileStream = new FileInputStream (System.getProperty("mso.config.path") + "/ASDC" + "/" + bpmnFileName);

		byte[] bytesToSend = IOUtils.toByteArray(bpmnFileStream);
		HttpEntity requestEntity = MultipartEntityBuilder.create()                
				.addPart(FormBodyPartBuilder.create()
						.setName("deployment-name")
						.setBody(new StringBody("MSO Sample 1", ContentType.TEXT_PLAIN))
						.setField("Content-Disposition", String.format("form-data; name=\"%s\"", "deployment-name"))
						.build())
				.addPart(FormBodyPartBuilder.create() 
						.setName("enable-duplicate-filtering")
						.setBody(new StringBody("false", ContentType.TEXT_PLAIN))
						.setField("Content-Disposition", String.format("form-data; name=\"%s\"", "enable-duplicate-filtering"))
						.build())
				.addPart(FormBodyPartBuilder.create()
						.setName("deplpy-changed-only")
						.setBody(new StringBody("false", ContentType.TEXT_PLAIN))
						.setField("Content-Disposition", String.format("form-data; name=\"%s\"", "deploy-changed-only"))
						.build())
				.addPart(FormBodyPartBuilder.create()
						.setName("deployment-source")  
						.setBody(new StringBody("local", ContentType.TEXT_PLAIN))
						.setField("Content-Disposition", String.format("form-data; name=\"%s\"", "deployment-source"))
						.build())
				.addPart(FormBodyPartBuilder.create()
						.setName(bpmnFileName)
						.setBody(new ByteArrayBody(bytesToSend, ContentType.create("octet"), bpmnFileName))
						.setField("Content-Disposition", String.format("form-data; name=\"%s\"; filename=\"%s\"; size=%d", bpmnFileName, bpmnFileName, bytesToSend.length))
						.build())
				.build();
		 return requestEntity;
	}
	
	protected void extractBpmnFileFromCsar(ZipInputStream zipIn, String fileName) throws IOException {
		String filePath = System.getProperty("mso.config.path") + "/ASDC" + "/" + fileName;
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[4096];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			outputStream.write(bytesIn, 0, read);
		}
		outputStream.close();
	}
}
