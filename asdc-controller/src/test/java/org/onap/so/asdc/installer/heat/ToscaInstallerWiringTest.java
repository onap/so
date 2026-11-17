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

package org.onap.so.asdc.installer.heat;

import static org.junit.Assert.assertFalse;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

public class ToscaInstallerWiringTest {

    private static final List<Class<?>> COLLABORATORS = List.of(PnfResourceInstaller.class,
            AllottedResourceInstaller.class, NetworkResourceInstaller.class, ConfigurationInstaller.class,
            HeatArtifactInstaller.class, VnfResourceInstaller.class, ServiceModelInstaller.class);

    // A default REQUIRED @Transactional is not harmless: a RuntimeException leaving a collaborator marks the shared
    // transaction rollback-only, so the swallowed "already deployed" path fails with UnexpectedRollbackException.
    // @Async would move the work out of the facade's transaction altogether.
    @Test
    public void collaboratorsAreNotTransactionalOrAsync() {
        for (Class<?> c : COLLABORATORS) {
            assertFalse(c + " is @Transactional or @Async", isTransactionalOrAsync(c));
            for (Method m : c.getDeclaredMethods()) {
                assertFalse(m + " is @Transactional or @Async", isTransactionalOrAsync(m));
            }
        }
    }

    private static boolean isTransactionalOrAsync(AnnotatedElement element) {
        return hasAnnotation(element, Transactional.class)
                || hasAnnotation(element, jakarta.transaction.Transactional.class)
                || hasAnnotation(element, Async.class);
    }
}
