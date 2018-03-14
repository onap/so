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

package org.openecomp.camunda.bpmn.plugin.urnmap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;
import org.openecomp.camunda.bpmn.plugin.urnmap.resources.URNMapPluginRootResource;

 

public class URNMapPlugin  extends AbstractCockpitPlugin{
	public static final String ID = "urnMap-plugin";

	  public String getId() {
	    return ID;
	  }

	  @Override
	  public Set<Class<?>> getResourceClasses() {
	    Set<Class<?>> classes = new HashSet<Class<?>>();

	    classes.add(URNMapPluginRootResource.class);

	    return classes;
	  }

	  @Override
	  public List<String> getMappingFiles() {
		  return Arrays.asList("org/openecomp/camunda/bpm/plugin/urnmap/queries/urnMap.xml");
	  }
}
