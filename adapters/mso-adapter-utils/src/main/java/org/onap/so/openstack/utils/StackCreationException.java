/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;

import org.onap.so.openstack.exceptions.MsoException;

public class StackCreationException extends MsoException {

    /**
     * 
     */
    private static final long serialVersionUID = 6221508301589330950L;

    boolean stackCreationFailed = false;
    boolean stackRollbackFailed = false;

    public StackCreationException(String error) {
        super(error);
    }

    public StackCreationException(String error, boolean stackCreationFailed, boolean stackRollbackFailed) {
        super(error);
        this.stackRollbackFailed = stackRollbackFailed;
        this.stackCreationFailed = stackCreationFailed;
    }

}
