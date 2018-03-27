package org.openecomp.mso.openstack.exceptions;

public class MsoAdapterExceptionTest {
    MsoAdapterException msoAdapterException = new MsoAdapterException("test");
    MsoAdapterException msoAdapterExceptionThr = new MsoAdapterException("test" , new Throwable());
}
