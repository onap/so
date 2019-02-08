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

import { TestBed, inject } from '@angular/core/testing';

import { DataService } from './data.service';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { async } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { ToastrNotificationService } from './toastr-notification-service.service';
import { environment } from '../environments/environment';

class StubbedToastrNotificationService extends ToastrNotificationService {
  toastrSettings() {
  }
}

describe('DataService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataService, { provide: ToastrNotificationService, useClass: StubbedToastrNotificationService }],
      imports: [HttpClientTestingModule]
    });
  });

  // Ensure creation of DataService component
  it('component should be created', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      expect(service).toBeTruthy();
    })));

  // Test retrieveInstance function making POST call
  it('test retrieveInstance POST request', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      service.retrieveInstance({}, 1, 2).subscribe(data => { });
      var url = environment.soMonitoringBackendURL + 'v1/search?from=1&to=2';
      const mockReq = httpClient.expectOne(url);
      expect(mockReq.request.method).toEqual('POST');
      mockReq.flush({});
    })));

  // Test getProcessInstanceId function making GET request to retrieve processInstanceID
  it('test getProcessInstanceId GET request', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      service.getProcessInstanceId("").subscribe(data => { });
      var url = environment.soMonitoringBackendURL + 'process-instance-id/' + "";
      const mockReq = httpClient.expectOne(url);
      expect(mockReq.request.method).toEqual('GET');
      mockReq.flush({});
    })));

  // Test getActivityInstance function making GET request to retrieve activityInstance
  it('test getActivityInstance GET request', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      service.getActivityInstance("").then(data => { });
      var url = environment.soMonitoringBackendURL + 'activity-instance/' + "";
      const mockReq = httpClient.expectOne(url);
      expect(mockReq.request.method).toEqual('GET');
      mockReq.flush({});
    })));

  // Test getProcessInstance function making GET request to retrieve processInstance
  it('test getProcessInstance GET request', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      service.getProcessInstance("");
      var url = environment.soMonitoringBackendURL + 'process-instance/' + "";
      const mockReq = httpClient.expectOne(url);
      expect(mockReq.request.method).toEqual('GET');
    })));

  // Test getProcessDefinition function making GET request to retrieve processDefinition
  it('test getProcessDefinition GET request', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      service.getProcessDefinition("").subscribe(data => { });
      var url = environment.soMonitoringBackendURL + 'process-definition/' + "";
      const mockReq = httpClient.expectOne(url);
      expect(mockReq.request.method).toEqual('GET');
      mockReq.flush({});
    })));

  // Test getVariableInstance function making GET request to retrieve variableInstance
  it('test getVariableInstance GET request', async(inject([HttpTestingController, DataService, ToastrNotificationService],
    (httpClient: HttpTestingController, service: DataService, toastr: ToastrNotificationService) => {
      service.getVariableInstance("").subscribe(data => { });
      var url = environment.soMonitoringBackendURL + 'variable-instance/' + "";
      const mockReq = httpClient.expectOne(url);
      expect(mockReq.request.method).toEqual('GET');
      mockReq.flush({});
    })));
});
