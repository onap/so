package org.onap.so.adapters.cnf.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class OtherConnectivityListEntity {

    @JsonProperty(value = "connectivity-records")
    private List<Map<String, String>> connectivityRecordsList;

    public List<Map<String, String>> getConnectivityRecordsList() {
        return connectivityRecordsList;
    }

    public void setConnectivityRecordsList(List<Map<String, String>> connectivityRecordsList) {
        this.connectivityRecordsList = connectivityRecordsList;
    }


}
