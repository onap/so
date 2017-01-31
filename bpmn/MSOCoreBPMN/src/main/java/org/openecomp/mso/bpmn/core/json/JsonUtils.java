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

package org.openecomp.mso.bpmn.core.json;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

//import org.openecomp.mso.bpmn.core.BPMNLogger;
import org.openecomp.mso.bpmn.core.xml.XmlTool;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Utility class for JSON processing
 * 
 * @version 1.0
 */

public class JsonUtils {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static int MSOJsonIndentFactor = 3;

	/**
	 * Uses the JSONObject static method to convert a XML doc to JSON.
	 *
	 * @param  xml		String containing the XML doc
	 * @param  pretty	flag to determine if the output should be formatted
	 * @return String containing the JSON translation
	 */
	public static String xml2json(String xml, Boolean pretty) {
//		String isDebugLogEnabled = "true";
		try {
			// name spaces cause problems, so just remove them
			JSONObject jsonObj = XML.toJSONObject(XmlTool.removeNamespaces(xml));
			if (!pretty) {
				return jsonObj.toString();
			} else {
				// add an indent to make it 'pretty'
				return jsonObj.toString(MSOJsonIndentFactor);
			}
		} catch (Exception e){
				msoLogger.debug("xml2json(): unable to parse xml and convert to json. Exception was: " + e.toString());
				return null;
		}
	}

	/**
	 * Invokes xml2json(String, Boolean) defaulting to 'pretty' output.
	 *
	 * @param  xml	String containing the XML doc
	 * @return String containing the JSON translation
	 */
	public static String xml2json(String xml) {
		return xml2json(xml, true);
	}

	/**
	 * Uses the JSONObject static method to convert a JSON doc to XML.
	 * Note: this method will not generate valid XML if the JSONObject
	 * contains JSONArrays which are used to represent XML attributes
	 * in the JSON doc.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  pretty 	flag to determine if the output should be formatted
	 * @return String containing the XML translation
	 */
	public static String json2xml(String jsonStr, Boolean pretty) {
//		String isDebugLogEnabled = "true";
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			if (pretty) {
				return XmlTool.normalize(XML.toString(jsonObj));
			} else {
				return XML.toString(jsonObj);
			}
		} catch (Exception e){
				msoLogger.debug("json2xml(): unable to parse json and convert to xml. Exception was: " + e.toString());
				return null;
		}
	}
	
	/**
	 * Invokes json2xml(String, Boolean) defaulting to 'pretty' output.
	 *
	 * @param  jsonStr	String containing the XML doc
	 * @return String containing the JSON translation
	 */
	public static String json2xml(String jsonStr) {
		return json2xml(jsonStr, true);
	}

	/**
	 * Uses the JSONObject static method to convert a JSON doc to XML.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @return Iterator over the JSON keys
	 */
	public static Iterator <String> getJsonIterator(String jsonStr) {
//		String isDebugLogEnabled = "true";
		try {
			JSONObject json = new JSONObject(jsonStr);
			return json.keys();
			
		} catch (Exception e){
				msoLogger.debug("getJsonIterator(): unable to parse json to retrieve the keys iterator. Exception was: " + e.toString());
				return null;
		}
	}

	/**
	 * Invokes the getJsonRawValue() method and returns the String equivalent of
	 * the object returned.
	 * 
	 * TBD: May need separate methods for boolean, float, and integer fields if the
	 * String representation is not sufficient to meet client needs.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @return String field value associated with keys
	 */
	public static String getJsonValue(String jsonStr, String keys) {
//		String isDebugLogEnabled = "true";
		try {
				Object rawValue = getJsonRawValue(jsonStr, keys);
				if (rawValue == null) {
					return null;
				} else {
					if (rawValue instanceof String) {
						msoLogger.debug("getJsonValue(): the raw value is a String Object=" + ((String) rawValue).toString());
						return (String) rawValue;
					} else {
						msoLogger.debug("getJsonValue(): the raw value is NOT a String Object=" + rawValue.toString());
						return rawValue.toString();
					}
				}
		} catch (Exception e) {
				msoLogger.debug("getJsonValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString());
		}
		return null;
	}

	/**
	 * Invokes the getJsonRawValue() method to obtain the JSONArray associated with
	 * the specified keys. The JSONArray is then walked to retrieve the content value of
	 * the specified field name.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @param  name		field name for the param to be retrieved
	 * @return String param value associated with field name
	 */
	public static String getJsonParamValue(String jsonStr, String keys, String name) {
//		String isDebugLogEnabled = "true";
		try {
			Object rawValue = getJsonRawValue(jsonStr, keys);
			if (rawValue == null) {
				return null;
			} else {
				if (rawValue instanceof JSONArray) {
					msoLogger.debug("getJsonParamValue(): keys=" + keys + " points to JSONArray: " + ((JSONArray) rawValue).toString());
					for (int i = 0; i < ((JSONArray) rawValue).length(); i++) {
						msoLogger.debug("getJsonParamValue(): index: " + i + ", value: " + ((JSONArray) rawValue).get(i).toString());
						if (((JSONArray) rawValue).get(i) instanceof JSONObject) {
							msoLogger.debug("getJsonParamValue(): index: " + i + " is a JSONObject");
							JSONObject jsonObj = (JSONObject)((JSONArray) rawValue).get(i);
							if (jsonObj.get("name").equals(name)) {
								msoLogger.debug("getJsonParamValue(): found value: " + (String) jsonObj.get("content") + " for name: " + name);
								return (String) jsonObj.get("content");
							}
						} else {
							msoLogger.debug("getJsonParamValue(): the JSONArray element is NOT a JSONObject=" + rawValue.toString());
							return null;
						}
					}
					msoLogger.debug("getJsonParamValue(): content value NOT found for name: " + name);
					return null;
				} else {
					msoLogger.debug("getJsonParamValue(): the raw value is NOT a JSONArray Object=" + rawValue.toString());
					return null;
				}
			}
		} catch (JSONException je) {
				// JSONObject::get() throws this exception if one of the specified keys is not found
				msoLogger.debug("getJsonParamValue(): caught JSONException attempting to retrieve param value for keys:" + keys + ", name=" + name);
		} catch (Exception e) {
				msoLogger.debug("getJsonParamValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString());
		}
		return null;
	}

	/**
	 * Wrapper to generate the JSONObject to pass to the getJsonValueForKey(JSONObject, String)
	 * method so that recursion over the subobjects can be supported there
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  key		key to the target value
	 * @return String field value associated with key
	 */
	public static String getJsonValueForKey(String jsonStr, String key) {
//		String isDebugLogEnabled = "true";
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			if (jsonObj != null) {
				return getJsonValueForKey(jsonObj, key);
			}
		} catch (Exception e) {
				msoLogger.debug("getJsonValueForKey(): unable to parse json to retrieve value for field=" + key + ". Exception was: " + e.toString());
		}
		return null;
	}

	/**
	 * Walks the JSONObject (and sub-objects recursively), searching for the first value associated with the
	 * single key/field name specified. Returns the associated value if found or null if the key is not found
	 *
	 * @param  jsonObj	JSONObject representation of the the JSON doc
	 * @param  key		key to the target value
	 * @return String field value associated with key
	 */
	public static String getJsonValueForKey(JSONObject jsonObj, String key) {
//		String isDebugLogEnabled = "true";
		String keyValue = null;
		try {
			if (jsonObj.has(key)) {
				msoLogger.debug("getJsonValueForKey(): found value for key=" + key);
				return ((String) jsonObj.get(key));
			} else {
				msoLogger.debug("getJsonValueForKey(): iterating over the keys");
				Iterator <String> itr = jsonObj.keys();
				while (itr.hasNext()) {
					String nextKey = (String) itr.next();
					Object obj = jsonObj.get(nextKey);
					if (obj instanceof JSONObject) {
						msoLogger.debug("getJsonValueForKey(): key=" + nextKey + ", points to JSONObject, recursive call");
						keyValue = getJsonValueForKey((JSONObject) obj, key);
						if (keyValue != null) {
							msoLogger.debug("getJsonValueForKey(): found value=" + keyValue + ", for key=" + key);
							break;
						}
					} else {
						msoLogger.debug("getJsonValueForKey(): key=" + nextKey + ", does not point to a JSONObject, next key");
					}
				}
			}
		} catch (JSONException je) {
				// JSONObject::get() throws this exception if one of the specified keys is not found
				msoLogger.debug("getJsonValueForKey(): caught JSONException attempting to retrieve value for key=" + key);
				keyValue = null;
		} catch (Exception e) {
				msoLogger.debug("getJsonValueForKey(): unable to parse json to retrieve value for field=" + key + ". Exception was: " + e.toString());
		}
		return keyValue;
	}
	
	/**
	 * Boolean method to determine if a key path is valid for the JSON doc. Invokes
	 * getJsonValue().
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @return Boolean true if keys points to value in the JSON doc
	 */
	public static Boolean jsonValueExists(String jsonStr, String keys) {
		if (getJsonRawValue(jsonStr, keys) == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Inserts the new key/value pair at the appropriate location in the JSON
	 * document after first determining if keyed field already exists. If
	 * it does exist, return the JSON unmodified, otherwise return the new JSON
	 * Note: this method currently only supports String value inserts.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the value to be added in the format of "key1.key2.key3..."
	 * @return String containing the updated JSON doc
	 */
	public static String addJsonValue(String jsonStr, String keys, String value) {
//		String isDebugLogEnabled = "true";
		// only attempt to insert the key/value pair if it does not exist
		if (!jsonValueExists(jsonStr, keys)) {
			return putJsonValue(jsonStr, keys, value);
		} else {
			msoLogger.debug("addJsonValue(): JSON add failed, key=" + keys + "/value=" + (String) value + " already exists");
			return jsonStr;
		}
	}

	/**
	 * Updates the value for the specified key in the JSON document
	 * after first determining if keyed field exists. If it does
	 * not exist, return the JSON unmodified, otherwise return the updated JSON.
	 * Note: this method currently only supports String value updates.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the value to be updated in the format of "key1.key2.key3..."
	 * @return String containing the updated JSON doc
	 */
	public static String updJsonValue(String jsonStr, String keys, String newValue) {
//		String isDebugLogEnabled = "true";
		// only attempt to modify the key/value pair if it exists
		if (jsonValueExists(jsonStr, keys)) {
			return putJsonValue(jsonStr, keys, newValue);
		} else {
			msoLogger.debug("updJsonValue(): JSON update failed, no value exists for key=" + keys);
			return jsonStr;
		}
	}

	/**
	 * Deletes the value for the specified key in the JSON document
	 * after first determining if keyed field exists. If it does
	 * not exist, return the JSON unmodified, otherwise return the updated JSON
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the value to be deleted in the format of "key1.key2.key3..."
	 * @return String containing the updated JSON doc
	 */
	public static String delJsonValue(String jsonStr, String keys) {
//		String isDebugLogEnabled = "true";
		// only attempt to remove the key/value pair if it exists
		if (jsonValueExists(jsonStr, keys)) {
			// passing a null value results in a delete
			return putJsonValue(jsonStr, keys, null);
		} else {
			msoLogger.debug("delJsonValue(): JSON delete failed, no value exists for key=" + keys);
			return jsonStr;
		}
	}

	/**
	 * Walks the JSON doc using the full key path to retrieve the associated
	 * value. All but the last key points to the 'parent' object name(s) in order
	 * in the JSON hierarchy with the last key pointing to the target value.
	 * The value returned is a Java object.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @return Object field value associated with keys
	 */
	private static Object getJsonRawValue(String jsonStr, String keys) {
//		String isDebugLogEnabled = "true";
		String keyStr = "";
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			StringTokenizer keyTokens = new StringTokenizer(keys, ".");
			while (keyTokens.hasMoreElements()) {
				keyStr = keyTokens.nextToken();
				Object keyValue = jsonObj.get(keyStr);
				if (keyValue instanceof JSONObject) {
					msoLogger.debug("getJsonRawValue(): key=" + keyStr + " points to json object");
					jsonObj = (JSONObject) keyValue;
				} else {
					if (keyTokens.hasMoreElements()) {
						msoLogger.debug("getJsonRawValue(): value found prior to last key for key=" + keyStr);
					}
					return keyValue;
				}
			}
			// we should not hit this point: either the key points to a valid value and
			// we return it above or the key is invalid and we handle the JSONException
			// below and return null
			return null;
			
		} catch (JSONException je) {
				// JSONObject::get() throws this exception if one of the specified keys is not found
				msoLogger.debug("getJsonRawValue(): caught JSONException attempting to retrieve raw value for key=" + keyStr);
		} catch (Exception e) {
				msoLogger.debug("getJsonRawValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString());
		}
		return null;
	}

	/**
	 * Private method invoked by the public add, update, and delete methods.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the value to be deleted in the format of "key1.key2.key3..."
	 * @return String containing the updated JSON doc
	 */
	private static String putJsonValue(String jsonStr, String keys, String value) {		
//		String isDebugLogEnabled = "true";
		String keyStr = "";
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			JSONObject jsonObjOut = jsonObj;
			StringTokenizer keyTokens = new StringTokenizer(keys, ".");
			while (keyTokens.hasMoreElements()) {
				keyStr = keyTokens.nextToken();
				if (keyTokens.hasMoreElements()) {
					Object keyValue = jsonObj.get(keyStr);
					if (keyValue instanceof JSONObject) {
						msoLogger.debug("putJsonValue(): key=" + keyStr + " points to json object");
						jsonObj = (JSONObject) keyValue;
					} else {
						msoLogger.debug("putJsonValue(): key=" + keyStr + " not the last key but points to non-json object: " + (String) keyValue);
						return null;
					}
				} else { // at the last/new key value
					jsonObj.put(keyStr, value);
					return jsonObjOut.toString(3);
				}
			}
			// should not hit this point if the key points to a valid key value
			return null;
			
		} catch (JSONException je) {
				// JSONObject::get() throws this exception if one of the specified keys is not found
				msoLogger.debug("putJsonValue(): caught JSONException attempting to retrieve value for key=" + keyStr);
				return null;
		} catch (Exception e) {
				msoLogger.debug("putJsonValue(): unable to parse json to put value for key=" + keys + ". Exception was: " + e.toString());
		}
		return null;
	}
}

