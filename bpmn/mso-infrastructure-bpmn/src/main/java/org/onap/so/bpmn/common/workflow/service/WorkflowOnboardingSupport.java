/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common.workflow.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;

/**
 * 
 * @version 1.0 Support SO workflow/script onboarding and recipe update
 */
@Path("/hotmanagement")
@Api(value = "/hotmanage", description = "Provides support for the workflow hot onboarding and recipe update")
@Provider
@Component
public class WorkflowOnboardingSupport extends ProcessEngineAwareService {

    protected static final Logger logger = LoggerFactory.getLogger(WorkflowOnboardingSupport.class);

    @Autowired
    private CatalogDbClient catalogDbClient;

    /**
     * Get all workflow packages including all bpmn infos.
     * 
     * @return
     */
    @GET
    @ApiOperation(value = "Get all workflow packages", notes = "Get all workflow packages")
    @Path("/workflowPackages")
    public Response getWorkflowPackages() {
        Map<String, List<String>> packages = getPackages();
        List<Map<String, Object>> packageList = new ArrayList<Map<String, Object>>();
        for (Entry<String, List<String>> entry : packages.entrySet()) {
            Map<String, Object> packageInfo = new HashMap<String, Object>();
            packageInfo.put("packageName", entry.getKey());
            packageInfo.put("orchestrationFlows", entry.getValue());
            packageList.add(packageInfo);
        }
        Map<String, List<Map<String, Object>>> mapPackages = new HashMap<String, List<Map<String, Object>>>();
        mapPackages.put("workflowPackages", packageList);
        String resp = JSONObject.toJSONString(mapPackages);
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(resp).build();
    }

    /**
     * Get the package info from the local system.
     * 
     * @return
     */
    private Map<String, List<String>> getPackages() {
        String pkgDir = "/camunda/webapps/";
        File packageFile = new File(pkgDir);
        String[] packageList = packageFile.list();
        Map<String, List<String>> mapPackage = new HashMap<String, List<String>>();
        for (String strPkgFileName : packageList) {
            if (strPkgFileName.endsWith(".war")) {
                String fileName = strPkgFileName.substring(0, strPkgFileName.length() - ".war".length());
                String flowsDir = pkgDir + fileName + "/WEB-INF/classes/";
                if ("mso".equals(fileName)) {
                    flowsDir = pkgDir + fileName + "/WEB-INF/classes/process/";
                }
                File flowFile = new File(flowsDir);
                if (!flowFile.isDirectory()) {
                    continue;
                }
                String[] flowFileNames = flowFile.list();
                List<String> orchestrationFlows = new ArrayList<String>();
                for (String flowFileName : flowFileNames) {
                    if (flowFileName.endsWith(".bpmn")) {
                        orchestrationFlows.add(flowFileName.substring(0, flowFileName.length() - ".bpmn".length()));
                    }
                }
                mapPackage.put(fileName, orchestrationFlows);
            }
        }
        return mapPackage;
    }

    /**
     * delete workflow package
     * 
     * @param request the body of the request
     * @return
     */
    @DELETE
    @Path("/workflowPackages/{packageName}")
    @ApiOperation(value = "delete a service recipe", notes = "delete a service recipe")
    @Produces("application/json")
    @Consumes("application/json")
    public Response deleteWorkflowPackage(@PathParam("packageName") String packageName) {
        String pkgDir = "/camunda/webapps/";
        File packageFile = new File(pkgDir + packageName + ".war");
        if (packageFile.isFile()) {
            packageFile.delete();
        }
        return Response.status(200).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * upload a workflow package to the server
     * 
     * @param uploadInputStream upload stream
     * @param disposition
     * @return
     */
    @POST
    @Path("/workflowPackages/onboard")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    @ApiOperation(value = "Add a new service recipe", notes = "Add a new service recipe")
    public Response onboardWorkflowPackage(@Multipart(value = "file") Attachment file) {
        String msg = "Upload package finished.";
        boolean isSuccess = false;
        DataHandler dh = file.getDataHandler();
        String fileName = "/camunda/webapps/" + dh.getName();
        File saveFile = new File(fileName);
        if (saveFile.isFile()) {
            msg = "Upload package failed: The Package already exist";
        } else {
            try {
                isSuccess = saveFile(dh.getInputStream(), fileName);
                if (!isSuccess) {
                    msg = "Upload package failed: write file failed.";
                }
            } catch (IOException e) {
                msg = "Upload package failed: Onboard File Exception!";
            }
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("result", String.valueOf(isSuccess));
        result.put("message", msg);
        String resp = JSONObject.toJSONString(result);
		return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(resp).build();
	}

    /**
     * Write the stream to file
     * 
     * @param uploadStream the stream need to writh
     * @param file the destination file
     */
    private boolean saveFile(InputStream uploadStream, String file) {
        try {
            OutputStream outStream = new FileOutputStream(new File(file));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = uploadStream.read(bytes)) != -1) {
                outStream.write(bytes, 0, read);
            }
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            logger.info("write stream to file failed");
            return false;
        }
        return true;
    }
}
