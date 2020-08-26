package org.onap.so.adapters.nssmf.entity;

import lombok.Data;
import org.onap.so.adapters.nssmf.enums.HttpMethod;

@Data
public class NssmfUrlInfo {

    private String url;

    private HttpMethod httpMethod;

    public NssmfUrlInfo(String url, HttpMethod httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }
}
