package org.onap.so.bpmn.infrastructure.workflow.serviceTask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.scripts.RequestDbHttp;
import org.onap.so.db.request.client.RequestDbOperationsClient;

public class SdncServiceTopologyRequestDbHttpTask extends SdncServiceTopologyOperationTask {

    @Override
    protected RequestDbOperationsClient getRequestDbOperationsClient(DelegateExecution execution) {
        return RequestDbHttp.createClient(execution);
    }
}
