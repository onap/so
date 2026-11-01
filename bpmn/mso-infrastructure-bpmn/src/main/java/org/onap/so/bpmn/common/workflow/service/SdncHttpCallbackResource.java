package org.onap.so.bpmn.common.workflow.service;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.io.IOException;
import java.util.Collections;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.onap.so.bpmn.common.adapter.sdnc.CallbackHeader;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackResult;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackSuccess;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@Component
@Path("/sdnc/callbacks/v1")
public class SdncHttpCallbackResource {
    private final CallbackHandlerService callback;

    public SdncHttpCallbackResource(CallbackHandlerService callback) {
        this.callback = callback;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deliver(SdncRestCallback request) {
        if (request == null || !StringUtils.hasText(request.getRequestId())
                || !StringUtils.hasText(request.getResponseCode())) {
            return Response.status(400).entity(Collections.singletonMap("error", "Invalid SDNC callback")).build();
        }
        String xml = request.getRequestDataXml();
        if (xml != null && !xml.isEmpty()) {
            try {
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
                var document = builder.parse(new InputSource(new StringReader(xml)));
                if (document.getElementsByTagNameNS("*", "Envelope").getLength() != 0) {
                    return Response.status(400).entity(Collections.singletonMap("error", "Invalid SDNC callback"))
                            .build();
                }
            } catch (ParserConfigurationException failure) {
                return Response.status(500).entity(Collections.singletonMap("error", "SDNC callback validation failed"))
                        .build();
            } catch (SAXException | IOException failure) {
                return Response.status(400).entity(Collections.singletonMap("error", "Invalid SDNC callback")).build();
            }
        }
        CallbackHeader header = new CallbackHeader();
        header.setRequestId(request.getRequestId());
        header.setResponseCode(request.getResponseCode());
        header.setResponseMessage(request.getResponseMessage());
        SDNCAdapterCallbackRequest converted = new SDNCAdapterCallbackRequest();
        converted.setCallbackHeader(header);
        converted.setRequestData(request.getRequestDataXml());
        CallbackResult result = callback.handleCallback("sdncHttpCallback", converted, "sdncAdapterCallbackRequest",
                "sdncAdapterCallbackRequest", "SDNCA_requestId", request.getRequestId(), "[SDNC-HTTP-CALLBACK]", null);
        if (!(result instanceof CallbackSuccess)) {
            return Response.status(500).entity(Collections.singletonMap("error", "SDNC callback correlation failed"))
                    .build();
        }
        return Response.noContent().build();
    }
}
