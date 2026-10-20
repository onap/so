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
import java.io.IOException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void aggregateExecutionPathsAreRelativeToEachModuleBase() throws Exception {
        Path root = Paths.get(System.getProperty("so.repository.root"));
        try (InputStream input = Files.newInputStream(root.resolve("bpmn/groovy-migration-verification/pom.xml"))) {
            String path = XPathFactory.newInstance().newXPath()
                    .evaluate("/project/build/plugins/plugin[artifactId='jacoco-maven-plugin']/executions/"
                            + "execution[id='aggregate-groovy-evidence']/configuration/dataFileIncludes/dataFileInclude",
                            parse(input));
            assertEquals("target/groovy-migration/jacoco.exec", path.trim());
        }
    }

    @Test
    public void legacyVerificationUsesUnpackagedApplicationClasses() throws Exception {
        Path root = Paths.get(System.getProperty("so.repository.root"));
        try (InputStream input = Files.newInputStream(root.resolve("bpmn/mso-infrastructure-bpmn/pom.xml"))) {
            String classes = XPathFactory.newInstance().newXPath().evaluate(
                    "/project/profiles/profile[id='groovy-migration-verification']/build/plugins/"
                            + "plugin[artifactId='maven-failsafe-plugin']/configuration/classesDirectory",
                    parse(input));
            assertEquals("${project.build.outputDirectory}", classes.trim());
        }
    }

    @Test
    public void legacyVerificationIncludesNonconflictingInfrastructureFixtures() throws Exception {
        Path root = Paths.get(System.getProperty("so.repository.root"));
        try (InputStream input = Files.newInputStream(root.resolve("bpmn/mso-infrastructure-bpmn/pom.xml"))) {
            Document pom = parse(input);
            String artifact = "/project/profiles/profile[id='groovy-migration-verification']/build/plugins/"
                    + "plugin[artifactId='maven-dependency-plugin']/executions/"
                    + "execution[id='unpack-shared-test-fixtures']/configuration/artifactItems/"
                    + "artifactItem[artifactId='so-bpmn-infrastructure-common']/";
            assertEquals("__files/GenericFlows/**,__files/VCPE/**,__files/VfModularity/**",
                    XPathFactory.newInstance().newXPath().evaluate(artifact + "includes", pom).trim());
            assertEquals("${project.build.testOutputDirectory}",
                    XPathFactory.newInstance().newXPath().evaluate(artifact + "outputDirectory", pom).trim());
            assertEquals("test-jar", XPathFactory.newInstance().newXPath().evaluate(artifact + "type", pom).trim());
        }
    }

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
        Map<String, List<String>> databaseDirectories = new TreeMap<>();
        Map<String, Type> types = compiledTests(root);
        Map<String, List<Map<String, Object>>> modules = new TreeMap<>();
        Map<String, Map<String, List<Map<String, Object>>>> executed = new TreeMap<>();
        ExecFileLoader execution = new ExecFileLoader();
        for (String module : MODULES) {
            Path directory = root.resolve("bpmn").resolve(module);
            List<String> remainingDatabases = remainingTestDatabases(directory);
            databaseDirectories.put(module, remainingDatabases);
            if (!remainingDatabases.isEmpty()) {
                blockers.add(module + ": test database resources remain: " + remainingDatabases);
            }
            for (String diagnostic : shutdownDiagnostics(directory)) {
                shutdownDiagnostics.add(module + "/" + diagnostic);
            }
            Map<String, List<Map<String, Object>>> reports = testReports(directory);
            Set<String> compiledSources = new TreeSet<>();
            for (Type type : types.values()) {
                if (module.equals(type.module) && type.source != null) {
                    compiledSources.add(sourceKey(type));
                }
            }
            for (String missing : uncompiledTestSources(directory, compiledSources)) {
                blockers.add(module + ": current test source has no compiled class: " + missing);
            }
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
                Set<String> intendedMethods = testMethods(type, types);
                entry.put("intendedMethods", intendedMethods);
                entry.put("testCases", cases == null ? new ArrayList<>() : cases);
                discovered.add(entry);
                if (cases == null || cases.isEmpty()) {
                    blockers.add(module + ": intended test was not reported: " + name);
                } else {
                    for (String missing : missingMethods(intendedMethods, cases, type.ignored)) {
                        blockers.add(module + ": intended test method was not reported: " + name + "#" + missing);
                    }
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
            Path data = directory.resolve("target/groovy-migration/jacoco.exec");
            if (!Files.isRegularFile(data) || Files.size(data) == 0) {
                blockers.add(module + ": missing coverage execution data");
            } else {
                execution.load(data.toFile());
            }
        }
        evidence.put("discovery", modules);
        evidence.put("remainingTestDatabaseDirectories", databaseDirectories);
        for (String module : MODULES) {
            Path classes = root.resolve("bpmn").resolve(module).resolve("target/test-classes");
            if (Files.isDirectory(classes)) {
                CoverageBuilder tests = new CoverageBuilder();
                new Analyzer(execution.getExecutionDataStore(), tests).analyzeAll(classes.toFile());
                for (IClassCoverage mismatch : tests.getNoMatchClasses()) {
                    blockers.add(module + ": execution data does not match test bytecode: " + mismatch.getName());
                }
            }
        }
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
        evidence.put("coverageSlices", GroovySliceCoverage.verify(scenarios, MAPPER.valueToTree(coverage), blockers));
        evidence.put("shutdownDiagnostics", shutdownDiagnostics);
        if (!shutdownDiagnostics.isEmpty()) {
            blockers.add("Test JVM shutdown timed out: " + shutdownDiagnostics);
        }
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
    public void aReportedClassDoesNotHideAnUnreportedTestMethod() throws Exception {
        Map<String, List<Map<String, Object>>> results = new TreeMap<>();
        collectCases(parse(
                new ByteArrayInputStream("<testsuite><testcase classname='example.Test' name='first'/></testsuite>"
                        .getBytes(StandardCharsets.UTF_8))),
                results);

        assertEquals(new TreeSet<>(Arrays.asList("second")),
                missingMethods(new TreeSet<>(Arrays.asList("first", "second")), results.get("example.Test"), false));
    }

    @Test
    public void parameterizedResultsMatchTheirMethodWithoutMatchingAnotherMethodPrefix() throws Exception {
        Map<String, List<Map<String, Object>>> results = new TreeMap<>();
        String xml = "<testsuite><testcase classname='example.Test' name='first[0: false]'/>"
                + "<testcase classname='example.Test' name='second(String)[1]'/></testsuite>";
        collectCases(parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))), results);

        assertEquals(new TreeSet<>(Arrays.asList("fir")), missingMethods(
                new TreeSet<>(Arrays.asList("first", "second", "fir")), results.get("example.Test"), false));
    }

    @Test
    public void anIgnoredClassCanHaveOneClassLevelSkipButAnIgnoredMethodMustStillBeReported() throws Exception {
        Map<String, List<Map<String, Object>>> results = new TreeMap<>();
        String xml = "<testsuite><testcase classname='example.Test' name='example.Test'>"
                + "<skipped message='Known defect'/></testcase></testsuite>";
        collectCases(parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))), results);
        Set<String> expected = new TreeSet<>(Arrays.asList("first", "second"));

        assertTrue(missingMethods(expected, results.get("example.Test"), true).isEmpty());
        assertEquals(expected, missingMethods(expected, results.get("example.Test"), false));
    }

    @Test
    public void inheritedAnnotatedMethodsAreDiscoveredAndLegacyMethodsRequireTestCaseAncestry() {
        Type parent = new Type("module");
        parent.name = "example/BaseTest";
        parent.parent = "java/lang/Object";
        parent.annotatedMethods.add("annotated");
        parent.legacyMethods.add("testLegacy");
        Type child = new Type("module");
        child.name = "example/ChildTest";
        child.parent = parent.name;
        child.annotatedMethods.add("local");
        Map<String, Type> types = new HashMap<>();
        types.put("module/" + parent.name, parent);

        assertEquals(new TreeSet<>(Arrays.asList("annotated", "local")), testMethods(child, types));
        parent.parent = "junit/framework/TestCase";
        assertEquals(new TreeSet<>(Arrays.asList("annotated", "local", "testLegacy")), testMethods(child, types));
        parent.annotatedMethods.clear();
        parent.legacyMethods.clear();
        child.annotatedMethods.clear();
        child.legacyMethods.add("testInGrandchild");
        assertTrue(hasTests(child, types, new TreeSet<>()));
    }

    @Test
    public void bytecodeVisitorRecordsAnnotatedAndLegacyMethodNames() throws Exception {
        Type type = new Type("module");
        try (InputStream input = MethodDiscoveryFixture.class
                .getResourceAsStream("GroovyVerificationEvidenceTest$MethodDiscoveryFixture.class")) {
            new ClassReader(input).accept(type, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
        }

        assertEquals(new TreeSet<>(Arrays.asList("annotated")), testMethods(type, new TreeMap<>()));
        assertEquals(new TreeSet<>(Arrays.asList("testLegacy")), type.legacyMethods);
    }

    @Test
    public void aNewTestSourceCannotDisappearBecauseItWasNeverCompiled() throws Exception {
        Path directory = temporary.newFolder("module").toPath();
        Path source = directory.resolve("src/test/java/example/NewTest.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "package example; public class NewTest {}", StandardCharsets.UTF_8);

        assertEquals(Arrays.asList("src/test/java/example/NewTest.java"),
                uncompiledTestSources(directory, new TreeSet<>()));
        assertTrue(uncompiledTestSources(directory, new TreeSet<>(Arrays.asList("example/NewTest.java"))).isEmpty());
    }

    @Test
    public void ownedDatabaseDirectoriesMustBeEmptyAfterTestForksExit() throws Exception {
        Path module = temporary.newFolder("module").toPath();
        assertTrue(remainingTestDatabases(module).isEmpty());
        Path databases = module.resolve("target/groovy-migration-mariadb");
        Files.createDirectories(databases);
        assertTrue(remainingTestDatabases(module).isEmpty());

        Files.createDirectories(databases.resolve("database-abandoned/data"));

        assertEquals(Arrays.asList("database-abandoned"), remainingTestDatabases(module));
    }

    @Test
    public void shutdownTimeoutsAreDetectedInSurefireAndFailsafeReports() throws Exception {
        Path module = temporary.newFolder("module").toPath();
        assertTrue(shutdownDiagnostics(module).isEmpty());
        Path surefire = Files.createDirectories(module.resolve("target/surefire-reports"));
        Path failsafe = Files.createDirectories(module.resolve("target/failsafe-reports"));
        Files.writeString(surefire.resolve("fork.dump"), "Thread dump after 30 seconds shutdown timeout");
        Files.writeString(failsafe.resolve("fork.dumpstream"),
                "Surefire is going to kill self fork JVM. The exit has elapsed 30 seconds after System.exit(0).");
        Files.writeString(failsafe.resolve("other.dump"), "A different diagnostic");

        assertEquals(Arrays.asList("surefire-reports/fork.dump", "failsafe-reports/fork.dumpstream"),
                shutdownDiagnostics(module));
    }

    static List<String> remainingTestDatabases(Path directory) throws IOException {
        Path databases = directory.resolve("target/groovy-migration-mariadb");
        if (!Files.isDirectory(databases)) {
            return new ArrayList<>();
        }
        try (Stream<Path> entries = Files.list(databases)) {
            return entries.map(path -> path.getFileName().toString()).sorted().collect(Collectors.toList());
        }
    }

    static List<String> shutdownDiagnostics(Path directory) throws IOException {
        List<String> result = new ArrayList<>();
        for (String reports : Arrays.asList("surefire-reports", "failsafe-reports")) {
            Path path = directory.resolve("target").resolve(reports);
            if (!Files.isDirectory(path)) {
                continue;
            }
            try (Stream<Path> entries = Files.list(path)) {
                for (Path diagnostic : (Iterable<Path>) entries
                        .filter(file -> file.toString().endsWith(".dump") || file.toString().endsWith(".dumpstream"))
                        .sorted()::iterator) {
                    String content = Files.readString(diagnostic);
                    if (content.contains("shutdown timeout")
                            || (content.contains("kill self fork JVM") && content.contains("exit has elapsed"))) {
                        result.add(reports + "/" + diagnostic.getFileName());
                    }
                }
            }
        }
        return result;
    }

    @Test
    public void compiledSourceMappingUsesTheGroovySourceNameWhenTheDeclaredClassNameDiffers() throws Exception {
        Path directory = temporary.newFolder("module").toPath();
        Path source = directory.resolve("src/test/groovy/example/ScaleCustomTest.groovy");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "package example; class SacleCustomTest {}", StandardCharsets.UTF_8);
        Type type = new Type("module");
        type.name = "example/SacleCustomTest";
        type.source = "ScaleCustomTest.groovy";

        assertTrue(uncompiledTestSources(directory, new TreeSet<>(Arrays.asList(sourceKey(type)))).isEmpty());
    }

    public static class MethodDiscoveryFixture {
        @Test
        public void annotated() {}

        public void testLegacy() {}

        public static void testStaticHelper() {}
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

    private static Set<String> missingMethods(Set<String> intended, List<Map<String, Object>> cases,
            boolean ignoredClass) {
        Set<String> missing = new TreeSet<>(intended);
        if (ignoredClass && !cases.isEmpty() && cases.stream().allMatch(test -> "skipped".equals(test.get("status")))) {
            return new TreeSet<>();
        }
        for (Map<String, Object> test : cases) {
            String reported = String.valueOf(test.get("name"));
            missing.removeIf(method -> reported.equals(method) || reported.startsWith(method + "[")
                    || reported.startsWith(method + "("));
        }
        return missing;
    }

    private static String sourceKey(Type type) {
        return type.name.substring(0, type.name.lastIndexOf('/') + 1) + type.source;
    }

    private static List<String> uncompiledTestSources(Path module, Set<String> compiledSources) throws Exception {
        List<String> missing = new ArrayList<>();
        for (String language : Arrays.asList("java", "groovy")) {
            Path sources = module.resolve("src/test/" + language);
            if (!Files.isDirectory(sources)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(sources)) {
                for (Path source : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                    String name = source.getFileName().toString();
                    if ((name.endsWith(".java") || name.endsWith(".groovy")) && !"package-info.java".equals(name)
                            && !"module-info.java".equals(name)
                            && !compiledSources.contains(sources.relativize(source).toString().replace('\\', '/'))) {
                        missing.add(module.relativize(source).toString().replace('\\', '/'));
                    }
                }
            }
        }
        return missing;
    }

    private static Set<String> testMethods(Type type, Map<String, Type> types) {
        Set<String> annotated = new TreeSet<>();
        Set<String> legacy = new TreeSet<>();
        Set<String> visited = new TreeSet<>();
        while (type != null && visited.add(type.module + "/" + type.name)) {
            annotated.addAll(type.annotatedMethods);
            legacy.addAll(type.legacyMethods);
            if ("junit/framework/TestCase".equals(type.parent)) {
                annotated.addAll(legacy);
            }
            Type parent = types.get(type.module + "/" + type.parent);
            if (parent == null) {
                parent = types.get("MSOCommonBPMN/" + type.parent);
            }
            type = parent;
        }
        return annotated;
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
        if (type.annotatedTests || !testMethods(type, types).isEmpty()) {
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
        private boolean ignored;
        private final Set<String> annotatedMethods = new TreeSet<>();
        private final Set<String> legacyMethods = new TreeSet<>();

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
            if (method.startsWith("test") && "()V".equals(descriptor) && (flags & Opcodes.ACC_PUBLIC) != 0
                    && (flags & Opcodes.ACC_STATIC) == 0) {
                legacyMethods.add(method);
            }
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                    if (Arrays.asList("Lorg/junit/Test;", "Lorg/junit/jupiter/api/Test;",
                            "Lorg/junit/jupiter/params/ParameterizedTest;", "Lorg/junit/jupiter/api/RepeatedTest;",
                            "Lorg/junit/jupiter/api/TestFactory;", "Lorg/junit/jupiter/api/TestTemplate;")
                            .contains(annotation)) {
                        annotatedTests = true;
                        annotatedMethods.add(method);
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
