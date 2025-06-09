package com.example.e4_collab.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipArchiveReader {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private ZipInputStream zipInputStream;

    public ZipArchiveReader() {
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public ZipArchiveReader(byte[] data) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        clearAndOpen(data);
    }


    public void clearAndOpen(byte[] data) {
        try {
            if (zipInputStream != null) {
                zipInputStream.close();
            }
            byteArrayOutputStream.reset();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            zipInputStream = new ZipInputStream(byteArrayInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map.Entry<String, byte[]> readNextFile() {
        try {
            if (zipInputStream == null) {
                return null;
            }

            ZipEntry zipEntry = zipInputStream.getNextEntry();

            if (zipEntry == null || zipEntry.isDirectory()) {
                return null;
            }

            String fileName = zipEntry.getName();

            byteArrayOutputStream.reset();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            zipInputStream.closeEntry();

            return new AbstractMap.SimpleEntry<>(fileName, byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void close() {
        try {
            byteArrayOutputStream.reset();
            zipInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
