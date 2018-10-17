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

import { HomeComponent } from './home.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { MatTableModule } from '@angular/material';
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

class StubbedToastrNotificationService extends ToastrNotificationService {
  toastrSettings() {
  }
}

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [HomeComponent, HttpClient, HttpTestingController,
        { provide: APP_BASE_HREF, useValue: '/' },
        { provide: ToastrNotificationService, useClass: StubbedToastrNotificationService }],
      imports: [MatTableModule, FormsModule, MatDatepickerModule, HttpClientModule, RouterModule.forRoot([])],
      declarations: [HomeComponent],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    })
      .compileComponents();
  }));

  // Ensure creation of HomeComponent component
  it('component should be created', inject([HttpTestingController, HomeComponent],
    (httpClient: HttpTestingController, service: HomeComponent) => {
      expect(service).toBeTruthy();
    }));

  // Ensure all statistic variables are initialised to zero
  it('ensure statistic variables are defaulted at zero', async(inject([HttpTestingController, HomeComponent],
    (httpClient: HttpTestingController, service: HomeComponent) => {
      expect(service.totalVal === 0 && service.completeVal === 0 &&
        service.inProgressVal === 0 && service.failedVal === 0 &&
        service.pendingVal === 0 && service.unlockedVal === 0 &&
        service.percentageComplete === 0 && service.percentageFailed === 0 &&
        service.percentageInProg === 0 && service.percentagePending === 0 &&
        service.percentageUnlocked === 0).toBeTruthy();
    })));
});
