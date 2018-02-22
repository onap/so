package org.openecomp.mso.cloud.authentication.wrappers;

import org.openecomp.mso.cloud.CloudIdentity;

final class WrapperTestUtility {

    static final String CLOUD_IDENTITY_MSO_ID = "msoIdTest";
    static final String CLOUD_IDENTITY_MSO_PASS = "msoPassTest";
    static final String EXCEPTION_MESSAGE = "Provided cloud identity is null, cannot extract username and "
            + "password";

    private WrapperTestUtility() {
    }

    static CloudIdentity createCloudIdentity() {
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setMsoId(CLOUD_IDENTITY_MSO_ID);
        cloudIdentity.setMsoPass(CloudIdentity.encryptPassword(CLOUD_IDENTITY_MSO_PASS));
        return cloudIdentity;
    }
}
