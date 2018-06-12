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

package org.openecomp.mso.bpmn.servicedecomposition.entities.exceptions;

public class InvalidBuildingBlockInputException extends Exception {
	private static final long serialVersionUID = 221404474263656742L;
	
    public InvalidBuildingBlockInputException() {
        super();
    }

    public InvalidBuildingBlockInputException(String message) {
        super(message);
    }

    public InvalidBuildingBlockInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBuildingBlockInputException(Throwable cause) {
        super(cause);
    }

    protected InvalidBuildingBlockInputException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
