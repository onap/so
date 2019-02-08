/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.openstack.utils;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.so.config.beans.PoConfig;
import org.onap.so.logger.MessageEnum;

import org.onap.so.logger.MsoLogger;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.model.CreateStackParam;
import com.woorea.openstack.heat.model.Explanation;
import com.woorea.openstack.keystone.model.Error;
import com.woorea.openstack.quantum.model.NeutronError;

@Component("CommonUtils")
public class MsoCommonUtils {

	private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, MsoCommonUtils.class);


	@Autowired
	private PoConfig poConfig;
    /*
     * Method to execute an Openstack command and track its execution time.
     * For the metrics log, a category of "Openstack" is used along with a
     * sub-category that identifies the specific call (using the real
     * openstack-java-sdk classname of the OpenStackRequest<T> parameter).
     */



    protected <T> T executeAndRecordOpenstackRequest (OpenStackRequest <T> request) {

    	int limit;

        long start = System.currentTimeMillis ();
    	String requestType;
        if (request.getClass ().getEnclosingClass () != null) {
            requestType = request.getClass ().getEnclosingClass ().getSimpleName () + "."
                          + request.getClass ().getSimpleName ();
        } else {
            requestType = request.getClass ().getSimpleName ();
        }

        int retryDelay = poConfig.getRetryDelay();
        int retryCount = poConfig.getRetryCount();
        String retryCodes  = poConfig.getRetryCodes();

        // Run the actual command. All exceptions will be propagated
        while (true)
        {
        	try {
                return request.execute ();
        	}
        	catch (OpenStackResponseException e) {
        		boolean retry = false;
        		if (retryCodes != null ) {
        			int code = e.getStatus();
                    logger.debug ("Config values RetryDelay:" + retryDelay + " RetryCount:" + retryCount + " RetryCodes:" + retryCodes + " ResponseCode:" + code);
        			for (String rCode : retryCodes.split (",")) {
        				try {
        					if (retryCount > 0 && code == Integer.parseInt (rCode))
        					{
        						retryCount--;
        						retry = true;
                                logger.debug ("OpenStackResponseException ResponseCode:" + code +  " request:" + requestType +  " Retry indicated. Attempts remaining:" + retryCount);
        						break;
        					}
        				} catch (NumberFormatException e1) {
                            logger.error (MessageEnum.RA_CONFIG_EXC, "No retries. Exception in parsing retry code in config:" + rCode, "", "", MsoLogger.ErrorCode.SchemaError, "Exception in parsing retry code in config");
        					throw e;
        				}
        			}
        		}
        		if (retry)
    			{
    				try {
    					Thread.sleep (retryDelay * 1000L);
    				} catch (InterruptedException e1) {
                        logger.debug ("Thread interrupted while sleeping", e1);
						Thread.currentThread().interrupt();
    				}
    			}
        		else
        			throw e; // exceeded retryCount or code is not retryable
        	}
        	catch (OpenStackConnectException e) {
        		// Connection to Openstack failed
        		if (retryCount > 0)
        		{
        			retryCount--;
                    logger.debug (" request:" + requestType + " Retry indicated. Attempts remaining:" + retryCount);
        			try {
        				Thread.sleep (retryDelay * 1000L);
        			} catch (InterruptedException e1) {
                        logger.debug ("Thread interrupted while sleeping", e1);
						Thread.currentThread().interrupt();
        			}
        		}
        		else
        			throw e;

        	}
        }
    }

    /*
     * Convert an Openstack Exception on a Keystone call to an MsoException.
     * This method supports both OpenstackResponseException and OpenStackConnectException.
     */
    protected MsoException keystoneErrorToMsoException (OpenStackBaseException e, String context) {
        MsoException me = null;

        if (e instanceof OpenStackResponseException) {
            OpenStackResponseException re = (OpenStackResponseException) e;

            try {
                // Failed Keystone calls return an Error entity body.
                Error error = re.getResponse ().getErrorEntity (Error.class);
                logger.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Openstack Keystone Error on " + context + ": " + error, "Openstack", "", MsoLogger.ErrorCode.DataError, "Openstack Keystone Error on " + context);
				me = new MsoOpenstackException (error.getCode (), error.getTitle (), error.getMessage ());
            } catch (Exception e2) {
                // Can't parse the body as an "Error". Report the HTTP error
                logger.error (MessageEnum.RA_CONNECTION_EXCEPTION, "HTTP Error on " + context + ": " + re.getStatus() + "," + re.getMessage(), "Openstack", "", MsoLogger.ErrorCode.DataError, "HTTP Error on " + context, e2);
				me = new MsoOpenstackException (re.getStatus (), re.getMessage (), "");
            }

            // Add the context of the error
            me.addContext (context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus () >= 500) {

            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.
            logger.error(MessageEnum.RA_GENERAL_EXCEPTION_ARG, "Openstack Keystone connection error on " + context + ": " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Openstack Keystone connection error on " + context);

        }

        return me;
    }

    /*
     * Convert an Openstack Exception on a Heat call to an MsoOpenstackException.
     * This method supports both OpenstackResponseException and OpenStackConnectException.
     */
    protected MsoException heatExceptionToMsoException (OpenStackBaseException e, String context) {
        MsoException me = null;

        if (e instanceof OpenStackResponseException) {
            OpenStackResponseException re = (OpenStackResponseException) e;

            try {
                // Failed Heat calls return an Explanation entity body.
                Explanation explanation = re.getResponse ().getErrorEntity (Explanation.class);
                logger.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Error on " + context + ": " + explanation.toString(), "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception - Openstack Error on " + context);
                String fullError = explanation.getExplanation() + ", error.type=" + explanation.getError().getType() + ", error.message=" + explanation.getError().getMessage();
                logger.debug(fullError);
				me = new MsoOpenstackException (explanation.getCode (),
                                                explanation.getTitle (),
                                                //explanation.getExplanation ());
                                                fullError);
            } catch (Exception e2) {
                // Couldn't parse the body as an "Explanation". Report the original HTTP error.
                logger.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "HTTP Error on " + context + ": " + re.getStatus() + "," + e.getMessage(), "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception - HTTP Error on " + context, e2);
				me = new MsoOpenstackException (re.getStatus (), re.getMessage (), "");
            }

            // Add the context of the error
            me.addContext (context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus () >= 500) {

            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.

            logger.error(MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Heat connection error on " + context + ": " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Openstack Heat connection error on " + context);
    	}

        return me;
    }

    /*
     * Convert an Openstack Exception on a Neutron call to an MsoOpenstackException.
     * This method supports both OpenstackResponseException and OpenStackConnectException.
     */
    protected MsoException neutronExceptionToMsoException (OpenStackBaseException e, String context) {
        MsoException me = null;

        if (e instanceof OpenStackResponseException) {
            OpenStackResponseException re = (OpenStackResponseException) e;

            try {
                // Failed Neutron calls return an NeutronError entity body
                NeutronError error = re.getResponse ().getErrorEntity (NeutronError.class);
                logger.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Neutron Error on " + context + ": " + error, "Openstack", "", MsoLogger.ErrorCode.DataError, "Openstack Neutron Error on " + context);
				me = new MsoOpenstackException (re.getStatus (), error.getType (), error.getMessage ());
            } catch (Exception e2) {
                // Couldn't parse body as a NeutronError. Report the HTTP error.
                logger.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "HTTP Error on " + context + ": " + re.getStatus() + "," + e.getMessage(), "Openstack", "", MsoLogger.ErrorCode.DataError, "Openstack HTTP Error on " + context, e2);
				me = new MsoOpenstackException (re.getStatus (), re.getMessage (), null);
            }

            // Add the context of the error
            me.addContext (context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus () >= 500) {

            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.

            logger.error(MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Neutron Connection error on "+ context + ": " + e, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Openstack Neutron Connection error on "+ context);
    	}

        return me;
    }

    /*
     * Convert a Java Runtime Exception to an MsoException.
     * All Runtime exceptions will be translated into an MsoAdapterException,
     * which captures internal errors.
     * Alarms will be generated on all such exceptions.
     */
    protected MsoException runtimeExceptionToMsoException (RuntimeException e, String context) {
        MsoAdapterException me = new MsoAdapterException (e.getMessage (), e);
        me.addContext (context);
        me.setCategory (MsoExceptionCategory.INTERNAL);

        // Always generate an alarm for internal exceptions
        logger.error(MessageEnum.RA_GENERAL_EXCEPTION_ARG, "An exception occured on  "+ context + ": " + e, "OpenStack", "", MsoLogger.ErrorCode.DataError, "An exception occured on  "+ context);


        return me;
    }

    protected MsoException ioExceptionToMsoException(IOException e, String context) {
    	MsoAdapterException me = new MsoAdapterException (e.getMessage (), e);
        me.addContext (context);
        me.setCategory (MsoExceptionCategory.INTERNAL);

        // Always generate an alarm for internal exceptions
        logger.error(MessageEnum.RA_GENERAL_EXCEPTION_ARG, "An exception occured on  "+ context + ": " + e, "OpenStack", "", MsoLogger.ErrorCode.DataError, "An exception occured on  "+ context);


        return me;
    }

    public boolean isNullOrEmpty (String s) {
        return s == null || s.isEmpty();
    }


    protected CreateStackParam createStackParam(String stackName,
            String heatTemplate,
            Map <String, ?> stackInputs,
            int timeoutMinutes,
            String environment,
            Map <String, Object> files,
            Map <String, Object> heatFiles) {

        // Create local variables checking to see if we have an environment, nested, get_files
        // Could later add some checks to see if it's valid.
        boolean haveEnvtVariable = true;
        if (environment == null || "".equalsIgnoreCase (environment.trim ())) {
            haveEnvtVariable = false;
            logger.debug ("createStackParam called with no environment variable");
        } else {
        	logger.debug ("createStackParam called with an environment variable: " + environment);
        }

        boolean haveFiles = true;
        if (files == null || files.isEmpty ()) {
            haveFiles = false;
            logger.debug ("createStackParam called with no files / child template ids");
        } else {
        	logger.debug ("createStackParam called with " + files.size () + " files / child template ids");
        }

        boolean haveHeatFiles = true;
        if (heatFiles == null || heatFiles.isEmpty ()) {
            haveHeatFiles = false;
            logger.debug ("createStackParam called with no heatFiles");
        } else {
        	logger.debug ("createStackParam called with " + heatFiles.size () + " heatFiles");
        }

	    //force entire stackInput object to generic Map<String, Object> for openstack compatibility
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> normalized = new HashMap<>();
		try {
			normalized = mapper.readValue(mapper.writeValueAsString(stackInputs), new TypeReference<HashMap<String,Object>>() {});
		} catch (IOException e1) {
			logger.debug("could not map json", e1);
		}

	    // Build up the stack to create
	    // Disable auto-rollback, because error reason is lost. Always rollback in the code.
	    CreateStackParam stack = new CreateStackParam ();
	    stack.setStackName (stackName);
	    stack.setTimeoutMinutes (timeoutMinutes);
	    stack.setParameters (normalized);
	    stack.setTemplate (heatTemplate);
	    stack.setDisableRollback (true);
	    // TJM New for PO Adapter - add envt variable
	    if (haveEnvtVariable) {
	        logger.debug ("Found an environment variable - value: " + environment);
	        stack.setEnvironment (environment);
	    }
	    // Now handle nested templates or get_files - have to combine if we have both
	    // as they're both treated as "files:" on the stack.
	    if (haveFiles && haveHeatFiles) {
	        // Let's do this here - not in the bean
	        logger.debug ("Found files AND heatFiles - combine and add!");
	        Map <String, Object> combinedFiles = new HashMap <> ();
	        for (Entry<String, Object> entry : files.entrySet()) {
	        	combinedFiles.put(entry.getKey(), entry.getValue());
	        }
	        for (Entry<String, Object> entry : heatFiles.entrySet()) {
	        	combinedFiles.put(entry.getKey(), entry.getValue());
	        }
	        stack.setFiles (combinedFiles);
	    } else {
	        // Handle if we only have one or neither:
	        if (haveFiles) {
	        	logger.debug ("Found files - adding to stack");
	            stack.setFiles (files);
	        }
	        if (haveHeatFiles) {
	        	logger.debug ("Found heatFiles - adding to stack");
	            // the setFiles was modified to handle adding the entries
	            stack.setFiles (heatFiles);
	        }
	    }

	    // 1802 - attempt to add better formatted printout of request to openstack
	    try {
	    	Map<String, Object> inputs = new HashMap<>();
	    	for (Entry<String, ?> entry : stackInputs.entrySet()) {
	    		if (entry.getValue() != null) {
	    			inputs.put(entry.getKey(), entry.getValue());
	    		}
	    	}
	    	logger.debug("stack request:" + stack.toString());
	    } catch (Exception e) {
	    	// that's okay - this is a nice-to-have
	    	logger.debug("(had an issue printing nicely formatted request to debuglog) " + e.getMessage());
	    }

	    return stack;
    }

}
