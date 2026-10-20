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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class SpringOwnedMariaDbService extends MariaDB4jSpringService {
    private Path ownedDirectory;

    @Override
    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        try {
            Path parent = Paths.get("target", "groovy-migration-mariadb").toAbsolutePath().normalize();
            Files.createDirectories(parent);
            ownedDirectory = Files.createTempDirectory(parent, "database-");
            DBConfigurationBuilder configuration = getConfiguration();
            configuration.setDataDir(ownedDirectory.resolve("data").toString());
            configuration.setTmpDir(ownedDirectory.resolve("tmp").toString());
            db = createDatabase(configuration.build());
            db.start();
        } catch (Exception failure) {
            try {
                preDestroy();
            } catch (Exception cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw new IllegalStateException("Unable to start the Spring-owned test database", failure);
        }
    }

    protected DB createDatabase(DBConfiguration configuration) throws ManagedProcessException {
        SpringOwnedDatabase database = new SpringOwnedDatabase(configuration);
        database.prepare();
        return database;
    }

    @Override
    public void stop() {
        // Spring stops Lifecycle beans before destroying Camunda and its datasource.
        // Keep MariaDB available until dependent beans have been destroyed.
    }

    @PreDestroy
    @Override
    public synchronized void preDestroy() {
        super.stop();
        if (ownedDirectory == null) {
            return;
        }
        try {
            Files.walkFileTree(ownedDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException failure) throws IOException {
                    if (failure != null) {
                        throw failure;
                    }
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
            ownedDirectory = null;
        } catch (IOException failure) {
            throw new IllegalStateException("Unable to remove the test database directory " + ownedDirectory, failure);
        }
    }

    private static class SpringOwnedDatabase extends DB {
        SpringOwnedDatabase(DBConfiguration configuration) {
            super(configuration);
        }

        void prepare() throws ManagedProcessException {
            prepareDirectories();
            unpackEmbeddedDb();
            install();
        }

        @Override
        protected void cleanupOnExit() {
            // An independent JVM hook can stop MariaDB before Spring closes Camunda.
        }
    }
}
