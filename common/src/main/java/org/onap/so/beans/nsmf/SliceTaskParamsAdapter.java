package org.onap.so.beans.nsmf;

import lombok.Data;
import org.onap.so.beans.nsmf.oof.TemplateInfo;


@Data
public class SliceTaskParamsAdapter extends SliceTaskParams {
    private static final long serialVersionUID = -7785578865170503301L;

    private String serviceId;

    private String serviceName;

    private String nstId;

    private String nstName;

    private ServiceProfile serviceProfile;

    private String suggestNsiId;

    private String suggestNsiName;

    private TemplateInfo NSTInfo;

    private  SliceTaskInfo<TnSliceProfile> tnBHSliceTaskInfo;

    private  SliceTaskInfo<TnSliceProfile> tnMHSliceTaskInfo;

    private  SliceTaskInfo<TnSliceProfile> tnFHSliceTaskInfo;

    private SliceTaskInfo<CnSliceProfile> cnSliceTaskInfo;

    private SliceTaskInfo<AnSliceProfile> anSliceTaskInfo;
}
