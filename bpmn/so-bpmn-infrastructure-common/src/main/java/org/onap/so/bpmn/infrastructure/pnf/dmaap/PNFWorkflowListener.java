package org.onap.so.bpmn.infrastructure.pnf.dmaap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class PNFWorkflowListener {

    private static PNFWorkflowListener instance;

    private static final Logger logger = LoggerFactory.getLogger(PnfEventReadyDmaapClient.class);

    static {
        try {
            instance = new PNFWorkflowListener();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    public static PNFWorkflowListener getInstance() {
        return instance;
    }

    private Map<String, Runnable> pnfCorrelationMap;

    private PNFWorkflowListener() {
        pnfCorrelationMap = new HashMap<>();
    }

    public void registerForUpdate(String pnfCorrelationId, Runnable informConsumer) {
        logger.debug("registering for pnf workflow complete event for pnf correlation id: {}", pnfCorrelationId);
        if (pnfCorrelationMap.containsKey(pnfCorrelationId))
            return;
        pnfCorrelationMap.put(pnfCorrelationId, informConsumer);
    }

    private Runnable unregister(String pnfCorrelationId) {
        logger.debug("unregistering from pnf workflow complete event for pnf correlation id: {}", pnfCorrelationId);
        if (!pnfCorrelationMap.containsKey(pnfCorrelationId))
            return null;
        return pnfCorrelationMap.remove(pnfCorrelationId);
    }

    public void notifyWorkflowCompleteEvent(String correlationId) {
        Runnable informConsumer = unregister(correlationId);
        if (informConsumer != null) {
            informConsumer.run();
        }
    }

}
