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

package org.onap.so.cloud.authentication;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.BaseTest;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.cloud.authentication.models.RackspaceAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;

/**
 * A few JUnit tests to evaluate the new factory that manages authentication
 * types and their associated wrapper classes. Here it is assumed that core types
 * only are tested.
 *
 */
public class AuthenticationMethodTest extends BaseTest {

	@Autowired
	private AuthenticationMethodFactory authenticationMethodFactory;
	/**
	 * 
	 */
	public AuthenticationMethodTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Test
	public void testCustomRackspaceAuth() {
		CloudIdentity ci = new CloudIdentity();
		ci.setIdentityAuthenticationType(AuthenticationType.RACKSPACE_APIKEY);
		ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
		ci.setMsoId("test");
		
		Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
		assertTrue(RackspaceAuthentication.class.equals(auth.getClass()));
	
	}
	
	@Test
	public void testCoreUsernamePasswordAuth() {
		CloudIdentity ci = new CloudIdentity();
		ci.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);
		ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
		ci.setMsoId("someuser");
		
		Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
		assertTrue(UsernamePassword.class.equals(auth.getClass()));
		
	}
	
	@Test
	public void testCustomRackspaceAuthFromCloudIdentity() {
		CloudIdentity ci = new CloudIdentity();
		ci.setIdentityAuthenticationType(AuthenticationType.RACKSPACE_APIKEY);
		ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
		ci.setMsoId("test");
		
		Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
		assertTrue(RackspaceAuthentication.class.equals(auth.getClass()));
	}
	
	@Test
	public void testCoreUsernamePasswordAuthFromCloudIdentity() {
		CloudIdentity ci = new CloudIdentity();
		ci.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);
		ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
		ci.setMsoId("someuser");

		Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
		assertTrue(UsernamePassword.class.equals(auth.getClass()));
	
	}
}
