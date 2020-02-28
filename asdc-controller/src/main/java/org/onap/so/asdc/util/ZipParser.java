/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019, CMCC Technologies Co., Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipParser {

    private static volatile ZipParser instance;

    public static ZipParser getInstance() {
        if (instance == null) {
            synchronized (ZipParser.class) {
                if (instance == null) {
                    instance = new ZipParser();
                }
            }
        }
        return instance;
    }

    public String parseJsonForZip(String path) throws IOException {
        ZipFile zf = new ZipFile(path);
        InputStream in = new BufferedInputStream(new FileInputStream(path));
        Charset cs = Charset.forName("utf-8");
        ZipInputStream zin = new ZipInputStream(in, cs);
        ZipEntry ze;
        String artifactContent = null;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.toString().endsWith("json")) {
                StringBuilder jsonStr = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                String line;
                while ((line = br.readLine()) != null) {
                    jsonStr.append(line);
                }
                br.close();
                artifactContent = jsonStr.toString().replace("\"", "\\\"").replaceAll("\\s", "");
            }
        }
        zin.closeEntry();
        return artifactContent;
    }

}
