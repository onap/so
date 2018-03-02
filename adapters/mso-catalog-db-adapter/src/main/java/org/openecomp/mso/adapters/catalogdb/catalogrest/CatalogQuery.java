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
package org.openecomp.mso.adapters.catalogdb.catalogrest;

import org.openecomp.mso.logger.MsoLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CatalogQuery {
	protected static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private static final boolean IS_EMBED = true;

	public abstract String JSON2(boolean isArray, boolean isEmbed);

	protected void put(Map<String, String> valueMap, String key, String value) {
		valueMap.put(key, value == null? "null": '"'+ value+ '"');
	}

	protected void put(Map<String, String> valueMap, String key, Integer value) {
		valueMap.put(key, value == null? "null": value.toString());
	}

	protected void put(Map<String, String> valueMap, String key, Boolean value) {
		valueMap.put(key, value == null? "null": value? "true": "false");
	}

	protected String setTemplate(String template, Map<String, String> valueMap) {
		LOGGER.debug ("CatalogQuery setTemplate");
		StringBuffer result = new StringBuffer();

		String pattern = "<.*>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(template);

		LOGGER.debug ("CatalogQuery template:"+ template);
		while(m.find()) {
			String key = template.substring(m.start()+1, m.end()-1);
			LOGGER.debug ("CatalogQuery key:"+ key+ " contains key? "+ valueMap.containsKey(key));
	         m.appendReplacement(result, valueMap.containsKey(key)? valueMap.get(key): "\"TBD\"");
		}
		m.appendTail(result);
                LOGGER.debug ("CatalogQuery return:"+ result.toString());
		return result.toString();
	}

	/**
	 * The simple, clean, generic way to handle the interface
	 */
 	protected String smartToJSON() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
		}
		catch (Exception e) {
		    LOGGER.debug("Exception:", e);
			LOGGER.debug ("jsonString exception:"+e.getMessage());
			jsonString = "invalid"; //throws instead?
		}
		return jsonString;
	}

	public String toJsonString(String version, boolean isArray) {
		switch(version) {
		case "v1": return smartToJSON();
		case "v2": return JSON2(isArray, !IS_EMBED);
		default:
			return "invalid version: "+ version;
		}
	}
}
