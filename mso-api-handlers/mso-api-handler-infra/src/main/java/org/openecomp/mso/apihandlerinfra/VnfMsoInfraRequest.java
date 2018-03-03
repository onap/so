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
import java.util.List;

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
import org.openecomp.mso.apihandlerinfra.vnfbeans.ActionType;
import org.openecomp.mso.apihandlerinfra.vnfbeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.vnfbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfInputs;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfRequest;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;

public class VnfMsoInfraRequest {

    private String requestId;
    private String requestXML;
    private String requestUri;
    private VnfRequest vnfReq;
    private RequestInfo rinfo;
    private VnfInputs vnfInputs;
    private String vnfParams;
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
    
    VnfMsoInfraRequest (String requestId) {
        this.requestId = requestId;
        this.startTime = System.currentTimeMillis();
        MsoLogger.setLogContext (requestId, null);

    }

    // Parse request XML
    void parse (String reqXML, String version, MsoJavaProperties props) throws ValidationException {

        msoLogger.debug ("Validating the request");

        this.requestXML = reqXML;

        VnfRequest vnfReq = null;
        boolean isWrongRootElement = false;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();

            InputSource inputSource = new InputSource (new StringReader (reqXML));
            SAXSource source = new SAXSource (inputSource);

            if (reqXML.contains ("vnf-request") && !reqXML.contains ("network-request")) {
                vnfReq = jaxbUnmarshaller.unmarshal (source, VnfRequest.class).getValue ();
            } else {
                isWrongRootElement = true;
            }

        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_VNFREQUEST_VALIDATION_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in format for vnf request ", e);
            throw new ValidationException ("format for vnf request");
        }

        if (isWrongRootElement) {
        	msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR_REASON, "root element is not correct", "", "", MsoLogger.ErrorCode.DataError, "root element is not correct");
            throw new ValidationException ("root element <vnf-request> expected");
        }

        if (vnfReq == null) {
            throw new ValidationException ("vnf-request");
        }
        this.vnfReq = vnfReq;

        this.rinfo = vnfReq.getRequestInfo ();

        if (this.rinfo == null) {
            throw new ValidationException ("request-info");
        }
        if (this.rinfo.getRequestId () != null) {
            msoLogger.info (MessageEnum.APIH_GENERATED_REQUEST_ID, requestId, this.rinfo.getRequestId ());
        }

        action = this.rinfo.getAction ();
        if (action == null) {
            throw new ValidationException ("action");
        }

        this.vnfInputs = vnfReq.getVnfInputs ();
        if (this.vnfInputs == null) {
            throw new ValidationException ("vnf-inputs");
        }
        
        // Verify that BPMN-specific elements are not in the APIH incoming request
        if (this.vnfInputs.getPersonaModelId () != null || this.vnfInputs.getPersonaModelVersion () != null ||
        		this.vnfInputs.getIsBaseVfModule () != null || this.vnfInputs.getVnfPersonaModelId () != null ||
        		this.vnfInputs.getVnfPersonaModelVersion () != null) {
        	 throw new ValidationException ("format for vnf request");        	
        }
        // Verify that the elements correspond to the version

        switch (version) {
            case Constants.SCHEMA_VERSION_V1:
                if (this.vnfInputs.getVfModuleName() != null || this.vnfInputs.getVfModuleId() != null ||
                    this.vnfInputs.getVfModuleModelName() != null || this.vnfInputs.getAsdcServiceModelVersion() != null
                    ||
                    this.vnfInputs.getBackoutOnFailure() != null || this.vnfInputs.getAicCloudRegion() != null ||
                    this.vnfInputs.getServiceInstanceId() != null) {
                    throw new ValidationException("format for v1 version of vnf request");
                }
                break;
            case Constants.SCHEMA_VERSION_V2:
                if (this.vnfInputs.getServiceType() != null || this.vnfInputs.getAicNodeClli() != null
                    || this.vnfInputs.getServiceInstanceId() != null) {
                    throw new ValidationException("format for v2 version of vnf request");
                }
                break;
            case Constants.SCHEMA_VERSION_V3:
                if (this.vnfInputs.getServiceType() != null || this.vnfInputs.getAicNodeClli() != null) {
                    throw new ValidationException("format for v3 version of vnf request");
                }
                break;
        }
        
        
        if (!InfraUtils.isActionAllowed (props, "vnf", version, action.value ())) {
        	throw new ValidationException ("action allowable for version " + version + " of vnf request");        	
        }
        
        if ((ActionType.UPDATE.equals(action) || ActionType.DELETE.equals(action)) && this.vnfInputs.getVnfId () == null) {
        	throw new ValidationException("vnf-id");        	
        }
             
        if ((ActionType.UPDATE_VF_MODULE.equals (action) || ActionType.DELETE_VF_MODULE.equals (action)) && this.vnfInputs.getVfModuleId () == null) {
            throw new ValidationException ("vf-module-id");
        }
        
        if (ActionType.CREATE.equals (action) && this.vnfInputs.getVnfName () == null) {
            throw new ValidationException ("vnf-name");
        }         
         
        if (ActionType.CREATE_VF_MODULE.equals (action)) {
        	if (this.vnfInputs.getVfModuleName () == null) {
        		throw new ValidationException ("vf-module-name");
        	}
        	if (!InfraUtils.isValidHeatName(this.vnfInputs.getVfModuleName ())) {
        		throw new ValidationException ("vf-module-name: no value meeting heat stack name syntax requirements");
        	}
        }        

        if (this.vnfInputs.getVnfType () == null) {
            throw new ValidationException ("vnf-type");
        }
        
        if ((ActionType.CREATE_VF_MODULE.equals (action) || ActionType.UPDATE_VF_MODULE.equals (action) || ActionType.DELETE_VF_MODULE.equals (action)) && this.vnfInputs.getVfModuleModelName () == null) {
            throw new ValidationException ("vf-module-model-name");
        }        
        
        if (!version.equals(Constants.SCHEMA_VERSION_V1) && this.vnfInputs.getServiceId () == null) {
        	throw new ValidationException ("service-id ");
        }
        
        if (this.vnfInputs.getServiceType () != null && this.vnfInputs.getServiceId () != null) {
        	throw new ValidationException ("service-type or service-id ");
        }
        
        if (version.equals(Constants.SCHEMA_VERSION_V1) && this.vnfInputs.getAicNodeClli () == null) {
        	throw new ValidationException ("aic-node-clli");
        }
        
        if ((version.equals(Constants.SCHEMA_VERSION_V2) || version.equals(Constants.SCHEMA_VERSION_V3)) && (this.vnfInputs.getAicCloudRegion () == null || this.vnfInputs.getAicCloudRegion ().isEmpty())) {
        	throw new ValidationException ("aic-cloud-region");
        }
        
        if (version.equals(Constants.SCHEMA_VERSION_V3) && this.vnfInputs.getServiceInstanceId () == null) {
        	throw new ValidationException ("service-instance-id");
        }

        
        if (this.vnfInputs.getTenantId () == null) {
                throw new ValidationException ("tenant-id");            
        }

        Object vp = vnfReq.getVnfParams ();

        if (vp != null) {
            msoLogger.debug ("This object is not null");

            Node node = (Node) vp;
            Document doc = node.getOwnerDocument ();
            this.vnfParams = domToStr (doc);
        }

        msoLogger.debug ("VNFParams: " + this.vnfParams);

        msoLogger.debug ("Request valid");

        // Rebuild the request string for BPEL to include request-id 
        rinfo.setRequestId (this.requestId);        
        this.vnfReq.setRequestInfo (rinfo);        
      
        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            // output pretty printed
            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal (this.vnfReq, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Exception: ", e);
        }

        this.requestXML = stringWriter.toString ();
        msoLogger.debug("REQUEST XML to BPEL: " + this.requestXML);

    }

    public void createRequestRecord (Status status) {

        Session session = null;
        try {

            session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();
            session.beginTransaction ();

            InfraActiveRequests aq = new InfraActiveRequests ();
            aq.setRequestId (requestId);
            aq.setClientRequestId(rinfo.getRequestId());

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
            aq.setRequestScope ("");

            if (vnfInputs != null) {
                if (vnfInputs.getVnfId () != null) {
                    aq.setVnfId (vnfInputs.getVnfId ());
                }
                if (vnfInputs.getVnfName () != null) {
                    aq.setVnfName (vnfInputs.getVnfName ());
                }
                if (vnfInputs.getVnfType () != null) {
                    aq.setVnfType (vnfInputs.getVnfType ());
                }
                if (vnfInputs.getServiceInstanceId () != null) {
                    aq.setServiceInstanceId (vnfInputs.getServiceInstanceId ());
                }
                if (vnfInputs.getServiceType () != null) {
                    aq.setServiceType (vnfInputs.getServiceType ());
                }
                if (vnfInputs.getServiceId () != null) {
                    aq.setAaiServiceId (vnfInputs.getServiceId ());
                }
                if (vnfInputs.getAicNodeClli () != null) {
                    aq.setAicNodeClli (vnfInputs.getAicNodeClli ());
                }
                if (vnfInputs.getAicCloudRegion () != null) {
                    aq.setAicCloudRegion (vnfInputs.getAicCloudRegion ());
                }
                if (vnfInputs.getTenantId () != null) {
                    aq.setTenantId (vnfInputs.getTenantId ());
                }
                if (vnfInputs.getProvStatus () != null) {
                    aq.setProvStatus (vnfInputs.getProvStatus ());
                }
                if (vnfInputs.getVolumeGroupName () != null) {
                    aq.setVolumeGroupName (vnfInputs.getVolumeGroupName ());
                }
                if (vnfInputs.getVolumeGroupId () != null) {
                    aq.setVolumeGroupId (vnfInputs.getVolumeGroupId ());
                }
                if (vnfInputs.getVfModuleId () != null) {
                    aq.setVfModuleId (vnfInputs.getVfModuleId ());
                }
                if (vnfInputs.getVfModuleName () != null) {
                    aq.setVfModuleName (vnfInputs.getVfModuleName ());
                }
                if (vnfInputs.getVfModuleModelName () != null) {
                    aq.setVfModuleModelName (vnfInputs.getVfModuleModelName ());
                }
                
                if (vnfInputs.getVfModuleName () != null || vnfInputs.getVfModuleId () != null) {
                	aq.setRequestScope (ModelType.vfModule.name ());
                }
                else {
                	aq.setRequestScope (ModelType.vnf.name ());
                }


            }

            aq.setStartTime (startTimeStamp);
            aq.setRequestStatus (status.toString ());
            aq.setLastModifiedBy (Constants.MODIFIED_BY_APIHANDLER);
            aq.setRequestType ("VNF");

            if (vnfParams != null) {
                msoLogger.debug ("Storing vnfParams: " + vnfParams);
                aq.setVnfParams (this.vnfParams);
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
        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception in createRequestRecord", e);
            if (session != null) {
                session.close ();
            }
            if (!status.equals (Status.FAILED)) {
                throw e;
            }
        }
    }

    public void updateFinalStatus (Status status) {
        int result = 0;
        try {
            result = (RequestsDatabase.getInstance()).updateInfraFinalStatus (requestId,
                                                              status.toString (),
                                                              this.errorMessage,
                                                              this.progress,
                                                              this.responseBody,
                                                              Constants.MODIFIED_BY_APIHANDLER);
        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DB_UPDATE_EXC, e.getMessage (), "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception in updateFinalStatus");
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

        VnfRequest vr = beansObjectFactory.createVnfRequest ();

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
                        errorMsg = String.format (Messages.errors.get (errorCode), "vnf", errorString);
                    } else {
                        errorMsg = String.format (Messages.errors.get (errorCode), errorString);
                    }
                } else if (errorCode.equals (ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS)) {
                    errorMsg = String.format (Messages.errors.get (errorCode),
                                              "vnf",
                                              inProgress.getVnfName (),
                                              inProgress.getRequestStatus (),
                                              "vnf");
                } else if (errorCode.equals (ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID)) {
                    errorMsg = String.format (Messages.errors.get (errorCode),
                                              "vnf",
                                              inProgress.getVnfId (),
                                              inProgress.getRequestStatus (),
                                              inProgress.getAction (),
                                              "vnf");
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
        
        if (this.progress == Constants.PROGRESS_REQUEST_COMPLETED) {
        	ri.setEndTime(startTimeString);
        }

        vr.setRequestInfo (ri);
        
        this.vnfInputs.setIsBaseVfModule(null);
        this.vnfInputs.setPersonaModelId(null);
        this.vnfInputs.setPersonaModelVersion(null);
        this.vnfInputs.setVnfPersonaModelId(null);
        this.vnfInputs.setVnfPersonaModelVersion(null);

        vr.setVnfInputs (this.vnfInputs);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
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
        VnfRequest vr = beansObjectFactory.createVnfRequest ();

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
        ri.setEndTime(startTimeString);
        
        vr.setRequestInfo (ri);

        vr.setVnfInputs (this.vnfInputs);

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
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

    public VnfInputs getVnfInputs () {
        return vnfInputs;
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
    
    public String getServiceType () {
    	if (this.vnfInputs.getServiceType () != null) 
    		return this.vnfInputs.getServiceType ();
    	if (this.vnfInputs.getServiceId () != null) 
    		return this.vnfInputs.getServiceId ();
    	return null;
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
    
    public void addBPMNSpecificInputs(String personaModelId, String personaModelVersion, Boolean isBaseVfModule,
    			String vnfPersonaModelId, String vnfPersonaModelVersion) {
    	vnfInputs.setPersonaModelId(personaModelId);
    	vnfInputs.setPersonaModelVersion(personaModelVersion);
    	vnfInputs.setIsBaseVfModule(isBaseVfModule);
    	vnfInputs.setVnfPersonaModelId(vnfPersonaModelId);
    	vnfInputs.setVnfPersonaModelVersion(vnfPersonaModelVersion);
    	
    	this.vnfReq.setVnfInputs(vnfInputs);
    	           
          StringWriter stringWriter = new StringWriter ();
          try {
              JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
              Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

              // output pretty printed
              jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

              jaxbMarshaller.marshal (this.vnfReq, stringWriter);

          } catch (JAXBException e) {
              msoLogger.debug ("Exception: ", e);
          }

          this.requestXML = stringWriter.toString ();
          msoLogger.debug("REQUEST XML to BPEL: " + this.requestXML);
    	
    	
    }
}
