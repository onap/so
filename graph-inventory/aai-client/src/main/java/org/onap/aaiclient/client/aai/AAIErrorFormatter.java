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

package org.onap.aaiclient.client.aai;

import java.util.List;
import org.onap.aaiclient.client.aai.entities.AAIError;
import org.onap.aaiclient.client.aai.entities.ServiceException;

public class AAIErrorFormatter {

    private final AAIError error;

    public AAIErrorFormatter(AAIError error) {
        this.error = error;
    }

    public String getMessage() {
        if (error.getRequestError() != null && error.getRequestError().getServiceException() != null) {
            ServiceException serviceException = error.getRequestError().getServiceException();
            return this.fillInTemplate(serviceException.getText(), serviceException.getVariables());
        }

        return "no parsable error message found";
    }

    protected String fillInTemplate(String text, List<String> variables) {
        for (int i = 0; i < variables.size(); i++) {
            variables.set(i, this.format(variables.get(i), variables));
        }

        return format(text, variables);
    }

    protected String format(String s, List<String> variables) {
        s = s.replaceAll("%(\\d(?!\\d))", "%$1\\$s");
        s = s.replaceAll("%(\\d{2})", "%%$1");
        return String.format(s, variables.toArray());
    }
}
