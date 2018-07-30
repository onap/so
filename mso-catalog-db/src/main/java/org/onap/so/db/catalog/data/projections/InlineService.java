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

package org.onap.so.db.catalog.data.projections;


import java.util.List;
import java.util.Map;

import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "InlineService", types = { Service.class }) 
public interface InlineService { 

  String getModelName(); 
  String getDescription(); 
  String getCreated(); 
  String getModelUUID(); 
  String getModelInvariantUUID(); 
  String getModelVersion();   
  String getServiceType();    
  String getServiceRole();   
  String getEnvironmentContext();    
  String getWorkloadContext();   

  List<InlineNetworks> getNetworkCustomizations();  

  List<InlineVnf> getVnfCustomizations();  

  List<InlineAllottedResources> getAllottedCustomizations();  

  Map<String, ServiceRecipe> getRecipes ();
}