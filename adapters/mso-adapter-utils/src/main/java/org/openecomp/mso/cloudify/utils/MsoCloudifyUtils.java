
package org.openecomp.mso.cloudify.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.cloud.CloudifyManager;
import org.openecomp.mso.cloudify.base.client.CloudifyBaseException;
import org.openecomp.mso.cloudify.base.client.CloudifyClientTokenProvider;
import org.openecomp.mso.cloudify.base.client.CloudifyConnectException;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;
import org.openecomp.mso.cloudify.base.client.CloudifyResponseException;
import org.openecomp.mso.cloudify.beans.DeploymentInfo;
import org.openecomp.mso.cloudify.beans.DeploymentStatus;
import org.openecomp.mso.cloudify.exceptions.MsoCloudifyException;
import org.openecomp.mso.cloudify.exceptions.MsoCloudifyManagerNotFound;
import org.openecomp.mso.cloudify.exceptions.MsoDeploymentAlreadyExists;
import org.openecomp.mso.cloudify.v3.client.BlueprintsResource.GetBlueprint;
import org.openecomp.mso.cloudify.v3.client.BlueprintsResource.UploadBlueprint;
import org.openecomp.mso.cloudify.v3.client.Cloudify;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.CreateDeployment;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.DeleteDeployment;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.GetDeployment;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.GetDeploymentOutputs;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.CancelExecution;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.GetExecution;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.ListExecutions;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.StartExecution;
import org.openecomp.mso.cloudify.v3.model.Blueprint;
import org.openecomp.mso.cloudify.v3.model.CancelExecutionParams;
import org.openecomp.mso.cloudify.v3.model.CloudifyError;
import org.openecomp.mso.cloudify.v3.model.CreateDeploymentParams;
import org.openecomp.mso.cloudify.v3.model.Deployment;
import org.openecomp.mso.cloudify.v3.model.DeploymentOutputs;
import org.openecomp.mso.cloudify.v3.model.Execution;
import org.openecomp.mso.cloudify.v3.model.Executions;
import org.openecomp.mso.cloudify.v3.model.OpenstackConfig;
import org.openecomp.mso.cloudify.v3.model.StartExecutionParams;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import org.openecomp.mso.openstack.utils.MsoCommonUtils;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MsoCloudifyUtils extends MsoCommonUtils {

	private MsoPropertiesFactory msoPropertiesFactory;
	private CloudConfigFactory cloudConfigFactory;
	
    private static final String CLOUDIFY_ERROR = "CloudifyError";

    private static final String CREATE_DEPLOYMENT = "CreateDeployment";
    private static final String DELETE_DEPLOYMENT = "DeleteDeployment";

    // Fetch cloud configuration each time (may be cached in CloudConfig class)
    protected CloudConfig cloudConfig;

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

    protected MsoJavaProperties msoProps = null;

    // Properties names and variables (with default values)
    protected String createPollIntervalProp = "ecomp.mso.adapters.heat.create.pollInterval";
    private String deletePollIntervalProp = "ecomp.mso.adapters.heat.delete.pollInterval";

    protected int createPollIntervalDefault = 15;
    private int deletePollIntervalDefault = 15;
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * This constructor MUST be used ONLY in the JUNIT tests, not for real code.
     * The MsoPropertiesFactory will be added by EJB injection.
     *
     * @param msoPropID ID of the mso pro config as defined in web.xml
     * @param msoPropFactory The mso properties factory instanciated by EJB injection
     * @param cloudConfFactory the Cloud Config instantiated by EJB injection
     */
    public MsoCloudifyUtils (String msoPropID, MsoPropertiesFactory msoPropFactory, CloudConfigFactory cloudConfFactory) {
    	msoPropertiesFactory = msoPropFactory;
    	cloudConfigFactory = cloudConfFactory;
    	// Dynamically get properties each time (in case reloaded).

    	try {
			msoProps = msoPropertiesFactory.getMsoJavaProperties (msoPropID);
		} catch (MsoPropertiesException e) {
			LOGGER.error (MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. Mso Properties ID not found in cache: " + msoPropID, "", "", MsoLogger.ErrorCode.DataError, "Exception - Mso Properties ID not found in cache", e);
		}
        cloudConfig = cloudConfigFactory.getCloudConfig ();
        LOGGER.debug("MsoCloudifyUtils:" + msoPropID);
        
    }


    /**
     * Create a new Deployment from a specified blueprint, and install it in the specified
     * cloud location and tenant. The blueprint identifier and parameter map are passed in
     * as arguments, along with the cloud access credentials.  The blueprint should have been
     * previously uploaded to Cloudify.
     * 
     * It is expected that parameters have been validated and contain at minimum the required
     * parameters for the given template with no extra (undefined) parameters..
     *
     * The deployment ID supplied by the caller must be unique in the scope of the Cloudify
     * tenant (not the Openstack tenant).  However, it should also be globally unique, as it
     * will be the identifier for the resource going forward in Inventory. This latter is
     * managed by the higher levels invoking this function.
     *
     * This function executes the "install" workflow on the newly created workflow.  Cloudify
     * will be polled for completion unless the client requests otherwise.
     *
     * An error will be thrown if the requested Deployment already exists in the specified
     * Cloudify instance.
     *
     * @param cloudSiteId The cloud (may be a region) in which to create the stack.
     * @param tenantId The Openstack ID of the tenant in which to create the Stack
     * @param deploymentId The identifier (name) of the deployment to create
     * @param blueprintId The blueprint from which to create the deployment.
     * @param inputs A map of key/value inputs
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @param timeoutMinutes Timeout after which the "install" will be cancelled
     * @param environment An optional yaml-format string to specify environmental parameters
     * @param backout Flag to delete deployment on install Failure - defaulted to True
     * @return A DeploymentInfo object
     * @throws MsoCloudifyException Thrown if the Cloudify API call returns an exception.
     * @throws MsoIOException Thrown on Cloudify connection errors.
     */

    public DeploymentInfo createAndInstallDeployment (String cloudSiteId,
                                  String tenantId,
                                  String deploymentId,
                                  String blueprintId,
                                  Map <String, ? extends Object> inputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes,
                                  boolean backout) throws MsoException
    {
        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound (cloudSiteId);
        }
        
        Cloudify cloudify = getCloudifyClient (cloudSite.get());

        // Create the Cloudify OpenstackConfig with the credentials
        OpenstackConfig openstackConfig = getOpenstackConfig (cloudSite.get(), tenantId);
        
        LOGGER.debug ("Ready to Create Deployment (" + deploymentId + ") with input params: " + inputs);

        // Build up the inputs, including:
        // - from provided "environment" file
        // - passed in by caller
        // - special input for Openstack Credentials
        Map<String,Object> expandedInputs = new HashMap<String,Object> (inputs);
        expandedInputs.put("openstack_config", openstackConfig);
         
        // Build up the parameters to create a new deployment
    	CreateDeploymentParams deploymentParams = new CreateDeploymentParams();
    	deploymentParams.setBlueprintId(blueprintId);
    	deploymentParams.setInputs((Map<String,Object>)expandedInputs);

    	Deployment deployment = null;
    	try {
    		CreateDeployment createDeploymentRequest = cloudify.deployments().create(deploymentId, deploymentParams);
    		LOGGER.debug (createDeploymentRequest.toString());
    		
    		deployment = executeAndRecordCloudifyRequest (createDeploymentRequest);
    	}
    	catch (CloudifyResponseException e) {
            // Since this came on the 'Create Deployment' command, nothing was changed
            // in the cloud. Return the error as an exception.
            if (e.getStatus () == 409) {
                // Deployment already exists. Return a specific error for this case
                MsoException me = new MsoDeploymentAlreadyExists (deploymentId, cloudSiteId);
                me.addContext (CREATE_DEPLOYMENT);
                throw me;
            } else {
                // Convert the CloudifyResponseException to an MsoException
            	LOGGER.debug("ERROR STATUS = " + e.getStatus() + ",\n" + e.getMessage() + "\n" + e.getLocalizedMessage());
                MsoException me = cloudifyExceptionToMsoException (e, CREATE_DEPLOYMENT);
                me.setCategory (MsoExceptionCategory.OPENSTACK);
                throw me;
            }
        } catch (CloudifyConnectException e) {
            // Error connecting to Cloudify instance. Convert to an MsoException
            MsoException me = cloudifyExceptionToMsoException (e, CREATE_DEPLOYMENT);
            throw me;
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, CREATE_DEPLOYMENT);
        }

    	/*
    	 * It can take some time for Cloudify to be ready to execute a workflow
    	 * on the deployment.  Sleep 10 seconds.
    	 */
    	try {
    		Thread.sleep(10000);
    	} catch (InterruptedException e) {}
    	
    	/*
    	 * Next execute the "install" workflow.
    	 * Note - this assumes there are no additional parameters required for the workflow.
    	 */
        int createPollInterval = msoProps.getIntProperty (createPollIntervalProp, createPollIntervalDefault);
        int pollTimeout = (timeoutMinutes * 60) + createPollInterval;
        
        Execution installWorkflow = null;
        
        try {
        	installWorkflow = executeWorkflow (cloudify, deploymentId, "install", null, pollForCompletion, pollTimeout, createPollInterval);

        	if (installWorkflow.getStatus().equals("terminated")) {
	        	//  Success!
	        	//  Create and return a DeploymentInfo structure.  Include the Runtime outputs
                DeploymentOutputs outputs = getDeploymentOutputs (cloudify, deploymentId);
	        	DeploymentInfo deploymentInfo = new DeploymentInfo (deployment, outputs, installWorkflow);
	        	return deploymentInfo;
	        }
        	else {
        		// The workflow completed with errors.  Must try to back it out.
            	if (!backout)
            	{
            		LOGGER.warn(MessageEnum.RA_CREATE_STACK_ERR, "Deployment installation failed, backout deletion suppressed", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in Deployment Installation, backout suppressed");
            	}	
            	else {
    	        	// Poll on delete if we rollback - use same values for now
    	            int deletePollInterval = createPollInterval;
    	            int deletePollTimeout = pollTimeout;
    	
    	            try {
    	            	// Run the uninstall to undo the install
    	            	Execution uninstallWorkflow = executeWorkflow (cloudify, deploymentId, "uninstall", null, pollForCompletion, deletePollTimeout, deletePollInterval);
    	        	
    	            	if (uninstallWorkflow.getStatus().equals("terminated"))
    	            	{
    	            		//  The uninstall completed.  Delete the deployment itself
    	            		DeleteDeployment deleteRequest = cloudify.deployments().deleteByName(deploymentId);
    	            		executeAndRecordCloudifyRequest (deleteRequest);
    	            	}
    	            	else {
    	            		// Didn't uninstall successfully.  Log this error
        					LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Deployment: Cloudify error rolling back deployment install: " + installWorkflow.getError(), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack: Cloudify error rolling back deployment installation");
    	            	}
    	            }
    	            catch (Exception e) {
    	            	// Catch-all for backout errors trying to uninstall/delete
    	            	// Log this error, and return the original exception
    					LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Stack: Nested exception rolling back deployment install: " + e, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack: Nested exception rolling back deployment installation");
    	            }
            	}
    	            
    	        MsoCloudifyException me = new MsoCloudifyException (0, "Workflow Execution Failed", installWorkflow.getError());
                me.addContext (CREATE_DEPLOYMENT);
                alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
                throw me;
        	}
        }
        catch (MsoException me) {
        	// Install failed.  Unless requested otherwise, back out the deployment 
        	
        	if (!backout)
        	{
        		LOGGER.warn(MessageEnum.RA_CREATE_STACK_ERR, "Deployment installation failed, backout deletion suppressed", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in Deployment Installation, backout suppressed");
        	}	
        	else {
	        	// Poll on delete if we rollback - use same values for now
	            int deletePollInterval = createPollInterval;
	            int deletePollTimeout = pollTimeout;
	
	            try {
	            	// Run the uninstall to undo the install.
	            	// Always try to run it, as it should be idempotent
	            	executeWorkflow (cloudify, deploymentId, "uninstall", null, pollForCompletion, deletePollTimeout, deletePollInterval);
	        	
	            	// Delete the deployment itself
	            	DeleteDeployment deleteRequest = cloudify.deployments().deleteByName(deploymentId);
            		executeAndRecordCloudifyRequest (deleteRequest);
	            }
	            catch (Exception e) {
	            	// Catch-all for backout errors trying to uninstall/delete
	            	// Log this error, and return the original exception
					LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Stack: Nested exception rolling back deployment install: " + e, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack: Nested exception rolling back deployment installation");
	            	
	            }
        	}

            // Propagate the original exception from Stack Query.
            me.addContext (CREATE_DEPLOYMENT);
            alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
            throw me;
        }
    }


    /*
     * Get the runtime Outputs of a deployment.
     * Return the Map of tag/value outputs.
     */
    private DeploymentOutputs getDeploymentOutputs (Cloudify cloudify, String deploymentId)
        throws MsoException
    {
    	// Build and send the Cloudify request
		DeploymentOutputs deploymentOutputs = null;
    	try {
    		GetDeploymentOutputs queryDeploymentOutputs = cloudify.deployments().outputsById(deploymentId);
    		LOGGER.debug (queryDeploymentOutputs.toString());
    		
    		deploymentOutputs = executeAndRecordCloudifyRequest(queryDeploymentOutputs, msoProps);
    	}
    	catch (CloudifyConnectException ce) {
    		// Couldn't connect to Cloudify
    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "QueryDeploymentOutputs: Cloudify connection failure: " + ce, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "QueryDeploymentOutputs: Cloudify connection failure");
    		throw new MsoIOException (ce.getMessage(), ce);
    	}
    	catch (CloudifyResponseException re) {
            if (re.getStatus () == 404) {
            	// No Outputs
            	return null;
            }
            throw new MsoCloudifyException (re.getStatus(), re.getMessage(), re.getLocalizedMessage(), re);
    	}
    	catch (Exception e) {
    		// Catch-all
    		throw new MsoAdapterException (e.getMessage(), e);
    	}
    	
    	return deploymentOutputs;
    }
    
    /*
     * Execute a workflow on a deployment.  Handle polling for completion with timeout.
     * Return the final Execution object with status.
     * Throw an exception on Errors.
     * Question - how does the client know whether rollback needs to be done?
     */
    private Execution executeWorkflow (Cloudify cloudify, String deploymentId, String workflowId, Map<String,Object> workflowParams, boolean pollForCompletion, int timeout, int pollInterval)
    	throws MsoCloudifyException
    {
    	LOGGER.debug("Executing '" + workflowId + "' workflow on deployment '" + deploymentId + "'");

		StartExecutionParams executeParams = new StartExecutionParams();
		executeParams.setWorkflowId(workflowId);
		executeParams.setDeploymentId(deploymentId);
		executeParams.setParameters(workflowParams);
		
		Execution execution = null;
		String executionId = null;
		String command = "start";
		Exception savedException = null;
		
		try {
			StartExecution executionRequest = cloudify.executions().start(executeParams);
			LOGGER.debug (executionRequest.toString());
    		execution = executeAndRecordCloudifyRequest (executionRequest);
			executionId = execution.getId();

			if (!pollForCompletion) {
				// Client did not request polling, so just return the Execution object
				return execution;
			}

			// Enter polling loop
			boolean timedOut = false;
			int pollTimeout = timeout;
			
			String status = execution.getStatus();
			
			// Create a reusable cloudify query request
			GetExecution queryExecution = cloudify.executions().byId(executionId);
			command = "query";
			
			while (!timedOut && !(status.equals("terminated") || status.equals("failed") || status.equals("cancelled")))
			{
				// workflow is still running; check for timeout
				if (pollTimeout <= 0) {
					LOGGER.debug ("workflow " + execution.getWorkflowId() + " timed out on deployment " + execution.getDeploymentId());                    
					timedOut = true;
					continue;
				}
				
				try {
					Thread.sleep (pollInterval * 1000L);
				} catch (InterruptedException e) {}

				pollTimeout -= pollInterval;
				LOGGER.debug("pollTimeout remaining: " + pollTimeout);
				
				execution = queryExecution.execute();
				status = execution.getStatus();
			}

			// Broke the loop.  Check again for a terminal state
			if (status.equals("terminated")){
				// Success!
	    		LOGGER.debug ("Workflow '" + workflowId + "' completed successfully on deployment '" + deploymentId + "'");
				return execution;
			}
			else if (status.equals("failed")){
				// Workflow failed.  Log it and return the execution object (don't throw exception here)
	    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Cloudify workflow failure: " + execution.getError(), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow: Failed: " + execution.getError());
	    		return execution;
			}
			else if (status.equals("cancelled")){
				// Workflow was cancelled, leaving the deployment in an indeterminate state.  Log it and return the execution object (don't throw exception here)
	    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Cloudify workflow cancelled.  Deployment is in an indeterminate state", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow cancelled: " + workflowId);
	    		return execution;
			}
			else {
				// Can only get here after a timeout
	    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Cloudify workflow timeout", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow: Timed Out");
			}
		}
		catch (CloudifyConnectException ce) {
    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Execute Workflow (" + command + "): Cloudify connection failure: " + ce, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow (" + command + "): Cloudify connection failure");
    		savedException = ce;
		}
		catch (CloudifyResponseException re) {
    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Execute Workflow (" + command + "): Cloudify response error: " + re, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow (" + command + "): Cloudify error" + re.getMessage());
    		savedException = re;
		}
		catch (RuntimeException e) {
			// Catch-all
    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Execute Workflow (" + command + "): Unexpected error: " + e, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow (" + command + "): Internal error" + e.getMessage());
    		savedException = e;
		}
		
		//  Get to this point ONLY on an error or timeout
		//  The cloudify execution is still running (we've not received a terminal status),
		//  so try to Cancel it.
		CancelExecutionParams cancelParams = new CancelExecutionParams();
		cancelParams.setAction("cancel");
		// TODO:  Use force_cancel?
		
		Execution cancelExecution = null;
		
		try {
			CancelExecution cancelRequest = cloudify.executions().cancel(executionId, cancelParams);
			LOGGER.debug (cancelRequest.toString());
			cancelExecution = cancelRequest.execute();

			// Enter polling loop
			boolean timedOut = false;
			int cancelTimeout = timeout;	// TODO: For now, just use same timeout
			
			String status = cancelExecution.getStatus();
			
			// Poll for completion.  Create a reusable cloudify query request
			GetExecution queryExecution = cloudify.executions().byId(executionId);
			
			while (!timedOut && !status.equals("cancelled"))
			{
				// workflow is still running; check for timeout
				if (cancelTimeout <= 0) {
					LOGGER.debug ("Cancel timeout for workflow " + workflowId + " on deployment " + deploymentId);                    
					timedOut = true;
					continue;
				}
				
				try {
					Thread.sleep (pollInterval * 1000L);
				} catch (InterruptedException e) {}

				cancelTimeout -= pollInterval;
				LOGGER.debug("pollTimeout remaining: " + cancelTimeout);
				
				execution = queryExecution.execute();
				status = execution.getStatus();
			}

			// Broke the loop.  Check again for a terminal state
			if (status.equals("cancelled")){
				// Finished cancelling.  Return the original exception
				LOGGER.debug ("Cancel workflow " + workflowId + " completed on deployment " + deploymentId);                    
				throw new MsoCloudifyException (-1, "", "", savedException);
			}
			else {
				// Can only get here after a timeout
				LOGGER.debug ("Cancel workflow " + workflowId + " timeout out on deployment " + deploymentId);                    
				MsoCloudifyException exception = new MsoCloudifyException (-1, "", "", savedException);
				exception.setPendingWorkflow(true);
				throw exception;
			}
		}
		catch (Exception e) {
			// Catch-all.  Log the message and throw the original exception
//    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Execute Workflow (" + command + "): Unexpected error: " + e, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Execute Workflow (" + command + "): Internal error" + e.getMessage());
			LOGGER.debug ("Cancel workflow " + workflowId + " failed for deployment " + deploymentId + ": " + e.getMessage());                    
			MsoCloudifyException exception = new MsoCloudifyException (-1, "", "", savedException);
			exception.setPendingWorkflow(true);
			throw exception;
		}
    }	
    

    
    /**
     * Query for a Cloudify Deployment (by Name). This call will always return a
     * DeploymentInfo object. If the deployment does not exist, an "empty" DeploymentInfo will be
     * returned - containing only the deployment ID and a special status of NOTFOUND.
     *
     * @param tenantId The Openstack ID of the tenant in which to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query
     * @param stackName The name of the stack to query (may be simple or canonical)
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */
    public DeploymentInfo queryDeployment (String cloudSiteId, String tenantId, String deploymentId)
    	throws MsoException
    {
        LOGGER.debug ("Query Cloudify Deployment: " + deploymentId + " in tenant " + tenantId);

        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound (cloudSiteId);
        }
        
        Cloudify cloudify = getCloudifyClient (cloudSite.get());
        
    	// Build and send the Cloudify request
		Deployment deployment = null;
		DeploymentOutputs outputs = null;
    	try {
    		GetDeployment queryDeployment = cloudify.deployments().byId(deploymentId);
    		LOGGER.debug (queryDeployment.toString());
    		
//    		deployment = queryDeployment.execute();
    		deployment = executeAndRecordCloudifyRequest(queryDeployment, msoProps);

            outputs = getDeploymentOutputs (cloudify, deploymentId);

    		//  Next look for the latest execution
    		ListExecutions listExecutions = cloudify.executions().listFiltered ("deployment_id=" + deploymentId, "-created_at");
    		Executions executions = listExecutions.execute();
    		
    		//  If no executions, does this give NOT_FOUND or empty set?
    		if (executions.getItems().isEmpty()) {
    			return new DeploymentInfo (deployment);
    		}
    		else {
    			return new DeploymentInfo (deployment, outputs, executions.getItems().get(0));
    		}
    	}
    	catch (CloudifyConnectException ce) {
    		// Couldn't connect to Cloudify
    		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "QueryDeployment: Cloudify connection failure: " + ce, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "QueryDeployment: Cloudify connection failure");
    		throw new MsoIOException (ce.getMessage(), ce);
    	}
    	catch (CloudifyResponseException re) {
            if (re.getStatus () == 404) {
            	// Got a NOT FOUND error.  React differently based on deployment vs. execution
            	if (deployment != null) {
            		// Got NOT_FOUND on the executions.  Assume this is a valid "empty" set
            		return new DeploymentInfo (deployment, outputs, null);
            	} else {
            		// Deployment not found.  Default status of a DeploymentInfo object is NOTFOUND
            		return new DeploymentInfo (deploymentId);
            	}
            }
            throw new MsoCloudifyException (re.getStatus(), re.getMessage(), re.getLocalizedMessage(), re);
    	}
    	catch (Exception e) {
    		// Catch-all
    		throw new MsoAdapterException (e.getMessage(), e);
    	}
    }
    	

    /**
     * Delete a Cloudify deployment (by ID). If the deployment is not found, it will be
     * considered a successful deletion. The return value is a DeploymentInfo object which
     * contains the last deployment status.
     *
     * There is no rollback from a successful deletion. A deletion failure will
     * also result in an undefined deployment state - the components may or may not have been
     * all or partially deleted, so the resulting deployment must be considered invalid.
     *
     * @param tenantId The Openstack ID of the tenant in which to perform the delete
     * @param cloudSiteId The cloud identifier (may be a region) from which to delete the stack.
     * @param stackName The name/id of the stack to delete. May be simple or canonical
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     * @throws MsoCloudSiteNotFound
     */
    public DeploymentInfo uninstallAndDeleteDeployment (String cloudSiteId,
                                  String tenantId,
                                  String deploymentId,
                                  int timeoutMinutes) throws MsoException
    {
        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound (cloudSiteId);
        }
        
        Cloudify cloudify = getCloudifyClient (cloudSite.get());

        LOGGER.debug ("Ready to Uninstall/Delete Deployment (" + deploymentId + ")");

        // Query first to save the trouble if deployment not found
    	Deployment deployment = null;
    	try {
    		GetDeployment queryDeploymentRequest = cloudify.deployments().byId(deploymentId);
    		LOGGER.debug (queryDeploymentRequest.toString());
    		
    		deployment = executeAndRecordCloudifyRequest (queryDeploymentRequest);
    	}
    	catch (CloudifyResponseException e) {
            // Since this came on the 'Create Deployment' command, nothing was changed
            // in the cloud. Return the error as an exception.
            if (e.getStatus () == 404) {
                // Deployment doesn't exist.  Return a "NOTFOUND" DeploymentInfo object
            	// TODO:  Should return NULL?
            	LOGGER.debug("Deployment requested for deletion does not exist: " + deploymentId);
            	return new DeploymentInfo (deploymentId, DeploymentStatus.NOTFOUND);
           } else {
                // Convert the CloudifyResponseException to an MsoOpenstackException
            	LOGGER.debug("ERROR STATUS = " + e.getStatus() + ",\n" + e.getMessage() + "\n" + e.getLocalizedMessage());
            	MsoException me = cloudifyExceptionToMsoException (e, DELETE_DEPLOYMENT);
                me.setCategory (MsoExceptionCategory.INTERNAL);
                throw me;
            }
        } catch (CloudifyConnectException e) {
            // Error connecting to Cloudify instance. Convert to an MsoException
        	MsoException me = cloudifyExceptionToMsoException (e, DELETE_DEPLOYMENT);
            throw me;
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, DELETE_DEPLOYMENT);
        }

    	/*
    	 *  Query the outputs before deleting so they can be returned as well
    	 */
    	DeploymentOutputs outputs = getDeploymentOutputs (cloudify, deploymentId);
    	
    	/*
    	 * Next execute the "uninstall" workflow.
    	 * Note - this assumes there are no additional parameters required for the workflow.
    	 */
    	// TODO: No deletePollInterval that I'm aware of.  Use the create interval
        int deletePollInterval = msoProps.getIntProperty (deletePollIntervalProp, deletePollIntervalDefault);
        int pollTimeout = (timeoutMinutes * 60) + deletePollInterval;
        
        Execution uninstallWorkflow = null;
        
        try {
        	uninstallWorkflow = executeWorkflow (cloudify, deploymentId, "uninstall", null, true, pollTimeout, deletePollInterval);

        	if (uninstallWorkflow.getStatus().equals("terminated")) {
	        	//  Successful uninstall.
        		LOGGER.debug("Uninstall successful for deployment " + deploymentId);
	        }
        	else {
        		// The uninstall workflow completed with an error.  Must fail the request, but will
        		// leave the deployment in an indeterminate state, as cloud resources may still exist.
    	        MsoCloudifyException me = new MsoCloudifyException (0, "Uninstall Workflow Failed", uninstallWorkflow.getError());
                me.addContext (DELETE_DEPLOYMENT);
                alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
                throw me;
        	}
        }
        catch (MsoException me) {
        	// Uninstall workflow has failed.
        	// Must fail the deletion... may leave the deployment in an inconclusive state 
            me.addContext (DELETE_DEPLOYMENT);
            alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
            throw me;
        }
        
        // At this point, the deployment has been successfully uninstalled.
        // Next step is to delete the deployment itself
        try {
        	DeleteDeployment deleteRequest = cloudify.deployments().deleteByName(deploymentId);
        	LOGGER.debug(deleteRequest.toString());
        	
        	// The delete request returns the deleted deployment
        	deployment = deleteRequest.execute();
        	
        }
        catch (CloudifyConnectException ce) {
        	// Failed to delete.  Must fail the request, but will leave the (uninstalled)
    		// deployment in Cloudify DB.
	        MsoCloudifyException me = new MsoCloudifyException (0, "Deployment Delete Failed", ce.getMessage(), ce);
            me.addContext (DELETE_DEPLOYMENT);
            alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
            throw me;
        }
        catch (CloudifyResponseException re) {
        	// Failed to delete.  Must fail the request, but will leave the (uninstalled)
    		// deployment in the Cloudify DB.
	        MsoCloudifyException me = new MsoCloudifyException (re.getStatus(), re.getMessage(), re.getMessage(), re);
            me.addContext (DELETE_DEPLOYMENT);
            alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
            throw me;
        }
        catch (Exception e) {
        	// Catch-all
        	MsoAdapterException ae = new MsoAdapterException (e.getMessage(), e);
            ae.addContext (DELETE_DEPLOYMENT);
            alarmLogger.sendAlarm(CLOUDIFY_ERROR, MsoAlarmLogger.CRITICAL, ae.getContextMessage());
            throw ae;
        }

    	// Return the deleted deployment info (with runtime outputs) along with the completed uninstall workflow status
        return new DeploymentInfo (deployment, outputs, uninstallWorkflow);
    }

    
    /**
     * Check if a blueprint is available for use at a targeted cloud site.
     * This requires checking the Cloudify Manager which is servicing that
     * cloud site to see if the specified blueprint has been loaded.
     * 
     * @param cloudSiteId The cloud site where the blueprint is needed
     * @param blueprintId The ID for the blueprint in Cloudify
     */
    public boolean isBlueprintLoaded (String cloudSiteId, String blueprintId)
    	throws MsoException
    {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound (cloudSiteId);
        }
        
        Cloudify cloudify = getCloudifyClient (cloudSite.get());

    	GetBlueprint getRequest = cloudify.blueprints().getMetadataById(blueprintId);
    	try {
    		Blueprint bp = getRequest.execute();
        	LOGGER.debug("Blueprint exists: " + bp.getId());
    		return true;
    	}
    	catch (CloudifyResponseException ce) {
    		if (ce.getStatus() == 404) {
    			return false;
    		} else {
    			throw ce;
    		}
    	} catch (Exception e) {
    		throw e;
    	} 
    }
    
    /**
     * Upload a blueprint to the Cloudify Manager that is servicing a Cloud Site.
     * The blueprint currently must be structured as a single directory with all
     * of the required files.  One of those files is designated the "main file"
     * for the blueprint.  Files are provided as byte arrays, though expect only
     * text files will be distributed from ASDC and stored by MSO.
     * 
     * Cloudify requires a single root directory in its blueprint zip files.
     * The requested blueprint ID will also be used as the directory.
     * All of the files will be added to this directory in the zip file.
     */
    public void uploadBlueprint (String cloudSiteId,
    							String blueprintId,
    							String mainFileName,
    							Map<String,byte[]> blueprintFiles,
    							boolean failIfExists)
    	throws MsoException
    {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound (cloudSiteId);
        }
        
        Cloudify cloudify = getCloudifyClient (cloudSite.get());

        boolean blueprintUploaded = uploadBlueprint (cloudify, blueprintId, mainFileName, blueprintFiles);
        
        if (!blueprintUploaded && failIfExists) {
        	throw new MsoAdapterException ("Blueprint already exists");
        }
    }
    
    /*
     * Common method to load a blueprint.  May be called from 
     */
    private boolean uploadBlueprint (Cloudify cloudify, String blueprintId, String mainFileName, Map<String,byte[]> blueprintFiles)
    	throws MsoException
    {
    	// Check if it already exists.  If so, return false.
    	GetBlueprint getRequest = cloudify.blueprints().getMetadataById(blueprintId);
    	try {
    		Blueprint bp = getRequest.execute();
    		LOGGER.debug("Blueprint " + bp.getId() + " already exists.");
    		return false;
    	}
    	catch (CloudifyResponseException ce) {
    		if (ce.getStatus() == 404) {
    			// This is the expected result.
    			LOGGER.debug("Verified that Blueprint doesn't exist yet");
    		} else {
    			throw ce;
    		}
    	} catch (Exception e) {
    		throw e;
    	}
    	
    	// Create a blueprint ZIP file in memory
		ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(zipBuffer);

		try {
			// Put the root directory
			String rootDir = blueprintId + ((blueprintId.endsWith("/") ? "" : "/"));
			zipOut.putNextEntry(new ZipEntry (rootDir));
			zipOut.closeEntry();
			
			for (String fileName : blueprintFiles.keySet()) {
				ZipEntry ze = new ZipEntry (rootDir + fileName);
				zipOut.putNextEntry (ze);
				zipOut.write (blueprintFiles.get(fileName));
				zipOut.closeEntry();
			}
			zipOut.close();
		}
		catch (IOException e) {
			// Since we're writing to a byte array, this should never happen
		}
		LOGGER.debug ("Blueprint zip file size: " + zipBuffer.size());
		
		// Ready to upload the blueprint zip
    	InputStream blueprintStream = new ByteArrayInputStream (zipBuffer.toByteArray());
    	try {
    		UploadBlueprint uploadRequest = cloudify.blueprints().uploadFromStream(blueprintId,  mainFileName,  blueprintStream);
    		Blueprint blueprint = uploadRequest.execute();
    		System.out.println("Successfully uploaded blueprint " + blueprint.getId());
    	}
    	catch (CloudifyResponseException e) {
    		MsoException me = cloudifyExceptionToMsoException (e, "UPLOAD_BLUEPRINT");
    		throw me;
    	}
    	catch (CloudifyConnectException e) {
    		MsoException me = cloudifyExceptionToMsoException (e, "UPLOAD_BLUEPRINT");
    		throw me;
    	}
    	catch (RuntimeException e) {
    		// Catch-all
    		MsoException me = runtimeExceptionToMsoException (e, "UPLOAD_BLUEPRINT");
    		throw me;
    	}
    	finally {
    		try {
    			blueprintStream.close();
    		} catch (IOException e) {}
    	}

    	return true;
    }
    
    

    // ---------------------------------------------------------------
    // PRIVATE FUNCTIONS FOR USE WITHIN THIS CLASS

    /**
     * Get a Cloudify client for the specified cloud site.
     * Everything that is required can be found in the Cloud Config.
     *
     * @param cloudSite
     * @return a Cloudify object
     */
    public Cloudify getCloudifyClient (CloudSite cloudSite) throws MsoException
    {
        CloudifyManager cloudifyConfig = cloudSite.getCloudifyManager();
        if (cloudifyConfig == null) {
        	throw new MsoCloudifyManagerNotFound (cloudSite.getId());
        }

        // Get a Cloudify client
    	// Set a Token Provider to fetch tokens from Cloudify itself.
        String cloudifyUrl = cloudifyConfig.getCloudifyUrl();
        Cloudify cloudify = new Cloudify (cloudifyUrl);
    	cloudify.setTokenProvider(new CloudifyClientTokenProvider(cloudifyUrl, cloudifyConfig.getUsername(), cloudifyConfig.getPassword()));

    	return cloudify;
    }


    /*
     * Query for a Cloudify Deployment. This function is needed in several places, so
     * a common method is useful. This method takes an authenticated CloudifyClient
     * (which internally identifies the cloud & tenant to search), and returns
     * a Deployment object if found, Null if not found, or an MsoCloudifyException
     * if the Cloudify API call fails.
     *
     * @param cloudifyClient an authenticated Cloudify client
     *
     * @param deploymentId the deployment to query
     *
     * @return a Deployment object or null if the requested deployment doesn't exist.
     *
     * @throws MsoCloudifyException Thrown if the Cloudify API call returns an exception
     */
    protected Deployment queryDeployment (Cloudify cloudify, String deploymentId) throws MsoException {
        if (deploymentId == null) {
            return null;
        }
        try {
            GetDeployment request = cloudify.deployments().byId (deploymentId);
            return executeAndRecordCloudifyRequest (request, msoProps);
        } catch (CloudifyResponseException e) {
            if (e.getStatus () == 404) {
                LOGGER.debug ("queryDeployment - not found: " + deploymentId);
                return null;
            } else {
                // Convert the CloudifyResponseException to an MsoCloudifyException
                throw cloudifyExceptionToMsoException (e, "QueryDeployment");
            }
        } catch (CloudifyConnectException e) {
            // Connection to Openstack failed
            throw cloudifyExceptionToMsoException (e, "QueryDeployment");
        }
    }


	public void copyStringOutputsToInputs(Map<String, String> inputs,
			Map<String, Object> otherStackOutputs, boolean overWrite) {
		if (inputs == null || otherStackOutputs == null)
			return;
		for (String key : otherStackOutputs.keySet()) {
			if (!inputs.containsKey(key)) {
				Object obj = otherStackOutputs.get(key);
				if (obj instanceof String) {
					inputs.put(key, (String) otherStackOutputs.get(key));
				} else if (obj instanceof JsonNode ){
					// This is a bit of mess - but I think it's the least impacting
					// let's convert it BACK to a string - then it will get converted back later
					try {
						String str = this.convertNode((JsonNode) obj);
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("WARNING: unable to convert JsonNode output value for "+ key);
						//effect here is this value will not have been copied to the inputs - and therefore will error out downstream
					}
				} else if (obj instanceof java.util.LinkedHashMap) {
					LOGGER.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
					try {
						String str = JSON_MAPPER.writeValueAsString(obj);
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("WARNING: unable to convert LinkedHashMap output value for "+ key);
					}
				} else {
					// just try to cast it - could be an integer or some such
					try {
						String str = (String) obj;
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("WARNING: unable to convert output value for "+ key);
						//effect here is this value will not have been copied to the inputs - and therefore will error out downstream
					}
				}
			}
		}
		return;
	}

	/*
	 * Normalize an input value to an Object, based on the target parameter type.
	 * If the type is not recognized, it will just be returned unchanged (as a string).
	 */
	public Object convertInputValue (String inputValue, HeatTemplateParam templateParam)
	{
		String type = templateParam.getParamType();
		LOGGER.debug("Parameter: " + templateParam.getParamName() + " is of type " + type);
		
		if (type.equalsIgnoreCase("number")) {
			try {
				return Integer.valueOf(inputValue);
			}
			catch (Exception e) {
				LOGGER.debug("Unable to convert " + inputValue + " to an integer!");
				return null;
			}
		} else if (type.equalsIgnoreCase("json")) {
			try {
				JsonNode jsonNode = new ObjectMapper().readTree(inputValue);
				return jsonNode;
			}
			catch (Exception e) {
				LOGGER.debug("Unable to convert " + inputValue + " to a JsonNode!");
				return null;
			}
		} else if (type.equalsIgnoreCase("boolean")) {
			return new Boolean(inputValue);
		}
		
		// Nothing else matched.  Return the original string
		return inputValue;
	}
	
	
	private String convertNode(final JsonNode node) {
		try {
			final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
			final String json = JSON_MAPPER.writeValueAsString(obj);
			return json;
		} catch (JsonParseException jpe) {
			LOGGER.debug("Error converting json to string " + jpe.getMessage());
		} catch (Exception e) {
			LOGGER.debug("Error converting json to string " + e.getMessage());
		}
		return "[Error converting json to string]";
	}
	
	
    /*
     * Method to execute a Cloudify command and track its execution time.
     * For the metrics log, a category of "Cloudify" is used along with a
     * sub-category that identifies the specific call (using the real
     * cloudify-client classname of the CloudifyRequest<T> parameter).
     */
    
    protected static <T> T executeAndRecordCloudifyRequest (CloudifyRequest <T> request)
    {
    	return executeAndRecordCloudifyRequest (request, null);
    }
    protected static <T> T executeAndRecordCloudifyRequest (CloudifyRequest <T> request, MsoJavaProperties msoProps) {
    	
    	int limit;
        // Get the name and method name of the parent class, which triggered this method
        StackTraceElement[] classArr = new Exception ().getStackTrace ();
        if (classArr.length >=2) {
        	limit = 3;
        } else {
        	limit = classArr.length;
        }
    	String parentServiceMethodName = classArr[0].getClassName () + "." + classArr[0].getMethodName ();
    	for (int i = 1; i < limit; i++) {
            String className = classArr[i].getClassName ();
            if (!className.equals (MsoCommonUtils.class.getName ())) {
            	parentServiceMethodName = className + "." + classArr[i].getMethodName ();
            	break;
            }
        }

    	String requestType;
        if (request.getClass ().getEnclosingClass () != null) {
            requestType = request.getClass ().getEnclosingClass ().getSimpleName () + "."
                          + request.getClass ().getSimpleName ();
        } else {
            requestType = request.getClass ().getSimpleName ();
        }
        
        int retryDelay = retryDelayDefault;
        int retryCount = retryCountDefault;
        String retryCodes  = retryCodesDefault;
        if (msoProps != null) //extra check to avoid NPE
        {
        	retryDelay = msoProps.getIntProperty (retryDelayProp, retryDelayDefault);
        	retryCount = msoProps.getIntProperty (retryCountProp, retryCountDefault);
        	retryCodes = msoProps.getProperty (retryCodesProp, retryCodesDefault);
        }
    	
        // Run the actual command. All exceptions will be propagated
        while (true)
        {
        	try {
                return request.execute ();
        	} 
        	catch (CloudifyResponseException e) {
        		boolean retry = false;
        		if (retryCodes != null ) {
        			int code = e.getStatus();
        			LOGGER.debug ("Config values RetryDelay:" + retryDelay + " RetryCount:" + retryCount + " RetryCodes:" + retryCodes + " ResponseCode:" + code);
        			for (String rCode : retryCodes.split (",")) {
        				try {
        					if (retryCount > 0 && code == Integer.parseInt (rCode))
        					{
        						retryCount--;
        						retry = true;
        						LOGGER.debug ("CloudifyResponseException ResponseCode:" + code +  " at:" + parentServiceMethodName + " request:" + requestType +  " Retry indicated. Attempts remaining:" + retryCount);
        						break;
        					}
        				} catch (NumberFormatException e1) {
        					LOGGER.error (MessageEnum.RA_CONFIG_EXC, "No retries. Exception in parsing retry code in config:" + rCode, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in parsing retry code in config");
        					throw e;
        				}
        			}
        		}
        		if (retry)
    			{
    				try {
    					Thread.sleep (retryDelay * 1000L);
    				} catch (InterruptedException e1) {
    					LOGGER.debug ("Thread interrupted while sleeping", e1);
    				}
    			}
        		else
        			throw e; // exceeded retryCount or code is not retryable
        	}
        	catch (CloudifyConnectException e) {
        		// Connection to Cloudify failed
        		if (retryCount > 0)
        		{
        			retryCount--;
        			LOGGER.debug ("CloudifyConnectException at:" + parentServiceMethodName + " request:" + requestType + " Retry indicated. Attempts remaining:" + retryCount);
        			try {
        				Thread.sleep (retryDelay * 1000L);
        			} catch (InterruptedException e1) {
        				LOGGER.debug ("Thread interrupted while sleeping", e1);
        			}
        		}
        		else
        			throw e;
        			
        	}
        }
    }
    /*
     * Convert an Exception on a Cloudify call to an MsoCloudifyException.
     * This method supports CloudifyResponseException and CloudifyConnectException.
     */
    protected MsoException cloudifyExceptionToMsoException (CloudifyBaseException e, String context) {
        MsoException me = null;

        if (e instanceof CloudifyResponseException) {
        	CloudifyResponseException re = (CloudifyResponseException) e;

            try {
                // Failed Cloudify calls return an error entity body.
                CloudifyError error = re.getResponse ().getErrorEntity (CloudifyError.class);
                LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Cloudify", "Cloudify Error on " + context + ": " + error.getErrorCode(), "Cloudify", "", MsoLogger.ErrorCode.DataError, "Exception - Cloudify Error on " + context);
                String fullError = error.getErrorCode() + ": " + error.getMessage();
                LOGGER.debug(fullError);
				me = new MsoCloudifyException (re.getStatus(),
                                                re.getMessage(),
                                                fullError);
            } catch (Exception e2) {
                // Couldn't parse the body as a "CloudifyError". Report the original HTTP error.
            	LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Cloudify", "HTTP Error on " + context + ": " + re.getStatus() + "," + e.getMessage(), "Cloudify", "", MsoLogger.ErrorCode.DataError, "Exception - HTTP Error on " + context, e2);
				me = new MsoCloudifyException (re.getStatus (), re.getMessage (), "");
            }

            // Add the context of the error
            me.addContext (context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus () >= 500) {
                alarmLogger.sendAlarm ("CloudifyError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
            }
        } else if (e instanceof CloudifyConnectException) {
        	CloudifyConnectException ce = (CloudifyConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.
            alarmLogger.sendAlarm ("CloudifyIOError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
            LOGGER.error(MessageEnum.RA_CONNECTION_EXCEPTION, "Cloudify", "Cloudify connection error on " + context + ": " + e, "Cloudify", "", MsoLogger.ErrorCode.DataError, "Cloudify connection error on " + context);
    	}

        return me;
    }

    /*
     * Return an OpenstackConfig object as expected by Cloudify Openstack Plug-in.
     * Base the values on the CloudSite definition.
     */
    private OpenstackConfig getOpenstackConfig (CloudSite cloudSite, String tenantId) {
        OpenstackConfig openstackConfig = new OpenstackConfig();
        openstackConfig.setRegion (cloudSite.getRegionId());
        openstackConfig.setAuthUrl (cloudSite.getIdentityService().getIdentityUrl());
        openstackConfig.setUsername (cloudSite.getIdentityService().getMsoId());
        openstackConfig.setPassword (cloudSite.getIdentityService().getMsoPass());
        openstackConfig.setTenantName (tenantId);
        return openstackConfig;
    }
}
