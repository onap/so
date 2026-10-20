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

package org.onap.so.bpmn.verification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GroovyVerificationEvidenceTest {
    private static final List<String> MODULES = Arrays.asList("MSOCommonBPMN", "so-bpmn-infrastructure-common",
            "so-bpmn-building-blocks", "so-bpmn-infrastructure-flows", "mso-infrastructure-bpmn");
    private static final List<String> HELPERS = MODULES.subList(0, 2);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void collectAndVerifyEvidence() throws Exception {
        Path root = Paths.get(System.getProperty("so.repository.root"));
        Path output = Paths.get("target", "groovy-migration", "verification-evidence.json");
        Files.createDirectories(output.getParent());
        Map<String, Object> evidence = new TreeMap<>();
        evidence.put("revision", git(root, "rev-parse", "HEAD").trim());
        evidence.put("sourceTreeSha256", sourceFingerprint(root));
        evidence.put("sourceFingerprintMethod", "git-head-binary-diff-and-untracked-bytes-v1");
        evidence.put("jdk", System.getProperty("java.runtime.version"));
        evidence.put("maven", System.getProperty("so.maven.version"));
        evidence.put("generatedAt", Instant.now().toString());
        List<String> blockers = new ArrayList<>();
        List<String> shutdownDiagnostics = new ArrayList<>();
        Map<String, Type> types = compiledTests(root);
        Map<String, List<Map<String, Object>>> modules = new TreeMap<>();
        Map<String, Map<String, List<Map<String, Object>>>> executed = new TreeMap<>();
        ExecFileLoader execution = new ExecFileLoader();
        for (String module : MODULES) {
            Path directory = root.resolve("bpmn").resolve(module);
            Map<String, List<Map<String, Object>>> reports = testReports(directory);
            executed.put(module, reports);
            List<Map<String, Object>> discovered = new ArrayList<>();
            for (Type type : types.values()) {
                if (!type.module.equals(module) || !type.isCandidate() || !hasTests(type, types, new TreeSet<>())) {
                    continue;
                }
                String name = type.name.replace('/', '.');
                String sourcePackage = type.name.substring(0, type.name.lastIndexOf('/') + 1);
                boolean sourcePresent = type.source != null && (Files
                        .isRegularFile(directory.resolve("src/test/java/" + sourcePackage + type.source))
                        || Files.isRegularFile(directory.resolve("src/test/groovy/" + sourcePackage + type.source)));
                if (!sourcePresent) {
                    blockers.add(module + ": compiled test has no current source file: " + name);
                }
                List<Map<String, Object>> cases = reports.get(name);
                Map<String, Object> entry = new TreeMap<>();
                entry.put("class", name);
                entry.put("sourceFile", type.source);
                entry.put("ignoredClass", type.ignored);
                entry.put("testCases", cases == null ? new ArrayList<>() : cases);
                discovered.add(entry);
                if (cases == null || cases.isEmpty()) {
                    blockers.add(module + ": intended test was not reported: " + name);
                } else {
                    for (Map<String, Object> test : cases) {
                        if ("failed".equals(test.get("status")) || "retry-rescued".equals(test.get("status"))) {
                            blockers.add(
                                    module + ": " + test.get("status") + " test: " + name + "#" + test.get("name"));
                        }
                    }
                }
            }
            if (discovered.isEmpty()) {
                blockers.add(module + ": no compiled test classes discovered");
            }
            modules.put(module, discovered);
            Path reportsDirectory = directory.resolve("target/surefire-reports");
            if (Files.isDirectory(reportsDirectory)) {
                try (Stream<Path> diagnostics = Files.list(reportsDirectory)) {
                    for (Path diagnostic : (Iterable<Path>) diagnostics
                            .filter(path -> path.toString().endsWith(".dump"))::iterator) {
                        if (Files.readString(diagnostic).contains("shutdown timeout")) {
                            shutdownDiagnostics.add(module + "/" + diagnostic.getFileName());
                        }
                    }
                }
            }
            Path data = directory.resolve("target/groovy-migration/jacoco.exec");
            if (!Files.isRegularFile(data) || Files.size(data) == 0) {
                blockers.add(module + ": missing coverage execution data");
            } else {
                execution.load(data.toFile());
            }
        }
        evidence.put("discovery", modules);
        Path scenarioManifest =
                root.resolve("bpmn/so-bpmn-infrastructure-flows/src/test/resources/groovy-migration-scenarios.json");
        JsonNode scenarios = MAPPER.readTree(scenarioManifest.toFile());
        verifyScenarios(scenarios, executed, blockers);
        evidence.put("scenarios", scenarios.path("scenarios"));
        Map<String, Set<Long>> classIds = new TreeMap<>();
        execution.getExecutionDataStore().getContents()
                .forEach(data -> classIds.computeIfAbsent(data.getName(), key -> new TreeSet<>()).add(data.getId()));
        Map<String, Object> coverage = new TreeMap<>();
        int coveredLines = 0;
        int missedLines = 0;
        for (String helper : HELPERS) {
            CoverageBuilder builder = new CoverageBuilder();
            Path classes = root.resolve("bpmn").resolve(helper).resolve("target/classes");
            if (!Files.isDirectory(classes)) {
                blockers.add(helper + ": missing production bytecode");
                continue;
            }
            int analyzed = new Analyzer(execution.getExecutionDataStore(), builder).analyzeAll(classes.toFile());
            if (analyzed == 0) {
                blockers.add(helper + ": zero analyzed production classes");
            }
            for (IClassCoverage mismatch : builder.getNoMatchClasses()) {
                blockers.add(helper + ": execution data does not match bytecode: " + mismatch.getName());
            }
            List<String> uncoveredMethods = new ArrayList<>();
            for (IClassCoverage type : builder.getClasses()) {
                if (classIds.containsKey(type.getName()) && classIds.get(type.getName()).size() > 1) {
                    blockers.add(helper + ": multiple bytecode versions were executed: " + type.getName());
                }
                if (type.getSourceFileName() != null && type.getSourceFileName().endsWith(".groovy")) {
                    type.getMethods().stream().filter(method -> method.getInstructionCounter().getCoveredCount() == 0)
                            .forEach(method -> uncoveredMethods
                                    .add(type.getName() + "#" + method.getName() + method.getDesc()));
                }
            }
            Map<String, Object> sources = new TreeMap<>();
            builder.getSourceFiles().stream().filter(source -> source.getName().endsWith(".groovy")).forEach(source -> {
                Map<String, Object> counters = new TreeMap<>();
                counters.put("lineCovered", source.getLineCounter().getCoveredCount());
                counters.put("lineMissed", source.getLineCounter().getMissedCount());
                counters.put("branchCovered", source.getBranchCounter().getCoveredCount());
                counters.put("branchMissed", source.getBranchCounter().getMissedCount());
                sources.put(source.getPackageName() + "/" + source.getName(), counters);
            });
            for (ISourceFileCoverage source : builder.getSourceFiles()) {
                if (source.getName().endsWith(".groovy")) {
                    coveredLines += source.getLineCounter().getCoveredCount();
                    missedLines += source.getLineCounter().getMissedCount();
                }
            }
            if (sources.isEmpty()) {
                blockers.add(helper + ": no Groovy source coverage counters");
            }
            Path groovySources = root.resolve("bpmn").resolve(helper).resolve("src/main/groovy");
            try (Stream<Path> files = Files.walk(groovySources)) {
                for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".groovy"))::iterator) {
                    Matcher packageName =
                            Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)").matcher(Files.readString(file));
                    String source = (packageName.find() ? packageName.group(1).replace('.', '/') + "/" : "")
                            + file.getFileName();
                    if (!sources.containsKey(source)) {
                        blockers.add(helper + ": Groovy source is absent from coverage: " + source);
                    }
                }
            }
            Map<String, Object> helperEvidence = new TreeMap<>();
            helperEvidence.put("sources", sources);
            helperEvidence.put("uncoveredMethods", uncoveredMethods);
            coverage.put(helper, helperEvidence);
        }
        evidence.put("groovySourceCoverage", coverage);
        evidence.put("shutdownDiagnostics", shutdownDiagnostics);
        String probe = System.getProperty("groovy.migration.probeMinimumLineCoverage");
        if (probe != null) {
            double minimum = Double.parseDouble(probe);
            evidence.put("coverageProbeMinimum", minimum);
            if (!Double.isFinite(minimum) || minimum < 0 || minimum > 1
                    || !meetsCoverage(coveredLines, missedLines, minimum)) {
                blockers.add("Coverage probe failed: " + coveredLines + " covered / " + (coveredLines + missedLines)
                        + " Groovy source lines; requested minimum " + minimum);
            }
        }
        Path aggregate = Paths.get("target/site/groovy-migration-aggregate/jacoco.xml");
        if (!Files.isRegularFile(aggregate)) {
            blockers.add("Missing aggregate XML report; include this module and all evidence inputs in the reactor");
        } else {
            try (InputStream input = Files.newInputStream(aggregate)) {
                if (parse(input).getElementsByTagName("class").getLength() == 0) {
                    blockers.add("Aggregate XML report contains zero analyzed classes");
                }
            }
        }
        evidence.put("blockers", blockers);
        evidence.put("baselineComplete", blockers.isEmpty());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), evidence);
        assertTrue("Incomplete verification evidence; inspect " + output.toAbsolutePath() + ": " + blockers,
                blockers.isEmpty());
    }

    @Test
    public void discoversDeclaredGroovyClassNameRatherThanSourceFilename() {
        Type type = new Type("module");
        type.name = "example/SacleCustomE2EServiceInstanceTest";
        type.source = "ScaleCustomE2EServiceInstanceTest.groovy";
        type.annotatedTests = true;
        assertTrue(type.isCandidate());
        assertTrue(hasTests(type, new HashMap<>(), new TreeSet<>()));
    }

    @Test
    public void excludesAbstractTestSupportAndIncludesInheritedTests() {
        Type parent = new Type("module");
        parent.name = "example/BaseTest";
        parent.access = Opcodes.ACC_ABSTRACT;
        parent.annotatedTests = true;
        Type child = new Type("module");
        child.name = "example/ConcreteTest";
        child.parent = parent.name;
        Map<String, Type> types = new HashMap<>();
        types.put(parent.module + "/" + parent.name, parent);
        assertFalse(parent.isCandidate());
        assertTrue(child.isCandidate());
        assertTrue(hasTests(child, types, new TreeSet<>()));
    }

    @Test
    public void readsSkippedAndRetryRescuedResultsWithoutHidingThem() throws Exception {
        String xml =
                "<testsuite><testcase classname='example.Test' name='skipped'><skipped message='reason'/></testcase>"
                        + "<testcase classname='example.Test' name='flaky'><flakyFailure/></testcase></testsuite>";
        Map<String, List<Map<String, Object>>> results = new TreeMap<>();
        collectCases(parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))), results);
        assertEquals("skipped", results.get("example.Test").get(0).get("status"));
        assertEquals("retry-rescued", results.get("example.Test").get(1).get("status"));
    }

    @Test
    public void coverageMinimumRejectsAnUnmetThresholdAndEmptyData() {
        assertFalse(meetsCoverage(1, 1, 1.0));
        assertTrue(meetsCoverage(3, 1, 0.75));
        assertFalse(meetsCoverage(0, 0, 0));
    }

    @Test
    public void requiredScenarioMustExecuteSuccessfullyWithoutRetries() throws Exception {
        JsonNode manifest = MAPPER.readTree("{\"scenarios\":[{\"id\":\"error-route\",\"module\":\"flows\","
                + "\"test\":\"example.FlowTest#handlesError\"}]}");
        Map<String, Map<String, List<Map<String, Object>>>> executed = new TreeMap<>();
        Map<String, List<Map<String, Object>>> reports = new TreeMap<>();
        executed.put("flows", reports);
        List<String> blockers = new ArrayList<>();
        verifyScenarios(manifest, executed, blockers);
        assertEquals(1, blockers.size());
        for (String status : Arrays.asList("skipped", "failed", "retry-rescued", "passed")) {
            Map<String, Object> test = new TreeMap<>();
            test.put("name", "handlesError");
            test.put("status", status);
            reports.put("example.FlowTest", Arrays.asList(test));
            blockers.clear();
            verifyScenarios(manifest, executed, blockers);
            assertEquals(status, "passed".equals(status) ? 0 : 1, blockers.size());
        }
    }

    private static void verifyScenarios(JsonNode manifest, Map<String, Map<String, List<Map<String, Object>>>> executed,
            List<String> blockers) {
        Set<String> identifiers = new TreeSet<>();
        for (JsonNode scenario : manifest.path("scenarios")) {
            String id = scenario.path("id").asText();
            String module = scenario.path("module").asText();
            String[] selector = scenario.path("test").asText().split("#", -1);
            if (id.isEmpty() || !identifiers.add(id) || selector.length != 2 || selector[0].isEmpty()
                    || selector[1].isEmpty() || !executed.containsKey(module)) {
                blockers.add("Invalid required scenario: " + scenario);
                continue;
            }
            List<Map<String, Object>> cases = executed.get(module).get(selector[0]);
            boolean passed = cases != null && cases.stream()
                    .anyMatch(test -> selector[1].equals(test.get("name")) && "passed".equals(test.get("status")));
            boolean unsuccessful = cases != null && cases.stream()
                    .anyMatch(test -> selector[1].equals(test.get("name")) && !"passed".equals(test.get("status")));
            if (!passed || unsuccessful) {
                blockers.add("Required scenario did not pass without retries: " + id + " (" + module + ":"
                        + scenario.path("test").asText() + ")");
            }
        }
    }

    private static boolean meetsCoverage(int covered, int missed, double minimum) {
        return covered + missed > 0 && (double) covered / (covered + missed) >= minimum;
    }

    private static Map<String, Type> compiledTests(Path root) throws Exception {
        Map<String, Type> types = new TreeMap<>();
        for (String module : MODULES) {
            Path directory = root.resolve("bpmn").resolve(module).resolve("target/test-classes");
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(directory)) {
                for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".class"))::iterator) {
                    Type type = new Type(module);
                    new ClassReader(Files.readAllBytes(file)).accept(type,
                            ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                    types.put(module + "/" + type.name, type);
                }
            }
        }
        return types;
    }

    private static boolean hasTests(Type type, Map<String, Type> types, Set<String> visited) {
        if (!visited.add(type.module + "/" + type.name)) {
            return false;
        }
        if (type.annotatedTests || type.testCaseMethod && "junit/framework/TestCase".equals(type.parent)) {
            return true;
        }
        Type parent = types.get(type.module + "/" + type.parent);
        if (parent == null) {
            parent = types.get("MSOCommonBPMN/" + type.parent);
        }
        return parent != null && hasTests(parent, types, visited);
    }

    private static Map<String, List<Map<String, Object>>> testReports(Path module) throws Exception {
        Map<String, List<Map<String, Object>>> reports = new TreeMap<>();
        for (String kind : Arrays.asList("surefire-reports", "failsafe-reports")) {
            Path directory = module.resolve("target").resolve(kind);
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> files = Files.list(directory)) {
                for (Path file : (Iterable<Path>) files.filter(path -> path.getFileName().toString().startsWith("TEST-")
                        && path.toString().endsWith(".xml"))::iterator) {
                    try (InputStream input = Files.newInputStream(file)) {
                        collectCases(parse(input), reports);
                    }
                }
            }
        }
        return reports;
    }

    private static void collectCases(Document document, Map<String, List<Map<String, Object>>> reports) {
        NodeList cases = document.getElementsByTagName("testcase");
        for (int i = 0; i < cases.getLength(); i++) {
            Element test = (Element) cases.item(i);
            String status = "passed";
            if (test.getElementsByTagName("skipped").getLength() > 0) {
                status = "skipped";
            } else if (test.getElementsByTagName("failure").getLength() > 0
                    || test.getElementsByTagName("error").getLength() > 0) {
                status = "failed";
            } else if (test.getElementsByTagName("flakyFailure").getLength() > 0
                    || test.getElementsByTagName("flakyError").getLength() > 0
                    || test.getElementsByTagName("rerunFailure").getLength() > 0
                    || test.getElementsByTagName("rerunError").getLength() > 0) {
                status = "retry-rescued";
            }
            Map<String, Object> result = new TreeMap<>();
            result.put("name", test.getAttribute("name"));
            result.put("status", status);
            if ("skipped".equals(status)) {
                Element skipped = (Element) test.getElementsByTagName("skipped").item(0);
                String reason = skipped.getAttribute("message");
                result.put("reason", reason.isEmpty() ? skipped.getTextContent().trim() : reason);
            }
            reports.computeIfAbsent(test.getAttribute("classname"), key -> new ArrayList<>()).add(result);
        }
    }

    private static Document parse(InputStream input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(input);
    }

    private static String sourceFingerprint(Path root) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(git(root, "rev-parse", "HEAD").getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        digest.update(git(root, "diff", "--binary", "--no-ext-diff", "--no-textconv", "HEAD", "--")
                .getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        Set<String> files = new TreeSet<>(
                Arrays.asList(git(root, "ls-files", "--others", "--exclude-standard", "-z").split("\u0000")));
        for (String file : files) {
            Path path = root.resolve(file);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            digest.update(file.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(Files.readAllBytes(path));
            digest.update((byte) 0);
        }
        StringBuilder result = new StringBuilder();
        for (byte part : digest.digest()) {
            result.append(String.format("%02x", part & 0xff));
        }
        return result.toString();
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("git", "-C", root.toString()));
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("Cannot record source provenance: " + output, 0, process.waitFor());
        return output;
    }

    private static final class Type extends ClassVisitor {
        private final String module;
        private String name;
        private String parent;
        private String source;
        private int access;
        private boolean annotatedTests;
        private boolean testCaseMethod;
        private boolean ignored;

        private Type(String module) {
            super(Opcodes.ASM9);
            this.module = module;
        }

        @Override
        public void visit(int version, int flags, String className, String signature, String superName,
                String[] interfaces) {
            name = className;
            parent = superName;
            access = flags;
        }

        @Override
        public void visitSource(String sourceName, String debug) {
            source = sourceName;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if ("Lorg/junit/Ignore;".equals(descriptor) || "Lorg/junit/jupiter/api/Disabled;".equals(descriptor)) {
                ignored = true;
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int flags, String method, String descriptor, String signature,
                String[] exceptions) {
            if (method.startsWith("test") && "()V".equals(descriptor) && (flags & Opcodes.ACC_PUBLIC) != 0) {
                testCaseMethod = true;
            }
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                    if (Arrays.asList("Lorg/junit/Test;", "Lorg/junit/jupiter/api/Test;",
                            "Lorg/junit/jupiter/params/ParameterizedTest;", "Lorg/junit/jupiter/api/RepeatedTest;",
                            "Lorg/junit/jupiter/api/TestFactory;", "Lorg/junit/jupiter/api/TestTemplate;")
                            .contains(annotation)) {
                        annotatedTests = true;
                    }
                    return null;
                }
            };
        }

        private boolean isCandidate() {
            return name != null && (name.endsWith("Test") || name.endsWith("IT")) && !name.contains("$")
                    && (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) == 0;
        }
    }
}
