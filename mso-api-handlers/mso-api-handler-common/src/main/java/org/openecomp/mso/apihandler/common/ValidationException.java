/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.apihandler.common;


public class ValidationException extends Exception {

    /**
     * This class simply extends Exception (without addition additional functionality)
     * to provide an identifier for RequestsDB related exceptions on create, delete, query.
     * 
     *
     **/

    private static final long serialVersionUID = 1L;
    private static final String validationFailMessage = "No valid $ELEMENT is specified";

    public ValidationException (String msg) {
        super (validationFailMessage.replaceAll ("\\$ELEMENT", msg));
    }

    public ValidationException (String msg, Exception cause) {
        super (validationFailMessage.replaceAll ("\\$ELEMENT", msg), cause);
    }

}
