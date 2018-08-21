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
import { ToastrNotificationService } from './toastr-notification-service.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
/** Handles HttpClient errors */
export class HttpErrorHandlerService {

  constructor(private popup: ToastrNotificationService) { }

  handleError(operation = 'operation', url = 'url') {
    return (error: HttpErrorResponse) => {
      if (error.error instanceof ErrorEvent) {
        console.error('An error occurred:', error.error.message);
        this.popup.error("An error occurred for operation: " + operation + " using url: " + url + " Detail: " + error.error.message);
        return throwError("An error occurred for operation: " + operation);
      }
      if (error.status == 500 || error.status == 0) {
        this.popup.error("Internal Service Error occured for operation: " + operation + " please check backend service log. status code: " + error.status);
      }
      console.error(
        'Backend returned code ${error.status}, ' +
        'body was: ${error.error}');
      return throwError(error.error || "Internal Service Error occured for operation: " + operation + " please check backend service log. status code: " + error.status);
    };

  }
}
