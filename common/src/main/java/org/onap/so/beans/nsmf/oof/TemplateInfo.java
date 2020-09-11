package org.onap.so.beans.nsmf.oof;

import lombok.Data;

import java.io.Serializable;


@Data
public class TemplateInfo implements Serializable {

    private static final long serialVersionUID = 4237411651259839093L;

    private String UUID;

    private String invariantUUID;

    private String name;
}
