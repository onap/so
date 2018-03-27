package org.openecomp.mso.openstack.exceptions;

public class MsoCloudIdentityNotFoundTest {
    MsoCloudIdentityNotFound msoCloudIdentityNotFound = new MsoCloudIdentityNotFound();
    MsoCloudIdentityNotFound msoCloudIdentityNotFoundStr = new MsoCloudIdentityNotFound("test");
    public String str = msoCloudIdentityNotFound.toString();
}
