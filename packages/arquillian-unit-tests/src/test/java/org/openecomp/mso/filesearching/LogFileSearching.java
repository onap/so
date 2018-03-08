/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.filesearching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class LogFileSearching {

    private static PrintWriter writer;

    public static void initFile(String filePath) {
        if (writer == null) {
            try {
                // This is to reopen an existing file
                writer = new PrintWriter(new FileOutputStream(filePath, true));
            } catch (IOException e) {
                System.out.println("Exception caught when trying to open the file /tmp/mso-log-checker.log to dump the result");
                e.printStackTrace();
            }
        }
    }

    public static void closeFile() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }


    public static boolean searchInFile(final String needle, final File file) throws FileNotFoundException {
        Scanner logScanner = new Scanner(file);
        try {
            writer.println("Searching pattern " + needle + " in " + file.getAbsolutePath());
            //System.out.println("Searching pattern " + needle + " in " + file.getAbsolutePath());

            String filedata = logScanner.useDelimiter("\\Z").next();

            int occurrences = 0;
            int index = 0;

            while (index < filedata.length() && (index = filedata.indexOf(needle, index)) >= 0) {
                occurrences++;

                int separatorIndex = filedata.indexOf(System.getProperty("line.separator"), index);
                if (separatorIndex >= 0) {
                    writer.println("FOUND:" + filedata.substring(index, separatorIndex));
                    //System.out.println("FOUND:"
                    //	+ filedata.substring(index, separatorIndex));
                } else {
                    writer.println("FOUND:" + filedata.substring(index, filedata.length() - 1));
                    //System.out.println("FOUND:"
                    //	+ filedata.substring(index, filedata.length()-1));
                }
                index += needle.length();
            }
            writer.println("TOTAL:" + occurrences + " FOUND");
            //System.out.println("TOTAL:" + occurrences + " FOUND");
            if (occurrences > 0) {

                return true;
            } else {

                return false;
            }
        } catch (NoSuchElementException e) {
            writer.println("TOTAL:0 FOUND");
            //System.out.println("TOTAL:0 FOUND");
            return false;
        } finally {
            logScanner.close();
        }
    }

    public static boolean searchInDirectory(final String needle, final File directory) throws FileNotFoundException {

        boolean res = false;
        String[] dirFiles = directory.list();

        if (dirFiles != null) {

            for (String dir : dirFiles) {
                res = res || searchInDirectory(needle, new File(directory.getAbsolutePath() + "/" + dir));
            }

        } else {
            return LogFileSearching.searchInFile(needle, directory);
        }
        return res;
    }

    public static boolean searchInDirectoryForCommonIssues(final String[] needles, final File directory) throws FileNotFoundException {
        String[] defaultPatternsToUse = {"ClassNotFound", "NullPointer", "RuntimeException", "IllegalStateException", "FATAL"};

        if (needles != null && needles.length > 0) {
            defaultPatternsToUse = needles;
        }

        boolean returnValue = false;
        for (String needle : defaultPatternsToUse) {
            returnValue |= LogFileSearching.searchInDirectory(needle, directory);
        }

        return returnValue;
    }
}
