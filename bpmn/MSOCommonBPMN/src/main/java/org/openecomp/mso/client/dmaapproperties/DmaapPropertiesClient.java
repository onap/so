package org.openecomp.mso.client.dmaapproperties;

import javax.inject.Provider;

import org.openecomp.mso.client.avpn.dmaap.beans.AVPNDmaapBean;
import org.openecomp.mso.client.avpn.dmaap.beans.AsyncRequestStatus;
import org.openecomp.mso.client.avpn.dmaap.beans.InstanceReferences;
import org.openecomp.mso.client.avpn.dmaap.beans.RequestStatus;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DmaapPropertiesClient {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DmaapPropertiesClient.class);

	@Autowired
	private Provider<GlobalDmaapPublisher> dmaapPublisher;

	protected AVPNDmaapBean buildRequestJson(String requestId, String clientSource, String correlator, String serviceInstanceId, String startTime, String finishTime,
											 String requestScope, String requestType, String timestamp, String requestState, String statusMessage, String percentProgress, Boolean wasRolledBack) {

		RequestStatus requestStatus = buildRequestStatus(timestamp, requestState, statusMessage, percentProgress, wasRolledBack);

		InstanceReferences instanceReferences = buildInstanceReferences(serviceInstanceId);

		AsyncRequestStatus asyncRequestStatus = buildAsyncRequestStatus(requestId, clientSource, correlator, startTime, finishTime,
				requestScope, requestType, requestStatus, instanceReferences);

		AVPNDmaapBean dmaapBean = new AVPNDmaapBean();
		dmaapBean.setAsyncRequestStatus(asyncRequestStatus);

		return dmaapBean;
	}

	private String jsonToString(AVPNDmaapBean dmaapBean) throws JsonProcessingException, MapperException {
		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dmaapBean);
		} catch (JsonProcessingException e) {
			msoLogger.error(e);
			throw new MapperException(e.getMessage());
		}
	}

	private AsyncRequestStatus buildAsyncRequestStatus(String requestId, String clientSource, String correlator, String startTime,
													   String finishTime, String requestScope, String requestType,
													   RequestStatus requestStatus, InstanceReferences instanceReferences) {

		AsyncRequestStatus asyncRequestStatus = new AsyncRequestStatus();
		asyncRequestStatus.setRequestId(requestId);
		asyncRequestStatus.setClientSource(clientSource);
		asyncRequestStatus.setCorrelator(correlator);
		asyncRequestStatus.setStartTime(startTime);
		asyncRequestStatus.setFinishTime(finishTime);
		asyncRequestStatus.setRequestScope(requestScope);
		asyncRequestStatus.setRequestType(requestType);
		asyncRequestStatus.setInstanceReferences(instanceReferences);
		asyncRequestStatus.setRequestStatus(requestStatus);

		return asyncRequestStatus;
	}

	private InstanceReferences buildInstanceReferences(String serviceInstanceId) {
		InstanceReferences instanceReferences = new InstanceReferences();
		instanceReferences.setServiceInstanceId(serviceInstanceId);
		return instanceReferences;
	}

	private RequestStatus buildRequestStatus(String timestamp, String requestState, String statusMessage,
											 String percentProgress, Boolean wasRolledBack) {
		RequestStatus requestStatus = new RequestStatus();
		requestStatus.setTimestamp(timestamp);
		requestStatus.setRequestState(requestState);
		requestStatus.setStatusMessage(statusMessage);
		requestStatus.setPercentProgress(percentProgress);
		requestStatus.setWasRolledBack(wasRolledBack);
		return requestStatus;
	}

	public void dmaapPublishRequest(String requestId, String clientSource, String correlator, String serviceInstanceId, String startTime,
									String finishTime, String requestScope, String requestType, String timestamp, String requestState,
									String statusMessage, String percentProgress, Boolean wasRolledBack) throws MapperException, JsonProcessingException {

		AVPNDmaapBean bean = this.buildRequestJson(requestId, clientSource, correlator, serviceInstanceId, startTime, finishTime,
				requestScope, requestType, timestamp, requestState, statusMessage, percentProgress, wasRolledBack);

		String request = jsonToString(bean);
		dmaapPublisher.get().send(request);
	}
}
