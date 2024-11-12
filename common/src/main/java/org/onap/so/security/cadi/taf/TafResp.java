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

package org.onap.so.security.cadi.taf;

import java.io.IOException;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.principal.TaggedPrincipal;

/**
 * Response from Taf objects, which inform users what has happened and/or what should be done
 *
 * @author Jonathan
 *
 */
public interface TafResp {
    public static enum RESP {
        IS_AUTHENTICATED,
        NO_FURTHER_PROCESSING,
        TRY_AUTHENTICATING,
        TRY_ANOTHER_TAF,
        FAIL,
        // A note was made to avoid the response REDIRECT. However, I have deemed that it is
        // unavoidable when the underlying TAF did do a REDIRECT, because it requires a HTTP
        // Service code to exit without modifying the Response any further.
        // Therefore, I have changed this to indicate what HAS happened, with should accommodate
        // both positions. Jonathan 10/18/2012
        // public static final int HTTP_REDIRECT_INVOKED = 11;
        HTTP_REDIRECT_INVOKED,
        HAS_PROCESSED
    };

    /**
     * Basic success check
     * 
     * @return
     */
    public boolean isValid();

    /**
     * String description of what has occurred (for logging/exceptions)
     * 
     * @return
     */
    public String desc();

    /**
     * Check Response
     * 
     * @return
     */
    public RESP isAuthenticated();

    /**
     * Authenticate, returning FAIL or Other Valid indication
     *
     * HTTP implementations should watch for "HTTP_REDIRECT_INVOKED", and end the HTTP call appropriately.
     * 
     * @return
     * @throws CadiException
     */
    public RESP authenticate() throws IOException;

    /**
     * Once authenticated, this object should hold a Principal created from the authorization
     * 
     * @return
     */
    public TaggedPrincipal getPrincipal();

    /**
     * Target - when Authentication Fails, need to know what ID was being attempted
     * 
     * @return
     */
    public String getTarget();

    /**
     * get the Access object which created this object, allowing the responder to appropriate Log, etc
     */
    public Access getAccess();

    /**
     * Be able to check if part of a Failed attempt
     */
    public boolean isFailedAttempt();

    /**
     * report how long this took
     * 
     * @return
     */
    public float timing();

    /**
     * Set end of timing in Millis, given Nanos
     * 
     * @param start
     */
    void timing(long start);

    /**
     * Support Taf Name
     */
    String taf();
}
