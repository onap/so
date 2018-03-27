package org.openecomp.mso.openstack.exceptions;

public class MsoIOExceptionTest {
    MsoIOException msoIOException = new MsoIOException("test");
    MsoIOException msoIOExceptionTh = new MsoIOException("test" , new Throwable());
    public String str = msoIOException.toString();
}
