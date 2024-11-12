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

/**
 * RolesAllowed
 *
 * @author Jonathan
 *
 *         Similar to Java EE's Spec from Annotations 1.1, 2.8
 *
 *         That Spec, however, was geared towards being able to route calls to Methods on Objects, and thus needed a
 *         more refined sense of permissions hierarchy. The same mechanism, however, can easily be achieved on single
 *         Servlet/Handlers in POJOs like Jetty by simply adding the Roles Allowed in a similar Annotation
 *
 */
package org.onap.so.security.cadi.filter;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.servlet.Servlet;

/**
 *
 * @author Jonathan
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface ServletImpl {
    /**
     * Security role of the implementation, which doesn't have to be an EJB or CORBA like object. Can be just a Handler
     * 
     * @return
     */
    Class<? extends Servlet> value();
}
