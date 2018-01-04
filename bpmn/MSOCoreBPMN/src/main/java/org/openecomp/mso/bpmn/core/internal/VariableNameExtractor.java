/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.core.internal;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts variable name from expression if entire expression is just
 * one variable, for example "${x}".
 *
 * Ignores all whitespaces, except inside variable name.
 *
 * Examples:
 * "${x}", extracted variable name is "x"
 * " ${\t weird_NAME    }", extracted variable name is "weird_NAME"
 * "${incorrect name}", no extracted name
 * "${two}+${two}", no extracted name
 */
public class VariableNameExtractor {

    private static final Pattern VARIABLE_NAME_PATTERN = Pattern
            .compile("^\\s*\\$\\s*\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\s*$");

    private final String expression;


    /**
     * Creates new VariableNameExtractor
     * @param expression expression to be parsed
     */
    public VariableNameExtractor(String expression) {
        this.expression = expression;
    }

    /**
     * Extracts variable name from expression given in constructor
     * @return Optional of variable name, empty if expression wasn't single variable
     */
    public Optional<String> extract() {
        Matcher matcher = VARIABLE_NAME_PATTERN.matcher(expression);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(matcher.group(1));
    }

}
