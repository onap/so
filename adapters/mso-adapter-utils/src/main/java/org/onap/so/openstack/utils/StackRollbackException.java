package org.onap.so.openstack.utils;

import org.onap.so.openstack.exceptions.MsoException;

public class StackRollbackException extends MsoException {

    public StackRollbackException(String error) {
        super(error);
    }

}
