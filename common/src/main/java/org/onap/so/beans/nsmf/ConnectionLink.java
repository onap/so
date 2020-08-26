package org.onap.so.beans.nsmf;


import java.io.Serializable;


public class ConnectionLink implements Serializable {
    private static final long serialVersionUID = -1834584960407180427L;

    private String transportEndpointA;

    private String transportEndpointB;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTransportEndpointA() {
        return transportEndpointA;
    }

    public void setTransportEndpointA(String transportEndpointA) {
        this.transportEndpointA = transportEndpointA;
    }

    public String getTransportEndpointB() {
        return transportEndpointB;
    }

    public void setTransportEndpointB(String transportEndpointB) {
        this.transportEndpointB = transportEndpointB;
    }
}
