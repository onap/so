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

import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "InlineAllottedResources", types = { AllottedResourceCustomization.class }) 
public interface InlineAllottedResources { 

  String getModelCustomizationUUID(); 
  String getModelInstanceName(); 
  String getMaxInstances(); 
  String getMinInstances(); 
  String getNfNamingCode(); 
  String getNfRole(); 
  String getNfType(); 
  String getNfFunction(); 
  String getTargetNetworkRole(); 
  String getProvidingServiceModelInvariantUUID(); 
  String getProvidingServiceModelName(); 
  String getProvidingServiceModelUUID();   
  
  @Value("#{target.getAllottedResource().getDescription()}")
  String getDescription();
  
  @Value("#{target.getAllottedResource().getCreated()}")
  String getCreated(); 

  
  @Value("#{target.getAllottedResource().getModelInvariantUUID()}")
  String getModelInvariantUuid ();
  
  @Value("#{target.getAllottedResource().getModelName()}")
  String getModelName ();
  
  @Value("#{target.getAllottedResource().getModelUUID()}")
  String getModelUuid ();  

  @Value("#{target.getAllottedResource().getToscaNodeType()}")
  String getToscaNodeType ();
  
  @Value("#{target.getAllottedResource().getSubcategory()}")
  String getSubcategory ();
}