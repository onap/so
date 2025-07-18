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

package org.onap.aaiclient.client.aai;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.onap.aai.annotations.Metadata;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIFluentTypeReverseLookup;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import com.google.common.base.CaseFormat;

/**
 * Types are accessed through AAIFluentTypeBuilder and should no longer be added as static members
 *
 */
public class AAIObjectType implements AAIObjectBase, AAIObjectName, GraphInventoryObjectType, Serializable {

    private static final long serialVersionUID = -2877184776691514600L;
    private static Map<String, AAIObjectType> map = new HashMap<>();

    public static final AAIObjectType GENERIC_QUERY = new AAIObjectType("/search", "/generic-query", "generic-query");
    public static final AAIObjectType BULK_PROCESS = new AAIObjectType("/bulkprocess", "", "bulkprocess");
    public static final AAIObjectType SINGLE_TRANSACTION =
            new AAIObjectType("/bulk/single-transaction", "", "single-transaction");
    public static final AAIObjectType NODES_QUERY = new AAIObjectType("/search", "/nodes-query", "nodes-query");
    public static final AAIObjectType CUSTOM_QUERY = new AAIObjectType("/query", "", "query");
    public static final AAIObjectType UNKNOWN = new AAIObjectType("", "", "unknown") {

        private static final long serialVersionUID = 9208984071038447607L;

        @Override
        public boolean passThrough() {
            return true;
        }
    };
    public static final AAIObjectType DSL = new AAIObjectType("/dsl", "", "dsl");
    public static final AAIObjectType SUB_L_INTERFACE = new AAIObjectType(
            "/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{interface-name}",
            "/l-interfaces/l-interface/{sub-interface-name}", "sub-l-interface");

    private final String uriTemplate;
    private final String parentUri;
    private final String partialUri;
    private final Class<?> aaiObjectClass;
    private final String name;

    static {
        /* Locate any AAIObjectTypes on the classpath and add them to our map */
        java.util.Collection<URL> packages = ClasspathHelper.forPackage("");
        Reflections r =
                new Reflections(new ConfigurationBuilder().setUrls(packages).setScanners(new SubTypesScanner()));

        Set<Class<? extends AAIObjectType>> resources = r.getSubTypesOf(AAIObjectType.class);

        for (Class<? extends AAIObjectType> customTypeClass : resources) {
            AAIObjectType customType;
            try {
                customType = customTypeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
        }
    }

    protected AAIObjectType() {
        this.parentUri = null;
        this.partialUri = null;
        this.uriTemplate = null;
        this.aaiObjectClass = null;
        this.name = null;
    }

    protected AAIObjectType(String parentUri, String partialUri, String name) {
        this(parentUri, partialUri, name, true);
    }

    public AAIObjectType(String parentUri, String partialUri, String name, boolean register) {
        this.parentUri = parentUri;
        this.partialUri = partialUri;
        this.uriTemplate = parentUri + partialUri;
        this.aaiObjectClass = null;
        this.name = name;
        if (register && !AAIObjectType.map.containsKey(name)) {
            AAIObjectType.map.put(name, this);
        }
    }

    protected AAIObjectType(String parentUri, Class<?> aaiObjectClass) {
        this.parentUri = parentUri;
        this.partialUri = removeParentUri(aaiObjectClass, parentUri);
        this.uriTemplate = parentUri + partialUri;
        this.aaiObjectClass = aaiObjectClass;
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, aaiObjectClass.getSimpleName());
        if (!AAIObjectType.map.containsKey(name)) {
            AAIObjectType.map.put(name, this);
        }
    }

    @Override
    public String toString() {
        return this.uriTemplate();
    }

    public static AAIObjectType fromTypeName(String name, String uri) {

        return new AAIFluentTypeReverseLookup().fromName(name, uri);
    }

    public static AAIObjectType fromTypeName(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        } else {
            return AAIObjectType.UNKNOWN;
        }
    }

    @Override
    public String typeName() {
        return this.typeName(CaseFormat.LOWER_HYPHEN);
    }

    @Override
    public String typeName(CaseFormat format) {
        return CaseFormat.LOWER_HYPHEN.to(format, this.name.replace("default-", ""));
    }

    @Override
    public String uriTemplate() {
        return this.uriTemplate;
    }

    @Override
    public String partialUri() {
        return this.partialUri;
    }

    @Override
    public int hashCode() {
        return this.typeName().hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof GraphInventoryObjectName) {
            return this.typeName().equals(((GraphInventoryObjectName) o).typeName());
        }

        return false;
    }

    protected String removeParentUri(Class<?> aaiObjectClass, String parentUri) {
        return aaiObjectClass.getAnnotation(Metadata.class).uriTemplate().replaceFirst(Pattern.quote(parentUri), "");
    }
}
