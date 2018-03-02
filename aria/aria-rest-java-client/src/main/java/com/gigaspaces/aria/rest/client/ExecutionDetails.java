/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2017 Cloudify.co.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END====================================================
*/
package com.gigaspaces.aria.rest.client;

import java.util.List;

/**
 * Created by DeWayne on 7/12/2017.
 */
public class ExecutionDetails {
    private String executor="";  //default
    private int task_max_attempts=30;
    private int task_retry_interval=30;
    private List<Input> inputs=null;
    private boolean retry_failed_tasks=false;

    public ExecutionDetails(){}

    public ExecutionDetails(String executor){
        this.executor=executor;
    }

    public ExecutionDetails(String executor, int task_max_attempts, int task_retry_interval, boolean retry_failed_tasks,
                            List<Input> inputs){
        this.executor=executor;
        this.task_max_attempts=task_max_attempts;
        this.task_retry_interval=task_retry_interval;
        this.retry_failed_tasks = retry_failed_tasks;
        this.inputs=inputs;
    }
    public String getExecutor(){
        return executor;
    }
    public void setExecutor(String executor){
        this.executor=executor;
    }
    public int getTaskMaxAttempts(){
        return task_max_attempts;
    }
    public void setTaskMaxAttempts(int max){
        this.task_max_attempts=max;
    }
    public int getTaskRetryInterval(){
        return task_retry_interval;
    }
    public void setTaskRetryInterval(int interval){
        this.task_retry_interval=interval;
    }
    public List<Input> getInputs(){
        return inputs;
    }
    public void setInputs(List<Input> inputs){
        this.inputs=inputs;
    }
    public boolean isRetry_failed_tasks() {return retry_failed_tasks;}
    public void setRetry_failed_tasks(boolean retry_failed_tasks) {this.retry_failed_tasks = retry_failed_tasks;}

}
