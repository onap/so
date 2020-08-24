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

package org.onap.so.asdc.installer;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.so.db.catalog.beans.CnfResource;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;

/**
 * The structure that contains the artifactInfo and its associated DownloadedResult.
 *
 */
public final class VfModuleArtifact {
    private final IArtifactInfo artifactInfo;
    private int deployedInDb = 0;
    private final String result;
    private HeatFiles heatFiles;
    private HeatTemplate heatTemplate;
    private HeatEnvironment heatEnvironment;
    private List<CnfResource> cnfResources;

    public VfModuleArtifact(IArtifactInfo artifactinfo, IDistributionClientDownloadResult clientResult)
            throws UnsupportedEncodingException {
        this(artifactinfo, clientResult, null);
    }

    public VfModuleArtifact(IArtifactInfo artifactinfo, IDistributionClientDownloadResult clientResult,
            String modifiedHeatTemplate) throws UnsupportedEncodingException {
        artifactInfo = artifactinfo;
        if (modifiedHeatTemplate != null) {
            result = modifiedHeatTemplate;
        } else {
            result = new String(clientResult.getArtifactPayload(), "UTF-8");
        }
    }

    public HeatFiles getHeatFiles() {
        return heatFiles;
    }

    public void setHeatFiles(HeatFiles heatFiles) {
        this.heatFiles = heatFiles;
    }

    public HeatTemplate getHeatTemplate() {
        return heatTemplate;
    }

    public void setHeatTemplate(HeatTemplate heatTemplate) {
        this.heatTemplate = heatTemplate;
    }

    public IArtifactInfo getArtifactInfo() {
        return artifactInfo;
    }

    public String getResult() {
        return result;
    }

    public int getDeployedInDb() {
        return deployedInDb;
    }

    public void incrementDeployedInDB() {
        ++deployedInDb;
    }

    public HeatEnvironment getHeatEnvironment() {
        return heatEnvironment;
    }

    public void setHeatEnvironment(HeatEnvironment heatEnvironment) {
        this.heatEnvironment = heatEnvironment;
    }

    public List<CnfResource> getCnfResources() {
        return cnfResources;
    }

    public void setCnfResources(List<CnfResource> cnfResources) {
        this.cnfResources = cnfResources;
    }

    public void addCnfResource(CnfResource cnfResource) {
        if (cnfResources == null) {
            cnfResources = new ArrayList<CnfResource>();
        }
        cnfResources.add(cnfResource);
    }

}
