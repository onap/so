package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.DmaapClient;

public class DmaapClientTestImpl implements DmaapClient {
    private String correlationId;
    private Runnable informConsumer;

    @Override
    public void registerForUpdate(String correlationId, Runnable informConsumer) {
        this.correlationId = correlationId;
        this.informConsumer = informConsumer;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Runnable getInformConsumer() {
        return informConsumer;
    }
}
