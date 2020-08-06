package org.onap.so.adapters.cnf.model;

import java.util.Map;

public class ConnectivityRecords {

    /*
     * private String connectivityRecordName; private String fQDNOrIP; private String caCertToVerifyServer; private
     * String sslInitiator; private String userName; private String password; private String privateKey; private String
     * certToPresent;
     */

    private Map<String, String> records;

    public Map<String, String> getRecords() {
        return records;
    }

    public void setRecords(Map<String, String> records) {
        this.records = records;
    }

}
