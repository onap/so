/*
 * Copyright (C) 2019 Google
 *
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
 */

package org.onap.so.client.cds.util;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor.Syntax;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ListValue;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

//Fork from :
//
//<dependency>
//<groupId>com.google.protobuf</groupId>
//<artifactId>protobuf-java-util</artifactId>
//<version>3.6.1</version>
//</dependency>
//
//
// Can be used to transform Json to Strcut. Ex:
//
//         Builder struct = Struct.newBuilder();
//        try {
//            JsonFormat.parser().merge(payload, struct);
//        } catch (InvalidProtocolBufferException e) {
//            log.error("Failed converting payload for blueprint({}:{}) for action({}). {}", blueprintVersion,
//                blueprintName, action, e);
//        }
public class JsonFormat {

    private static final Logger logger = Logger.getLogger(JsonFormat.class.getName());

    private JsonFormat() {
    }

    public static JsonFormat.Printer printer() {
        return new JsonFormat.Printer(JsonFormat.TypeRegistry.getEmptyTypeRegistry(), false, Collections.emptySet(),
            false, false, false);
    }

    public static JsonFormat.Parser parser() {
        return new JsonFormat.Parser(JsonFormat.TypeRegistry.getEmptyTypeRegistry(), false, 100);
    }

    private static String unsignedToString(int value) {
        return value >= 0 ? Integer.toString(value) : Long.toString((long) value & 4294967295L);
    }

    private static String unsignedToString(long value) {
        return value >= 0L ? Long.toString(value)
            : BigInteger.valueOf(value & 9223372036854775807L).setBit(63).toString();
    }

    private static String getTypeName(String typeUrl) throws InvalidProtocolBufferException {
        String[] parts = typeUrl.split("/");
        if (parts.length == 1) {
            throw new InvalidProtocolBufferException("Invalid type url found: " + typeUrl);
        } else {
            return parts[parts.length - 1];
        }
    }

    private static class ParserImpl {

        private final JsonFormat.TypeRegistry registry;
        private final JsonParser jsonParser;
        private final boolean ignoringUnknownFields;
        private final int recursionLimit;
        private int currentDepth;
        private static final Map<String, WellKnownTypeParser> wellKnownTypeParsers = buildWellKnownTypeParsers();
        private final Map<Descriptor, Map<String, FieldDescriptor>> fieldNameMaps = new HashMap();
        private static final BigInteger MAX_UINT64 = new BigInteger("FFFFFFFFFFFFFFFF", 16);
        private static final double EPSILON = 1.0E-6D;
        private static final BigDecimal MORE_THAN_ONE = new BigDecimal(String.valueOf(1.000001D));
        private static final BigDecimal MAX_DOUBLE;
        private static final BigDecimal MIN_DOUBLE;

        ParserImpl(JsonFormat.TypeRegistry registry, boolean ignoreUnknownFields, int recursionLimit) {
            this.registry = registry;
            this.ignoringUnknownFields = ignoreUnknownFields;
            this.jsonParser = new JsonParser();
            this.recursionLimit = recursionLimit;
            this.currentDepth = 0;
        }

        void merge(Reader json, com.google.protobuf.Message.Builder builder) throws IOException {
            try {
                JsonReader reader = new JsonReader(json);
                reader.setLenient(false);
                this.merge(this.jsonParser.parse(reader), builder);
            } catch (InvalidProtocolBufferException var4) {
                throw var4;
            } catch (JsonIOException var5) {
                if (var5.getCause() instanceof IOException) {
                    throw (IOException) var5.getCause();
                } else {
                    throw new InvalidProtocolBufferException(var5.getMessage());
                }
            } catch (Exception var6) {
                throw new InvalidProtocolBufferException(var6.getMessage());
            }
        }

        void merge(String json, com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
            try {
                JsonReader reader = new JsonReader(new StringReader(json));
                reader.setLenient(false);
                this.merge(this.jsonParser.parse(reader), builder);
            } catch (InvalidProtocolBufferException var4) {
                throw var4;
            } catch (Exception var5) {
                throw new InvalidProtocolBufferException(var5.getMessage());
            }
        }

        private static Map<String, WellKnownTypeParser> buildWellKnownTypeParsers() {
            Map<String, WellKnownTypeParser> parsers = new HashMap();
            parsers.put(Any.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeAny(json, builder);
                }
            });
            JsonFormat.ParserImpl.WellKnownTypeParser wrappersPrinter = new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeWrapper(json, builder);
                }
            };
            parsers.put(BoolValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(Int32Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(UInt32Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(Int64Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(UInt64Value.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(StringValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(BytesValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(FloatValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(DoubleValue.getDescriptor().getFullName(), wrappersPrinter);
            parsers.put(Struct.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeStruct(json, builder);
                }
            });
            parsers.put(ListValue.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeListValue(json, builder);
                }
            });
            parsers.put(Value.getDescriptor().getFullName(), new JsonFormat.ParserImpl.WellKnownTypeParser() {
                public void merge(JsonFormat.ParserImpl parser, JsonElement json,
                    com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
                    parser.mergeValue(json, builder);
                }
            });
            return parsers;
        }

        private void merge(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            JsonFormat.ParserImpl.WellKnownTypeParser specialParser = (JsonFormat.ParserImpl.WellKnownTypeParser) wellKnownTypeParsers
                .get(builder.getDescriptorForType().getFullName());
            if (specialParser != null) {
                specialParser.merge(this, json, builder);
            } else {
                this.mergeMessage(json, builder, false);
            }
        }

        private Map<String, FieldDescriptor> getFieldNameMap(Descriptor descriptor) {
            if (this.fieldNameMaps.containsKey(descriptor)) {
                return (Map) this.fieldNameMaps.get(descriptor);
            } else {
                Map<String, FieldDescriptor> fieldNameMap = new HashMap();
                Iterator var3 = descriptor.getFields().iterator();

                while (var3.hasNext()) {
                    FieldDescriptor field = (FieldDescriptor) var3.next();
                    fieldNameMap.put(field.getName(), field);
                    fieldNameMap.put(field.getJsonName(), field);
                }

                this.fieldNameMaps.put(descriptor, fieldNameMap);
                return fieldNameMap;
            }
        }

        private void mergeMessage(JsonElement json, com.google.protobuf.Message.Builder builder, boolean skipTypeUrl)
            throws InvalidProtocolBufferException {
            if (!(json instanceof JsonObject)) {
                throw new InvalidProtocolBufferException("Expect message object but got: " + json);
            } else {
                JsonObject object = (JsonObject) json;
                Map<String, FieldDescriptor> fieldNameMap = this.getFieldNameMap(builder.getDescriptorForType());
                Iterator var6 = object.entrySet().iterator();

                while (true) {
                    Entry entry;
                    do {
                        if (!var6.hasNext()) {
                            return;
                        }

                        entry = (Entry) var6.next();
                    } while (skipTypeUrl && ((String) entry.getKey()).equals("@type"));

                    FieldDescriptor field = (FieldDescriptor) fieldNameMap.get(entry.getKey());
                    if (field == null) {
                        if (!this.ignoringUnknownFields) {
                            throw new InvalidProtocolBufferException(
                                "Cannot find field: " + (String) entry.getKey() + " in message " + builder
                                    .getDescriptorForType().getFullName());
                        }
                    } else {
                        this.mergeField(field, (JsonElement) entry.getValue(), builder);
                    }
                }
            }
        }

        private void mergeAny(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor descriptor = builder.getDescriptorForType();
            FieldDescriptor typeUrlField = descriptor.findFieldByName("type_url");
            FieldDescriptor valueField = descriptor.findFieldByName("value");
            if (typeUrlField != null && valueField != null && typeUrlField.getType() == Type.STRING
                && valueField.getType() == Type.BYTES) {
                if (!(json instanceof JsonObject)) {
                    throw new InvalidProtocolBufferException("Expect message object but got: " + json);
                } else {
                    JsonObject object = (JsonObject) json;
                    if (!object.entrySet().isEmpty()) {
                        JsonElement typeUrlElement = object.get("@type");
                        if (typeUrlElement == null) {
                            throw new InvalidProtocolBufferException("Missing type url when parsing: " + json);
                        } else {
                            String typeUrl = typeUrlElement.getAsString();
                            Descriptor contentType = this.registry.find(JsonFormat.getTypeName(typeUrl));
                            if (contentType == null) {
                                throw new InvalidProtocolBufferException("Cannot resolve type: " + typeUrl);
                            } else {
                                builder.setField(typeUrlField, typeUrl);
                                com.google.protobuf.Message.Builder contentBuilder = DynamicMessage
                                    .getDefaultInstance(contentType).newBuilderForType();
                                JsonFormat.ParserImpl.WellKnownTypeParser specialParser = (JsonFormat.ParserImpl.WellKnownTypeParser) wellKnownTypeParsers
                                    .get(contentType.getFullName());
                                if (specialParser != null) {
                                    JsonElement value = object.get("value");
                                    if (value != null) {
                                        specialParser.merge(this, value, contentBuilder);
                                    }
                                } else {
                                    this.mergeMessage(json, contentBuilder, true);
                                }

                                builder.setField(valueField, contentBuilder.build().toByteString());
                            }
                        }
                    }
                }
            } else {
                throw new InvalidProtocolBufferException("Invalid Any type.");
            }
        }


        private void mergeStruct(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor descriptor = builder.getDescriptorForType();
            FieldDescriptor field = descriptor.findFieldByName("fields");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid Struct type.");
            } else {
                this.mergeMapField(field, json, builder);
            }
        }

        private void mergeListValue(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor descriptor = builder.getDescriptorForType();
            FieldDescriptor field = descriptor.findFieldByName("values");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid ListValue type.");
            } else {
                this.mergeRepeatedField(field, json, builder);
            }
        }

        private void mergeValue(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor type = builder.getDescriptorForType();
            if (json instanceof JsonPrimitive) {
                JsonPrimitive primitive = (JsonPrimitive) json;
                if (primitive.isBoolean()) {
                    builder.setField(type.findFieldByName("bool_value"), primitive.getAsBoolean());
                } else if (primitive.isNumber()) {
                    builder.setField(type.findFieldByName("number_value"), primitive.getAsDouble());
                } else {
                    builder.setField(type.findFieldByName("string_value"), primitive.getAsString());
                }
            } else {
                com.google.protobuf.Message.Builder listBuilder;
                FieldDescriptor field;
                if (json instanceof JsonObject) {
                    field = type.findFieldByName("struct_value");
                    listBuilder = builder.newBuilderForField(field);
                    this.merge(json, listBuilder);
                    builder.setField(field, listBuilder.build());
                } else if (json instanceof JsonArray) {
                    field = type.findFieldByName("list_value");
                    listBuilder = builder.newBuilderForField(field);
                    this.merge(json, listBuilder);
                    builder.setField(field, listBuilder.build());
                } else {
                    if (!(json instanceof JsonNull)) {
                        throw new IllegalStateException("Unexpected json data: " + json);
                    }

                    builder.setField(type.findFieldByName("null_value"), NullValue.NULL_VALUE.getValueDescriptor());
                }
            }

        }

        private void mergeWrapper(JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            Descriptor type = builder.getDescriptorForType();
            FieldDescriptor field = type.findFieldByName("value");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid wrapper type: " + type.getFullName());
            } else {
                builder.setField(field, this.parseFieldValue(field, json, builder));
            }
        }

        private void mergeField(FieldDescriptor field, JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            if (field.isRepeated()) {
                if (builder.getRepeatedFieldCount(field) > 0) {
                    throw new InvalidProtocolBufferException("Field " + field.getFullName() + " has already been set.");
                }
            } else {
                if (builder.hasField(field)) {
                    throw new InvalidProtocolBufferException("Field " + field.getFullName() + " has already been set.");
                }

                if (field.getContainingOneof() != null
                    && builder.getOneofFieldDescriptor(field.getContainingOneof()) != null) {
                    FieldDescriptor other = builder.getOneofFieldDescriptor(field.getContainingOneof());
                    throw new InvalidProtocolBufferException(
                        "Cannot set field " + field.getFullName() + " because another field " + other.getFullName()
                            + " belonging to the same oneof has already been set ");
                }
            }

            if (!field.isRepeated() || !(json instanceof JsonNull)) {
                if (field.isMapField()) {
                    this.mergeMapField(field, json, builder);
                } else if (field.isRepeated()) {
                    this.mergeRepeatedField(field, json, builder);
                } else {
                    Object value = this.parseFieldValue(field, json, builder);
                    if (value != null) {
                        builder.setField(field, value);
                    }
                }

            }
        }

        private void mergeMapField(FieldDescriptor field, JsonElement json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            if (!(json instanceof JsonObject)) {
                throw new InvalidProtocolBufferException("Expect a map object but found: " + json);
            } else {
                Descriptor type = field.getMessageType();
                FieldDescriptor keyField = type.findFieldByName("key");
                FieldDescriptor valueField = type.findFieldByName("value");
                if (keyField != null && valueField != null) {
                    JsonObject object = (JsonObject) json;
                    Iterator var8 = object.entrySet().iterator();

                    while (var8.hasNext()) {
                        Entry<String, JsonElement> entry = (Entry) var8.next();
                        com.google.protobuf.Message.Builder entryBuilder = builder.newBuilderForField(field);
                        Object key = this
                            .parseFieldValue(keyField, new JsonPrimitive((String) entry.getKey()), entryBuilder);
                        Object value = this.parseFieldValue(valueField, (JsonElement) entry.getValue(), entryBuilder);
                        if (value == null) {
                            throw new InvalidProtocolBufferException("Map value cannot be null.");
                        }

                        entryBuilder.setField(keyField, key);
                        entryBuilder.setField(valueField, value);
                        builder.addRepeatedField(field, entryBuilder.build());
                    }

                } else {
                    throw new InvalidProtocolBufferException("Invalid map field: " + field.getFullName());
                }
            }
        }

        private void mergeRepeatedField(FieldDescriptor field, JsonElement json,
            com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
            if (!(json instanceof JsonArray)) {
                throw new InvalidProtocolBufferException("Expect an array but found: " + json);
            } else {
                JsonArray array = (JsonArray) json;

                for (int i = 0; i < array.size(); ++i) {
                    Object value = this.parseFieldValue(field, array.get(i), builder);
                    if (value == null) {
                        throw new InvalidProtocolBufferException(
                            "Repeated field elements cannot be null in field: " + field.getFullName());
                    }

                    builder.addRepeatedField(field, value);
                }

            }
        }

        private int parseInt32(JsonElement json) throws InvalidProtocolBufferException {
            try {
                return Integer.parseInt(json.getAsString());
            } catch (Exception var4) {
                try {
                    BigDecimal value = new BigDecimal(json.getAsString());
                    return value.intValueExact();
                } catch (Exception var3) {
                    throw new InvalidProtocolBufferException("Not an int32 value: " + json);
                }
            }
        }

        private long parseInt64(JsonElement json) throws InvalidProtocolBufferException {
            try {
                return Long.parseLong(json.getAsString());
            } catch (Exception var4) {
                try {
                    BigDecimal value = new BigDecimal(json.getAsString());
                    return value.longValueExact();
                } catch (Exception var3) {
                    throw new InvalidProtocolBufferException("Not an int64 value: " + json);
                }
            }
        }

        private int parseUint32(JsonElement json) throws InvalidProtocolBufferException {
            try {
                long result = Long.parseLong(json.getAsString());
                if (result >= 0L && result <= 4294967295L) {
                    return (int) result;
                } else {
                    throw new InvalidProtocolBufferException("Out of range uint32 value: " + json);
                }
            } catch (InvalidProtocolBufferException var6) {
                throw var6;
            } catch (Exception var7) {
                try {
                    BigDecimal decimalValue = new BigDecimal(json.getAsString());
                    BigInteger value = decimalValue.toBigIntegerExact();
                    if (value.signum() >= 0 && value.compareTo(new BigInteger("FFFFFFFF", 16)) <= 0) {
                        return value.intValue();
                    } else {
                        throw new InvalidProtocolBufferException("Out of range uint32 value: " + json);
                    }
                } catch (InvalidProtocolBufferException var4) {
                    throw var4;
                } catch (Exception var5) {
                    throw new InvalidProtocolBufferException("Not an uint32 value: " + json);
                }
            }
        }

        private long parseUint64(JsonElement json) throws InvalidProtocolBufferException {
            try {
                BigDecimal decimalValue = new BigDecimal(json.getAsString());
                BigInteger value = decimalValue.toBigIntegerExact();
                if (value.compareTo(BigInteger.ZERO) >= 0 && value.compareTo(MAX_UINT64) <= 0) {
                    return value.longValue();
                } else {
                    throw new InvalidProtocolBufferException("Out of range uint64 value: " + json);
                }
            } catch (InvalidProtocolBufferException var4) {
                throw var4;
            } catch (Exception var5) {
                throw new InvalidProtocolBufferException("Not an uint64 value: " + json);
            }
        }

        private boolean parseBool(JsonElement json) throws InvalidProtocolBufferException {
            if (json.getAsString().equals("true")) {
                return true;
            } else if (json.getAsString().equals("false")) {
                return false;
            } else {
                throw new InvalidProtocolBufferException("Invalid bool value: " + json);
            }
        }

        private float parseFloat(JsonElement json) throws InvalidProtocolBufferException {
            if (json.getAsString().equals("NaN")) {
                return (float) (0.0F / 0.0);
            } else if (json.getAsString().equals("Infinity")) {
                return (float) (1.0F / 0.0);
            } else if (json.getAsString().equals("-Infinity")) {
                return (float) (-1.0F / 0.0);
            } else {
                try {
                    double value = Double.parseDouble(json.getAsString());
                    if (value <= 3.402826869208755E38D && value >= -3.402826869208755E38D) {
                        return (float) value;
                    } else {
                        throw new InvalidProtocolBufferException("Out of range float value: " + json);
                    }
                } catch (InvalidProtocolBufferException var4) {
                    throw var4;
                } catch (Exception var5) {
                    throw new InvalidProtocolBufferException("Not a float value: " + json);
                }
            }
        }

        private double parseDouble(JsonElement json) throws InvalidProtocolBufferException {
            if (json.getAsString().equals("NaN")) {
                return 0.0D / 0.0;
            } else if (json.getAsString().equals("Infinity")) {
                return 1.0D / 0.0;
            } else if (json.getAsString().equals("-Infinity")) {
                return -1.0D / 0.0;
            } else {
                try {
                    BigDecimal value = new BigDecimal(json.getAsString());
                    if (value.compareTo(MAX_DOUBLE) <= 0 && value.compareTo(MIN_DOUBLE) >= 0) {
                        return value.doubleValue();
                    } else {
                        throw new InvalidProtocolBufferException("Out of range double value: " + json);
                    }
                } catch (InvalidProtocolBufferException var3) {
                    throw var3;
                } catch (Exception var4) {
                    throw new InvalidProtocolBufferException("Not an double value: " + json);
                }
            }
        }

        private String parseString(JsonElement json) {
            return json.getAsString();
        }

        private ByteString parseBytes(JsonElement json) throws InvalidProtocolBufferException {
            try {
                return ByteString.copyFrom(BaseEncoding.base64().decode(json.getAsString()));
            } catch (IllegalArgumentException var3) {
                return ByteString.copyFrom(BaseEncoding.base64Url().decode(json.getAsString()));
            }
        }

        private EnumValueDescriptor parseEnum(EnumDescriptor enumDescriptor, JsonElement json)
            throws InvalidProtocolBufferException {
            String value = json.getAsString();
            EnumValueDescriptor result = enumDescriptor.findValueByName(value);
            if (result == null) {
                try {
                    int numericValue = this.parseInt32(json);
                    if (enumDescriptor.getFile().getSyntax() == Syntax.PROTO3) {
                        result = enumDescriptor.findValueByNumberCreatingIfUnknown(numericValue);
                    } else {
                        result = enumDescriptor.findValueByNumber(numericValue);
                    }
                } catch (InvalidProtocolBufferException var6) {
                    ;
                }

                if (result == null) {
                    throw new InvalidProtocolBufferException(
                        "Invalid enum value: " + value + " for enum type: " + enumDescriptor.getFullName());
                }
            }

            return result;
        }

        private Object parseFieldValue(FieldDescriptor field, JsonElement json,
            com.google.protobuf.Message.Builder builder) throws InvalidProtocolBufferException {
            if (json instanceof JsonNull) {
                if (field.getJavaType() == JavaType.MESSAGE && field.getMessageType().getFullName()
                    .equals(Value.getDescriptor().getFullName())) {
                    Value value = Value.newBuilder().setNullValueValue(0).build();
                    return builder.newBuilderForField(field).mergeFrom(value.toByteString()).build();
                } else {
                    return field.getJavaType() == JavaType.ENUM && field.getEnumType().getFullName()
                        .equals(NullValue.getDescriptor().getFullName()) ? field.getEnumType().findValueByNumber(0)
                        : null;
                }
            } else {
                switch (field.getType()) {
                    case INT32:
                    case SINT32:
                    case SFIXED32:
                        return this.parseInt32(json);
                    case INT64:
                    case SINT64:
                    case SFIXED64:
                        return this.parseInt64(json);
                    case BOOL:
                        return this.parseBool(json);
                    case FLOAT:
                        return this.parseFloat(json);
                    case DOUBLE:
                        return this.parseDouble(json);
                    case UINT32:
                    case FIXED32:
                        return this.parseUint32(json);
                    case UINT64:
                    case FIXED64:
                        return this.parseUint64(json);
                    case STRING:
                        return this.parseString(json);
                    case BYTES:
                        return this.parseBytes(json);
                    case ENUM:
                        return this.parseEnum(field.getEnumType(), json);
                    case MESSAGE:
                    case GROUP:
                        if (this.currentDepth >= this.recursionLimit) {
                            throw new InvalidProtocolBufferException("Hit recursion limit.");
                        }

                        ++this.currentDepth;
                        com.google.protobuf.Message.Builder subBuilder = builder.newBuilderForField(field);
                        this.merge(json, subBuilder);
                        --this.currentDepth;
                        return subBuilder.build();
                    default:
                        throw new InvalidProtocolBufferException("Invalid field type: " + field.getType());
                }
            }
        }

        static {
            MAX_DOUBLE = (new BigDecimal(String.valueOf(1.7976931348623157E308D))).multiply(MORE_THAN_ONE);
            MIN_DOUBLE = (new BigDecimal(String.valueOf(-1.7976931348623157E308D))).multiply(MORE_THAN_ONE);
        }

        private interface WellKnownTypeParser {

            void merge(JsonFormat.ParserImpl var1, JsonElement var2, com.google.protobuf.Message.Builder var3)
                throws InvalidProtocolBufferException;
        }
    }

    private static final class PrinterImpl {

        private final JsonFormat.TypeRegistry registry;
        private final boolean alwaysOutputDefaultValueFields;
        private final Set<FieldDescriptor> includingDefaultValueFields;
        private final boolean preservingProtoFieldNames;
        private final boolean printingEnumsAsInts;
        private final JsonFormat.TextGenerator generator;
        private final Gson gson;
        private final CharSequence blankOrSpace;
        private final CharSequence blankOrNewLine;
        private static final Map<String, WellKnownTypePrinter> wellKnownTypePrinters = buildWellKnownTypePrinters();

        PrinterImpl(JsonFormat.TypeRegistry registry, boolean alwaysOutputDefaultValueFields,
            Set<FieldDescriptor> includingDefaultValueFields, boolean preservingProtoFieldNames, Appendable jsonOutput,
            boolean omittingInsignificantWhitespace, boolean printingEnumsAsInts) {
            this.registry = registry;
            this.alwaysOutputDefaultValueFields = alwaysOutputDefaultValueFields;
            this.includingDefaultValueFields = includingDefaultValueFields;
            this.preservingProtoFieldNames = preservingProtoFieldNames;
            this.printingEnumsAsInts = printingEnumsAsInts;
            this.gson = JsonFormat.PrinterImpl.GsonHolder.DEFAULT_GSON;
            if (omittingInsignificantWhitespace) {
                this.generator = new JsonFormat.CompactTextGenerator(jsonOutput);
                this.blankOrSpace = "";
                this.blankOrNewLine = "";
            } else {
                this.generator = new JsonFormat.PrettyTextGenerator(jsonOutput);
                this.blankOrSpace = " ";
                this.blankOrNewLine = "\n";
            }

        }

        void print(MessageOrBuilder message) throws IOException {
            JsonFormat.PrinterImpl.WellKnownTypePrinter specialPrinter = (JsonFormat.PrinterImpl.WellKnownTypePrinter) wellKnownTypePrinters
                .get(message.getDescriptorForType().getFullName());
            if (specialPrinter != null) {
                specialPrinter.print(this, message);
            } else {
                this.print(message, (String) null);
            }
        }

        private static Map<String, WellKnownTypePrinter> buildWellKnownTypePrinters() {
            Map<String, WellKnownTypePrinter> printers = new HashMap();
            printers.put(Any.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printAny(message);
                }
            });
            JsonFormat.PrinterImpl.WellKnownTypePrinter wrappersPrinter = new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printWrapper(message);
                }
            };
            printers.put(BoolValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(Int32Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(UInt32Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(Int64Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(UInt64Value.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(StringValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(BytesValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(FloatValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(DoubleValue.getDescriptor().getFullName(), wrappersPrinter);
            printers.put(Struct.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printStruct(message);
                }
            });
            printers.put(Value.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printValue(message);
                }
            });
            printers.put(ListValue.getDescriptor().getFullName(), new JsonFormat.PrinterImpl.WellKnownTypePrinter() {
                public void print(JsonFormat.PrinterImpl printer, MessageOrBuilder message) throws IOException {
                    printer.printListValue(message);
                }
            });
            return printers;
        }

        private void printAny(MessageOrBuilder message) throws IOException {
            if (Any.getDefaultInstance().equals(message)) {
                this.generator.print("{}");
            } else {
                Descriptor descriptor = message.getDescriptorForType();
                FieldDescriptor typeUrlField = descriptor.findFieldByName("type_url");
                FieldDescriptor valueField = descriptor.findFieldByName("value");
                if (typeUrlField != null && valueField != null && typeUrlField.getType() == Type.STRING
                    && valueField.getType() == Type.BYTES) {
                    String typeUrl = (String) message.getField(typeUrlField);
                    String typeName = JsonFormat.getTypeName(typeUrl);
                    Descriptor type = this.registry.find(typeName);
                    if (type == null) {
                        throw new InvalidProtocolBufferException("Cannot find type for url: " + typeUrl);
                    } else {
                        ByteString content = (ByteString) message.getField(valueField);
                        Message contentMessage = (Message) DynamicMessage.getDefaultInstance(type).getParserForType()
                            .parseFrom(content);
                        JsonFormat.PrinterImpl.WellKnownTypePrinter printer = (JsonFormat.PrinterImpl.WellKnownTypePrinter) wellKnownTypePrinters
                            .get(typeName);
                        if (printer != null) {
                            this.generator.print("{" + this.blankOrNewLine);
                            this.generator.indent();
                            this.generator.print("\"@type\":" + this.blankOrSpace + this.gson.toJson(typeUrl) + ","
                                + this.blankOrNewLine);
                            this.generator.print("\"value\":" + this.blankOrSpace);
                            printer.print(this, contentMessage);
                            this.generator.print(this.blankOrNewLine);
                            this.generator.outdent();
                            this.generator.print("}");
                        } else {
                            this.print(contentMessage, typeUrl);
                        }

                    }
                } else {
                    throw new InvalidProtocolBufferException("Invalid Any type.");
                }
            }
        }

        private void printWrapper(MessageOrBuilder message) throws IOException {
            Descriptor descriptor = message.getDescriptorForType();
            FieldDescriptor valueField = descriptor.findFieldByName("value");
            if (valueField == null) {
                throw new InvalidProtocolBufferException("Invalid Wrapper type.");
            } else {
                this.printSingleFieldValue(valueField, message.getField(valueField));
            }
        }

        private ByteString toByteString(MessageOrBuilder message) {
            return message instanceof Message ? ((Message) message).toByteString()
                : ((com.google.protobuf.Message.Builder) message).build().toByteString();
        }

        private void printStruct(MessageOrBuilder message) throws IOException {
            Descriptor descriptor = message.getDescriptorForType();
            FieldDescriptor field = descriptor.findFieldByName("fields");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid Struct type.");
            } else {
                this.printMapFieldValue(field, message.getField(field));
            }
        }

        private void printValue(MessageOrBuilder message) throws IOException {
            Map<FieldDescriptor, Object> fields = message.getAllFields();
            if (fields.isEmpty()) {
                this.generator.print("null");
            } else if (fields.size() != 1) {
                throw new InvalidProtocolBufferException("Invalid Value type.");
            } else {
                Iterator var3 = fields.entrySet().iterator();

                while (var3.hasNext()) {
                    Entry<FieldDescriptor, Object> entry = (Entry) var3.next();
                    this.printSingleFieldValue((FieldDescriptor) entry.getKey(), entry.getValue());
                }

            }
        }

        private void printListValue(MessageOrBuilder message) throws IOException {
            Descriptor descriptor = message.getDescriptorForType();
            FieldDescriptor field = descriptor.findFieldByName("values");
            if (field == null) {
                throw new InvalidProtocolBufferException("Invalid ListValue type.");
            } else {
                this.printRepeatedFieldValue(field, message.getField(field));
            }
        }

        private void print(MessageOrBuilder message, String typeUrl) throws IOException {
            this.generator.print("{" + this.blankOrNewLine);
            this.generator.indent();
            boolean printedField = false;
            if (typeUrl != null) {
                this.generator.print("\"@type\":" + this.blankOrSpace + this.gson.toJson(typeUrl));
                printedField = true;
            }

            Map<FieldDescriptor, Object> fieldsToPrint = null;
            Iterator var5;
            if (!this.alwaysOutputDefaultValueFields && this.includingDefaultValueFields.isEmpty()) {
                fieldsToPrint = message.getAllFields();
            } else {
                fieldsToPrint = new TreeMap(message.getAllFields());
                var5 = message.getDescriptorForType().getFields().iterator();

                label66:
                while (true) {
                    FieldDescriptor field;
                    do {
                        do {
                            while (true) {
                                if (!var5.hasNext()) {
                                    break label66;
                                }

                                field = (FieldDescriptor) var5.next();
                                if (!field.isOptional()) {
                                    break;
                                }

                                if (field.getJavaType() != JavaType.MESSAGE || message.hasField(field)) {
                                    OneofDescriptor oneof = field.getContainingOneof();
                                    if (oneof == null || message.hasField(field)) {
                                        break;
                                    }
                                }
                            }
                        } while (((Map) fieldsToPrint).containsKey(field));
                    } while (!this.alwaysOutputDefaultValueFields && !this.includingDefaultValueFields.contains(field));

                    ((Map) fieldsToPrint).put(field, message.getField(field));
                }
            }

            Entry field;
            for (var5 = ((Map) fieldsToPrint).entrySet().iterator(); var5.hasNext();
                this.printField((FieldDescriptor) field.getKey(), field.getValue())) {
                field = (Entry) var5.next();
                if (printedField) {
                    this.generator.print("," + this.blankOrNewLine);
                } else {
                    printedField = true;
                }
            }

            if (printedField) {
                this.generator.print(this.blankOrNewLine);
            }

            this.generator.outdent();
            this.generator.print("}");
        }

        private void printField(FieldDescriptor field, Object value) throws IOException {
            if (this.preservingProtoFieldNames) {
                this.generator.print("\"" + field.getName() + "\":" + this.blankOrSpace);
            } else {
                this.generator.print("\"" + field.getJsonName() + "\":" + this.blankOrSpace);
            }

            if (field.isMapField()) {
                this.printMapFieldValue(field, value);
            } else if (field.isRepeated()) {
                this.printRepeatedFieldValue(field, value);
            } else {
                this.printSingleFieldValue(field, value);
            }

        }

        private void printRepeatedFieldValue(FieldDescriptor field, Object value) throws IOException {
            this.generator.print("[");
            boolean printedElement = false;

            Object element;
            for (Iterator var4 = ((List) value).iterator(); var4.hasNext();
                this.printSingleFieldValue(field, element)) {
                element = var4.next();
                if (printedElement) {
                    this.generator.print("," + this.blankOrSpace);
                } else {
                    printedElement = true;
                }
            }

            this.generator.print("]");
        }

        private void printMapFieldValue(FieldDescriptor field, Object value) throws IOException {
            Descriptor type = field.getMessageType();
            FieldDescriptor keyField = type.findFieldByName("key");
            FieldDescriptor valueField = type.findFieldByName("value");
            if (keyField != null && valueField != null) {
                this.generator.print("{" + this.blankOrNewLine);
                this.generator.indent();
                boolean printedElement = false;
                Iterator var7 = ((List) value).iterator();

                while (var7.hasNext()) {
                    Object element = var7.next();
                    Message entry = (Message) element;
                    Object entryKey = entry.getField(keyField);
                    Object entryValue = entry.getField(valueField);
                    if (printedElement) {
                        this.generator.print("," + this.blankOrNewLine);
                    } else {
                        printedElement = true;
                    }

                    this.printSingleFieldValue(keyField, entryKey, true);
                    this.generator.print(":" + this.blankOrSpace);
                    this.printSingleFieldValue(valueField, entryValue);
                }

                if (printedElement) {
                    this.generator.print(this.blankOrNewLine);
                }

                this.generator.outdent();
                this.generator.print("}");
            } else {
                throw new InvalidProtocolBufferException("Invalid map field.");
            }
        }

        private void printSingleFieldValue(FieldDescriptor field, Object value) throws IOException {
            this.printSingleFieldValue(field, value, false);
        }

        private void printSingleFieldValue(FieldDescriptor field, Object value, boolean alwaysWithQuotes)
            throws IOException {
            switch (field.getType()) {
                case INT32:
                case SINT32:
                case SFIXED32:
                    if (alwaysWithQuotes) {
                        this.generator.print("\"");
                    }

                    this.generator.print(((Integer) value).toString());
                    if (alwaysWithQuotes) {
                        this.generator.print("\"");
                    }
                    break;
                case INT64:
                case SINT64:
                case SFIXED64:
                    this.generator.print("\"" + ((Long) value).toString() + "\"");
                    break;
                case BOOL:
                    if (alwaysWithQuotes) {
                        this.generator.print("\"");
                    }

                    if ((Boolean) value) {
                        this.generator.print("true");
                    } else {
                        this.generator.print("false");
                    }

                    if (alwaysWithQuotes) {
                        this.generator.print("\"");
                    }
                    break;
                case FLOAT:
                    Float floatValue = (Float) value;
                    if (floatValue.isNaN()) {
                        this.generator.print("\"NaN\"");
                    } else if (floatValue.isInfinite()) {
                        if (floatValue < 0.0F) {
                            this.generator.print("\"-Infinity\"");
                        } else {
                            this.generator.print("\"Infinity\"");
                        }
                    } else {
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }

                        this.generator.print(floatValue.toString());
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }
                    }
                    break;
                case DOUBLE:
                    Double doubleValue = (Double) value;
                    if (doubleValue.isNaN()) {
                        this.generator.print("\"NaN\"");
                    } else if (doubleValue.isInfinite()) {
                        if (doubleValue < 0.0D) {
                            this.generator.print("\"-Infinity\"");
                        } else {
                            this.generator.print("\"Infinity\"");
                        }
                    } else {
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }

                        this.generator.print(doubleValue.toString());
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }
                    }
                    break;
                case UINT32:
                case FIXED32:
                    if (alwaysWithQuotes) {
                        this.generator.print("\"");
                    }

                    this.generator.print(JsonFormat.unsignedToString((Integer) value));
                    if (alwaysWithQuotes) {
                        this.generator.print("\"");
                    }
                    break;
                case UINT64:
                case FIXED64:
                    this.generator.print("\"" + JsonFormat.unsignedToString((Long) value) + "\"");
                    break;
                case STRING:
                    this.generator.print(this.gson.toJson(value));
                    break;
                case BYTES:
                    this.generator.print("\"");
                    this.generator.print(BaseEncoding.base64().encode(((ByteString) value).toByteArray()));
                    this.generator.print("\"");
                    break;
                case ENUM:
                    if (field.getEnumType().getFullName().equals("google.protobuf.NullValue")) {
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }

                        this.generator.print("null");
                        if (alwaysWithQuotes) {
                            this.generator.print("\"");
                        }
                    } else if (!this.printingEnumsAsInts && ((EnumValueDescriptor) value).getIndex() != -1) {
                        this.generator.print("\"" + ((EnumValueDescriptor) value).getName() + "\"");
                    } else {
                        this.generator.print(String.valueOf(((EnumValueDescriptor) value).getNumber()));
                    }
                    break;
                case MESSAGE:
                case GROUP:
                    this.print((Message) value);
            }

        }

        private interface WellKnownTypePrinter {

            void print(JsonFormat.PrinterImpl var1, MessageOrBuilder var2) throws IOException;
        }

        private static class GsonHolder {

            private static final Gson DEFAULT_GSON = (new GsonBuilder()).disableHtmlEscaping().create();

            private GsonHolder() {
            }
        }
    }

    private static final class PrettyTextGenerator implements JsonFormat.TextGenerator {

        private final Appendable output;
        private final StringBuilder indent;
        private boolean atStartOfLine;

        private PrettyTextGenerator(Appendable output) {
            this.indent = new StringBuilder();
            this.atStartOfLine = true;
            this.output = output;
        }

        public void indent() {
            this.indent.append("  ");
        }

        public void outdent() {
            int length = this.indent.length();
            if (length < 2) {
                throw new IllegalArgumentException(" Outdent() without matching Indent().");
            } else {
                this.indent.delete(length - 2, length);
            }
        }

        public void print(CharSequence text) throws IOException {
            int size = text.length();
            int pos = 0;

            for (int i = 0; i < size; ++i) {
                if (text.charAt(i) == '\n') {
                    this.write(text.subSequence(pos, i + 1));
                    pos = i + 1;
                    this.atStartOfLine = true;
                }
            }

            this.write(text.subSequence(pos, size));
        }

        private void write(CharSequence data) throws IOException {
            if (data.length() != 0) {
                if (this.atStartOfLine) {
                    this.atStartOfLine = false;
                    this.output.append(this.indent);
                }

                this.output.append(data);
            }
        }
    }

    private static final class CompactTextGenerator implements JsonFormat.TextGenerator {

        private final Appendable output;

        private CompactTextGenerator(Appendable output) {
            this.output = output;
        }

        public void indent() {
        }

        public void outdent() {
        }

        public void print(CharSequence text) throws IOException {
            this.output.append(text);
        }
    }

    interface TextGenerator {

        void indent();

        void outdent();

        void print(CharSequence var1) throws IOException;
    }

    public static class TypeRegistry {

        private final Map<String, Descriptor> types;

        public static JsonFormat.TypeRegistry getEmptyTypeRegistry() {
            return JsonFormat.TypeRegistry.EmptyTypeRegistryHolder.EMPTY;
        }

        public static JsonFormat.TypeRegistry.Builder newBuilder() {
            return new JsonFormat.TypeRegistry.Builder();
        }

        public Descriptor find(String name) {
            return (Descriptor) this.types.get(name);
        }

        private TypeRegistry(Map<String, Descriptor> types) {
            this.types = types;
        }

        public static class Builder {

            private final Set<String> files;
            private Map<String, Descriptor> types;

            private Builder() {
                this.files = new HashSet();
                this.types = new HashMap();
            }

            public JsonFormat.TypeRegistry.Builder add(Descriptor messageType) {
                if (this.types == null) {
                    throw new IllegalStateException("A TypeRegistry.Builer can only be used once.");
                } else {
                    this.addFile(messageType.getFile());
                    return this;
                }
            }

            public JsonFormat.TypeRegistry.Builder add(Iterable<Descriptor> messageTypes) {
                if (this.types == null) {
                    throw new IllegalStateException("A TypeRegistry.Builder can only be used once.");
                } else {
                    Iterator var2 = messageTypes.iterator();

                    while (var2.hasNext()) {
                        Descriptor type = (Descriptor) var2.next();
                        this.addFile(type.getFile());
                    }

                    return this;
                }
            }

            public JsonFormat.TypeRegistry build() {
                JsonFormat.TypeRegistry result = new JsonFormat.TypeRegistry(this.types);
                this.types = null;
                return result;
            }

            private void addFile(FileDescriptor file) {
                if (this.files.add(file.getFullName())) {
                    Iterator var2 = file.getDependencies().iterator();

                    while (var2.hasNext()) {
                        FileDescriptor dependency = (FileDescriptor) var2.next();
                        this.addFile(dependency);
                    }

                    var2 = file.getMessageTypes().iterator();

                    while (var2.hasNext()) {
                        Descriptor message = (Descriptor) var2.next();
                        this.addMessage(message);
                    }

                }
            }

            private void addMessage(Descriptor message) {
                Iterator var2 = message.getNestedTypes().iterator();

                while (var2.hasNext()) {
                    Descriptor nestedType = (Descriptor) var2.next();
                    this.addMessage(nestedType);
                }

                if (this.types.containsKey(message.getFullName())) {
                    JsonFormat.logger.warning("Type " + message.getFullName() + " is added multiple times.");
                } else {
                    this.types.put(message.getFullName(), message);
                }
            }
        }

        private static class EmptyTypeRegistryHolder {

            private static final JsonFormat.TypeRegistry EMPTY = new JsonFormat.TypeRegistry(Collections.emptyMap());

            private EmptyTypeRegistryHolder() {
            }
        }
    }

    public static class Parser {

        private final JsonFormat.TypeRegistry registry;
        private final boolean ignoringUnknownFields;
        private final int recursionLimit;
        private static final int DEFAULT_RECURSION_LIMIT = 100;

        private Parser(JsonFormat.TypeRegistry registry, boolean ignoreUnknownFields, int recursionLimit) {
            this.registry = registry;
            this.ignoringUnknownFields = ignoreUnknownFields;
            this.recursionLimit = recursionLimit;
        }

        public JsonFormat.Parser usingTypeRegistry(JsonFormat.TypeRegistry registry) {
            if (this.registry != JsonFormat.TypeRegistry.getEmptyTypeRegistry()) {
                throw new IllegalArgumentException("Only one registry is allowed.");
            } else {
                return new JsonFormat.Parser(registry, this.ignoringUnknownFields, this.recursionLimit);
            }
        }

        public JsonFormat.Parser ignoringUnknownFields() {
            return new JsonFormat.Parser(this.registry, true, this.recursionLimit);
        }

        public void merge(String json, com.google.protobuf.Message.Builder builder)
            throws InvalidProtocolBufferException {
            (new JsonFormat.ParserImpl(this.registry, this.ignoringUnknownFields, this.recursionLimit))
                .merge(json, builder);
        }

        public void merge(Reader json, com.google.protobuf.Message.Builder builder) throws IOException {
            (new JsonFormat.ParserImpl(this.registry, this.ignoringUnknownFields, this.recursionLimit))
                .merge(json, builder);
        }

        JsonFormat.Parser usingRecursionLimit(int recursionLimit) {
            return new JsonFormat.Parser(this.registry, this.ignoringUnknownFields, recursionLimit);
        }
    }

    public static class Printer {

        private final JsonFormat.TypeRegistry registry;
        private boolean alwaysOutputDefaultValueFields;
        private Set<FieldDescriptor> includingDefaultValueFields;
        private final boolean preservingProtoFieldNames;
        private final boolean omittingInsignificantWhitespace;
        private final boolean printingEnumsAsInts;

        private Printer(JsonFormat.TypeRegistry registry, boolean alwaysOutputDefaultValueFields,
            Set<FieldDescriptor> includingDefaultValueFields, boolean preservingProtoFieldNames,
            boolean omittingInsignificantWhitespace, boolean printingEnumsAsInts) {
            this.registry = registry;
            this.alwaysOutputDefaultValueFields = alwaysOutputDefaultValueFields;
            this.includingDefaultValueFields = includingDefaultValueFields;
            this.preservingProtoFieldNames = preservingProtoFieldNames;
            this.omittingInsignificantWhitespace = omittingInsignificantWhitespace;
            this.printingEnumsAsInts = printingEnumsAsInts;
        }

        public JsonFormat.Printer usingTypeRegistry(JsonFormat.TypeRegistry registry) {
            if (this.registry != JsonFormat.TypeRegistry.getEmptyTypeRegistry()) {
                throw new IllegalArgumentException("Only one registry is allowed.");
            } else {
                return new JsonFormat.Printer(registry, this.alwaysOutputDefaultValueFields,
                    this.includingDefaultValueFields, this.preservingProtoFieldNames,
                    this.omittingInsignificantWhitespace, this.printingEnumsAsInts);
            }
        }

        public JsonFormat.Printer includingDefaultValueFields() {
            this.checkUnsetIncludingDefaultValueFields();
            return new JsonFormat.Printer(this.registry, true, Collections.emptySet(), this.preservingProtoFieldNames,
                this.omittingInsignificantWhitespace, this.printingEnumsAsInts);
        }

        public JsonFormat.Printer printingEnumsAsInts() {
            this.checkUnsetPrintingEnumsAsInts();
            return new JsonFormat.Printer(this.registry, this.alwaysOutputDefaultValueFields, Collections.emptySet(),
                this.preservingProtoFieldNames, this.omittingInsignificantWhitespace, true);
        }

        private void checkUnsetPrintingEnumsAsInts() {
            if (this.printingEnumsAsInts) {
                throw new IllegalStateException("JsonFormat printingEnumsAsInts has already been set.");
            }
        }

        public JsonFormat.Printer includingDefaultValueFields(Set<FieldDescriptor> fieldsToAlwaysOutput) {
            Preconditions.checkArgument(null != fieldsToAlwaysOutput && !fieldsToAlwaysOutput.isEmpty(),
                "Non-empty Set must be supplied for includingDefaultValueFields.");
            this.checkUnsetIncludingDefaultValueFields();
            return new JsonFormat.Printer(this.registry, false, fieldsToAlwaysOutput, this.preservingProtoFieldNames,
                this.omittingInsignificantWhitespace, this.printingEnumsAsInts);
        }

        private void checkUnsetIncludingDefaultValueFields() {
            if (this.alwaysOutputDefaultValueFields || !this.includingDefaultValueFields.isEmpty()) {
                throw new IllegalStateException("JsonFormat includingDefaultValueFields has already been set.");
            }
        }

        public JsonFormat.Printer preservingProtoFieldNames() {
            return new JsonFormat.Printer(this.registry, this.alwaysOutputDefaultValueFields,
                this.includingDefaultValueFields, true, this.omittingInsignificantWhitespace, this.printingEnumsAsInts);
        }

        public JsonFormat.Printer omittingInsignificantWhitespace() {
            return new JsonFormat.Printer(this.registry, this.alwaysOutputDefaultValueFields,
                this.includingDefaultValueFields, this.preservingProtoFieldNames, true, this.printingEnumsAsInts);
        }

        public void appendTo(MessageOrBuilder message, Appendable output) throws IOException {
            (new JsonFormat.PrinterImpl(this.registry, this.alwaysOutputDefaultValueFields,
                this.includingDefaultValueFields, this.preservingProtoFieldNames, output,
                this.omittingInsignificantWhitespace, this.printingEnumsAsInts)).print(message);
        }

        public String print(MessageOrBuilder message) throws InvalidProtocolBufferException {
            try {
                StringBuilder builder = new StringBuilder();
                this.appendTo(message, builder);
                return builder.toString();
            } catch (InvalidProtocolBufferException var3) {
                throw var3;
            } catch (IOException var4) {
                throw new IllegalStateException(var4);
            }
        }
    }
}
