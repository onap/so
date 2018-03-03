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

package org.openecomp.mso.adapter_utils.tests;

import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MsoLogger.ErrorCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
/**
 * This class implements all test methods of the MsoLogger features.
 *
 *
 */
public class MsoLoggerTest {

	static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

     /**
     * This method is called before any test occurs.
     * It creates a fake tree from scratch
     */
    @BeforeClass
    public static final void prepare () {

    }

    @Before
    public final void cleanErrorLogFile() throws FileNotFoundException {
    	URL url = this.getClass().getClassLoader().getResource("logback-test.xml");
    	String logFile = url.getFile().substring(0, url.getFile().indexOf("test-classes")) + "/MSO/Test/errorjboss.server.name_IS_UNDEFINED.log";
    	PrintWriter asdcConfigFileWriter = new PrintWriter(logFile);
		asdcConfigFileWriter.print("");
		asdcConfigFileWriter.flush();
		asdcConfigFileWriter.close();
    }	
    
    @Before
    public final void cleanMetricLogFile() throws FileNotFoundException {
    	URL url = this.getClass().getClassLoader().getResource("logback-test.xml");
		String logFile = url.getFile().substring(0, url.getFile().indexOf("test-classes")) + "/MSO/Test/metricsjboss.server.name_IS_UNDEFINED.log";
    	PrintWriter asdcConfigFileWriter = new PrintWriter(logFile);
		asdcConfigFileWriter.print("");
		asdcConfigFileWriter.flush();
		asdcConfigFileWriter.close();
    }	
    
    @Before
    public final void cleanAuditLogFile() throws FileNotFoundException {
    	URL url = this.getClass().getClassLoader().getResource("logback-test.xml");
    	String logFile = url.getFile().substring(0, url.getFile().indexOf("test-classes")) + "/MSO/Test/auditjbo                                                                                                                           ss.server.name_IS_UNDEFINED.log";
    	PrintWriter asdcConfigFileWriter = new PrintWriter(logFile);
		asdcConfigFileWriter.print("");
		asdcConfigFileWriter.flush();
		asdcConfigFileWriter.close();
    }	



    /**
     * This method implements a test of getSeverifyLevel method.
     */
	@Test
    public final void testGetSeverityLevel () {

		try {
			String levelInfo = (String)invokePriveMethod("getSeverityLevel", "INFO");
			Assert.assertEquals (levelInfo, "0");

			String levelWarn = (String)invokePriveMethod("getSeverityLevel", "WARN");
			Assert.assertEquals (levelWarn, "1");

			String levelERROR = (String)invokePriveMethod("getSeverityLevel", "ERROR");
			Assert.assertEquals (levelERROR, "2");

			String levelDEBUG = (String)invokePriveMethod("getSeverityLevel", "DEBUG");
			Assert.assertEquals (levelDEBUG, "0");

			String levelFATAL = (String)invokePriveMethod("getSeverityLevel", "FATAL");
			Assert.assertEquals (levelFATAL, "3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * This method implements a test of getFinalServiceName method.
     */
	@Test
    public final void testGetFinalServiceName ()  {
		try {
			String serviceName1 = (String)invokePriveMethod("getFinalServiceName", "testServiceName1");
			Assert.assertEquals(serviceName1, "testServiceName1");

			MsoLogger.setServiceName("testServiceName2");
			String serviceName2 = (String)invokePriveMethod("getFinalServiceName", "testServiceName1");
			Assert.assertEquals(serviceName2, "testServiceName1");

			String msgNull = null;
			String serviceName3 = (String)invokePriveMethod("getFinalServiceName", msgNull);
			Assert.assertEquals(serviceName3, "testServiceName2");

			MsoLogger.resetServiceName();
			String serviceName4 = (String)invokePriveMethod("getFinalServiceName", msgNull);
			Assert.assertEquals(serviceName4, "invoke0");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Test
    public final void testPrepareMsg ()  {
		try {
			String msgNull = null;
			MDC.clear();
			invokePrepareMsg("INFO", null, null);

			Assert.assertTrue (MDC.get(MsoLogger.REQUEST_ID).equals("trace-#") && MDC.get(MsoLogger.SERVICE_INSTANCE_ID).equals("trace-#") && MDC.get(MsoLogger.SERVICE_NAME).equals("invoke0")
					&& MDC.get(MsoLogger.TIMER) == null && MDC.get(MsoLogger.ALERT_SEVERITY).equals("0"));

			MsoLogger.setLoggerParameters("testRemoteIp", "testUser");
			MsoLogger.setLogContext("testReqId", "testSvcId");
			invokePrepareMsg("ERROR", "testServiceName3", null);
			Assert.assertTrue (MDC.get(MsoLogger.REQUEST_ID).equals("testReqId") && MDC.get(MsoLogger.SERVICE_INSTANCE_ID).equals("testSvcId") && MDC.get(MsoLogger.SERVICE_NAME).equals("testServiceName3")
					&& MDC.get(MsoLogger.TIMER) == null && MDC.get(MsoLogger.ALERT_SEVERITY).equals("2") );

			MsoLogger.setServiceName("testServiceName2");
			invokePrepareMsg("WARN", msgNull, msgNull);
			Assert.assertTrue (MDC.get(MsoLogger.REQUEST_ID).equals("testReqId") && MDC.get(MsoLogger.SERVICE_INSTANCE_ID).equals("testSvcId") && MDC.get(MsoLogger.SERVICE_NAME).equals("testServiceName2")
					&& MDC.get(MsoLogger.TIMER) == null && MDC.get(MsoLogger.ALERT_SEVERITY).equals("1"));

			MDC.clear ();
			MsoRequest msoRequest = new MsoRequest ();
			msoRequest.setRequestId ("reqId2");
			msoRequest.setServiceInstanceId ("servId2");
			MsoLogger.setLogContext (msoRequest);
            invokePrepareMsg("FATAL", null, "123");
            Assert.assertTrue (MDC.get(MsoLogger.REQUEST_ID).equals("reqId2") && MDC.get(MsoLogger.SERVICE_INSTANCE_ID).equals("servId2") && MDC.get(MsoLogger.TIMER).equals("123") && MDC.get(MsoLogger.ALERT_SEVERITY).equals("3"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * This method implements a test of log methods
     */
	@Test
    public final void testLogMethods () {
		try {
			MDC.clear();
			MsoLogger.setLogContext("reqId2", "servId2");
			MsoLogger.setServiceName("MSO.testServiceName");
			msoLogger.info (MessageEnum.LOGGER_UPDATE_SUC, "testLogger", "INFO", "DEBUG", "target entity", "target service");
			msoLogger.warn (MessageEnum.GENERAL_WARNING, "warning test", "", "", MsoLogger.ErrorCode.UnknownError, "warning test");
			msoLogger.error (MessageEnum.GENERAL_EXCEPTION, "target entity", "target service", MsoLogger.ErrorCode.UnknownError, "error test");

			//Fetch from the error log
			URL url = this.getClass().getClassLoader().getResource("logback-test.xml");
			String logFile = url.getFile().substring(0, url.getFile().indexOf("test-classes")) + "/MSO/Test/errorjboss.server.name_IS_UNDEFINED.log";

			Path filePath = new File(logFile).toPath();
			Charset charset = Charset.defaultCharset();
			List<String> stringList = Files.readAllLines(filePath, charset);
			String[] stringArray = stringList.toArray(new String[]{});
			int size = stringArray.length;

			Assert.assertTrue(stringArray[size-3].contains("|reqId2|main|MSO.testServiceName||target entity|target service|INFO|null||") && stringArray[size-3].contains("||MSO-GENERAL-5408I Successfully update Logger: testLogger from level INFO to level DEBUG"));
			Assert.assertTrue(stringArray[size-2].contains("|reqId2|main|MSO.testServiceName||||WARN|UnknownError|warning test|") && stringArray[size-2].contains("|MSO-GENERAL-5401W WARNING: warning test"));
			Assert.assertTrue(stringArray[size-1].contains("|reqId2|main|MSO.testServiceName||target entity|target service|ERROR|UnknownError|error test|") && stringArray[size-1].contains("|MSO-GENERAL-9401E Exception encountered"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

     /**
     * This method implements a test of recordMetricEvent method.
     */
	@Test
    public final void testRecordMetricEvent () {
		try {
			MDC.clear();
			MsoLogger.setLogContext("reqId", "servId");
			msoLogger.recordMetricEvent(123456789L, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful", "VNF" , "createVNF", null);
			MDC.put (MsoLogger.REMOTE_HOST, "127.0.0.1");
			MDC.put (MsoLogger.PARTNERNAME, "testUser");
			msoLogger.recordMetricEvent(123456789L, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Exception", "SDNC", "removeSDNC", "testVNF");

			//Fetch from the metric log
			URL url = this.getClass().getClassLoader().getResource("logback-test.xml");
			String logFile = url.getFile().substring(0, url.getFile().indexOf("test-classes")) + "/MSO/Test/metricsjboss.server.name_IS_UNDEFINED.log";

			Path filePath = new File(logFile).toPath();
			Charset charset = Charset.defaultCharset();
			List<String> stringList = Files.readAllLines(filePath, charset);
			String[] stringArray = stringList.toArray(new String[]{});
			msoLogger.error (MessageEnum.GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.UnknownError, "test error msg");

			Assert.assertTrue(stringArray[0].contains("|reqId|servId|main||testRecordMetricEvent||VNF|createVNF|COMPLETE|0|Successful|Test UUID as JBoss not found|INFO|0|"));
			// count the occurance of symbol "|"
			Assert.assertTrue ((stringArray[0].length() - stringArray[0].replace("|", "").length()) == 28);
			Assert.assertTrue(stringArray[1].contains("|reqId|servId|main||testRecordMetricEvent|testUser|SDNC|removeSDNC|ERROR|501|Exception|Test UUID as JBoss not found|INFO|0|") && stringArray[1].contains("|127.0.0.1||||testVNF|||||"));
			Assert.assertTrue ((stringArray[1].length() - stringArray[1].replace("|", "").length()) == 28);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * This method implements a test of testRecordAuditEvent method.
     */
	@Test
    public final void testRecordAuditEvent () {

		try {

			MDC.clear();
			MsoLogger.setLogContext("reqId", "servId");
			msoLogger.recordAuditEvent(123456789L, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
			MDC.put (MsoLogger.REMOTE_HOST, "127.0.0.1");
			MDC.put (MsoLogger.PARTNERNAME, "testUser");
			msoLogger.recordAuditEvent(123456789L, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Exception");

			//Fetch from the metric log
			URL url = this.getClass().getClassLoader().getResource("logback-test.xml");
			String logFile = url.getFile().substring(0, url.getFile().indexOf("test-classes")) + "/MSO/Test/auditjboss.server.name_IS_UNDEFINED.log";

			Path filePath = new File(logFile).toPath();
			Charset charset = Charset.defaultCharset();
			List<String> stringList = Files.readAllLines(filePath, charset);
			String[] stringArray = stringList.toArray(new String[]{});
			msoLogger.error (MessageEnum.GENERAL_EXCEPTION, "", "", ErrorCode.UnknownError, "log error");

			Assert.assertTrue (stringArray[0].contains("|reqId|servId|main||testRecordAuditEvent||COMPLETE|0|Successful|Test UUID as JBoss not found|INFO|0|"));
			// count the occurance of symbol "|"
			Assert.assertTrue ((stringArray[0].length() - stringArray[0].replace("|", "").length()) == 25);
			Assert.assertTrue (stringArray[1].contains("|reqId|servId|main||testRecordAuditEvent|testUser|ERROR|501|Exception|Test UUID as JBoss not found|INFO|0|") && stringArray[1].contains("|127.0.0.1||||||||"));
			Assert.assertTrue ((stringArray[1].length() - stringArray[1].replace("|", "").length()) == 25);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }




    // User reflection to invoke to avoid change the publicity of the method
    private static String invokePrepareMsg  (String arg1, String arg2, String arg3) {
    	Method method;
		try {
			method = MsoLogger.class.getDeclaredMethod("prepareMsg", String.class, String.class, String.class);
			method.setAccessible(true);
	    	return  (String)method.invoke(msoLogger, arg1, arg2, arg3);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    // User reflection to invoke to avoid change the publicity of the method
    private static Object invokePriveMethod (String methodName, String arg) {
    	Method method;
		try {
			method = MsoLogger.class.getDeclaredMethod(methodName, String.class);
			method.setAccessible(true);
	    	return  method.invoke(msoLogger, arg);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
}
