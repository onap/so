package org.openecomp.mso.client.sniro;

import java.util.LinkedHashMap;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.openecomp.mso.bpmn.core.UrnPropertiesReader;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.sdn.common.BaseClient;
import org.openecomp.mso.client.sniro.beans.ManagerProperties;
import org.openecomp.mso.client.sniro.beans.SniroConductorRequest;
import org.openecomp.mso.client.sniro.beans.SniroManagerRequest;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;


@Component
public class SniroClient {

	private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SniroClient.class);

	@Autowired
	private ManagerProperties managerProperties;

	@Autowired
	private SniroValidator validator;


	/**
	 * Makes a rest call to sniro manager to perform homing and licensing for a
	 * list of demands
	 *
	 * @param homingRequest
	 * @return
	 * @throws JsonProcessingException
	 * @throws BpmnError
	 */
	public void postDemands(SniroManagerRequest homingRequest) throws BadResponseException, JsonProcessingException{
		log.trace("Started Sniro Client Post Demands");
		String url = managerProperties.getHost() + managerProperties.getUri();
		log.debug("Post demands url: " + url);
		log.debug("Post demands payload: " + homingRequest.toJsonString());

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		header.set("Authorization", managerProperties.getHeaders().get("auth"));
		header.set("X-patchVersion", managerProperties.getHeaders().get("patchVersion"));
		header.set("X-minorVersion", managerProperties.getHeaders().get("minorVersion"));
		header.set("X-latestVersion", managerProperties.getHeaders().get("latestVersion"));
		BaseClient<String, LinkedHashMap<?, ?>> baseClient = new BaseClient<>();

		baseClient.setTargetUrl(url);
		baseClient.setHttpHeader(header);

		LinkedHashMap<?, ?> response = baseClient.post(homingRequest.toJsonString());
		validator.validateDemandsResponse(response);
		log.trace("Completed Sniro Client Post Demands");
	}

	/**
	 * Makes a rest call to sniro conductor to notify them of successful or unsuccessful vnf
	 * creation for previously homed resources
	 *
	 * TODO Temporarily being used in groovy therefore can not utilize autowire. Once java "release"
	 * subflow is developed it will be refactored to use autowire.
	 *
	 * @param releaseRequest
	 * @return
	 * @throws BadResponseException
	 */
	public void postRelease(SniroConductorRequest releaseRequest) throws BadResponseException {
		log.trace("Started Sniro Client Post Release");
		String url = UrnPropertiesReader.getVariable("sniro.conductor.host") + UrnPropertiesReader.getVariable("sniro.conductor.uri");
		log.debug("Post release url: " + url);
		log.debug("Post release payload: " + releaseRequest.toJsonString());

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		header.set("Authorization", UrnPropertiesReader.getVariable("sniro.conductor.headers.auth"));
		BaseClient<String, LinkedHashMap<?, ?>> baseClient = new BaseClient<>();

		baseClient.setTargetUrl(url);
		baseClient.setHttpHeader(header);

		LinkedHashMap<?, ?> response = baseClient.post(releaseRequest.toJsonString());
		SniroValidator v = new SniroValidator();
		v.validateReleaseResponse(response);
		log.trace("Completed Sniro Client Post Release");
	}

}
