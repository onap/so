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
package org.onap.so.bpmn.common.verification;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.onap.so.SpringOwnedMariaDbService;
import org.onap.so.bpmn.common.scripts.ReceiveWorkflowMessage;
import org.onap.so.bpmn.core.UrnPropertiesReader;

public abstract class WorkflowPersistenceSupport {
    protected static final long START_TIME = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();
    protected ProcessEngine engine;
    private static DB maria;
    private static SpringOwnedMariaDbService mariaService;
    private static byte[] productionBpmn;
    private static byte[] manualHandlingBpmn;
    private static byte[] rainyDayBpmn;
    private static byte[] cnfAdapterBpmn;
    private String database;
    private String jdbcUrl;

    @BeforeClass
    public static void configureBackendAndProductionResource() throws Exception {
        String backend = System.getProperty("groovy.migration.persistence.backend", "h2");
        assertTrue("Expected h2 or mariadb persistence backend", Arrays.asList("h2", "mariadb").contains(backend));
        productionBpmn = productionResource("subprocess/ReceiveWorkflowMessage.bpmn");
        manualHandlingBpmn = productionResource("subprocess/BuildingBlock/ManualHandling.bpmn");
        rainyDayBpmn = productionResource("subprocess/BuildingBlock/RainyDayHandler.bpmn");
        cnfAdapterBpmn = productionResource("subprocess/CNFAdapterAsyncCall.bpmn");
        if ("mariadb".equals(backend)) {
            Path directory = Paths.get("target", "groovy-migration", "persistence").toAbsolutePath();
            Files.createDirectories(directory);
            mariaService = new SpringOwnedMariaDbService();
            DBConfigurationBuilder config = mariaService.getConfiguration();
            config.setPort(0);
            config.setBaseDir(directory.resolve("mariadb-base").toString());
            config.addArg("--lower_case_table_names=1");
            mariaService.start();
            maria = mariaService.getDB();
        }
    }

    @AfterClass
    public static void stopBackend() {
        if (mariaService != null) {
            mariaService.preDestroy();
            mariaService = null;
            maria = null;
        }
    }

    @Before
    public void createEngine() throws Exception {
        ClockUtil.setCurrentTime(new Date(START_TIME));
        database = "groovy_" + UUID.randomUUID().toString().replace("-", "");
        if (maria == null) {
            jdbcUrl = "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1";
        } else {
            try (Connection connection = DriverManager.getConnection(maria.getConfiguration().getURL(""), "root", "")) {
                connection.createStatement().execute("CREATE DATABASE " + database);
            }
            jdbcUrl = maria.getConfiguration().getURL(database);
        }
        reopenEngine();
        engine.getRepositoryService().createDeployment()
                .addInputStream("ReceiveWorkflowMessage.bpmn", new ByteArrayInputStream(productionBpmn))
                .addInputStream("ManualHandling.bpmn", new ByteArrayInputStream(manualHandlingBpmn))
                .addInputStream("RainyDayHandler.bpmn", new ByteArrayInputStream(rainyDayBpmn))
                .addInputStream("CNFAdapterAsyncCall.bpmn", new ByteArrayInputStream(cnfAdapterBpmn)).deploy();
    }

    @After
    public void closeEngineAndDatabase() throws Exception {
        try {
            if (engine != null) {
                engine.close();
            }
            if (jdbcUrl != null) {
                if (maria == null) {
                    try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "")) {
                        connection.createStatement().execute("SHUTDOWN");
                    }
                } else {
                    try (Connection connection =
                            DriverManager.getConnection(maria.getConfiguration().getURL(""), "root", "")) {
                        connection.createStatement().execute("DROP DATABASE " + database);
                    }
                }
            }
        } finally {
            ClockUtil.reset();
        }
    }

    private void reopenEngine() {
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration();
        configuration.setProcessEngineName(database);
        configuration.setJdbcUrl(jdbcUrl);
        configuration.setJdbcDriver(maria == null ? "org.h2.Driver" : "org.mariadb.jdbc.Driver");
        configuration.setJdbcUsername(maria == null ? "sa" : "root");
        configuration.setJdbcPassword("");
        configuration.setDatabaseType(maria == null ? "h2" : "mysql");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setJdbcBatchProcessing(false);
        configuration.setEnforceHistoryTimeToLive(false);
        // SO's application.yaml keeps MariaDB's default isolation and disables this check.
        configuration.setSkipIsolationLevelCheck(maria != null);
        configuration.setJavaSerializationFormatEnabled(true);
        configuration.setJobExecutorActivate(false);
        configuration.setProcessEnginePlugins(processEnginePlugins());
        configuration.setBeans(Collections.singletonMap("UrnPropertiesReader", new UrnPropertiesReader()));
        engine = configuration.buildProcessEngine();
    }

    protected List<ProcessEnginePlugin> processEnginePlugins() {
        return Collections.singletonList(new ConnectProcessEnginePlugin());
    }

    protected void restart() {
        engine.close();
        engine = null;
        reopenEngine();
    }

    private static byte[] productionResource(String name) throws Exception {
        Path location =
                Paths.get(ReceiveWorkflowMessage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location)) {
            return Files.readAllBytes(location.resolve(name));
        }
        try (JarFile jar = new JarFile(location.toFile())) {
            JarEntry entry = jar.getJarEntry(name);
            assertNotNull("Missing packaged production resource " + name, entry);
            try (var input = jar.getInputStream(entry)) {
                return input.readAllBytes();
            }
        }
    }

}
