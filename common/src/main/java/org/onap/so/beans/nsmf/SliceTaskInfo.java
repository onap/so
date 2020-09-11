package org.onap.so.beans.nsmf;

import lombok.Data;
import org.onap.so.beans.nsmf.oof.TemplateInfo;

import java.io.Serializable;

@Data
public class SliceTaskInfo<T> implements Serializable {
    private static final long serialVersionUID = 7580056468353975320L;

    private String suggestNssiId;

    private String suggestNssiName;

    private String progress;

    private String status;

    private String statusDescription;

    private T sliceProfile;

    private TemplateInfo NSSTInfo;

    private String serviceInstanceId;

    private String scriptName;

    private String vendor;

    private NetworkType networkType;

}
