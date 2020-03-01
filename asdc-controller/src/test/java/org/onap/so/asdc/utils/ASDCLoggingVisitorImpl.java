/// *-
// * ============LICENSE_START=======================================================
// * ONAP - SO
// * ================================================================================
// * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
// * ================================================================================
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * ============LICENSE_END=========================================================
// */
//
// package org.onap.so.asdc.utils;
//
// import java.util.ArrayDeque;
// import java.util.Deque;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ArrayNode;
// import com.fasterxml.jackson.databind.node.ContainerNode;
// import com.fasterxml.jackson.databind.node.ObjectNode;
// import com.google.common.base.CaseFormat;
//
//
// public class ASDCLoggingVisitorImpl extends ASDCLoggingBaseVisitor<ContainerNode> {
//
// private final ObjectMapper mapper = new ObjectMapper();
// private ObjectNode doc;
// private Deque<ContainerNode> nodeStore = new ArrayDeque<>();
//
// @Override
// public ContainerNode visitDoc(ASDCLoggingParser.DocContext ctx) {
// doc = mapper.createObjectNode();
// nodeStore.addFirst(doc);
// this.visitChildren(ctx);
// return doc;
// }
//
// @Override
// public ContainerNode visitValue(ASDCLoggingParser.ValueContext ctx) {
//
// return this.visitChildren(ctx);
//
// }
//
// @Override
// public ContainerNode visitSimplePair(ASDCLoggingParser.SimplePairContext ctx) {
// ObjectNode node = mapper.createObjectNode();
// ((ObjectNode) nodeStore.peekFirst()).put(this.toLowerCamel(ctx.key().getText()), ctx.keyValue().getText());
//
// return node;
// }
//
// @Override
// public ContainerNode visitComplexPair(ASDCLoggingParser.ComplexPairContext ctx) {
// ContainerNode container = nodeStore.peekFirst();
// if (container.isArray()) {
// ArrayNode array = (ArrayNode) container;
// ObjectNode node = mapper.createObjectNode();
// array.add(node);
// nodeStore.addFirst(node);
// } else {
// nodeStore.addFirst(((ObjectNode) nodeStore.peekFirst()).putObject(this.toLowerCamel(ctx.key().getText())));
// }
// this.visitChildren(ctx);
// return nodeStore.removeFirst();
//
//
// }
//
// @Override
// public ContainerNode visitList(ASDCLoggingParser.ListContext ctx) {
// nodeStore.addFirst(((ObjectNode) nodeStore.peekFirst()).putArray(this.keyMapper(ctx.listName().getText())));
// this.visitChildren(ctx);
// return nodeStore.removeFirst();
// }
//
// private String keyMapper(String key) {
// if ("Service Artifacts List".equals(key)) {
// return "serviceArtifacts";
// } else if ("Resource Instances List".equals(key)) {
// return "resources";
// } else if ("Resource Artifacts List".equals(key)) {
// return "artifacts";
// } else {
// return key;
// }
// }
//
// private String toLowerCamel(String key) {
// String result = key.replaceAll("\\s", "");
// if ("ServiceArtifactsInfo".equals(result)) {
// return "artifactInfo";
// }
// return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, result);
// }
// }
