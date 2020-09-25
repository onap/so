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

import lombok.*;
import org.onap.so.beans.nsmf.oof.TemplateInfo;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SliceTaskParamsAdapter {
    private static final long serialVersionUID = -7785578865170503301L;

    private String serviceId;

    private String serviceName;

    private String nstId;

    private String nstName;

    private Map<String, Object> serviceProfile;

    private String suggestNsiId;

    private String suggestNsiName;

    private TemplateInfo NSTInfo;

    private SliceTaskInfo<TnSliceProfile> tnBHSliceTaskInfo;

    private SliceTaskInfo<TnSliceProfile> tnMHSliceTaskInfo;

    private SliceTaskInfo<TnSliceProfile> tnFHSliceTaskInfo;

    private SliceTaskInfo<CnSliceProfile> cnSliceTaskInfo;

    private SliceTaskInfo<AnSliceProfile> anSliceTaskInfo;

    public String convertToJson() {
        SliceTaskParams sliceTaskParams = new SliceTaskParams();
        sliceTaskParams.setServiceId(serviceId);
        return sliceTaskParams.convertToJson();
    }
}
