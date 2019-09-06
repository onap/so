package org.onap.so.bpmn.cloudify;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.cloudify.client.APIV31Impl;
import org.onap.so.cloudify.client.ExecutionV31;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a few utilities to the JavaDelegate interface
 * 
 * @author dewayne
 *
 */
public abstract class AbstractJavaDelegate implements JavaDelegate {
    private static Logger log = LoggerFactory.getLogger(AbstractJavaDelegate.class);

    protected static final String CFY_CORRELATION_ID = "CFY_CORRELATION_ID";
    // limitation: only archives with blueprint.yaml will work
    protected final static String INP_BLUEPRINT_KEY = "InputCfy_blueprint";
    protected final static String INP_CREDENTIALS_KEY = "InputCfy_credentials";
    protected final static String INP_BLUEPRINT_YAML_KEY = "InputCfy_blueprint_yaml";
    protected final static String INP_BLUEPRINT_NAME_KEY = "InputCfy_blueprint_name";
    protected final static String INSTALL_WF = "install";



    @Override
    public abstract void execute(DelegateExecution execution) throws Exception;

    /**
     * Dumbed down facade for APIV31#runExecution
     * 
     * @param workflowId
     * @param execution
     * @param client
     * @param did the deployment id
     * @param parms workflow parameters
     */
    protected void runWorkflow(String workflowId, DelegateExecution execution, APIV31Impl client, String did,
            Map<String, String> parms) throws Exception {
        if (parms == null)
            parms = new HashMap<String, String>();
        log.info("running workflow '" + workflowId + "' for deployment '" + did + "'");
        ExecutionV31 exe = client.runExecution(workflowId, did, parms, false, false, false, null, 60, false);

        if (exe.getError() != null && exe.getError().length() > 0) {
            throw new Exception("Error executing workflow '" + workflowId + "': " + exe.getError());
        }
        log.info("workflow " + workflowId + " for deployment" + did + " is complete");
    }


    protected APIV31Impl getCloudifyClient(Map<String, String> creds) {
        APIV31Impl client =
                APIV31Impl.create(creds.get("tenant"), creds.get("username"), creds.get("password"), creds.get("url"));
        return client;
    }

}
