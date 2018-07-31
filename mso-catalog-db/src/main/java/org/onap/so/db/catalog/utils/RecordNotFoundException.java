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

package org.onap.so.db.catalog.utils;

/*
* Exception of the ASDC controller.
*/
public class RecordNotFoundException extends Exception {

	/**
    * serialization id.
	 */
	private static final long serialVersionUID = 8425657297510362736L;

   /**
    * @param message The message to dump
    * @param cause The Throwable cause object
    */
   public RecordNotFoundException(final String message) {
       super (message);

   }

   /**
    * @param message The message to dump
    * @param cause The Throwable cause object
    */
   public RecordNotFoundException(final String message, final Throwable cause) {
       super (message, cause);

   }
}
