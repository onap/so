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
/***
import java.beans.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
*/
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;


//import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.openecomp.camunda.bpmn.plugin.urnmap.db.URNData;
import org.openecomp.mso.logger.MsoLogger;


//public class ProcessInstanceResource extends AbstractPluginResource {
public class URNResource extends AbstractCockpitPluginResource{
  public URNResource(String engineName) {
    super(engineName);
  }
  
  private Connection conn;
  private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
  @GET
  public List<URNData> getUrnDataMap() 
  {  
	  List<URNData> list = new ArrayList();
	  
	  try {
			
		    conn = getDBConnection();
			    PreparedStatement psData = conn
						.prepareStatement("select * from MSO_URN_MAPPING order by NAME_");
			    
			    ResultSet r = psData.executeQuery();
			    
				while(r.next()) 
				{
					URNData d = new URNData();
					d.setURNName(r.getString("NAME_"));
					d.setURNValue(r.getString("VALUE_"));
					d.setVer_( r.getString("REV_"));
					
					list.add(d);					
				}
				
			    psData.close();
				conn.close();
			
		} catch (Exception e) 
		{
			
			e.printStackTrace();
		}
     
    for(URNData d: list)
    {
    	msoLogger.debug(  d.getURNName() + "		"  + d.getURNValue());
    	//msoLogger.debug("Started Executing " + getTaskName());
    	msoLogger.debug("Started Executing " + d.getURNName() + " " + d.getURNValue());
    }
   
    return list;
  }
  
  public List<URNData> getUrnDataMapOLD() 
  {  
	  
    List<URNData> list = getQueryService()
            .executeQuery("cockpit.urnMap.retrieveUrnKeyValuePair", new QueryParameters<URNData>());
    
    msoLogger.debug("urnmap-plugin project - Results Retrieved: ");
    msoLogger.debug("URNName: " + "		" + "URNValue: " );
    
    for(URNData d: list)
    {
    	//msoLogger.debug(  d.getURNName() + "		"  + d.getURNValue());
    	msoLogger.debug( d.getURNName() + "		"  + d.getURNValue());
    }
   
    return list;
  }
  
  public Connection getDBConnection()
  {
	  try {
			
			if(conn == null)
			{
				Context ctx = new InitialContext();
			    DataSource ds = (DataSource)ctx.lookup("java:jboss/datasources/ProcessEngine");//jboss
		    	conn =			ds.getConnection();
	 
			}			 
			
		} catch (Exception e) 
		{
			
			e.printStackTrace();
		}
	  
	  return conn;
  }
  
  @PUT
  public void insertNewRow(String temp) 
   {  
 	 msoLogger.debug("AddNewRow: XXXXXXXXXXXXXXXXX ---> " + temp);
 	 msoLogger.debug("AddNewRow: EngineName  ---> " + engineName);
 	 
 	 StringTokenizer st = new StringTokenizer(temp, "|");
 	 String key_ = "";
 	 String value_ = "";
 	 
 	 while(st.hasMoreTokens()) { 
 		  key_ = st.nextToken(); 
 		  value_ = st.nextToken(); 
 		 msoLogger.debug(key_ + "\t" + value_); 
 		 } 
  			 
       msoLogger.debug("AddNewRow: XXXXXXXXXXXXXXXXX ---> key: " + key_ + " , Value: " + value_);
 	  final URNData nRow = new URNData();
 	  nRow.setVer_("1"); 	
 	  final String myKey = key_;
 	  final String myValue = value_;
 	  
		msoLogger.debug("----------- START ----------------------");
		try {
			
  		    conn = getDBConnection();
			    PreparedStatement psData = conn
						.prepareStatement("Insert into MSO_URN_MAPPING values ('" + key_ + "', '" + value_  + "', '1')");
			    
			    psData.executeUpdate();
			    
			    psData.close();
				conn.close();
			//}			 
			
		} catch (Exception e) 
		{
			
			e.printStackTrace();
		}
 	// getQueryService().executeQuery("cockpit.urnMap.insertNewRow", nRow, URNData.class);
   }
  
  @POST
  public void getPersistData(URNData d) {  
	  
	    	//getQueryService().executeQuery("cockpit.urnMap.persistURNData", d, URNData.class);
 	    
	  try {
			
		    conn = getDBConnection();
			PreparedStatement psData = conn
						.prepareStatement("UPDATE MSO_URN_MAPPING set VALUE_ ='"+ d.getURNValue() + "' WHERE NAME_='" + d.getURNName() + "'");
			    
			    psData.executeUpdate();
			    
			    psData.close();
				conn.close();
		} catch (Exception e) 
		{
			
			e.printStackTrace();
		}
	 
  	}
}
