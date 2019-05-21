package org.onap.so.openstack.utils;

import org.onap.so.openstack.exceptions.MsoException;

public class StackCreationException extends MsoException {

    public StackCreationException(String error) {
        super(error);
    }

}
