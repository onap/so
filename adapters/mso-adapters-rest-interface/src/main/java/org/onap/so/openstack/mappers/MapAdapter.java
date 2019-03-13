/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.mappers;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.w3c.dom.Element;

public class MapAdapter extends XmlAdapter<MapEntry, Map<String, Object>> {

	@Override
	public MapEntry marshal(Map<String, Object> v) throws Exception {

		if (v == null || v.isEmpty()) {return null;}

		MapEntry map = new MapEntry();

		for (String key : v.keySet()) {
			map.addEntry(key, v.get(key));
		}

		return map;
	}

	@Override
	public Map<String, Object> unmarshal(MapEntry v) throws Exception {
		if (v == null) {return null;}

		Map<String, Object> map = new HashMap<>(v.entry.size());

		for(MapElements entry: v.entry) {
			if (entry.value instanceof Element) {
				map.put(entry.key, ((Element)entry.value).getTextContent());
			} else {
				map.put(entry.key, entry.value);
			}
		}

		return map;
	}
}
