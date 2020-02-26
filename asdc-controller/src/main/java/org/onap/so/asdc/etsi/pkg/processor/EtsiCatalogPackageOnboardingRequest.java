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
package org.onap.so.asdc.etsi.pkg.processor;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class EtsiCatalogPackageOnboardingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "csarId")
    private String csarId;

    public EtsiCatalogPackageOnboardingRequest csarId(final String csarId) {
        this.csarId = csarId;
        return this;
    }

    public String getCsarId() {
        return csarId;
    }

    public void setCsarId(final String csarId) {
        this.csarId = csarId;
    }

    @Override
    public String toString() {
        return "EtsiCatalogPackageOnboardingRequest  [csarId=" + csarId + "]";
    }

}
