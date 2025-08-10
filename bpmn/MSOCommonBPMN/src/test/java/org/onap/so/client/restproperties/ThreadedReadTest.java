/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.restproperties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.aaiclient.client.aai.AAIProperties;

public class ThreadedReadTest {
    @Test
    public void allAtOnce() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        Callable<AAIProperties> callableTask = () -> {
            return RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
        };
        List<Callable<AAIProperties>> callableTasks = new ArrayList<>();

        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);

        List<Future<AAIProperties>> futures = executorService.invokeAll(callableTasks);

        Set<AAIProperties> results = new HashSet<>();
        futures.forEach(item -> {
            try {
                results.add(item.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        assertThat("expect all unique results", results.size(), equalTo(callableTasks.size()));

    }

    @Test
    public void executeOverTime() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

        Callable<AAIProperties> callableTask = () -> {
            TimeUnit.MILLISECONDS.sleep(500);
            return RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
        };
        List<Callable<AAIProperties>> callableTasks = new ArrayList<>();

        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);

        Set<AAIProperties> results = new HashSet<>();
        callableTasks.forEach(callable -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
                Future<AAIProperties> result = executorService.submit(callable);
                results.add(result.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        assertThat("expect all unique results", results.size(), equalTo(callableTasks.size()));
    }

}
