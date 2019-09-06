package org.onap.so.bpmn.cloudify;

import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cloudify.client.APIV31Impl;
import org.onap.so.cloudify.client.DeploymentV31;
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
public class CloudifyUninstallBlueprintDelegate extends AbstractJavaDelegate {
    private static Logger log = LoggerFactory.getLogger(CloudifyUninstallBlueprintDelegate.class);

    private final static String UNINSTALL_WF = "uninstall";
    private final static String INP_DEPLOYMENT_ID_KEY = "InputCfy_deployment";
    private final static String INP_DELETE_DEPLOYMENT_KEY = "InputCfy_delete_deployment";
    private final static String INP_DELETE_BLUEPRINT_KEY = "InputCfy_delete_blueprint";
    private final static String INP_UNINSTALL_PARMS_KEY = "InputCfy_uninstall_parms";

    public void execute(DelegateExecution execution) throws Exception {
        checkInputs(execution);
        Map<String, String> credentials = (Map<String, String>) execution.getVariable(INP_CREDENTIALS_KEY);
        APIV31Impl client = getCloudifyClient(credentials);
        String did = (String) execution.getVariable(INP_DEPLOYMENT_ID_KEY);
        Map<String, String> parms = execution.hasVariable(INP_UNINSTALL_PARMS_KEY)
                ? (Map<String, String>) execution.getVariable(INP_UNINSTALL_PARMS_KEY)
                : null;
        boolean deleteDeployment = execution.hasVariable(INP_DELETE_DEPLOYMENT_KEY)
                ? Boolean.valueOf((String) execution.getVariable(INP_DELETE_DEPLOYMENT_KEY))
                : false;
        boolean deleteBlueprint = execution.hasVariable(INP_DELETE_BLUEPRINT_KEY)
                ? Boolean.valueOf((String) execution.getVariable(INP_DELETE_BLUEPRINT_KEY))
                : false;

        // Run install workflow
        log.info("uninstalling deployment " + did);
        runWorkflow(UNINSTALL_WF, execution, client, did, parms);
        log.info("uninstall complete");

        // Delete deployment if selected. Don't attempt blueprint delete if deployment delete not selected.
        log.debug("delete deployment var =" + deleteDeployment);
        if (deleteDeployment) {
            log.info("deleting deployment " + did);
            DeploymentV31 dep = client.deleteDeployment(did, false);

            if (deleteBlueprint) {
                log.info("deleting blueprint " + dep.getBlueprint_id());
                Thread.sleep(10000L);
                client.deleteBlueprint(dep.getBlueprint_id(), false);
            }
        }

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

        if (sb.length() > 0) {
            throw new Exception(sb.toString());
        }
    }
}
