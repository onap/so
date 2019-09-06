package org.onap.so.bpmn.cloudify;

import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cloudify.client.APIV31Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this delegate is to provide the capability to "uninstall" (in the general sense) a Cloudify blueprint.
 * This can mean just running the uninstall workflow, optionally deleting the deployment, and optionally deleting the
 * blueprint.
 * 
 * @author dewayne
 *
 */
public class CloudifyRunWorkflowDelegate extends AbstractJavaDelegate {
    private static Logger log = LoggerFactory.getLogger(CloudifyRunWorkflowDelegate.class);

    private final static String INP_DEPLOYMENT_ID_KEY = "InputCfy_deployment";
    private final static String INP_WORKFLOW_ID_KEY = "InputCfy_workflow";
    private final static String INP_WORKFLOW_PARMS_KEY = "Input_workflow_parms";

    public void execute(DelegateExecution execution) throws Exception {
        checkInputs(execution);
        Map<String, String> credentials = (Map<String, String>) execution.getVariable(INP_CREDENTIALS_KEY);
        APIV31Impl client = getCloudifyClient(credentials);
        String did = (String) execution.getVariable(INP_DEPLOYMENT_ID_KEY);
        String wid = (String) execution.getVariable(INP_WORKFLOW_ID_KEY);
        Map<String, String> parms = execution.hasVariable(INP_WORKFLOW_PARMS_KEY)
                ? (Map<String, String>) execution.getVariable(INP_WORKFLOW_PARMS_KEY)
                : null;

        // Run install workflow
        runWorkflow(wid, execution, client, did, parms);

    }

    /******************************************************************
     * PRIVATE METHODS
     ******************************************************************/

    private void checkInputs(DelegateExecution execution) throws Exception {
        StringBuilder sb = new StringBuilder();

        if (!execution.hasVariable(INP_CREDENTIALS_KEY)) {
            sb.append("required input not supplied: " + INP_CREDENTIALS_KEY);
        } else {
            Map<String, String> creds = (Map<String, String>) execution.getVariable(INP_CREDENTIALS_KEY);
            if (!creds.containsKey("url")) {
                sb.append("required credentials entry not supplied: url");
            }
            if (!creds.containsKey("username")) {
                sb.append("required credentials entry not supplied: username");
            }
            if (!creds.containsKey("password")) {
                sb.append("required credentials entry not supplied: password");
            }
            if (!creds.containsKey("tenant")) {
                sb.append("required credentials entry not supplied: tenant");
            }
        }
        if (!execution.hasVariable(INP_CREDENTIALS_KEY)) {
            sb.append("required input not supplied: " + INP_CREDENTIALS_KEY);
        }
        if (!execution.hasVariable(INP_DEPLOYMENT_ID_KEY)) {
            sb.append("required input not supplied: " + INP_DEPLOYMENT_ID_KEY);
        }
        if (!execution.hasVariable(INP_WORKFLOW_ID_KEY)) {
            sb.append("required input not supplied: " + INP_WORKFLOW_ID_KEY);
        }

        if (sb.length() > 0) {
            throw new Exception(sb.toString());
        }
    }
}
