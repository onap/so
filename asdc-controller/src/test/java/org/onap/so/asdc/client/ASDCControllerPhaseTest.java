/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for the phase-1 (poll thread) / phase-2 (watchdog executor) split of {@link ASDCController}. The real
 * {@link ControllerState} is used so the BUSY/IDLE pairing is exercised for real; every collaborator is mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ASDCControllerPhaseTest {

    @Mock
    private ToscaResourceInstaller toscaInstaller;
    @Mock
    private WatchdogDistributionStatusRepository wdsRepo;
    @Mock
    private ASDCConfiguration asdcConfig;
    @Mock
    private DistributionStatusSender statusSender;
    @Mock
    private NotificationJsonMapper jsonMapper;
    @Mock
    private ArtifactDownloader artifactDownloader;
    @Mock
    private ResourceInstaller resourceInstaller;
    @Mock
    private WatchdogStatusWaiter watchdogWaiter;
    @Mock
    private WatchdogDistribution wd;

    private ASDCController controller;

    private NotificationDataImpl notification;

    @Before
    public void setUp() {
        controller = new ASDCController();
        ReflectionTestUtils.setField(controller, "toscaInstaller", toscaInstaller);
        ReflectionTestUtils.setField(controller, "wdsRepo", wdsRepo);
        ReflectionTestUtils.setField(controller, "asdcConfig", asdcConfig);
        ReflectionTestUtils.setField(controller, "statusSender", statusSender);
        ReflectionTestUtils.setField(controller, "jsonMapper", jsonMapper);
        ReflectionTestUtils.setField(controller, "artifactDownloader", artifactDownloader);
        ReflectionTestUtils.setField(controller, "resourceInstaller", resourceInstaller);
        ReflectionTestUtils.setField(controller, "watchdogWaiter", watchdogWaiter);
        ReflectionTestUtils.setField(controller, "wd", wd);

        notification = new NotificationDataImpl();
        notification.setDistributionID("dist-1");
        notification.setServiceUUID("svc-1");
        notification.setServiceInvariantUUID("svc-inv-1");
        notification.setResources(Collections.emptyList());
        when(jsonMapper.toJson(any())).thenReturn(java.util.Optional.of("{}"));
    }

    @Test
    public void treatNotification_success_installsThenReportsAndReturnsIdle() {
        RecordingExecutor executor = new RecordingExecutor(true);
        controller.setWatchdogExecutor(executor);
        when(watchdogWaiter.waitForComponents("dist-1")).thenReturn(
                new WatchdogStatusResult(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name(), null, true));

        controller.treatNotification(notification);

        // phase 1 ran on the calling (poll) thread
        verify(resourceInstaller).processResourceNotification(any(), any());
        // phase 2 was handed to the executor
        assertEquals(1, executor.submitted);
        verify(watchdogWaiter).waitForComponents("dist-1");
        verify(statusSender).sendFinalDistributionStatus(any(), anyString(), any(DistributionStatusEnum.class), any());
        // BUSY (poll thread) paired with IDLE (executor)
        assertEquals(0, controller.getNbOfNotificationsOngoing());
    }

    @Test
    public void treatNotification_phase1DoesNotWait() {
        // executor that never runs the task: phase 2 must not have executed inline on the poll thread
        RecordingExecutor executor = new RecordingExecutor(false);
        controller.setWatchdogExecutor(executor);

        controller.treatNotification(notification);

        verify(resourceInstaller).processResourceNotification(any(), any());
        assertEquals(1, executor.submitted);
        verifyNoInteractions(watchdogWaiter);
    }

    @Test
    public void treatNotification_optimisticLockingInPhase1_skipsPhase2AndReturnsIdle() {
        RecordingExecutor executor = new RecordingExecutor(true);
        controller.setWatchdogExecutor(executor);
        doThrow(new ObjectOptimisticLockingFailureException("Service", 1L)).when(resourceInstaller)
                .processResourceNotification(any(), any());

        controller.treatNotification(notification);

        assertEquals(0, executor.submitted);
        verifyNoInteractions(watchdogWaiter);
        verifyNoInteractions(statusSender);
        assertEquals(0, controller.getNbOfNotificationsOngoing());
    }

    @Test
    public void treatNotification_executorRejects_runsInlineAndRestoresIdle() {
        // Worst case: the executor rejects outright (pool shut down). Phase 2 must run inline so BUSY is not leaked.
        controller.setWatchdogExecutor(task -> {
            throw new RejectedExecutionException("shutting down");
        });
        when(watchdogWaiter.waitForComponents("dist-1")).thenReturn(
                new WatchdogStatusResult(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name(), null, true));

        controller.treatNotification(notification);

        verify(watchdogWaiter).waitForComponents("dist-1");
        assertEquals("BUSY must not leak when the executor rejects", 0, controller.getNbOfNotificationsOngoing());
    }

    private static final class RecordingExecutor implements Executor {
        private final boolean runInline;
        private int submitted;

        RecordingExecutor(boolean runInline) {
            this.runInline = runInline;
        }

        @Override
        public void execute(Runnable command) {
            submitted++;
            if (runInline) {
                command.run();
            }
        }
    }
}
