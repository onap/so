#!/bin/bash
#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 Nordix Foundation.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================

JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk/
SVNFM_HOME=/opt/app/svnfm

if [ "$#" -eq 1 ]; then
    CONFIG_FILE=$1
else
    CONFIG_FILE=${CONFIG_FILE}
fi

if [ -z "$CONFIG_FILE" ]
  then
    CONFIG_FILE="/etc/defaultConfig.json"
fi

echo "SVNFM Config File path: $CONFIG_FILE"

$JAVA_HOME/bin/java -cp "$SVNFM_HOME/etc:$SVNFM_HOME/lib/*" 
#-Djavax.net.ssl.keyStore="$KEYSTORE" -Djavax.net.ssl.keyStorePassword="$KEYSTORE_PASSWD" -Djavax.net.ssl.trustStore="$TRUSTSTORE" 
