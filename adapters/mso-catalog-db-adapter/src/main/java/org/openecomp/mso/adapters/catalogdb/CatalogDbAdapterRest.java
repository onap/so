/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import java.util.Map;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Holder;

import org.apache.http.HttpStatus;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse;
import org.openecomp.mso.adapters.catalogrest.QueryServiceVnfs;
import org.openecomp.mso.adapters.catalogrest.QueryServiceNetworks;
import org.openecomp.mso.adapters.catalogrest.QueryServiceMacroHolder;
import org.openecomp.mso.adapters.catalogrest.QueryAllottedResourceCustomization;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;

/**
 * This class services calls to the REST interface for VF Modules (http://host:port/ecomp/mso/catalog/v1)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * Requests respond synchronously only
 */
@Path("/v1")
public class CatalogDbAdapterRest {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

	@HEAD
	@GET
	@Path("healthcheck")
	@Produces(MediaType.TEXT_HTML)
	public Response healthcheck () {
		String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
		return Response.ok().entity(CHECK_HTML).build();
	}

	/*
	 * GET {http-catalog-adapter-root}/v1/serviceVnfs?vnfModelCustomizationUuid=<vnf-model-customization-uuid>
	 * URL:http://localhost:8080/ecomp/mso/catalog/v1/getVfModuleType?vnfType=Test/vSAMP10&vfModuleType=vSAMP10::base::module-0
	 * RESP:
	 * {"queryVfModule":{"version":1,"asdcUuid":"MANUAL RECORD","created":{"nanos":0},"description":"vSAMP10","environmentId":15184,"id":2312,"isBase":1,"modelName":"vSAMP10::base::module-0","modelVersion":1,"templateId":15123,"type":"Test\/vSAMP10::vSAMP10::base::module-0","vnfResourceId":15187}}
	 */
	@GET
	@Path("serviceVnfs")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceVnfs(
			@QueryParam("vnfModelCustomizationUuid") String vnfUuid,
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
			@QueryParam("serviceModelVersion") String smVer,
			@QueryParam("serviceModelName") String smName
			) {
		QueryServiceVnfs qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		List<VnfResource> ret;

        try (CatalogDatabase db = new CatalogDatabase()) {

			if (vnfUuid != null && !vnfUuid.equals("")) {
				uuid = vnfUuid;
				LOGGER.debug ("Query serviceVnfs getAllVnfsByVnfModelCustomizationUuid vnfModelCustomizationUuid: " + uuid);
				ret = db.getAllVnfsByVnfModelCustomizationUuid(uuid);
			}
			else if (smUuid != null && !smUuid.equals("")) {
				uuid = smUuid;
				LOGGER.debug ("Query serviceVnfs getAllVnfsByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllVnfsByServiceModelUuid(uuid);
			}
			else if (smiUuid != null && !smiUuid.equals("")) {
				uuid = smiUuid;
				if (smVer != null && !smVer.equals("")) {
					LOGGER.debug ("Query serviceVnfs getAllNetworksByServiceModelInvariantUuid serviceModelInvariantUuid: " + uuid+ " serviceModelVersion: "+ smVer);
					ret = db.getAllVnfsByServiceModelInvariantUuid(uuid, smVer);
				}
				else {
					LOGGER.debug ("Query serviceVnfs getAllNetworksByServiceModelInvariantUuid serviceModelUuid: " + uuid);
					ret = db.getAllVnfsByServiceModelInvariantUuid(uuid);
				}
			}
			else if (smName != null && !smName.equals("")) {
				if (smVer != null && !smVer.equals("")) {
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
				LOGGER.debug ("serviceVnfs tojsonstring="+ qryResp.toJsonString());
			}
			LOGGER.debug ("Query serviceVnfs exit");
			return Response
				.status(respStatus)
				//.entity(new GenericEntity<QueryServiceVnfs>(qryResp) {})
				.entity(qryResp.toJsonString())
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryServiceVnfs", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryServiceVnfs", e);
			VfModuleExceptionResponse excResp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<VfModuleExceptionResponse>(excResp) {})
				.build();
		}
	}

	@GET
	@Path("serviceNetworks")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceNetworks (
			@QueryParam("networkModelCustomizationUuid") String nUuid,
			@QueryParam("networkType") String nType,
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
			@QueryParam("serviceModelVersion") String smVer
			) {
		QueryServiceNetworks qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		List<NetworkResourceCustomization> ret;

        try (CatalogDatabase db = new CatalogDatabase()) {
			if (nUuid != null && !nUuid.equals("")) {
				uuid = nUuid;
				LOGGER.debug ("Query serviceNetworks getAllNetworksByNetworkModelCustomizationUuid networkModelCustomizationUuid: " + uuid);
				ret = db.getAllNetworksByNetworkModelCustomizationUuid(uuid);
			}
			else if (smUuid != null && !smUuid.equals("")) {
				uuid = smUuid;
				LOGGER.debug ("Query serviceNetworks getAllNetworksByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllNetworksByServiceModelUuid(uuid);
			}
			else if (nType != null && !nType.equals("")) {
				uuid = nType;
				LOGGER.debug ("Query serviceNetworks getAllNetworksByNetworkType serviceModelUuid: " + uuid);
				ret = db.getAllNetworksByNetworkType(uuid);
			}
			else if (smiUuid != null && !smiUuid.equals("")) {
				uuid = smiUuid;
				if (smVer != null && !smVer.equals("")) {
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
				LOGGER.debug ("serviceNetworks tojsonstring="+ qryResp.toJsonString());
			}
			LOGGER.debug ("Query serviceNetworks exit");
			return Response
				.status(respStatus)
				//.entity(new GenericEntity<QueryServiceNetworks>(qryResp) {})
				.entity(qryResp.toJsonString())
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryServiceNetworks", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryServiceNetworks", e);
			VfModuleExceptionResponse excResp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<VfModuleExceptionResponse>(excResp) {})
				.build();
		}
	}

	@GET
	@Path("serviceResources")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceResources(
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
			@QueryParam("serviceModelVersion") String smVer
			) {
		QueryServiceMacroHolder qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		ServiceMacroHolder ret;

        try (CatalogDatabase db = new CatalogDatabase()) {

			if (smUuid != null && !smUuid.equals("")) {
				uuid = smUuid;
				LOGGER.debug ("Query serviceMacroHolder getAllResourcesByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllResourcesByServiceModelUuid(uuid);
			}
			else if (smiUuid != null && !smiUuid.equals("")) {
				uuid = smiUuid;
				if (smVer != null && !smVer.equals("")) {
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
				LOGGER.debug ("serviceMacroHolder tojsonstring="+ qryResp.toJsonString());
			}
			LOGGER.debug ("Query serviceMacroHolder exit");
			return Response
				.status(respStatus)
				//.entity(new GenericEntity<QueryServiceMacroHolder>(qryResp) {})
				.entity(qryResp.toJsonString())
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryServiceMacroHolder", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryServiceMacroHolder", e);
			VfModuleExceptionResponse excResp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<VfModuleExceptionResponse>(excResp) {})
				.build();
		}
	}

	@GET
	@Path("serviceAllottedResources")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response serviceAllottedResources(
			@QueryParam("serviceModelUuid") String smUuid,
			@QueryParam("serviceModelInvariantUuid") String smiUuid,
			@QueryParam("serviceModelVersion") String smVer,
			@QueryParam("arModelCustomizationUuid") String aUuid
			) {
		QueryAllottedResourceCustomization qryResp;
		int respStatus = HttpStatus.SC_OK;
		String uuid = "";
		List<AllottedResourceCustomization > ret;

        try (CatalogDatabase db = new CatalogDatabase()) {

			if (smUuid != null && !smUuid.equals("")) {
				uuid = smUuid;
				LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByServiceModelUuid serviceModelUuid: " + uuid);
				ret = db.getAllAllottedResourcesByServiceModelUuid(uuid);
			}
			else if (smiUuid != null && !smiUuid.equals("")) {
				uuid = smiUuid;
				if (smVer != null && !smVer.equals("")) {
					LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByServiceModelInvariantUuid serviceModelInvariantUuid: " + uuid+ " serviceModelVersion: "+ smVer);
					ret = db.getAllAllottedResourcesByServiceModelInvariantUuid(uuid, smVer);
				}
				else {
					LOGGER.debug ("Query AllottedResourceCustomization getAllAllottedResourcesByServiceModelInvariantUuid serviceModelUuid: " + uuid);
					ret = db.getAllAllottedResourcesByServiceModelInvariantUuid(uuid);
				}
			}
			else if (aUuid != null && !aUuid.equals("")) {
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
				LOGGER.debug ("AllottedResourceCustomization tojsonstring="+ qryResp.toJsonString());
			}
			LOGGER.debug ("Query AllottedResourceCustomization exit");
			return Response
				.status(respStatus)
				//.entity(new GenericEntity<QueryAllottedResourceCustomization>(qryResp) {})
				.entity(qryResp.toJsonString())
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  uuid, "", "queryAllottedResourceCustomization", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - queryAllottedResourceCustomization", e);
			VfModuleExceptionResponse excResp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<VfModuleExceptionResponse>(excResp) {})
				.build();
		}
	}
}