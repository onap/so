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

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.openecomp.camunda.bpmn.plugin.urnmap.db.*;

public class ProcessInstanceResource extends AbstractPluginResource {

  public ProcessInstanceResource(String engineName) {
    super(engineName);
  }

  @GET
  public List<URNData> getUrnDataMap() {    
    List<URNData> list = getQueryService()
            .executeQuery(
                    "cockpit.urnMap.retrieveUrnKeyValuePair",
                    new QueryParameters<URNData>());
    
    System.out.println("urnmap-plugin project - Results Retrieved: ");
    System.out.println("URNName: " + "		" + "URNValue: " );
    for(URNData d: list)
    {
    	System.out.println(  d.getURNName() + "		"  + d.getURNValue());
    }
   
    return list;
  }
  
  @PUT
  //public void insertNewRow(String key_, String value_) 
  public void insertNewRow(String temp) 
   {  
 	 System.out.println("AddNewRow: XXXXXXXXXXXXXXXXX ---> " + temp);
 	 StringTokenizer st = new StringTokenizer(temp, "|");
 	 String key_ = "";
 	 String value_ = "";
 	 
 	 while(st.hasMoreTokens()) { 
 		  key_ = st.nextToken(); 
 		  value_ = st.nextToken(); 
 		 System.out.println(key_ + "\t" + value_); 
 		 } 
  			 
       System.out.println("AddNewRow: XXXXXXXXXXXXXXXXX ---> key: " + key_ + " , Value: " + value_);
 	  URNData nRow = new URNData();
 	  nRow.setVer_("1"); 	 
 	  nRow.setURNName(key_);
 	  nRow.setURNValue(value_);
 	  
 	 getQueryService().executeQuery("cockpit.urnMap.insertNewRow", nRow, URNData.class);
 	 
 	 System.out.println("AddNewRow: XXXXXX    END   XXXXXXXXXXX");
   }
  
  @POST
 // public void getPersistData(List<URNData > myList) {  
  public void getPersistData(URNData d) {  
	  
	  System.out.println("getPersistData:  UrnName: " + d.getURNName() + " , URNValue: " + d.getURNValue() );
	    
 	    	getQueryService().executeQuery("cockpit.urnMap.persistURNData", d, URNData.class);
 	    	//getQueryService().executeQuery("cockpit.sample.persistURNData", d, ProcessInstanceCountDto.class);
	     
	    	    
	    System.out.println("XXXXXXXXXX - END - XXXXXXXXXXXXXXX");
  	}
}
