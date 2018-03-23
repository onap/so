/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.openecomp.mso.cloudify.beans;

import static org.mockito.Mockito.mock;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.mso.cloudify.v3.model.Deployment;
import org.openecomp.mso.cloudify.v3.model.DeploymentOutputs;
import org.openecomp.mso.cloudify.v3.model.Execution;
import org.powermock.api.mockito.PowerMockito;

public class DeploymentInfoTest {
    
    @Mock
    DeploymentStatus status;
    
    @Mock
    DeploymentOutputs out;
    
    @Mock
    Execution execution;
    
    @Mock
    Deployment deployment;

    @Test
    public void test() {
        Deployment deployment=mock(Deployment.class);
        Map<String,Object> dep=new HashMap();
        Map<String,Object> outputs = new HashMap<String,Object>();
        Map<String,Object> inputs = new HashMap<String,Object>();
        inputs.put("id",dep);
        status=DeploymentStatus.CREATED;
        outputs.put("id", out);
        dep.put("id", outputs);
        DeploymentInfo dinfo=new DeploymentInfo(deployment);
        DeploymentInfo dinfi=new DeploymentInfo("id");
        DeploymentInfo din=new DeploymentInfo("id",outputs);
        DeploymentInfo dfo=new DeploymentInfo("id", status);
        DeploymentInfo dfoi=new DeploymentInfo(deployment, out, execution);
        dinfo=PowerMockito.spy(new DeploymentInfo());
        dinfo.setId("id");
        dinfi.setInputs(inputs);
        din.setStatus(status);
        din.setOutputs(outputs); 
        assert(din.toString()!=null);
        assert(din.getOutputs().equals(outputs));
        assert(din.getId().equals("id"));
        assert(din.getStatus().equals(status));
        din.getLastAction();
        din.getErrorMessage();
        din.getActionStatus();
    }

}
