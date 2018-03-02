package org.openecomp.mso.client.aai.entities.uri.parsers;

import java.util.Map;
import java.util.Set;

public interface UriParser {
  public Set<String> getVariables();
  public Map<String, String> parse(final String uri);
}