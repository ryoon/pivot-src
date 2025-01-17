/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.beans;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Exposes Java bean properties of an object via the {@link Map} interface. A
 * call to {@link Map#get(Object)} invokes the getter for the corresponding
 * property, and a call to {@link Map#put(Object, Object)} invokes the
 * property's setter. <p> Properties may provide multiple setters; the
 * appropriate setter to invoke is determined by the type of the value being
 * set. If the value is {@code null}, the return type of the getter method is
 * used. <p> Getter methods must be named "getProperty" where "property" is the
 * property name. If there is no "get" method, then an "isProperty" method can
 * also be used. Setter methods (if present) must be named "setProperty". <p>
 * Getter and setter methods are checked before straight fields named "property"
 * in order to support proper data encapsulation. And only <code>public</code>
 * and non-<code>static</code> methods and fields can be accessed.
 */
public class BeanAdapter implements Map<String, Object> {
    /**
     * Property iterator. Returns a property name for each getter method and public,
     * non-final field defined by the bean.
     */
    private class PropertyIterator implements Iterator<String> {
        /**
         * The list of methods in the bean object.
         */
        private Method[] methods = null;
        /**
         * The list of fields in the bean object.
         */
        private Field[] fields = null;

        /**
         * Current index into the {@link #methods} array.
         */
        private int methodIndex = 0;
        /**
         * Current index into the {@link #fields} array.
         */
        private int fieldIndex = 0;
        /**
         * The next property name to return (if any) during the iteration.
         */
        private String nextPropertyName = null;

        /**
         * Construct the property iterator over our bean object.
         */
        PropertyIterator() {
            methods = beanClass.getMethods();
            fields = beanClass.getFields();
            nextProperty();
        }

        @Override
        public boolean hasNext() {
            return (nextPropertyName != null);
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            String nameToReturn = nextPropertyName;
            nextProperty();

            return nameToReturn;
        }

        /**
         * Iterate to the next acceptable property method/field in the bean object.
         */
        private void nextProperty() {
            nextPropertyName = null;

            while (methodIndex < methods.length && nextPropertyName == null) {
                Method method = methods[methodIndex++];

                if (method.getParameterTypes().length == 0
                    && (method.getModifiers() & Modifier.STATIC) == 0) {
                    String methodName = method.getName();

                    String prefix = null;
                    if (methodName.startsWith(GET_PREFIX)) {
                        prefix = GET_PREFIX;
                    } else {
                        if (methodName.startsWith(IS_PREFIX)) {
                            prefix = IS_PREFIX;
                        }
                    }

                    if (prefix != null) {
                        int propertyOffset = prefix.length();
                        String propertyName = Character.toLowerCase(methodName.charAt(propertyOffset))
                            + methodName.substring(propertyOffset + 1);

                        if (!propertyName.equals("class")) {
                            if (!ignoreReadOnlyProperties || !isReadOnly(propertyName)) {
                               nextPropertyName = propertyName;
                            }
                        }
                    }
                }
            }

            if (nextPropertyName == null) {
                while (fieldIndex < fields.length && nextPropertyName == null) {
                    Field field = fields[fieldIndex++];

                    int modifiers = field.getModifiers();
                    if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & Modifier.STATIC) == 0) {
                        if (!ignoreReadOnlyProperties || (modifiers & Modifier.FINAL) == 0) {
                            nextPropertyName = field.getName();
                        }
                    }
                }
            }
        }

        @Override
        @UnsupportedOperation
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The POJO object we are wrapping in order to get/set its properties.
     */
    private final Object bean;
    /**
     * For convenience, the class of the bean object.
     */
    private final Class<?> beanClass;
    /**
     * For convenience, name of the bean object's class.
     */
    private final String beanClassName;
    /**
     * Flag to say whether or not we ignore properties that have no "setting" methods
     * and are thus "readonly".
     */
    private final boolean ignoreReadOnlyProperties;

    /**
     * List of listeners for changes to properties (that is, values) in this map (bean).
     */
    private MapListener.Listeners<String, Object> mapListeners = new MapListener.Listeners<>();

    /** Prefix for "getProperty" method names. */
    public static final String GET_PREFIX = "get";
    /** Prefix for "isProperty" method names. */
    public static final String IS_PREFIX = "is";
    /** Prefix for "setProperty" method names. */
    public static final String SET_PREFIX = "set";

    /** Method name of an enum class to return an enum value from a String. */
    private static final String ENUM_VALUE_OF_METHOD_NAME = "valueOf";

    /** Error message format for illegal access exceptions. */
    private static final String ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT =
            "Unable to access property \"%s\" for type %s.";
    /** Error message for failed attempt to coerce to an enum value. */
    private static final String ENUM_COERCION_EXCEPTION_MESSAGE =
            "Unable to coerce %s (\"%s\") to %s.\nValid enum constants - %s";

    /**
     * Creates a new bean dictionary.
     *
     * @param beanObject The bean object to wrap.
     */
    public BeanAdapter(final Object beanObject) {
        this(beanObject, false);
    }

    /**
     * Creates a new bean dictionary which can ignore readonly fields (that is,
     * straight fields marked as <code>final</code> or bean properties where
     * there is a "get" method but no corresponding "set" method).
     *
     * @param beanObject The bean object to wrap.
     * @param ignoreReadOnlyValue {@code true} if {@code final} or non-settable
     * fields should be excluded from the dictionary, {@code false} to include all fields.
     */
    public BeanAdapter(final Object beanObject, final boolean ignoreReadOnlyValue) {
        Utils.checkNull(beanObject, "bean object");

        bean = beanObject;
        beanClass = bean.getClass();
        beanClassName = beanClass.getName();
        ignoreReadOnlyProperties = ignoreReadOnlyValue;
    }

    /**
     * Returns the bean object this dictionary wraps.
     *
     * @return The bean object, or {@code null} if no bean has been set.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Invokes the getter method for the given property.
     *
     * @param key The property name.
     * @return The value returned by the method, or {@code null} if no such
     * method exists.
     */
    @Override
    public Object get(final String key) {
        Utils.checkNullOrEmpty(key, "key");

        Object value = null;

        Method getterMethod = getGetterMethod(beanClass, key);

        if (getterMethod == null) {
            Field field = getField(beanClass, key);

            if (field != null) {
                try {
                    value = field.get(bean);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(String.format(
                        ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT, key, beanClassName),
                        exception);
                }
            }
        } else {
            try {
                value = getterMethod.invoke(bean, new Object[] {});
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                    key, beanClassName), exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(String.format(
                    "Error getting property \"%s\" for type %s.", key, beanClassName),
                    exception.getCause());
            }
        }

        return value;
    }

    /**
     * Invokes the setter method for the given property. The method signature is
     * determined by the type of the value. If the value is {@code null}, the
     * return type of the getter method is used.
     *
     * @param key The property name.
     * @param value The new property value.
     * @return Returns {@code null}, since returning the previous value would
     * require a call to the getter method, which may not be an efficient
     * operation.
     * @throws PropertyNotFoundException If the given property does not exist or
     * is read-only.
     */
    @Override
    public Object put(final String key, final Object value) {
        Utils.checkNullOrEmpty(key, "key");

        Method setterMethod = null;
        Object valueUpdated = value;

        if (valueUpdated != null) {
            // Get the setter method for the value type
            setterMethod = getSetterMethod(beanClass, key, valueUpdated.getClass());
        }

        if (setterMethod == null) {
            // Get the property type and attempt to coerce the value to it
            Class<?> propertyType = getType(key);

            if (propertyType != null) {
                setterMethod = getSetterMethod(beanClass, key, propertyType);
                valueUpdated = coerce(valueUpdated, propertyType, key);
            }
        }

        if (setterMethod == null) {
            Field field = getField(beanClass, key);

            if (field == null) {
                throw new PropertyNotFoundException("Property \"" + key + "\""
                    + " does not exist or is read-only for type "
                    + beanClassName + ".");
            }

            Class<?> fieldType = field.getType();
            if (valueUpdated != null) {
                Class<?> valueType = valueUpdated.getClass();
                if (!fieldType.isAssignableFrom(valueType)) {
                    valueUpdated = coerce(valueUpdated, fieldType, key);
                }
            }

            try {
                field.set(bean, valueUpdated);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                    key, beanClassName), exception);
            }
        } else {
            try {
                setterMethod.invoke(bean, new Object[] {valueUpdated});
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                    key, beanClassName), exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(String.format(
                    "Error setting property \"%s\" for type %s to value \"%s\"", key,
                    beanClassName, "" + valueUpdated), exception.getCause());
            }

        }

        Object previousValue = null;
        mapListeners.valueUpdated(this, key, previousValue);

        return previousValue;
    }

    /**
     * Invokes the setter methods for all the given properties that are present
     * in the map. The method signatures are determined by the type of the
     * values. If any value is {@code null}, the return type of the getter
     * method is used. There is an option to ignore (that is, not throw)
     * exceptions during the process, but to return status if any exceptions
     * were caught and ignored.
     *
     * @param valueMap The map of keys and values to be set.
     * @param ignoreErrors If <code>true</code> then any
     * {@link PropertyNotFoundException} thrown by the {@link #put put()} method
     * will be caught and ignored.
     * @return <code>true</code> if any exceptions were caught,
     * <code>false</code> if not.
     */
    public boolean putAll(final Map<String, ?> valueMap, final boolean ignoreErrors) {
        boolean anyErrors = false;
        for (String key : valueMap) {
            try {
                put(key, valueMap.get(key));
            } catch (PropertyNotFoundException ex) {
                if (!ignoreErrors) {
                    throw ex;
                }
                anyErrors = true;
            }
        }
        return anyErrors;
    }

    /**
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    @UnsupportedOperation
    public Object remove(final String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    @UnsupportedOperation
    public synchronized void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Verifies the existence of a property. The property must have a getter
     * method; write-only properties are not supported.
     *
     * @param key The property name.
     * @return {@code true} if the property exists; {@code false}, otherwise.
     */
    @Override
    public boolean containsKey(final String key) {
        Utils.checkNullOrEmpty(key, "key");

        boolean containsKey = (getGetterMethod(beanClass, key) != null);

        if (!containsKey) {
            containsKey = (getField(beanClass, key) != null);
        }

        return containsKey;
    }

    /**
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    @UnsupportedOperation
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    @UnsupportedOperation
    public int getCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Comparator<String> getComparator() {
        return null;
    }

    /**
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    @UnsupportedOperation
    public void setComparator(final Comparator<String> comparator) {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests the read-only state of a property.
     *
     * @param key The property name.
     * @return {@code true} if the property is read-only; {@code false},
     * otherwise.
     */
    public boolean isReadOnly(final String key) {
        return isReadOnly(beanClass, key);
    }

    /**
     * Returns the type of a property.
     *
     * @param key The property name.
     * @return The real class type of this property.
     * @see #getType(Class, String)
     */
    public Class<?> getType(final String key) {
        return getType(beanClass, key);
    }

    /**
     * Returns the generic type of a property.
     *
     * @param key The property name.
     * @return The generic type of this property.
     * @see #getGenericType(Class, String)
     */
    public Type getGenericType(final String key) {
        return getGenericType(beanClass, key);
    }

    /**
     * Returns an iterator over the bean's properties.
     *
     * @return A property iterator for this bean.
     */
    @Override
    public Iterator<String> iterator() {
        return new PropertyIterator();
    }

    @Override
    public final ListenerList<MapListener<String, Object>> getMapListeners() {
        return mapListeners;
    }

    /**
     * Tests the read-only state of a property. Note that if no such property
     * exists, this method will return {@code true} (it will <u>not</u> throw
     * an exception).
     *
     * @param beanClass The bean class.
     * @param key The property name.
     * @return {@code true} if the property is read-only; {@code false},
     * otherwise.
     */
    public static boolean isReadOnly(final Class<?> beanClass, final String key) {
        Utils.checkNull(beanClass, "beanClass");
        Utils.checkNullOrEmpty(key, "key");

        boolean isReadOnly = true;

        Method getterMethod = getGetterMethod(beanClass, key);
        if (getterMethod == null) {
            Field field = getField(beanClass, key);
            if (field != null) {
                isReadOnly = ((field.getModifiers() & Modifier.FINAL) != 0);
            }
        } else {
            Method setterMethod = getSetterMethod(beanClass, key, getType(beanClass, key));
            isReadOnly = (setterMethod == null);
        }

        return isReadOnly;
    }

    /**
     * Returns the type of a property.
     *
     * @param beanClass The bean class.
     * @param key The property name.
     * @return The type of the property, or {@code null} if no such bean
     * property exists.
     */
    public static Class<?> getType(final Class<?> beanClass, final String key) {
        Utils.checkNull(beanClass, "beanClass");
        Utils.checkNullOrEmpty(key, "key");

        Class<?> type = null;

        Method getterMethod = getGetterMethod(beanClass, key);

        if (getterMethod == null) {
            Field field = getField(beanClass, key);

            if (field != null) {
                type = field.getType();
            }
        } else {
            type = getterMethod.getReturnType();
        }

        return type;
    }

    /**
     * Returns the generic type of a property.
     *
     * @param beanClass The bean class.
     * @param key The property name.
     * @return The generic type of the property, or {@code null} if no such bean
     * property exists. If the type is a generic, an instance of
     * {@link java.lang.reflect.ParameterizedType} will be returned. Otherwise,
     * an instance of {@link java.lang.Class} will be returned.
     */
    public static Type getGenericType(final Class<?> beanClass, final String key) {
        Utils.checkNull(beanClass, "beanClass");
        Utils.checkNullOrEmpty(key, "key");

        Type genericType = null;

        Method getterMethod = getGetterMethod(beanClass, key);

        if (getterMethod == null) {
            Field field = getField(beanClass, key);

            if (field != null) {
                genericType = field.getGenericType();
            }
        } else {
            genericType = getterMethod.getGenericReturnType();
        }

        return genericType;
    }

    /**
     * Returns the public, non-static fields for a property. Note that fields
     * will only be consulted for bean properties after bean methods.
     *
     * @param beanClass The bean class.
     * @param key The property name.
     * @return The field, or {@code null} if the field does not exist, or is
     * non-public or static.
     */
    public static Field getField(final Class<?> beanClass, final String key) {
        Utils.checkNull(beanClass, "beanClass");
        Utils.checkNullOrEmpty(key, "key");

        Field field = null;

        try {
            field = beanClass.getField(key);

            int modifiers = field.getModifiers();

            // Exclude non-public and static fields
            if ((modifiers & Modifier.PUBLIC) == 0 || (modifiers & Modifier.STATIC) > 0) {
                field = null;
            }
        } catch (NoSuchFieldException exception) {
            // No-op
        }

        return field;
    }

    /**
     * Returns the getter method for a property.
     *
     * @param beanClass The bean class.
     * @param key The property name.
     * @return The getter method, or {@code null} if the method does not exist.
     */
    public static Method getGetterMethod(final Class<?> beanClass, final String key) {
        Utils.checkNull(beanClass, "beanClass");
        Utils.checkNullOrEmpty(key, "key");

        // Upper-case the first letter
        String keyUpdated = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Method getterMethod = null;

        try {
            getterMethod = beanClass.getMethod(GET_PREFIX + keyUpdated);
        } catch (NoSuchMethodException exception) {
            // No-op
        }

        if (getterMethod == null) {
            try {
                getterMethod = beanClass.getMethod(IS_PREFIX + keyUpdated);
            } catch (NoSuchMethodException exception) {
                // No-op
            }
        }

        return getterMethod;
    }

    /**
     * Simplified version of {@link #getSetterMethod(Class, String, Class)} that
     * doesn't do the null checks, or have to redo the method name calculation.
     *
     * @param beanClass The bean class.
     * @param methodName The setter method name we are looking for.
     * @param valueType The type of the property value.
     * @return The setter method, or {@code null} if the method cannot be found.
     */
    private static Method internalGetSetterMethod(final Class<?> beanClass, final String methodName,
            final Class<?> valueType) {
        Method setterMethod = null;

        try {
            setterMethod = beanClass.getMethod(methodName, valueType);
        } catch (NoSuchMethodException exception) {
            // No-op
        }

        if (setterMethod == null) {
            // Look for a match on the value's super type
            Class<?> superType = valueType.getSuperclass();
            if (superType != null) {
                setterMethod = internalGetSetterMethod(beanClass, methodName, superType);
            }
        }

        if (setterMethod == null) {
            // If value type is a primitive wrapper, look for a method
            // signature with the corresponding primitive type
            try {
                Field primitiveTypeField = valueType.getField("TYPE");
                Class<?> primitiveValueType = (Class<?>) primitiveTypeField.get(null);

                try {
                    setterMethod = beanClass.getMethod(methodName, primitiveValueType);
                } catch (NoSuchMethodException exception) {
                    // No-op
                }
            } catch (NoSuchFieldException exception) {
                // No-op
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(String.format(
                    ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT, methodName, beanClass.getName()),
                    exception);
            }
        }

        if (setterMethod == null) {
            // Walk the interface graph to find a matching method
            Class<?>[] interfaces = valueType.getInterfaces();

            int i = 0, n = interfaces.length;
            while (setterMethod == null && i < n) {
                Class<?> interfaceType = interfaces[i++];
                setterMethod = internalGetSetterMethod(beanClass, methodName, interfaceType);
            }
        }

        return setterMethod;
    }

    /**
     * Returns the setter method for a property.
     *
     * @param beanClass The bean class.
     * @param key The property name.
     * @param valueType The type of the property.
     * @return The setter method, or {@code null} if the method does not exist.
     */
    public static Method getSetterMethod(final Class<?> beanClass, final String key,
            final Class<?> valueType) {
        Utils.checkNull(beanClass, "beanClass");
        Utils.checkNullOrEmpty(key, "key");

        Method setterMethod = null;

        if (valueType != null) {
            // Upper-case the first letter and prepend the "set" prefix to
            // determine the method name
            String keyUpdated = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            final String methodName = SET_PREFIX + keyUpdated;

            setterMethod = internalGetSetterMethod(beanClass, methodName, valueType);
        }

        return setterMethod;
    }

    /**
     * Coerces a value to a given type.
     *
     * @param <T> The parametric type to coerce to.
     * @param value The object to be coerced.
     * @param type The type to coerce it to.
     * @param key The property name in question.
     * @return The coerced value.
     * @throws IllegalArgumentException for all the possible other exceptions.
     */
    @SuppressWarnings("unchecked")
    public static <T> T coerce(final Object value, final Class<? extends T> type, final String key) {
        Utils.checkNull(type, "type");

        Object coercedValue;

        if (value == null) {
            // Null values can only be coerced to null
            coercedValue = null;
        } else if (type == Object.class || type.isAssignableFrom(value.getClass())) {
            // Value doesn't need coercion
            coercedValue = value;
        } else if (type.isEnum()) {
            // Find and invoke the valueOf(String) method using an upper
            // case conversion of the supplied Object's toString() value
            try {
                String valueString = value.toString().toUpperCase(Locale.ENGLISH);
                Method valueOfMethod = type.getMethod(ENUM_VALUE_OF_METHOD_NAME, String.class);
                coercedValue = valueOfMethod.invoke(null, valueString);
            } catch (IllegalAccessException | InvocationTargetException
                    | SecurityException | NoSuchMethodException e) {
                // Nothing to be gained by handling the getMethod() & invoke() exceptions separately
                throw new IllegalArgumentException(String.format(
                    ENUM_COERCION_EXCEPTION_MESSAGE, value.getClass().getName(), value, type,
                    Arrays.toString(type.getEnumConstants())), e);
            }
        } else if (type == String.class) {
            coercedValue = value.toString();
        } else if (type == Boolean.class || type == Boolean.TYPE) {
            coercedValue = Boolean.parseBoolean(value.toString());
        } else if (type == Character.class || type == Character.TYPE) {
            coercedValue = value.toString().charAt(0);
        } else if (type == Byte.class || type == Byte.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).byteValue();
            } else {
                coercedValue = Byte.parseByte(value.toString());
            }
        } else if (type == Short.class || type == Short.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).shortValue();
            } else {
                coercedValue = Short.parseShort(value.toString());
            }
        } else if (type == Integer.class || type == Integer.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).intValue();
            } else {
                coercedValue = Integer.parseInt(value.toString());
            }
        } else if (type == Long.class || type == Long.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).longValue();
            } else {
                coercedValue = Long.parseLong(value.toString());
            }
        } else if (type == Float.class || type == Float.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).floatValue();
            } else {
                coercedValue = Float.parseFloat(value.toString());
            }
        } else if (type == Double.class || type == Double.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).doubleValue();
            } else {
                coercedValue = Double.parseDouble(value.toString());
            }
        } else if (type == BigInteger.class) {
            coercedValue = new BigInteger(value.toString());
        } else if (type == BigDecimal.class) {
            coercedValue = new BigDecimal(value.toString());
        } else {
            throw new IllegalArgumentException("Unable to coerce "
                + value.getClass().getName() + " to " + type + " for \"" + key + "\" property.");
        }

        return (T) coercedValue;
    }
}
