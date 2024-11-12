/**
 * ============LICENSE_START==================================================== org.onap.so
 * =========================================================================== Copyright (c) 2018 AT&T Intellectual
 * Property. All rights reserved. =========================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.so.security.cadi;

/**
 * An Interface for testing on Requests to see if we can get a User and Password It works for CadiWrap, but also,
 * Container Specific Wraps (aka Tomcat) should also implement.
 *
 * @author Jonathan
 *
 */
public interface BasicCred extends GetCred {
    public void setUser(String user);

    public void setCred(byte[] passwd);

    public String getUser();
}
