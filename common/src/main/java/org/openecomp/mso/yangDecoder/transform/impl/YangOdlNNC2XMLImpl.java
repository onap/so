/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
