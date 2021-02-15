import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Title:       Class Utils class (used to get Class from name (String))
 * Copyright:   Copyright (c) 2021
 * @author Maxime NARBAUD
 * @version 1.0
 */

public final class ClassUtils {
    public static final Map<String, Class<?>> typeMap = Map.of(
                "boolean", boolean.class,
                "byte", byte.class,
                "short", short.class,
                "int", int.class,
                "long", long.class,
                "float", float.class,
                "double", double.class,
                "char", char.class,
                "void", void.class);


    public static Class<?> getClassFromString(final String typeString, final HashMap<String, Class<?>> loadedClassMap) throws IllegalArgumentException {
        if (typeMap.containsKey(typeString)) {
            return typeMap.get(typeString);

        } else if (loadedClassMap.containsKey(typeString)) {
            Class<?> cls = loadedClassMap.get(typeString);
            System.out.println(Arrays.toString(cls.getMethods()));

            return loadedClassMap.get(typeString);

        } else {
            throw new IllegalArgumentException("Class or type not found: " + typeString);
        }
    }

    public static Class<?>[] getClassArray(final String[] types, final HashMap<String, Class<?>> loadedClassMap) throws IllegalArgumentException {
        if (types != null) {
            Class<?>[] classes = new Class[types.length];

            for (int i = 0; i < types.length; i++) {
                classes[i] = getClassFromString(types[i], loadedClassMap);
            }

            return classes;
        }

        return null;
    }

    /**
     *  Writes result in the output file, creates a new file if not exist, else adds at the end
     *
     *  @param result the result of the executed Command that you want to write in output file
     *  @param outFilePath output file relative path
     */
    public static void writeResultInFile(String result, String outFilePath) {
        try {
            FileWriter fw = new FileWriter(outFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(result);
            bw.newLine();
            bw.close();
            fw.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
