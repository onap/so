
/**
============LICENSE_START=======================================================
 Copyright (C) 2019 Ericsson. All rights reserved.
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

@authors: andrei.barcovschi@ericsson.com, waqas.ikram@ericsson.com
*/

export class Constants {
  public static OPTIONS = [{ name: "EQUAL", value: "EQ" }, { name: "NOT EQUAL", value: "NEQ" }, { name: "LIKE", value: "LIKE" }];

  public static STATUS_OPTIONS = [{ name: "ALL", value: "ALL" }, { name: "COMPLETE", value: "COMPLETE" }, { name: "IN_PROGRESS", value: "IN_PROGRESS" },
  { name: "FAILED", value: "FAILED" }, { name: "PENDING", value: "PENDING" }, { name: "UNLOCKED", value: "UNLOCKED" }];

  public static HOUR_OPTIONS = ["00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
    "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"];

  public static MINUTE_OPTIONS = ["00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15",
    "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35",
    "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55",
    "56", "57", "58", "59"];

  public static DISPLAYED_COLUMNS = ['requestId', 'serviceInstanceId', 'serviceIstanceName', 'networkId', 'requestStatus', 'serviceType', 'startTime', 'endTime'];

  public static DEFAULT_PAGE_SIZE_OPTIONS = [10, 25, 50, 100];
}
