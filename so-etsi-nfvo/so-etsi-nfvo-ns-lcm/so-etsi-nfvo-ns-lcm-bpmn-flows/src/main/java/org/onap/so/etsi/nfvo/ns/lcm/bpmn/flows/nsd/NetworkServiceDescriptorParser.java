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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class NetworkServiceDescriptorParser {
    public static final String NS_NODE_TYPE = "tosca.nodes.nfv.NS";
    private static final String NODE_TYPE = "node_type";
    private static final String SUBSTITUTION_MAPPINGS = "substitution_mappings";
    private static final Logger logger = LoggerFactory.getLogger(NetworkServiceDescriptorParser.class);
    private static final String VNF_TYPE = "tosca.nodes.nfv.VNF";
    private static final String PROPERTIES = "properties";
    private static final String TYPE = "type";
    private static final String NODE_TEMPLATES = "node_templates";
    private static final String TOPOLOGY_TEMPLATE = "topology_template";
    private static final String ENTRY_DEFINITIONS = "Entry-Definitions";
    private static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";
    private final ToscaMetadataParser toscaMetadataParser;
    private final FileParser fileParser;

    @Autowired
    public NetworkServiceDescriptorParser(final ToscaMetadataParser toscaMetadataParser, final FileParser fileParser) {
        this.toscaMetadataParser = toscaMetadataParser;
        this.fileParser = fileParser;
    }

    public Optional<NetworkServiceDescriptor> parser(final byte[] zipBytes) {
        try {
            final Map<String, FileEntry> files = getZipContent(zipBytes);
            if (isMetaFilePresent(files)) {
                final Optional<ToscaMetadata> optional =
                        toscaMetadataParser.parse(files.get(TOSCA_META_PATH_FILE_NAME));
                if (optional.isPresent()) {
                    final ToscaMetadata toscaMetadata = optional.get();
                    logger.info("Parsed ToscaMetadata {}", toscaMetadata);
                    final String entryDefinitionFile = toscaMetadata.getEntry(ENTRY_DEFINITIONS);
                    if (entryDefinitionFile != null && files.containsKey(entryDefinitionFile)) {
                        final Map<String, Object> fileContent =
                                fileParser.getFileContent(files.get(entryDefinitionFile));
                        final Map<String, Object> topologyTemplates = getTopologyTemplates(fileContent);
                        final Map<String, Object> nodeTemplates = getNodeTemplates(topologyTemplates);

                        final Optional<NetworkServiceDescriptor> nsdOptional =
                                getNetworkServiceDescriptor(topologyTemplates);;
                        if (nsdOptional.isPresent()) {
                            final NetworkServiceDescriptor networkServiceDescriptor = nsdOptional.get();
                            networkServiceDescriptor.setVnfs(getVirtualNetworkFunctions(nodeTemplates));
                            return Optional.of(networkServiceDescriptor);
                        }

                    }
                }

            }

        } catch (final Exception exception) {
            logger.error("Unable to parser nsd zip content", exception);
        }
        logger.error("Unable to parser nsd zip content");
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<NetworkServiceDescriptor> getNetworkServiceDescriptor(
            final Map<String, Object> topologyTemplates) {
        final Map<String, Object> substitutionMappings =
                (Map<String, Object>) topologyTemplates.get(SUBSTITUTION_MAPPINGS);
        final Object nodeType = substitutionMappings.get(NODE_TYPE);
        if (substitutionMappings != null && nodeType != null && NS_NODE_TYPE.equals(nodeType)) {
            final NetworkServiceDescriptor networkServiceDescriptor = new NetworkServiceDescriptor();
            networkServiceDescriptor.setType(nodeType.toString());
            networkServiceDescriptor.setProperties((Map<String, Object>) substitutionMappings.get(PROPERTIES));
            return Optional.of(networkServiceDescriptor);
        }
        logger.error("No {} found in fileContent: {}", SUBSTITUTION_MAPPINGS, topologyTemplates);

        return Optional.empty();
    }

    private List<VirtualNetworkFunction> getVirtualNetworkFunctions(final Map<String, Object> nodeTemplates) {
        final List<VirtualNetworkFunction> vnfs = new ArrayList<>();
        for (final Entry<String, Object> entry : nodeTemplates.entrySet()) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> entryValue = (Map<String, Object>) entry.getValue();
            final Object type = entryValue.get(TYPE);
            if (type != null && type.equals(VNF_TYPE)) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> vnfProperties = (Map<String, Object>) entryValue.get(PROPERTIES);
                final VirtualNetworkFunction vnf = new VirtualNetworkFunction();
                vnf.setVnfName(entry.getKey());

                if (vnfProperties != null && !vnfProperties.isEmpty()) {
                    final Object vnfDescriptorId = vnfProperties.get("descriptor_id");
                    @SuppressWarnings("unchecked")
                    final List<String> vnfmInfoList = (List<String>) vnfProperties.get("vnfm_info");
                    if (vnfDescriptorId != null && vnfmInfoList != null) {
                        vnf.setVnfmInfoList(vnfmInfoList);
                        vnf.setVnfdId(vnfDescriptorId.toString());
                        vnf.setProperties(vnfProperties);
                        vnfs.add(vnf);
                    } else {
                        logger.warn("descriptor_id missing {}", entryValue);
                    }
                }
            }

        }
        return vnfs;
    }

    private Map<String, Object> getNodeTemplates(final Map<String, Object> topologyTemplates) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> nodeTemplates = (Map<String, Object>) topologyTemplates.get(NODE_TEMPLATES);
        if (nodeTemplates != null) {
            logger.debug("Found nodeTemplates: {}", topologyTemplates);
            return nodeTemplates;
        }
        logger.error("No {} found in fileContent: {}", NODE_TEMPLATES, topologyTemplates);
        return Collections.emptyMap();
    }

    private Map<String, Object> getTopologyTemplates(final Map<String, Object> fileContent) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> topologyTemplates = (Map<String, Object>) fileContent.get(TOPOLOGY_TEMPLATE);
        if (topologyTemplates != null) {
            logger.debug("Found {}: {}", TOPOLOGY_TEMPLATE, topologyTemplates);

            return topologyTemplates;
        }
        logger.error("No {} found in fileContent: {}", TOPOLOGY_TEMPLATE, fileContent);
        return Collections.emptyMap();
    }

    private boolean isMetaFilePresent(final Map<String, FileEntry> files) {
        return files.containsKey(TOSCA_META_PATH_FILE_NAME);
    }

    private Map<String, FileEntry> getZipContent(final byte[] zipBytes) {
        final Map<String, FileEntry> files = new HashMap<>();
        try (final ZipInputStream inputZipStream = new ZipInputStream(new ByteArrayInputStream(zipBytes));) {
            ZipEntry zipEntry;
            while ((zipEntry = inputZipStream.getNextEntry()) != null) {
                logger.info("{} : {}", zipEntry.getName(), zipEntry.isDirectory());
                if (files.get(zipEntry.getName()) != null) {
                    logger.warn("{} File entry already exists ...", zipEntry.getName());
                } else {
                    final FileEntry fileEntry = new FileEntry().filePath(zipEntry.getName())
                            .fileContent(getBytes(inputZipStream)).isDirectory(zipEntry.isDirectory());
                    files.put(zipEntry.getName(), fileEntry);

                }

            }
            return files;
        } catch (final Exception exception) {
            logger.error("Unable to parser nsd zip content", exception);
            return Collections.emptyMap();
        }
    }

    private byte[] getBytes(final ZipInputStream inputZipStream) throws IOException {
        try {
            return IOUtils.toByteArray(inputZipStream);
        } catch (final IOException exception) {
            logger.error("Could not read bytes from file", exception);
            throw exception;
        }
    }


}
