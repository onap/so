package org.onap.so.bpmn.moi.util;

import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.so.moi.Attributes;
import org.onap.so.moi.PlmnId;
import org.onap.so.moi.Snssai;
import org.springframework.stereotype.Component;

@Component
public class SliceProfileAaiToMoiMapperUtil {

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
        aaiSliceProfile.setMaxNumberOfUEs(maxNumberOfUEs);
        aaiSliceProfile.setResourceSharingLevel(resourceSharingLevel);
        aaiSliceProfile.setCoverageAreaTAList(String.valueOf(coverageAreaTAList));
        aaiSliceProfile.setAreaTrafficCapDL(areaTrafficCapDL);

        return aaiSliceProfile;
    }
}
