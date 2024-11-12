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
 * Interface to add a User Chain String to Principal
 *
 *
 *
 * Where APP is name suitable for Logging (i.e. official App Acronym) ID is official User or MechID, best if includes
 * Identity Source (i.e. ab1234@people.osaaf.org) Protocol is the Security protocol,
 *
 * Format:<ID>:<APP>:<protocol>[:AS][,<ID>:<APP>:<protocol>]*
 *
 *
 * @author Jonathan
 *
 */
public interface UserChain {
    public enum Protocol {
        BasicAuth, Cookie, Cert, OAuth
    };

    public String userChain();
}
