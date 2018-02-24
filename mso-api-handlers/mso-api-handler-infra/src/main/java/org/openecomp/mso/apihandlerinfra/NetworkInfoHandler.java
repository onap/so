
package org.openecomp.mso.apihandlerinfra;

/*-
 * #%L
 * MSO
 * %%
 * Copyright (C) 2016 OPENECOMP - MSO
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
import org.openecomp.mso.apihandlerinfra.networkbeans.ActionType;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkInputs;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkOutputs;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequest;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequests;
import org.openecomp.mso.apihandlerinfra.networkbeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.networkbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;
import org.xml.sax.InputSource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/{version: v1|v2|v3}/network-request")
@Api(value="/{version: v1|v2|v3}/network-request",description="API Requests for network requests")
public class NetworkInfoHandler {

    protected ObjectFactory beansObjectFactory = new ObjectFactory ();

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    @GET
    @ApiOperation(value="Finds Network Requests",response=Response.class)
    public Response queryFilters (@QueryParam("network-type") String networkType,
                                  @QueryParam("service-type") String serviceType,
                                  @QueryParam("aic-node-clli") String aicNodeClli,
                                  @QueryParam("tenant-id") String tenantId,
                                  @PathParam("version") String version) {
        MsoLogger.setServiceName ("QueryFilters");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        long startTime = System.currentTimeMillis ();

        msoLogger.debug ("Incoming request received for query filters with Network type " + networkType
                                         + " - service type "
                                         + serviceType
                                         + " - aicNodeClli "
                                         + aicNodeClli
                                         + " - tenant id "
                                         + tenantId);
        Response response;
        if (networkType != null) {
            response = this.getRequestList ("vnfType", networkType, version);
        } else {
            response = queryGenericFilters (serviceType, aicNodeClli, tenantId, version);
        }
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
        return response;
    }

    @GET
    @Path(Constants.REQUEST_ID_PATH)
    @ApiOperation(value="Add a Network Outputs from requestId and version",response=Response.class)
    public Response getRequest (@PathParam("request-id") String requestId, @PathParam("version") String version) {
        // Check INFRA_ACTIVE_REQUESTS table to find info
        // on this request
        MsoLogger.setServiceName ("GetRequest");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for getRequest with requestId=" + requestId + ", version = " + version);
        long startTime = System.currentTimeMillis ();

        Response response = getRequestGeneric (requestId, version);
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
        return response;
    }

    protected MsoLogger getMsoLogger () {
        return msoLogger;
    }

    protected void fillNetworkRequest (NetworkRequest qr, InfraRequests ar, String version) {
        NetworkInputs vi = beansObjectFactory.createNetworkInputs ();

        if (ar.getVnfId () != null) {
            vi.setNetworkId (ar.getVnfId ());
        }
        if (ar.getVnfName () != null) {
            vi.setNetworkName (ar.getVnfName ());
        }
        if (ar.getVnfType () != null) {
            vi.setNetworkType (ar.getVnfType ());
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
        }
        else if (version.equals(Constants.SCHEMA_VERSION_V3)) {
        	if (ar.getAaiServiceId () != null) {
        		vi.setServiceId (ar.getAaiServiceId ());
        	}
        	if (ar.getAicCloudRegion () != null) {
        		vi.setAicCloudRegion (ar.getAicCloudRegion ());
        	}
        	if (ar.getServiceInstanceId () != null) {
        		vi.setServiceInstanceId (ar.getServiceInstanceId ());
        	}
        			
        }
        
        if (ar.getTenantId () != null) {
            vi.setTenantId (ar.getTenantId ());
        }
        if (ar.getProvStatus () != null) {
            vi.setProvStatus (ar.getProvStatus ());
        }
        qr.setNetworkInputs (vi);

        qr.setNetworkParams (ar.getVnfParams ());

        try {
            String networkoutputs = ar.getVnfOutputs ();
            if (networkoutputs != null && networkoutputs.length () > 0) {
                msoLogger.debug ("Read NETWORK outputs: " + networkoutputs);
                NetworkOutputs networkOutput = null;

                // Now unmarshal it into network outputs
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance (NetworkOutputs.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();

                    InputSource inputSource = new InputSource (new StringReader (networkoutputs));
                    SAXSource source = new SAXSource (inputSource);

                    networkOutput = jaxbUnmarshaller.unmarshal (source, NetworkOutputs.class).getValue ();

                } catch (Exception e) {
                    msoLogger.debug ("Validation failed", e);
                    throw new ValidationException ("format for network outputs");
                }

                qr.setNetworkOutputs (networkOutput);
            }
        } catch (Exception e) {
            msoLogger.debug ("exception reading networkOutputs Clob", e);
        }
    }

    protected Response queryGenericFilters (String serviceType, String aicNodeClli, String tenantId, String version) {
        if (serviceType != null) {
            return this.getRequestList ("serviceType", serviceType, version);
        }
        if (aicNodeClli != null) {
            return this.getRequestList ("aicNodeClli", aicNodeClli, version);
        }
        if (tenantId != null) {
            return this.getRequestList ("tenantId", tenantId, version);
        }
        return Response.status (HttpStatus.SC_BAD_REQUEST).entity ("").build ();
    }

    protected Response getRequestGeneric (String requestId, String version) {
        // Check INFRA_ACTIVE_REQUESTS table to find info
        // on this request
        MsoLogger.setLogContext (requestId, null);
        getMsoLogger ().debug ("getRequest: " + requestId);

        String responseString;

        InfraActiveRequests activeReq = (RequestsDatabase.getInstance()).getRequestFromInfraActive (requestId, "NETWORK");
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
                                                                                                   "NETWORK");

        List <NetworkRequest> queryResponseList = new LinkedList <> ();

        if (activeReqList != null) {
            // build response for active
            queryResponseList = infraRequestsResponses (activeReqList, version);

        }

        if (queryResponseList != null && !queryResponseList.isEmpty ()) {
            String result = this.translateNetworkRequests (queryResponseList);
            return Response.status (HttpStatus.SC_OK).entity (result).build ();

        } else {
            // Report that no request has been found
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }
    }

    private NetworkRequest fillGeneric (InfraRequests ar) {
        NetworkRequest qr = beansObjectFactory.createNetworkRequest ();
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

    private List <NetworkRequest> infraRequestsResponses (List <? extends InfraRequests> arList, String version) {
        List <NetworkRequest> queryResponseList = new LinkedList <> ();

        for (InfraRequests ar : arList) {
            NetworkRequest qr = fillGeneric (ar);
            fillNetworkRequest (qr, ar, version);
            queryResponseList.add (qr);
        }
        return queryResponseList;
    }

    private String translateNetworkRequests (List <NetworkRequest> queryResponseList) {
        NetworkRequests queryResponses = new NetworkRequests ();
        for (NetworkRequest aQueryResponseList : queryResponseList) {
            queryResponses.getNetworkRequest().add(aQueryResponseList);
        }

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkRequests.class);
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
        NetworkRequest qr = fillGeneric (ar);
        fillNetworkRequest (qr, ar, version);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkRequest.class);
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
