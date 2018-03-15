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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.properties.MsoJsonProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.sdc.api.consumer.IConfiguration;

import com.fasterxml.jackson.databind.JsonNode;

public class ASDCConfiguration implements IConfiguration {

    private MsoPropertiesFactory msoPropertiesFactory;

    // SHell command to obtain the same encryption, 128 bits key, key must be HEX
    // echo -n "This is a test string" | openssl aes-128-ecb -e -K 546573746F736973546573746F736973 -nosalt | xxd

    private String configKey;
    public static String PASSWORD_ATTRIBUTE_NAME;
    public static String KEY_STORE_PASSWORD;
    private MsoJsonProperties msoProperties;

    private String asdcControllerName;

    public static final String MSO_PROP_ASDC = "MSO_PROP_ASDC";
    public static final String PARAMETER_PATTERN = "asdc-connections";
    public static final String MSG_BUS_ADDRESS_ATTRIBUTE_NAME = "messageBusAddress";
    public static final String COMPONENT_NAMES_ADDRESS_ATTRIBUTE_NAME = "componentNames";
    public static final String WATCHDOG_TIMEOUT_NAME = "watchDogTimeout";

    public static final String CONSUMER_GROUP_ATTRIBUTE_NAME = "consumerGroup";
    public static final String CONSUMER_ID_ATTRIBUTE_NAME = "consumerId";
    public static final String ENVIRONMENT_NAME_ATTRIBUTE_NAME = "environmentName";
    public static final String POLLING_INTERVAL_ATTRIBUTE_NAME = "pollingInterval";
    public static final String RELEVANT_ARTIFACT_TYPES_ATTRIBUTE_NAME = "relevantArtifactTypes";
    public static final String USER_ATTRIBUTE_NAME = "user";
    public static final String ASDC_ADDRESS_ATTRIBUTE_NAME = "asdcAddress";
    public static final String POLLING_TIMEOUT_ATTRIBUTE_NAME = "pollingTimeout";
    public static final String ACTIVATE_SERVER_TLS_AUTH = "activateServerTLSAuth";
    public static final String KEY_STORE_PATH = "keyStorePath";

    public static final String HEAT="HEAT";
    public static final String HEAT_ARTIFACT="HEAT_ARTIFACT";
    public static final String HEAT_ENV="HEAT_ENV";
    public static final String HEAT_NESTED="HEAT_NESTED";
    public static final String HEAT_NET="HEAT_NET";
    public static final String HEAT_VOL="HEAT_VOL";
    public static final String OTHER="OTHER";
    public static final String TOSCA_CSAR="TOSCA_CSAR";
    public static final String VF_MODULES_METADATA="VF_MODULES_METADATA";


    private static final String[] SUPPORTED_ARTIFACT_TYPES = {HEAT,
    		HEAT_ARTIFACT,
    		HEAT_ENV,
    		HEAT_NESTED,
    		HEAT_NET,
    		HEAT_VOL,
    		OTHER,
    		TOSCA_CSAR,
    		VF_MODULES_METADATA};

    public static final List<String>  SUPPORTED_ARTIFACT_TYPES_LIST =  Collections.unmodifiableList(Arrays.asList(SUPPORTED_ARTIFACT_TYPES));

    /**
     * Default constructor, the mso.properties is searched in the classpath (for testing)
     * Or in /etc/ecomp/mso/config/mso.properties
     *
     * @param controllerName The controllerName of the config JSON tree
     * @throws ASDCParametersException in case of issues with the parameters
     * @throws IOException If the key file has not been loaded properly
     */
    public ASDCConfiguration (String controllerName) throws ASDCParametersException, IOException {

        Properties keyProp = new Properties ();
        this.asdcControllerName = controllerName;

        keyProp.load (Thread.currentThread ().getContextClassLoader ().getResourceAsStream ("config-key.properties"));
        configKey = (String) keyProp.get ("asdc.config.key");

        // This structure contains static values initialized by servlet initializer
        this.msoPropertiesFactory = new MsoPropertiesFactory ();

        refreshASDCConfig ();

    }
    
    @Override
    public java.lang.Boolean isUseHttpsWithDmaap() {
    	return false;
    }
    
    @Override
    public boolean isConsumeProduceStatusTopic(){
    	return true;
    }
    
    @Override
    public List<String> getMsgBusAddress(){

       JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (MSG_BUS_ADDRESS_ATTRIBUTE_NAME) != null) {
            List<String> msgAddressList = new ArrayList<>();
            
            Iterator<JsonNode> config = masterConfigNode.get(MSG_BUS_ADDRESS_ATTRIBUTE_NAME).elements();
      
            while( config.hasNext() ) {
                String key = (String)config.next().asText();
                msgAddressList.add(key);
            }

            if ("NULL".equals (msgAddressList) || msgAddressList.isEmpty ()) {
                return null;
            } else {
                return msgAddressList;
            }
        } else {
            return null;
        } 
    	
    	
    }
    
    public String getAsdcControllerName () {
        return asdcControllerName;
    }
    
    private JsonNode getASDCControllerConfigJsonNode () {
        if (this.msoProperties.getJsonRootNode ().get (PARAMETER_PATTERN) != null) {
            return this.msoProperties.getJsonRootNode ().get (PARAMETER_PATTERN).get (this.asdcControllerName);
        } else {
            return null;
        }

    }

    /**
     * This method reload the config if needed.
     *
     * @return true if config has been reloaded, false otherwise
     * @throws ASDCParametersException In case the parameters validation fails
     * @throws MsoPropertiesException
     */
    public boolean refreshASDCConfig () throws ASDCParametersException {

        try {
            if (msoPropertiesFactory.propertiesHaveChanged (MSO_PROP_ASDC, msoProperties)) {
                msoProperties = msoPropertiesFactory.getMsoJsonProperties (MSO_PROP_ASDC);

                this.testAllParameters ();
                return true;
            } else {
                return false;
            }
        } catch (MsoPropertiesException e) {
            throw new ASDCParametersException ("mso.asdc.json not initialized properly, ASDC config cannot be reloaded",
                                               e);
        }
    }

    /**
     * This method is useful to check whether a mso properties config has been changed.
     *
     * @return true is a new config is availabe, false otherwise
     * @throws ASDCParametersException
     * @throws MsoPropertiesException
     */
    public boolean hasASDCConfigChanged () throws ASDCParametersException {
        try {
            return msoPropertiesFactory.propertiesHaveChanged (MSO_PROP_ASDC, msoProperties);
        } catch (MsoPropertiesException e) {
            throw new ASDCParametersException ("mso.asdc.json not initialized properly, ASDC config cannot be read", e);
        }
    }

    @Override
    public String getConsumerGroup () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (CONSUMER_GROUP_ATTRIBUTE_NAME) != null) {
            String config = masterConfigNode.get (CONSUMER_GROUP_ATTRIBUTE_NAME).asText ();

            if ("NULL".equals (config) || config.isEmpty ()) {
                return null;
            } else {
                return config;
            }
        } else {
            return null;
        }
    }
    
    public int getWatchDogTimeout () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (WATCHDOG_TIMEOUT_NAME) != null) {
        	
            return masterConfigNode.get (WATCHDOG_TIMEOUT_NAME).asInt ();
        } else {
            return 0;
        }
    }

    @Override
    public String getConsumerID () {

        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (CONSUMER_ID_ATTRIBUTE_NAME) != null) {
            String config = masterConfigNode.get (CONSUMER_ID_ATTRIBUTE_NAME).asText ();

            if (config.isEmpty ()) {
                return null;
            } else {
                return config;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getEnvironmentName () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (ENVIRONMENT_NAME_ATTRIBUTE_NAME) != null) {
            String config = masterConfigNode.get (ENVIRONMENT_NAME_ATTRIBUTE_NAME).asText ();

            if (config.isEmpty ()) {
                return null;
            } else {
                return config;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getPassword () {
	Properties keyProp = new Properties ();
    	try {
			keyProp.load (Thread.currentThread ().getContextClassLoader ().getResourceAsStream ("config-key.properties"));
		} catch (IOException e) {
			
		}
    	PASSWORD_ATTRIBUTE_NAME=(String) keyProp.get ("password.attribute.name");
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (PASSWORD_ATTRIBUTE_NAME) != null) {
            String config = this.msoProperties.getEncryptedProperty (masterConfigNode.get (PASSWORD_ATTRIBUTE_NAME),
                                                                     null,
                                                                     this.configKey);

            if (config.isEmpty ()) {
                return null;
            } else {
                return config;
            }
        } else {
            return null;
        }
    }

    @Override
    public int getPollingInterval () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (POLLING_INTERVAL_ATTRIBUTE_NAME) != null) {
            return masterConfigNode.get (POLLING_INTERVAL_ATTRIBUTE_NAME).asInt ();
        } else {
            return 0;
        }
    }

    @Override
    public List <String> getRelevantArtifactTypes () {
    	// DO not return the Static List SUPPORTED_ARTIFACT_TYPES_LIST because the ASDC Client will try to modify it !!!
    	return Arrays.asList(SUPPORTED_ARTIFACT_TYPES);
    }

    @Override
    public String getUser () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (USER_ATTRIBUTE_NAME) != null) {
            String config = masterConfigNode.get (USER_ATTRIBUTE_NAME).asText ();

            if (config.isEmpty ()) {
                return null;
            } else {
                return config;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsdcAddress () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (ASDC_ADDRESS_ATTRIBUTE_NAME) != null) {
            String config = masterConfigNode.get (ASDC_ADDRESS_ATTRIBUTE_NAME).asText ();

            if (config.isEmpty ()) {
                return null;
            } else {
                return config;
            }
        } else {
            return null;
        }
    }

    @Override
    public int getPollingTimeout () {
        JsonNode masterConfigNode = getASDCControllerConfigJsonNode ();
        if (masterConfigNode != null && masterConfigNode.get (POLLING_TIMEOUT_ATTRIBUTE_NAME) != null) {
            return masterConfigNode.get (POLLING_TIMEOUT_ATTRIBUTE_NAME).asInt ();
        } else {
            return 0;
        }
    }

	@Override
	public boolean activateServerTLSAuth() {
		JsonNode masterConfigNode = getASDCControllerConfigJsonNode();
		if (masterConfigNode != null && masterConfigNode.get(ACTIVATE_SERVER_TLS_AUTH) != null) {
			return masterConfigNode.get(ACTIVATE_SERVER_TLS_AUTH).asBoolean(false);
		} else {
			return false;
		}
	}

	@Override
	public String getKeyStorePassword() {
	Properties keyProp = new Properties ();
		try {
			keyProp.load (Thread.currentThread ().getContextClassLoader ().getResourceAsStream ("config-key.properties"));
		} catch (IOException e) {
			
		}
		KEY_STORE_PASSWORD=(String) keyProp.get ("key.store.password");
		JsonNode masterConfigNode = getASDCControllerConfigJsonNode();
		if (masterConfigNode != null && masterConfigNode.get(KEY_STORE_PASSWORD) != null) {
			String config = this.msoProperties.getEncryptedProperty(masterConfigNode.get(KEY_STORE_PASSWORD), null,
					this.configKey);

			if (config.isEmpty()) {
				return null;
			} else {
				return config;
			}
		} else {
			return null;
		}
	}

	@Override
	public String getKeyStorePath() {
		JsonNode masterConfigNode = getASDCControllerConfigJsonNode();
		if (masterConfigNode != null && masterConfigNode.get(KEY_STORE_PATH) != null) {
			String config = masterConfigNode.get(KEY_STORE_PATH).asText();

			if (config.isEmpty()) {
				return null;
			} else {
				return config;
			}
		} else {
			return null;
		}
	}

    public void testAllParameters () throws ASDCParametersException {

        // Special case for this attribute that can be null from getConsumerGroup
        if (this.getConsumerGroup () == null
            && (getASDCControllerConfigJsonNode () == null
                || !"NULL".equals (getASDCControllerConfigJsonNode ().get (CONSUMER_GROUP_ATTRIBUTE_NAME).asText ()))) {
            throw new ASDCParametersException (CONSUMER_GROUP_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getConsumerID () == null || this.getConsumerID ().isEmpty ()) {
            throw new ASDCParametersException (CONSUMER_ID_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getEnvironmentName () == null || this.getEnvironmentName ().isEmpty ()) {
            throw new ASDCParametersException (ENVIRONMENT_NAME_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getAsdcAddress () == null || this.getAsdcAddress ().isEmpty ()) {
            throw new ASDCParametersException (ASDC_ADDRESS_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getPassword () == null || this.getPassword ().isEmpty ()) {
            throw new ASDCParametersException (PASSWORD_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getPollingInterval () == 0) {
            throw new ASDCParametersException (POLLING_INTERVAL_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getPollingTimeout () == 0) {
            throw new ASDCParametersException (POLLING_TIMEOUT_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }
        
        if (this.getWatchDogTimeout() == 0) {
            throw new ASDCParametersException (WATCHDOG_TIMEOUT_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getRelevantArtifactTypes () == null || this.getRelevantArtifactTypes ().isEmpty ()) {
            throw new ASDCParametersException (RELEVANT_ARTIFACT_TYPES_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }

        if (this.getUser () == null || this.getUser ().isEmpty ()) {
            throw new ASDCParametersException (USER_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }
                
        if (this.getMsgBusAddress() == null || this.getMsgBusAddress().isEmpty ()) {
            throw new ASDCParametersException (MSG_BUS_ADDRESS_ATTRIBUTE_NAME
                                               + " parameter cannot be found in config mso.properties");
        }
        
    }

    /**
     * This method triggers the MsoPropertiesFactory to get the ASDC config from the cache and extracts all controllers
     * defined.
     *
     * @return A list of controller Names defined in the cache config
     * @throws ASDCParametersException In cas of issues with the cache
     */
    public static List <String> getAllDefinedControllers () throws ASDCParametersException {

        MsoJsonProperties msoProp;
        try {
            List <String> result = new ArrayList<>();
            msoProp = new MsoPropertiesFactory ().getMsoJsonProperties (MSO_PROP_ASDC);

            if (msoProp.getJsonRootNode ().get (PARAMETER_PATTERN) != null) {
                Iterator <Entry <String, JsonNode>> it = msoProp.getJsonRootNode ()
                                                                .get (PARAMETER_PATTERN)
                                                                .fields();

                Entry <String, JsonNode> entry;
                while (it.hasNext ()) {
                    entry = it.next ();
                    result.add (entry.getKey ());

                }
            }
            return result;
        } catch (MsoPropertiesException e) {
            throw new ASDCParametersException ("Unable to get the JSON Properties in cache:" + MSO_PROP_ASDC, e);
        }

    }

    /**
     * The flag allows the client to receive metadata for all resources of the service regardless of the artifacts associated to them.
     * Setting the flag to false will preserve legacy behavior.
     */
    public boolean isFilterInEmptyResources() {
 	   return true;
    }

}
