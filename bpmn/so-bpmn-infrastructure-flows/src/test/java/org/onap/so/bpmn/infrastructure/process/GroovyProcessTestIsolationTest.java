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

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GroovyProcessTestIsolationTest {
    private static final List<String> MODULES =
            List.of("MSOCommonBPMN", "so-bpmn-infrastructure-flows", "mso-infrastructure-bpmn");
    // Keep this reviewed set explicit so moving an automatic test behind a profile fails the check.
    private static final Set<String> PROFILE_ONLY =
            Set.of("org.onap.so.bpmn.common.verification.WorkflowMessagePersistenceTest",
                    "org.onap.so.bpmn.common.verification.SdncAdapterProcessPersistenceTest",
                    "org.onap.so.bpmn.infrastructure.process.OofWorkflowPersistenceTest",
                    "org.onap.so.bpmn.core.plugins.WorkflowExceptionPluginProcessTest");
    private static final Pattern LITERALS_AND_COMMENTS =
            Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|//[^\\r\\n]*|/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern TYPE =
            Pattern.compile("public\\s+((?:(?:abstract|final)\\s+)*)class\\s+(\\w+)(?:\\s+extends\\s+([\\w.]+))?");
    private static final Pattern PACKAGE = Pattern.compile("\\bpackage\\s+([\\w.]+)\\s*;");
    private static final Pattern CLOCK = Pattern.compile("\\bClockUtil\\s*\\.\\s*(?:setCurrentTime|reset)\\s*\\(");

    @Test
    public void clockControlledTestsKeepTheirBuildScopesAndUseFreshForks() throws Exception {
        Path classes = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        Path moduleDirectory = Paths.get(System.getProperty("basedir", classes.getParent().getParent().toString()));
        Path root = moduleDirectory.getParent().getParent();
        Map<String, String> sources = new HashMap<>();
        Map<String, Bindings> bindings = new HashMap<>();
        // The app depends on flows; source scanning avoids a reverse dependency on the app's test jar.
        for (String module : MODULES) {
            Path directory = root.resolve("bpmn").resolve(module);
            assertTrue("Missing module POM " + directory, Files.isRegularFile(directory.resolve("pom.xml")));
            Path sourceRoot = directory.resolve("src/test/java");
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                for (Path path : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    sources.put(module + "/src/test/java/" + sourceRoot.relativize(path).toString().replace('\\', '/'),
                            Files.readString(path));
                }
            }
            bindings.put(module, bindings(Files.readString(directory.resolve("pom.xml"))));
        }
        List<TestSource> controlled = clockControlled(sources);
        assertFalse("Clock-controlled process tests disappeared", controlled.isEmpty());
        for (TestSource source : controlled) {
            verify(source, bindings.get(source.module), PROFILE_ONLY);
        }
    }

    @Test
    public void inheritedClockUseResolvesImportsAndIgnoresSourceExamples() {
        Map<String, String> sources = fixtureSources();
        sources.put("fixture/src/test/java/suite/ExamplesTest.java",
                "package suite; public class ExamplesTest { @Test public void example() {"
                        + "String value = \"ClockUtil.setCurrentTime(null)\"; } }");
        List<TestSource> controlled = clockControlled(sources);
        assertEquals(1, controlled.size());
        assertEquals("suite.ChildTest", controlled.get(0).name);
    }

    @Test
    public void defaultSelectionAndReusedForksFailTheIsolationCheck() throws Exception {
        TestSource source = clockControlled(fixtureSources()).get(0);
        assertThrows(AssertionError.class, () -> verify(source, bindings(pom(false, true, false, true)), Set.of()));
        AssertionError failure = assertThrows(AssertionError.class,
                () -> verify(source, bindings(pom(true, true, false, false)), Set.of()));
        assertTrue(failure.getMessage(), failure.getMessage().contains("reuseForks"));
        verify(source, bindings(pom(true, true, false, true)), Set.of());
    }

    @Test
    public void aProfileCannotSilentlyRemoveAnAutomaticTestOrRunItTwice() throws Exception {
        TestSource source = clockControlled(fixtureSources()).get(0);
        AssertionError hidden = assertThrows(AssertionError.class,
                () -> verify(source, bindings(pom(true, false, true, true)), Set.of()));
        assertTrue(hidden.getMessage(), hidden.getMessage().contains("regular build"));
        verify(source, bindings(pom(true, false, true, true)), Set.of(source.name));
        assertThrows(AssertionError.class, () -> verify(source, bindings(pom(true, true, true, true)), Set.of()));
    }

    private static Map<String, String> fixtureSources() {
        Map<String, String> sources = new HashMap<>();
        sources.put("fixture/src/test/java/helpers/ClockBase.java", "package helpers; public abstract class ClockBase {"
                + "void before() { ClockUtil.setCurrentTime(null); } }");
        sources.put("fixture/src/test/java/suite/ChildTest.java", "package suite; import helpers.ClockBase;"
                + "public class ChildTest extends ClockBase { @Test public void scenario() {} }");
        return sources;
    }

    private static List<TestSource> clockControlled(Map<String, String> sources) {
        Map<String, TestSource> types = new HashMap<>();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            String text = LITERALS_AND_COMMENTS.matcher(entry.getValue()).replaceAll(" ");
            Matcher type = TYPE.matcher(text);
            Matcher pkg = PACKAGE.matcher(text);
            if (!type.find() || !pkg.find()) {
                continue;
            }
            String parent = type.group(3);
            if (parent != null && !parent.contains(".")) {
                Matcher imported =
                        Pattern.compile("\\bimport\\s+([\\w.]+\\." + Pattern.quote(parent) + ")\\s*;").matcher(text);
                parent = imported.find() ? imported.group(1) : pkg.group(1) + "." + parent;
            }
            TestSource source = new TestSource(entry.getKey(), pkg.group(1) + "." + type.group(2), parent,
                    type.group(1).contains("abstract"), CLOCK.matcher(text).find(),
                    Pattern.compile("@Test\\b").matcher(text).find());
            types.put(source.name, source);
        }
        boolean changed;
        do {
            changed = false;
            for (TestSource source : types.values()) {
                TestSource parent = types.get(source.parent);
                if (parent != null) {
                    if (parent.clock && !source.clock) {
                        source.clock = true;
                        changed = true;
                    }
                    if (parent.tests && !source.tests) {
                        source.tests = true;
                        changed = true;
                    }
                }
            }
        } while (changed);
        List<TestSource> controlled = new ArrayList<>();
        for (TestSource source : types.values()) {
            if (source.clock && source.tests && !source.abstractType && source.name.endsWith("Test")) {
                controlled.add(source);
            }
        }
        return controlled;
    }

    private static void verify(TestSource source, Bindings bindings, Set<String> profileOnly) {
        assertNotNull("Missing module configuration for " + source.name, bindings);
        Execution defaults = bindings.normal.get("default-test");
        assertNotNull("Missing default-test configuration for " + source.name, defaults);
        assertTrue("Default shared execution selects clock-controlled test " + source.name,
                matches(defaults.excludes, source.path));
        List<Execution> normal = selected(bindings.normal, source);
        assertEquals("Changed regular build selection for " + source.name, profileOnly.contains(source.name) ? 0 : 1,
                normal.size());
        Map<String, Execution> verification = new LinkedHashMap<>(bindings.normal);
        verification.putAll(bindings.profile);
        List<Execution> all = selected(verification, source);
        assertEquals("Verification must select exactly one execution for " + source.name, 1, all.size());
        for (Execution execution : all) {
            assertEquals(source.name + " phase", "test", execution.phase);
            assertEquals(source.name + " forkCount", "1", execution.forks);
            assertEquals(source.name + " reuseForks", "false", execution.reuse);
            assertEquals(source.name + " failIfNoTests", "true", execution.failIfNoTests);
        }
    }

    private static List<Execution> selected(Map<String, Execution> executions, TestSource source) {
        List<Execution> selected = new ArrayList<>();
        for (Execution execution : executions.values()) {
            if (!"default-test".equals(execution.id) && matches(execution.includes, source.path)
                    && !matches(execution.excludes, source.path)) {
                selected.add(execution);
            }
        }
        return selected;
    }

    private static boolean matches(List<String> patterns, String path) {
        for (String pattern : patterns) {
            StringBuilder regex = new StringBuilder("^");
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '*' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                    regex.append(".*");
                    i++;
                } else if (c == '*') {
                    regex.append("[^/]*");
                } else {
                    regex.append(Pattern.quote(String.valueOf(c)));
                }
            }
            if (path.matches(regex.append('$').toString())) {
                return true;
            }
        }
        return false;
    }

    private static Bindings bindings(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Element project = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
        Bindings bindings = new Bindings();
        readBuild(child(project, "build"), bindings.normal);
        for (Element profile : children(child(project, "profiles"), "profile")) {
            if ("groovy-migration-verification".equals(value(profile, "id"))) {
                readBuild(child(profile, "build"), bindings.profile);
            }
        }
        return bindings;
    }

    private static void readBuild(Element build, Map<String, Execution> executions) {
        if (build == null) {
            return;
        }
        for (Element owner : new Element[] {child(build, "pluginManagement"), build}) {
            for (Element plugin : children(child(owner, "plugins"), "plugin")) {
                if (!"maven-surefire-plugin".equals(value(plugin, "artifactId"))) {
                    continue;
                }
                for (Element element : children(child(plugin, "executions"), "execution")) {
                    Execution execution = new Execution();
                    execution.id = value(element, "id");
                    execution.phase = value(element, "phase");
                    Element config = child(element, "configuration");
                    execution.includes = values(child(config, "includes"), "include");
                    execution.excludes = values(child(config, "excludes"), "exclude");
                    execution.forks = value(config, "forkCount");
                    execution.reuse = value(config, "reuseForks");
                    execution.failIfNoTests = value(config, "failIfNoTests");
                    executions.put(execution.id, execution);
                }
            }
        }
    }

    private static Element child(Element element, String name) {
        List<Element> children = children(element, name);
        return children.isEmpty() ? null : children.get(0);
    }

    private static List<Element> children(Element element, String name) {
        List<Element> children = new ArrayList<>();
        if (element != null) {
            for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node instanceof Element && name.equals(node.getLocalName())) {
                    children.add((Element) node);
                }
            }
        }
        return children;
    }

    private static String value(Element element, String name) {
        Element found = child(element, name);
        return found == null ? "" : found.getTextContent().trim();
    }

    private static List<String> values(Element element, String name) {
        List<String> values = new ArrayList<>();
        for (Element child : children(element, name)) {
            values.add(child.getTextContent().trim());
        }
        return values;
    }

    private static String pom(boolean excluded, boolean normal, boolean profile, boolean fresh) {
        String defaults = "<execution><id>default-test</id><configuration><excludes>"
                + (excluded ? "<exclude>**/ChildTest.java</exclude>" : "") + "</excludes></configuration></execution>";
        String execution = "<phase>test</phase><configuration><includes><include>**/ChildTest.java</include></includes>"
                + "<forkCount>1</forkCount><reuseForks>" + !fresh + "</reuseForks>"
                + "<failIfNoTests>true</failIfNoTests></configuration></execution>";
        String prefix = "<build><plugins><plugin><artifactId>maven-surefire-plugin</artifactId><executions>";
        String suffix = "</executions></plugin></plugins></build>";
        return "<project xmlns='http://maven.apache.org/POM/4.0.0'>" + prefix + defaults
                + (normal ? "<execution><id>isolated-normal</id>" + execution : "") + suffix
                + "<profiles><profile><id>groovy-migration-verification</id>" + prefix
                + (profile ? "<execution><id>isolated-profile</id>" + execution : "") + suffix
                + "</profile></profiles></project>";
    }

    private static class Bindings {
        private final Map<String, Execution> normal = new LinkedHashMap<>();
        private final Map<String, Execution> profile = new LinkedHashMap<>();
    }

    private static class Execution {
        private String id;
        private String phase;
        private List<String> includes = Collections.emptyList();
        private List<String> excludes = Collections.emptyList();
        private String forks;
        private String reuse;
        private String failIfNoTests;
    }

    private static class TestSource {
        private final String module;
        private final String path;
        private final String name;
        private final String parent;
        private final boolean abstractType;
        private boolean clock;
        private boolean tests;

        private TestSource(String key, String name, String parent, boolean abstractType, boolean clock, boolean tests) {
            this.module = key.substring(0, key.indexOf('/'));
            this.path = key.substring(key.indexOf('/') + 1);
            this.name = name;
            this.parent = parent;
            this.abstractType = abstractType;
            this.clock = clock;
            this.tests = tests;
        }
    }
}
