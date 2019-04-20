package org.onap.so.adapters.catalogdb.catalogrest;

import org.junit.Test;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueryGroupsTest {

    @Test
    public void convertToJson_successful() {
        QueryGroups queryGroups = new QueryGroups(createList());
        System.out.println(queryGroups.JSON2(true, true));
    }

    private List<VFCInstanceGroup> createList() {

        VnfcCustomization vnfcCustomization = new VnfcCustomization();
        vnfcCustomization.setModelCustomizationUUID("test");
        vnfcCustomization.setModelVersion("test");
        vnfcCustomization.setModelInvariantUUID("test");
        vnfcCustomization.setModelName("test");

        VFCInstanceGroup vfcInstanceGroup = new VFCInstanceGroup();
        vfcInstanceGroup.setModelName("test");
        vfcInstanceGroup.setModelUUID("test");
        vfcInstanceGroup.setModelInvariantUUID("test");
        vfcInstanceGroup.setModelVersion("test");

        VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = new VnfcInstanceGroupCustomization();
        vnfcInstanceGroupCustomization.setVnfcCustomizations(Arrays.asList(vnfcCustomization));

        vfcInstanceGroup.setVnfcInstanceGroupCustomizations(Arrays.asList(vnfcInstanceGroupCustomization));
        return Arrays.asList(vfcInstanceGroup);
    }
}
