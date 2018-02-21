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

package org.openecomp.mso.openstack.utils;


import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import org.openecomp.mso.properties.MsoJavaProperties;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.model.Explanation;
import com.woorea.openstack.keystone.model.Error;
import com.woorea.openstack.quantum.model.NeutronError;

public class MsoCommonUtils {

    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
    protected static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
    protected static String retryDelayProp = "ecomp.mso.adapters.po.retryDelay";
    protected static String retryCountProp = "ecomp.mso.adapters.po.retryCount";
    protected static String retryCodesProp = "ecomp.mso.adapters.po.retryCodes";
    protected static int retryDelayDefault = 5;
    protected static int retryCountDefault = 3;
    protected static String retryCodesDefault = "504";
  
    /*
     * Method to execute an Openstack command and track its execution time.
     * For the metrics log, a category of "Openstack" is used along with a
     * sub-category that identifies the specific call (using the real
     * openstack-java-sdk classname of the OpenStackRequest<T> parameter).
     */
    
    protected static <T> T executeAndRecordOpenstackRequest (OpenStackRequest <T> request)
    {
    	return executeAndRecordOpenstackRequest (request, null);
    }
    protected static <T> T executeAndRecordOpenstackRequest (OpenStackRequest <T> request, MsoJavaProperties msoProps) {
    	
    	int limit;
        // Get the name and method name of the parent class, which triggered this method
        StackTraceElement[] classArr = new Exception ().getStackTrace ();
        if (classArr.length >=2) {
        	limit = 3;
        } else {
        	limit = classArr.length;
        }
    	String parentServiceMethodName = classArr[0].getClassName () + "." + classArr[0].getMethodName ();
    	for (int i = 1; i < limit; i++) {
            String className = classArr[i].getClassName ();
            if (!className.equals (MsoCommonUtils.class.getName ())) {
            	parentServiceMethodName = className + "." + classArr[i].getMethodName ();
            	break;
            }
        }

    	String requestType;
        if (request.getClass ().getEnclosingClass () != null) {
            requestType = request.getClass ().getEnclosingClass ().getSimpleName () + "."
                          + request.getClass ().getSimpleName ();
        } else {
            requestType = request.getClass ().getSimpleName ();
        }
        
        int retryDelay = retryDelayDefault;
        int retryCount = retryCountDefault;
        String retryCodes  = retryCodesDefault;
        if (msoProps != null) //extra check to avoid NPE
        {
        	retryDelay = msoProps.getIntProperty (retryDelayProp, retryDelayDefault);
        	retryCount = msoProps.getIntProperty (retryCountProp, retryCountDefault);
        	retryCodes = msoProps.getProperty (retryCodesProp, retryCodesDefault);
        }
    	
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
                                logger.debug ("OpenStackResponseException ResponseCode:" + code +  " at:" + parentServiceMethodName + " request:" + requestType +  " Retry indicated. Attempts remaining:" + retryCount);
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
                    logger.debug ("OpenstackConnectException at:" + parentServiceMethodName + " request:" + requestType + " Retry indicated. Attempts remaining:" + retryCount);
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
    protected static MsoException keystoneErrorToMsoException (OpenStackBaseException e, String context) {
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
                alarmLogger.sendAlarm ("KeystoneError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.
            logger.error(MessageEnum.RA_GENERAL_EXCEPTION_ARG, "Openstack Keystone connection error on " + context + ": " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Openstack Keystone connection error on " + context);
			alarmLogger.sendAlarm ("KeystoneIOError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
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
                alarmLogger.sendAlarm ("HeatError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.
            alarmLogger.sendAlarm ("HeatIOError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
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
                alarmLogger.sendAlarm ("NeutronError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException (ce.getMessage ());
            me.addContext (context);

            // Generate an alarm for all connection errors.
            alarmLogger.sendAlarm ("NeutronIOError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());
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
		alarmLogger.sendAlarm ("AdapterInternalError", MsoAlarmLogger.CRITICAL, me.getContextMessage ());

        return me;
    }

    public static boolean isNullOrEmpty (String s) {
        return s == null || s.isEmpty();
    }



}
