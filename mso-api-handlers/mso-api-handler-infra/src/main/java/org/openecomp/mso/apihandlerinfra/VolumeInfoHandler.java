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
import org.openecomp.mso.apihandlerinfra.volumebeans.ActionType;
import org.openecomp.mso.apihandlerinfra.volumebeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.volumebeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType;
import org.openecomp.mso.apihandlerinfra.volumebeans.VolumeInputs;
import org.openecomp.mso.apihandlerinfra.volumebeans.VolumeOutputs;
import org.openecomp.mso.apihandlerinfra.volumebeans.VolumeRequest;
import org.openecomp.mso.apihandlerinfra.volumebeans.VolumeRequests;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;
import org.xml.sax.InputSource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/{version: v1|v2|v3}/volume-request")
@Api(value="/{version: v1|v2|v3}/volume-request",description="API Requests for volumeRequest")
public class VolumeInfoHandler {
	
	protected ObjectFactory beansObjectFactory = new ObjectFactory ();

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);

    @GET
    @ApiOperation(value="Finds Volume Requests",response=Response.class)
    public Response queryFilters (@QueryParam("vnf-type") String vnfType,
                                  @QueryParam("service-type") String serviceType,
                                  @QueryParam("aic-node-clli") String aicNodeClli,
                                  @QueryParam("tenantId") String tenantId,
                                  @QueryParam("volume-group-id") String volumeGroupId,
                                  @QueryParam("volume-group-name") String volumeGroupName,
                                  @PathParam("version") String version) {
    	long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("VolumeQueryFilters");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for queryFilter with vnf-type:" + vnfType
        							+ " service-type:" + serviceType
        							+ " aic-node-clli:" + aicNodeClli
        							+ " tenant-id:" + tenantId
        							+ " volume-group-id:" + volumeGroupId
        							+ " volume-group-name:" + volumeGroupName);
        Response response;
    	if (vnfType != null) {
            response = this.getRequestList ("vnfType", vnfType, version);
        } else {
        	response = queryGenericFilters (serviceType, aicNodeClli, tenantId, volumeGroupId, volumeGroupName, version);
        }
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
        return response;
    }

    @GET
    @Path(Constants.REQUEST_ID_PATH)
    @ApiOperation(value="Find Volume Outputs by requestId and verison",response=Response.class)
    public Response getRequest (@PathParam("request-id") String requestId, @PathParam("version") String version) {
    	
    	// Check INFRA_ACTIVE_REQUESTS table to find info
        // on this request
        
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("VolumeGetRequest");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for getRequest with request-id:" + requestId);

        Response response = getRequestGeneric (requestId, version);
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
        return response;

    }

   
    protected MsoLogger getMsoLogger () {
        return msoLogger;
    }

   
     protected void fillVolumeRequest (VolumeRequest qr, InfraRequests ar, String version) {
        VolumeInputs vi = beansObjectFactory.createVolumeInputs ();

        if (ar.getVolumeGroupId () != null) {
            vi.setVolumeGroupId (ar.getVolumeGroupId ());
        }
        if (ar.getVolumeGroupName () != null) {
            vi.setVolumeGroupName (ar.getVolumeGroupName ());
        }
        if (ar.getVnfType () != null) {
            vi.setVnfType (ar.getVnfType ());
        }

         switch (version) {
             case Constants.SCHEMA_VERSION_V1:
                 if (ar.getServiceType() != null) {
                     vi.setServiceType(ar.getServiceType());
                 }
                 if (ar.getAicNodeClli() != null) {
                     vi.setAicNodeClli(ar.getAicNodeClli());
                 }
                 break;
             case Constants.SCHEMA_VERSION_V2:
                 if (ar.getAaiServiceId() != null) {
                     vi.setServiceId(ar.getAaiServiceId());
                 }
                 if (ar.getAicCloudRegion() != null) {
                     vi.setAicCloudRegion(ar.getAicCloudRegion());
                 }
                 if (ar.getVfModuleModelName() != null) {
                     vi.setVfModuleModelName(ar.getVfModuleModelName());
                 }
                 break;
             case Constants.SCHEMA_VERSION_V3:
                 if (ar.getAaiServiceId() != null) {
                     vi.setServiceId(ar.getAaiServiceId());
                 }
                 if (ar.getAicCloudRegion() != null) {
                     vi.setAicCloudRegion(ar.getAicCloudRegion());
                 }
                 if (ar.getVfModuleModelName() != null) {
                     vi.setVfModuleModelName(ar.getVfModuleModelName());
                 }
                 if (ar.getServiceInstanceId() != null) {
                     vi.setServiceInstanceId(ar.getServiceInstanceId());
                 }
                 if (ar.getVnfId() != null) {
                     vi.setVnfId(ar.getVnfId());
                 }
                 break;
         }
        if (ar.getTenantId () != null) {
            vi.setTenantId (ar.getTenantId ());
        }
        
        qr.setVolumeInputs (vi);
        
        qr.setVolumeParams(ar.getVnfParams ());

        try {
            String volumeoutputs = ar.getVnfOutputs ();
            if (volumeoutputs != null && volumeoutputs.length () > 0) {
                msoLogger.debug ("Read VOLUME outputs: " + volumeoutputs);
                VolumeOutputs volumeOutput = null;

                // Now unmarshal it into network outputs
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance (VolumeOutputs.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();

                    InputSource inputSource = new InputSource (new StringReader (volumeoutputs));
                    SAXSource source = new SAXSource (inputSource);

                    volumeOutput = (VolumeOutputs) jaxbUnmarshaller.unmarshal (source, VolumeOutputs.class).getValue ();

                } catch (Exception e) {
                    msoLogger.debug ("Validation failed", e);
                    throw new ValidationException ("format for volume outputs");
                }

                qr.setVolumeOutputs (volumeOutput);
            }
        } catch (Exception e) {
            msoLogger.debug ("exception reading networkOutputs Clob", e);
        }
    }
    
    protected Response queryGenericFilters (String serviceType, String aicNodeClli, String tenantId, String volumeGroupId, String volumeGroupName, String version) {
        if (serviceType != null) {
            return this.getRequestList ("serviceType", serviceType, version);
        }
        if (aicNodeClli != null) {
            return this.getRequestList ("aicNodeClli", aicNodeClli, version);
        }
        if (tenantId != null) {
            return this.getRequestList ("tenantId", tenantId, version);
        }
        if (volumeGroupName != null) {
        	return this.getRequestList ("volumeGroupName", volumeGroupName, version);
        }
        if (volumeGroupId != null) {
        	return this.getRequestList ("volumeGroupId", volumeGroupId, version);
        }
        return Response.status (HttpStatus.SC_BAD_REQUEST).entity ("").build ();
    }


    protected Response getRequestGeneric (String requestId, String version) {
        // Check INFRA_ACTIVE_REQUESTS table  to find info
        // on this request

        getMsoLogger ().debug ("getRequest: " + requestId);

        String responseString;

        InfraActiveRequests activeReq = (RequestsDatabase.getInstance()).getRequestFromInfraActive (requestId,
                                                                                    "VOLUME");
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
                                                                                                   "VOLUME");
  
        List <VolumeRequest> queryResponseList = new LinkedList <> ();

        if (activeReqList != null) {
            // build response for active
            queryResponseList = infraRequestsResponses (activeReqList, version);

        }

 
        if (queryResponseList != null && !queryResponseList.isEmpty ()) {
            String result = this.translateVolumeRequests (queryResponseList);
            return Response.status (HttpStatus.SC_OK).entity (result).build ();

        } else {
            // Report that no request has been found
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }
    }

    private VolumeRequest fillGeneric (InfraRequests ar) {
        VolumeRequest qr = beansObjectFactory.createVolumeRequest ();
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

    private List <VolumeRequest> infraRequestsResponses (List <? extends InfraRequests> arList, String version) {
        List <VolumeRequest> queryResponseList = new LinkedList <> ();

        for (InfraRequests ar : arList) {
            VolumeRequest qr = fillGeneric (ar);
            fillVolumeRequest (qr, ar, version);
            queryResponseList.add (qr);
        }
        return queryResponseList;
    }

    private String translateVolumeRequests (List <VolumeRequest> queryResponseList) {
        VolumeRequests queryResponses = new VolumeRequests ();
        for (VolumeRequest aQueryResponseList : queryResponseList) {
            queryResponses.getVolumeRequest().add(aQueryResponseList);
        }

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VolumeRequests.class);
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
        VolumeRequest qr = fillGeneric (ar);
        fillVolumeRequest (qr, ar, version);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VolumeRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (qr, stringWriter);

        } catch (JAXBException e) {
            getMsoLogger ().debug ("Marshalling issue", e);
        }

        String response = stringWriter.toString ();
        return response;
    }

    private String getAuditLogReturnMsg (Response response) {
        String returnMsg = "";
        if (response.getStatus() == HttpStatus.SC_OK) {
        	returnMsg = "Successful. StatusCode=" + HttpStatus.SC_OK;
        } else if (response.getStatus() == HttpStatus.SC_NOT_FOUND) {
        	returnMsg = "Record not found . StatusCode=" + HttpStatus.SC_NOT_FOUND;
        } else if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
        	returnMsg = "Bad request: one of the following attribute serviceType, aicNodeClli, tenantId, volumeGroupId, volumeGroupName should be defined. StatusCode=" + HttpStatus.SC_BAD_REQUEST;
        }
        return returnMsg;
    }
}
