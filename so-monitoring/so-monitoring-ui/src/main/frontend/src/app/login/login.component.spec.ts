/**
 ============LICENSE_START=======================================================
 Copyright (C) 2019 Samsung. All rights reserved.
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

 @authors: k.kazak@samsung.com
 **/

import {async, ComponentFixture, inject, TestBed} from '@angular/core/testing';

import {LoginComponent} from './login.component';
import {AuthenticationService} from "../authentication.service";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterTestingModule} from "@angular/router/testing";
import {ActivatedRoute, Router, RouterModule} from "@angular/router";

describe('LoginComponent', () => {
  // Create SPY Object for Jasmine tests to mock DataService
  let spyDataService: jasmine.SpyObj<AuthenticationService>;
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let router: Router;

  beforeEach(async(() => {
    spyDataService = jasmine.createSpyObj('AuthenticationService', ['login', 'logout']);

    TestBed.configureTestingModule({
      providers: [LoginComponent,
        {provide: AuthenticationService, useValue: spyDataService},
        {provide: ActivatedRoute, useValue: { snapshot: {queryParams: { returnUrl: 'test'}}}}
      ],
      imports: [RouterTestingModule, ReactiveFormsModule, FormsModule, RouterModule.forRoot([])],
      declarations: [LoginComponent],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    });

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.get(Router);
  }));


  it('should create', inject([LoginComponent],
    (component: LoginComponent) => {
      expect(component).toBeTruthy();
    }));

  it('should logout and route to test directory', inject([LoginComponent],
    (component: LoginComponent) => {
      component.ngOnInit();
      expect(component.returnUrl).toBe('test');
    }));

  it('should logout and route to root directory', inject([LoginComponent],
    (component: LoginComponent) => {
      router.initialNavigation();
      component.ngOnInit();
      expect(component.returnUrl).toBe('test');
    }));

  it('should submit without success', inject([LoginComponent],
    (component: LoginComponent) => {
      component.ngOnInit();
      expect(component.loginForm.valid).toBe(false);
      component.onSubmit();
      expect(component.submitted).toBe(true);
    }));

  it('should submit without success', inject([LoginComponent],
    (component: LoginComponent) => {
      component.ngOnInit();
      expect(component.loginForm.valid).toBe(false);
      spyDataService.login.and.returnValue(Promise.resolve());

      let compiled = fixture.debugElement.nativeElement;
      let username = compiled.querySelector('input[type="text"]');
      let password = compiled.querySelector('input[type="password"]');

      fixture.detectChanges();

      // Change value
      username.value = 'test';
      password.value = 'password';

      // dispatch input event
      dispatchEvent(new Event('input'));

      component.onSubmit();
      expect(component.submitted).toBe(true);
    }));
});
