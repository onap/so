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

package org.onap.so.apihandlerinfra.logging;

import java.io.Serializable;

public class AlarmLoggerInfo implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6730979289437576112L;
	private String alarm;
    private int state;
    private String detail;


    private AlarmLoggerInfo(String alarm, int state, String detail){
        this.alarm = alarm;
        this.state = state;
        this.detail = detail;
    }

    public int getState() {
        return state;
    }

    public String getAlarm() {
        return alarm;
    }

    public String getDetail() {
        return detail;
    }

    public static class Builder{
        private String alarm = "";
        private int state;
        private String detail = "";

        public Builder(String alarm, int state, String detail){
            this.alarm = alarm;
            this.state = state;
            this.detail = detail;
        }

        public Builder alarm(String alarm){
            this.alarm = alarm;
            return this;
        }

        public Builder state(int state){
            this.state = state;
            return this;
        }

        public Builder detail(String detail){
            this.detail = detail;
            return this;
        }

        public AlarmLoggerInfo build(){
            return new AlarmLoggerInfo(alarm, state, detail);
        }

    }
}
