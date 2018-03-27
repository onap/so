/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.openecomp.mso.db.catalog.test;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.utils.RecordNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class RecordNotFoundExceptionTest {

    @Test
    public void paramConstructor(){
        RecordNotFoundException ex = new RecordNotFoundException("Exceoption raised", new Exception());
        assertNotNull(ex);
        assertNotNull(ex.getMessage());
    }
}
