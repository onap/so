package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AbstractSDNCTask {

    @Autowired
    Environment env;


    public URI buildCallbackURI(SDNCRequest sdncRequest) {
        UriBuilder builder = UriBuilder.fromPath(env.getRequiredProperty("mso.workflow.message.endpoint"))
                .path(sdncRequest.getCorrelationName()).path(sdncRequest.getCorrelationValue());
        return builder.build();
    }
}
