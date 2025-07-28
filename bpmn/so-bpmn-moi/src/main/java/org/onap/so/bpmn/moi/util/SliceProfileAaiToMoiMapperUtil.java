/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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


package org.onap.so.bpmn.moi.util;

import org.onap.aai.domain.yang.MaxNumberOfUes;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.so.bpmn.moi.tasks.AssignRANNssiBBTasks;
import org.onap.so.moi.Attributes;
import org.onap.so.moi.PlmnId;
import org.onap.so.moi.Snssai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SliceProfileAaiToMoiMapperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SliceProfileAaiToMoiMapperUtil.class);


    public ServiceInstance fillSliceProfileInstanceFromMoiRequest(Attributes moiRequestAttributes,
            ServiceInstance serviceInstance) {

        // org.onap.so.moi.SliceProfile moiSliceProfile = null;

        String serviceInstanceLocationId = null;
        String environmentContext = null;
        String serviceType = null;
        String operationalState = "LOCKED";
        String orchistrationStatus = "Assigned";

        for (org.onap.so.moi.SliceProfile moiSliceProfile : moiRequestAttributes.getSliceProfileList()) {
            serviceInstanceLocationId = getPlmnId(moiSliceProfile.getPlmnInfoList().get(0).getPlmnId());
            environmentContext = getSnssai(moiSliceProfile.getPlmnInfoList().get(0).getSnssai());
            serviceType = moiSliceProfile.getRANSliceSubnetProfile().getServiceType();
        }

        serviceInstance.setServiceInstanceLocationId(serviceInstanceLocationId);
        serviceInstance.setEnvironmentContext(environmentContext);
        serviceInstance.setServiceType(serviceType);
        serviceInstance.setOperationalStatus(operationalState);
        return serviceInstance;
    }

    String getSnssai(Snssai snssai) {
        return snssai.getSst() + "-" + snssai.getSd();
    }

    String getPlmnId(PlmnId plmnId) {
        return plmnId.getMcc() + "-" + plmnId.getMnc();
    }

    public SliceProfile extractAaiSliceProfileFromMoiRequest(Attributes moiRequestAttributes) {

        SliceProfile aaiSLiceProfile = null;

        for (org.onap.so.moi.SliceProfile sliceProfileMoi : moiRequestAttributes.getSliceProfileList()) {
            aaiSLiceProfile = mapMoiSliceProfileToAaiSliceProfile(sliceProfileMoi);
        }

        return aaiSLiceProfile;
    }

    private SliceProfile mapMoiSliceProfileToAaiSliceProfile(org.onap.so.moi.SliceProfile moiSliceProfile) {
        SliceProfile aaiSliceProfile = new SliceProfile();

        Integer latency = moiSliceProfile.getRANSliceSubnetProfile().getLatency();
        Integer areaTrafficCapDL = moiSliceProfile.getRANSliceSubnetProfile().getAreaTrafficCapDL();
        Integer maxNumberOfUEs = moiSliceProfile.getRANSliceSubnetProfile().getMaxNumberofUEs();
        String resourceSharingLevel = moiSliceProfile.getRANSliceSubnetProfile().getResourceSharingLevel();
        Integer coverageAreaTAList = moiSliceProfile.getRANSliceSubnetProfile().getCoverageAreaTAList();

        aaiSliceProfile.setLatency(latency);
        LOGGER.warn("Setting max-number-of-ues is not supported yet, setting empty value");
        aaiSliceProfile.setMaxNumberOfUes(new MaxNumberOfUes()); // TODO: adjust org.onap.so.moi.SliceProfile to allow
                                                                 // to set a proper value here
        aaiSliceProfile.setResourceSharingLevel(resourceSharingLevel);
        aaiSliceProfile.setCoverageAreaTAList(String.valueOf(coverageAreaTAList));
        aaiSliceProfile.setAreaTrafficCapDL(areaTrafficCapDL);

        return aaiSliceProfile;
    }
}
