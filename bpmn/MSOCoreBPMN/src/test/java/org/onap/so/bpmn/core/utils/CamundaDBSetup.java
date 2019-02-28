/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.core.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the unit test (H2) database for Camunda.
 */
public class CamundaDBSetup {
	private static boolean isDBConfigured = false;
	private static final Logger logger = LoggerFactory.getLogger(CamundaDBSetup.class);
	
	private CamundaDBSetup() {
	}
	
	public static synchronized void configure() throws SQLException {
		if (isDBConfigured) {
			return;
		}

		logger.debug ("Configuring the Camunda H2 database for MSO");

		Connection connection = null;
		PreparedStatement stmt = null;

		try {


			isDBConfigured = true;
		
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.debug("Exception :",e);
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					logger.debug("Exception :",e);
				}
			}
		}
	}
}
