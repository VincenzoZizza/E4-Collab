package com.example.e4_collab_rest.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiveWriter {
    private static final int BUFFER_SIZE = 4096;

    private final ByteArrayOutputStream byteArrayOutputStream;
    private ZipOutputStream zipOutputStream;

    public ZipArchiveWriter() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        clearAndOpen();
    }

    public void clearAndOpen() {
        try {
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
            byteArrayOutputStream.reset();
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFile(String filename, byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            ZipEntry entry = new ZipEntry(filename);
            zipOutputStream.putNextEntry(entry);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] close() {
        try {
            zipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
