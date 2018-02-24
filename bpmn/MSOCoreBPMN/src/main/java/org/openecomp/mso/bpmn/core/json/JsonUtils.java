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

package org.openecomp.mso.bpmn.core.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.camunda.bpm.engine.runtime.Execution;
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
				msoLogger.debug("xml2json(): unable to parse xml and convert to json. Exception was: " + e.toString(), e);
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
	 * Note: this method may not generate valid XML if the JSONObject
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
//				return XmlTool.normalize(XML.toString(jsonObj));
//				use the local class method which properly handles certain JSONArray content
				return XmlTool.normalize(toXMLString(jsonObj, null));
			} else {
//				return XML.toString(jsonObj);
//				use the local class method which properly handles certain JSONArray content
				return toXMLString(jsonObj, null);
			}
		} catch (Exception e){
				msoLogger.debug("json2xml(): unable to parse json and convert to xml. Exception was: " + e.toString(), e);
				return null;
		}
	}

	/**
	 * Uses a modified version of the org.json.XML toString() algorithm
	 * to convert a JSONObject to an XML Doc. The intent of this is to
	 * correctly generate XML from JSON including TAGs for JSONArrays
	 *
	 * @param  jsonObj	org.json.JSON object to be converted to XML
	 * @param  tagName 	optional XML tagname supplied primarily during recursive calls
	 * @return String containing the XML translation
	 */
	public static String toXMLString(Object obj, String tagName) throws JSONException {
		StringBuilder strBuf = new StringBuilder();
		int i;
		JSONArray jsonArr;
		JSONObject jsonObj;
		String key;
		Iterator<String> keys;
		int len;
		String str;
		Object curObj;
		if (obj instanceof JSONObject) {
			// msoLogger.debug("toXMLString(): is a JSONObject");
			// append "<tagName>" to the XML output
			if (tagName != null) {
//				msoLogger.debug("toXMLString(): adding opening tagName: " + tagName);
				strBuf.append("<");
				strBuf.append(tagName);
				strBuf.append(">");
			}
			// iterate thru the keys.
			jsonObj = (JSONObject) obj;
			keys = jsonObj.keys();
			while (keys.hasNext()) {
				key = keys.next();
				// msoLogger.debug("toXMLString(): key is " + k);
				curObj = jsonObj.opt(key);
				if (curObj == null) {
					curObj = "";
				}
				if (curObj instanceof String) {
					str = (String) curObj;
				} else {
					str = null;
				}
				// append the content to the XML output
				if ("content".equals(key)) {
					if (curObj instanceof JSONArray) {
						jsonArr = (JSONArray) curObj;
						len = jsonArr.length();
						for (i = 0; i < len; i += 1) {
							if (i > 0) {
								strBuf.append('\n');
							}
							strBuf.append(XML.escape(jsonArr.get(i).toString()));
						}
					} else {
						strBuf.append(XML.escape(curObj.toString()));
					}
				// append an array of similar keys to the XML output
				} else if (curObj instanceof JSONArray) {
					jsonArr = (JSONArray) curObj;
					len = jsonArr.length();
//					msoLogger.debug("toXMLString(): found JSONArray: " + key + ", size: " + len);
					for (i = 0; i < len; i += 1) {
						curObj = jsonArr.get(i);
						if (curObj instanceof JSONArray) {
//							The XML tags for the nested array should be generated below when this method
//							is called recursively and the JSONArray object is passed
//							strBuf.append("<");
//							strBuf.append(key);
//							strBuf.append(">");
							strBuf.append(toXMLString(curObj, null));
//							strBuf.append("</");
//							strBuf.append(key);
//							strBuf.append(">");
						} else {
//							msoLogger.debug("toXMLString(): recursive call toXML() with tagName null");
							// append the opening tag for the array (before 1st element)
							if (i == 0) {
								strBuf.append("<");
								strBuf.append(key);
								strBuf.append(">");
							}
							// append the opening tag for the array
							strBuf.append(toXMLString(curObj, null));
							// append the closing tag for the array (after last element)
							if (i == (len - 1)) {
								strBuf.append("</");
								strBuf.append(key);
								strBuf.append(">");
							}
						}
					}
				} else if (curObj.equals("")) {
					// append a closing tag "<key>" to the XML output
					strBuf.append("<");
					strBuf.append(key);
					strBuf.append("/>");
				} else {
//					msoLogger.debug("toXMLString(): recursive call toXMLString() with tagName: " + key);
					strBuf.append(toXMLString(curObj, key));
				}
				// msoLogger.debug("toXML(): partial XML: " + strBuf.toString());
			}
			if (tagName != null) {
				// append the closing tag "</tagName>" to the XML output
//				msoLogger.debug("toXMLString(): adding closing tagName: " + tagName);
				strBuf.append("</");
				strBuf.append(tagName);
				strBuf.append(">");
			}
			return strBuf.toString();
		// XML does not have good support for arrays. If an array appears in a place
		// where XML is lacking, synthesize an < array > element.
		} else if (obj instanceof JSONArray) {
			jsonArr = (JSONArray) obj;
			len = jsonArr.length();
			for (i = 0; i < len; ++i) {
				curObj = jsonArr.opt(i);
				strBuf.append(toXMLString(curObj, (tagName == null) ? "array"
						: tagName));
			}
			return strBuf.toString();
		} else {
//			msoLogger.debug("toXML(): in else block with tagName: " + tagName);
			str = (obj == null) ? "null" : XML.escape(obj.toString());
			return (tagName == null) ? "\"" + str + "\""
					: (str.length() == 0) ? "<" + tagName + "/>" : "<"
							+ tagName + ">" + str + "</" + tagName + ">";
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
	 * Formats the JSON String using the value of MSOJsonIndentFactor.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @return String containing the formatted JSON doc
	 */
	public static String prettyJson(String jsonStr) {
//		String isDebugLogEnabled = "true";
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			return jsonObj.toString(MSOJsonIndentFactor);
		} catch (Exception e){
			msoLogger.debug("prettyJson(): unable to parse/format json input. Exception was: " + e.toString(), e);
			return null;
		}
	}

	/**
	 * Returns an Iterator over the JSON keys in the specified JSON doc.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @return Iterator over the JSON keys
	 * @throws JSONException if the doc cannot be parsed
	 */
	public static Iterator <String> getJsonIterator(String jsonStr) throws JSONException {
		return new JSONObject(jsonStr).keys();
	}

	/**
	 * Returns the name of the "root" property in the specified JSON doc. The
	 * "root" property is the single top-level property in the JSON doc. An
	 * exception is thrown if the doc is empty or if it contains more than one
	 * top-level property.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @return the name of the "root" property
	 * @throws JSONException if the doc cannot be parsed, or if it is empty, or if
	 *         it contains more than one top-level property
	 */
	public static String getJsonRootProperty(String jsonStr) throws JSONException {
		Iterator<String> iter = getJsonIterator(jsonStr);

		if (!iter.hasNext()) {
			throw new JSONException("Empty JSON object");
		}

		String rootPropertyName = iter.next();

		if (iter.hasNext()) {
			throw new JSONException("JSON object has more than one root property");
		}

		return rootPropertyName;
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
						msoLogger.debug("getJsonValue(): the raw value is a String Object=" + ((String) rawValue));
						return (String) rawValue;
					} else {
						msoLogger.debug("getJsonValue(): the raw value is NOT a String Object=" + rawValue.toString());
						return rawValue.toString();
					}
				}
		} catch (Exception e) {
				msoLogger.debug("getJsonValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString(),e);
		}
		return null;
	}

	/**
	 * Invokes the getJsonRawValue() method with the wrap flag set to true
	 * and returns the String equivalent of the json node object returned.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @return String field value associated with keys
	 */
	public static String getJsonNodeValue(String jsonStr, String keys) {
//		String isDebugLogEnabled = "true";
		try {
				Object rawValue = getJsonRawValue(jsonStr, keys, true);
				if (rawValue == null) {
					return null;
				} else {
					if (rawValue instanceof String) {
						msoLogger.debug("getJsonNodeValue(): the raw value is a String Object=" + ((String) rawValue));
						return (String) rawValue;
					} else {
						msoLogger.debug("getJsonNodeValue(): the raw value is NOT a String Object=" + rawValue.toString());
						return rawValue.toString();
					}
				}
		} catch (Exception e) {
				msoLogger.debug("getJsonNodeValue(): unable to parse json to retrieve node for field=" + keys + ". Exception was: " + e.toString(), e);
		}
		return null;
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
	public static int getJsonIntValue(String jsonStr, String keys) {
//		String isDebugLogEnabled = "true";
		try {
				Object rawValue = getJsonRawValue(jsonStr, keys);
				if (rawValue == null) {
					return 0;
				} else {
					if (rawValue instanceof Integer) {
						msoLogger.debug("getJsonValue(): the raw value is an Integer Object=" + ((String) rawValue).toString());
						return (Integer) rawValue;
					} else {
						msoLogger.debug("getJsonValue(): the raw value is NOT an Integer Object=" + rawValue.toString());
						return 0;
					}
				}
		} catch (Exception e) {
				msoLogger.debug("getJsonValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString(), e);
		}
		return 0;
	}

	/**
	 * Invokes the getJsonRawValue() method and returns the boolean equivalent of
	 * the object returned.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @return boolean field value associated with keys - default is false
	 */
	public static boolean getJsonBooleanValue(String jsonStr, String keys) {
		try {
				Object rawValue = getJsonRawValue(jsonStr, keys);
				if (rawValue == null) {
					return false;
				} else {
					if (rawValue instanceof Boolean) {
						msoLogger.debug("getJsonValue(): the raw value is a Boolean Object=" + ((String) rawValue));
						return (Boolean) rawValue;
					} else {
						msoLogger.debug("getJsonValue(): the raw value is NOT an Boolean Object=" + rawValue.toString());
						return false;
					}
				}
		} catch (Exception e) {
				msoLogger.debug("getJsonValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString(),e);
		}
		return false;
	}

	/**
	 * Invokes the getJsonParamValue() method to obtain the JSONArray associated with
	 * the specified keys. The JSONArray is then walked to retrieve the first array
	 * value associated with the specified field name (index=0).
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @param  name		field name for the param to be retrieved
	 * @return String param value associated with field name
	 */
	public static String getJsonParamValue(String jsonStr, String keys, String name) {
		return getJsonParamValue(jsonStr, keys, name, 0);
	}

	/**
	 * Invokes the getJsonRawValue() method to obtain the JSONArray associated with
	 * the specified keys. The JSONArray is then walked to retrieve the nth array
	 * value associated with the specified field name and index.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @param  name		field name for the param to be retrieved
	 * @param  index    the nth param associated with name starting at 0
	 * @return String param value associated with field name
	 */
	public static String getJsonParamValue(String jsonStr, String keys, String name, int index) {
//		String isDebugLogEnabled = "true";
		try {
			Object rawValue = getJsonRawValue(jsonStr, keys);
			if (rawValue == null) {
				return null;
			} else {
				if (rawValue instanceof JSONArray) {
					msoLogger.debug("getJsonParamValue(): keys=" + keys + " points to JSONArray: " + ((JSONArray) rawValue).toString());
					int arrayLen = ((JSONArray) rawValue).length();
					if (index < 0 || arrayLen < index+1) {
						msoLogger.debug("getJsonParamValue(): index: " + index + " is out of bounds for array size of " + arrayLen);
						return null;
					}
					int foundCnt = 0;
					for (int i = 0; i < arrayLen; i++) {
						msoLogger.debug("getJsonParamValue(): index: " + i + ", value: " + ((JSONArray) rawValue).get(i).toString());
						if (((JSONArray) rawValue).get(i) instanceof JSONObject) {
							msoLogger.debug("getJsonParamValue(): index: " + i + " is a JSONObject");
							JSONObject jsonObj = (JSONObject)((JSONArray) rawValue).get(i);
							String parmValue = jsonObj.get(name).toString();
							if (parmValue != null) {
								msoLogger.debug("getJsonParamValue(): found value: " + parmValue + " for name: " + name + " and index: " + i);
								if (foundCnt == index) {
									return parmValue;
								} else {
									foundCnt++;
									continue;
								}
							} else {
								continue;
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
				msoLogger.debug("getJsonParamValue(): caught JSONException attempting to retrieve param value for keys:" + keys + ", name=" + name, je);
		} catch (Exception e) {
				msoLogger.debug("getJsonParamValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString(), e);
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
			return getJsonValueForKey(jsonObj, key);
		} catch (Exception e) {
				msoLogger.debug("getJsonValueForKey(): unable to parse json to retrieve value for field=" + key + ". Exception was: " + e.toString(), e);
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
				Object value = jsonObj.get(key);
				if (value == null)
					return null;
				else
					return ((String) value);
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
				msoLogger.debug("getJsonValueForKey(): caught JSONException attempting to retrieve value for key=" + key, je);
				keyValue = null;
		} catch (Exception e) {
				msoLogger.debug("getJsonValueForKey(): unable to parse json to retrieve value for field=" + key + ". Exception was: " + e.toString(), e);
		}
		return keyValue;
	}

	/**
	 * Walks the JSONObject (and sub-objects recursively), searching for the first value associated with the
	 * single key/field name specified. Returns the associated value if found or null if the key is not found
	 *
	 * @param  jsonObj	JSONObject representation of the the JSON doc
	 * @param  key		key to the target value
	 * @return String field value associated with key
	 */
	public static Integer getJsonIntValueForKey(JSONObject jsonObj, String key) {
//		String isDebugLogEnabled = "true";
		Integer keyValue = 0;
		try {
			if (jsonObj.has(key)) {
				msoLogger.debug("getJsonValueForKey(): found value for key=" + key);
				return (Integer) jsonObj.get(key);
			} else {
				msoLogger.debug("getJsonValueForKey(): iterating over the keys");
				Iterator <String> itr = jsonObj.keys();
				while (itr.hasNext()) {
					String nextKey = (String) itr.next();
					Object obj = jsonObj.get(nextKey);
					if (obj instanceof JSONObject) {
						msoLogger.debug("getJsonValueForKey(): key=" + nextKey + ", points to JSONObject, recursive call");
						keyValue = getJsonIntValueForKey((JSONObject) obj, key);
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
				msoLogger.debug("getJsonValueForKey(): caught JSONException attempting to retrieve value for key=" + key, je);
				keyValue = null;
		} catch (Exception e) {
				msoLogger.debug("getJsonValueForKey(): unable to parse json to retrieve value for field=" + key + ". Exception was: " + e.toString(),e);
		}
		return keyValue;
	}

	/**
	 * Walks the JSONObject (and sub-objects recursively), searching for the first value associated with the
	 * single key/field name specified. Returns the associated value if found or null if the key is not found
	 *
	 * @param  jsonObj	JSONObject representation of the the JSON doc
	 * @param  key		key to the target value
	 * @return String field value associated with key
	 */
	public static Boolean getJsonBooleanValueForKey(JSONObject jsonObj, String key) {
		Boolean keyValue = false;
		try {
			if (jsonObj.has(key)) {
				msoLogger.debug("getJsonBooleanValueForKey(): found value for key=" + key);
				return (Boolean) jsonObj.get(key);
			} else {
				msoLogger.debug("getJsonBooleanValueForKey(): iterating over the keys");
				Iterator <String> itr = jsonObj.keys();
				while (itr.hasNext()) {
					String nextKey = (String) itr.next();
					Object obj = jsonObj.get(nextKey);
					if (obj instanceof JSONObject) {
						msoLogger.debug("getJsonBooleanValueForKey(): key=" + nextKey + ", points to JSONObject, recursive call");
						keyValue = getJsonBooleanValueForKey((JSONObject) obj, key);
						if (keyValue != null) {
							msoLogger.debug("getJsonBooleanValueForKey(): found value=" + keyValue + ", for key=" + key);
							break;
						}
					} else {
						msoLogger.debug("getJsonBooleanValueForKey(): key=" + nextKey + ", does not point to a JSONObject, next key");
					}
				}
			}
		} catch (JSONException je) {
				// JSONObject::get() throws this exception if one of the specified keys is not found
				msoLogger.debug("getJsonBooleanValueForKey(): caught JSONException attempting to retrieve value for key=" + key,je);
				keyValue = null;
		} catch (Exception e) {
				msoLogger.debug("getJsonBooleanValueForKey(): unable to parse json to retrieve value for field=" + key + ". Exception was: " + e.toString(),e);
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
		return getJsonRawValue(jsonStr, keys, false);
	}

	/**
	 * Walks the JSON doc using the full key path to retrieve the associated
	 * value. All but the last key points to the 'parent' object name(s) in order
	 * in the JSON hierarchy with the last key pointing to the target value.
	 * The value returned is a Java object.
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @param  wrap		Boolean which determines if returned JSONObjects sould be "wrapped"
	 *                  Note: wrap does not apply to returned scalar values
	 * @return Object field value associated with keys
	 */
	private static Object getJsonRawValue(String jsonStr, String keys, Boolean wrap) {
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
			// return the json 'node' that the key points to
			// note: since this is a json object and not a scalar value,
			//       use the wrap flag to determine if the object should
			//       be wrapped with a root node value
			//       (the last key in the keys String)
			if (wrap) {
				JSONObject wrappedJsonObj = new JSONObject();
				wrappedJsonObj.put(keyStr, jsonObj);
				return wrappedJsonObj.toString();
			} else {
				return jsonObj.toString();
			}

		} catch (JSONException je) {
				// JSONObject::get() throws this exception if one of the specified keys is not found
				msoLogger.debug("getJsonRawValue(): caught JSONException attempting to retrieve raw value for key=" + keyStr,je);
		} catch (Exception e) {
				msoLogger.debug("getJsonRawValue(): unable to parse json to retrieve value for field=" + keys + ". Exception was: " + e.toString(),e);
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
				msoLogger.debug("putJsonValue(): caught JSONException attempting to retrieve value for key=" + keyStr,je);
				return null;
		} catch (Exception e) {
				msoLogger.debug("putJsonValue(): unable to parse json to put value for key=" + keys + ". Exception was: " + e.toString(),e);
		}
		return null;
	}

	/**
	 * This json util method converts a json array of Key Value
	 * pair objects into a Java Map.
	 *
	 * @param execution
	 * @param entryArray - the getJsonValue of a json Array of key/value pairs
	 *
	 * @return Map - a Map containing the entries
	 */
	public Map<String, String> entryArrayToMap(Execution execution, String entryArray) {
		msoLogger.debug("Started Entry Array To Map Util Method");

		Map<String, String> map = new HashMap<>();

		//Populate Map
		String entryListJson = "{ \"entry\":" + entryArray + "}";
		JSONObject obj = new JSONObject(entryListJson);
		JSONArray arr = obj.getJSONArray("entry");
		for (int i = 0; i < arr.length(); i++){
			JSONObject jo = arr.getJSONObject(i);
			String key = jo.getString("key");
			String value =jo.getString("value");
			map.put(key, value);
		}
		msoLogger.debug("Outgoing Map is: " + map);
		msoLogger.debug("Completed Entry Array To Map Util Method");
		return map;
	}

	/**
	 * This json util method converts a json array of Key Value
	 * pair objects into a Java Map.
	 *
	 * @param execution
	 * @param entryArray - the getJsonValue of a json Array of key/value pairs
	 * @param keyNode - the name of the node that represents the key
	 * @param valueNode - the name of the node that represents the value
	 *
	 * @return Map - a Map containing the entries
	 *
	 */
	public Map<String, String> entryArrayToMap(Execution execution, String entryArray, String keyNode, String valueNode) {
		msoLogger.debug("Started Entry Array To Map Util Method");

		Map<String, String> map = new HashMap<>();
		//Populate Map
		String entryListJson = "{ \"entry\":" + entryArray + "}";
		JSONObject obj = new JSONObject(entryListJson);
		JSONArray arr = obj.getJSONArray("entry");
		for (int i = 0; i < arr.length(); i++){
			JSONObject jo = arr.getJSONObject(i);
			String key = jo.getString(keyNode);
			String value =jo.getString(valueNode);
			map.put(key, value);
		}
		msoLogger.debug("Outgoing Map is: " + map);
		msoLogger.debug("Completed Entry Array To Map Util Method");
		return map;
	}

	/**
	 * This json util method converts a json Array of Strings
	 * to a Java List. It takes each String in the json Array
	 * and puts it in a Java List<String>.
	 *
	 * @param execution
	 * @param stringArray - the getJsonValue of a json array of strings
	 *
	 * @return List - a java list containing the strings
	 *
	 *
	 */
	public List<String> StringArrayToList(Execution execution, String jsonArrayOfStrings) {
		msoLogger.debug("Started  String Array To List Util Method");

		List<String> list = new ArrayList<>();
		//Populate List
		String stringListJson = "{ \"strings\":" + jsonArrayOfStrings + "}";
		JSONObject obj = new JSONObject(stringListJson);
		JSONArray arr = obj.getJSONArray("strings");
		for (int i = 0; i < arr.length(); i++){
			String s = arr.get(i).toString();
			list.add(s);
		}
		msoLogger.debug("Outgoing List is: " + list);
		msoLogger.debug("Completed String Array To List Util Method");
		return list;
	}

	/**
	 *
	 * Invokes the getJsonRawValue() method to determine if the
	 * json element/variable exist. Returns true if the
	 * json element exist
	 *
	 * @param  jsonStr	String containing the JSON doc
	 * @param  keys		full key path to the target value in the format of "key1.key2.key3..."
	 * @return boolean field value associated with keys
	 *
	 *
	 */
	public static boolean jsonElementExist(String jsonStr, String keys) {

		try {
			Object rawValue = getJsonRawValue(jsonStr, keys);

			return !(rawValue == null);

		} catch (Exception e) {
				msoLogger.debug("jsonElementExist(): unable to determine if json element exist. Exception is: " + e.toString(),e);
		}
		return true;
	}

}