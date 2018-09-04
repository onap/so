package org.onap.so.logging.cxf.interceptor;


import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;



public class SOAPLoggingOutInterceptor extends AbstractSoapInterceptor{

    private static final String _500 = "500";

    protected static Logger logger = LoggerFactory.getLogger(SOAPLoggingOutInterceptor.class);
    

    
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
