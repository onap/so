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
import org.openecomp.mso.yangDecoder.transform.api.NormalizedNodeVisitor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;


/**
 * Created by 10036837 on 16-7-21.
 */
public class NormalizedNodePrinter implements NormalizedNodeVisitor {
    StringBuilder result;
    private static final String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    public NormalizedNodePrinter(StringBuilder result) {
        this.result = result;
    }
    private final static String endl=System.getProperty("line.separator");
    public final static String getEndl(){
        return endl;
    }
    private static String spaces(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    @Override
    public void visitNode(int level, String parentPath, NormalizedNode<?, ?> normalizedNode, boolean start) {
        if(normalizedNode == null)
        {
            return;
        }
        if(normalizedNode.getNodeType() == null)
        {
            return;
        }

        String localName = normalizedNode.getNodeType().getLocalName();
        if (normalizedNode instanceof LeafNode || normalizedNode instanceof LeafSetEntryNode) {
            if(normalizedNode.getValue() instanceof  byte[]){
                result.append(spaces((level-1) * 4) + "<" + localName + ">" + (normalizedNode.getValue() == null ? "" : base64Encode((byte[]) normalizedNode.getValue())) + "</" + localName + ">"+endl);
            }
            else {
               String svalue=normalizedNode.getValue().toString();
                if(normalizedNode.getValue() instanceof QName){
                    QName qn=(QName)normalizedNode.getValue();
                    svalue= qn.getLocalName();
                }
                result.append(spaces((level - 1) * 4) + "<" + localName + ">" + (normalizedNode.getValue() == null ? "" :svalue) + "</" + localName + ">"+endl);
            }
        } else {
            if (start) {
                if (level == 1) {
                    result.append(spaces((level-1) * 4) + "<" + localName + " xmlns=\"" + normalizedNode.getNodeType().getNamespace() + "\">"+endl);
                } else {
                    result.append(spaces((level-1) * 4) + "<" + localName + ">"+endl);
                }
            } else {
                result.append(spaces((level-1) * 4) + "</" + localName + ">"+endl);
            }
        }
    }
    private String base64Encode(byte[] in) {
        StringBuilder out = new StringBuilder((in.length * 4) / 3);
        int b;
        for (int i = 0; i < in.length; i += 3) {
            b = (in[i] & 0xFC) >> 2;
            out.append(CODES.charAt(b));
            b = (in[i] & 0x03) << 4;
            if (i + 1 < in.length) {
                b |= (in[i + 1] & 0xF0) >> 4;
                out.append(CODES.charAt(b));
                b = (in[i + 1] & 0x0F) << 2;
                if (i + 2 < in.length) {
                    b |= (in[i + 2] & 0xC0) >> 6;
                    out.append(CODES.charAt(b));
                    b = in[i + 2] & 0x3F;
                    out.append(CODES.charAt(b));
                } else {
                    out.append(CODES.charAt(b));
                    out.append('=');
                }
            } else {
                out.append(CODES.charAt(b));
                out.append("==");
            }
        }
        return out.toString();
    }
}