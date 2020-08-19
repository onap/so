package org.onap.so.bpmn.infrastructure.service.level.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServiceLevelConstants {
    public static final String BPMN_REQUEST = "bpmnRequest";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    public static final String PNF_NAME = "pnfName";
    public static final String PNF = "pnf";
    public static final String VNF = "vnf";
    public static final String EMPTY_STRING = "";
    public static final String HEALTH_CHECK_WORKFLOW_TO_INVOKE = "healthCheckWorkflow";
    public static final String SOFTWARE_WORKFLOW_TO_INVOKE = "softwareUpgradeWorkflow";
    public static final String HEALTH_CHECK_OPERATION = "ResourceHealthCheck";
    public static final String SW_UP_OPERATION = "ResourceSoftwareUpgrade";
    public static final String CONTROLLER_STATUS = "ControllerStatus";
    public static final int ERROR_CODE = 601;

    // TODO GenericVNFHealthCheck and GenericVnfSoftwareUpgrade workflow names should be updated once the workflow is
    // implemented.
    public static final Map<String, String> DEFAULT_HEALTH_CHECK_WORKFLOWS =
            Map.of(PNF, "GenericPnfHealthCheck", VNF, "GenericVNFHealthCheck");

    public static final Map<String, String> DEFAULT_SOFTWARE_UP_WORKFLOWS =
            Map.of(PNF, "PNFSoftwareUpgrade", VNF, "GenericVnfSoftwareUpgrade");

    public static final List<String> VALID_CONTROLLER_SCOPE = Arrays.asList(PNF, VNF);



}
