package org.onap.so.logging.cxf.interceptor;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logging.jaxrs.filter.JaxRsFilterLogging;
import org.onap.so.logging.jaxrs.filter.MDCSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SOAPLoggingInInterceptor extends AbstractSoapInterceptor{

    protected static Logger logger = LoggerFactory.getLogger(SOAPLoggingInInterceptor.class);
    
    @Autowired
    MDCSetup mdcSetup;
    
    public SOAPLoggingInInterceptor() {
        super(Phase.READ);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
  
            Map<String, List<String>> headers  = (Map<String,List<String>>) message.get(Message.PROTOCOL_HEADERS);
            HttpServletRequest request = (HttpServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
            request.getRemoteAddr();
            
            setRequestId(headers);
            setInvocationId(headers);
            setServiceName(message);
            setMDCPartnerName(headers);
            mdcSetup.setServerFQDN();
            mdcSetup.setClientIPAddress(request);
            mdcSetup.setInstanceUUID();
            mdcSetup.setEntryTimeStamp();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, "INPROGRESS");
            logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        } catch (Exception e) {
            logger.warn("Error in incoming SOAP Message Inteceptor", e);
        }
    }
 
    private void setServiceName(SoapMessage message) {
        String requestURI = (String) message.get(Message.REQUEST_URI);
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, requestURI);
    }

    //CXF Appears to flatten headers to lower case
    private void setMDCPartnerName(Map<String, List<String>> headers){
        String partnerName=getValueOrDefault(headers, ONAPLogConstants.Headers.PARTNER_NAME.toLowerCase(),"");
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME,partnerName);
    }

    private void setInvocationId(Map<String, List<String>> headers) {
        String invocationId=getValueOrDefault(headers, ONAPLogConstants.Headers.INVOCATION_ID.toLowerCase(),UUID.randomUUID().toString());
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    private void setRequestId(Map<String, List<String>> headers) {
        String requestId=getValueOrDefault(headers, ONAPLogConstants.Headers.REQUEST_ID.toLowerCase(),UUID.randomUUID().toString());
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,requestId);
    }

    private String getValueOrDefault(Map<String, List<String>> headers, String headerName, String defaultValue){
        String headerValue;
        List<String> headerList=headers.get(headerName);
        if(headerList != null && !headerList.isEmpty()){
            headerValue= headerList.get(0);
            if(headerValue == null || headerValue.isEmpty())
                headerValue = defaultValue;
        }else
            headerValue = defaultValue;
        return headerValue;
    }

}
