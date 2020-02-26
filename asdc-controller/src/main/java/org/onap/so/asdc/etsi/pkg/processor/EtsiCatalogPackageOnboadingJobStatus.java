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
public class EtsiCatalogPackageOnboadingJobStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "jobId")
    private String jobId;

    @XmlElement(name = "responseDescriptor")
    private EtsiCatalogPackageOnboardingJobDescriptor responseDescriptor;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public EtsiCatalogPackageOnboardingJobDescriptor getResponseDescriptor() {
        return responseDescriptor;
    }

    public void setResponseDescriptor(final EtsiCatalogPackageOnboardingJobDescriptor responseDescriptor) {
        this.responseDescriptor = responseDescriptor;
    }

    @Override
    public String toString() {
        return "EtsiCatalogPackageOnboadingJobStatus [jobId=" + jobId + ", responseDescriptor=" + responseDescriptor
                + "]";
    }



}
