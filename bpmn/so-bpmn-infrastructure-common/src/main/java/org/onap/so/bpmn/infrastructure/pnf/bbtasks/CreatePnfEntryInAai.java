package org.onap.so.bpmn.infrastructure.pnf.bbtasks;

import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CreatePnfEntryInAai extends PnfBaseDelegate {
    private static final Logger logger = LoggerFactory.getLogger(CreatePnfEntryInAai.class);

    @Override
    public void execute(BuildingBlockExecution execution) throws IOException {
        try {
            org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf = extractPnf(execution);
            String pnfCorrelationId = pnf.getPnfName();
            pnfManagement.createEntry(pnfCorrelationId, preparePnfForAai(pnf));
            logger.debug("AAI entry is created for pnf correlation id: {}, pnf uuid: {}", pnfCorrelationId,
                    pnf.getPnfId());
        } catch (BBObjectNotFoundException e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }
    }

    private Pnf preparePnfForAai(org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf) {
        Pnf pnfAai = new Pnf();
        pnfAai.setPnfId(pnf.getPnfId());
        pnfAai.setPnfName(pnf.getPnfName());
        return pnfAai;
    }

}
