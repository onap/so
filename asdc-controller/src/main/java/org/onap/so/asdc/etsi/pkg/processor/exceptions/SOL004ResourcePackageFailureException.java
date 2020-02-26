/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.asdc.etsi.pkg.processor.exceptions;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class SOL004ResourcePackageFailureException extends RuntimeException {

    private static final long serialVersionUID = 5834657185124807797L;

    public SOL004ResourcePackageFailureException(final String message) {
        super(message);

    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
