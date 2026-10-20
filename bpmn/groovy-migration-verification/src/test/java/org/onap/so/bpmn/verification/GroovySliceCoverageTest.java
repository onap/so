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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class GroovySliceCoverageTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void enforcesLineAndBranchMinimumsIndependently() throws Exception {
        JsonNode manifest = manifest();
        List<String> blockers = new ArrayList<>();
        GroovySliceCoverage.verify(manifest, coverage(85, 15, 75, 25), blockers);
        assertTrue(blockers.toString(), blockers.isEmpty());

        GroovySliceCoverage.verify(manifest, coverage(84, 16, 75, 25), blockers);
        assertEquals(1, blockers.size());
        assertTrue(blockers.get(0).contains("line coverage below 0.85"));
        blockers.clear();
        GroovySliceCoverage.verify(manifest, coverage(85, 15, 74, 26), blockers);
        assertEquals(1, blockers.size());
        assertTrue(blockers.get(0).contains("branch coverage below 0.75"));
    }

    @Test
    public void sharedHelpersUseTheHigherMinimums() throws Exception {
        ObjectNode manifest = manifest();
        slice(manifest).put("sharedHelper", true);
        List<String> blockers = new ArrayList<>();
        GroovySliceCoverage.verify(manifest, coverage(85, 15, 75, 25), blockers);
        assertEquals(2, blockers.size());
        blockers.clear();
        GroovySliceCoverage.verify(manifest, coverage(90, 10, 80, 20), blockers);
        assertTrue(blockers.toString(), blockers.isEmpty());
    }

    @Test
    public void absentSourceCoverageAndZeroExecutableLinesCannotPass() throws Exception {
        List<String> blockers = new ArrayList<>();
        GroovySliceCoverage.verify(manifest(), MAPPER.createObjectNode(), blockers);
        assertTrue(blockers.stream().anyMatch(message -> message.contains("missing source-file coverage")));
        blockers.clear();
        GroovySliceCoverage.verify(manifest(), coverage(0, 0, 0, 0), blockers);
        assertTrue(blockers.stream().anyMatch(message -> message.contains("line coverage below")));
    }

    @Test
    public void aSourceWithoutBranchesReportsThatTheBranchGateIsNotApplicable() throws Exception {
        List<String> blockers = new ArrayList<>();
        List<Map<String, Object>> evidence = GroovySliceCoverage.verify(manifest(), coverage(10, 0, 0, 0), blockers);
        assertTrue(blockers.toString(), blockers.isEmpty());
        assertEquals(Boolean.FALSE, evidence.get(0).get("branchCoverageApplicable"));
    }

    @Test
    public void coverageDoesNotReplaceRequiredSemanticScenarios() throws Exception {
        ObjectNode manifest = manifest();
        ((ArrayNode) slice(manifest).path("scenarios")).removeAll();
        List<String> blockers = new ArrayList<>();
        GroovySliceCoverage.verify(manifest, coverage(100, 0, 100, 0), blockers);
        assertTrue(blockers.stream().anyMatch(message -> message.contains("no required semantic scenarios")));
        ((ArrayNode) slice(manifest).path("scenarios")).add("unknown");
        blockers.clear();
        GroovySliceCoverage.verify(manifest, coverage(100, 0, 100, 0), blockers);
        assertTrue(blockers.stream().anyMatch(message -> message.contains("unknown required scenario")));
    }

    @Test
    public void aConfiguredMinimumCannotWeakenTheGate() throws Exception {
        for (String configured : new String[] {"0.5", "1.1", "\"0.9\"", "null"}) {
            ObjectNode manifest = manifest();
            slice(manifest).set("minimumLineCoverage", MAPPER.readTree(configured));
            List<String> blockers = new ArrayList<>();
            GroovySliceCoverage.verify(manifest, coverage(100, 0, 100, 0), blockers);
            assertFalse(configured, blockers.isEmpty());
        }
    }

    @Test
    public void duplicateSourcesAndSlicesFailRatherThanInflateTheMeasurement() throws Exception {
        ObjectNode manifest = manifest();
        ArrayNode sources = (ArrayNode) slice(manifest).path("sources");
        sources.add(sources.get(0).deepCopy());
        List<String> blockers = new ArrayList<>();
        GroovySliceCoverage.verify(manifest, coverage(100, 0, 100, 0), blockers);
        assertTrue(blockers.stream().anyMatch(message -> message.contains("duplicate production source")));
        ((ArrayNode) manifest.path("coverageSlices")).add(slice(manifest).deepCopy());
        blockers.clear();
        GroovySliceCoverage.verify(manifest, coverage(100, 0, 100, 0), blockers);
        assertTrue(blockers.stream().anyMatch(message -> message.contains("duplicate coverage slice")));
    }

    @Test
    public void malformedCountersAreBlockersRatherThanZeroDefaults() throws Exception {
        for (String configured : new String[] {"-1", "1.5", "\"100\"", "null"}) {
            JsonNode coverage = coverage(100, 0, 100, 0);
            ((ObjectNode) coverage.path("common").path("sources").path("example/Helper.groovy")).set("lineCovered",
                    MAPPER.readTree(configured));
            List<String> blockers = new ArrayList<>();
            GroovySliceCoverage.verify(manifest(), coverage, blockers);
            assertTrue(configured, blockers.stream().anyMatch(message -> message.contains("invalid coverage counter")));
        }
    }

    private static ObjectNode manifest() throws Exception {
        return (ObjectNode) MAPPER.readTree("{\"scenarios\":[{\"id\":\"error-route\"}],\"coverageSlices\":[{"
                + "\"id\":\"helper-slice\",\"sources\":[{\"module\":\"common\",\"source\":\"example/Helper.groovy\"}],"
                + "\"scenarios\":[\"error-route\"]}]}");
    }

    private static ObjectNode slice(ObjectNode manifest) {
        return (ObjectNode) manifest.path("coverageSlices").get(0);
    }

    private static JsonNode coverage(int lineCovered, int lineMissed, int branchCovered, int branchMissed)
            throws Exception {
        return MAPPER.readTree("{\"common\":{\"sources\":{\"example/Helper.groovy\":{\"lineCovered\":" + lineCovered
                + ",\"lineMissed\":" + lineMissed + ",\"branchCovered\":" + branchCovered + ",\"branchMissed\":"
                + branchMissed + "}}}}");
    }
}
