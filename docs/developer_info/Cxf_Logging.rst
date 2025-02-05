.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2020 Huawei Technologies Co., Ltd.

CXF-logging:
=============
It is a logging framework from SO. Here basically we are having 2 interceptors which extends AbstractSoapInterceptor.

SOAPLoggingInInterceptor:
+++++++++++++++++++++++++++

* This interceptor is responsible for Capturing requestId , client ip address , invocation id, service name, instance id, entry timestamp , log timestamp, Elapsed time for each request and update the MDC logger with staus In-progress.

SOAPLoggingOutInterceptor:
++++++++++++++++++++++++++++
* This interceptor is responsible for log timestamp , elapsed time for each request and checks for if there is any exception update the MDC loggers with 500 response code otherwise update the status as completed.


cxf-logging Used By below components of SO:
++++++++++++++++++++++++++++++++++++++++++++
The cxf-logging framework is used by below components of so.

mso-openstack-adapter.
mso-requests-db-adapter.
mso-sdnc-adapter.
mso-infrastructure-bpmn.

cxf-logging dependency for using in other components:
++++++++++++++++++++++++++++++++++++++++++++++++++++++
  
  <dependency>
      <groupId>org.onap.so</groupId>
      <artifactId>cxf-logging</artifactId>
      <version>${project.version}</version>

    </dependency>

pom.xml:
+++++++++
<?xml version="1.0"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 

  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"> 

  <modelVersion>4.0.0</modelVersion>
  <parent>

    <groupId>org.onap.so</groupId>
    <artifactId>so</artifactId>
    <version>1.6.0-SNAPSHOT</version>

  </parent>
  <name>CXFLogging</name>
  <description>Common CXF Logging Classes</description>
  <dependencies>

    <dependency>

      <groupId>org.apache.cxf</groupId>
      <artifactId>cxf-rt-rs-client</artifactId>
      <version>${cxf.version}</version>

    </dependency>

    <dependency>
      <groupId>org.apache.cxf</groupId>
      <artifactId>cxf-rt-bindings-soap</artifactId>
      <version>${cxf.version}</version>

    </dependency>

    <dependency>

      <groupId>org.apache.cxf</groupId>
      <artifactId>cxf-rt-transports-http</artifactId>
      <version>${cxf.version}</version>

    </dependency>

    <dependency>

      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>

    </dependency>

    <dependency>

      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-ext</artifactId>

    </dependency>

    <dependency>

      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>

    </dependency>

  </dependencies>

  <build>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>

      </resource>

      <resource>

        <directory>src/main/java</directory>

        <includes>

          <include>*.java</include>

        </includes>

      </resource>

    </resources>

  </build>

  <artifactId>cxf-logging</artifactId>

</project>

Configuration file:
++++++++++++++++++++
Here we can do configure the logger properties for the cxf-logging.

<configuration>

	<property name="p_tim" value="%d{&quot;yyyy-MM-dd'T'HH:mm:ss.SSSXXX&quot;, UTC}"/>

    <property name="p_lvl" value="%level"/>

    <property name="p_log" value="%logger"/>

    <property name="p_mdc" value="%replace(%replace(%mdc){'\t','\\\\t'}){'\n', '\\\\n'}"/>

    <property name="p_msg" value="%replace(%replace(%msg){'\t', '\\\\t'}){'\n','\\\\n'}"/>

    <property name="p_exc" value="%replace(%replace(%rootException){'\t', '\\\\t'}){'\n','\\\\n'}"/>

    <property name="p_mak" value="%replace(%replace(%marker){'\t', '\\\\t'}){'\n','\\\\n'}"/>

    <property name="p_thr" value="%thread"/>

    <property name="pattern" value="%nopexception${p_tim}\t${p_thr}\t${p_lvl}\t${p_log}\t${p_mdc}\t${p_msg}\t${p_exc}\t${p_mak}\t%n"/>

	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">

		<encoder>

			<pattern>${pattern}</pattern>

		</encoder>

	</appender>

	<appender name="test"
		class="org.onap.so.utils.TestAppender" />

	<logger name="com.att.ecomp.audit" level="info" additivity="false">

		<appender-ref ref="STDOUT" />

	</logger>

	<logger name="com.att.eelf.metrics" level="info" additivity="false">

		<appender-ref ref="STDOUT" />

	</logger>

	<logger name="com.att.eelf.error" level="WARN" additivity="false">

		<appender-ref ref="STDOUT" />

	</logger>

	<logger name="org.onap" level="${so.log.level:-DEBUG}" additivity="false">
		<appender-ref ref="STDOUT" />

		<appender-ref ref="test" />

	</logger>
	
	<logger name="org.flywaydb" level="DEBUG" additivity="false">
        <appender-ref ref="STDOUT" />

    </logger>

	<logger name="ch.vorburger" level="WARN" additivity="false">
		<appender-ref ref="STDOUT" />

	</logger>

	<root level="WARN">
		<appender-ref ref="STDOUT" />
		<appender-ref ref="test" />

	</root>

</configuration>

