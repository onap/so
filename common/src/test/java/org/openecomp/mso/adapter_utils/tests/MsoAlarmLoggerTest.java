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


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

/**
 * This junit test very roughly the alarm logger
 */
public class MsoAlarmLoggerTest {

    public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
    public static MsoAlarmLogger msoAlarmLogger;

    @BeforeClass
    public static final void createObjects() throws MsoPropertiesException {

        File outputFile = new File("target/alarm-test.log");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        msoAlarmLogger = new MsoAlarmLogger("target/alarm-test.log");
    }

    @Test
    public void testAlarmConfig() throws MsoPropertiesException, IOException {

        msoAlarmLogger.sendAlarm("test", 0, "detail message");

        FileInputStream inputStream = new FileInputStream("target/alarm-test.log");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = reader.readLine();
        String[] splitLine = line.split("\\|");
        assertTrue(splitLine.length == 4);
        assertTrue("test".equals(splitLine[1]));
        assertTrue("0".equals(splitLine[2]));
        assertTrue("detail message".equals(splitLine[3]));

        line = reader.readLine();
        assertNull(line);
        reader.close();
        inputStream.close();

        // Reset the file for others tests
        PrintWriter writer = new PrintWriter(new File("target/alarm-test.log"));
        writer.print("");
        writer.close();

    }

    @Test
    public void testAlarm() throws IOException {

        msoAlarmLogger.sendAlarm("test", 0, "detail message");
        msoAlarmLogger.sendAlarm("test2", 1, "detail message2");
        msoAlarmLogger.sendAlarm("test3", 2, "detail message3");

        FileInputStream inputStream = new FileInputStream("target/alarm-test.log");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = reader.readLine();
        String[] splitLine = line.split("\\|");
        assertTrue(splitLine.length == 4);
        assertTrue("test".equals(splitLine[1]));
        assertTrue("0".equals(splitLine[2]));
        assertTrue("detail message".equals(splitLine[3]));

        line = reader.readLine();
        splitLine = line.split("\\|");
        assertTrue(splitLine.length == 4);
        assertTrue("test2".equals(splitLine[1]));
        assertTrue("1".equals(splitLine[2]));
        assertTrue("detail message2".equals(splitLine[3]));

        line = reader.readLine();
        splitLine = line.split("\\|");
        assertTrue(splitLine.length == 4);
        assertTrue("test3".equals(splitLine[1]));
        assertTrue("2".equals(splitLine[2]));
        assertTrue("detail message3".equals(splitLine[3]));

        line = reader.readLine();
        assertNull(line);
        reader.close();
        inputStream.close();

        // Reset the file for others tests
        PrintWriter writer = new PrintWriter(new File("target/alarm-test.log"));
        writer.print("");
        writer.close();

    }
}
