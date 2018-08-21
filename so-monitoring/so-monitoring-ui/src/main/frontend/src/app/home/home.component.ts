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

import { ProcessInstanceId } from '../model/processInstanceId.model';
import { ToastrNotificationService } from '../toastr-notification-service.service';
import { MatSelectModule } from '@angular/material/select';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  encapsulation: ViewEncapsulation.None
})

export class HomeComponent implements OnInit {

  public selectedValue = "EQ";

  //{"serviceInstanceId":["EQ","e3b5744d-2ad1-4cdd-8390-c999a38829bc"]}

  // Initialise Array of type User
  processData: Process[];

  // Set column identifiers to display in data tabulation
  displayedColumns = ['requestId', 'serviceInstanceId', 'serviceIstanceName', 'networkId', 'requestStatus', 'serviceType', 'startTime', 'endTime'];

  constructor(private route: ActivatedRoute, private data: DataService,
    private router: Router, private popup: ToastrNotificationService) {
  }

  makeCall(selectedValue, searchValue) {
    console.log(searchValue);
    console.log(selectedValue);

    this.data.retrieveInstance({ "serviceInstanceId": [selectedValue.toString(), searchValue.toString()] })
      .subscribe((data: Process[]) => {
        this.processData = data;
        this.popup.info("Number of records found: " + data.length)
      }, error => {
        console.log(error);
        this.popup.error("Unable to perform search Error code:" + error.status);
      });
  }

  getProcessIsntanceId(requestId: string) {
    var response = this.data.getProcessInstanceId(requestId).subscribe((data) => {
      if (data.status == 200) {
        var processInstanceId = (data.body as ProcessInstanceId).processInstanceId;
        this.router.navigate(['/details/' + processInstanceId]);
      } else {
        this.popup.error('No process instance id found: ' + requestId);
        console.log('No process instance id found: ' + requestId);
      }
    });
  }

  ngOnInit() { }
}
