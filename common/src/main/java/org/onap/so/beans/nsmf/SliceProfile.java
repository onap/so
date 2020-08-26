package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SliceProfile implements Serializable {

    private static final long serialVersionUID = 7924389625656716814L;

    private List<String> snssaiList;

    private String sliceProfileId;

    private List<String> plmnIdList;

    private PerfReq perfReq;

    private Integer maxNumberofUEs;

    private List<String> coverageAreaTAList;

    private Integer latency;

    private String resourceSharingLevel;

}
