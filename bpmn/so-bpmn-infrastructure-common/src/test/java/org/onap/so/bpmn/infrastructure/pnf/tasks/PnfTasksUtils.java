package org.onap.so.bpmn.infrastructure.pnf.tasks;

import java.util.UUID;

public class PnfTasksUtils {
    static final String PNF_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    static final String PNF_CORRELATION_ID = "testPnfCorrelationId";

    public static org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf preparePnf(String pnfName, String pnfUuid) {
        org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf();
        pnf.setPnfName(pnfName);
        pnf.setPnfId(pnfUuid);
        return pnf;
    }

}
