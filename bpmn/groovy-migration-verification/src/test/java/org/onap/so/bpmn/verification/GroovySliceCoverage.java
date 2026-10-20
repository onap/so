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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class GroovySliceCoverage {
    private GroovySliceCoverage() {}

    static List<Map<String, Object>> verify(JsonNode manifest, JsonNode coverage, List<String> blockers) {
        List<Map<String, Object>> evidence = new ArrayList<>();
        Set<String> identifiers = new TreeSet<>();
        Set<String> scenarios = new TreeSet<>();
        manifest.path("scenarios").forEach(scenario -> scenarios.add(scenario.path("id").asText()));
        JsonNode slices = manifest.path("coverageSlices");
        if (!slices.isMissingNode() && !slices.isArray()) {
            blockers.add("coverageSlices must be an array");
            return evidence;
        }
        for (JsonNode slice : slices) {
            String id = slice.path("id").asText();
            if (id.isEmpty() || !identifiers.add(id)) {
                blockers.add("Invalid or duplicate coverage slice: " + id);
                continue;
            }
            boolean shared = slice.path("sharedHelper").asBoolean();
            double lineMinimum = minimum(slice, "minimumLineCoverage", shared ? 0.90 : 0.85, blockers);
            double branchMinimum = minimum(slice, "minimumBranchCoverage", shared ? 0.80 : 0.75, blockers);
            JsonNode requiredScenarios = slice.path("scenarios");
            if (!requiredScenarios.isArray() || requiredScenarios.isEmpty()) {
                blockers.add(id + ": no required semantic scenarios");
            } else {
                for (JsonNode scenario : requiredScenarios) {
                    if (!scenario.isTextual() || !scenarios.contains(scenario.asText())) {
                        blockers.add(id + ": unknown required scenario: " + scenario);
                    }
                }
            }
            long[] counts = new long[4];
            JsonNode sources = slice.path("sources");
            if (!sources.isArray() || sources.isEmpty()) {
                blockers.add(id + ": no production Groovy sources selected");
            } else {
                Set<String> selected = new TreeSet<>();
                for (JsonNode source : sources) {
                    String module = source.path("module").asText();
                    String name = source.path("source").asText();
                    String key = module + ":" + name;
                    JsonNode counters = coverage.path(module).path("sources").path(name);
                    if (!selected.add(key)) {
                        blockers.add(id + ": duplicate production source: " + key);
                        continue;
                    }
                    if (!name.endsWith(".groovy") || !counters.isObject()) {
                        blockers.add(id + ": missing source-file coverage: " + key);
                        continue;
                    }
                    String[] fields = {"lineCovered", "lineMissed", "branchCovered", "branchMissed"};
                    for (int index = 0; index < fields.length; index++) {
                        JsonNode value = counters.path(fields[index]);
                        if (!value.isIntegralNumber() || !value.canConvertToLong() || value.asLong() < 0) {
                            blockers.add(id + ": invalid coverage counter: " + key + ":" + fields[index]);
                        } else {
                            counts[index] = Math.addExact(counts[index], value.asLong());
                        }
                    }
                }
            }
            if (counts[0] + counts[1] == 0 || (double) counts[0] / (counts[0] + counts[1]) < lineMinimum) {
                blockers.add(
                        id + ": line coverage below " + lineMinimum + ": " + counts[0] + "/" + (counts[0] + counts[1]));
            }
            if (counts[2] + counts[3] != 0 && (double) counts[2] / (counts[2] + counts[3]) < branchMinimum) {
                blockers.add(id + ": branch coverage below " + branchMinimum + ": " + counts[2] + "/"
                        + (counts[2] + counts[3]));
            }
            Map<String, Object> result = new TreeMap<>();
            result.put("id", id);
            result.put("minimumLineCoverage", lineMinimum);
            result.put("minimumBranchCoverage", branchMinimum);
            result.put("lineCovered", counts[0]);
            result.put("lineMissed", counts[1]);
            result.put("branchCovered", counts[2]);
            result.put("branchMissed", counts[3]);
            result.put("branchCoverageApplicable", counts[2] + counts[3] != 0);
            result.put("sources", sources);
            result.put("scenarios", requiredScenarios);
            evidence.add(result);
        }
        return evidence;
    }

    private static double minimum(JsonNode slice, String field, double floor, List<String> blockers) {
        JsonNode configured = slice.path(field);
        if (configured.isMissingNode()) {
            return floor;
        }
        double value = configured.asDouble(Double.NaN);
        if (!configured.isNumber() || !Double.isFinite(value) || value < floor || value > 1) {
            blockers.add(slice.path("id").asText() + ": " + field + " must be between " + floor + " and 1");
            return floor;
        }
        return value;
    }
}
