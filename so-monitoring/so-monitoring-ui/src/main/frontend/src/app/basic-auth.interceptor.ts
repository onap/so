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

import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class BasicAuthInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    //add authorization header with basic auth credentials if available
    let auth = localStorage.getItem('authdata');
    if (auth) {
        const authReq = request.clone({
          headers: request.headers.set('Authorization', 'Basic ' + auth)
        });

        // send cloned request with header to the next handler.
        return next.handle(authReq);
   }

    return next.handle(request);
  }
}
