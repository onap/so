import { ToastrNotificationService } from "../toastr-notification-service.service";

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

import { Observable, throwError, of } from 'rxjs';
import { SearchRequest } from "./SearchRequest.model";
import { Input } from "@angular/core";

export class SearchData {

  @Input() selectedValueSII = "EQ";
  @Input() selectedValueRI = "EQ";
  @Input() selectedValueSN = "EQ";
  @Input() selectedValueSTATUS = "ALL";

  private now = Date.now();
  // Minus 1 hour from current time for start date
  @Input() startDate = new Date(this.now - (1 * 60 * 60 * 1000));
  @Input() selectedStartHour = this.getNumberAsString(this.startDate.getHours());
  @Input() selectedStartMinute = this.getNumberAsString(this.startDate.getMinutes());

  @Input() endDate = new Date(this.now);
  @Input() selectedEndHour = this.getNumberAsString(this.endDate.getHours());
  @Input() selectedEndMinute = this.getNumberAsString(this.endDate.getMinutes());


  @Input() serviceInstanceId: string;
  @Input() requestId: string;
  @Input() serviceInstanceName: string;

  private startTimeInMilliseconds: number;
  private endTimeInMilliseconds: number;

  constructor() {
  }

  public getSearchRequest(): Observable<SearchRequest> {
    var searchFields = {};
    if ((!this.startDate || this.startDate === null) || (!this.endDate || this.endDate === null)) {
      console.error("Found either start time or end time null or undefined");
      return throwError("Found end or start date empty, Please enter start and end date");
    }

    this.startDate.setHours(parseInt(this.selectedStartHour));
    this.startDate.setMinutes(parseInt(this.selectedStartMinute));

    this.endDate.setHours(parseInt(this.selectedEndHour));
    this.endDate.setMinutes(parseInt(this.selectedEndMinute));

    this.startTimeInMilliseconds = this.startDate.getTime();
    this.endTimeInMilliseconds = this.endDate.getTime();

    if (this.startTimeInMilliseconds > this.endTimeInMilliseconds) {
      console.error("End time: " + this.endDate + " can not be greater then start time: " + this.startDate);
      return throwError("End time: " + this.endDate + " can not be greater then start time: " + this.startDate);
    }


    if (!this.isEmpty(this.selectedValueSII) && !this.isEmpty(this.serviceInstanceId)) {
      searchFields["serviceInstanceId"] = [this.selectedValueSII, this.serviceInstanceId]
    }
    if (!this.isEmpty(this.selectedValueRI) && !this.isEmpty(this.requestId)) {
      searchFields["requestId"] = [this.selectedValueRI, this.requestId]
    }
    if (!this.isEmpty(this.selectedValueSN) && !this.isEmpty(this.serviceInstanceName)) {
      searchFields["serviceInstanceName"] = [this.selectedValueSN, this.serviceInstanceName]
    }

    if (!this.isEmpty(this.selectedValueSTATUS) && this.selectedValueSTATUS !== "ALL") {
      searchFields["requestStatus"] = ["EQ", this.selectedValueSTATUS]
    }

    return of(new SearchRequest(searchFields, this.startTimeInMilliseconds, this.endTimeInMilliseconds));
  }

  private isEmpty(str) {
    return (!str || 0 === str.length);
  }

  private getNumberAsString(num: number) {
    if (num <= 9) {
      return "0" + num;
    }
    return "" + num;
  }

}
