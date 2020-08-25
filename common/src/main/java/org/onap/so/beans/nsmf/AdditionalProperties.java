package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AdditionalProperties implements Serializable {

    private static final long serialVersionUID = -4020397418955518175L;

    private SliceProfile sliceProfile;

    private List<EndPoint> endPoints;

    private NsiInfo nsiInfo;

    private String scriptName;

    private ModifyAction modifyAction;
}
