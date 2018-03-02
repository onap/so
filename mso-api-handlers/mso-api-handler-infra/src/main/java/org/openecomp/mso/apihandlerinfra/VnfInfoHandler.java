package org.openecomp.mso.apihandlerinfra;

/*-
 * #%L
 * MSO
 * %%
 * Copyright (C) 2016 ONAP - SO
 * %%
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
 * #L%
 */


import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.http.HttpStatus;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.vnfbeans.ActionType;
import org.openecomp.mso.apihandlerinfra.vnfbeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.vnfbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfInputs;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfOutputs;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfRequest;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfRequests;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;
import org.xml.sax.InputSource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/{version: v1|v2|v3}/vnf-request")
@Api(value="/{version: v1|v2|v3}/vnf-request",description="API Requests of vnfRequest")
public class VnfInfoHandler {

    protected ObjectFactory beansObjectFactory = new ObjectFactory ();

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);


    @GET
    @ApiOperation(value="Finds Volume Requests",response=Response.class)
    public Response queryFilters (@QueryParam("vnf-type") String vnfType,
                                  @QueryParam("service-type") String serviceType,
                                  @QueryParam("aic-node-clli") String aicNodeClli,
                                  @QueryParam("tenant-id") String tenantId,
                                  @QueryParam("volume-group-id") String volumeGroupId,
                                  @QueryParam("volume-group-name") String volumeGroupName,
                                  @QueryParam("vnf-name") String vnfName,
                                  @PathParam("version") String version) {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("VNFQueryFilters");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for queryFilter with vnf-type:" + vnfType
        							+ " service-type:" + serviceType
        							+ " aic-node-clli:" + aicNodeClli
        							+ " tenant-id:" + tenantId
        							+ " volume-group-id:" + volumeGroupId
        							+ " volume-group-name:" + volumeGroupName
        							+ " vnf-name: " + vnfName);
        Response response;
        if (vnfType != null) {
            response = this.getRequestList ("vnfType", vnfType, version);
        } else {
            response = queryGenericFilters (serviceType, aicNodeClli, tenantId, volumeGroupId, volumeGroupName, vnfName, version);
        }
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
        return response;
    }

    @GET
    @Path(Constants.REQUEST_ID_PATH)
    @ApiOperation(value="Add a Vnf Outputs from requestId and version",response=Response.class)
    public Response getRequest (@PathParam("request-id") String requestId, @PathParam("version") String version) {
        // Check INFRA_ACTIVE_REQUESTS table to find info
        // on this request
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("VNFGetRequest");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for getRequest with request-id:" + requestId + ", version = " + version);

        Response response = getRequestGeneric (requestId, version);
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
        return response;
    }

    protected MsoLogger getMsoLogger () {
        return msoLogger;
    }

    protected String getRequestType () {
        return VnfRequestType.VNF.toString ();
    }

    protected void fillVnfRequest (VnfRequest qr, InfraRequests ar, String version) {
        VnfInputs vi = beansObjectFactory.createVnfInputs ();

        if (ar.getVnfId () != null) {
            vi.setVnfId (ar.getVnfId ());
        }
        if (ar.getVnfName () != null) {
            vi.setVnfName (ar.getVnfName ());
        }
        if (ar.getVnfType () != null) {
            vi.setVnfType (ar.getVnfType ());
        }        
        if (ar.getTenantId () != null) {
            vi.setTenantId (ar.getTenantId ());
        }
        if (ar.getProvStatus () != null) {
            vi.setProvStatus (ar.getProvStatus ());
        }
        if (ar.getVolumeGroupName () != null) {
        	vi.setVolumeGroupName (ar.getVolumeGroupName ());
        }
        if (ar.getVolumeGroupId () != null) {
        	vi.setVolumeGroupId (ar.getVolumeGroupId ());
        }
        if (version.equals(Constants.SCHEMA_VERSION_V1)) {
        	if (ar.getServiceType () != null) {
        		vi.setServiceType (ar.getServiceType ());
        	}
        	if (ar.getAicNodeClli () != null) {
        		vi.setAicNodeClli (ar.getAicNodeClli ());
        	}
        }
        else if (version.equals(Constants.SCHEMA_VERSION_V2)) {
        	if (ar.getAaiServiceId () != null) {
        		vi.setServiceId (ar.getAaiServiceId ());
        	}
        	if (ar.getAicCloudRegion () != null) {
        		vi.setAicCloudRegion (ar.getAicCloudRegion ());
        	}
        	if (ar.getVfModuleName () != null) {
        		vi.setVfModuleName (ar.getVfModuleName ());
        	}
        	if (ar.getVfModuleId () != null) {
        		vi.setVfModuleId (ar.getVfModuleId ());
        	}
        	if (ar.getVfModuleModelName () != null) {
        		vi.setVfModuleModelName (ar.getVfModuleModelName ());
        	}        	
        }
        else if (version.equals(Constants.SCHEMA_VERSION_V3)) {
        	if (ar.getAaiServiceId () != null) {
        		vi.setServiceId (ar.getAaiServiceId ());
        	}
        	if (ar.getAicCloudRegion () != null) {
        		vi.setAicCloudRegion (ar.getAicCloudRegion ());
        	}
        	if (ar.getVfModuleName () != null) {
        		vi.setVfModuleName (ar.getVfModuleName ());
        	}
        	if (ar.getVfModuleId () != null) {
        		vi.setVfModuleId (ar.getVfModuleId ());
        	}
        	if (ar.getVfModuleModelName () != null) {
        		vi.setVfModuleModelName (ar.getVfModuleModelName ());
        	}
        	if (ar.getServiceInstanceId () != null) {
        		vi.setServiceInstanceId (ar.getServiceInstanceId ());
        	}
        }
        qr.setVnfInputs (vi);

        qr.setVnfParams (ar.getVnfParams ());

        try {
            String vnfoutputs = ar.getVnfOutputs ();
            if (vnfoutputs != null && vnfoutputs.length () > 0) {
                msoLogger.debug ("Read VNF outputs: " + vnfoutputs);
                VnfOutputs vnfOutput = null;

                // Now unmarshal it into vnf outputs
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance (VnfOutputs.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();

                    InputSource inputSource = new InputSource (new StringReader (vnfoutputs));
                    SAXSource source = new SAXSource (inputSource);

                    vnfOutput = jaxbUnmarshaller.unmarshal (source, VnfOutputs.class).getValue ();

                } catch (Exception e) {
                    msoLogger.debug ("Validation failed", e);
                    throw new ValidationException ("format for vnf outputs");
                }

                qr.setVnfOutputs (vnfOutput);
            }
        } catch (Exception e) {
            msoLogger.debug ("exception reading vnfOutputs Clob", e);
        }
    }

    protected Response queryGenericFilters (String serviceType, String aicNodeClli, String tenantId, String volumeGroupId, String volumeGroupName, String vnfName, String version) {
        if (serviceType != null) {
            return this.getRequestList ("serviceType", serviceType, version);
        }
        if (aicNodeClli != null) {
            return this.getRequestList ("aicNodeClli", aicNodeClli, version);
        }
        if (tenantId != null) {
            return this.getRequestList ("tenantId", tenantId, version);
        }
        if (volumeGroupId != null) {
        	return this.getRequestList ("volumeGroupId", volumeGroupId, version);
        }
        if (volumeGroupName != null) {
        	return this.getRequestList ("volumeGroupName", volumeGroupName, version);
        }
        if (vnfName != null) {
        	return this.getRequestList ("vnfName", vnfName, version);
        }
        return Response.status (HttpStatus.SC_BAD_REQUEST).entity ("").build ();
    }

    protected Response getRequestGeneric (String requestId, String version) {
        // Check INFRA_ACTIVE_REQUESTS table to find info
        // on this request

        getMsoLogger ().debug ("getRequest: " + requestId);

        String responseString;

        InfraActiveRequests activeReq = (RequestsDatabase.getInstance()).getRequestFromInfraActive (requestId, getRequestType ());
        if (activeReq != null) {
            // build response for active
            responseString = infraRequestsResponse (activeReq, version);
            return Response.status (HttpStatus.SC_OK).entity (responseString).build ();
        } else {
            // Report that no request has been found
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }
    }

    protected Response getRequestList (String queryAttribute, String queryValue, String version) {
        // Check INFRA_ACTIVE_REQUESTS table to find info
        // on this request

        getMsoLogger ().debug ("getRequest based on " + queryAttribute + ": " + queryValue);

        List <InfraActiveRequests> activeReqList = (RequestsDatabase.getInstance()).getRequestListFromInfraActive (queryAttribute,
                                                                                                   queryValue,
                                                                                                   getRequestType ());

        List <VnfRequest> queryResponseList = new LinkedList <> ();

        if (activeReqList != null) {
            // build response for active
            queryResponseList = infraRequestsResponses (activeReqList, version);

        }

        if (queryResponseList != null && !queryResponseList.isEmpty ()) {
            String result = this.translateVnfRequests (queryResponseList);
            return Response.status (HttpStatus.SC_OK).entity (result).build ();

        } else {
            // Report that no request has been found
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }
    }

    private VnfRequest fillGeneric (InfraRequests ar) {
        VnfRequest qr = beansObjectFactory.createVnfRequest ();
        RequestInfo ri = beansObjectFactory.createRequestInfo ();
        ri.setRequestId (ar.getRequestId ());
        ri.setAction (ActionType.fromValue (ar.getAction ()));
        ri.setRequestStatus (RequestStatusType.fromValue (ar.getRequestStatus ()));
        if (ar.getProgress () != null) {
            ri.setProgress (ar.getProgress ().intValue ());
        }
        if (ar.getSource () != null) {
            ri.setSource (ar.getSource ());
        }

        ri.setStartTime (ar.getStartTime ().toString ());
        if (ar.getEndTime () != null) {
            ri.setEndTime (ar.getEndTime ().toString ());
        }

        if (ar.getStatusMessage () != null) {
            ri.setStatusMessage (ar.getStatusMessage ());
        }
        qr.setRequestInfo (ri);
        return qr;
    }

    private List <VnfRequest> infraRequestsResponses (List <? extends InfraRequests> arList, String version) {
        List <VnfRequest> queryResponseList = new LinkedList <> ();

        for (InfraRequests ar : arList) {
            VnfRequest qr = fillGeneric (ar);
            fillVnfRequest (qr, ar, version);
            queryResponseList.add (qr);
        }
        return queryResponseList;
    }

    private String translateVnfRequests (List <VnfRequest> queryResponseList) {
        VnfRequests queryResponses = new VnfRequests ();
        for (VnfRequest aQueryResponseList : queryResponseList) {
            queryResponses.getVnfRequest().add(aQueryResponseList);
        }

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequests.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            // output pretty printed
            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal (queryResponses, stringWriter);

        } catch (JAXBException e) {
            getMsoLogger ().debug ("Marshalling issue", e);
        }

        return stringWriter.toString ();
    }

    private String infraRequestsResponse (InfraRequests ar, String version) {
        VnfRequest qr = fillGeneric (ar);
        fillVnfRequest (qr, ar, version);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (qr, stringWriter);

        } catch (JAXBException e) {
            getMsoLogger ().debug ("Marshalling issue", e);
        }

        String response = stringWriter.toString ();
        return response;
    }
}
