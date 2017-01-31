/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An object that stores data for rollbacks.  Data is organized by type.  A
 * type is simply a string identifier.  Multiple types of data may be stored
 * in the same object for separate rollback operations.
 */
public class RollbackData implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, Map<String, Serializable>> dictionary =
		new HashMap<String, Map<String, Serializable>>();
	
	/**
	 * Returns true if the specified type is stored in this object.
	 * @param type the data type
	 */
	public boolean hasType(String type) {
		return dictionary.containsKey(type);
	}

	/**
	 * Stores a single item.
	 * @param type the data type
	 * @param key the key
	 * @param value the value
	 */
	public void put(String type, String key, String value) {
		Map<String, Serializable> mapForType = dictionary.get(type);

		if (mapForType == null) {
			mapForType = new HashMap<String, Serializable>();
			dictionary.put(type, mapForType);
		}

		mapForType.put(key, value);
	}

	/**
	 * Gets a single item.
	 * @param type the data type
	 * @param key the key
	 * @return the item or null if there is no item for the specified type and key
	 */
	public Serializable get(String type, String key) {
		Map<String, Serializable> mapForType = dictionary.get(type);

		if (mapForType == null) {
			return null;
		}

		return mapForType.get(key);
	}

	/**
	 * Gets a map containing all items associated with the specified data type.
	 * @param type the data type
	 * @return a map, or null if there are no items associated with the specified
	 *         data type
	 */
	public Map<String, Serializable> get(String type) {
		return dictionary.get(type);
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(getClass().getSimpleName());
		out.append('[');
		boolean hasOne = false;
		for (String type : dictionary.keySet()) {
			if (hasOne) {
				out.append(',');
			}
			out.append(type);
			out.append(dictionary.get(type));
			hasOne = true;
		}
		out.append(']');
		return out.toString();
	}
}
