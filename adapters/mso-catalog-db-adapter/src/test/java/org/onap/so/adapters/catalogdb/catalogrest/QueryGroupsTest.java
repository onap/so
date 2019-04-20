package org.onap.so.adapters.catalogdb.catalogrest;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.rest.beans.ServiceMacroHolder;
import org.onap.so.jsonpath.JsonPathUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryGroupsTest {

    @Test
    public void convertToJson_successful() {
        QueryGroups queryGroups = new QueryGroups(createList());
        String jsonResult = queryGroups.JSON2(true, false);

        Assertions.assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.groups[0].modelInfo.modelName"))
                .contains("test");
        Assertions
                .assertThat(
                        JsonPathUtil.getInstance().locateResult(jsonResult, "$.groups[0].vnfcs[0].modelInfo.modelName"))
                .contains("test");
    }

    private List<VnfcInstanceGroupCustomization> createList() {

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
        vnfcInstanceGroupCustomization.setInstanceGroup(vfcInstanceGroup);


        vfcInstanceGroup.setVnfcInstanceGroupCustomizations(Arrays.asList(vnfcInstanceGroupCustomization));
        return Arrays.asList(vnfcInstanceGroupCustomization);
    }
}
