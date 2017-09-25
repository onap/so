/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.bpmn.core.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openecomp.mso.logger.MsoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML transformation methods and other useful functions.
 */
public final class XmlTool {

	private static final Map<String, Integer> ENTITIES = new HashMap<>();
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.BPEL);
	static {
		ENTITIES.put("amp", new Integer(38));
		ENTITIES.put("quot", new Integer(34));
		ENTITIES.put("lt", new Integer(60));
		ENTITIES.put("gt", new Integer(62));
	}

	/**
     * Instantiation is not allowed.
     */
    private XmlTool() {
    }
    
	/**
	 * Normalizes and formats XML.  This method consolidates and moves all namespace
	 * declarations to the root element.  The result will not have an XML prolog or
	 * a trailing newline.
	 * @param xml the XML to normalize
	 * @throws IOException 
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public static String normalize(Object xml) throws IOException, TransformerException,
			ParserConfigurationException, SAXException, XPathExpressionException {
		
		if (xml == null) {
			return null;
		}

		Source xsltSource = new StreamSource(new StringReader(
			readResourceFile("normalize-namespaces.xsl")));

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder db = dbFactory.newDocumentBuilder();
		InputSource source = new InputSource(new StringReader(String.valueOf(xml)));
		Document doc = db.parse(source);

		// Start of code to remove whitespace outside of tags
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.evaluate(
			"//text()[normalize-space()='']", doc, XPathConstants.NODESET);

		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			node.getParentNode().removeChild(node);
		}
		// End of code to remove whitespace outside of tags

		// the factory pattern supports different XSLT processors
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer(xsltSource);

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString().trim();
	}

	/**
	 * Encodes a value so it can be used inside an XML text element.
	 * @param s the string to encode
	 * @return the encoded string
	 */
	public static String encode(Object value) {
		if (value == null) {
			return null;
		}

		String s = String.valueOf(value);
		StringBuilder out = new StringBuilder();
		boolean modified = false;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == '<') {
				out.append("&lt;");
				modified = true;
			} else if (c == '>') {
				out.append("&gt;");
				modified = true;
			} else if (c == '&') {
				out.append("&amp;");
				modified = true;
			} else if (c < 32 || c > 126) {
				out.append("&#" + (int)c + ";");
				modified = true;
			} else {
				out.append(c);
			}
		}

		if (modified) {
			return out.toString();
		} else {
			return s;
		}
	}
	
	/**
	 * Encodes a value so it can be used inside an XML attribute.
	 * @param s the string to encode
	 * @return the encoded string
	 */
	public static String encodeAttr(Object value) {
		if (value == null) {
			return null;
		}

		String s = String.valueOf(value);
		StringBuilder out = new StringBuilder();
		boolean modified = false;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == '<') {
				out.append("&lt;");
				modified = true;
			} else if (c == '>') {
				out.append("&gt;");
				modified = true;
			} else if (c == '"') {
				out.append("&quot;");
				modified = true;
			} else if (c == '&') {
				out.append("&amp;");
				modified = true;
			} else if (c < 32 || c > 126) {
				out.append("&#" + (int)c + ";");
				modified = true;
			} else {
				out.append(c);
			}
		}

		if (modified) {
			return out.toString();
		} else {
			return s;
		}
	}
	
	/**
	 * Decodes XML entities in a string value
	 * @param value a value with embedded XML entities
	 * @return the decoded string
	 */
	public static String decode(Object value) {
		if (value == null) {
			return null;
		}
		
		String s = String.valueOf(value);

		StringBuilder out = new StringBuilder(s.length());
		int ampIndex = s.indexOf("&");
		int lastEnd = 0;

		while (ampIndex >= 0) {
			int nextAmpIndex = s.indexOf("&", ampIndex + 1);
			int nextSemiIndex = s.indexOf(";", ampIndex + 1);
			if (nextSemiIndex != -1 && (nextAmpIndex == -1 || nextSemiIndex < nextAmpIndex)) {
				int code = -1;
				String entity = s.substring(ampIndex + 1, nextSemiIndex);

				try {
					if (entity.startsWith("#")) {
						code = Integer.parseInt(entity.substring(1), 10);
					} else {
						if (ENTITIES.containsKey(entity)) {
							code = ENTITIES.get(entity);
						}
					}
				} catch (NumberFormatException x) {
					// Do nothing
				}

				out.append(s.substring(lastEnd, ampIndex));
				lastEnd = nextSemiIndex + 1;
				if (code >= 0 && code <= 0xffff) {
					out.append((char) code);
				} else {
					out.append("&");
					out.append(entity);
					out.append(";");
				}
			}

			ampIndex = nextAmpIndex;
		}

		out.append(s.substring(lastEnd));
		return out.toString();
	}

	/**
	 * Removes the preamble, if present, from an XML document.
	 * @param xml the XML document
	 * @return a possibly modified document
	 */
	public static String removePreamble(Object xml) {
		if (xml == null) {
			return null;
		}

		return String.valueOf(xml).replaceAll("(<\\?[^<]*\\?>\\s*[\\r\\n]*)?", "");
	}

	/**
	 * Removes namespaces and namespace declarations from an XML document.
	 * @param xml the XML document
	 * @return a possibly modified document
	 */
	public static String removeNamespaces(Object xml) {
		if (xml == null) {
		LOGGER.debug("removeNamespaces input object is null , returning null");
			return null;
		}

		String text = String.valueOf(xml);

		// remove xmlns declaration
		text = text.replaceAll("xmlns.*?(\"|\').*?(\"|\')", "");
		// remove opening tag prefix
		text = text.replaceAll("(<)(\\w+:)(.*?>)", "$1$3");
		// remove closing tags prefix
		text = text.replaceAll("(</)(\\w+:)(.*?>)", "$1$3");
		// remove extra spaces left when xmlns declarations are removed
		text = text.replaceAll("\\s+>", ">");

		return text;
	}


	/**
	 * Reads the specified resource file and return the contents as a string.
	 * @param file Name of the resource file
	 * @return the contents of the resource file as a String
	 * @throws IOException if there is a problem reading the file
	 */
	private static String readResourceFile(String file) throws IOException {

		try (InputStream stream = XmlTool.class.getClassLoader().getResourceAsStream(file);
			 Reader reader = new InputStreamReader(stream, "UTF-8")) {

			StringBuilder out = new StringBuilder();
			char[] buf = new char[1024];
			int n;

			while ((n = reader.read(buf)) >= 0) {
				out.append(buf, 0, n);
			}
			return out.toString();
		} catch (Exception e) {
			LOGGER.debug("Exception at readResourceFile stream: " + e);
			return null;
		}
	}
	
	/**
	 * Parses the XML document String for the first occurrence of the specified element tag.
	 * If found, the value associated with that element tag is replaced with the new value
	 * and a String containing the modified XML document is returned. If the XML passed is
	 * null or the element tag is not found in the document, null will be returned.
	 * @param xml String containing the original XML document.
	 * @param elementTag String containing the tag of the element to be modified.
	 * @param newValue String containing the new value to be used to modify the corresponding element.
	 * @return the contents of the modified XML document as a String or null/empty if the modification failed.
	 * @throws IOException, TransformerException, ParserConfigurationException, SAXException
	 */
	public static Optional<String> modifyElement(String xml, String elementTag, String newValue) throws IOException, TransformerException,
	ParserConfigurationException, SAXException {

		if (xml == null || xml.isEmpty()) {
			// no XML content to be modified, return empty
			return Optional.empty();
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder db = dbFactory.newDocumentBuilder();
		InputSource source = new InputSource(new StringReader(xml));
		Document doc = db.parse(source);
		
		Node modNode = doc.getElementsByTagName(elementTag).item(0);
		if (modNode == null) {
			// did not find the specified element to be modified, return empty
			//System.out.println("Did not find element tag " + elementTag + " in XML");
			return Optional.empty();
		} else {
			modNode.setTextContent(newValue);			
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		// return the modified String representation of the XML
		return Optional.of(writer.toString().trim());
	}
}
