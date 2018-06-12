package org.openecomp.mso.adapters.sdnc;

import org.openecomp.mso.logger.MsoLogger;

import java.io.IOException;
import java.io.InputStream;

/**
 * file utility class
 */
public class FileUtil {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, FileUtil.class);

    /**
     * Read the specified resource file and return the contents as a String.
     *
     * @param fileName Name of the resource file
     * @return the contents of the resource file as a String
     * @throws IOException if there is a problem reading the file
     */
    public static String readResourceFile(String fileName) {
        InputStream stream;
        try {
            stream = getResourceAsStream(fileName);
            byte[] bytes;
            bytes = new byte[stream.available()];
            if(stream.read(bytes) > 0) {
                stream.close();
                return new String(bytes);
            } else {
                stream.close();
                return "";
            }
        } catch (IOException e) {
            LOGGER.debug("Exception:", e);
            return "";
        }
    }

    /**
     * Get an InputStream for the resource specified.
     *
     * @param resourceName Name of resource for which to get InputStream.
     * @return an InputStream for the resource specified.
     * @throws IOException If we can't get the InputStream for whatever reason.
     */
    private static InputStream getResourceAsStream(String resourceName) throws IOException {
        InputStream stream =
                FileUtil.class.getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IOException("Can't access resource '" + resourceName + "'");
        }
        return stream;
    }
}