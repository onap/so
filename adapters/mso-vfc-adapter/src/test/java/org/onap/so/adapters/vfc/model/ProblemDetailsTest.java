/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProblemDetailsTest {
    ProblemDetails problemDetails = new ProblemDetails();

    @Test
    public void getType() {
        problemDetails.getType();
    }

    @Test
    public void setType() {
        problemDetails.setType("Dummy Type");
    }

    @Test
    public void getTitle() {
        problemDetails.getTitle();
    }

    @Test
    public void setTitle() {
        problemDetails.setTitle("Dummy Title");
    }

    @Test
    public void getStatus() {
        problemDetails.getStatus();
    }

    @Test
    public void setStatus() {
        problemDetails.setStatus(200);
    }

    @Test
    public void getDetail() {
        problemDetails.getDetail();
    }

    @Test
    public void setDetail() {
        problemDetails.setTitle("Dummy Title");
    }

    @Test
    public void getInstance() {
        problemDetails.getInstance();
    }

    @Test
    public void setInstance() {
        problemDetails.setInstance("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }
}
