package org.openecomp.mso.bpmn.infrastructure.pnf.implementation;

public interface DmaapClient {

    void registerForUpdate(String correlationId, Runnable informConsumer);
}
