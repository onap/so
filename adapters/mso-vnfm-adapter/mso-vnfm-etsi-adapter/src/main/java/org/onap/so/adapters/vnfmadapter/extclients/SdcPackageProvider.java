/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.extclients;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.String.format;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.onap.so.adapters.vnfmadapter.NvfmAdapterUtils.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;


@Component
public class SdcPackageProvider {
    private static final String GET_PACKAGE_URL = "%s/catalog/resources/%s/toscaModel";
    @Value("sdc.toscametapath:TOSCA-Metadata/TOSCA.meta")
    private List<String> toscaMetaPaths;
    private final String TOSCA_VNFD_KEY = "Entry-Definitions";
    private static Logger logger = getLogger(SdcPackageProvider.class);

    @Value("${sdc.username}")
    private String sdcUsername;
    @Value("${sdc.password}")
    private String sdcPassword;
    @Value("${sdc.key}")
    private String sdcKey;
    @Value("${sdc.endpoint}")
    private String baseUrl;


    public String getVnfdId(String csarId) {
        return getVnfNodeProperty(csarId, "descriptor_id");
    }

    private String getVnfNodeProperty(final String csarId, final String propertyName) {
        logger.debug("Getting " + propertyName + " from " + csarId);
        final byte[] onapPackage = getPackage(csarId);

        try {
            final String vnfdLocation = getVnfdLocation(new ByteArrayInputStream(onapPackage));
            final String onapVnfdContent = getFileInZip(new ByteArrayInputStream(onapPackage), vnfdLocation).toString();
            final JsonObject root = new Gson().toJsonTree(new Yaml().load(onapVnfdContent)).getAsJsonObject();

            final JsonObject topologyTemplates = child(root, "topology_template");
            final JsonObject nodeTemplates = child(topologyTemplates, "node_templates");
            for (final JsonObject child : children(nodeTemplates)) {
                final String type = childElement(child, "type").getAsString();
                String propertyValue = null;
                if (type.equals("tosca.nodes.nfv.VNF")) {
                    final JsonObject properties = child(child, "properties");
                    logger.debug("properties: " + properties.toString());

                    propertyValue = properties.get(propertyName).getAsJsonPrimitive().getAsString();
                }
                if (propertyValue == null) {
                    propertyValue = getValueFromNodeTypeDefinition(root, type, propertyName);
                }
                return propertyValue;
            }

        } catch (final Exception e) {
            throw new IllegalArgumentException("Unable to extract " + propertyName + " from ONAP package", e);
        }
        throw new IllegalArgumentException("Unable to extract " + propertyName + " from ONAP package");
    }

    private String getValueFromNodeTypeDefinition(final JsonObject root, final String nodeTypeName,
            final String propertyName) {
        final JsonObject nodeTypes = child(root, "node_types");
        final JsonObject nodeType = child(nodeTypes, nodeTypeName);

        if (childElement(nodeType, "derived_from").getAsString().equals("tosca.nodes.nfv.VNF")) {
            final JsonObject properties = child(nodeType, "properties");
            logger.debug("properties: " + properties.toString());
            final JsonObject property = child(properties, propertyName);
            logger.debug("property: " + property.toString());
            logger.debug("property default: " + childElement(property, "default").toString());
            return childElement(property, "default").getAsJsonPrimitive().getAsString();
        }
        return null;
    }

    private byte[] getPackage(String csarId) {
        final String SERVICE_NAME = "vnfm-adapter";
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(format(GET_PACKAGE_URL, baseUrl, csarId));
            httpget.setHeader(ACCEPT, APPLICATION_OCTET_STREAM_VALUE);
            httpget.setHeader("X-ECOMP-InstanceID", SERVICE_NAME);
            httpget.setHeader("X-FromAppId", SERVICE_NAME);
            String auth = sdcUsername + ":" + CryptoUtils.decrypt(sdcPassword, sdcKey);
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            httpget.setHeader(AUTHORIZATION, authHeader);
            logger.debug("Fetching from SDC: " + httpget);
            CloseableHttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            byte[] bytes = toByteArray(is);
            client.close();
            return bytes;
        } catch (Exception e) {
            throw abortOperation("Unable to download " + csarId + " package from SDC", e);
        }
    }

    private String getVnfdLocation(InputStream stream) throws IOException {
        Iterator pathIterator = toscaMetaPaths.iterator();
        while (pathIterator.hasNext()) {
            String toscaMetadata = new String(getFileInZip(stream, pathIterator.next().toString()).toByteArray());
            if (!toscaMetadata.isEmpty()) {
                String toscaVnfdLine =
                        filter(on("\n").split(toscaMetadata), line -> line.contains(TOSCA_VNFD_KEY)).iterator().next();
                return toscaVnfdLine.replace(TOSCA_VNFD_KEY + ":", "").trim();
            }
        }
        throw abortOperation("Unable to find valid Tosca Path");
    }

    private static ByteArrayOutputStream getFileInZip(InputStream zip, String path) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(zip);
        ByteArrayOutputStream fileContent = getFileInZip(zipInputStream, path);
        zipInputStream.close();
        return fileContent;
    }

    private static ByteArrayOutputStream getFileInZip(ZipInputStream zipInputStream, String path) throws IOException {
        ZipEntry zipEntry;
        Set<String> items = new HashSet<>();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            items.add(zipEntry.getName());
            if (zipEntry.getName().matches(path)) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ByteStreams.copy(zipInputStream, byteArrayOutputStream);
                return byteArrayOutputStream;
            }
        }
        logger.error("Unable to find the {} in archive found: {}", path, items);
        throw new NoSuchElementException("Unable to find the " + path + " in archive found: " + items);
    }


    public String getFlavourId(String csarId) {
        return getVnfNodeProperty(csarId, "flavour_id");
    }
}
