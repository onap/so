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

/**
 * The unique element of TAF is that we establish the relationship/mechanism to mutate the Principal derived from one
 * Authentication mechanism into a trustable Principal of another. The mechanism needs to be decided by system trusting.
 *
 * The Generic "T" is used so that the code used will be very specific for the implementation, enforced by Compiler
 *
 * This interface will allow differences of trusting Transmutation of Authentication
 * 
 * @author Jonathan
 *
 */
public interface Transmutate<T> {
    /**
     * Mutate the (assumed validated) Principal into the expected Principal name to be used to construct
     *
     * @param p
     * @return
     */
    public T mutate(Principal p);
}
