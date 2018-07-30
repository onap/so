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

import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "InlineVnf", types = { VnfResourceCustomization.class }) 
public interface InlineVnf { 
 
  String getModelInstanceName(); 
  
  String getModelCustomizationUUID(); 
  
  @Value("#{target.getVnfResources().getModelUUID()}")
  String getModelUUID();
  
  @Value("#{target.getVnfResources().getModelInvariantUUID()}")
  String getModelInvariantUUID();
  
  @Value("#{target.getVnfResources().getModelName()}")
  String getModelName();    
  
  @Value("#{target.getVnfResources().getModelVersion()}")
  String getModelVersion();    
  
  @Value("#{target.getVnfResources().getToscaNodeType()}")
  String getToscaNodeType ();
  
  @Value("#{target.getVnfResources().getDescription()}")
  String getDescription();
  
  @Value("#{target.getVnfResources().getOrchestrationMode()}")
  String getOrchestrationMode();
  
  @Value("#{target.getVnfResources().getAicVersionMin()}")
  String getAicVersionMin();
  
  @Value("#{target.getVnfResources().getAicVersionMax()}")
  String getAicVersionMax();
  
  String getMinInstances(); 
  String getMaxInstances(); 
  String getAvailabilityZoneMaxCount(); 
  String getNfFunction(); 
  String getNfType(); 
  String getNfRole(); 
  String getNfNamingCode(); 
  String getMultiStageDesign(); 
  
  @Value("#{target.getVnfResources().getCreated()}")
  String getCreated(); 
  
  List<InlineVfModules> getVfModuleCustomizations();  
}