/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.beans.nsmf;

import lombok.Data;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.beans.nsmf.oof.TemplateInfo;
import java.io.Serializable;

@Data
public class SliceTaskInfo<T> implements Serializable {
    private static final long serialVersionUID = 7580056468353975320L;

    private String suggestNssiId;

    private String suggestNssiName;

    private String progress;

    private String status;

    private String statusDescription;

    private T sliceProfile;

    private TemplateInfo NSSTInfo = new TemplateInfo();

    private String sliceInstanceId;

    private String scriptName;

    private String vendor;

    private NetworkType networkType;

    private SubnetType subnetType;

    private String endPointId;

}
