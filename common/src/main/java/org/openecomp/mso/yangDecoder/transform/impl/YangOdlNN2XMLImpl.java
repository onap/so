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

import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.InstanceIdentifierContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Administrator on 2017/3/21.
 */
public class YangOdlNN2XMLImpl {
    private static final XMLOutputFactory XML_FACTORY;
    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
    }
    SchemaContext schemaContext;
    public YangOdlNN2XMLImpl(SchemaContext schemaContext){
        this.schemaContext=schemaContext;
    }
    //general for contain/listitem/rpc/notification
    public String yangNNtoXML(String uriPath,NormalizedNode nn) throws Exception {
        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(uriPath);
        SchemaNode snode = YangDataTransformJava2NNServiceImpl.findSchemaNode(iicontext, nn.getNodeType().getLocalName());
        String strx = snode.toString();
        SchemaPath path = snode.getPath();
        int it=uriPath.lastIndexOf("/");
        if (strx.contains("container ")&&(it<0)) {
           // path = SchemaPath.create(true);
        }
        String sxml =writeNormalizedNodetoXml(nn,path,schemaContext);
        return sxml;
    }
    public  static String writeNormalizedNodetoXml(final NormalizedNode<?, ?> cn,
                                                   final SchemaPath schemaPath, final SchemaContext schemaContext)
    {

        String serializationResultXMLString = null;
        try {
            //nn2xml
            final Document doc = XmlDocumentUtils.getDocument();
            final DOMResult serializationResult = new DOMResult(doc);
            writeNormalizedNode(cn,serializationResult,schemaPath, schemaContext);
            serializationResultXMLString = toString(serializationResult.getNode());
        } catch (IOException |XMLStreamException e) {
            e.printStackTrace();
        }
        return serializationResultXMLString;
    }
    public static void writeNormalizedNode(final NormalizedNode<?, ?> normalized, final DOMResult result,
                                           final SchemaPath schemaPath, final SchemaContext context) throws IOException, XMLStreamException {
        NormalizedNodeWriter normalizedNodeWriter = null;
        NormalizedNodeStreamWriter normalizedNodeStreamWriter = null;
        XMLStreamWriter writer = null;
        try {
            writer = XML_FACTORY.createXMLStreamWriter(result);
            normalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(writer, context, schemaPath);
            normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(normalizedNodeStreamWriter);

            normalizedNodeWriter.write(normalized);

            normalizedNodeWriter.flush();
        } finally {
            if (normalizedNodeWriter != null) {
                normalizedNodeWriter.close();
            }
            if (normalizedNodeStreamWriter != null) {
                normalizedNodeStreamWriter.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }
    public static String toString(final Node xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }
}
