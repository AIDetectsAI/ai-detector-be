package org.example.aidetectorbe;

public class TestUtils {

    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Class<?> targetClass = target.getClass();
            java.lang.reflect.Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}