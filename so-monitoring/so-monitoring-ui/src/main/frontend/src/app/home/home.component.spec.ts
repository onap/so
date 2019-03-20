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

import { HomeComponent } from './home.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { MatTableModule, MatNativeDateModule, MatTableDataSource, MatPaginatorModule } from '@angular/material';
import { FormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { APP_BASE_HREF } from '@angular/common';
import { ToastrNotificationService } from '../toastr-notification-service.service';
import { environment } from '../../environments/environment.prod';
import { Observable, of, throwError } from 'rxjs';
import { SearchRequest } from '../model/SearchRequest.model';
import { DataService } from '../data.service'; // may be able to remove
import { Process } from '../model/process.model';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DebugElement } from '@angular/core';
import { fakeAsync } from '@angular/core/testing';
import { tick } from '@angular/core/testing';
import { ProcessInstanceId } from '../model/processInstanceId.model';
import { HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { DetailsComponent } from '../details/details.component';

class StubbedToastrNotificationService extends ToastrNotificationService {
  toastrSettings() {}
  info() {}
  error() {}
}

const routes: Routes = [ { path: 'details/114e9ae4-4a32-11e9-8646-d663bd873d93' , component: DetailsComponent} ]

describe('HomeComponent', () => {
  let spyDataService: jasmine.SpyObj<DataService>;
  let router: Router;
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(() => {
    spyDataService = jasmine.createSpyObj('DataService', ['retrieveInstance', 'getProcessInstanceId']);

    TestBed.configureTestingModule({
      providers: [
        { provide: DataService, useValue: spyDataService },
        { provide: APP_BASE_HREF, useValue: '/' },
        { provide: ToastrNotificationService, useClass: StubbedToastrNotificationService }],
      imports: [MatPaginatorModule, BrowserAnimationsModule, MatTableModule, FormsModule, MatDatepickerModule, MatNativeDateModule, HttpClientModule, RouterTestingModule.withRoutes(routes)],
      declarations: [HomeComponent, DetailsComponent],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    });

    fixture = TestBed.createComponent(HomeComponent);
    router = TestBed.get(Router);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Ensure creation of HomeComponent component
  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  // Ensure all statistic variables are initialised to zero
  it('should ensure statistic variables are defaulted at zero', () => {
    expect(component.totalVal === 0 && component.completeVal === 0 &&
      component.inProgressVal === 0 && component.failedVal === 0 &&
      component.pendingVal === 0 && component.unlockedVal === 0 &&
      component.percentageComplete === 0 && component.percentageFailed === 0 &&
      component.percentageInProg === 0 && component.percentagePending === 0 &&
      component.percentageUnlocked === 0).toBeTruthy();
  });

  it('should should navigate to a process if response status is OK', fakeAsync(() => {
      spyDataService.getProcessInstanceId.and.returnValue(of(<HttpResponse<ProcessInstanceId>>{body: {processInstanceId: '114e9ae4-4a32-11e9-8646-d663bd873d93'}, status: 200}));
      spyOn(router, 'navigate');
      component.getProcessIsntanceId('e8a75940-4a32-11e9-8646-d663bd873d93');
      tick();
      expect(router.navigate).toHaveBeenCalledWith(['/details/114e9ae4-4a32-11e9-8646-d663bd873d93']);
    }));

  it('should handle error if no process instance id found', () => {
    spyDataService.getProcessInstanceId.and.returnValue(of(<HttpResponse<ProcessInstanceId>>{body: {processInstanceId: 'getProcessInstanceId error not found'}, status: 404}));
    component.getProcessIsntanceId('e8a75940-4a32-11e9-8646-d663bd873d93');
  });

  it('should handle error when searchData.getSearchRequest returns an error', () => {
      spyOn(component.searchData, 'getSearchRequest').and.callFake(() => {
        return throwError(new Error('getSearchRequest error'));
      });
      component.makeCall();
  });

  it('should handle error when dataService.retrieveInstance returns an error', () => {
      spyOn(component.searchData, 'getSearchRequest').and.returnValue(of(getSearchRequest("965d3c92-44e0-11e9-b210-d663bd873d93", "85a7c354-44e0-11e9-b210-d663bd873d93", undefined, undefined, undefined, undefined, undefined, undefined, "ALL")));
      spyDataService.retrieveInstance.and.callFake(() => {
        return throwError(new Error('retrieveInstance error'));
      });
      component.makeCall();
  });

  it('should calculate statistics correctly', fakeAsync(() => {
    let requestStatusTypes: string[] = ["COMPLETE", "IN_PROGRESS", "FAILED", "PENDING", "UNLOCKED"];
    let processArr: Process[] = [];

    // create 5 processes, one of each requestStatusType, with default time.
    requestStatusTypes.forEach((status) => {
      let serviceName = "service-" + status;
      var process = getProcess("85a7c354-44e0-11e9-b210-d663bd873d93", "965d3c92-44e0-11e9-b210-d663bd873d93", serviceName, "048a6212-44e1-11e9-b210-d663bd873d93", status, "TestType", undefined, undefined);
      processArr.push(process);
    })

      // search request has default filter.
      spyOn(component.searchData, 'getSearchRequest').and.returnValue(of(getSearchRequest(undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined, "ALL")));
      spyDataService.retrieveInstance.and.returnValue(of(processArr));
      component.makeCall();
      tick();
      fixture.detectChanges();

      expect(
        component.totalVal === 5 && component.completeVal === 1 &&
        component.inProgressVal === 1 && component.failedVal === 1 &&
        component.pendingVal === 1 && component.unlockedVal === 1 &&
        component.percentageComplete === 20 &&
        component.percentageFailed === 20 &&
        component.percentageInProg === 20 &&
        component.percentagePending === 20 &&
        component.percentageUnlocked === 20)
        .toBeTruthy();
    }));

  function getSearchRequest(selectedValueSII = "EQ", serviceInstanceIdVal: string, selectedValueRI = "EQ", requestIdVal: string, selectedValueSN = "EQ", serviceInstanceNameVal: string, startTimeInMilliseconds = 0, endTimeInMilliseconds = 4, selectedValueSTATUS?: string): SearchRequest {
    if (startTimeInMilliseconds > endTimeInMilliseconds) {
      console.error("End time cannot be greater than start time.");
      return undefined;
    }
    if (typeof selectedValueSTATUS === "string")
      return new SearchRequest({ serviceInstanceId: [selectedValueSII, serviceInstanceIdVal], requestId: [selectedValueRI, requestIdVal], serviceInstanceName: [selectedValueSN, serviceInstanceNameVal], requestStatus: ["EQ", selectedValueSTATUS] }, startTimeInMilliseconds, endTimeInMilliseconds);
    else
      return new SearchRequest({ serviceInstanceId: [selectedValueSII, serviceInstanceIdVal], requestId: [selectedValueRI, requestIdVal], serviceInstanceName: [selectedValueSN, serviceInstanceNameVal] }, startTimeInMilliseconds, endTimeInMilliseconds);
  }

  function getProcess(requestIdVal: string, serviceInstanceIdVal: string, serviceIstanceNameVal: string, networkIdVal: string, requestStatusVal: string, serviceTypeVal: string, startTimeVal = "1", endTimeVal = "2"): Process {
    return <Process>{ requestId: requestIdVal, serviceInstanceId: serviceInstanceIdVal, serviceIstanceName: serviceIstanceNameVal, networkId: networkIdVal, requestStatus: requestStatusVal, serviceType: serviceTypeVal, startTime: startTimeVal, endTime: endTimeVal };
  }
});
