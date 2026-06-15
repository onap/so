/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.cds;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.hibernate.TransactionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.springframework.orm.jpa.JpaSystemException;

@RunWith(MockitoJUnitRunner.class)
public class MessageSendingHandlerRetryTest {

    @Test
    public void testCorrelateRetriesOnDeadlock() throws Exception {
        MessageCorrelationBuilder correlationBuilder = mock(MessageCorrelationBuilder.class);
        when(correlationBuilder.setVariable(anyString(), any())).thenReturn(correlationBuilder);
        AutoCloseable client = mock(AutoCloseable.class);

        SQLTransactionRollbackException deadlock = new SQLTransactionRollbackException(
                "Deadlock found when trying to get lock; try restarting transaction");
        TransactionException txException = new TransactionException("Unable to commit against JDBC Connection",
                deadlock);
        JpaSystemException jpaException = new JpaSystemException(txException);

        doThrow(jpaException).doReturn(mock(MessageCorrelationResult.class)).when(correlationBuilder).correlate();

        CDSProcessingListener handler = createMessageSendingHandler(correlationBuilder, client);
        ExecutionServiceOutput message = buildSuccessResponse();
        handler.onMessage(message);

        verify(correlationBuilder, times(2)).correlate();
        verify(client).close();
    }

    @Test
    public void testCorrelateSucceedsWithoutDeadlock() throws Exception {
        MessageCorrelationBuilder correlationBuilder = mock(MessageCorrelationBuilder.class);
        when(correlationBuilder.setVariable(anyString(), any())).thenReturn(correlationBuilder);
        AutoCloseable client = mock(AutoCloseable.class);

        when(correlationBuilder.correlate()).thenReturn(mock(MessageCorrelationResult.class));

        CDSProcessingListener handler = createMessageSendingHandler(correlationBuilder, client);
        ExecutionServiceOutput message = buildSuccessResponse();
        handler.onMessage(message);

        verify(correlationBuilder, times(1)).correlate();
        verify(client).close();
    }

    @Test
    public void testCorrelateDoesNotRetryNonDeadlockDataAccessException() throws Exception {
        MessageCorrelationBuilder correlationBuilder = mock(MessageCorrelationBuilder.class);
        when(correlationBuilder.setVariable(anyString(), any())).thenReturn(correlationBuilder);
        AutoCloseable client = mock(AutoCloseable.class);

        SQLException sqlException = new SQLException("Some other SQL error");
        TransactionException txException = new TransactionException("Unable to commit", sqlException);
        JpaSystemException jpaException = new JpaSystemException(txException);

        doThrow(jpaException).when(correlationBuilder).correlate();

        CDSProcessingListener handler = createMessageSendingHandler(correlationBuilder, client);
        ExecutionServiceOutput message = buildSuccessResponse();
        try {
            handler.onMessage(message);
        } catch (JpaSystemException e) {
            // expected
        }

        verify(correlationBuilder, times(1)).correlate();
        verify(client).close();
    }

    private CDSProcessingListener createMessageSendingHandler(MessageCorrelationBuilder correlationBuilder,
            AutoCloseable client) throws Exception {
        AbstractCDSProcessingBBUtils utils = new AbstractCDSProcessingBBUtils();
        Class<?> handlerClass = Class
                .forName("org.onap.so.client.cds.AbstractCDSProcessingBBUtils$MessageSendingHandler");
        Constructor<?> constructor = handlerClass.getDeclaredConstructor(AbstractCDSProcessingBBUtils.class,
                MessageCorrelationBuilder.class);
        constructor.setAccessible(true);
        CDSProcessingListener handler = (CDSProcessingListener) constructor.newInstance(utils, correlationBuilder);

        Method setClient = handlerClass.getDeclaredMethod("setClient", AutoCloseable.class);
        setClient.setAccessible(true);
        setClient.invoke(handler, client);

        return handler;
    }

    private ExecutionServiceOutput buildSuccessResponse() {
        return ExecutionServiceOutput.newBuilder()
                .setCommonHeader(
                        CommonHeader.newBuilder().setRequestId("test-request").setSubRequestId("test-sub").build())
                .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("config-assign").build())
                .setStatus(Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_EXECUTED).build()).build();
    }
}
