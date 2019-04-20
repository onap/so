package org.onap.so.adapters.catalogdb.catalogrest;

import org.junit.Test;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.rest.beans.ServiceMacroHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryGroupsTest {

    @Test
    public void convertToJson_successful() {
        // QueryGroups queryGroups = new QueryGroups(createList());
        // System.out.println(queryGroups.JSON2(true, true));
    }

    @Test
    public void convertToJsonSer_successful() {
        QueryServiceMacroHolder queryServiceMacroHolder =
                new QueryServiceMacroHolder(new ServiceMacroHolder(createService()));
        System.out.println(queryServiceMacroHolder.JSON2(true, true));
    }

    private Service createService() {
        Service service = new Service();

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        service.setVnfCustomizations(vnfResourceCustomizations);

        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();

        VnfResource vnfResource = new VnfResource();
        vnfResourceCustomization.setVnfResources(vnfResource);

        vnfResourceCustomizations.add(vnfResourceCustomization);


        List<VnfcInstanceGroupCustomization> vnfcInstaceGroupCustomizations = new ArrayList<>();
        VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = new VnfcInstanceGroupCustomization();

        // add vnfcCustomizatoin
        VnfcCustomization vnfcCustomization = new VnfcCustomization();
        vnfcCustomization.setModelName("vnfccustomizatoin");
        vnfcInstanceGroupCustomization.setVnfcCustomizations(Arrays.asList(vnfcCustomization));

        VFCInstanceGroup vfcInstanceGroup = new VFCInstanceGroup();
        vfcInstanceGroup.setModelUUID("test1");
        vfcInstanceGroup.setModelInvariantUUID("test2");
        vfcInstanceGroup.setVnfcInstanceGroupCustomizations(vnfcInstaceGroupCustomizations);
        vnfcInstanceGroupCustomization.setInstanceGroup(vfcInstanceGroup);

        vnfcInstaceGroupCustomizations.add(vnfcInstanceGroupCustomization);

        vnfResourceCustomization.setVnfcInstanceGroupCustomizations(vnfcInstaceGroupCustomizations);
        return service;
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
