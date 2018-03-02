/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.openstack.utils;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.yaml.snakeyaml.Yaml;

import org.openecomp.mso.logger.MsoLogger;

public class MsoYamlEditorWithEnvt {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    
    private Map <String, Object> yml;
    private Yaml yaml = new Yaml ();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	public MsoYamlEditorWithEnvt() {
		super();
	}
	public MsoYamlEditorWithEnvt(byte[] b) {
		init(b);
	}

    @SuppressWarnings("unchecked")
    private synchronized void init (byte[] body) {
        InputStream input = new ByteArrayInputStream (body);
        yml = (Map <String, Object>) yaml.load (input);
    }	

    @SuppressWarnings("unchecked")
	public synchronized Set <MsoHeatEnvironmentParameter> getParameterListFromEnvt() {
    	// In an environment entry, the parameters section can only contain the name:value - 
    	// not other attributes.
    	Set <MsoHeatEnvironmentParameter> paramSet = new HashSet<>();
    	Map<String, Object> resourceMap = null;
    	try {
    		resourceMap = (Map<String,Object>) yml.get("parameters");
    	} catch (Exception e) {
    	    LOGGER.debug("Exception:", e);
    		return paramSet;
    	}
    	if (resourceMap == null) {
    		return paramSet;
    	}

        for (Entry<String, Object> stringObjectEntry : resourceMap.entrySet()) {
            MsoHeatEnvironmentParameter hep = new MsoHeatEnvironmentParameter();
            Entry<String, Object> pair = stringObjectEntry;
            String value;
            Object obj = pair.getValue();
            if (obj instanceof String) {
                value = yaml.dump(obj);
                // but this adds an extra '\n' at the end - which won't hurt - but we don't need it
                value = value.substring(0, value.length() - 1);
            } else if (obj instanceof LinkedHashMap) {
                //Handle that it's json
                try {
                    value = JSON_MAPPER.writeValueAsString(obj);
                } catch (Exception e) {
                    LOGGER.debug("Exception:", e);
                    value = "_BAD_JSON_MAPPING";
                }
            } else {
                //this handles integers/longs/floats/etc.
                value = String.valueOf(obj);
            }
            hep.setName((String) pair.getKey());
            hep.setValue(value);
            paramSet.add(hep);
        }
    	return paramSet;
    }
    public synchronized Set <MsoHeatEnvironmentResource> getResourceListFromEnvt() {
    	try {
    		Set<MsoHeatEnvironmentResource> resourceList = new HashSet<>();
    		@SuppressWarnings("unchecked")
    		Map<String, Object> resourceMap = (Map<String,Object>) yml.get("resource_registry");

            for (Entry<String, Object> stringObjectEntry : resourceMap.entrySet()) {
                MsoHeatEnvironmentResource her = new MsoHeatEnvironmentResource();
                Entry<String, Object> pair = stringObjectEntry;
                her.setName((String) pair.getKey());
                her.setValue((String) pair.getValue());
                resourceList.add(her);
            }
    		return resourceList;
    	} catch (Exception e) {
    	    LOGGER.debug("Exception:", e);
    	}
    	return null;
    }
    public synchronized Set <HeatTemplateParam> getParameterList () {
        Set <HeatTemplateParam> paramSet = new HashSet <> ();
        @SuppressWarnings("unchecked")
        Map <String, Object> resourceMap = (Map <String, Object>) yml.get ("parameters");

        for (Entry<String, Object> stringObjectEntry : resourceMap.entrySet()) {
            HeatTemplateParam param = new HeatTemplateParam();
            Entry<String, Object> pair = stringObjectEntry;
            @SuppressWarnings("unchecked")
            Map<String, String> resourceEntry = (Map<String, String>) pair.getValue();
            String value = null;
            try {
                value = resourceEntry.get("default");
            } catch (ClassCastException cce) {
                LOGGER.debug("Exception:", cce);
                // This exception only - the value is an integer. For what we're doing
                // here - we don't care - so set value to something - and it will
                // get marked as not being required - which is correct.
                //System.out.println("cce exception!");
                value = "300";
                // okay
            }
            param.setParamName((String) pair.getKey());
            if (value != null) {
                param.setRequired(false);
            } else {
                param.setRequired(true);
            }
            value = resourceEntry.get("type");
            param.setParamType(value);

            paramSet.add(param);

        }
        return paramSet;

    }


}
