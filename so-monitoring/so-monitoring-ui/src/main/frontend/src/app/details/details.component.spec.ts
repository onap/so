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

@authors: ronan.kenny@ericsson.com, waqas.ikram@ericsson.com, andrei.barcovschi@ericsson.com
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
import { Observable, of, Subject, throwError } from 'rxjs';
import { ACTINST } from '../model/activityInstance.model';
import { PDI } from '../model/processDefinition.model';
import { PII } from '../model/processInstance.model';
import { VarInstance } from '../model/variableInstance.model';
import { ActivatedRoute } from '@angular/router';
import { Params } from '@angular/router';
import { tick } from '@angular/core/testing';
import { fakeAsync } from '@angular/core/testing';

// Generate stub for toastr popup notifications
class StubbedToastrNotificationService extends ToastrNotificationService {
  toastrSettings() { }
  info() { }
  error() { }
}

const startActivity: ACTINST = {
  activityId: "StartEvent_1",
  processInstanceId: "processInstanceId-val-1234",
  calledProcessInstanceId: "",
  activityName: "",
  activityType: "",
  durationInMillis: "1",
  endTime: "",
  startTime: ""
};

const subProcessActivity: ACTINST = {
  activityId: "CallActivity_14h26ae",
  processInstanceId: "processInstanceId-val-1234",
  calledProcessInstanceId: "1234",
  activityName: "",
  activityType: "",
  durationInMillis: "1",
  endTime: "",
  startTime: ""
};

const processDefinition: PDI = {
  processDefinitionId: "processDefinitionId-val-1234",
  processDefinitionXml: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_01lwydo\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"2.2.4\">" +
    "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">" +
    "    <bpmn:startEvent id=\"StartEvent_1\">" +
    "      <bpmn:outgoing>SequenceFlow_1rrp6qt</bpmn:outgoing>" +
    "    </bpmn:startEvent>" +
    "    <bpmn:callActivity id=\"CallActivity_14h26ae\" name=\"Sub Process\" calledElement=\"DecomposeService\">" +
    "      <bpmn:incoming>SequenceFlow_1rrp6qt</bpmn:incoming>" +
    "      <bpmn:outgoing>SequenceFlow_0yvdjct</bpmn:outgoing>" +
    "    </bpmn:callActivity>" +
    "    <bpmn:sequenceFlow id=\"SequenceFlow_1rrp6qt\" sourceRef=\"StartEvent_1\" targetRef=\"CallActivity_14h26ae\" />" +
    "    <bpmn:endEvent id=\"EndEvent_039q5o1\">" +
    "      <bpmn:incoming>SequenceFlow_0yvdjct</bpmn:incoming>" +
    "    </bpmn:endEvent>" +
    "    <bpmn:sequenceFlow id=\"SequenceFlow_0yvdjct\" sourceRef=\"CallActivity_14h26ae\" targetRef=\"EndEvent_039q5o1\" />" +
    "  </bpmn:process>" +
    "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">" +
    "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1\">" +
    "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">" +
    "        <dc:Bounds x=\"173\" y=\"102\" width=\"36\" height=\"36\" />" +
    "      </bpmndi:BPMNShape>" +
    "      <bpmndi:BPMNShape id=\"CallActivity_14h26ae_di\" bpmnElement=\"CallActivity_14h26ae\">" +
    "        <dc:Bounds x=\"267\" y=\"80\" width=\"100\" height=\"80\" />" +
    "      </bpmndi:BPMNShape>" +
    "      <bpmndi:BPMNEdge id=\"SequenceFlow_1rrp6qt_di\" bpmnElement=\"SequenceFlow_1rrp6qt\">" +
    "        <di:waypoint x=\"209\" y=\"120\" />" +
    "        <di:waypoint x=\"267\" y=\"120\" />" +
    "      </bpmndi:BPMNEdge>" +
    "      <bpmndi:BPMNShape id=\"EndEvent_039q5o1_di\" bpmnElement=\"EndEvent_039q5o1\">" +
    "        <dc:Bounds x=\"451\" y=\"102\" width=\"36\" height=\"36\" />" +
    "      </bpmndi:BPMNShape>" +
    "      <bpmndi:BPMNEdge id=\"SequenceFlow_0yvdjct_di\" bpmnElement=\"SequenceFlow_0yvdjct\">" +
    "        <di:waypoint x=\"367\" y=\"120\" />" +
    "        <di:waypoint x=\"451\" y=\"120\" />" +
    "      </bpmndi:BPMNEdge>" +
    "    </bpmndi:BPMNPlane>" +
    "  </bpmndi:BPMNDiagram>" +
    "</bpmn:definitions>"
};

const emptyProcessDefinition: PDI = {
  processDefinitionId: "processDefinitionId-val",
  processDefinitionXml: ""
};

const processInstance: PII = {
  processInstancId: "processInstanceId-val-1234",
  processDefinitionId: "1",
  processDefinitionName: "test",
  superProcessInstanceId: "1"
};

const varInstanceObj: VarInstance = {
  name: 'ABC',
  type: 'Object',
  value: '{value: 1234}'
};

const varInstanceStr: VarInstance = {
  name: 'NameStr',
  type: 'String',
  value: 'valOfStr'
};

describe('DetailsComponent', (displayCamundaflow = {}) => {
  // Create SPY Object for Jasmine tests to mock DataService
  let spyDataService: jasmine.SpyObj<DataService>;
  let component: DetailsComponent;
  let fixture: ComponentFixture<DetailsComponent>;

  beforeEach(async(() => {
    spyDataService = jasmine.createSpyObj('DataService', ['getActivityInstance', 'getVariableInstance', 'getProcessDefinition', 'getProcessInstance']);

    TestBed.configureTestingModule({
      providers: [DetailsComponent, HttpClient, HttpTestingController,
        { provide: APP_BASE_HREF, useValue: '/' },
        { provide: ToastrNotificationService, useClass: StubbedToastrNotificationService },
        { provide: DataService, useValue: spyDataService },
        { provide: ActivatedRoute, useValue: { params: of({ id: '1234' }) } }],
      imports: [RouterTestingModule, MatTableModule, HttpClientModule, RouterModule.forRoot([])],
      declarations: [DetailsComponent],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    });
    fixture = TestBed.createComponent(DetailsComponent);
    component = fixture.componentInstance;
  }));

  // Ensure creation of DetailsComponent component
  it('component should be created', inject([DetailsComponent],
    (detailsComponent: DetailsComponent) => {
      expect(detailsComponent).toBeTruthy();
    }));

  // Create a processInstance and ensure it is defined
  it('processInstance should be defined if PII populated', async(() => {
    spyDataService.getActivityInstance.and.returnValue(Promise.resolve([startActivity, subProcessActivity]));
    spyDataService.getProcessDefinition.and.returnValue(of(processDefinition));
    spyDataService.getProcessInstance.and.returnValue(Promise.resolve(processInstance));
    spyDataService.getVariableInstance.and.returnValue(of([varInstanceObj]));
    component.ngOnInit();

    fixture.whenStable().then(() => {
      expect(component.processInstance).toBeDefined();
    });
  }));

  it('should handle bpmnViewer.importXML error', () => {
    spyDataService.getActivityInstance.and.returnValue(Promise.resolve([startActivity, subProcessActivity]));
    spyDataService.getProcessDefinition.and.returnValue(of(emptyProcessDefinition));
    spyDataService.getProcessInstance.and.returnValue(Promise.resolve(processInstance));
    spyDataService.getVariableInstance.and.returnValue(of([varInstanceObj]));
    component.ngOnInit();
  });

  it('should handle error when dataService.getProcessInstance returns an error', () => {
    spyDataService.getVariableInstance.and.returnValue(of([varInstanceObj]));
    spyDataService.getProcessInstance.and.returnValue(Promise.reject(new Error('getProcessInstance Promise should not be resolved')));
    component.ngOnInit();
  });

  it('should handle error when data.getVariableInstance returns an error', () => {
    spyDataService.getActivityInstance.and.returnValue(Promise.resolve([startActivity, subProcessActivity]));
    spyDataService.getProcessDefinition.and.returnValue(of(processDefinition));
    spyDataService.getProcessInstance.and.returnValue(Promise.resolve(processInstance));
    spyDataService.getVariableInstance.and.callFake(() => {
      return throwError(new Error('getVariableInstance error'));
    });
    component.ngOnInit();
  });

  it('should handle error when data.getActivityInstance and data.getProcessDefinition return errors', () => {
    spyDataService.getProcessInstance.and.returnValue(Promise.resolve(processInstance));
    spyDataService.getVariableInstance.and.returnValue(of([varInstanceObj]));
    spyDataService.getProcessDefinition.and.returnValue(of(processDefinition));
    spyDataService.getActivityInstance.and.returnValue(Promise.reject(new Error('getActivityInstance Promise should not be resolved')));
    spyDataService.getProcessDefinition.and.callFake(() => {
      return throwError(new Error('getProcessDefinition error'));
    });
    component.ngOnInit();
  });
});
