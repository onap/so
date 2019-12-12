package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;

import java.util.UUID;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

public class PnfInputCheckersTestUtils {

    static final String PNF_ENTRY_NOTIFICATION_TIMEOUT = "P1D";
    static final String VALID_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    static final String RESERVED_UUID = new UUID(0, 0).toString();
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "da7d07d9-b71c-4128-809d-2ec01c807169";
    private static final String DEFAULT_PNF_CORRELATION_ID = "testPnfCorrelationId";

    static class DelegateExecutionBuilder {
        private String pnfCorrelationId = DEFAULT_PNF_CORRELATION_ID;
        private String pnfUuid = VALID_UUID;
        private String serviceInstanceId = DEFAULT_SERVICE_INSTANCE_ID;

        public DelegateExecutionBuilder setPnfCorrelationId(String pnfCorrelationId) {
            this.pnfCorrelationId = pnfCorrelationId;
            return this;
        }

        public DelegateExecutionBuilder setPnfUuid(String pnfUuid) {
            this.pnfUuid = pnfUuid;
            return this;
        }

        public DelegateExecutionBuilder setServiceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public DelegateExecution build() {
            DelegateExecution execution = new DelegateExecutionFake();
            execution.setVariable("testProcessKey", "testProcessKeyValue");
            execution.setVariable(PNF_CORRELATION_ID, this.pnfCorrelationId);
            execution.setVariable(PNF_UUID, this.pnfUuid);
            execution.setVariable(SERVICE_INSTANCE_ID, this.serviceInstanceId);
            return execution;
        }
    }
}
