/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.db.catalog.test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;

/**
 * This class is purely for development testing.  It hard-codes a very limited
 * catalog of HeatTemplates and Services for the case where no actual database
 * is available.
 * 
 *
 */
public class CatalogTestDatabase extends CatalogDatabase {
	
	private static int id = 1;
	private static Map<String,HeatTemplate> heatTemplates;
	
	static {
		heatTemplates = new HashMap<String,HeatTemplate>();
		
		addTemplate("ApacheDemo", "C:/temp/apache-demo.json", 2,
					new ArrayList<String>(Arrays.asList("private_subnet_gateway", "private_subnet_cidr")),
					new ArrayList<String> (Arrays.asList("vnf_id", "public_net_id")));
	}

	public CatalogTestDatabase () {
	}
	
	private static void addTemplate (String name, String path, int timeout, List<String> reqd, List<String> opt)
	{
		HeatTemplate template = new HeatTemplate();
		template.setId(id++);
		template.setTemplateName("ApacheDemo");
		template.setTemplatePath("C:/temp/apache-demo.json");
		template.setTimeoutMinutes(2);
		
		Set<HeatTemplateParam> params = new HashSet<HeatTemplateParam>();
		
		for (String s : reqd) {
			HeatTemplateParam param = new HeatTemplateParam();
			param.setId(id++);
			param.setParamName(s);
			param.setRequired(true);
			params.add(param);
		}
		
		for (String s : opt) {
			HeatTemplateParam param = new HeatTemplateParam();
			param.setId(id++);
			param.setParamName(s);
			param.setRequired(false);
			params.add(param);
		}
		template.setParameters(params);
		
		heatTemplates.put(name,  template);
	}
	
    @Override
    public HeatTemplate getHeatTemplate (String templateName)
    {
    	if (heatTemplates.containsKey(templateName)) {
    		return heatTemplates.get(templateName);
    	} else {
    		return null;
    	}
    }
    
    @Override
    public HeatTemplate getHeatTemplate (String templateName, String version)
    {
    	return getHeatTemplate(templateName);
    }

}
