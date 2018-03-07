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
package org.openecomp.mso.adapters.catalogdb;

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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.openecomp.mso.adapters.catalogdb.catalogrest.CatalogQuery;
import org.openecomp.mso.adapters.catalogdb.catalogrest.CatalogQueryException;
import org.openecomp.mso.adapters.catalogdb.catalogrest.CatalogQueryExceptionCategory;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryAllottedResourceCustomization;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryResourceRecipe;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryServiceCsar;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryServiceMacroHolder;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryServiceNetworks;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryServiceVnfs;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryVfModule;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Recipe;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This class services calls to the REST interface for VF Modules (http://host:port/ecomp/mso/catalog/v1)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * Requests respond synchronously only
 */
@Path("/{version: v[0-9]+}")
public class CatalogDbAdapterRest {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private static final boolean IS_ARRAY = true;

	public Response respond(String version, int respStatus, boolean isArray, CatalogQuery qryResp) {
		return Response
				.status(respStatus)
				//.entity(new GenericEntity<QueryServiceVnfs>(qryResp) {})
				.entity(qryResp.toJsonString(version, isArray))
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
	}

	@HEAD
	@GET
	@Path("healthcheck")
	@Produces(MediaType.TEXT_HTML)
	public Response healthcheck (
			@PathParam("version") String version
	) {
		String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application "+ version+ " ready</body></html>";
		return Response.ok().entity(CHECK_HTML).build();
	}

	@GET
	@Path("vnfResources/{vnfModelCustomizationUuid}")
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

	public Response serviceVnfsImpl(String version, boolean isArray, String vnfUuid, String smUuid, String smiUuid, String smVer, String smName) {
		QueryServiceVnfs qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		List<VnfResourceCustomization> ret;

		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			if (vnfUuid != null && !"".equals(vnfUuid)) {
				uuid = vnfUuid;
				LOGGER.debug ("Query serviceVnfs getAllVnfsByVnfModelCustomizationUuid vnfModelCustomizationUuid: " + uuid);
				ret = db.getAllVnfsByVnfModelCustomizationUuid(uuid);
			}
			else if (smUuid != null && !"".equals(smUuid)) {
				uuid = smUuid;
				LOGGER.debug ("Query serviceVnfs getAllVnfsByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllVnfsByServiceModelUuid(uuid);
			}
			else if (smiUuid != null && !"".equals(smiUuid)) {
				uuid = smiUuid;
				if (smVer != null && !"".equals(smVer)) {
					LOGGER.debug ("Query serviceVnfs getAllNetworksByServiceModelInvariantUuid serviceModelInvariantUuid: " + uuid+ " serviceModelVersion: "+ smVer);
					ret = db.getAllVnfsByServiceModelInvariantUuid(uuid, smVer);
				}
				else {
					LOGGER.debug ("Query serviceVnfs getAllNetworksByServiceModelInvariantUuid serviceModelUuid: " + uuid);
					ret = db.getAllVnfsByServiceModelInvariantUuid(uuid);
				}
			}
			else if (smName != null && !"".equals(smName)) {
				if (smVer != null && !"".equals(smVer)) {
					LOGGER.debug ("Query serviceVnfs getAllVnfsByServiceName serviceModelInvariantName: " + smName+ " serviceModelVersion: "+ smVer);
					ret = db.getAllVnfsByServiceName(smName, smVer);
				}
				else {
					LOGGER.debug ("Query serviceVnfs getAllVnfsByServiceName serviceModelName: " + smName);
					ret = db.getAllVnfsByServiceName(smName);
				}
			}
			else {
				throw(new Exception("no matching parameters"));
			}

			if (ret == null || ret.isEmpty()) {
				LOGGER.debug ("serviceVnfs not found");
				respStatus = HttpStatus.SC_NOT_FOUND;
				qryResp = new QueryServiceVnfs();
			} else {
				LOGGER.debug ("serviceVnfs found");
				qryResp = new QueryServiceVnfs(ret);
				LOGGER.debug ("serviceVnfs qryResp="+ qryResp);
			}
			LOGGER.debug ("Query serviceVnfs exit");
			return respond(version, respStatus, isArray, qryResp);
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryServiceVnfs", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryServiceVnfs", e);
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
	public Response serviceNetworks (
			@PathParam("version") String version,
			@PathParam("networkModelCustomizationUuid") String nUuid
	) {
		return serviceNetworksImpl (version, !IS_ARRAY, nUuid, null, null, null, null);
	}

	@GET
	@Path("serviceNetworks")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceNetworks (
			@PathParam("version") String version,
			@QueryParam("networkModelCustomizationUuid") String nUuid,
			@QueryParam("networkType") String nType,
		@QueryParam("networkModelName") String nModelName,
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
		@QueryParam("serviceModelVersion") String smVer,
		@QueryParam("networkModelVersion") String nmVer
	) {
		if (nModelName != null && !"".equals(nModelName)) {
			nType = nModelName;
		}
		return serviceNetworksImpl (version, IS_ARRAY, nUuid, nType, smUuid, smiUuid, smVer);
	}

	public Response serviceNetworksImpl (String version, boolean isArray, String nUuid, String nType, String smUuid, String smiUuid, String smVer) {
		QueryServiceNetworks qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		List<NetworkResourceCustomization> ret;

		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			if (nUuid != null && !"".equals(nUuid)) {
				uuid = nUuid;
				LOGGER.debug ("Query serviceNetworks getAllNetworksByNetworkModelCustomizationUuid networkModelCustomizationUuid: " + uuid);
				ret = db.getAllNetworksByNetworkModelCustomizationUuid(uuid);
			}
			else if (smUuid != null && !"".equals(smUuid)) {
				uuid = smUuid;
				LOGGER.debug ("Query serviceNetworks getAllNetworksByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllNetworksByServiceModelUuid(uuid);
			}
			else if (nType != null && !"".equals(nType)) {
				uuid = nType;
				LOGGER.debug ("Query serviceNetworks getAllNetworksByNetworkType serviceModelUuid: " + uuid);
				ret = db.getAllNetworksByNetworkType(uuid);
			}
			else if (smiUuid != null && !"".equals(smiUuid)) {
				uuid = smiUuid;
				if (smVer != null && !"".equals(smVer)) {
					LOGGER.debug ("Query serviceNetworks getAllNetworksByServiceModelInvariantUuid serviceModelInvariantUuid: " + uuid+ " serviceModelVersion: "+ smVer);
					ret = db.getAllNetworksByServiceModelInvariantUuid(uuid, smVer);
				}
				else {
					LOGGER.debug ("Query serviceNetworks getAllNetworksByServiceModelInvariantUuid serviceModelUuid: " + uuid);
					ret = db.getAllNetworksByServiceModelInvariantUuid(uuid);
				}
			}
			else {
				throw(new Exception("no matching parameters"));
			}

			if (ret == null || ret.isEmpty()) {
				LOGGER.debug ("serviceNetworks not found");
				respStatus = HttpStatus.SC_NOT_FOUND;
				qryResp = new QueryServiceNetworks();
			} else {
				LOGGER.debug ("serviceNetworks found");
				qryResp = new QueryServiceNetworks(ret);
				LOGGER.debug ("serviceNetworks qryResp="+ qryResp);
			}
			LOGGER.debug ("Query serviceNetworks exit");
			return respond(version, respStatus, isArray, qryResp);
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryServiceNetworks", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryServiceNetworks", e);
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
	public Response serviceResources(
			@PathParam("version") String version,
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
			@QueryParam("serviceModelVersion") String smVer) {
		QueryServiceMacroHolder qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		ServiceMacroHolder ret;

		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			if (smUuid != null && !"".equals(smUuid)) {
				uuid = smUuid;
				LOGGER.debug ("Query serviceMacroHolder getAllResourcesByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllResourcesByServiceModelUuid(uuid);
			}
			else if (smiUuid != null && !"".equals(smiUuid)) {
				uuid = smiUuid;
				if (smVer != null && !"".equals(smVer)) {
					LOGGER.debug ("Query serviceMacroHolder getAllResourcesByServiceModelInvariantUuid serviceModelInvariantUuid: " + uuid+ " serviceModelVersion: "+ smVer);
					ret = db.getAllResourcesByServiceModelInvariantUuid(uuid, smVer);
				}
				else {
					LOGGER.debug ("Query serviceMacroHolder getAllResourcesByServiceModelInvariantUuid serviceModelUuid: " + uuid);
					ret = db.getAllResourcesByServiceModelInvariantUuid(uuid);
				}
			}
			else {
				throw(new Exception("no matching parameters"));
			}

			if (ret == null) {
				LOGGER.debug ("serviceMacroHolder not found");
				respStatus = HttpStatus.SC_NOT_FOUND;
				qryResp = new QueryServiceMacroHolder();
			} else {
				LOGGER.debug ("serviceMacroHolder found");
				qryResp = new QueryServiceMacroHolder(ret);
				LOGGER.debug ("serviceMacroHolder qryResp="+ qryResp);
			}
			LOGGER.debug ("Query serviceMacroHolder exit");
			return respond(version, respStatus, IS_ARRAY, qryResp);
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryServiceMacroHolder", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryServiceMacroHolder", e);
			CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<CatalogQueryException>(excResp) {})
				.build();
		}
	}

	@GET
	@Path("allottedResources/{arModelCustomizationUuid}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceAllottedResources (
			@PathParam("version") String version,
			@PathParam("arModelCustomizationUuid") String aUuid
	) {
		return serviceAllottedResourcesImpl(version, !IS_ARRAY, aUuid, null, null, null);
	}

	@GET
	@Path("serviceAllottedResources")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceAllottedResources(
			@PathParam("version") String version,
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
			@QueryParam("serviceModelVersion") String smVer,
			@QueryParam("arModelCustomizationUuid") String aUuid
	) {
		return serviceAllottedResourcesImpl(version, IS_ARRAY, aUuid, smUuid, smiUuid, smVer);
	}

	public Response serviceAllottedResourcesImpl(String version, boolean isArray, String aUuid, String smUuid, String smiUuid, String smVer) {
		QueryAllottedResourceCustomization qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		List<AllottedResourceCustomization > ret;

		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			if (smUuid != null && !"".equals(smUuid)) {
				uuid = smUuid;
				LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllAllottedResourcesByServiceModelUuid(uuid);
			}
			else if (smiUuid != null && !"".equals(smiUuid)) {
				uuid = smiUuid;
				if (smVer != null && !"".equals(smVer)) {
					LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByServiceModelInvariantUuid serviceModelInvariantUuid: " + uuid+ " serviceModelVersion: "+ smVer);
					ret = db.getAllAllottedResourcesByServiceModelInvariantUuid(uuid, smVer);
				}
				else {
					LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByServiceModelInvariantUuid serviceModelUuid: " + uuid);
					ret = db.getAllAllottedResourcesByServiceModelInvariantUuid(uuid);
				}
			}
			else if (aUuid != null && !"".equals(aUuid)) {
				uuid = aUuid;
				LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByArModelCustomizationUuid serviceModelUuid: " + uuid);
				ret = db.getAllAllottedResourcesByArModelCustomizationUuid(uuid);
			}
			else {
				throw(new Exception("no matching parameters"));
			}

			if (ret == null || ret.isEmpty()) {
				LOGGER.debug ("AllottedResourceCustomization not found");
				respStatus = HttpStatus.SC_NOT_FOUND;
				qryResp = new QueryAllottedResourceCustomization();
			} else {
				LOGGER.debug ("AllottedResourceCustomization found");
				qryResp = new QueryAllottedResourceCustomization(ret);
				LOGGER.debug ("AllottedResourceCustomization qryResp="+ qryResp);
			}
			LOGGER.debug ("Query AllottedResourceCustomization exit");
			return respond(version, respStatus, isArray, qryResp);
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryAllottedResourceCustomization", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryAllottedResourceCustomization", e);
			CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<CatalogQueryException>(excResp) {})
				.build();
		}
	}
	
	// Added for DHV in 1702.  Might be a temporary solution!
	// Changing to use QueryVfModule so the modelCustomizationUuid is included in response
	@GET
	@Path("vfModules")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response vfModules(@QueryParam("vfModuleModelName") String vfModuleModelName) {
		QueryVfModule qryResp;
		int respStatus = HttpStatus.SC_OK;
		List<VfModuleCustomization> ret = null;

        try (CatalogDatabase db = CatalogDatabase.getInstance()) {
            if (vfModuleModelName != null && !"".equals(vfModuleModelName)) {
                LOGGER.debug("Query vfModules by vfModuleModuleName: " + vfModuleModelName);
                VfModuleCustomization vfModule = db.getVfModuleCustomizationByModelName(vfModuleModelName);
                if (vfModule != null) {
                    ret = new ArrayList<>(1);
                    ret.add(vfModule);
                }
            } else {
                throw (new Exception("Incoming parameter is null or blank"));
            }
            if (ret == null || ret.isEmpty()) {
                LOGGER.debug("vfModules not found");
                respStatus = HttpStatus.SC_NOT_FOUND;
                qryResp = new QueryVfModule();
            } else {
                LOGGER.debug("vfModules found");
                qryResp = new QueryVfModule(ret);
                LOGGER.debug("vfModules query Results is: " + qryResp);
                LOGGER.debug("vfModules tojsonstring is: " + qryResp.JSON2(false, false));
            }
            LOGGER.debug("Query vfModules exit");
            return Response
                .status(respStatus)
                .entity(qryResp.JSON2(false, false))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOGGER.error(MessageEnum.RA_QUERY_VNF_ERR, vfModuleModelName, "", "queryVfModules",
                MsoLogger.ErrorCode.BusinessProcesssError, "Exception during query VfModules by vfModuleModuleName: ",
                e);
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
        try (CatalogDatabase db = CatalogDatabase.getInstance()) {
            if (smUuid != null && !"".equals(smUuid)) {
                LOGGER.debug("Query Csar by service model uuid: " + smUuid);
                ToscaCsar toscaCsar = db.getToscaCsarByServiceModelUUID(smUuid);
                if (toscaCsar != null) {
                    QueryServiceCsar serviceCsar = new QueryServiceCsar(toscaCsar);
                    entity = serviceCsar.JSON2(false, false);
                } else {
                    respStatus = HttpStatus.SC_NOT_FOUND;
                }
            } else {
                throw (new Exception("Incoming parameter is null or blank"));
            }
            LOGGER.debug("Query Csar exit");
            return Response
                .status(respStatus)
                .entity(entity)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOGGER.error(MessageEnum.RA_QUERY_VNF_ERR, smUuid, "", "ServiceToscaCsar",
                MsoLogger.ErrorCode.BusinessProcesssError, "Exception during query csar by service model uuid: ", e);
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
        CatalogDatabase db = CatalogDatabase.getInstance();
        String entity = "";
        try{
            if(rmUuid != null && !"".equals(rmUuid)){
                LOGGER.debug ("Query recipe by resource model uuid: " + rmUuid);
                //check vnf and network and ar, the resource could be any resource.
                Recipe recipe = db.getVnfRecipeByModuleUuid(rmUuid, action);
                if(null == recipe){
                    recipe = db.getNetworkRecipeByModuleUuid(rmUuid, action);
                }
                if(null == recipe){
                    recipe = db.getArRecipeByModuleUuid(rmUuid, action);
                }
                if(recipe != null){
                    QueryResourceRecipe resourceRecipe = new QueryResourceRecipe(recipe);
                    entity = resourceRecipe.JSON2(false, false);
                }
                else{
                    respStatus = HttpStatus.SC_NOT_FOUND;
                }
            }else{
                throw(new Exception("Incoming parameter is null or blank"));
            }           
            LOGGER.debug ("Query recipe exit");
            return Response
                    .status(respStatus)
                    .entity(entity) 
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build();
        }catch(Exception e){
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  rmUuid, "", "resourceRecipe", MsoLogger.ErrorCode.BusinessProcesssError, "Exception during query recipe by resource model uuid: ", e);
            CatalogQueryException excResp = new CatalogQueryException(e.getMessage(), CatalogQueryExceptionCategory.INTERNAL, Boolean.FALSE, null);
            return Response
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<CatalogQueryException>(excResp) {})
                    .build();
        }finally {
            db.close();
        }
    }
}
