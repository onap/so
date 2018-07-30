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

import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "InlineVfModules", types = { VfModuleCustomization.class }) 
public interface InlineVfModules { 

  String getModelCustomizationUUID(); 
  
  @Value("#{target.getVfModule().getModelName()}")
  String getModelName ();
  
  @Value("#{target.getVfModule().getModelUUID()}")
  String getModelUUID ();
  
  @Value("#{target.getVfModule().getModelInvariantUUID()}")
  String getModelInvariantUUID ();
  
  @Value("#{target.getVfModule().getModelVersion()}")
  String getModelVersion ();
  
  @Value("#{target.getVfModule().getDescription()}")
  String getDescription();
  
  @Value("#{target.getVfModule().getIsBase()}")
  Boolean getIsBase();  
  
  String getMinInstances(); 
  String getMaxInstances(); 
  String getAvailabilityZoneCount(); 
  String getLabel(); 
  String getInitialCount();  

  @Value("#{target.getVfModule().getCreated()}")
  String getCreated();     

}