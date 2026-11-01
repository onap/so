package org.onap.so.adapters.sdnc;

import java.io.StringReader;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.onap.so.adapters.sdnc.impl.SDNCRestClient;
import org.onap.so.adapters.sdncrest.SdncRestRequest;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@RestController
public class SdncHttpController {
    private final SDNCRestClient worker;

    public SdncHttpController(SDNCRestClient worker) {
        this.worker = worker;
    }

    @PostMapping(path = "/adapters/rest/v2/sdnc/requests", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> submit(@RequestBody SdncRestRequest request) {
        if (!StringUtils.hasText(request.getRequestId()) || !StringUtils.hasText(request.getCallbackUrl())
                || !StringUtils.hasText(request.getSvcAction()) || !StringUtils.hasText(request.getSvcOperation())
                || !StringUtils.hasText(request.getRequestDataXml())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid SDNC request");
        }
        Document document;
        try {
            URI callback = URI.create(request.getCallbackUrl());
            if (!("http".equals(callback.getScheme()) || "https".equals(callback.getScheme()))
                    || callback.getHost() == null || callback.getUserInfo() != null || callback.getFragment() != null) {
                throw new IllegalArgumentException("Invalid callback URL");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            document = builder.parse(new InputSource(new StringReader(request.getRequestDataXml())));
            String root = document.getDocumentElement().getLocalName();
            if (!("RequestData".equals(root) || "request-data".equals(root))
                    || document.getElementsByTagNameNS("*", "Envelope").getLength() != 0) {
                throw new IllegalArgumentException("Invalid RequestData document");
            }
        } catch (ParserConfigurationException failure) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SDNC request validation failed");
        } catch (IllegalArgumentException | SAXException | IOException failure) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid SDNC request");
        }
        RequestHeader header = new RequestHeader();
        header.setRequestId(request.getRequestId());
        header.setSvcInstanceId(request.getServiceInstanceId());
        header.setMsoAction(request.getMsoAction());
        header.setSvcAction(request.getSvcAction());
        header.setSvcOperation(request.getSvcOperation());
        header.setCallbackUrl(request.getCallbackUrl());
        SDNCAdapterRequest converted = new SDNCAdapterRequest();
        converted.setRequestHeader(header);
        converted.setRequestData(document.getDocumentElement());
        try {
            worker.executeRestRequest(converted);
        } catch (TaskRejectedException failure) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.singletonMap("error", "SDNC request dispatch unavailable"));
        }
        return ResponseEntity.accepted().body(Collections.singletonMap("requestId", request.getRequestId()));
    }
}
