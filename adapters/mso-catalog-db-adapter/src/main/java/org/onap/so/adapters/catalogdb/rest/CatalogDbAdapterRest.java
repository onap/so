/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.catalogdb.rest;

/*
Create an initial query to retrieve a VNF Resource definition (including a list of possible module types)
within the context of a given service. Input is a vnf resource model customization ID (new field for 1702),
or a composite key (from 1610) of service name, service version, vnf instance name

Returns a structure (JSON?) containing VNF RESOURCE attributes, plus a list of VF Module structures.

Query a NETWORK_RESOURCE from the MSO Catalog, based on a networkModelCustomizationUUID (new for 1702),
a network type (unique type identifier in 1610), or based on network role within a service.

Create Adapter framework for access to Catalog DB, including connection management,
login/password access, transaction logic, etc. This can be modeled after the Request DB Adapter

Update the MSO Catalog DB schema to include the new fields defined in this user story.

Note that the resourceModelCustomizationUUID (or vfModuleModelCustomizationUUID) will be unique keys (indexes)
on the VNF_RESOURCE and VF_MODULE tables respectively.
The previously constructed "vnf-type" and "vf-module-type" field may continue to be populated,
but should no longer be needed and can deprecate in future release.

For migration, a new randomly generated UUID field may be generated for the *ModelCustomizationUUID" fields
until such time that the model is redistributed from ASDC.

All other fields Check with Mike Z for appropriate value for the vfModuleLabel.
We might be able to derive it's value from the current vnf-type (using the "middle" piece that identifies the module type).

min and initial counts can be 0. max can be null to indicate no maximum.

Once the network-level distribution artifacts are defined, similar updates can be made to the NETWORK_RESOURCE table.
 */

import org.apache.http.HttpStatus;
import org.onap.so.adapters.catalogdb.catalogrest.CatalogQuery;
import org.onap.so.adapters.catalogdb.catalogrest.CatalogQueryException;
import org.onap.so.adapters.catalogdb.catalogrest.CatalogQueryExceptionCategory;
import org.onap.so.adapters.catalogdb.catalogrest.QueryAllottedResourceCustomization;
import org.onap.so.adapters.catalogdb.catalogrest.QueryResourceRecipe;
import org.onap.so.adapters.catalogdb.catalogrest.QueryServiceCsar;
import org.onap.so.adapters.catalogdb.catalogrest.QueryServiceMacroHolder;
import org.onap.so.adapters.catalogdb.catalogrest.QueryServiceNetworks;
import org.onap.so.adapters.catalogdb.catalogrest.QueryServiceVnfs;
import org.onap.so.adapters.catalogdb.catalogrest.QueryVfModule;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.ArRecipeRepository;
import org.onap.so.db.catalog.data.repository.NetworkRecipeRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
import org.onap.so.db.catalog.data.repository.VFModuleRepository;
import org.onap.so.db.catalog.data.repository.VnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfRecipeRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.db.catalog.rest.beans.ServiceMacroHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * This class services calls to the REST interface for VF Modules (http://host:port/ecomp/mso/catalog/v1)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * Requests respond synchronously only
 */
@Path("/{version: v[0-9]+}")
@Component
public class CatalogDbAdapterRest {
    protected static Logger logger = LoggerFactory.getLogger(CatalogDbAdapterRest.class);
    private static final boolean IS_ARRAY = true;
    private static final String NETWORK_SERVICE = "network service";

    @Autowired
    private VnfCustomizationRepository vnfCustomizationRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private NetworkResourceCustomizationRepository networkCustomizationRepo;

    @Autowired
    private NetworkResourceRepository networkResourceRepo;

    @Autowired
    private AllottedResourceCustomizationRepository allottedCustomizationRepo;

    @Autowired
    private ToscaCsarRepository toscaCsarRepo;

    @Autowired
    private VFModuleRepository vfModuleRepo;

    @Autowired
    private VnfRecipeRepository vnfRecipeRepo;

    @Autowired
    private NetworkRecipeRepository networkRecipeRepo;

    @Autowired
    private ArRecipeRepository arRecipeRepo;

    @Autowired
    private VnfResourceRepository vnfResourceRepo;

    @Autowired
    private AllottedResourceRepository arResourceRepo;

    private static final String NO_MATCHING_PARAMETERS = "no matching parameters";

    public Response respond(String version, int respStatus, boolean isArray, CatalogQuery qryResp) {
        return Response
                .status(respStatus)
                //.entity(new GenericEntity<QueryServiceVnfs>(qryResp) {})
                .entity(qryResp.toJsonString(version, isArray))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("vnfResources/{vnfModelCustomizationUuid}")
    @Transactional( readOnly = true)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response serviceVnfs (
            @PathParam("version") String version,
            @PathParam("vnfModelCustomizationUuid") String vnfUuid
            ) {
        return serviceVnfsImpl (version, !IS_ARRAY, vnfUuid, null, null, null, null);
    }

    @GET
    @Path("serviceVnfs")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional( readOnly = true)
    public Response serviceVnfs(
            @PathParam("version") String version,
            @QueryParam("vnfModelCustomizationUuid") String vnfUuid,
            @QueryParam("serviceModelUuid") String smUuid,
            @QueryParam("serviceModelInvariantUuid") String smiUuid,
            @QueryParam("serviceModelVersion") String smVer,
            @QueryParam("serviceModelName") String smName
            ) {
        return serviceVnfsImpl (version, IS_ARRAY, vnfUuid, smUuid, smiUuid, smVer, smName);
    }

    public Response serviceVnfsImpl(String version, boolean isArray, String vnfUuid, String serviceModelUUID, String smiUuid, String smVer, String smName) {
        QueryServiceVnfs qryResp = null;
        int respStatus = HttpStatus.SC_OK;		
        List<VnfResourceCustomization> ret = new ArrayList<>();
        Service service = null;
        try {
            if (vnfUuid != null && !"".equals(vnfUuid)) 
                ret = vnfCustomizationRepo.findByModelCustomizationUUID(vnfUuid);			
            else if (serviceModelUUID != null && !"".equals(serviceModelUUID)) 				
                service = serviceRepo.findFirstOneByModelUUIDOrderByModelVersionDesc(serviceModelUUID);
            else if (smiUuid != null && !"".equals(smiUuid))			
                if (smVer != null && !"".equals(smVer)) 
                    service = serviceRepo.findFirstByModelVersionAndModelInvariantUUID(smVer,smiUuid);					
                else 					
                    service = serviceRepo.findFirstByModelInvariantUUIDOrderByModelVersionDesc(smiUuid);
            else if (smName != null && !"".equals(smName)) {
                if (smVer != null && !"".equals(smVer)) 					
                    service = serviceRepo.findByModelNameAndModelVersion(smName, smVer);
                else 
                    service = serviceRepo.findFirstByModelNameOrderByModelVersionDesc(smName);			
            }
            else {
                throw(new Exception(NO_MATCHING_PARAMETERS));
            }

            if (service == null && ret.isEmpty()) {
                respStatus = HttpStatus.SC_NOT_FOUND;
                qryResp = new QueryServiceVnfs();
            }else if( service == null && !ret.isEmpty()){
                qryResp = new QueryServiceVnfs(ret);				
            } else if (service != null) {
                qryResp = new QueryServiceVnfs(service.getVnfCustomizations());				
            }
            logger.debug ("serviceVnfs qryResp= {}", qryResp);
            return respond(version, respStatus, isArray, qryResp);
        } catch (Exception e) {
            logger.error("Exception - queryServiceVnfs", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {})
                    .build();
        }
    }

    @GET
    @Path("networkResources/{networkModelCustomizationUuid}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional( readOnly = true)
    public Response serviceNetworks (
            @PathParam("version") String version,
            @PathParam("networkModelCustomizationUuid") String nUuid
            ) {
        return serviceNetworksImpl (version, !IS_ARRAY, nUuid, null, null, null, null);
    }

    @GET
    @Path("serviceNetworks")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional( readOnly = true)
    public Response serviceNetworks (
            @PathParam("version") String version,
            @QueryParam("networkModelCustomizationUuid") String networkModelCustomizationUuid,
            @QueryParam("networkType") String networkType,
            @QueryParam("networkModelName") String networkModelName,
            @QueryParam("serviceModelUuid") String serviceModelUuid,
            @QueryParam("serviceModelInvariantUuid") String serviceModelInvariantUuid,
            @QueryParam("serviceModelVersion") String serviceModelVersion,
            @QueryParam("networkModelVersion") String networkModelVersion
            ) {
        if (networkModelName != null && !"".equals(networkModelName)) {
            networkType = networkModelName;
        }
        return serviceNetworksImpl (version, IS_ARRAY,  networkModelCustomizationUuid, networkType, serviceModelUuid, serviceModelInvariantUuid, serviceModelVersion);
    }

    public Response serviceNetworksImpl (String version, boolean isArray, String  networkModelCustomizationUuid, String networkType, String serviceModelUuid, String serviceModelInvariantUuid, String serviceModelVersion) {
        QueryServiceNetworks qryResp;
        int respStatus = HttpStatus.SC_OK;
        String uuid = "";
        List<NetworkResourceCustomization> ret = new ArrayList<>();
        Service service = null;

        try{
            if (networkModelCustomizationUuid != null && !"".equals(networkModelCustomizationUuid)) {
                uuid = networkModelCustomizationUuid;				
                ret = networkCustomizationRepo.findByModelCustomizationUUID(networkModelCustomizationUuid);
            }else if (networkType != null && !"".equals(networkType)) {
                uuid = networkType;				
                NetworkResource networkResources = networkResourceRepo.findFirstByModelNameOrderByModelVersionDesc(networkType);
                if(networkResources != null)
                    ret=networkResources.getNetworkResourceCustomization();
            }
            else if (serviceModelInvariantUuid != null && !"".equals(serviceModelInvariantUuid)) {
                uuid = serviceModelInvariantUuid;
                if (serviceModelVersion != null && !"".equals(serviceModelVersion)) {					
                    service = serviceRepo.findFirstByModelVersionAndModelInvariantUUID(serviceModelVersion, uuid);
                }
                else {					
                    service = serviceRepo.findFirstByModelInvariantUUIDOrderByModelVersionDesc(uuid);
                }
            }else if (serviceModelUuid != null && !"".equals(serviceModelUuid)) {
                uuid = serviceModelUuid;				
                service = serviceRepo.findOneByModelUUID(serviceModelUuid);
            }
            else {
                throw(new Exception(NO_MATCHING_PARAMETERS));
            }

            if(service != null)
                ret = service.getNetworkCustomizations();

            if (ret == null || ret.isEmpty()) {
                logger.debug ("serviceNetworks not found");
                respStatus = HttpStatus.SC_NOT_FOUND;
                qryResp = new QueryServiceNetworks();
            } else {				
                qryResp = new QueryServiceNetworks(ret);
                logger.debug ("serviceNetworks found qryResp= {}", qryResp);
            }
            return respond(version, respStatus, isArray, qryResp);
        } catch (Exception e) {
            logger.error ("Exception - queryServiceNetworks", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {})
                    .build();
        }
    }

    @GET
    @Path("serviceResources")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional(readOnly = true)
    public Response serviceResources(
            @PathParam("version") String version,
            @QueryParam("serviceModelUuid") String modelUUID,
            @QueryParam("serviceModelInvariantUuid") String modelInvariantUUID,
            @QueryParam("serviceModelVersion") String modelVersion) {
        QueryServiceMacroHolder qryResp;
        int respStatus = HttpStatus.SC_OK;
        String uuid = "";
        ServiceMacroHolder ret = new ServiceMacroHolder();

        try{
            if (modelUUID != null && !"".equals(modelUUID)) {
                uuid = modelUUID;
                logger.debug ("Query serviceMacroHolder getAllResourcesByServiceModelUuid serviceModelUuid: {}" , uuid);
                Service serv =serviceRepo.findOneByModelUUID(uuid);

                if (serv != null) {
                    ret.setNetworkResourceCustomizations(new ArrayList(serv.getNetworkCustomizations()));
                    ret.setVnfResourceCustomizations(new ArrayList(serv.getVnfCustomizations()));
                    ret.setAllottedResourceCustomizations(new ArrayList(serv.getAllottedCustomizations()));
                }
                ret.setService(serv);
            }
            else if (modelInvariantUUID != null && !"".equals(modelInvariantUUID)) {
                uuid = modelInvariantUUID;
                if (modelVersion != null && !"".equals(modelVersion)) {
                    logger.debug ("Query serviceMacroHolder getAllResourcesByServiceModelInvariantUuid serviceModelInvariantUuid: {}  serviceModelVersion: {}",uuid, modelVersion);
                    Service serv = serviceRepo.findFirstByModelVersionAndModelInvariantUUID(modelVersion, uuid);

                    ret.setService(serv);
                }
                else {
                    logger.debug ("Query serviceMacroHolder getAllResourcesByServiceModelInvariantUuid serviceModelUuid: {}" , uuid);
                    Service serv = serviceRepo.findFirstByModelInvariantUUIDOrderByModelVersionDesc(uuid);
                    ret.setService(serv);
                }
            }
            else {
                throw(new Exception(NO_MATCHING_PARAMETERS));
            }

            if (ret.getService() == null) {
                logger.debug ("serviceMacroHolder not found");
                respStatus = HttpStatus.SC_NOT_FOUND;
                qryResp = new QueryServiceMacroHolder();
            } else {
                qryResp = new QueryServiceMacroHolder(ret);
                logger.debug ("serviceMacroHolder qryResp= {}", qryResp);
            }
            return respond(version, respStatus, IS_ARRAY, qryResp);
        } catch (Exception e) {
            logger.error ("Exception - queryServiceMacroHolder", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp){} )
                    .build();
        }
    }


    @GET
    @Path("allottedResources/{arModelCustomizationUuid}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional( readOnly = true)
    public Response serviceAllottedResources (
            @PathParam("version") String version,
            @PathParam("arModelCustomizationUuid") String aUuid
            ) {
        return serviceAllottedResourcesImpl(version, !IS_ARRAY, aUuid, null, null, null);
    }

    @GET
    @Path("serviceAllottedResources")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional( readOnly = true)
    public Response serviceAllottedResources(
            @PathParam("version") String version,
            @QueryParam("serviceModelUuid") String smUuid,
            @QueryParam("serviceModelInvariantUuid") String smiUuid,
            @QueryParam("serviceModelVersion") String smVer,
            @QueryParam("arModelCustomizationUuid") String aUuid
            ) {
        return serviceAllottedResourcesImpl(version, IS_ARRAY, aUuid, smUuid, smiUuid, smVer);
    }

    public Response serviceAllottedResourcesImpl(String version, boolean isArray, String aUuid, String smUuid, String serviceModelInvariantUuid, String smVer) {
        QueryAllottedResourceCustomization qryResp;
        int respStatus = HttpStatus.SC_OK;
        String uuid = "";
        List<AllottedResourceCustomization> ret = new ArrayList<>();
        Service service = null;
        try{
            if (smUuid != null && !"".equals(smUuid)) {
                uuid = smUuid;				
                service = serviceRepo.findFirstOneByModelUUIDOrderByModelVersionDesc(uuid);			
            }
            else if (serviceModelInvariantUuid != null && !"".equals(serviceModelInvariantUuid)) {
                uuid = serviceModelInvariantUuid;
                if (smVer != null && !"".equals(smVer)) {					
                    service = serviceRepo.findFirstByModelVersionAndModelInvariantUUID(smVer, uuid);
                }
                else {				
                    service = serviceRepo.findFirstByModelInvariantUUIDOrderByModelVersionDesc(uuid);
                }
            }
            else if (aUuid != null && !"".equals(aUuid)) {
                uuid = aUuid;				
                ret = allottedCustomizationRepo.findByModelCustomizationUUID(uuid);
            }
            else {
                throw(new Exception(NO_MATCHING_PARAMETERS));
            }

            if(service != null)
                ret=service.getAllottedCustomizations();

            if (ret == null || ret.isEmpty()) {
                logger.debug ("AllottedResourceCustomization not found");
                respStatus = HttpStatus.SC_NOT_FOUND;
                qryResp = new QueryAllottedResourceCustomization();
            } else {				
                qryResp = new QueryAllottedResourceCustomization(ret);
                logger.debug ("AllottedResourceCustomization qryResp= {}", qryResp);
            }			
            return respond(version, respStatus, isArray, qryResp);
        } catch (Exception e) {
            logger.error ("Exception - queryAllottedResourceCustomization", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {})
                    .build();
        }
    }

    @GET
    @Path("vfModules")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional( readOnly = true)
    public Response vfModules(@QueryParam("vfModuleModelName") String vfModuleModelName) {
        QueryVfModule qryResp;
        int respStatus = HttpStatus.SC_OK;
        List<VfModuleCustomization> ret = null;	
        try{
            if(vfModuleModelName != null && !"".equals(vfModuleModelName)){
                VfModule vfModule = vfModuleRepo.findFirstByModelNameOrderByModelVersionDesc(vfModuleModelName);
                if(vfModule != null)
                    ret = vfModule.getVfModuleCustomization();				
            }else{
                throw(new Exception(NO_MATCHING_PARAMETERS));
            }

            if(ret == null || ret.isEmpty()){
                logger.debug ("vfModules not found");
                respStatus = HttpStatus.SC_NOT_FOUND;
                qryResp = new QueryVfModule();
            }else{			
                qryResp = new QueryVfModule(ret);				
                if(logger.isDebugEnabled())
                    logger.debug ("vfModules tojsonstring is: {}", qryResp.JSON2(false, false));
            }			
            return Response
                    .status(respStatus)
                    .entity(qryResp.JSON2(false, false)) 
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build();
        }catch(Exception e){
            logger.error ("Exception during query VfModules by vfModuleModuleName: ", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {})
                    .build();
        }
    }
    /**
     * Get the tosca csar info from catalog
     * <br>
     * 
     * @param smUuid service model uuid
     * @return the tosca csar information of the serivce.
     * @since ONAP Beijing Release
     */
    @GET
    @Path("serviceToscaCsar")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response serviceToscaCsar(@QueryParam("serviceModelUuid") String smUuid) {
        int respStatus = HttpStatus.SC_OK;
        String entity = "";
        try {
            if (smUuid != null && !"".equals(smUuid)) {
                logger.debug("Query Csar by service model uuid: {}",smUuid);
                Service service = serviceRepo.findFirstOneByModelUUIDOrderByModelVersionDesc(smUuid);

                if (service != null) {
                    ToscaCsar toscaCsar = service.getCsar();
                    if (toscaCsar != null) {
                        QueryServiceCsar serviceCsar = new QueryServiceCsar(toscaCsar);
                        entity = serviceCsar.JSON2(false, false);
                    } else {
                        respStatus = HttpStatus.SC_NOT_FOUND;
                    }
                } else {
                    respStatus = HttpStatus.SC_NOT_FOUND;
                }

            } else {
                throw (new Exception("Incoming parameter is null or blank"));
            }
            return Response
                    .status(respStatus)
                    .entity(entity)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception during query csar by service model uuid: ", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(),
                    CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {
                    })
                    .build();
        }
    }

    /**
     * Get the resource recipe info from catalog
     * <br>
     * 
     * @param rmUuid resource model uuid
     * @return the recipe information of the resource.
     * @since ONAP Beijing Release
     */
    @GET
    @Path("resourceRecipe")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response resourceRecipe(@QueryParam("resourceModelUuid") String rmUuid, @QueryParam("action") String action) {
        int respStatus = HttpStatus.SC_OK;
        String entity = "";
        try {
            if (rmUuid != null && !"".equals(rmUuid)) {
                logger.debug("Query recipe by resource model uuid: {}", rmUuid);
                //check vnf and network and ar, the resource could be any resource.
                Recipe recipe = null;

                VnfResource vnf = vnfResourceRepo.findResourceByModelUUID(rmUuid);
                if (vnf != null) {
                    recipe = vnfRecipeRepo.findFirstVnfRecipeByNfRoleAndActionAndVersionStr(vnf.getModelName(), action, vnf.getModelVersion());

                    // for network service fetch the default recipe
                    if (recipe == null && vnf.getSubCategory().equalsIgnoreCase(NETWORK_SERVICE)) {
                        recipe = vnfRecipeRepo.findFirstVnfRecipeByNfRoleAndAction("NS_DEFAULT", action);
                    }
                }


                if (null == recipe) {
                    NetworkResource nResource = networkResourceRepo.findResourceByModelUUID(rmUuid);
                    recipe = networkRecipeRepo.findFirstByModelNameAndActionAndVersionStr(nResource.getModelName(), action, vnf.getModelVersion());

                    // for network fetch the default recipe
                    if (recipe == null) {
                        recipe = networkRecipeRepo.findFirstByModelNameAndAction("SDNC_DEFAULT", action);
                    }
                }

                if (null == recipe) {
                    AllottedResource arResource = arResourceRepo.findResourceByModelUUID(rmUuid);
                    recipe = arRecipeRepo.findByModelNameAndActionAndVersion(arResource.getModelName(), action, arResource.getModelVersion());
                }
                if (recipe != null) {
                    QueryResourceRecipe resourceRecipe = new QueryResourceRecipe(recipe);
                    entity = resourceRecipe.JSON2(false, false);
                } else {
                    respStatus = HttpStatus.SC_NOT_FOUND;
                }
            } else {
                throw new Exception("Incoming parameter is null or blank");
            }
            return Response
                    .status(respStatus)
                    .entity(entity)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception during query recipe by resource model uuid: ", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(),
                    CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {
                    })
                    .build();
        }
    }
}
