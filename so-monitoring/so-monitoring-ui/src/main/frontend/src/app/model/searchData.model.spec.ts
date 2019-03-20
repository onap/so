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

@authors: andrei.barcovschi@ericsson.com
*/

import { SearchData } from './searchData.model';
import { SearchRequest } from './SearchRequest.model';
import { Observable, throwError } from 'rxjs';

describe('SearchData', () => {
  let searchRequest: SearchRequest;
  let searchData: SearchData;

  beforeEach(() => {
    searchData = new SearchData();
  });

  it('should return a SearchRequest observable with selectedValueSTATUS != ALL', () => { // NOT ALL CHANGE TEST TO TEST THIS ISNTESASD
    searchData.serviceInstanceId = "bd827d8c-4b07-11e9-8646-d663bd873d93";
    searchData.requestId = "cf1343d8-4b07-11e9-8646-d663bd873d93";
    searchData.serviceInstanceName = "service-123";
    searchData.selectedValueSTATUS = "COMPLETE";

    searchData.getSearchRequest().subscribe((request: SearchRequest) => {
      searchRequest = request;
    });
    let filters = searchRequest.getFilters();
    expect(searchRequest.getFilters()).toEqual({ serviceInstanceId: [ 'EQ', 'bd827d8c-4b07-11e9-8646-d663bd873d93' ],
                                              requestId: [ 'EQ', 'cf1343d8-4b07-11e9-8646-d663bd873d93' ],
                                              serviceInstanceName: [ 'EQ', 'service-123' ],
                                              requestStatus: [ 'EQ', 'COMPLETE' ]
                                              });
    expect(searchRequest.getStartTimeInMilliseconds()).toBe(searchData.startDate.getTime());
    expect(searchRequest.getEndTimeInMilliseconds()).toBe(searchData.endDate.getTime());
  });

  it('should throw an error if found incorrect start or end date', () => {
    searchData.startDate = null;
    searchData.endDate = undefined;
    searchData.getSearchRequest().subscribe({
      error: (err) => {
        expect(err).toEqual('Found end or start date empty, Please enter start and end date')
      }
    });
  });

  it('should throw an error if startTimeInMilliseconds > endTimeInMilliseconds', () => {
    searchData.startDate = new Date('March 20, 2019 02:00:00');
    searchData.endDate = new Date('March 20, 2019 01:00:00');
    searchData.selectedStartHour = '02';
    searchData.selectedEndHour = '01';
    searchData.selectedStartMinute = '00';
    searchData.selectedEndMinute = '00';

    searchData.getSearchRequest().subscribe({
      error: (err) => {
        expect(err).toEqual("End time: " + searchData.endDate + " can not be greater then start time: " + searchData.startDate)
      }
    });
  });
});
