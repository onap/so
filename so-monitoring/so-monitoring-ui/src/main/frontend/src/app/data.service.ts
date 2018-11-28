/**
============LICENSE_START=======================================================
 Copyright (C) 2018 Ericsson. All rights reserved.
================================================================================
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
 limitations under the License.

SPDX-License-Identifier: Apache-2.0
============LICENSE_END=========================================================

@authors: ronan.kenny@ericsson.com, waqas.ikram@ericsson.com
*/

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Process } from './model/process.model';
import { catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ProcessInstanceId } from './model/processInstanceId.model';
import { environment } from '../environments/environment';
import { HttpResponse } from '@angular/common/http';
import { PII } from './model/processInstance.model';
import { HttpErrorHandlerService } from './http-error-handler.service';
import { ACTINST } from './model/activityInstance.model';


@Injectable({
  providedIn: 'root'
})
export class DataService {

  constructor(private http: HttpClient, private httpErrorHandlerService: HttpErrorHandlerService) { }

  // HTTP POST call to running Spring Boot application
  retrieveInstance(servInstId: {}, from: number, to: number) {
    var url = environment.soMonitoringBackendURL + 'v1/search?from=' + from + "&to=" + to;
    return this.http.post<Process[]>(url, servInstId)
      .pipe(
        catchError(this.httpErrorHandlerService.handleError("POST", url))
      );
  }

  // HTTP GET to return Process Instance using RequestID
  getProcessInstanceId(requestId): Observable<HttpResponse<ProcessInstanceId>> {
    var url = environment.soMonitoringBackendURL + 'process-instance-id/' + requestId;
    console.log(requestId);
    return this.http.get<ProcessInstanceId>(url, { observe: 'response' })
      .pipe(
        catchError(this.httpErrorHandlerService.handleError("GET", url))
      );
  }

  // HTTP GET to return Activity instancs using ProcessInstanceID
  getActivityInstance(processInstanceId): Promise<ACTINST[]> {
    var url = environment.soMonitoringBackendURL + 'activity-instance/' + processInstanceId;
    return this.http.get<ACTINST[]>(url)
      .pipe(
        catchError(this.httpErrorHandlerService.handleError("GET", url))
      ).toPromise();
  }

  // HTTP GET to return Activity Instance using ProcessInstanceID
  async getProcessInstance(processInstanceId): Promise<PII> {
    var url = environment.soMonitoringBackendURL + 'process-instance/' + processInstanceId;
    return await (this.http.get<PII>(url)
      .pipe(
        catchError(this.httpErrorHandlerService.handleError("GET", url))))
      .toPromise();
  }

  // HTTP GET to return Process Definition using processDefinitionId
  getProcessDefinition(processDefinitionId) {
    var url = environment.soMonitoringBackendURL + 'process-definition/' + processDefinitionId;
    return this.http.get(url).pipe(
      catchError(this.httpErrorHandlerService.handleError("GET", url))
    );
  }

  // HTTP GET to return Variable Instance using ProcessInstanceID
  getVariableInstance(processDefinitionId) {
    var url = environment.soMonitoringBackendURL + 'variable-instance/' + processDefinitionId;
    return this.http.get(url).pipe(
      catchError(this.httpErrorHandlerService.handleError("GET", url))
    );
  }
}
