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