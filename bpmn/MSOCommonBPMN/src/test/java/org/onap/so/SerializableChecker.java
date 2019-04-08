/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SerializableChecker {
    public static class SerializationFailure {
        private final String mContainingClass;
        private final String mMemberName;

        public SerializationFailure(String inNonSerializableClass, String inMemberName) {
            mContainingClass = inNonSerializableClass;
            mMemberName = inMemberName;
        }

        public String getContainingClass() {
            return mContainingClass;
        }

        public String getMemberName() {
            return mMemberName;
        }

        public String getBadMemberString() {
            if (mMemberName == null)
                return mContainingClass;
            return mContainingClass + "." + mMemberName;
        }

        @Override
        public String toString() {
            return "SerializationFailure [mNonSerializableClass=" + mContainingClass + ", mMemberName=" + mMemberName
                    + "]";
        }
    }

    private static class SerializationCheckerData {
        private Set<Class<?>> mSerializableClasses;

        SerializationCheckerData() {
            mSerializableClasses = new HashSet<Class<?>>();
        }

        boolean isAlreadyChecked(Class<?> inClass) {
            return mSerializableClasses.contains(inClass);
        }

        void addSerializableClass(Class<?> inClass) {
            mSerializableClasses.add(inClass);
        }
    }

    private SerializableChecker() {}

    public static SerializationFailure isFullySerializable(Class<?> inClass) {
        if (!isSerializable(inClass))
            return new SerializationFailure(inClass.getName(), null);

        return isFullySerializable(inClass, new SerializationCheckerData());
    }

    private static SerializationFailure isFullySerializable(Class<?> inClass,
            SerializationCheckerData inSerializationCheckerData) {
        for (Field field : declaredFields(inClass)) {
            Class<?> fieldDeclaringClass = field.getType();

            if (field.getType() == Object.class)
                continue;

            if (Modifier.isStatic(field.getModifiers()))
                continue;

            if (field.isSynthetic())
                continue;

            if (fieldDeclaringClass.isInterface() || fieldDeclaringClass.isPrimitive())
                continue;

            if (Modifier.isAbstract(field.getType().getModifiers()))
                continue;

            if (inSerializationCheckerData.isAlreadyChecked(fieldDeclaringClass))
                continue;

            if (isSerializable(fieldDeclaringClass)) {
                inSerializationCheckerData.addSerializableClass(inClass);

                SerializationFailure failure = isFullySerializable(field.getType(), inSerializationCheckerData);
                if (failure != null)
                    return failure;
                else
                    continue;
            }

            if (Modifier.isTransient(field.getModifiers()))
                continue;

            return new SerializationFailure(field.getDeclaringClass().getName(), field.getName());
        }
        return null;
    }

    private static boolean isSerializable(Class<?> inClass) {
        Set<Class<?>> interfaces = getInterfaces(inClass);
        if (interfaces == null)
            return false;
        boolean isSerializable = interfaces.contains(Serializable.class);
        if (isSerializable)
            return true;

        for (Class<?> classInterface : interfaces) {
            if (isSerializable(classInterface))
                return true;
        }

        if (inClass.getSuperclass() != null && isSerializable(inClass.getSuperclass()))
            return true;

        return false;
    }

    private static Set<Class<?>> getInterfaces(Class<?> inFieldDeclaringClass) {
        return new HashSet<Class<?>>(Arrays.asList(inFieldDeclaringClass.getInterfaces()));
    }

    private static List<Field> declaredFields(Class<?> inClass) {
        List<Field> fields = new ArrayList<Field>(Arrays.asList(inClass.getDeclaredFields()));

        Class<?> parentClasses = inClass.getSuperclass();

        if (parentClasses == null)
            return fields;
        fields.addAll(declaredFields(parentClasses));

        return fields;
    }
}
