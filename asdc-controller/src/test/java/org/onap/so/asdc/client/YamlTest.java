/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.client;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onap.so.asdc.util.YamlEditor;
import org.onap.so.db.catalog.beans.HeatTemplateParam;


public class YamlTest {
    @Test
    public void getYamlResourceTypeTestList() throws Exception {

        InputStream input = new FileInputStream(new File("src/test/resources/resource-examples/simpleTest.yaml"));
        YamlEditor decoder = new YamlEditor(IOUtils.toByteArray(input));
        List<String> typeList = decoder.getYamlNestedFileResourceTypeList();

        assertTrue(typeList.size() == 1 && typeList.get(0).equals("file:///my_test.yaml"));
    }

    @Test
    public void getParameterListTest() throws Exception {

        InputStream input = new FileInputStream(new File("src/test/resources/resource-examples/simpleTest.yaml"));
        YamlEditor decoder = new YamlEditor(IOUtils.toByteArray(input));
        Set<HeatTemplateParam> paramSet = decoder.getParameterList("123456");

        assertTrue(paramSet.size() == 5);

        for (HeatTemplateParam param : paramSet) {
            if ("ip_port_snmp_manager".equals(param.getParamName())
                    || "cor_direct_net_name".equals(param.getParamName())
                    || "cor_direct_net_RT".equals(param.getParamName())) {

                assertTrue(param.isRequired() == false);
            } else {

                assertTrue(param.isRequired() == true);
            }

            assertTrue("string".equals(param.getParamType()));
        }
    }

    @Test
    public void addParameterListWhenEmptyTest() throws Exception {

        InputStream input =
                new FileInputStream(new File("src/test/resources/resource-examples/simpleTestWithoutParam.yaml"));
        YamlEditor decoder = new YamlEditor(IOUtils.toByteArray(input));

        Set<HeatTemplateParam> newParamSet = new HashSet<>();

        HeatTemplateParam heatParam1 = new HeatTemplateParam();
        heatParam1.setHeatTemplateArtifactUuid("1");
        heatParam1.setParamName("testos1");
        heatParam1.setParamType("string");

        HeatTemplateParam heatParam2 = new HeatTemplateParam();
        heatParam2.setHeatTemplateArtifactUuid("2");
        heatParam2.setParamName("testos2");
        heatParam2.setParamType("number");

        newParamSet.add(heatParam1);
        newParamSet.add(heatParam2);

        decoder.addParameterList(newParamSet);

        Set<HeatTemplateParam> paramSet = decoder.getParameterList("123456");
        assertTrue(paramSet.size() == 2);

        assertTrue(decoder.encode().contains("testos1"));
        assertTrue(decoder.encode().contains("string"));
        assertTrue(decoder.encode().contains("testos2"));
        assertTrue(decoder.encode().contains("number"));
    }

    @Test
    public void addParameterListTest() throws Exception {

        InputStream input = new FileInputStream(new File("src/test/resources/resource-examples/simpleTest.yaml"));
        YamlEditor decoder = new YamlEditor(IOUtils.toByteArray(input));

        Set<HeatTemplateParam> newParamSet = new HashSet<>();

        HeatTemplateParam heatParam1 = new HeatTemplateParam();
        heatParam1.setHeatTemplateArtifactUuid("1");
        heatParam1.setParamName("testos1");
        heatParam1.setParamType("string");

        HeatTemplateParam heatParam2 = new HeatTemplateParam();
        heatParam2.setHeatTemplateArtifactUuid("2");
        heatParam2.setParamName("testos2");
        heatParam2.setParamType("number");

        newParamSet.add(heatParam1);
        newParamSet.add(heatParam2);

        decoder.addParameterList(newParamSet);

        Set<HeatTemplateParam> paramSet = decoder.getParameterList("123456");

        assertTrue(paramSet.size() == 7);

        Boolean check1 = Boolean.FALSE;
        Boolean check2 = Boolean.FALSE;

        for (HeatTemplateParam param : paramSet) {
            if ("ip_port_snmp_manager".equals(param.getParamName())
                    || "cor_direct_net_name".equals(param.getParamName())
                    || "cor_direct_net_RT".equals(param.getParamName())) {
                assertFalse(param.isRequired());
            } else {
                assertTrue(param.isRequired());
            }

            if ("testos1".equals(param.getParamName()) && "string".equals(param.getParamType())) {
                check1 = Boolean.TRUE;
            }

            if ("testos2".equals(param.getParamName()) && "number".equals(param.getParamType())) {
                check2 = Boolean.TRUE;
            }

        }

        assertTrue(check1);
        assertTrue(check2);
    }
}
