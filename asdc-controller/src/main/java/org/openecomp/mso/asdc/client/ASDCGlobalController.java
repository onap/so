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

package org.openecomp.mso.asdc.client;



import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openecomp.mso.asdc.client.exceptions.ASDCControllerException;
import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJsonProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;



public class ASDCGlobalController {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);
    private Map <String,ASDCController> controllers = new HashMap<>();
        
    private MsoJsonProperties msoProp= null;
    
    private void loadControllers () throws ASDCParametersException {
    	
		List<String> controllerNames = ASDCConfiguration.getAllDefinedControllers();

		StringBuilder controllerListLog = new StringBuilder("List of controllers loaded:");
		for (String controllerName : controllerNames) {
			controllers.put(controllerName, new ASDCController(controllerName));
			controllerListLog.append(controllerName);
			controllerListLog.append(";");
		}
		LOGGER.debug(controllerListLog.toString());
    }
    
    private boolean sameControllersDefined() throws ASDCParametersException {
    	List<String> controllerNames = ASDCConfiguration.getAllDefinedControllers();
    	if (controllerNames.size() == controllers.size()) {
    		boolean areIdentical = true;
    		
    		for (String name:controllerNames) {
    			if (!controllers.containsKey(name)) {
    				areIdentical = false;
    				break;
    			}
    		}
    		return areIdentical;
    		
    	} else {
    		return false;
    	}
    }
    
    /**
     * Check that controllers list needs to be updated or not.
     * @param return true if the list has been updated
     */
    private boolean updateControllersListIfNeeded ()  {
    	boolean updateNeeded=false;
    	try {
    	
			MsoPropertiesFactory msoPropFactory = new MsoPropertiesFactory();
			MsoJsonProperties newMsoProp;
	
			newMsoProp = msoPropFactory.getMsoJsonProperties(ASDCConfiguration.MSO_PROP_ASDC);
	
			if (msoPropFactory.propertiesHaveChanged(ASDCConfiguration.MSO_PROP_ASDC, msoProp) && !sameControllersDefined()) {
				updateNeeded = true;
				LOGGER.debug("List of ASDC controllers has been changed, trying to kill them");
				this.closeASDC();
				
				// Wait that all controllers are down before restarting, next pass will kill them all
				if (this.controllers.size() == 0) {
					msoProp = newMsoProp;
					this.loadControllers();
				}
			} 			
		
    	} catch (ASDCParametersException e) {
			 LOGGER.warn (MessageEnum.ASDC_LOAD_ASDC_CLIENT_EXC,
                     "All ASDC Hosts",
                     "All ASDC Envs", "ASDC", "", MsoLogger.ErrorCode.BusinessProcesssError, "ASDCParametersException in updateControllersListIfNeeded",
                     e);
		} catch (MsoPropertiesException e) {
			 LOGGER.warn (MessageEnum.ASDC_LOAD_ASDC_CLIENT_EXC,
                    "All ASDC Hosts",
                    "All ASDC Envs", "ASDC", "", MsoLogger.ErrorCode.BusinessProcesssError, "MsoPropertiesException in updateControllersListIfNeeded",
                    e);
		}
    	return updateNeeded;
		

    }
    
    /**
     * Checks for each controller if it is STOPPED and restart if it is the case.
     */
    public void checkInStoppedState () {

        for (ASDCController controller : controllers.values()) {
            if (ASDCControllerStatus.STOPPED.equals (controller.getControllerStatus ())) {

                // Try to restart just in case of issues
                try {
                    controller.initASDC ();
                } catch (ASDCControllerException ec) {
                    LOGGER.warn (MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC,
                                 controller.getAddress (),
                                 controller.getEnvironment (), "ASDC", "", MsoLogger.ErrorCode.BusinessProcesssError, "ASDCControllerException in checkInStoppedState",
                                 ec);
                } catch (ASDCParametersException ep) {
                    LOGGER.warn (MessageEnum.ASDC_LOAD_ASDC_CLIENT_EXC,
                                 controller.getAddress (),
                                 controller.getEnvironment (), "ASDC", "", MsoLogger.ErrorCode.BusinessProcesssError, "ASDCParametersException in checkInStoppedState",
                                 ep);
                } catch (RuntimeException | IOException e) {
                    LOGGER.error (MessageEnum.ASDC_SINGLETON_CHECKT_EXC,
                                  controller.getAddress (),
                                  controller.getEnvironment (), "ASDC", "", MsoLogger.ErrorCode.BusinessProcesssError, "RuntimeException in checkInStoppedState",
                                  e);
                }
            }
        }
    }

    public void closeASDC () {
    	List<String> controllerToRemove = new LinkedList<>();
    	
        for (ASDCController controller : controllers.values()) {
            try {
                controller.closeASDC ();
                controllerToRemove.add(controller.controllerName);
                
            } catch (RuntimeException e) {
                LOGGER.warn (MessageEnum.ASDC_SHUTDOWN_ASDC_CLIENT_EXC,
                             "RuntimeException",
                             controller.getAddress (),
                             controller.getEnvironment (), "ASDC", "closeASDC", MsoLogger.ErrorCode.BusinessProcesssError, "RuntimeException in closeASDC",
                             e);
            } catch (ASDCControllerException e) {
                LOGGER.warn (MessageEnum.ASDC_SHUTDOWN_ASDC_CLIENT_EXC,
                             "ASDCControllerException",
                             controller.getAddress (),
                             controller.getEnvironment (), "ASDC", "closeASDC", MsoLogger.ErrorCode.BusinessProcesssError, "ASDCControllerException in closeASDC",
                             e);
            }
        }
        
        // Now remove the ones properly closed
        for (String toRemove:controllerToRemove) {
        	controllers.remove(toRemove);
        }
        
    }

    /**
     * Check whether the config has been changed
     */
    public boolean updateControllersConfigIfNeeded () {
    	boolean listUpdated=updateControllersListIfNeeded();
    	if (!listUpdated) {
    	
	        for (ASDCController controller : controllers.values()) {
	            try {
	                controller.updateConfigIfNeeded ();
	            } catch (ASDCControllerException ec) {
	                LOGGER.warn (MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC,
	                             controller.getAddress (),
	                             controller.getEnvironment (), "ASDC", "closeASDC", MsoLogger.ErrorCode.BusinessProcesssError, "ASDCControllerException in updateControllersConfigIfNeeded",
	                             ec);
	            } catch (ASDCParametersException ep) {
	                LOGGER.warn (MessageEnum.ASDC_LOAD_ASDC_CLIENT_EXC,
	                             controller.getAddress (),
	                             controller.getEnvironment (), "ASDC", "closeASDC", MsoLogger.ErrorCode.BusinessProcesssError, "ASDCParametersException in updateControllersConfigIfNeeded",
	                             ep);
	            } catch (RuntimeException | IOException e) {
	                LOGGER.error (MessageEnum.ASDC_SINGLETON_CHECKT_EXC,
	                              controller.getAddress (),
	                              controller.getEnvironment (), "ASDC", "closeASDC", MsoLogger.ErrorCode.BusinessProcesssError, "RuntimeException in updateControllersConfigIfNeeded",
	                              e);
	            }
	        }
    	}
    	return listUpdated;
    }

	public Map<String, ASDCController> getControllers() {
		return controllers;
	}
    
    
}
