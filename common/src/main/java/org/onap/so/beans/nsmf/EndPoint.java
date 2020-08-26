package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class EndPoint implements Serializable {

    private static final long serialVersionUID = 2479795890807020491L;

    private String nodeId;

    private Map<String, Object> additionalInfo;
}
