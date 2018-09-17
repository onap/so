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
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule, MatInputModule } from '@angular/material';
import { SearchData } from '../model/searchData.model';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { FormControl } from '@angular/forms';
import { SearchRequest } from '../model/SearchRequest.model';
import { ElementRef } from '@angular/core';
import { Input } from '@angular/core';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  encapsulation: ViewEncapsulation.None
})

export class HomeComponent implements OnInit {

  totalVal = 0;
  completeVal = 0;
  inProgressVal = 0;
  failedVal = 0;
  pendingVal = 0;
  unlockedVal = 0;
  percentageComplete = 0;
  percentageFailed = 0;
  percentageInProg = 0;
  percentagePending = 0;
  percentageUnlocked = 0;

  options = [{ name: "EQUAL", value: "EQ" }, { name: "NOT EQUAL", value: "NEQ" }, { name: "LIKE", value: "LIKE" }];
  statusOptions = [{ name: "ALL", value: "ALL" }, { name: "COMPLETE", value: "COMPLETE" }, { name: "IN_PROGRESS", value: "IN_PROGRESS" },
  { name: "FAILED", value: "FAILED" }, { name: "PENDING", value: "PENDING" }, { name: "UNLOCKED", value: "UNLOCKED" }];

  hourOptions = ["00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
    "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"];

  minuteOptions = ["00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15",
    "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35",
    "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55",
    "56", "57", "58", "59"];

  processData: Process[];
  searchData: SearchData;

  startingDate: Date;

  displayedColumns = ['requestId', 'serviceInstanceId', 'serviceIstanceName', 'networkId', 'requestStatus', 'serviceType', 'startTime', 'endTime'];

  constructor(private route: ActivatedRoute, private data: DataService,
    private router: Router, private popup: ToastrNotificationService,
    private spinner: NgxSpinnerService) {
    this.searchData = new SearchData();
  }

  makeCall() {
    this.spinner.show();

    var search = this.searchData.getSearchRequest().subscribe((result: SearchRequest) => {

      this.data.retrieveInstance(result.getFilters(), result.getStartTimeInMilliseconds(), result.getEndTimeInMilliseconds())
        .subscribe((data: Process[]) => {
          this.spinner.hide();
          this.processData = data;
          this.popup.info("Number of records found: " + data.length)

          // Calculate Statistics for Service Statistics tab
          this.completeVal = this.processData.filter(i => i.requestStatus === "COMPLETE").length;
          this.inProgressVal = this.processData.filter(i => i.requestStatus === "IN_PROGRESS").length;
          this.failedVal = this.processData.filter(i => i.requestStatus === "FAILED").length;
          this.pendingVal = this.processData.filter(i => i.requestStatus === "PENDING").length;
          this.unlockedVal = this.processData.filter(i => i.requestStatus === "UNLOCKED").length;
          this.totalVal = this.processData.length;

          // Calculate percentages to 2 decimal places and compare to 0 to avoid NaN error
          if (this.totalVal != 0) {
            this.percentageComplete = Math.round(((this.completeVal / this.totalVal) * 100) * 100) / 100;
            this.percentageFailed = Math.round(((this.failedVal / this.totalVal) * 100) * 100) / 100;
            this.percentageInProg = Math.round(((this.inProgressVal / this.totalVal) * 100) * 100) / 100;
            this.percentagePending = Math.round(((this.pendingVal / this.totalVal) * 100) * 100) / 100;
            this.percentageUnlocked = Math.round(((this.unlockedVal / this.totalVal) * 100) * 100) / 100;
          }
          console.log("COMPLETE: " + this.completeVal);
          console.log("FAILED: " + this.failedVal);
        }, error => {
          console.log(error);
          this.popup.error("Unable to perform search Error code:" + error.status);
          this.spinner.hide();
        });
    }, error => {
      console.log("Data validation error " + error);
      this.popup.error(error);
      this.spinner.hide();
    });
  }

  getProcessIsntanceId(requestId: string) {
    this.spinner.show();

    var response = this.data.getProcessInstanceId(requestId).subscribe((data) => {
      if (data.status == 200) {
        this.spinner.hide();
        var processInstanceId = (data.body as ProcessInstanceId).processInstanceId;
        this.router.navigate(['/details/' + processInstanceId]);
      } else {
        this.popup.error('No process instance id found: ' + requestId);
        this.spinner.hide();
        console.log('No process instance id found: ' + requestId);
      }
    });
  }

  ngOnInit() {

  }
}
