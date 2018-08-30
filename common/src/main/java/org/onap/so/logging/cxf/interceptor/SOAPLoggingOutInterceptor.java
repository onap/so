package org.onap.so.logging.cxf.interceptor;


import java.util.Collections;
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
import org.onap.so.exceptions.MSOException;
import org.onap.so.logging.jaxrs.filter.JaxRsFilterLogging;
import org.onap.so.logging.jaxrs.filter.MDCSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SOAPLoggingOutInterceptor extends AbstractSoapInterceptor{

    private static final String _500 = "500";

    protected static Logger logger = LoggerFactory.getLogger(SOAPLoggingOutInterceptor.class);
    
    @Autowired
    MDCSetup mdcSetup;
    
    public SOAPLoggingOutInterceptor() {
        super(Phase.WRITE);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            Exception ex = message.getContent(Exception.class);
            if (ex == null) {
                MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.COMPLETED.toString());
            }else{
                int responseCode = 0;
                responseCode = (int) message.get(Message.RESPONSE_CODE);
                if(responseCode != 0 )
                    MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseCode));
                else
                    MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, _500);
                
                MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.ERROR.toString());
            }
            logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
        } catch (Exception e) {
            logger.warn("Error in incoming SOAP Message Inteceptor", e);
        }
    }
}
