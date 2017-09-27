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
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.InstanceIdentifierContext;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.openecomp.mso.yangDecoder.transform.impl.JsonParserStream.getWrapSchemaNode;

public class YangDataTransformNN2XMLServiceImpl {
    private static final XMLOutputFactory XML_FACTORY;
    private static final DocumentBuilderFactory BUILDERFACTORY;
    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }
    BindingNormalizedNodeSerializer mappingservice;
    SchemaContext schemaContext;
    private static final Logger LOG = LoggerFactory.getLogger(YangDataTransformNN2XMLServiceImpl.class);

    public YangDataTransformNN2XMLServiceImpl(BindingNormalizedNodeSerializer mappingservice, SchemaContext context ) {

        this.schemaContext = context;
        this.mappingservice=mappingservice;
    }

    public String transformNNToString(NormalizedNode nn)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(YangOdlNNC2XMLImpl.getXMLHeader());
        new NormalizedNodeNavigator(new NormalizedNodePrinter(stringBuilder)).navigate(null, nn);
        return stringBuilder.toString();
    }


    public static DataSchemaNode getSchemaNodebyNs(final SchemaContext context, final String ns, final String childNodeName) {
        for (Module module : context.getModules()) {
            if (module.getNamespace().toString().equals(ns)) {
                DataSchemaNode found = findChildNode(module.getChildNodes(), childNodeName);
                Preconditions.checkState(found != null, "Unable to find %s", childNodeName);
                return found;
            }
        }
        throw new IllegalStateException("Unable to find child node " + childNodeName);
    }
    private static DataSchemaNode findChildNode(final Iterable<DataSchemaNode> children, final String name) {
        List<DataNodeContainer> containers = Lists.newArrayList();

        for (DataSchemaNode dataSchemaNode : children) {
            if (dataSchemaNode.getQName().getLocalName().equals(name)) {
                return dataSchemaNode;
            }
            if (dataSchemaNode instanceof DataNodeContainer) {
                containers.add((DataNodeContainer) dataSchemaNode);
            } else if (dataSchemaNode instanceof ChoiceSchemaNode) {
                containers.addAll(((ChoiceSchemaNode) dataSchemaNode).getCases());
            }
        }

        for (DataNodeContainer container : containers) {
            DataSchemaNode retVal = findChildNode(container.getChildNodes(), name);
            if (retVal != null) {
                return retVal;
            }
        }

        return null;
    }
    public NormalizedNode transformNNFromString(String sxml) throws Exception {
        InputStream in_nocode = new ByteArrayInputStream(sxml.getBytes(Charsets.UTF_8));//.getBytes("UTF-8")
         //xml2nn
            final Document docxml = readXmlToDocument(in_nocode);
            Element element0 = docxml.getDocumentElement();
            String localname = element0.getNodeName();
            String ns = element0.getAttribute("xmlns");
           // final ContainerSchemaNode containerNodex = (ContainerSchemaNode)null;
           final DataSchemaNode dsn=getSchemaNodebyNs(schemaContext, ns, localname);
            //cn.getNodeType().getNamespace().toString(), cn.getNodeType().getLocalName());
        DomToNormalizedNodeParserFactory parserFactory =DomToNormalizedNodeParserFactory.getInstance(DomUtils.defaultValueCodecProvider(), schemaContext);
        NormalizedNode parsed = null;
        final List<Element> elements = Collections.singletonList(docxml.getDocumentElement());
        if (dsn instanceof ContainerSchemaNode) {
            parsed= parserFactory.getContainerNodeParser().parse(elements, (ContainerSchemaNode)dsn);
        }else   if (dsn instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) dsn;
            parsed=parserFactory.getMapEntryNodeParser().parse(elements, casted);
        }
        return parsed;

    }



    public NormalizedNode transformNotificationNNfromString(String sxml, final InstanceIdentifierContext<?> iicontext) throws Exception
    {
        SchemaNode schemaNode = getWrapSchemaNode(iicontext.getSchemaNode());
        InputStream in_nocode = new ByteArrayInputStream(sxml.getBytes(Charsets.UTF_8));//.getBytes("UTF-8")
        //xml2nn
        final Document docxml = readXmlToDocument(in_nocode);
        Element element0 = docxml.getDocumentElement();
        String localname = element0.getNodeName();
        String ns = element0.getAttribute("xmlns");
        // final ContainerSchemaNode containerNodex = (ContainerSchemaNode)null;
        //  final DataSchemaNode dsn=(DataSchemaNode)schemaNode;/*getSchemaNodebyNs(schemaContext, ns, localname);*/
        //cn.getNodeType().getNamespace().toString(), cn.getNodeType().getLocalName());
        DomToNormalizedNodeParserFactory parserFactory =DomToNormalizedNodeParserFactory.getInstance(DomUtils.defaultValueCodecProvider(), schemaContext);
        NormalizedNode parsed = null;
        final List<Element> elements = Collections.singletonList(docxml.getDocumentElement());
        if (schemaNode instanceof ContainerSchemaNode) {
            parsed= parserFactory.getContainerNodeParser().parse(elements, (ContainerSchemaNode)schemaNode);
        }else   if (schemaNode instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) schemaNode;
            parsed=parserFactory.getMapEntryNodeParser().parse(elements, casted);
        }
        return parsed;
    }
    public NormalizedNode transformNotificationStringtoNN(String sxml,String notficationName) throws Exception {
        final InstanceIdentifierContext<?> iicontext= ControllerContext.getInstance().toInstanceIdentifier(notficationName);
         return transformNotificationNNfromString(sxml,iicontext);
    }
    public Map.Entry<DataSchemaNode, NormalizedNode>  transformRpcNNEntryfromString(String sxml, final InstanceIdentifierContext<?> iirpccontext) throws Exception{
        InputStream in_nocode = new ByteArrayInputStream(sxml.getBytes(Charsets.UTF_8));//.getBytes("UTF-8")
        //xml2nn
        final Document docxml = readXmlToDocument(in_nocode);
        Element element0 = docxml.getDocumentElement();
        String localname = element0.getNodeName();
        String ns = element0.getAttribute("xmlns");
        DataSchemaNode schemaNode;
        final SchemaNode schemaNode0 = iirpccontext.getSchemaNode();
        boolean isInput = false;
        if (schemaNode0 instanceof RpcDefinition) {
            if (docxml.getDocumentElement().getLocalName().contains("output")) {
                schemaNode = ((RpcDefinition) schemaNode0).getOutput();
                isInput = false;
            } else {
                schemaNode = ((RpcDefinition) schemaNode0).getInput();
                isInput = true;
            }

        } else if (docxml instanceof DataSchemaNode) {
            schemaNode = (DataSchemaNode) docxml;
        } else {
            throw new IllegalStateException("Unknow SchemaNode");
        }

        final List<YangInstanceIdentifier.PathArgument> iiToDataList = new ArrayList<>();
        InstanceIdentifierContext<? extends SchemaNode> outIIContext;
        // final ContainerSchemaNode containerNodex = (ContainerSchemaNode)null;
        //  final DataSchemaNode dsn=(DataSchemaNode)schemaNode;/*getSchemaNodebyNs(schemaContext, ns, localname);*/
        //cn.getNodeType().getNamespace().toString(), cn.getNodeType().getLocalName());
        DomToNormalizedNodeParserFactory parserFactory =DomToNormalizedNodeParserFactory.getInstance(DomUtils.defaultValueCodecProvider(), schemaContext);
        NormalizedNode parsed = null;
        final List<Element> elements = Collections.singletonList(docxml.getDocumentElement());
        if (schemaNode instanceof ContainerSchemaNode) {
            parsed= parserFactory.getContainerNodeParser().parse(elements, (ContainerSchemaNode)schemaNode);
        }else   if (schemaNode instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) schemaNode;
            parsed=parserFactory.getMapEntryNodeParser().parse(elements, casted);
        }
        return new SimpleEntry<DataSchemaNode, NormalizedNode>(schemaNode,parsed);
    }
    public NormalizedNode transformRpcNNfromString(String sxml, final InstanceIdentifierContext<?> iirpccontext) throws Exception{

        InputStream in_nocode = new ByteArrayInputStream(sxml.getBytes(Charsets.UTF_8));//.getBytes("UTF-8")
        //xml2nn
        final Document docxml = readXmlToDocument(in_nocode);
        Element element0 = docxml.getDocumentElement();
        String localname = element0.getNodeName();
        String ns = element0.getAttribute("xmlns");
        DataSchemaNode schemaNode;
        final SchemaNode schemaNode0 = iirpccontext.getSchemaNode();
        boolean isInput = false;
        if (schemaNode0 instanceof RpcDefinition) {
            if (docxml.getDocumentElement().getLocalName().contains("output")) {
                schemaNode = ((RpcDefinition) schemaNode0).getOutput();
                isInput = false;
            } else {
                schemaNode = ((RpcDefinition) schemaNode0).getInput();
                isInput = true;
            }

        } else if (docxml instanceof DataSchemaNode) {
            schemaNode = (DataSchemaNode) docxml;
        } else {
            throw new IllegalStateException("Unknow SchemaNode");
        }

        final List<YangInstanceIdentifier.PathArgument> iiToDataList = new ArrayList<>();
        InstanceIdentifierContext<? extends SchemaNode> outIIContext;
        // final ContainerSchemaNode containerNodex = (ContainerSchemaNode)null;
        //  final DataSchemaNode dsn=(DataSchemaNode)schemaNode;/*getSchemaNodebyNs(schemaContext, ns, localname);*/
        //cn.getNodeType().getNamespace().toString(), cn.getNodeType().getLocalName());
        DomToNormalizedNodeParserFactory parserFactory =DomToNormalizedNodeParserFactory.getInstance(DomUtils.defaultValueCodecProvider(), schemaContext);
        NormalizedNode parsed = null;
        final List<Element> elements = Collections.singletonList(docxml.getDocumentElement());
        if (schemaNode instanceof ContainerSchemaNode) {
            parsed= parserFactory.getContainerNodeParser().parse(elements, (ContainerSchemaNode)schemaNode);
        }else   if (schemaNode instanceof ListSchemaNode) {
            final ListSchemaNode casted = (ListSchemaNode) schemaNode;
            parsed=parserFactory.getMapEntryNodeParser().parse(elements, casted);
        }
        return parsed;
    }
    public NormalizedNode transformRpcNNfromString(String sxml, String rpcName) throws Exception {
        final InstanceIdentifierContext<?> iicontext= ControllerContext.getInstance().toInstanceIdentifier(rpcName);
        return this.transformRpcNNfromString(sxml,iicontext);

    }
    public static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
        final Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }

}
