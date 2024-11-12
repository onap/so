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

import java.security.Principal;
import java.util.List;



/**
 * LUR: Local User Registry
 *
 * Concept by Robert Garskof, Implementation by Jonathan Gathman
 *
 * Where we can keep local copies of users and roles for faster Authorization when asked.
 *
 * Note: Author cannot resist the mental image of using a Fishing Lure to this LUR pattern
 *
 * @author Jonathan
 *
 */
public interface Lur {
    /**
     * Allow the Lur, which has correct Permission access, to create and hand back.
     */
    public Permission createPerm(String p);

    /**
     * Fish for Principals in a Pond
     *
     * or more boringly, is the User identified within a named collection representing permission.
     *
     * @param principalName
     * @return
     */
    public boolean fish(Principal bait, Permission... pond);

    /**
     * Fish all the Principals out a Pond
     *
     * For additional humor, pronounce the following with a Southern Drawl, "FishOil"
     *
     * or more boringly, load the List with Permissions found for Principal
     *
     * @param principalName
     * @return
     */
    public void fishAll(Principal bait, List<Permission> permissions);

    /**
     * Allow implementations to disconnect, or cleanup resources if unneeded
     */
    public void destroy();

    /**
     * Does this LUR handle this pond exclusively? Important for EpiLUR to determine whether to try another (more
     * expensive) LUR
     * 
     * @param pond
     * @return
     */
    public boolean handlesExclusively(Permission... pond);

    /**
     * Does the LUR support a particular kind of Principal This can be used to check name's domain, like above, or
     * Principal type
     */
    public boolean handles(Principal principal);

    /**
     * Clear: Clear any Caching, if exists
     */
    public void clear(Principal p, StringBuilder report);
}
