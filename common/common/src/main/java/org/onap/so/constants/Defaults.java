/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.constants;

import java.util.Optional;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.context.ApplicationContext;

public enum Defaults {

    CLOUD_OWNER("org.onap.so.cloud-owner", "CloudOwner");

    private final String propName;
    private final String defaultValue;

    private Defaults(String propName, String defaultValue) {
        this.defaultValue = defaultValue;
        this.propName = propName;
    }

    @Override
    public String toString() {
        Optional<ApplicationContext> context = getAppContext();
        if (context.isPresent()) {
            return context.get().getEnvironment().getProperty(this.propName, this.defaultValue);
        } else {
            return this.defaultValue;
        }

    }

    protected Optional<ApplicationContext> getAppContext() {
        return Optional.ofNullable(SpringContextHelper.getAppContext());
    }
}
