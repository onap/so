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

package org.openecomp.mso.apihandler.common;


public class ValidationException extends Exception {

    /**
     * This class simply extends Exception (without addition additional functionality)
     * to provide an identifier for RequestsDB related exceptions on create, delete, query.
     *
     *
     **/

    private static final long serialVersionUID = 1L;
    private static final String VALIDATION_FAIL_MESSAGE = "No valid $ELEMENT is specified";
    private static final String INVALID_ELEMENT_MESSAGE = "$ELEMENT is not valid in the $VERSION version";
    private static final String ELEMENT_MESSAGE = "\\$ELEMENT";

    public ValidationException (String msg) {
        super (VALIDATION_FAIL_MESSAGE.replaceAll (ELEMENT_MESSAGE, msg));
    }

    public ValidationException (String msg, Exception cause) {
        super (VALIDATION_FAIL_MESSAGE.replaceAll (ELEMENT_MESSAGE, msg), cause);
    }
    public ValidationException(String msg, String version) {
        super(INVALID_ELEMENT_MESSAGE.replaceAll(ELEMENT_MESSAGE, msg).replaceAll("\\$VERSION", version));
    }
}
