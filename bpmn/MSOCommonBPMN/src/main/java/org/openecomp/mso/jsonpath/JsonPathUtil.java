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

package org.openecomp.mso.jsonpath;

import java.util.Optional;

public class JsonPathUtil {

	
	private final Configuration conf;
	
	private JsonPathUtil() {
		conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
	}
	
	private static class Helper {
		private static final JsonPathUtil INSTANCE = new JsonPathUtil();
	}
	
	public static JsonPathUtil getInstance() {
		return Helper.INSTANCE;
	}
	public boolean pathExists(String json, String jsonPath) {
		return !JsonPath.using(conf).parse(json).<JSONArray>read(jsonPath).isEmpty();
	}
	
	public <T> Optional<T> locateResult(String json, String jsonPath) {
		final JSONArray result = JsonPath.using(conf).parse(json).read(jsonPath);
		if (result.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of((T)result.get(0));
		}
	}
}
