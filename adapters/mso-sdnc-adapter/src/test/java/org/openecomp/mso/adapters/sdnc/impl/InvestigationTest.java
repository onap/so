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
package org.openecomp.mso.adapters.sdnc.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class InvestigationTest {
	
	private static MsoPropertiesFactory msoPF;
	@BeforeClass
	public static void setUp() throws MsoPropertiesException {
		System.setProperty("mso.config.path", "src/test/resources/");
		msoPF = new MsoPropertiesFactory();
		msoPF.initializeMsoProperties("MSO_PROP_SDNC_ADAPTER", "mso.sdnc.properties");

	}
	@Test
	public void run() throws ParserConfigurationException, IOException, SAXException {
		
		RequestTunables rt = new RequestTunables("reqid","","svc-topology-operation","delete", msoPF);
		rt.setTunables();
		/*Document reqDoc = parse();
		NodeList nodeList = reqDoc.getElementsByTagName("sdncadapterworkflow:SDNCRequestData");
		Node node = null;
		System.out.println("nodeList length: "+ nodeList.getLength());
		for (int i =0; i<nodeList.getLength();i++){
			node = nodeList.item(i);
		}
		
		Document doc = nodeToDocument(node);
		*/
		Scanner scanner = new Scanner( new File("src/test/resources/sdnc_adapter_request.xml") );
		String input = scanner.useDelimiter("\\A").next();
		System.out.println("input: "+input);
		scanner.close();
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(input));
			Document reqDoc = db.parse(is);
			String sdncReqBody = Utils.genSdncReq(reqDoc, rt);
			System.out.println(sdncReqBody);
			String uuid = UUID.randomUUID().toString();
			System.out.println("uuid: "+uuid);
		}catch(Exception ex) {
			throw new IllegalStateException();
		}
		
	}
	public static Document parse() throws ParserConfigurationException, IOException, SAXException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setValidating(true);
	    factory.setIgnoringElementContentWhitespace(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    File file = new File("src/test/resources/sdnc_adapter_request.xml");
	    Document doc = builder.parse(file);
	    return doc;
	}
	public static Document nodeToDocument (Node node) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document newDocument = builder.newDocument();
		Node importedNode = newDocument.importNode(node, true);
		newDocument.appendChild(importedNode);
		return newDocument;
	}
}

