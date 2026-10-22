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
package org.onap.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.List;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringOwnedMariaDbServiceTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void windowsStartupDisablesDomainAuthenticationOnlyForTheOwnedDatabase() throws Exception {
        TestService service = new TestService();
        service.getConfiguration().setOS("winx64");
        try {
            service.start();
            assertTrue(service.configuration.getArgs().contains("--gssapi=OFF"));
            assertEquals(service.directory.resolve("data").toString(), service.configuration.getDataDir());
        } finally {
            service.preDestroy();
        }
    }

    @Test
    public void linuxStartupRetainsTheDefaultAuthenticationConfiguration() throws Exception {
        TestService service = new TestService();
        service.getConfiguration().setOS("linux");
        try {
            service.start();
            assertFalse(service.configuration.getArgs().contains("--gssapi=OFF"));
        } finally {
            service.preDestroy();
        }
    }

    @Test
    public void windowsBootstrapReceivesAnOwnedTemplateWithoutChangingTheSharedBinaryDirectory() throws Exception {
        Path owned = temporary.newFolder("owned database").toPath();
        Path binaries = temporary.newFolder("shared binaries").toPath();
        DBConfiguration configuration = mock(DBConfiguration.class);
        when(configuration.isWindows()).thenReturn(true);
        when(configuration.getDataDir()).thenReturn(owned.resolve("data").toString());
        when(configuration.getBaseDir()).thenReturn(binaries.toString());
        when(configuration.getLibDir()).thenReturn(binaries.resolve("lib").toString());
        when(configuration.getOSLibraryEnvironmentVarName()).thenReturn("PATH");
        when(configuration.getExecutable(DBConfiguration.Executable.InstallDB))
                .thenReturn(binaries.resolve("mariadb-install-db.exe").toFile());
        ManagedProcess process = mock(ManagedProcess.class);
        try (MockedConstruction<ManagedProcessBuilder> builders =
                mockConstruction(ManagedProcessBuilder.class, (builder, context) -> {
                    when(builder.getEnvironment()).thenReturn(new HashMap<>());
                    when(builder.build()).thenReturn(process);
                })) {
            assertSame(process,
                    new SpringOwnedMariaDbService.SpringOwnedDatabase(configuration).createDBInstallProcess());
            ManagedProcessBuilder builder = builders.constructed().get(0);
            verify(builder).addFileArgument("--config", owned.resolve("bootstrap.ini").toFile());
            verify(builder).addFileArgument("--datadir", owned.resolve("data").toFile().getCanonicalFile());
            verify(builder).setWorkingDirectory(binaries.toFile());
            assertEquals(binaries.resolve("lib").toString(), builder.getEnvironment().get("PATH"));
        }
        assertEquals("[mysqld]\ngssapi=OFF\n",
                new String(Files.readAllBytes(owned.resolve("bootstrap.ini")), StandardCharsets.UTF_8));
        assertFalse(Files.exists(binaries.resolve("my.ini")));
        assertFalse(Files.exists(binaries.resolve("bootstrap.ini")));
    }

    @Test
    public void lifecycleStopKeepsTheDatabaseAvailableUntilDependentBeansAreDestroyed() throws Exception {
        TestService service = new TestService();
        doAnswer(call -> {
            service.running.set(true);
            service.events.add("start");
            return null;
        }).when(service.database).start();
        doAnswer(call -> {
            service.running.set(false);
            service.events.add("stop");
            return null;
        }).when(service.database).stop();

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean("database", SpringOwnedMariaDbService.class, () -> service);
            context.registerBean("dependent", DatabaseDependent.class);
            context.refresh();

            assertTrue(service.running.get());
            assertTrue(Files.exists(service.directory));
            context.stop();
            assertTrue("Lifecycle stop must leave MariaDB available to bean destruction", service.running.get());
        }

        assertEquals(Arrays.asList("start", "dependent", "stop"), service.events);
        assertFalse(service.running.get());
        assertFalse(Files.exists(service.directory));
        assertFalse(service.isRunning());
    }

    @Test
    public void failedStartupStopsThePartialDatabaseAndRemovesOnlyItsOwnedDirectory() throws Exception {
        TestService service = new TestService();
        ManagedProcessException failure = new ManagedProcessException("startup failure");
        doThrow(failure).when(service.database).start();

        try {
            service.start();
            fail("Expected database startup failure");
        } catch (IllegalStateException error) {
            assertSame(failure, error.getCause());
        }

        verify(service.database).stop();
        assertFalse(service.isRunning());
        assertFalse(Files.exists(service.directory));
        assertTrue(Files.isDirectory(service.directory.getParent()));
    }

    @Test
    public void repeatedBeanDestructionDoesNotStopOrDeleteTheDatabaseTwice() throws Exception {
        TestService service = new TestService();
        service.start();
        service.preDestroy();
        service.preDestroy();

        verify(service.database, times(1)).stop();
        assertFalse(service.isRunning());
        assertFalse(Files.exists(service.directory));
    }

    public static class DatabaseDependent implements DisposableBean {
        private final TestService service;

        public DatabaseDependent(SpringOwnedMariaDbService service) {
            this.service = (TestService) service;
        }

        @Override
        public void destroy() {
            assertTrue("The dependent bean still needs its database during destruction", service.running.get());
            service.events.add("dependent");
        }
    }

    private static class TestService extends SpringOwnedMariaDbService {
        final DB database = mock(DB.class);
        final AtomicBoolean running = new AtomicBoolean();
        final List<String> events = new ArrayList<>();
        Path directory;
        DBConfiguration configuration;

        @Override
        protected DB createDatabase(DBConfiguration configuration) {
            this.configuration = configuration;
            directory = Paths.get(configuration.getDataDir()).getParent();
            return database;
        }
    }
}
