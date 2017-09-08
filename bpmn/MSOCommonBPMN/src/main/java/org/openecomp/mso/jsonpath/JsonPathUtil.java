package org.openecomp.mso.jsonpath;

import java.util.Optional;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import net.minidev.json.JSONArray;

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
