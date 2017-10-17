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

package org.openecomp.camunda.bpmn.plugin.urnmap.resources;

import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.openecomp.camunda.bpmn.plugin.urnmap.db.*;

public class ProcessInstanceResource extends AbstractPluginResource {

  private static final Logger log = LoggerFactory.getLogger(ProcessInstanceResource.class);

  public ProcessInstanceResource(String engineName) {
    super(engineName);
  }

  @GET
  public List<URNData> getUrnDataMap() {    
    List<URNData> list = getQueryService()
            .executeQuery(
                    "cockpit.urnMap.retrieveUrnKeyValuePair",
                    new QueryParameters<URNData>());
    
    log.info("urnmap-plugin project - Results Retrieved: ");
    log.info("URNName: " + "		" + "URNValue: " );
    for(URNData d: list)
    {
    	log.info(  d.getURNName() + "		"  + d.getURNValue());
    }
   
    return list;
  }
  
  @PUT
  //public void insertNewRow(String key, String value) 
  public void insertNewRow(String temp) 
   {  
 	 log.info("AddNewRow: XXXXXXXXXXXXXXXXX ---> " + temp);
 	 StringTokenizer st = new StringTokenizer(temp, "|");
 	 String key = "";
 	 String value = "";
 	 
 	 while(st.hasMoreTokens()) { 
 		  key = st.nextToken(); 
 		  value = st.nextToken(); 
 		  log.info(key + "\t" + value); 
 		 } 
  			 
 	  log.info("AddNewRow: XXXXXXXXXXXXXXXXX ---> key: " + key + " , Value: " + value);
 	  URNData nRow = new URNData();
 	  nRow.setVer_("1"); 	 
 	  nRow.setURNName(key);
 	  nRow.setURNValue(value);
 	  
 	 getQueryService().executeQuery("cockpit.urnMap.insertNewRow", nRow, URNData.class);
 	 
 	 log.info("AddNewRow: XXXXXX    END   XXXXXXXXXXX");
   }
  
  @POST
  public void getPersistData(URNData d) {  
	  
	  log.info("getPersistData:  UrnName: " + d.getURNName() + " , URNValue: " + d.getURNValue() );
	    
 	    	getQueryService().executeQuery("cockpit.urnMap.persistURNData", d, URNData.class);
     
	    	    
	    log.info("XXXXXXXXXX - END - XXXXXXXXXXXXXXX");
  	}
}
