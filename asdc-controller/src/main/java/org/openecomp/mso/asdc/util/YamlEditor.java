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

package org.openecomp.mso.asdc.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;

public class YamlEditor {

    private static final String REFER_PATTERN = "file:///";
    private Map <String, Object> yml;
    private Yaml yaml = new Yaml ();

    public YamlEditor () {

    }

    public YamlEditor (byte[] body) {
        init (body);
    }

    @SuppressWarnings("unchecked")
    private synchronized void init (byte[] body) {
        InputStream input = new ByteArrayInputStream (body);
        yml = (Map <String, Object>) yaml.load (input);
    }

    public synchronized List <String> getYamlNestedFileResourceTypeList () {
        List <String> typeList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Map <String, Object> resourceMap = (Map <String, Object>) yml.get ("resources");
        Iterator <Entry <String, Object>> it = resourceMap.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry <String, Object> pair = it.next ();
            @SuppressWarnings("unchecked")
            Map <String, String> resourceEntry = (Map <String, String>) pair.getValue ();
            String type = resourceEntry.get ("type");

            if (type.contains (REFER_PATTERN)) {
                typeList.add (type);
            }
            it.remove (); // avoids a ConcurrentModificationException
        }
        return typeList;
    }
    
    public synchronized List <String> getYamlResourceTypeList () {
        List <String> typeList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Map <String, Object> resourceMap = (Map <String, Object>) yml.get ("resources");
        for (Entry<String, Object> pair : resourceMap.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, String> resourceEntry = (Map<String, String>) pair.getValue();
            typeList.add(resourceEntry.get("type"));
        }
        return typeList;
    }    

    // Generate the parameter list based on the Heat Template
    // Based on the email from Ella Kvetny:
    // Within Heat Template, under parameters catalog, it might indicate the default value of the parameter
    // If default value exist, the parameter is not mandatory, otherwise its value should be set
    public synchronized Set <HeatTemplateParam> getParameterList (String artifactUUID) {
        Set <HeatTemplateParam> paramSet = new HashSet<>();
        @SuppressWarnings("unchecked")
        Map <String, Object> resourceMap = (Map <String, Object>) yml.get ("parameters");

        for (Entry<String, Object> stringObjectEntry : resourceMap.entrySet()) {
            HeatTemplateParam param = new HeatTemplateParam();
            Entry<String, Object> pair = stringObjectEntry;
            @SuppressWarnings("unchecked")
            Map<String, String> resourceEntry = (Map<String, String>) pair.getValue();

            param.setParamName(pair.getKey());
            // System.out.println(pair.getKey()+":"+type);
            if (resourceEntry.containsKey("default")) {
                param.setRequired(false);
            } else {
                param.setRequired(true);
            }
            // Now set the type
            String value = resourceEntry.get("type");
            param.setParamType(value);

            param.setHeatTemplateArtifactUuid(artifactUUID);

            paramSet.add(param);

        }
        return paramSet;

    }

    public synchronized void addParameterList (Set <HeatTemplateParam> heatSet) {

        @SuppressWarnings("unchecked")
        Map <String, Object> resourceMap = (Map <String, Object>) yml.get ("parameters");
        if (resourceMap == null) {
            resourceMap = new LinkedHashMap<>();
            this.yml.put ("parameters", resourceMap);
        }
        for (HeatTemplateParam heatParam : heatSet) {
            Map <String, Object> paramInfo = new HashMap<>();
            paramInfo.put ("type", heatParam.getParamType ());

            resourceMap.put (heatParam.getParamName (), paramInfo);
        }

        // this.yml.put("parameters", resourceMap);

    }

    public boolean isParentTemplate (String templateBody) {
        return templateBody.contains (REFER_PATTERN);
    }

    public boolean verifyTemplate () {
        // Verify whether the heat template is for Vnf Resource
        // We don't support other template installation yet

        return true;
    }

    public String encode (Map <String, Object> content) {
        return yaml.dump (content);
    }

    public synchronized String encode () {
        return this.yaml.dump (this.yml);
    }

    /**
     * This method return the YAml file as a string.
     * 
     */
    @Override
    public String toString () {

        return encode ();
    }

}
