/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.validation;

import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.OwningEntity;
import org.onap.so.serviceinstancebeans.Project;

public class ProjectOwningEntityValidation implements ValidationRule {
    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        int reqVersion = info.getReqVersion();
        Project project;
        OwningEntity owningEntity;
        String requestScope = info.getRequestScope();
        Actions action = info.getAction();


        project = info.getSir().getRequestDetails().getProject();
        owningEntity = info.getSir().getRequestDetails().getOwningEntity();
        if (reqVersion >= 5 && requestScope.equalsIgnoreCase(ModelType.service.name())
                && action == Action.createInstance || action == Action.assignInstance) {
            if (reqVersion > 5 && owningEntity == null) {
                throw new ValidationException("owningEntity");
            }
            if (owningEntity != null && empty(owningEntity.getOwningEntityId())) {
                throw new ValidationException("owningEntityId");
            }
            if (project != null && empty(project.getProjectName())) {
                throw new ValidationException("projectName");
            }
        }
        info.setProject(project);
        info.setOE(owningEntity);
        return info;
    }
}
