package org.onap.so.adapters.catalogdb.catalogrest;

import org.json.JSONException;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.adapters.catalogdb.rest.CatalogDbAdapterRest;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class QueryServicePnfTest extends QueryServicePnfs {

    @Mock
    private Service service;

    @Mock
    private ServiceRepository serviceRepo;

    private QueryServicePnfs queryServicePnf = new QueryServicePnfs();

    private final String invalidJSON =
            "\"{\\\\\\\"nf_function\\\\\\\":\\\\\\\"DO_STUFF\\\\\\\",\\\"_image_name\\\\\\\":\\\\\\\"test_image\\\"";

    private final String validJSON = "\"{\"nf_function\":\"DO_STUFF\",\"image_name\":\"test_image\"}";

    @Test
    public void test_IsValidJsonTrue() throws JSONException {
        boolean isValidJson = queryServicePnf.isJSONValid(validJSON);
        assertEquals(true, isValidJson);
    }

    @Test
    public void test_IsValidJsonFalse() throws JSONException {
        boolean isValidJson = queryServicePnf.isJSONValid(invalidJSON);
        assertEquals(false, isValidJson);
    }

    @Test
    public void test_IsValidJsonNull() throws JSONException {
        boolean isValidJson = queryServicePnf.isJSONValid(null);
        assertEquals(false, isValidJson);
    }


    @Test
    public void tempTest() {

        CatalogDbAdapterRest obj = new CatalogDbAdapterRest();

        when(serviceRepo.findOneByModelUUID(any())).thenReturn(service);

        obj.serviceResources("3.0", "3ee0849c-2abd-4197-aa46-f0406997ad41", "16ebfc40-cec6-4478-8117-976a296919cb",
                "3ee0849c-2abd-4197-aa46-f0406997ad41", "modelUUID");


    }


}
