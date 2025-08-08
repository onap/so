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

package org.onap.so.bpmn.core.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import java.util.Optional;
import org.junit.Test;

public class VariableNameExtractorTest {

    @Test
    public void shouldExtractVariableName() {
        // given
        String name = "A_different_NAME123";
        String variable = "${A_different_NAME123}";
        VariableNameExtractor extractor = new VariableNameExtractor(variable);
        // when
        Optional<String> extracted = extractor.extract();
        // then
        assertTrue(extracted.isPresent());
        assertThat(extracted.get(), containsString(name));
    }

    @Test
    public void shouldExtractVariableNameFromWhitespaces() {
        // given
        String name = "name";
        String variable = " \n\t$ \n\t{ \n\tname \n\t} \n\t";
        VariableNameExtractor extractor = new VariableNameExtractor(variable);
        // when
        Optional<String> extracted = extractor.extract();
        // then
        assertTrue(extracted.isPresent());
        assertThat(extracted.get(), containsString(name));
    }

    @Test
    public void shouldReturnEmptyIfThereIsMoreThanVariable() {
        // given
        String variable = "a ${test}";
        VariableNameExtractor extractor = new VariableNameExtractor(variable);
        // when
        Optional<String> extracted = extractor.extract();
        // then
        assertFalse(extracted.isPresent());
    }

    @Test
    public void shouldReturnEmptyIfVariableNameIsIncorrect() {
        // given
        String variable = "${name with space}";
        VariableNameExtractor extractor = new VariableNameExtractor(variable);
        // when
        Optional<String> extracted = extractor.extract();
        // then
        assertFalse(extracted.isPresent());
    }

    @Test
    public void shouldReturnEmptyIfTwoVariablesPresent() {
        // given
        String variable = "${var1} ${var2}";
        VariableNameExtractor extractor = new VariableNameExtractor(variable);
        // when
        Optional<String> extracted = extractor.extract();
        // then
        assertFalse(extracted.isPresent());
    }
}
