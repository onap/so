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

import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { ActivatedRoute, Router } from "@angular/router";
import { Process } from '../model/process.model';
import { ACTINST } from '../model/activityInstance.model';
import { PII } from '../model/processInstance.model';
import { PDI } from '../model/processDefinition.model';
import { CommonModule } from '@angular/common';
import Viewer from 'bpmn-js/lib/NavigatedViewer';
import { ViewEncapsulation } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { VarInstance } from '../model/variableInstance.model';
import { ToastrNotificationService } from '../toastr-notification-service.service';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'app-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.scss'],
  encapsulation: ViewEncapsulation.None
})

export class DetailsComponent implements OnInit {
  bpmnViewer: any;

  processInstanceID: string;

  processDefinitionID: string;

  processDefinitionName: string;

  activityInstance: ACTINST[];

  processInstance: PII;

  processDefinition: PDI;

  variableInstance: VarInstance[];

  displayedColumns = ['activityId', 'activityName', 'activityType', 'startTime', 'endTime', 'durationInMillis'];

  displayedColumnsVariable = ['name', 'type', 'value'];

  constructor(private route: ActivatedRoute, private data: DataService, private popup: ToastrNotificationService,
    private router: Router, private spinner: NgxSpinnerService) { }

  async getActInst(procInstId: string) {
    await this.data.getActivityInstance(procInstId).then(
      (data: ACTINST[]) => {
        this.activityInstance = data;
        console.log(data);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get activity instance details for id: " + procInstId + " Error code:" + error.status);
      });
  }

  async getProcessDefinition(procDefId) {
    await this.data.getProcessDefinition(procDefId).subscribe(
      async (data: PDI) => {
        this.processDefinition = data;
        console.log(data);
        await this.displayCamundaflow(this.processDefinition.processDefinitionXml, this.activityInstance, this.router);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get process definition for id: " + procDefId + " Error code:" + error.status);
      });
  }

  async getProcInstance(procInstId) {
    await this.data.getProcessInstance(procInstId).then(
      async (data: PII) => {
        this.processInstance = data;
        this.processDefinitionID = this.processInstance.processDefinitionId;
        this.processDefinitionName = this.processInstance.processDefinitionName;
        console.log("Process definition id: " + this.processDefinitionID);
        await this.getActInst(this.processInstanceID);
        await this.getProcessDefinition(this.processDefinitionID);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get process instance for id: " + procInstId + " Error code:" + error.status);
      });
  }

  displayCamundaflow(bpmnXml, activities: ACTINST[], r: Router) {
    this.spinner.show();

    this.bpmnViewer.importXML(bpmnXml, (error) => {
      if (error) {
        console.error('Unable to load BPMN flow ', error);
        this.popup.error('Unable to load BPMN flow ');
        this.spinner.hide();
      } else {
        this.spinner.hide();
        let canvas = this.bpmnViewer.get('canvas');
        var eventBus = this.bpmnViewer.get('eventBus');
        eventBus.on('element.click', function(e) {

          activities.forEach(a => {
            if (a.activityId == e.element.id && a.calledProcessInstanceId !== null) {
              console.log("will drill down to : " + a.calledProcessInstanceId);
              r.navigate(['/details/' + a.calledProcessInstanceId]);
              this.spinner.show();
            }
          });
        });
        // zoom to fit full viewport
        canvas.zoom('fit-viewport');
        activities.forEach(a => {
          canvas.addMarker(a.activityId, 'highlight');
        });
      }
    });
  }

  getVarInst(procInstId) {
    this.data.getVariableInstance(procInstId).subscribe(
      (data: VarInstance[]) => {
        this.variableInstance = data;
        console.log(data);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get Variable instances for id: " + procInstId + " Error code:" + error.status);
      });
  }

  async ngOnInit() {
    this.bpmnViewer = new Viewer({
      container: '.canvas'
    });
    this.route.params.subscribe(
      async params => {
        this.processInstanceID = params.id as string;
        console.log("Will GET Process instanc using id: " + this.processInstanceID);
        await this.getProcInstance(this.processInstanceID);

        this.getVarInst(this.processInstanceID);
      });
  }

}
