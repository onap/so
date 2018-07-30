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


import java.sql.Timestamp;

import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "InlineNetworks", types = { NetworkResourceCustomization.class }) 
public interface InlineNetworks { 

  String getModelCustomizationUUID(); 
  String getModelInstanceName();
  String getNetworkTechnology();
  String getNetworkType();   
  String getNetworkScope(); 
  String getNetworkRole();   
  
  @Value("#{target.getNetworkResource().getDescription()}")
  String getDescription();
  
  @Value("#{target.getNetworkResource().getCreated()}")
  Timestamp getCreated();
  
  @Value("#{target.getNetworkResource().getModelVersion()}")
  String getModelVersion();
  
  @Value("#{target.getNetworkResource().getModelInvariantUUID()}")
  String getModelInvariantUUID();
  
  @Value("#{target.getNetworkResource().getModelName()}")
  String getModelName ();
  
  @Value("#{target.getNetworkResource().getModelUUID()}")
  String getModelUUID ();  

  @Value("#{target.getNetworkResource().getNeutronNetworkType()}")
  String getNeutronNetworkType ();
  
  @Value("#{target.getNetworkResource().getAicVersionMin()}")
  String getAicVersionMin ();
  
  @Value("#{target.getNetworkResource().getAicVersionMax()}")
  String getAicVersionMax ();
  
  @Value("#{target.getNetworkResource().getOrchestrationMode()}")
  String getOrchestrationMode ();
  
  @Value("#{target.getNetworkResource().getToscaNodeType()}")
  String getToscaNodeType ();
}