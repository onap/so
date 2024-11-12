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

package org.onap.so.security.cadi.wsse;

/**
 * Interface to specify an action deep within a parsing tree on a local object
 *
 * We use a Generic so as to be flexible on create what that object actually is. This is passed in at the root "parse"
 * call of Match. Similar to a "Visitor" Pattern, this object is passed upon reaching the right point in a parse tree.
 *
 * @author Jonathan
 *
 * @param <OUTPUT>
 */
interface Action<OUTPUT> {
    public boolean content(OUTPUT output, String text);
}
