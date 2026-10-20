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

package org.onap.so.bpmn.infrastructure.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GroovyExecutionInventoryTest {
    private static final String BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String CAMUNDA = "http://camunda.org/schema/1.0/bpmn";
    private static final List<String> MODULES =
            Arrays.asList("MSOCommonBPMN", "so-bpmn-infrastructure-flows", "so-bpmn-building-blocks", "so-bpmn-moi");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void packagedGroovySurfacesMatchReviewedBaseline() throws Exception {
        Assume.assumeTrue("Packaged inventory requires groovy-migration-verification profile",
                Boolean.getBoolean("groovy.migration.verification"));
        Inventory inventory = packagedInventory();
        Path output = Paths.get("target", "groovy-migration", "execution-inventory.json");
        Files.createDirectories(output.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), inventory.report());
        try (InputStream input = getClass().getResourceAsStream("/groovy-migration-scenarios.json")) {
            assertTrue("Missing scenario manifest", input != null);
            JsonNode manifest = MAPPER.readTree(input);
            assertEquals(1, manifest.path("schemaVersion").asInt());
            JsonNode baseline = manifest.path("baseline");
            String diagnostic = "Packaged execution surface changed; inspect " + output.toAbsolutePath();
            assertEquals(diagnostic, baseline.path("resources").asInt(), inventory.resources.size());
            assertEquals(diagnostic, baseline.path("processes").asInt(), inventory.processes.size());
            assertEquals(diagnostic, baseline.path("groovySurfaces").asInt(), inventory.surfaces.size());
            assertEquals(diagnostic, baseline.path("sha256").asText(), inventory.fingerprint());
            validateProtectedSurfaces(inventory, manifest);
        }
    }

    @Test
    public void packagedProductionDefinitionsDeployInCamunda() throws Exception {
        Assume.assumeTrue("Packaged deployment smoke requires groovy-migration-verification profile",
                Boolean.getBoolean("groovy.migration.verification"));
        Inventory inventory = packagedInventory();
        Map<String, String> failures = new TreeMap<>();
        Set<String> deployed = new TreeSet<>();
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        String name = "production-deployment-" + java.util.UUID.randomUUID();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJobExecutorActivate(false);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_NONE);
        configuration.setProcessEnginePlugins(Collections.singletonList(new ConnectProcessEnginePlugin()));
        ProcessEngine engine = configuration.buildProcessEngine();
        try {
            for (Map.Entry<String, byte[]> resource : inventory.productionBytes.entrySet()) {
                String resourceName = resource.getKey();
                if (!resourceName.endsWith(".bpmn") && !resourceName.endsWith(".bpmn20.xml")) {
                    resourceName += ".bpmn";
                }
                try (InputStream content = new ByteArrayInputStream(resource.getValue())) {
                    engine.getRepositoryService().createDeployment().name(resource.getKey())
                            .addInputStream(resourceName, content).deploy();
                    deployed.add(resource.getKey());
                } catch (RuntimeException failure) {
                    failures.put(resource.getKey(), failure.getMessage());
                }
            }
            Path output = Paths.get("target", "groovy-migration", "production-deployment.json");
            Files.createDirectories(output.getParent());
            Map<String, Object> report = new TreeMap<>();
            report.put("resources", inventory.resources);
            report.put("deployedResources", deployed);
            report.put("failedResources", failures);
            long definitions = engine.getRepositoryService().createProcessDefinitionQuery().count();
            report.put("deployedProcessDefinitions", definitions);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
            assertTrue(
                    "Production BPMN deployment failed; inspect " + output.toAbsolutePath() + ": " + failures.keySet(),
                    failures.isEmpty());
            assertEquals("Every packaged process must be deployable", inventory.processes.size(), definitions);
        } finally {
            engine.close();
        }
    }

    @Test
    public void inventoriesListenersAndIoScriptsRegardlessOfFileExtension() throws Exception {
        Inventory inventory = new Inventory();
        inventory.add("fixture", "workflow.custom", fixture("<b:serviceTask id='task'><b:extensionElements>"
                + "<c:executionListener event='start'><c:script scriptFormat='groovy'>start(execution)</c:script>"
                + "</c:executionListener><c:inputOutput><c:inputParameter name='input'>"
                + "<c:script scriptFormat='groovy' resource='deployment://input.groovy'/>"
                + "</c:inputParameter></c:inputOutput></b:extensionElements></b:serviceTask>"));
        assertEquals(2, inventory.surfaces.size());
        assertTrue(inventory.surfaces.keySet().stream().anyMatch(key -> key.contains("executionListener")));
        assertTrue(inventory.surfaces.values().stream().anyMatch(value -> value.contains("input.groovy")));
    }

    @Test
    public void additionalScriptOrRemovedActivityChangesTheFingerprint() throws Exception {
        Inventory original = new Inventory();
        original.add("fixture", "flow.bpmn", fixture(script("first")));
        Inventory additional = new Inventory();
        additional.add("fixture", "flow.bpmn", fixture(script("first") + script("second")));
        assertNotEquals(original.fingerprint(), additional.fingerprint());
        Inventory removed = new Inventory();
        removed.add("fixture", "flow.bpmn", fixture(""));
        assertNotEquals(original.fingerprint(), removed.fingerprint());
    }

    @Test
    public void preservesLiteralCallTargetsIncludingWhitespace() throws Exception {
        Inventory inventory = new Inventory();
        inventory.add("fixture", "flow.bpmn", fixture("<b:callActivity id='call' calledElement='QueryJobStatus '/>"));
        assertEquals("QueryJobStatus ", inventory.calls.values().iterator().next());
    }

    @Test
    public void recordsHelperEntryPointsAndLiteralVariableContracts() throws Exception {
        Inventory inventory = new Inventory();
        inventory.add("fixture", "flow.bpmn",
                fixture("<b:scriptTask id='task' scriptFormat='groovy'><b:script>"
                        + "def helper = new ExampleHelper(); helper.run(execution); execution.getVariable('input');"
                        + "execution.setVariableLocal('output', 'done')</b:script></b:scriptTask>"));
        Map<String, Object> contract = inventory.contracts.values().iterator().next();
        assertTrue(((Set<?>) contract.get("entryPoints")).contains("ExampleHelper#run"));
        assertTrue(((Set<?>) contract.get("variableAccess")).contains("getVariable:input"));
        assertTrue(((Set<?>) contract.get("variableAccess")).contains("setVariableLocal:output"));
    }

    @Test
    public void protectedSurfaceWithoutScenarioFailsValidation() throws Exception {
        Inventory inventory = new Inventory();
        inventory.add("fixture", "flow.bpmn", fixture(script("first")));
        String key = inventory.surfaces.firstKey();
        JsonNode manifest = MAPPER.readTree(
                "{\"scenarios\":[],\"protectedSurfaces\":[{\"surface\":\"" + key + "\",\"scenario\":\"missing\"}]}");
        boolean rejected = false;
        try {
            validateProtectedSurfaces(inventory, manifest);
        } catch (AssertionError expected) {
            assertTrue(expected.getMessage().startsWith("Missing scenario"));
            rejected = true;
        }
        assertTrue("Removing a required scenario must fail inventory verification", rejected);
    }

    private static void validateProtectedSurfaces(Inventory inventory, JsonNode manifest) {
        Set<String> scenarios = new TreeSet<>();
        for (JsonNode scenario : manifest.path("scenarios")) {
            assertFalse("Scenario needs an executable test", scenario.path("test").asText().isEmpty());
            assertTrue("Duplicate scenario", scenarios.add(scenario.path("id").asText()));
        }
        Set<String> protectedKeys = new TreeSet<>();
        for (JsonNode mapping : manifest.path("protectedSurfaces")) {
            String key = mapping.path("surface").asText();
            assertTrue("Protected activity disappeared: " + key, inventory.surfaces.containsKey(key));
            assertTrue("Duplicate protected surface: " + key, protectedKeys.add(key));
            assertTrue("Missing scenario for " + key, scenarios.contains(mapping.path("scenario").asText()));
        }
    }

    private static Inventory packagedInventory() throws Exception {
        Inventory inventory = new Inventory();
        Set<String> found = new TreeSet<>();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        for (String entry : classpath.split(Pattern.quote(File.pathSeparator))) {
            Path path = Paths.get(entry);
            for (String module : MODULES) {
                if (Files.isDirectory(path) && path.endsWith(Paths.get(module, "target", "classes"))) {
                    assertTrue("Duplicate production classpath root for " + module, found.add(module));
                    try (Stream<Path> resources = Files.walk(path)) {
                        for (Path resource : (Iterable<Path>) resources.filter(Files::isRegularFile)::iterator) {
                            inventory.add(module, path.relativize(resource).toString().replace('\\', '/'),
                                    Files.readAllBytes(resource));
                        }
                    }
                } else if (path.getFileName() != null && path.getFileName().toString().startsWith(module + "-")
                        && entry.endsWith(".jar") && !entry.endsWith("-tests.jar") && !entry.endsWith("-sources.jar")) {
                    assertTrue("Duplicate production artifact for " + module, found.add(module));
                    try (JarFile jar = new JarFile(path.toFile())) {
                        Enumeration<JarEntry> resources = jar.entries();
                        while (resources.hasMoreElements()) {
                            JarEntry resource = resources.nextElement();
                            if (!resource.isDirectory()) {
                                try (InputStream input = jar.getInputStream(resource)) {
                                    inventory.add(module, resource.getName(), input.readAllBytes());
                                }
                            }
                        }
                    }
                }
            }
        }
        assertEquals("All shipped workflow modules must be present on the test classpath", new TreeSet<>(MODULES),
                found);
        return inventory;
    }

    private static byte[] fixture(String activities) {
        return ("<b:definitions xmlns:b='" + BPMN + "' xmlns:c='" + CAMUNDA + "'><b:process id='process'>" + activities
                + "</b:process></b:definitions>").getBytes(StandardCharsets.UTF_8);
    }

    private static String script(String id) {
        return "<b:scriptTask id='" + id
                + "' scriptFormat='groovy'><b:script>work(execution)</b:script></b:scriptTask>";
    }

    private static String digest(String value) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte part : hash) {
            result.append(String.format("%02x", part & 0xff));
        }
        return result.toString();
    }

    private static final class Inventory {
        private final Set<String> resources = new TreeSet<>();
        private final Map<String, byte[]> productionBytes = new TreeMap<>();
        private final Set<String> processes = new TreeSet<>();
        private final Map<String, String> processOwners = new TreeMap<>();
        private final TreeMap<String, String> surfaces = new TreeMap<>();
        private final Map<String, String> calls = new TreeMap<>();
        private final Map<String, Map<String, Object>> contracts = new TreeMap<>();

        private void add(String module, String resource, byte[] bytes) throws Exception {
            String source = new String(bytes, StandardCharsets.UTF_8);
            if (source.startsWith("\uFEFF")) {
                source = source.substring(1);
            }
            if (!source.contains(BPMN) || !source.stripLeading().startsWith("<")) {
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
            if (!BPMN.equals(document.getDocumentElement().getNamespaceURI())
                    || !"definitions".equals(document.getDocumentElement().getLocalName())) {
                return;
            }
            String resourceKey = module + "/" + resource;
            assertTrue("Duplicate BPMN resource: " + resourceKey, resources.add(resourceKey));
            productionBytes.put(resourceKey, bytes);
            NodeList definitions = document.getElementsByTagNameNS(BPMN, "process");
            for (int i = 0; i < definitions.getLength(); i++) {
                Element process = (Element) definitions.item(i);
                String processId = process.getAttribute("id");
                assertFalse("Process needs an ID: " + resourceKey, processId.isEmpty());
                assertTrue("Duplicate process ID: " + processId, processes.add(processId));
                processOwners.put(processId, resourceKey);
                NodeList elements = process.getElementsByTagName("*");
                for (int j = 0; j < elements.getLength(); j++) {
                    Element element = (Element) elements.item(j);
                    if (BPMN.equals(element.getNamespaceURI()) && "callActivity".equals(element.getLocalName())) {
                        calls.put(resourceKey + "/" + processId + "/" + element.getAttribute("id"),
                                element.getAttribute("calledElement"));
                    }
                    if ("groovy".equalsIgnoreCase(element.getAttribute("scriptFormat"))
                            && (BPMN.equals(element.getNamespaceURI()) || CAMUNDA.equals(element.getNamespaceURI()))) {
                        String key = resourceKey + "/" + processId + "/" + context(element, process);
                        String external = element.getAttributeNS(CAMUNDA, "resource");
                        if (external.isEmpty()) {
                            external = element.getAttribute("resource");
                        }
                        String body = element.getTextContent().replace("\r\n", "\n").trim();
                        assertTrue("Duplicate script context: " + key,
                                surfaces.put(key, digest(body) + ":" + external) == null);
                        contracts.put(key, contract(body, external));
                    }
                }
            }
        }

        private static Map<String, Object> contract(String body, String external) {
            Set<String> entryPoints = new TreeSet<>();
            Set<String> constructedTypes = new TreeSet<>();
            Matcher construction = Pattern.compile("\\b(\\w+)\\s*=\\s*new\\s+([\\w.]+)\\s*\\(").matcher(body);
            while (construction.find()) {
                String type = construction.group(2);
                constructedTypes.add(type);
                Matcher calls =
                        Pattern.compile("\\b" + Pattern.quote(construction.group(1)) + "\\s*\\.\\s*(\\w+)\\s*\\(")
                                .matcher(body);
                while (calls.find()) {
                    entryPoints.add(type + "#" + calls.group(1));
                }
            }
            Matcher direct = Pattern.compile("new\\s+([\\w.]+)\\s*\\([^)]*\\)\\s*\\.\\s*(\\w+)\\s*\\(").matcher(body);
            while (direct.find()) {
                constructedTypes.add(direct.group(1));
                entryPoints.add(direct.group(1) + "#" + direct.group(2));
            }
            Matcher staticCall = Pattern.compile("\\b([A-Z][\\w.]*)\\s*\\.\\s*(\\w+)\\s*\\(").matcher(body);
            while (staticCall.find()) {
                entryPoints.add(staticCall.group(1) + "#" + staticCall.group(2));
            }
            Set<String> imports = new TreeSet<>();
            Matcher imported = Pattern.compile("\\bimport\\s+([\\w.*]+)").matcher(body);
            while (imported.find()) {
                imports.add(imported.group(1));
            }
            Set<String> variables = new TreeSet<>();
            Matcher access = Pattern.compile("execution\\.(getVariable|getVariableLocal|hasVariable|hasVariableLocal|"
                    + "setVariable|setVariableLocal|removeVariable|removeVariableLocal)\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*(?=[,)])")
                    .matcher(body);
            while (access.find()) {
                variables.add(access.group(1) + ":" + access.group(2));
            }
            Map<String, Object> contract = new TreeMap<>();
            contract.put("entryPoints", entryPoints);
            contract.put("imports", imports);
            contract.put("constructedTypes", constructedTypes);
            contract.put("variableAccess", variables);
            contract.put("externalScript", external);
            contract.put("requiresScenario", true);
            contract.put("unresolvedHelperCall", !constructedTypes.isEmpty() && entryPoints.isEmpty());
            return contract;
        }

        private static String context(Element element, Element process) {
            List<String> context = new ArrayList<>();
            Node current = element;
            while (current != process) {
                Element part = (Element) current;
                String label = part.getLocalName();
                if (part.hasAttribute("id")) {
                    context.add(0, part.getAttribute("id"));
                    break;
                }
                if (part.hasAttribute("event")) {
                    label += "[event=" + part.getAttribute("event") + "]";
                }
                if (part.hasAttribute("name")) {
                    label += "[name=" + part.getAttribute("name") + "]";
                }
                int preceding = 0;
                Node sibling = part.getPreviousSibling();
                while (sibling != null) {
                    if (sibling instanceof Element && part.getLocalName().equals(sibling.getLocalName())
                            && part.getNamespaceURI().equals(sibling.getNamespaceURI())) {
                        preceding++;
                    }
                    sibling = sibling.getPreviousSibling();
                }
                label += "[" + preceding + "]";
                context.add(0, label);
                current = current.getParentNode();
            }
            return String.join("/", context);
        }

        private String fingerprint() throws Exception {
            Map<String, Object> shape = new TreeMap<>();
            shape.put("resources", resources);
            shape.put("processes", processOwners);
            shape.put("surfaces", surfaces);
            shape.put("calls", calls);
            return digest(MAPPER.writeValueAsString(shape));
        }

        private Map<String, Object> report() throws Exception {
            Map<String, Object> report = new TreeMap<>();
            report.put("sha256", fingerprint());
            report.put("resources", resources);
            report.put("processes", processOwners);
            report.put("groovySurfaces", surfaces);
            report.put("calls", calls);
            report.put("contracts", contracts);
            Map<String, String> unresolved = new TreeMap<>();
            for (Map.Entry<String, String> call : calls.entrySet()) {
                if (!processes.contains(call.getValue())) {
                    unresolved.put(call.getKey(), call.getValue());
                }
            }
            report.put("unresolvedCalls", unresolved);
            return report;
        }
    }
}
