package org.onap.so.beans.nsmf;


import lombok.Data;
import java.io.Serializable;

@Data
public class NetworkSliceInfo implements Serializable {
    private static final long serialVersionUID = 4401921718259398587L;

    private String snssai;

    private String customer;

    private String serviceType;
}
