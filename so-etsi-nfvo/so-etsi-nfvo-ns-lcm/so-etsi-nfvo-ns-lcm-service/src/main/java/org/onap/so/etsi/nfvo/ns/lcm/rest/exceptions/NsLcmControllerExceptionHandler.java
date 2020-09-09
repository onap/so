/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
package org.onap.so.etsi.nfvo.ns.lcm.rest.exceptions;

import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.NsRequestProcessingException;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.onap.so.etsi.nfvo.ns.lcm.rest.NsLifecycleManagementController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@ControllerAdvice(assignableTypes = NsLifecycleManagementController.class)
public class NsLcmControllerExceptionHandler {

    @ExceptionHandler(NsRequestProcessingException.class)
    public ResponseEntity<InlineResponse400> handleNsRequestProcessingException(
            final NsRequestProcessingException nsRequestProcessingException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(getInlineResponse400(nsRequestProcessingException));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<InlineResponse400> handleNsRequestProcessingException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InlineResponse400()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(exception.getMessage()));
    }

    private InlineResponse400 getInlineResponse400(final NsRequestProcessingException nsRequestProcessingException) {
        if (nsRequestProcessingException.getProblemDetails() != null) {
            return nsRequestProcessingException.getProblemDetails();
        }
        return new InlineResponse400().status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(nsRequestProcessingException.getMessage());
    }

}
