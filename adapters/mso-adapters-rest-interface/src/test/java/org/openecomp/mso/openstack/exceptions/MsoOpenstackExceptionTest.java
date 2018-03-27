package org.openecomp.mso.openstack.exceptions;

public class MsoOpenstackExceptionTest {
    MsoOpenstackException msoOpenstackException= new MsoOpenstackException(404,"test","test");
    MsoOpenstackException msoOpenstackExceptionEx= new MsoOpenstackException(404,"test","test",new Exception());
    public String str = msoOpenstackException.toString();

}
