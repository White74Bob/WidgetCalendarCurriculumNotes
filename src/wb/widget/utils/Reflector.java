package wb.widget.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class Reflector {
    private static final String FORMAT_CLASS_NO_FIELD = "Class[%s] has no field[%s]!";

    private static final String FORMAT_CLASS_NO_METHOD = "Class[%s] has no method[%s]!";

    public static Object callDefaultConstructor(Class<?> clazz) throws Exception {
        boolean changed = false;
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
                changed = true;
            }
            return constructor.newInstance();
        } finally {
            if (changed) {
                constructor.setAccessible(false);
            }
        }
    }

    public static void setMember(Object instance, String member, Object value) throws Exception {
        boolean changed = false;
        Field field = null;
        try {
            field = instance.getClass().getDeclaredField(member);
            if (!field.isAccessible()) {
                field.setAccessible(true);
                changed = true;
            }
            field.set(instance, value);
        } finally {
            if (changed) {
                field.setAccessible(false);
            }
        }
    }

    public static Object getMember(Object instance, String member) throws Exception {
        return getMember(instance, instance.getClass(), member);
    }

    public static Object getMember(Object instance, Class<?> clazz, String member) throws Exception {
        if (clazz.equals(Object.class)) return null;
        boolean changed = false;
        Field field = null;
        try {
            field = clazz.getDeclaredField(member);
            if (!field.isAccessible()) {
                field.setAccessible(true);
                changed = true;
            }
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz == null) {
                throw new NoSuchFieldException(String.format(FORMAT_CLASS_NO_FIELD,
                        clazz.toString(), member));
            }
            return getMember(instance, superClazz, member);
        } catch (Exception e) {
            return null;
        } finally {
            if (changed) {
                field.setAccessible(false);
            }
        }
    }

    public static void setStaticField(final Class<?> clazz, String fieldName, Object value)
            throws Exception {
        boolean changed = false;
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
                changed = true;
            }
            field.set(null, value);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException(String.format(FORMAT_CLASS_NO_FIELD,
                    clazz.toString(), fieldName));
        } finally {
            if (changed) {
                field.setAccessible(false);
            }
        }
    }

    public static final String SEPARATOR_DOT = ".";

    public static class StaticFieldInfo {
        // For example: Environment.DIRECTORY_ALARM
        private static final String FORMAT_NAME = "%s.%s";
        // 举例:
        // Environment.DIRECTORY_ALARM - name
        // DIRECTORY_ALARM             - fieldName
        // alarm                       - value
        public final String name;
        public final String fieldName;
        public final Object value;

        public StaticFieldInfo(final String className, final String fieldName, Object value) {
            this.name = String.format(FORMAT_NAME, className, fieldName);
            this.fieldName = fieldName;
            this.value = value;
        }
    }

    private static String getClassName(final Class<?> clazz) {
        String className = clazz.getName();

        // 去掉package name;
        int lastSeparatorIndex = className.lastIndexOf(SEPARATOR_DOT);
        className = className.substring(lastSeparatorIndex + 1);

        return className;
    }

    // 根据field name前缀获得static fields信息
    // 比如，可以获得Environment.DIRECTORY_XXXXX的信息
    public static StaticFieldInfo[] getStaticFieldsByPrefix(final Class<?> clazz,
            final String fieldNamePrefix) {
        final String className = getClassName(clazz);
        String fieldName;

        boolean changed = false;
        Field[] fields = clazz.getDeclaredFields();
        ArrayList<StaticFieldInfo> list = new ArrayList<StaticFieldInfo>();
        Object value = null;
        for (Field field : fields) {
            fieldName = field.getName();
            if (!fieldName.startsWith(fieldNamePrefix)) continue;
            changed = false;
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    changed = true;
                }
                value = field.get(null);
            } catch (Exception e) {
                value = "Failed to get value for " + fieldName + "in Class[" + clazz + "]\n" + e;
            } finally {
                if (changed) {
                    field.setAccessible(false);
                }
            }
            list.add(new StaticFieldInfo(className, fieldName, value));
        }
        StaticFieldInfo[] infos = new StaticFieldInfo[list.size()];
        list.toArray(infos);
        return infos;
    }

    public static StaticFieldInfo[] getPublicStaticFields(final Class<?> clazz) {
        final String className = getClassName(clazz);
        String fieldName;

        boolean changed = false;
        Field[] fields = clazz.getDeclaredFields();
        ArrayList<StaticFieldInfo> list = new ArrayList<StaticFieldInfo>();
        Object value = null;
        for (Field field : fields) {
            fieldName = field.getName();
            if (!isPublicStatic(field.getModifiers())) continue;
            changed = false;
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    changed = true;
                }
                value = field.get(null);
            } catch (Exception e) {
                value = "Failed to get value for " + fieldName + "in Class[" + clazz + "]\n" + e;
            } finally {
                if (changed) {
                    field.setAccessible(false);
                }
            }
            list.add(new StaticFieldInfo(className, fieldName, value));
        }
        StaticFieldInfo[] infos = new StaticFieldInfo[list.size()];
        list.toArray(infos);
        return infos;
    }

    public static Object getStaticField(final Class<?> clazz, String fieldName) throws Exception {
        boolean changed = false;
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
                changed = true;
            }
            return field.get(null);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException(String.format(FORMAT_CLASS_NO_FIELD,
                    clazz.toString(), fieldName));
        } finally {
            if (changed) {
                field.setAccessible(false);
            }
        }
    }

    public static <T> Object callMethod(Object target, String name, Class<?>[] argTypes,
            Object[] args) throws Exception {
        Method method = null;
        boolean changed = false;
        try {
            method = target.getClass().getDeclaredMethod(name, argTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
                changed = true;
            }
            return method.invoke(target, args);
        } finally {
            if (changed) {
                method.setAccessible(false);
            }
        }
    }

    public static Object callStaticMethod(final Class<?> clazz, String methodName,
            Class<?>[] argTypes, Object[] args) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        boolean changed = false;
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, argTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
                changed = true;
            }
            return method.invoke(null, args);
        } catch (NoSuchMethodException e) {
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz == null) {
                throw new NoSuchMethodException(String.format(FORMAT_CLASS_NO_METHOD,
                        clazz.toString(), methodName));
            }
            return callStaticMethod(superClazz, methodName, argTypes, args);
        } finally {
            if (changed) {
                method.setAccessible(false);
            }
        }
    }

    public static class StaticMethodInfo {
        // For example: Environment.getDataDirectory()
        // Attention: the method has no parameter(s).
        private static final String FORMAT_NAME = "%s.%s()";
        // 举例:
        // Environment.getDataDirectory() - name
        // getDataDirectory - methodName
        // /data - returnValue
        public final String name;
        public final String methodName;
        public final Object returnValue;

        public StaticMethodInfo(final String className, final String methodName, Object returnValue) {
            this.name = String.format(FORMAT_NAME, className, methodName);
            this.methodName = methodName;
            this.returnValue = returnValue;
        }
    }

    private static boolean isSame(Class<?>[] types, Class<?>[] argTypes) {
        if (types == null) {
            return argTypes == null || argTypes.length == 0;
        }
        if (argTypes == null) {
            return types == null || types.length == 0;
        }
        if (types.length != argTypes.length) return false;

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(argTypes[i])) return false;
        }
        return true;
    }

    private static void appendTypes(StringBuilder sb, Class<?>[] types) {
        if (types == null) {
            sb.append("null");
        } else if (types.length <= 0) {
            sb.append('~');
        } else {
            for (Class<?> clazz : types) {
                sb.append(clazz);
                sb.append(',');
            }
        }
    }

    private static String getParameterTypes(Method method, Class<?>[] argTypes) {
        StringBuilder sb = new StringBuilder();

        sb.append("Method parameter types:\n");
        Class<?>[] types = method.getParameterTypes();
        appendTypes(sb, types);

        sb.append("\nInput types:\n");
        appendTypes(sb, argTypes);

        return sb.toString();
    }

    private static boolean isPublicStatic(final int modifiers) {
        final int mask = Modifier.PUBLIC | Modifier.STATIC;
        return (modifiers & mask) == mask;
    }

    public static StaticMethodInfo[] callPublicStaticMethodsWithPrefix(final Class<?> clazz,
            String methodNamePrefix, Class<?>[] argTypes, Object[] args) {
        final String className = getClassName(clazz);
        String methodName;

        boolean changed = false;
        Method[] methods = clazz.getDeclaredMethods();
        ArrayList<StaticMethodInfo> list = new ArrayList<StaticMethodInfo>();
        Object value = null;
        for (Method method : methods) {
            methodName = method.getName();
            if (!methodName.startsWith(methodNamePrefix)) {
                continue;
            }
            if (!isPublicStatic(method.getModifiers())) {
                continue;
            }
            if (!isSame(method.getParameterTypes(), argTypes)) {
                continue;
            }
            changed = false;
            try {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                    changed = true;
                }
                value = method.invoke(null, args);
            } catch (Exception e) {
                value = "Failed to invoke method[" + methodName + "] in Class[" + clazz + "]\n" + getParameterTypes(method, argTypes);
            } finally {
                if (changed) {
                    method.setAccessible(false);
                }
            }
            list.add(new StaticMethodInfo(className, methodName, value));
        }
        StaticMethodInfo[] infos = new StaticMethodInfo[list.size()];
        list.toArray(infos);
        return infos;
    }

    public static int getPidViaOS() throws Exception {
        Class<?> osClass = Class.forName("android.system.Os");
        Method getpid = osClass.getDeclaredMethod("getpid");
        int pid = (Integer) getpid.invoke(null);
        return pid;
    }
}
