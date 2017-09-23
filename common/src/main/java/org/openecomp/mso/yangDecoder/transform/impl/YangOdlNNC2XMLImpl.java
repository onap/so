package org.openecomp.mso.yangDecoder.transform.impl;

import com.google.common.base.Charsets;
import org.opendaylight.netconf.sal.rest.impl.NormalizedNodeXmlBodyWriter;
import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.InstanceIdentifierContext;
import org.opendaylight.netconf.sal.restconf.impl.NormalizedNodeContext;
import org.opendaylight.netconf.sal.restconf.impl.WriterParameters;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class YangOdlNNC2XMLImpl {
   final static MediaType mediaType=getMediaType();
   static    protected MediaType getMediaType() {
       return new MediaType(MediaType.APPLICATION_XML, null);
   }
   static Field requestField=getprettyPrintField();
   
   private static Field getprettyPrintField( ) 
   {
	    Field rf=null;
		try {
			rf = WriterParameters.class.getDeclaredField("prettyPrint");
			  rf.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   return rf;
   }
   public static String getXMLHeader()
   {
	   return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+NormalizedNodePrinter.getEndl();
   }
    public static String toXML(final NormalizedNodeContext normalizedNodeContext) throws Exception
    {
        final NormalizedNodeXmlBodyWriter xmlBodyWriter = new NormalizedNodeXmlBodyWriter();
        final OutputStream output = new ByteArrayOutputStream();
        requestField.set(normalizedNodeContext.getWriterParameters(), true);
        output.write(getXMLHeader().getBytes(Charsets.UTF_8));
        xmlBodyWriter.writeTo(normalizedNodeContext, null, null, null,
        		mediaType, null, output);
        final String outputXML = output.toString();
        return outputXML;
    }
    
    public static NormalizedNodeContext fromXML(String uriPath,final  String xmlpayload,boolean ispost) throws Exception
    {
    	final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(uriPath);
    	InputStream inputStream = new ByteArrayInputStream(xmlpayload.getBytes(Charsets.UTF_8));
    	XmlNormalizedNodeBodyReaderUmeImpl xmlBodyReader = new XmlNormalizedNodeBodyReaderUmeImpl();
    	xmlBodyReader.Set(iicontext, ispost);
//    	final NormalizedNodeContext returnValue = xmlBodyReader.readFrom(iicontext, inputStream);
        final NormalizedNodeContext returnValue = xmlBodyReader.readFrom(null,
                null, null, mediaType, null, inputStream);
        return returnValue; 
    }

}
