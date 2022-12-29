package com.Jeka8833.GenomeTests.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSaver {

    public static void saveToFile(Path file, Object obj) throws IOException {
        try (var stream = new ObjectOutputStream(Files.newOutputStream(file))) {
            stream.writeObject(obj);
        }
    }

    public static byte[] saveToArray(Object obj) throws IOException {
        try (var byteBuffer = new ByteArrayOutputStream(); var stream = new ObjectOutputStream(byteBuffer)) {
            stream.writeObject(obj);
            stream.flush();
            return byteBuffer.toByteArray();
        }
    }

    public static <T> T loadFromFile(Path file, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (var stream = new ObjectInputStream(Files.newInputStream(file))) {
            return clazz.cast(stream.readObject());
        }
    }

    public static <T> T loadFromArray(byte[] array, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (var stream = new ObjectInputStream(new ByteArrayInputStream(array))) {
            return clazz.cast(stream.readObject());
        }
    }
}
