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

package org.openecomp.mso.logger;


import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**
 * Wrapper around log4j and Nagios NRDP passive alarming for MSO.
 *
 * For local alarm logging, this class will look for an alarm log file name
 * in the servlet context parameter "mso.alarms.file". If none is found,
 * it will look for an MsoProperty of the same name. As a last resort,
 * it will use the default path "/var/log/ecomp/MSO/alarms/alarm.log".
 * It is expected that all alarms within an application will use the same
 * alarm file, so there is no way to dynamically add other alarm files.
 *
 * Alarms are logged as a simple pipe-delimited string of the format:
 * <dateTime>|<alarmType>|<state>|<detailMessage>
 *
 * This class also supports real-time Nagios NRDP alarming. If enabled via
 * MsoProperties, all alarms generated and logged to the local alarm file will
 * also be transmitted to a Nagios NRDP instance. NRDP requires 4 parameters
 * in service alarm events (all Mso Alarms will be Service Alarms):
 * hostname, servicename, state, detail
 *
 * The log file format is also intended to be compatible with Nagios NRDP for
 * non-real-time reporting. The command-line tool for sending alarms is
 * is "send_nrdp.php", which takes the same 4 parameters as input.
 * It will be easy enough to translate entries from an alarm.log file to
 * NRDP if real-time NRDP alarming is not enabled.
 *
 * For Nagios integration, the alarmTypes should all match "service names"
 * configured in the receiving Nagios server. Also, the alarm state will
 * be limited to the 4 values defined by Nagios:
 * 0 = OK, 1 = Warning, 2 = Critical, 3 = Unknown
 *
 *
 */
public class MsoAlarmLogger implements ServletContextListener {

	private Logger alarmLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MSO_ALARM_CONTEXT);
    private static RollingFileAppender<ILoggingEvent> fileAppender = null;
    public static final String DEFAULT_MSO_ALARM_FILE = "/var/log/ecomp/MSO/alarms/alarm.log";
    public static final String MSO_ALARM_CONTEXT = "mso.alarms";
    
    public static final int OK = 0;
    public static final int WARNING = 1;
    public static final int CRITICAL = 2;
    public static final int UNKNOWN = 3;

    /**
     * Get the default MSO Alarm Logger
     */
    public MsoAlarmLogger () {
    	    	
    	initializeAlarmLogger(null);

    }

    public MsoAlarmLogger (String alarmFile) {
    	initializeAlarmLogger(alarmFile);

    }

     /**
     * Method to record an alarm.
     *
     * @param alarm - the alarm identifier (Nagios "service")
     * @param state - the alarm state/severity, based on Nagios service
     *        state values: 0 = OK, 1 = Warning, 2 = Critical, 3 = Unknown
     * @param detail - detail message (may contain additional internal
     *        structure per alarm type)
     */
    public void sendAlarm (String alarm, int state, String detail) {
        // Write the alarm to Log file
        if (alarmLogger != null) {
            String output = alarm + "|" + state + "|" + detail;
            alarmLogger.info (output);
        }

    }

    @Override
    public void contextDestroyed (ServletContextEvent event) {
        // Nothing to do...
    }

    @Override
    public void contextInitialized (ServletContextEvent event) {
        String msoAlarmFile = event.getServletContext ().getInitParameter ("mso.alarm.file");
        if (msoAlarmFile == null) {
            msoAlarmFile = DEFAULT_MSO_ALARM_FILE;
        }

        initializeAlarmLogger (msoAlarmFile);
    }

    private void initializeAlarmLogger (String alarmFile) {
        synchronized (MsoAlarmLogger.class) {
            if (fileAppender == null) {
            	if (alarmFile != null) {
            		fileAppender = MsoAlarmLogger.getAppender (alarmFile);
            	} else {
            		fileAppender = MsoAlarmLogger.getAppender (DEFAULT_MSO_ALARM_FILE);
            	}
            }
        }
        // The alarmLogger was static originally.
        // The initialization of the alarmLogger was fine, but not sure why, it lost its appender info later
        // Due to that issue, the alarmLogger is not static any more.
        // Instead static attribute fileAppender is added and will be assigned to the alarmLogger every time new MsoAlarmLogger is created.
        alarmLogger.setLevel (Level.INFO);
        alarmLogger.addAppender (fileAppender);
        alarmLogger.setAdditive (false);
    }


    private static RollingFileAppender<ILoggingEvent> getAppender (String msoAlarmFile) {
        // Create a Logger for alarms. Just use a default Pattern that outputs
        // a message. MsoAlarmLogger will handle the formatting.
        File alarmFile = new File (msoAlarmFile);
        File alarmDir = alarmFile.getParentFile ();
        if (!alarmDir.exists ()) {
            alarmDir.mkdirs ();
        }

        String logPattern = "%d{yyyy-MM-dd HH:mm:ss}|%m%n";

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder encoder=new PatternLayoutEncoder();
        encoder.setPattern(logPattern);
        encoder.setContext(context);
        encoder.start();
        RollingFileAppender<ILoggingEvent> fileAppender= new RollingFileAppender<>();
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy= new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(msoAlarmFile + ".%d");
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.start();
        fileAppender.setFile(msoAlarmFile);
        fileAppender.setAppend(true);
        fileAppender.setEncoder(encoder);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setContext(context);
        fileAppender.start();

        return fileAppender;
    }

}
