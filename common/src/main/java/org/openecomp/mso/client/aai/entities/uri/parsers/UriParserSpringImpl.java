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

package org.openecomp.mso.client.aai.entities.uri.parsers;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UriUtils;

public class UriParserSpringImpl implements UriParser {

  private final UriTemplate uriTemplate;

  public UriParserSpringImpl(final String template) {
    this.uriTemplate = new UriTemplate(template);
  }

  @Override
  public Map<String, String> parse(final String uri) {
    final boolean match = this.uriTemplate.matches(uri);
    if (!match) {
      return new LinkedHashMap<>();
    }
    return Collections.unmodifiableMap(decodeParams(this.uriTemplate.match(uri)));
  }

  @Override
  public Set<String> getVariables() {
    return Collections.unmodifiableSet(new LinkedHashSet<String>(this.uriTemplate.getVariableNames()));
  }
  
  protected Map<String, String> decodeParams(Map<String, String> map) {
	  final Map<String, String> result = new LinkedHashMap<>();
	  
	  for (Entry<String, String> entry : map.entrySet()) {
		  try {
			result.put(entry.getKey(), UriUtils.decode(entry.getValue(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			result.put(entry.getKey(), "");
		}
	  }
	  
	  return result;
  }
}