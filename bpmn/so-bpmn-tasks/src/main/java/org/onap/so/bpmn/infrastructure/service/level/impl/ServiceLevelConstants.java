package org.onap.so.bpmn.infrastructure.service.level.impl;

public class ServiceLevelConstants {
    public static final String BPMN_REQUEST = "bpmnRequest";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    public static final String PNF_NAME = "pnfName";
    public static final String PNF = "pnf";
    public static final String VNF = "vnf";
    public static final String EMPTY_STRING = "";
    public static final String WORKFLOW_TO_INVOKE = "healthCheckWorkflow";
    public static final String SOFTWARE_WORKFLOW_TO_INVOKE = "softwareUpgradeWorkflow";
    public static final String GENERIC_PNF_HEALTH_CHECK_WORKFLOW = "GenericPnfHealthCheck";
    public static final String PNF_SOFTWARE_UPGRADE_WORKFLOW = "PNFSoftwareUpgrade";
    public static final String CONTROLLER_STATUS = "ControllerStatus";
    public static final int ERROR_CODE = 601;
    // TODO This value needs to be updated once vnf health check workflow is available
    protected static final String GENERIC_VNF_HEALTH_CHECK_WORKFLOW = "GenericVNFHealthCheck";

}
