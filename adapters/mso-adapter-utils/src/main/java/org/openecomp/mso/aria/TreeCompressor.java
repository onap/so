package org.openecomp.mso.aria;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TreeCompressor implements AutoCloseable {

    private final ZipOutputStream zipOutputStream;
    private final ByteArrayOutputStream byteArrayOutputStream;

    public TreeCompressor() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.zipOutputStream = new ZipOutputStream(this.byteArrayOutputStream);
    }

    public byte[] compressTree(String path, File basedir, File dir) throws IOException, NullPointerException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        compressDirectory(path, basedir, file);
                    } else {
                        compressFile(basedir, file);
                    }
                }
            }
        }
        return this.byteArrayOutputStream.toByteArray();
    }

    private void compressDirectory(String path, File basedir, File file) throws IOException {
        String dirPath = path + file.getName() + "/";
        ZipEntry zipEntry = new ZipEntry(dirPath);
        try {
            this.zipOutputStream.putNextEntry(zipEntry);
            compressTree(dirPath, basedir, file);
        } finally {
            this.zipOutputStream.closeEntry();
        }
    }

    private void compressFile(File basedir, File file) throws IOException {
        String filePath = file.getAbsolutePath().substring(basedir.getAbsolutePath().length() + 1)
            .replaceAll("\\\\", "/");
        writeFileToZipOutputStreamAsEntry(file, filePath);
    }

    private void writeFileToZipOutputStreamAsEntry(File file, String newPath) throws IOException {
        ZipEntry zipEntry = new ZipEntry(newPath);
        try {
            this.zipOutputStream.putNextEntry(zipEntry);
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    this.zipOutputStream.write(buffer, 0, len);
                }
            }
        } finally {
            this.zipOutputStream.closeEntry();
        }
    }

    @Override
    public void close() throws Exception {
        this.zipOutputStream.close();
        this.byteArrayOutputStream.close();
    }
}
