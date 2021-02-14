import java.util.Map;

/**
 * Title:       Class Utils class (used to get Class from name (String))
 * Copyright:   Copyright (c) 2021
 * @author Maxime NARBAUD
 * @version 1.0
 */

public final class ClassUtils {
    private static final Map<String, Class<?>> typeMap = Map.of(
                "boolean", boolean.class,
                "byte", byte.class,
                "short", short.class,
                "int", int.class,
                "long", long.class,
                "float", float.class,
                "double", double.class,
                "char", char.class,
                "void", void.class);


    public static Class<?> getClassFromString(final String typeString) throws IllegalArgumentException {
        if (typeMap.containsKey(typeString)) {
            return typeMap.get(typeString);

        } else {
            try {
                return Class.forName(typeString);

            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Class or type not found: " + typeString);
            }
        }
    }

    public static Class<?>[] getClassArray(final String[] types) throws IllegalArgumentException {
        if (types != null) {
            Class<?>[] classes = new Class[types.length];

            for (int i = 0; i < types.length; i++) {
                classes[i] = getClassFromString(types[i]);
            }

            return classes;
        }

        return null;
    }
}
