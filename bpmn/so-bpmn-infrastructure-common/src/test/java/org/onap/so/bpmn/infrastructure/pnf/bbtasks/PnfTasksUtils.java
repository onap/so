package org.onap.so.bpmn.infrastructure.pnf.bbtasks;

import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import java.util.UUID;

public class PnfTasksUtils {
    static final String PNF_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    static final String PNF_CORRELATION_ID = "testPnfCorrelationId";

    public static Pnf preparePnf(String pnfName, String pnfUuid) {
        Pnf pnf = new Pnf();
        pnf.setPnfName(pnfName);
        pnf.setPnfId(pnfUuid);
        return pnf;
    }

}
