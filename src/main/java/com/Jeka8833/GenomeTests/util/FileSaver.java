package com.Jeka8833.GenomeTests.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSaver {

    public static void saveToFile(Path file, Object obj) throws IOException {
        try (var stream = new ObjectOutputStream(Files.newOutputStream(file))) {
            stream.writeObject(obj);
        }
    }

    public static <T> T loadFromFile(Path file, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (var stream = new ObjectInputStream(Files.newInputStream(file))) {
            return clazz.cast(stream.readObject());
        }
    }
}
