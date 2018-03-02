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

package org.openecomp.mso.asdc.installer;


import java.math.BigDecimal;

public class BigDecimalVersion {
	  
	/**
     * This method truncates and convert the version String provided in the notification.
     * 
     * @param version The version to check
     * @return A BigDecimal value checked and truncated
     */
    public static BigDecimal castAndCheckNotificationVersion(String version) {
    	// Truncate the version if bad type
    	String[] splitVersion = version.split("\\.");
    	StringBuilder newVersion = new StringBuilder();
    	if (splitVersion.length > 1) {
	    	newVersion.append(splitVersion[0]);
	    	newVersion.append(".");
	    	newVersion.append(splitVersion[1]);
    	} else {
    		return new BigDecimal(splitVersion[0]);
    	}
    	
    	for (int i=2;i<splitVersion.length;i++) {
    		newVersion.append(splitVersion[i]);
    	}
    	
    	return new BigDecimal(newVersion.toString());
    }
    
    /**
     * This method truncates and convert the version String provided in the notification.
     * 
     * @param version The version to check
     * @return A String value checked and truncated to Decimal format
     */
    public static String castAndCheckNotificationVersionToString (String version) {
    	return castAndCheckNotificationVersion(version).toString();
    }
}
