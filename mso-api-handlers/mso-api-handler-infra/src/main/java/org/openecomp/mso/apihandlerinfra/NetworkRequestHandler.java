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

package org.openecomp.mso.apihandlerinfra;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.xml.sax.InputSource;

import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.networkbeans.ActionType;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkInputs;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkOutputs;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequest;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequests;
import org.openecomp.mso.apihandlerinfra.networkbeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.networkbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Recipe;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

@Path("/{version: v1|v2|v3}/network-request")
public class NetworkRequestHandler {

    @Context
    private UriInfo uriInfo;

    protected ObjectFactory beansObjectFactory = new ObjectFactory ();

    public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

    private static MsoJavaProperties props = MsoPropertiesUtils.loadMsoProperties ();

    private static final String NOT_FOUND = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Application Not Started</title></head><body>Application not started, properties file missing or invalid or Database Connection failed</body></html>";

    private static final Response NOT_STARTED_RESPONSE = Response.status (HttpStatus.SC_SERVICE_UNAVAILABLE)
            .entity (NOT_FOUND)
            .build ();

    private RequestsDatabase requestDB = RequestsDatabase.getInstance();
    
    @GET
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
                break;
            case Constants.SCHEMA_VERSION_V3:
                if (ar.getAaiServiceId() != null) {
                    vi.setServiceId(ar.getAaiServiceId());
                }
                if (ar.getAicCloudRegion() != null) {
                    vi.setAicCloudRegion(ar.getAicCloudRegion());
                }
                if (ar.getServiceInstanceId() != null) {
                    vi.setServiceInstanceId(ar.getServiceInstanceId());
                }

                break;
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

        InfraActiveRequests activeReq = requestDB.getRequestFromInfraActive (requestId, "NETWORK");
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

        List <InfraActiveRequests> activeReqList = requestDB.getRequestListFromInfraActive (queryAttribute,
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

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_XML)
    public Response manageNetworkRequest (String reqXML, @PathParam("version") String version) {
        MsoLogger.setServiceName ("NetworkRequest");
    	if ("v1".equals(version)) {
            return manageNetworkRequestImpl (reqXML, Constants.SCHEMA_VERSION_V1);
    	} else if ("v2".equals(version)) {
            return manageNetworkRequestImpl (reqXML, Constants.SCHEMA_VERSION_V2);
    	} else if ("v3".equals(version)) {
            return manageNetworkRequestImpl (reqXML, Constants.SCHEMA_VERSION_V3);
    	} else {
            long startTime = System.currentTimeMillis ();
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "Version not found");
    		return Response.status(HttpStatus.SC_NOT_FOUND).build();
    	}
    }

    private Response manageNetworkRequestImpl (String reqXML, String version) {

    	props = MsoPropertiesUtils.loadMsoProperties ();
        
        long startTime = System.currentTimeMillis ();
        if (MsoPropertiesUtils.getNoPropertiesState()) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Application not started, properties file missing or invalid");
        	return NOT_STARTED_RESPONSE;
        }
        uriInfo.getRequestUri ();

        // Generate unique request id for the new request
        UUID requestUUID = UUID.randomUUID ();

        NetworkMsoInfraRequest msoRequest = new NetworkMsoInfraRequest (requestUUID.toString ());
        MsoLogger.setLogContext (msoRequest.getRequestId (), null);

        if (reqXML == null) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "The input Request is null");
            return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
        }

        String requestUri = uriInfo.getRequestUri ().toString ();

        msoLogger.debug ("Incoming Request: " + reqXML);

        msoRequest.setRequestUri (requestUri);
        msoLogger.debug ("Schema version: " + version);
        try {
            msoRequest.parse (reqXML, version, props);
        } catch (Exception e) {
            msoLogger.debug ("Validation failed: ", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseFailedValidation (HttpStatus.SC_BAD_REQUEST, e.getMessage ());
            if (msoRequest.getRequestId () != null) {
                msoLogger.debug ("Logging failed message to the database");
                msoRequest.createRequestRecord (Status.FAILED);
            }
            msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, reqXML, "", "", MsoLogger.ErrorCode.DataError, "Exception when parsing reqXML", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
        MsoLogger.setServiceName (MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo().getAction().name());
        msoLogger.debug ("Update serviceName with detailed action info to:" + MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo().getAction().name());
        if (msoRequest.getRequestInfo ()
                      .getAction () == org.openecomp.mso.apihandlerinfra.networkbeans.ActionType.CREATE) {
            // Check if this request is a duplicate of the one with the same network Name
            msoLogger.debug ("Checking for a duplicate with the same network-name");
            InfraActiveRequests dup = null;
            try {

                dup = requestDB.checkDuplicateByVnfName (msoRequest.getNetworkInputs ().getNetworkName (),
                                                                msoRequest.getRequestInfo ().getAction ().value (),
                                                                "NETWORK");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "network-name", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for duplicated request", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for duplicated request");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                "CREATE on the same Network Name is already progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicates request - CREATE on the same Network Name is already progress");
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicates request - CREATE on the same Network Name is already progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        } else {
            // Check if this request is a duplicate of the one with the same networkId
            InfraActiveRequests dup = null;
            msoLogger.debug ("Checking for a duplicate with the same network-id");
            try {
                dup = requestDB.checkDuplicateByVnfId (msoRequest.getNetworkInputs ().getNetworkId (),
                                                              msoRequest.getRequestInfo ().getAction ().value (),
                                                              "NETWORK");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "network-id", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for a duplicate request with the same network-id", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for a duplicate request with the same network-id");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                msoRequest.getRequestInfo ().getAction ().value ()
                                                                  + " on the same Network Id already in progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicated request on the same Network Id already in progress");

                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicated request on the same Network Id already in progress.");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        }

        String orchestrationURI = "";

        // Query MSO Catalog DB
        try (CatalogDatabase db = CatalogDatabase.getInstance()) {

            Recipe recipe = null;

            if (msoRequest.getServiceType () != null
                    && msoRequest.getServiceType ().length () > 0) {
                recipe = db.getNetworkRecipe (msoRequest.getNetworkInputs ().getNetworkType (),
                        msoRequest.getRequestInfo ().getAction ().value (),
                        msoRequest.getServiceType ());

            }
            if (recipe == null) {
                recipe = db.getNetworkRecipe (msoRequest.getNetworkInputs ().getNetworkType (),
                        msoRequest.getRequestInfo ().getAction ().value (),
                        null);
            }

            if (recipe == null) {
                msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, "VNF Recipe", "", "", MsoLogger.ErrorCode.DataError, "VNF Recipe attribute not found");
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                        ErrorNumbers.RECIPE_DOES_NOT_EXIST,
                        null,
                        "");
                msoRequest.createRequestRecord (Status.FAILED);
                db.close ();
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No recipe found in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            orchestrationURI = recipe.getOrchestrationUri ();
            msoLogger.debug ("Orchestration URI is: " + orchestrationURI);

            db.close ();

            String requestId = msoRequest.getRequestId ();
            msoLogger.debug ("requestId is: " + requestId);
            msoLogger.debug ("About to insert a record");

            try {
                msoRequest.createRequestRecord (Status.PENDING);
            } catch (Exception e) {
                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.DataError, "Exception while creating record in DB", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB,
                        null,
                        "non-unique request-id specified");
                // Cannot create a record of this request here, our communication with MSO DB just failed. Do not try
                // to create a failed record
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }

            RequestClient requestClient = null;
            HttpResponse response = null;
            long subStartTime = System.currentTimeMillis();
            try {
                requestClient = RequestClientFactory.getRequestClient (orchestrationURI, props);
                // Capture audit event
                msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());
                response = requestClient.post (msoRequest.getRequestXML (),
                        requestId,
                        Integer.toString (recipe.getRecipeTimeout ()),
                        version,
                        null,
                        null);
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", orchestrationURI, null);
            } catch (Exception e) {
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", orchestrationURI, null);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_COMMUNICATION_TO_BPEL,
                        null,
                        e.getMessage ());
                alarmLogger.sendAlarm ("MsoConfigurationError",
                        MsoAlarmLogger.CRITICAL,
                        Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, "Camunda", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            if (response == null) {
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_RESPONSE_FROM_BPEL,
                        null,
                        "bpelResponse is null");
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Null response from BPEL", "Camunda", "", MsoLogger.ErrorCode.DataError, "bpelResponse is null");
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is null");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
            int bpelStatus = respHandler.getStatus ();

            // BPEL accepted the request, the request is in progress
            if (bpelStatus == HttpStatus.SC_ACCEPTED) {
                String bpelXMLResponseBody = respHandler.getResponseBody ();
                msoLogger.debug ("Received from BPEL: " + bpelXMLResponseBody);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.IN_PROGRESS);
                requestDB.updateInfraStatus (msoRequest.getRequestId (),
                        Status.IN_PROGRESS.toString (),
                        Constants.PROGRESS_REQUEST_IN_PROGRESS,
                        Constants.MODIFIED_BY_APIHANDLER);
                Response resp = msoRequest.buildResponse (bpelStatus, null, null);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN accepted the request, the request is in progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            } else {

                String bpelXMLResponseBody = respHandler.getResponseBody ();
                if (bpelXMLResponseBody != null && !bpelXMLResponseBody.isEmpty ()) {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, bpelXMLResponseBody, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR,
                            "Response from BPEL engine is failed with HTTP Status=" + bpelStatus, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is with status Failed");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                } else {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, ErrorNumbers.ERROR_FROM_BPEL, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Response from BPEL engine is empty", "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                }
            }
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception while communciate with Catalog DB", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                                                                   ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB,
                                                                   null,
                                                                   e.getMessage ());
            alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                   MsoAlarmLogger.CRITICAL,
                                   Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
            msoRequest.createRequestRecord (Status.FAILED);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
    }
}
