package org.openecomp.mso.adapters.vdu;

/**
 * This interface defines a common API for template-based cloud deployments.
 * The methods here should be adaptable for Openstack (Heat), Cloudify (TOSCA),
 * Aria (TOSCA), Multi-VIM (TBD), and others (e.g. Azure Resource Manager).
 * 
 * The deployed instances are referred to here as Virtual Deployment Units (VDUs).
 * The package of templates that define a give VDU is referred to as its blueprint.
 * 
 * Template-based orchestrators all follow a similar template/blueprint model.
 * - One main template that is the top level definition
 * - Optional nested templates referenced/included by the main template
 * - Optional files attached to the template package, typically containing
 *   configuration files, install scripts, orchestration scripts, etc.
 *   
 * The main template also defines the required inputs for creating a new instance,
 * and output values exposed by successfully deployed instances.  Inputs and outputs
 * may include simple or complex (JSON) data types.
 *   
 * Each implementation of this interface is expected to understand the MSO CloudConfig
 * to obtain the credentials for its sub-orchestrator and the targeted cloud.
 * The sub-orchestrator may have different credentials from the cloud (e.g. an Aria 
 * instance in front of an Openstack cloud) or they may be the same (e.g. Heat)
 */
import java.util.Map;

public interface VduPlugin {

    /**
     * The instantiateVdu interface deploys a new VDU instance from a vdu model package.
     * 
     * For some VIMs, this may be a single command (e.g. Heat -> create stack) or may
     * require a series of API calls (e.g. Cloudify -> upload blueprint, create deployment,
     * execute install workflow).  These details are hidden within the plug-in implementation.
     * The instantiation should be fully completed before returning.  On failures, this
     * method is expected to back out the attempt, leaving the cloud in its previous state.
     * 
     * It is expected that parameters have been validated and contain at minimum the
     * required parameters for the given template with no extra parameters.
     *
     * The VDU name supplied by the caller will be globally unique, and identify the artifact
     * in A&AI.  Inventory is managed by the higher levels invoking this function.
     *
     * @param cloudInfo The target cloud + tenant identifiers for the VDU.
     * @param instanceName A unique name for the VDU instance to update.
     * @param inputs A map of key/value inputs.  Values may be strings, numbers, or JSON objects.
     * 		Will completely replace any inputs provided on the original instantiation.
     * @param vduModel Object containing the collection of templates and files that comprise
     * 		the blueprint for this VDU.
     * @param rollbackOnFailure Flag to preserve or roll back the update on Failure.  Should normally
     *		be True except in troubleshooting/debug cases.  Might not be supported in all plug-ins.
     * 
     * @return A VduInstance object
     * @throws VduException Thrown if the sub-orchestrator API calls fail or if a timeout occurs.
     * Various subclasses of VduException may be thrown.
     */
    public VduInstance instantiateVdu (
    				CloudInfo cloudInfo,
    				String instanceName,
    				Map<String,Object> inputs,
    				VduModelInfo vduModel,
    				boolean rollbackOnFailure)
			throws VduException;
    
    /**
     * Query a deployed VDU instance.  This call will return a VduInstance object, or null
     * if the deployment does not exist.
     * 
     * Some VIM orchestrators identify deployment instances by string UUIDs, and others 
     * by integers.  In the latter case, the ID will be passed in as a numeric string.
     *
     * The returned VduInstance object contains the input and output parameter maps,
     * as well as other properties of the deployment (name, status, last action, etc.).
     * 
     * @param cloudInfo The target cloud + tenant identifiers for the VDU.
     * @param vduInstanceId The ID of the deployment to query
     * 
     * @return A VduInstance object
     * @throws VduException Thrown if the sub-orchestrator API calls fail or if a timeout occurs.
     * Various subclasses of VduException may be thrown.
     */
    public VduInstance queryVdu (
    				CloudInfo cloudInfo,
    				String vduInstanceId)
			throws VduException;

    
    /**
     * Delete a VDU instance by ID.  If the VIM sub-orchestrator supports pre-installation
     * of blueprints/models, the blueprint itself may remain installed.  This is recommended,
     * since other VDU instances may be using it.
     * 
     * Some VIM orchestrators identify deployment instances by string UUIDs, and others 
     * by integers.  In the latter case, the ID will be passed in as a numeric string.
     * 
     * For some VIMs, deletion may be a single command (e.g. Heat -> delete stack) or a
     * series of API calls (e.g. Cloudify -> execute uninstall workflow, delete deployment).
     * These details are hidden within the plug-in implementation.  The deletion should be
     * fully completed before returning.    
     *  
     * The successful return is a VduInstance object which contains the state of the VDU
     * just prior to deletion, with a status of DELETED.  If the deployment was not found,
     * the VduInstance object should be empty (with a status of NOTFOUND).
     * There is no rollback from a successful deletion.
     * 
     * A deletion failure will result in an undefined deployment state - the components may
     * or may not have been all or partially uninstalled, so the resulting deployment must
     * be considered invalid.
     *
     * @param cloudInfo The target cloud + tenant identifiers for the VDU.
     * @param instanceId The unique id of the deployment to delete.
     * @param timeoutMinutes Timeout after which the delete action will be cancelled.
     * 		Consider sending the entire model here, if it may be of use to the plug-in?
     * 
     * @return A VduInstance object, representing its state just prior to deletion.
     * 
     * @throws VduException Thrown if the API calls fail or if a timeout occurs.
     * Various subclasses of VduException may be thrown.
     */
    public VduInstance deleteVdu (
    				CloudInfo cloudInfo,
    				String instanceId,
    				int timeoutMinutes)
			throws VduException;

    
    /**
     * The updateVdu interface attempts to update a VDU in-place, using either new inputs or
     * a new model definition (i.e. updated templates/blueprints).  This depends on the
     * capabilities of the targeted sub-orchestrator, as not all implementations are expected
     * to support this ability.  It is primary included initially only for Heat.
	 *
     * It is expected that parameters have been validated and contain at minimum the required
     * parameters for the given template with no extra parameters.  The VDU instance name cannot
     * be updated. 
     * 
   	 * The update should be fully completed before returning. The successful return is a
	 * VduInstance object containing the updated VDU state.
     * 
     * An update failure will result in an undefined deployment state - the components may
     * or may not have been all or partially modified, deleted, recreated, etc.  So the resulting
     * VDU must be considered invalid.
     * 
     * @param cloudInfo The target cloud + tenant identifiers for the VDU.
     * @param instanceId The unique ID for the VDU instance to update.
     * @param inputs A map of key/value inputs.  Values may be strings, numbers, or JSON objects.
     * 		Will completely replace any inputs provided on the original instantiation.
     * @param vduModel Object containing the collection of templates and files that comprise
     * 		the blueprint for this VDU.
     * @param rollbackOnFailure Flag to preserve or roll back the update on Failure.  Should normally
     *		be True except in troubleshooting/debug cases.  Might not be supported in all plug-ins.
     * 
     * @return A VduInfo object
     * @throws VduException Thrown if the sub-orchestrator API calls fail or if a timeout occurs.
     * Various subclasses of VduException may be thrown.
     */
    public VduInstance updateVdu (
			CloudInfo cloudInfo,
			String instanceId,
			Map<String,Object> inputs,
			VduModelInfo vduModel,
			boolean rollbackOnFailure)
					throws VduException;

}