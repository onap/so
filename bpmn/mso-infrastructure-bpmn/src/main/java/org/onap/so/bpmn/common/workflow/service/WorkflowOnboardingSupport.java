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
import java.util.Arrays;
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
import org.onap.so.db.catalog.beans.NetworkRecipe;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.rest.catalog.beans.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;

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
    protected static final long DEFAULT_WAIT_TIME = 60000; // default wait time
	private static final String SERVICE = "SERVICE";
	private static final String NETWORK = "NETWORK";
	private static final String VNF = "VNF";

    @Autowired
    private CatalogDbClient catalogDbClient;

    /**
     * Get all service recipes.
     * 
     * @return
     */
    @GET
    @ApiOperation(value = "Get all service recipes", notes = "Get all service recipes")
    @Path("/serviceRecipes")
    public Response getServiceRecipes() {
        List<ServiceRecipe> serviceRecipes = catalogDbClient.getServiceRecipes();
        List<Service> services = catalogDbClient.getServices();
        Map<String, String> idToName = new HashMap<String, String>();
        for (Service service : services) {
            idToName.put(service.getModelVersionId(), service.getModelName());
        }
        Map<String, String> flowToName = new HashMap<String, String>();
        Map<String, List<String>> packages = getPackages();
        for (Entry<String, List<String>> entry : packages.entrySet()) {
            for (String flow : entry.getValue()) {
                flowToName.put(flow, entry.getKey());
            }
        }
        Map<String, List<Map<String, String>>> mapServiceRecipes = new HashMap<String, List<Map<String, String>>>();
        List<Map<String, String>> recipeList = new ArrayList<Map<String, String>>();
        for (ServiceRecipe serviceRecipe : serviceRecipes) {
            Map<String, String> recipeObj = new HashMap<String, String>();
            recipeObj.put("id", String.valueOf(serviceRecipe.getId()));
            recipeObj.put("modelVersionId", serviceRecipe.getServiceModelUUID());
            recipeObj.put("modelName", idToName.get(serviceRecipe.getServiceModelUUID()));
            recipeObj.put("operation", serviceRecipe.getAction());
            recipeObj.put("orchestrationPackageName", flowToName.get(serviceRecipe.getOrchestrationUri()));
            recipeObj.put("orchestrationFlow", serviceRecipe.getOrchestrationUri());
            recipeList.add(recipeObj);
        }
        mapServiceRecipes.put("serviceRecipes", recipeList);
        String resp = JSONObject.toJSONString(mapServiceRecipes);

        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(resp).build();
    }

    /**
	 * Add new recipe for service
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("/serviceRecipes")
	@ApiOperation(value = "Add a new service recipe", notes = "Add a new service recipe")
	@Produces("application/json")
	@Consumes("application/json")
	public Response addServiceRecipDese(String request) {
		Map<String, String> mapRecipeInfo;
		ObjectMapper mapper = new ObjectMapper();

		try {

			try {
				mapRecipeInfo = mapper.readValue(request, Map.class);

			} catch (Exception e) {
				logger.debug("Mapping of request to JSON object failed : ", e);
				return Response.status(200).header("Access-Control-Allow-Origin", "*").build();
			}

			String type = mapRecipeInfo.get("modelType");
			String modelVersionId = mapRecipeInfo.get("modelVersionId");
			String action = mapRecipeInfo.get("operation");
			String orchestrationFlow = "/mso/async/services/" + mapRecipeInfo.get("orchestrationFlow");
			String modelName = mapRecipeInfo.get("modelName");
			String description = action + " orchestration flow for template " + mapRecipeInfo.get("modelName");

			String[] validTypes = { SERVICE, NETWORK, VNF };

			if (org.springframework.util.StringUtils.isEmpty(type)
					|| !Arrays.asList(validTypes).contains(type.toUpperCase())) {
				return Response.status(200).header("Access-Control-Allow-Origin", "*")
						.entity("{\"errMsg\":\"type is invalid.\"}").build();

			}
			int assignedId = 0;
			boolean isModelVersionExists = false;
			Object[] conflictAndIdCheck;

			if (type.equalsIgnoreCase(SERVICE)) {
				isModelVersionExists = isServiceModelVersionIdExists(modelVersionId);
				if (!isModelVersionExists) {
					return Response.status(200).header("Access-Control-Allow-Origin", "*")
							.entity("{\"errMsg\":\"The service template does not exist.\"}").build();
				}

				conflictAndIdCheck = isServiceActionConflict(modelVersionId, action);
				if ((boolean) conflictAndIdCheck[0]) {
					return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(
							"{\"errMsg\":\"The recipe for this action of the service template already exists.\"}")
							.build();
				}
				assignedId = (int) conflictAndIdCheck[1] + 1;
				ServiceRecipe serviceRecipe = new ServiceRecipe();
				serviceRecipe.setId(assignedId);
				serviceRecipe.setServiceModelUUID(modelVersionId);
				serviceRecipe.setAction(action);
				serviceRecipe.setOrchestrationUri(orchestrationFlow);
				serviceRecipe.setRecipeTimeout(180);
				serviceRecipe.setDescription(description);
				catalogDbClient.postServiceRecipe(serviceRecipe);
			} else if (type.equalsIgnoreCase(NETWORK)) {

				isModelVersionExists = isNetworkVersionIdValid(modelVersionId);
				if (!isModelVersionExists) {
					return Response.status(200).header("Access-Control-Allow-Origin", "*")
							.entity("{\"errMsg\":\"The network template does not exist.\"}").build();
				}

				conflictAndIdCheck = isNetworkActionConflict(modelVersionId, action);
				if ((boolean) conflictAndIdCheck[0]) {
					return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(
							"{\"errMsg\":\"The recipe for this action of the network template already exists.\"}")
							.build();
				}

				assignedId = (int) conflictAndIdCheck[1] + 1;
				NetworkRecipe nwrecipe = new NetworkRecipe();
				nwrecipe.setId(assignedId);
				nwrecipe.setModelName(modelName);
				nwrecipe.setAction(action);
				nwrecipe.setOrchestrationUri(orchestrationFlow);
				nwrecipe.setDescription(description);
				nwrecipe.setRecipeTimeout(180);
				nwrecipe.setVersionStr(modelVersionId);
				catalogDbClient.postNetworkRecipe(nwrecipe);

			} else if (type.equalsIgnoreCase(VNF)) {

				isModelVersionExists = isVnfVersionIdValid(modelVersionId);
				if (!isModelVersionExists) {
					return Response.status(200).header("Access-Control-Allow-Origin", "*")
							.entity("{\"errMsg\":\"The Vnf template does not exist.\"}").build();

				}

				conflictAndIdCheck = isVfActionConflict(modelVersionId, action);
				if ((boolean) conflictAndIdCheck[0]) {
					return Response.status(200).header("Access-Control-Allow-Origin", "*")
							.entity("{\"errMsg\":\"The recipe for this action of the vnf template already exists.\"}")
							.build();
				}

				assignedId = (int) conflictAndIdCheck[1] + 1;
				VnfRecipe vnfRecipe = new VnfRecipe();
				vnfRecipe.setId(assignedId);
				vnfRecipe.setAction(action);
				vnfRecipe.setDescription(description);
				vnfRecipe.setVersionStr(modelVersionId);
				vnfRecipe.setOrchestrationUri(orchestrationFlow);
				vnfRecipe.setRecipeTimeout(180);
				catalogDbClient.postVnfRecipe(vnfRecipe);

			}

			mapRecipeInfo.put("id", String.valueOf(assignedId));
		} catch (Exception e) {
			logger.debug("WorkflowOnboardingSupport addServiceRecipDese error {} : ", e);
			return Response.status(200).header("Access-Control-Allow-Origin", "*")
					.entity("{\"errMsg\":\"Unable to process.\"}").build();
		}
		String resp = JSONObject.toJSONString(mapRecipeInfo);
		return Response.status(201).header("Access-Control-Allow-Origin", "*").entity(resp).build();
	}

	private boolean isServiceModelVersionIdExists(String modelVersionId) {
		List<Service> services = catalogDbClient.getServices();
		boolean isExists = false;
		for(Service service: services) {
			if(service.getModelVersionId().equals(modelVersionId)){
				isExists = true;
				break;
			}
		}
		return isExists;
	}
	
	private Object[] isServiceActionConflict(String  modelVersionId,String action) {
		List<ServiceRecipe> serviceRecipes = catalogDbClient.getServiceRecipes();
		boolean isConflict = false;
		Object[] data= new Object[2]; 
		int maxId =  serviceRecipes.get(0)!=null?  serviceRecipes.get(0).getId(): 1;
		for (ServiceRecipe recipe : serviceRecipes) {
			maxId = recipe.getId() > maxId ? recipe.getId() : maxId;
			if (recipe.getServiceModelUUID().equals(modelVersionId)
					&& recipe.getAction().equals(action)) {
				isConflict = true;
			}
		}
		data[0]=isConflict; 
		data[1]=maxId;
		return data;
	}
	
	private Object[] isNetworkActionConflict(String  modelVersionId,String action) {
		List<NetworkRecipe> recipes = catalogDbClient.getNetworkRecipes();
		boolean isConflict = false;
		Object[] data= new Object[2]; 
		int  maxId = recipes.get(0)!=null ? recipes.get(0).getId() : 1;
		for (NetworkRecipe recipe : recipes) {
			maxId = recipe.getId() > maxId ? recipe.getId() : maxId;
			if (recipe.getVersionStr().equals(modelVersionId)
					&& recipe.getAction().equals(action)) {
				isConflict = true;
				
			}
			
		}
		data[0]=isConflict; 
		data[1]=maxId;
		return data;
	}
	
	private Object[] isVfActionConflict(String modelVersionId,String action) {
		List<VnfRecipe> vnfRecipes = catalogDbClient.getVnfRecipes();
		boolean isConflict = false;
		Object[] data= new Object[2]; 
		int maxId = vnfRecipes.get(0) !=null ? vnfRecipes.get(0).getId()  : 1;
		for (VnfRecipe recipe : vnfRecipes) {
			maxId = recipe.getId() > maxId ? recipe.getId() : maxId;
			if (recipe.getVersionStr().equals(modelVersionId)
					&& recipe.getAction().equals(action)) {
				isConflict = true;
			}
		}
		data[0]=isConflict; 
		data[1]=maxId;
		return data;
	}
	
	
	
	private boolean isNetworkVersionIdValid(String modelVersionId) {
		List<NetworkResource> networkResources = catalogDbClient.getNetworkResources();
		boolean isExists = false;
		for(NetworkResource networkResource: networkResources) {
			if(networkResource.getModelVersion().equals(modelVersionId)){
				isExists = true;
				break;
			}
		}
		return isExists;
	}

	private boolean isVnfVersionIdValid(String modelVersionId) {
		List<VnfResource> vnfResources = catalogDbClient.getVnfResources();
		boolean isExists = false;
		for(VnfResource vnfResource: vnfResources) {
			if(vnfResource.getModelVersion().equals(modelVersionId)){
				isExists = true;
				break;
			}
		}
		return isExists;
	}

    /**
     * delete service recipe
     * 
     * @param request the body of the request
     * @return
     */
    @DELETE
    @Path("/serviceRecipes/{id}")
    @ApiOperation(value = "delete a service recipe", notes = "delete a service recipe")
    @Produces("application/json")
    @Consumes("application/json")
    public Response delServiceRecipe(String request, @PathParam("id") String id) {
        catalogDbClient.deleteServiceRecipe(id);
        return Response.status(200).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * Get service templates
     * 
     * @return
     */
    @GET
    @ApiOperation(value = "query all service templates", notes = "query all service templates")
    @Path("/serviceTemplates")
    public Response getServices() {
        List<Service> services = catalogDbClient.getServices();
        Map<String, List<Map<String, String>>> mapServices = new HashMap<String, List<Map<String, String>>>();
        List<Map<String, String>> serviceList = new ArrayList<Map<String, String>>();
        for (Service service : services) {
            Map<String, String> serviceObj = new HashMap<String, String>();
            serviceObj.put("modelInvariantId", service.getModelInvariantId());
            serviceObj.put("modelVersionId", service.getModelVersionId());
            serviceObj.put("modelName", service.getModelName());
            serviceList.add(serviceObj);
        }
        mapServices.put("services", serviceList);
        String resp = JSONObject.toJSONString(mapServices);
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(resp).build();
    }

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
