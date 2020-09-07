/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class ToscaMetadataParser {
    private static final String ATTRIBUTE_VALUE_SEPARATOR = ":";
    private static final Logger logger = LoggerFactory.getLogger(ToscaMetadataParser.class);

    public Optional<ToscaMetadata> parse(final FileEntry toscaMetaFile) {
        try {
            final ToscaMetadata toscaMetadata = new ToscaMetadata();
            final List<String> lines = IOUtils.readLines(toscaMetaFile.getFileContentAsStream(), "utf-8");
            for (final String line : lines) {
                final String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && trimmedLine.contains(ATTRIBUTE_VALUE_SEPARATOR)) {
                    final String[] entry = trimmedLine.split(ATTRIBUTE_VALUE_SEPARATOR);
                    if (entry.length >= 2 && isNotBlank(entry[0]) && isNotBlank(entry[1])) {
                        toscaMetadata.addEntry(entry[0].trim(), entry[1].trim());
                    } else {
                        logger.warn("Unexpected line in metadata file: {}", line);
                    }
                } else {
                    logger.warn("Unexpected line does not contain valid separator {} in metadata file: {}",
                            ATTRIBUTE_VALUE_SEPARATOR, line);
                }

            }
            return Optional.of(toscaMetadata);

        } catch (final Exception exception) {
            logger.error("Unable to parser metadata file content", exception);
        }
        return Optional.empty();
    }


}
