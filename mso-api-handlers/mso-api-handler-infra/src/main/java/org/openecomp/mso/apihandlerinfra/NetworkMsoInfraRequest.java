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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.networkbeans.ActionType;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkInputs;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequest;
import org.openecomp.mso.apihandlerinfra.networkbeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.networkbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;

public class NetworkMsoInfraRequest {

    private String requestId;
    private String requestXML;
    private String requestUri;
    private RequestInfo rinfo;
    private NetworkInputs networkInputs;
    private String networkParams;
    private ActionType action;
    private String errorMessage;
    private String httpResponse;
    private String responseBody;
    private RequestStatusType status;
    private long startTime;
    private long progress = Constants.PROGRESS_REQUEST_RECEIVED;

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    private static final String NOT_PROVIDED = "not provided";

    protected AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager ();
    
    NetworkMsoInfraRequest (String requestId) {
        this.requestId = requestId;
        this.startTime = System.currentTimeMillis();
        MsoLogger.setLogContext (requestId, null);

    }

    // Parse request XML
    void parse (String reqXML, String version, MsoJavaProperties props) throws ValidationException {

        msoLogger.debug ("Validating the request");

        this.requestXML = reqXML;

        NetworkRequest networkReq = null;
        boolean isWrongRootElement = false;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkRequest.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();

            InputSource inputSource = new InputSource (new StringReader (reqXML));
            SAXSource source = new SAXSource (inputSource);

            if (reqXML.contains ("network-request") && !reqXML.contains ("vnf-request")) {
                networkReq = jaxbUnmarshaller.unmarshal (source, NetworkRequest.class).getValue ();
            } else {
                isWrongRootElement = true;
            }

        } catch (Exception e) {
        	 msoLogger.error (MessageEnum.APIH_VNFREQUEST_VALIDATION_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception when parsing reqXML", e);
            throw new ValidationException ("format for network request");
        }

        if (isWrongRootElement) {
        	msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR_REASON, "root element is not correct", "", "", MsoLogger.ErrorCode.DataError, "root element <network-request> expected");
            throw new ValidationException ("root element <network-request> expected");
        }

        if (networkReq == null) {
            throw new ValidationException ("network-request");
        }

        this.rinfo = networkReq.getRequestInfo ();

        if (this.rinfo == null) {
            throw new ValidationException ("request-info");
        }
        
        action = this.rinfo.getAction ();
        if (action == null) {
            throw new ValidationException ("action");
        }
        
        if (!InfraUtils.isActionAllowed (props, "network", version, action.value ())) {
        	throw new ValidationException ("action allowable for version " + version + " of network request");        	
        }

        this.networkInputs = networkReq.getNetworkInputs ();
        if (this.networkInputs == null) {
            throw new ValidationException ("network-inputs");
        }
        
        // Verify that the elements correspond to the version

        switch (version) {
            case Constants.SCHEMA_VERSION_V1:
                if (this.networkInputs.getBackoutOnFailure() != null || this.networkInputs.getAicCloudRegion() != null
                    ||
                    this.networkInputs.getServiceInstanceId() != null) {
                    throw new ValidationException("format for v1 version of network request");
                }
                break;
            case Constants.SCHEMA_VERSION_V2:
                if (this.networkInputs.getServiceType() != null || this.networkInputs.getAicNodeClli() != null ||
                    this.networkInputs.getServiceInstanceId() != null) {
                    throw new ValidationException("format for v2 version of network request");
                }
                break;
            case Constants.SCHEMA_VERSION_V3:
                if (this.networkInputs.getServiceType() != null || this.networkInputs.getAicNodeClli() != null) {
                    throw new ValidationException("format for v3 version of network request");
                }
                break;
        }

        switch (action) {            
            case UPDATE:
            case DELETE:
                if (this.networkInputs.getNetworkId () == null) {
                    throw new ValidationException ("network-id");
                }
                break;
            default:
                break;
        }

        if (ActionType.CREATE.equals (action) && this.networkInputs.getNetworkName () == null) {
            throw new ValidationException ("network-name");
        }

        if (this.networkInputs.getNetworkType () == null) {
            throw new ValidationException ("network-type");
        }
        
        if (!version.equals(Constants.SCHEMA_VERSION_V1) && this.networkInputs.getServiceId () == null) {
        	throw new ValidationException ("service-id ");
        }
        
        if (this.networkInputs.getServiceType () != null && this.networkInputs.getServiceId () != null) {
        	throw new ValidationException ("service-type or service-id ");
        }
        
        if (version.equals(Constants.SCHEMA_VERSION_V1) && this.networkInputs.getAicNodeClli () == null) {
        	throw new ValidationException ("aic-node-clli");
        }
        
        if ((version.equals (Constants.SCHEMA_VERSION_V2) || version.equals (Constants.SCHEMA_VERSION_V3)) && (this.networkInputs.getAicCloudRegion () == null || this.networkInputs.getAicCloudRegion ().isEmpty())) {
        	throw new ValidationException ("aic-cloud-region");
        }
        
        if (version.equals(Constants.SCHEMA_VERSION_V3) && this.networkInputs.getServiceInstanceId () == null) {
        	throw new ValidationException ("service-instance-id");
        }

        if (ActionType.CREATE.equals (action)) {
            if (this.networkInputs.getTenantId () == null) {
                throw new ValidationException ("tenant-id");
            }
        }

        
        Object vpN = networkReq.getNetworkParams ();

        if (vpN != null) {
            Node node = (Node) vpN;
            Document doc = node.getOwnerDocument ();
            this.networkParams = domToStr (doc);
        }

        msoLogger.debug ("NetworkParams: " + this.networkParams);

        msoLogger.debug ("Request valid");

        // Rebuild the request string for BPEL to include request-id
        rinfo.setRequestId (this.requestId);
        networkReq.setRequestInfo (rinfo);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            // output pretty printed
            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal (networkReq, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Exception: ", e);
        }

        this.requestXML = stringWriter.toString ().replace ("http://org.openecomp/mso/infra/network-request",
                                                            "http://org.openecomp/mso/infra/vnf-request");
        msoLogger.debug("REQUEST XML to BPEL: " + this.requestXML);

    }

    public void createRequestRecord (Status status) {

        long startTime = System.currentTimeMillis ();

        Session session = null;
        try {

            session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();
            session.beginTransaction ();

            InfraActiveRequests aq = new InfraActiveRequests ();
            aq.setRequestId (requestId);

            Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
            if (rinfo != null) {
                if (rinfo.getAction () != null) {
                    aq.setAction (rinfo.getAction ().value ());
                    aq.setRequestAction (RequestActionMap.getMappedRequestAction (rinfo.getAction ().value ()));
                }
                aq.setSource (rinfo.getSource ());
            } else {
                // Set up mandatory parameters
                aq.setAction (NOT_PROVIDED);
                aq.setRequestAction (NOT_PROVIDED);
            }

            aq.setRequestBody (this.requestXML);
            aq.setRequestScope (ModelType.network.name ());

            if (networkInputs != null) {
                if (networkInputs.getNetworkId () != null) {
                    aq.setVnfId (networkInputs.getNetworkId ());
                }
                if (networkInputs.getNetworkName () != null) {
                    aq.setVnfName (networkInputs.getNetworkName ());
                }
                if (networkInputs.getNetworkType () != null) {
                    aq.setVnfType (networkInputs.getNetworkType ());
                }
                if (networkInputs.getServiceInstanceId () != null) {
                    aq.setServiceInstanceId (networkInputs.getServiceInstanceId ());
                }
                if (networkInputs.getServiceType () != null) {
                    aq.setServiceType (networkInputs.getServiceType ());
                }
                if (networkInputs.getServiceId () != null) {
                    aq.setServiceType (networkInputs.getServiceId ());
                }
                if (networkInputs.getAicNodeClli () != null) {
                    aq.setAicNodeClli (networkInputs.getAicNodeClli ());
                }
                if (networkInputs.getTenantId () != null) {
                    aq.setTenantId (networkInputs.getTenantId ());
                }
                if (networkInputs.getProvStatus () != null) {
                    aq.setProvStatus (networkInputs.getProvStatus ());
                }

            }
            aq.setStartTime (startTimeStamp);
            aq.setRequestStatus (status.toString ());
            aq.setLastModifiedBy (Constants.MODIFIED_BY_APIHANDLER);
            aq.setRequestType ("NETWORK");

            if (networkParams != null) {
                msoLogger.debug ("Storing networkParams: " + networkParams);
                aq.setVnfParams (this.networkParams);
            }

            if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
                aq.setStatusMessage (this.errorMessage);
                aq.setResponseBody (this.responseBody);

                Timestamp endTimeStamp = new Timestamp (System.currentTimeMillis());
                aq.setEndTime (endTimeStamp);                
            } 
            aq.setProgress (this.progress);
            

            msoLogger.debug ("About to insert a record");

            session.save (aq);
            session.getTransaction ().commit ();
            session.close ();
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully", "RequestDB", "saveRequest", null);

        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in createRequestRecord", e);
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, e.getMessage (), "RequestDB", "saveRequest", null);
            if (session != null) {
                session.close ();
            }
            if (!status.equals (Status.FAILED)) {
                throw e;
            }
        }
    }

    public void updateFinalStatus (Status status) {
        try {
            (RequestsDatabase.getInstance()).updateInfraFinalStatus (requestId,
                                                     status.toString (),
                                                     this.errorMessage,
                                                     this.progress,
                                                     this.responseBody,
                                                     Constants.MODIFIED_BY_APIHANDLER);
        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DB_UPDATE_EXC, e.getMessage (), "", "", MsoLogger.ErrorCode.SchemaError, "Exception in updateFinalStatus");
            msoLogger.debug ("Exception: ", e);
        }
    }

    public Response buildResponse (int httpResponseCode, String errorCode, InfraActiveRequests inProgress) {
        return buildResponseWithError (httpResponseCode, errorCode, inProgress, null);
    }

    public Response buildResponseWithError (int httpResponseCode,
                                            String errorCode,
                                            InfraActiveRequests inProgress,
                                            String errorString) {

        ObjectFactory beansObjectFactory = new ObjectFactory ();

        NetworkRequest vr = beansObjectFactory.createNetworkRequest ();

        RequestInfo ri = beansObjectFactory.createRequestInfo ();

        ri.setRequestId (requestId);
        ri.setRequestStatus (this.status);
        ri.setAction (this.rinfo.getAction ());
        ri.setSource (this.rinfo.getSource ());

        String errorMsg = null;
        if (errorCode != null) {
            // If error code is actually an XML error response from BPEL, treat it specially:
            if (!Messages.errors.containsKey (errorCode)) {
                if (errorCode.length () > 300) {
                    errorMsg = errorCode.substring (0, 299);
                } else {
                    errorMsg = errorCode;
                }

            } else {

                if (inProgress == null) {
                    if (errorCode.equals (ErrorNumbers.RECIPE_DOES_NOT_EXIST)) {
                        errorMsg = String.format (Messages.errors.get (errorCode), "network", errorString);
                    } else {
                        errorMsg = String.format (Messages.errors.get (errorCode), errorString);
                    }
                } else if (errorCode.equals (ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS)) {
                    errorMsg = String.format (Messages.errors.get (errorCode),
                                              "network",
                                              inProgress.getVnfName (),
                                              inProgress.getRequestStatus (),
                                              "network");
                } else if (errorCode.equals (ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID)) {
                    errorMsg = String.format (Messages.errors.get (errorCode),
                                              "network",
                                              inProgress.getVnfId (),
                                              inProgress.getRequestStatus (),
                                              inProgress.getAction (),
                                              "network");
                }
            }

            ri.setStatusMessage (errorMsg);
            this.errorMessage = errorMsg;
        }
        ri.setProgress ((int) this.progress);

        Date startDate = new Date (this.startTime);
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
        String startTimeString = sdf.format (startDate);

        ri.setStartTime (startTimeString);

        vr.setRequestInfo (ri);
        vr.setNetworkInputs (this.networkInputs);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();
            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal (vr, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Exception: ", e);
        }

        String response = stringWriter.toString ();

        this.httpResponse = Integer.toString (httpResponseCode);
        this.responseBody = response;

        // Log the failed request into the MSO Requests database

        return Response.status (httpResponseCode).entity (response).build ();

    }

    public Response buildResponseFailedValidation (int httpResponseCode, String exceptionMessage) {

        ObjectFactory beansObjectFactory = new ObjectFactory ();
        NetworkRequest vr = beansObjectFactory.createNetworkRequest ();

        RequestInfo ri = beansObjectFactory.createRequestInfo ();
        ri.setRequestId (requestId);

        if (this.rinfo != null) {
            if (this.rinfo.getAction () != null) {
                ri.setAction (this.rinfo.getAction ());
            } else {
                ri.setAction (ActionType.NOT_PROVIDED);
            }
            if (this.rinfo.getSource () != null) {
                ri.setSource (this.rinfo.getSource ());
            }
        } else {
            ri.setAction (ActionType.NOT_PROVIDED);
        }

        // Nothing more is expected for this request

        String errorMsg = String.format (Messages.errors.get (ErrorNumbers.REQUEST_FAILED_SCHEMA_VALIDATION
                                                              + "_service"),
                                         exceptionMessage);
        ri.setStatusMessage (errorMsg);
        this.errorMessage = errorMsg;

        ri.setProgress ((int) this.progress);
        ri.setRequestStatus (RequestStatusType.FAILED);
        Date startDate = new Date (this.startTime);
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
        String startTimeString = sdf.format (startDate);

        ri.setStartTime (startTimeString);

        vr.setRequestInfo (ri);
        vr.setNetworkInputs (this.networkInputs);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            // output pretty printed
            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal (vr, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Error marshalling", e);
        }

        String response = stringWriter.toString ();

        this.httpResponse = Integer.toString (httpResponseCode);
        this.responseBody = response;

        return Response.status (httpResponseCode).entity (response).build ();
    }

    public String getRequestUri () {
        return requestUri;
    }

    public void setRequestUri (String requestUri) {
        this.requestUri = requestUri;
    }

    public NetworkInputs getNetworkInputs () {
        return networkInputs;
    }

    public RequestInfo getRequestInfo () {
        return rinfo;
    }

    public String getResponseBody () {
        return responseBody;
    }

    public void setResponseBody (String responseBody) {
        this.responseBody = responseBody;
    }

    public String getHttpResponse () {
        return httpResponse;
    }

    public void setHttpResponse (String httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getRequestId () {
        return requestId;
    }

    public String getRequestXML () {
        return requestXML;
    }

    public void setRequestXML (String requestXML) {
        this.requestXML = requestXML;
    }

    public RequestStatusType getStatus () {
        return status;
    }

    public void setStatus (RequestStatusType status) {
        this.status = status;
        switch (status) {
        case FAILED:
        case COMPLETE:
        	this.progress = Constants.PROGRESS_REQUEST_COMPLETED;
        	break;
        case IN_PROGRESS:
        	this.progress = Constants.PROGRESS_REQUEST_IN_PROGRESS;
        	break;
        }
    }
    
    public String getServiceType () {
    	if (this.networkInputs.getServiceType () != null) 
    		return this.networkInputs.getServiceType ();
    	if (this.networkInputs.getServiceId () != null) 
    		return this.networkInputs.getServiceId ();
    	return null;
    }

    public static String domToStr (Document doc) {
        if (doc == null) {
            return null;
        }

        try {
            StringWriter sw = new StringWriter ();
            StreamResult sr = new StreamResult (sw);
            TransformerFactory tf = TransformerFactory.newInstance ();
            Transformer t = tf.newTransformer ();
            t.setOutputProperty (OutputKeys.STANDALONE, "yes");
            NodeList nl = doc.getDocumentElement ().getChildNodes ();
            DOMSource source = null;
            for (int x = 0; x < nl.getLength (); x++) {
                Node e = nl.item (x);
                if (e instanceof Element) {
                    source = new DOMSource (e);
                    break;
                }
            }
            if (source != null) {
                t.transform (source, sr);

                String s = sw.toString ();
                return s;
            }

            return null;

        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DOM2STR_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in domToStr", e);
        }
        return null;
    }
}
