package org.onap.so.heatbridge.decoder;

import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;

public class ResponseDecoderUsingJacksonWithXml extends ResponseDecoderUsingJackson {

  public ResponseDecoderUsingJacksonWithXml(final Class clazz) {
    super(clazz);
    objectMapper.setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector());
  }
}
