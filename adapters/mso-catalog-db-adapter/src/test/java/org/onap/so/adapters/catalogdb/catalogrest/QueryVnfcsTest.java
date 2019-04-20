package org.onap.so.adapters.catalogdb.catalogrest;

import org.junit.Test;
import org.onap.so.db.catalog.beans.VnfcCustomization;

import java.util.ArrayList;
import java.util.List;

public class QueryVnfcsTest {

    @Test
    public void convertToJson_successful() {
        QueryVnfcs queryVnfcs = new QueryVnfcs(createList());
        String jsonResult = queryVnfcs.JSON2(true, false);
        System.out.println(jsonResult);
    }

    private List<VnfcCustomization> createList() {
        List<VnfcCustomization> customizations = new ArrayList();

        VnfcCustomization c1 = new VnfcCustomization();
        c1.setModelName("model1");
        c1.setModelUUID("uuid1");
        c1.setModelInvariantUUID("inv1");
        c1.setModelVersion("v1");
        c1.setModelCustomizationUUID("cust1");

        VnfcCustomization c2 = new VnfcCustomization();
        c2.setModelName("model2");
        c2.setModelUUID("uuid2");
        c2.setModelInvariantUUID("inv2");
        c2.setModelVersion("v2");
        c2.setModelCustomizationUUID("cust2");

        customizations.add(c1);
        customizations.add(c2);
        return customizations;
    }
}
