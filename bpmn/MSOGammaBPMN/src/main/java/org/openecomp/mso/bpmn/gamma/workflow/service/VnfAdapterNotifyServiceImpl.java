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

package org.openecomp.mso.bpmn.gamma.workflow.service;

import java.util.HashMap;
import java.util.Map;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.core.Context;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.WebServiceContext;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;

import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.CreateVnfNotification;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.DeleteVnfNotification;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.MsoExceptionCategory;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.QueryVnfNotification;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.RollbackVnfNotification;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.UpdateVnfNotification;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.VnfAdapterNotify;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.VnfRollback;
import com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.VnfStatus;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This is the service class for VnfAdapterNotify
 * TODO: Add addition VnfAdapterNotify Methods for remaining VnfAdapterNotify operations.
 */

@WebService(serviceName = "vnfAdapterNotify", targetNamespace = "http://com.att.mso/vnfNotify")
public class VnfAdapterNotifyServiceImpl implements VnfAdapterNotify{

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	private final String logMarker = "[VNF-NOTIFY]";

	@Context WebServiceContext wsContext;

	private volatile ProcessEngineServices pes4junit = null;

    /**
     *
     * @param errorMessage
     * @param exception
     * @param messageId
     * @param completed
     */
    @WebMethod(operationName = "rollbackVnfNotification")
    @Oneway
    @RequestWrapper(localName = "rollbackVnfNotification", targetNamespace = "http://com.att.mso/vnfNotify", className = "org.openecomp.mso.adapters.vnf.async.client.RollbackVnfNotification")
    @Action(input = "http://com.att.mso/notify/adapterNotify/rollbackVnfNotificationRequest")
    public void rollbackVnfNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage) {



		RollbackVnfNotification rollbackVnfNotification = new RollbackVnfNotification();

		rollbackVnfNotification.setMessageId(messageId);
		rollbackVnfNotification.setCompleted(completed);
		rollbackVnfNotification.setException(exception);
		rollbackVnfNotification.setErrorMessage(errorMessage);

		ProcessEngineServices pes = getProcessEngineServices();
		RuntimeService runtimeService = pes.getRuntimeService();

		MsoLogger.setServiceName("MSO." + "vnfAdapterRollback");
		MsoLogger.setLogContext(messageId, "N/A");
		msoLogger.debug(logMarker + "Received RollbackVnfNotification" + rollbackVnfNotification.toString());

		long startTime = System.currentTimeMillis();
		try {

			/* Check to make sure the process instance is ready for correlation*/
			isReadyforCorrelation(runtimeService, messageId, "rollbackVnfNotificationCallback", "VNFRB_messageId");

			msoLogger.debug(logMarker + "*** Received MSO rollbackVnfNotification Callback ******");			
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO VnfAdapterNotifyService ");			
			msoLogger.debug(logMarker + "Rollback VNF Notification string:\n"  + rollbackVnfNotification.toString());

			System.out.println("testing ROllbackVnfNotification : " + rollbackVnfNotification.toString());

			Map<String,Object> variables = new HashMap<String,Object>();
			variables.put("VNFRB_messageId", messageId );
			variables.put("rollbackVnfNotificationCallback", rollbackVnfNotification.toString());

			/*Correlating the response with the running instance*/

			runtimeService.createMessageCorrelation("rollbackVnfNotificationCallback").setVariables(variables)
				  .processInstanceVariableEquals("VNFRB_messageId", messageId).correlate();

			msoLogger.debug(logMarker + "***** Completed processing of MSO VnfAdapterNotifyService ******");
		} catch(MismatchingMessageCorrelationException e) {
			msoLogger.debug(logMarker + "[CORM]correlation id mismatch");
			String msg =
				"VNF Adapter Notify Service received a Create VNF Notification request with RequestId '"
				+ messageId
				+ "' but that RequestId could not be correlated to any active process - ignoring the request";
			
			msoLogger.error (MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
					MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg);
			
		}		
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Rollback VNF Notification.");
		
		msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Rollback VNF Notification.", "BPMN", 
				MsoLogger.getServiceName(), "rollbackVnfNotification");
		
		return;
	 }



    /**
     *
     * @param errorMessage
     * @param vnfExists
     * @param status
     * @param exception
     * @param outputs
     * @param messageId
     * @param vnfId
     * @param completed
     */
    @WebMethod(operationName = "queryVnfNotification")
    @Oneway
    @RequestWrapper(localName = "queryVnfNotification", targetNamespace = "http://com.att.mso/vnfNotify", className = "org.openecomp.mso.adapters.vnf.async.client.QueryVnfNotification")
    @Action(input = "http://com.att.mso/notify/adapterNotify/queryVnfNotificationRequest")
    public void queryVnfNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage,
        @WebParam(name = "vnfExists", targetNamespace = "")
        Boolean vnfExists,
        @WebParam(name = "vnfId", targetNamespace = "")
        String vnfId,
        @WebParam(name = "status", targetNamespace = "")
        VnfStatus status,
        @WebParam(name = "outputs", targetNamespace = "")
        com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.QueryVnfNotification.Outputs outputs){

    	QueryVnfNotification queryVnfNotification = new QueryVnfNotification();

    	queryVnfNotification.setMessageId(messageId);
    	queryVnfNotification.setCompleted(completed);
    	queryVnfNotification.setException(exception);
    	queryVnfNotification.setErrorMessage(errorMessage);
    	queryVnfNotification.setVnfExists(vnfExists);
    	queryVnfNotification.setVnfId(vnfId);
    	queryVnfNotification.setStatus(status);
    	queryVnfNotification.setOutputs(outputs);


    	ProcessEngineServices pes = getProcessEngineServices();
		RuntimeService runtimeService = pes.getRuntimeService();

		msoLogger.setServiceName("MSO." + "vnf Adapter Query");
		msoLogger.setLogContext(messageId, "N/A");
		msoLogger.debug(logMarker + "Received QueryVnfNotification" + queryVnfNotification.toString());

		System.out.println("Received QueryVnfNotification : " + queryVnfNotification.toString());

		long startTime = System.currentTimeMillis();
		try {

			/* Check to make sure the process instance is ready for correlation*/
			isReadyforCorrelation(runtimeService, messageId, "queryVnfNotificationCallback", "VNFQ_messageId");

			msoLogger.debug(logMarker + "*** Received MSO queryVnfNotification Callback ******");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO VnfAdapterNotifyService ");
			msoLogger.debug(logMarker + "Query VNF Notification string:\n"  + queryVnfNotification.toString());

			Map<String,Object> variables = new HashMap<String,Object>();
			variables.put("VNFQ_messageId", messageId );
			variables.put("queryVnfNotificationCallback", queryVnfNotification.toString());

			/*Correlating the response with the running instance*/

			runtimeService.createMessageCorrelation("queryVnfNotificationCallback").setVariables(variables)
				  .processInstanceVariableEquals("VNFQ_messageId", messageId).correlate();

			msoLogger.debug(logMarker + "***** Completed processing of MSO VnfAdapterNotifyService ******");
		} catch(MismatchingMessageCorrelationException e) {
			msoLogger.debug(logMarker + "[CORM]correlation id mismatch");
			String msg =
				"VNF Adapter Notify Service received a Query VNF Notification request with RequestId '"
				+ messageId
				+ "' but that RequestId could not be correlated to any active process - ignoring the request";
			
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
					MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);
		}

		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Query VNF Notification.");
		
		return;
	 }




    /**
     *
     * @param errorMessage
     * @param exception
     * @param rollback
     * @param outputs
     * @param messageId
     * @param vnfId
     * @param completed
     */
	@WebMethod(operationName = "createVnfNotification")
    @Oneway
    @RequestWrapper(localName = "createVnfNotification", targetNamespace = "http://com.att.mso/vnfNotify", className = "org.openecomp.mso.adapters.vnf.async.client.CreateVnfNotification")
    @Action(input = "http://com.att.mso/notify/adapterNotify/createVnfNotificationRequest")
	public void createVnfNotification(
			@WebParam(name = "messageId", targetNamespace = "")
	        String messageId,
	        @WebParam(name = "completed", targetNamespace = "")
	        boolean completed,
	        @WebParam(name = "exception", targetNamespace = "")
	        MsoExceptionCategory exception,
	        @WebParam(name = "errorMessage", targetNamespace = "")
	        String errorMessage,
	        @WebParam(name = "vnfId", targetNamespace = "")
	        String vnfId,
	        @WebParam(name = "outputs", targetNamespace = "")
	        com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.CreateVnfNotification.Outputs outputs,
	        @WebParam(name = "rollback", targetNamespace = "")
	        VnfRollback rollback){

		CreateVnfNotification createVnfNotification = new CreateVnfNotification();

		createVnfNotification.setMessageId(messageId);
		createVnfNotification.setCompleted(completed);
		createVnfNotification.setException(exception);
		createVnfNotification.setErrorMessage(errorMessage);
		createVnfNotification.setVnfId(vnfId);
		createVnfNotification.setOutputs(outputs);
		createVnfNotification.setRollback(rollback);

		ProcessEngineServices pes = getProcessEngineServices();
		RuntimeService runtimeService = pes.getRuntimeService();

		msoLogger.setServiceName("MSO." + "vnf Adapter Create");
		msoLogger.setLogContext(messageId, "N/A");
		msoLogger.debug(logMarker + "Received CreateVnfNotification - " + createVnfNotification.toString());

		long startTime = System.currentTimeMillis();
		try {

			/* Check to make sure the process instance is ready for correlation*/
			isReadyforCorrelation(runtimeService, messageId, "createVnfNotificationCallback", "VNFC_messageId");

			msoLogger.debug(logMarker + "*** Received MSO createVnfNotification Callback ******");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO VnfAdapterNotifyService ");
			
			msoLogger.debug(logMarker + "Create VNF Notification string:\n"  + createVnfNotification.toString());

			Map<String,Object> variables = new HashMap<String,Object>();
			variables.put("VNFC_messageId", messageId );
			variables.put("createVnfNotificationCallback", createVnfNotification.toString());

			/*Correlating the response with the running instance*/

			runtimeService.createMessageCorrelation("createVnfNotificationCallback").setVariables(variables)
				  .processInstanceVariableEquals("VNFC_messageId", messageId).correlate();

			msoLogger.debug(logMarker + "***** Completed processing of MSO VnfAdapterNotifyService ******");
		} catch(MismatchingMessageCorrelationException e) {
			msoLogger.debug(logMarker + "[CORM]correlation id mismatch");
			String msg =
				"VNF Adapter Notify Service received a Create VNF Notification request with RequestId '"
				+ messageId
				+ "' but that RequestId could not be correlated to any active process - ignoring the request";
			
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
					MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);
			
		}
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Query VNF Notification.");
		
		msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Query VNF Notification.", "BPMN", 
				MsoLogger.getServiceName(), "createVnfNotification");
		
		return;
	 }

    /**
     *
     * @param errorMessage
     * @param exception
     * @param rollback
     * @param outputs
     * @param messageId
     * @param completed
     */
	@WebMethod(operationName = "updateVnfNotification")
    @Oneway
    @RequestWrapper(localName = "updateVnfNotification", targetNamespace = "http://com.att.mso/vnfNotify", className = "org.openecomp.mso.adapters.vnf.async.client.UpdateVnfNotification")
    @Action(input = "http://com.att.mso/notify/adapterNotify/updateVnfNotificationRequest")
    public void updateVnfNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage,
        @WebParam(name = "outputs", targetNamespace = "")
        com.att.domain2.workflow.vnf.async.adapter.callback.wsdl.v1.UpdateVnfNotification.Outputs outputs,
        @WebParam(name = "rollback", targetNamespace = "")
        VnfRollback rollback){

    	UpdateVnfNotification updateVnfNotification = new UpdateVnfNotification();

    	updateVnfNotification.setMessageId(messageId);
    	updateVnfNotification.setCompleted(completed);
    	updateVnfNotification.setException(exception);
    	updateVnfNotification.setErrorMessage(errorMessage);
    	updateVnfNotification.setOutputs(outputs);
    	updateVnfNotification.setRollback(rollback);

		ProcessEngineServices pes = getProcessEngineServices();
		RuntimeService runtimeService = pes.getRuntimeService();

		msoLogger.setServiceName("MSO." + "vnf Adapter Update");
		msoLogger.setLogContext(messageId, "N/A");
		msoLogger.debug(logMarker + "Received UpdateVnfNotification - " + updateVnfNotification.toString());

		long startTime = System.currentTimeMillis();
		try {

			// Check to make sure the process instance is ready for correlation
			isReadyforCorrelation(runtimeService, messageId, "updateVnfNotificationCallback", "VNFU_messageId");

			msoLogger.debug(logMarker + "*** Received MSO updateVnfNotification Callback ******");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO VnfAdapterNotifyService ");
			
			msoLogger.debug(logMarker + "Update VNF Notification string:\n"  + updateVnfNotification.toString());

			Map<String,Object> variables = new HashMap<String,Object>();
			variables.put("VNFU_messageId", messageId );
			variables.put("updateVnfNotificationCallback", updateVnfNotification.toString());

			//Correlating the response with the running instance
			runtimeService.createMessageCorrelation("updateVnfNotificationCallback").setVariables(variables)
				  .processInstanceVariableEquals("VNFU_messageId", messageId).correlate();

			msoLogger.debug(logMarker + "***** Completed processing of MSO VnfAdapterNotifyService ******");
			
		} catch(MismatchingMessageCorrelationException e) {
			msoLogger.debug(logMarker + "[CORM]correlation id mismatch");
			String msg =
				"VNF Adapter Notify Service received a Update VNF Notification request with RequestId '"
				+ messageId
				+ "' but that RequestId could not be correlated to any active process - ignoring the request";
			
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
					MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);
			
		}
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Update VNF Notification.");
		
		msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO Vnf Adapter Notify for Update VNF Notification.", "BPMN", 
				MsoLogger.getServiceName(), "updateVnfNotification");
		
		return;
	 }

    /**
     *
     * @param errorMessage
     * @param exception
     * @param messageId
     * @param completed
     */

    //@WebService(serviceName="VNFAdapterDeleteCallbackV1", targetNamespace="http://com.att.mso/vnfNotify")
    @WebMethod(operationName = "deleteVnfNotification")
    @Oneway
    @RequestWrapper(localName = "deleteVnfNotification", targetNamespace = "http://com.att.mso/vnfNotify", className = "org.openecomp.mso.adapters.vnf.async.client.DeleteVnfNotification")
    @Action(input = "http://com.att.mso/notify/adapterNotify/deleteVnfNotificationRequest")
    public void deleteVnfNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage) {

		//Callback URL to use http://localhost:8080/mso/services/VNFAdapterDeleteCallbackV1

    	//DeleteVnfNotification Class
    	DeleteVnfNotification deleteVnfNotification = new DeleteVnfNotification();
    	deleteVnfNotification.setMessageId(messageId);
    	deleteVnfNotification.setCompleted(completed);
    	deleteVnfNotification.setException(exception);
    	deleteVnfNotification.setErrorMessage(errorMessage);

		ProcessEngineServices pes = getProcessEngineServices();
		RuntimeService runtimeService = pes.getRuntimeService();

		MsoLogger.setServiceName("MSO." + "vnfAdapterDelete");
		MsoLogger.setLogContext(messageId, "N/A");
		msoLogger.debug(logMarker + "Received DeleteVnfNotification callback: " + deleteVnfNotification.toString());

		long startTime = System.currentTimeMillis();
		try {

			/* Check to make sure the process instance is ready for correlation*/
			//isReadyforCorrelation(runtimeService, messageId, "deleteVnfACallback", "VNFDEL_uuid");

			msoLogger.debug(logMarker + " *** Received MSO deleteVnfACallback ******");			
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO deleteVnfACallback ");		
			msoLogger.debug(logMarker + " Callback response string:\n"  + deleteVnfNotification.toString());

			Map<String,Object> variables = new HashMap<String,Object>();
			variables.put("VNFDEL_uuid", messageId);
			variables.put("deleteVnfACallback", deleteVnfNotification.toString());

			/*Correlating the response with the running instance*/

			runtimeService.createMessageCorrelation("deleteVnfACallback")
				  .setVariables(variables)
				  .processInstanceVariableEquals("VNFDEL_uuid", messageId).correlate();

			msoLogger.debug(logMarker + "***** Completed processing of MSO deleteVnfACallback ******");

		} catch(MismatchingMessageCorrelationException e) {

			msoLogger.debug(logMarker + " [CORM]correlation id mismatch");
			// Couldn't correlate requestId to any active flow
			//MsoLogger logger = MsoLogger.getMsoLogger("SDNCAdapterCallbackService");

			String msg =
				"Vnf Adapter Callback Service received a Vnf Adapter Callback with messageId '"
				+ messageId
				+ "' but that messageId could not be correlated to any active process - ignoring the Request";
			
			msoLogger.error(MessageEnum.BPMN_SDNC_CALLBACK_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
					MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);

		}
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO VNFAdapterDeleteCallbackV1.");
		
		msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + "Completed the execution of MSO VNFAdapterDeleteCallbackV1.", "BPMN", 
				MsoLogger.getServiceName(), "deleteVnfNotification");
		
		return;
	}

	private void isReadyforCorrelation(RuntimeService runtimeService, String requestId, String responseName, String correlationValue) {

		long waitingInstances = runtimeService.createExecutionQuery().messageEventSubscriptionName(responseName).processVariableValueEquals(correlationValue, requestId).count();
		int retries = 50;
		while (waitingInstances==0 && retries > 0) {
		  try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
            // should I add new exception Message to MessageEnum???
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, logMarker, e);
			
		} // you can still play with the numbers
		  waitingInstances = runtimeService.createExecutionQuery() //
			  .messageEventSubscriptionName(responseName)
			  .processVariableValueEquals(correlationValue, requestId).count();
		  retries--;
		}
	}


	private ProcessEngineServices getProcessEngineServices() {
		if (pes4junit == null) {
			return BpmPlatform.getDefaultProcessEngine();
		} else {
			return pes4junit;
		}
	}

	@WebMethod(exclude=true)
	public void setProcessEngineServices4junit(ProcessEngineServices pes) {
		pes4junit = pes;
	}
}
