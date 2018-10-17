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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailsComponent } from './details.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { MatTableModule } from '@angular/material';
import { inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { APP_BASE_HREF } from '@angular/common';
import { ToastrNotificationService } from '../toastr-notification-service.service';
import { DataService } from '../data.service';
import { Observable, of } from 'rxjs';
import { ACTINST } from '../model/activityInstance.model';
import { PDI } from '../model/processDefinition.model';
import { PII } from '../model/processInstance.model';
import { VarInstance } from '../model/variableInstance.model';

// Generate stub for toastr popup notifications
class StubbedToastrNotificationService extends ToastrNotificationService {
  toastrSettings() {
  }
}

// Create SPY Object for Jasmine tests to mock DataService
let spyDataService: jasmine.SpyObj<DataService>;

describe('DetailsComponent', () => {
  beforeEach(async(() => {
    spyDataService = jasmine.createSpyObj('DataService', ['getActivityInstance', 'getVariableInstance']);

    TestBed.configureTestingModule({
      providers: [DetailsComponent, HttpClient, HttpTestingController,
        { provide: APP_BASE_HREF, useValue: '/' },
        { provide: ToastrNotificationService, useClass: StubbedToastrNotificationService },
        { provide: DataService, useValue: spyDataService }],
      imports: [RouterTestingModule, MatTableModule, HttpClientModule, RouterModule.forRoot([])],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    })
      .compileComponents();
  }));

  // Ensure creation of DetailsComponent component
  it('component should be created', inject([DetailsComponent],
    (detailsComponent: DetailsComponent) => {
      expect(detailsComponent).toBeTruthy();
    }));


  // Mock an activityInstance and ensure array is populated
  it('activityInstance should be defined if data service returns activities', inject([DetailsComponent],
    (detailsComponent: DetailsComponent) => {
      const activity: ACTINST = {
        activityId: "",
        processInstanceId: "",
        calledProcessInstanceId: "",
        activityName: "",
        activityType: "",
        durationInMillis: "1",
        endTime: "",
        startTime: ""
      };
      spyDataService.getActivityInstance.and.returnValue(of([activity]));
      detailsComponent.getActInst("");
      expect(detailsComponent.activityInstance.length).toEqual(1);
    }));


  // Create a processDefinition and ensure it is defined
  it('processDefinition should be defined if PDI populated', inject([DetailsComponent],
    (detailsComponent: DetailsComponent) => {
      const activity: PDI = {
        processDefinitionId: "1",
        processDefinitionXml: ""
      };
      detailsComponent.getProcessDefinition("");
      detailsComponent.processDefinition = activity;
      expect(detailsComponent.processDefinition).toBeDefined();
    }));


  // Create a processInstance and ensure it is defined
  it('processInstance should be defined if PII populated', inject([DetailsComponent],
    (detailsComponent: DetailsComponent) => {
      const testVals: PII = {
        processInstancId: "1",
        processDefinitionId: "1",
        processDefinitionName: "test",
        superProcessInstanceId: "1"
      };
      detailsComponent.getProcInstance("");
      detailsComponent.processInstance = testVals;
      expect(detailsComponent.processInstance).toBeDefined();
    }));


    // displayCamundaflow test
    // TODO

    // Mock an variableInstance and ensure array is populated
    it('variableInstance should be defined if data service returns activities', inject([DetailsComponent],
      (detailsComponent: DetailsComponent) => {
        const activity2: VarInstance = {
          name: "a",
          type: "a",
          value: "1"
        };
        spyDataService.getVariableInstance.and.returnValue(of([activity2]));
        detailsComponent.getVarInst("");
        expect(detailsComponent.variableInstance.length).toEqual(1);
      }));
});
