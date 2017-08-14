/*
 * MIT License
 *
 * Copyright (c) 2017 Andreas Guther
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.aguther.dds.util;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.rti.dds.typecode.TypeCode;
import com.rti.dds.util.Union;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class UnionTypeAdapterFactory implements TypeAdapterFactory {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnionTypeAdapterFactory.class);

  @Override
  public <T> TypeAdapter<T> create(
      Gson gson,
      TypeToken<T> typeToken
  ) {

    // get raw type
    Class rawType = typeToken.getRawType();

    // check if the type applies to this factory
    if (!Union.class.isAssignableFrom(rawType)) {
      return null;
    }

    try {
      // get type code class of union
      final Class unionTypeCodeClass = Class.forName(rawType.getCanonicalName() + "TypeCode");

      // get type code of union
      final TypeCode unionTypeCode = (TypeCode) unionTypeCodeClass.getField("VALUE").get(null);
      // get type code of discriminator
      final TypeCode discriminatorTypeCode = unionTypeCode.discriminator_type();

      // detect any prefix package
      final String unionTypeNameFromClass = rawType.getName();
      final String unionTypeNameFromTypeCode = unionTypeCode.name().replace("::", ".");

      String packagePrefix = "";
      final Pattern packagePrefixPattern = Pattern.compile(String.format("^(.*)%s$", unionTypeNameFromTypeCode));
      final Matcher packagePrefixMatcher = packagePrefixPattern.matcher(unionTypeNameFromClass);
      if (packagePrefixMatcher.matches()) {
        packagePrefix = packagePrefixMatcher.group(1);
      }

      // construct type name of discriminator
      String discriminatorTypeName = packagePrefix.concat(discriminatorTypeCode.name().replace("::", "."));
      Class discriminatorClass = Class.forName(discriminatorTypeName);

      // create map with union information
      Map<String, UnionFieldInfo> unionFieldInfoMap = new HashMap<>();

      // iterate over discriminator values
      for (int i = 0; i < discriminatorTypeCode.member_count(); i++) {
        // get enum string
        String name = discriminatorTypeCode.member_name(i);
        // get enum ordinal
        int ordinal = discriminatorTypeCode.member_ordinal(i);

        // find corresponding field in union
        int branchFieldNumber = unionTypeCode.find_member_by_label(ordinal);
        String branchFieldName = unionTypeCode.member_name(branchFieldNumber);

        // get type code of field
        TypeCode branchFieldTypeCode = unionTypeCode.member_type(branchFieldNumber);

        // construct type name of field
        String branchFieldTypeName = packagePrefix.concat(branchFieldTypeCode.name().replace("::", "."));

        // get class of field
        Class branchFieldClass = Class.forName(branchFieldTypeName);

        // get type adapter of field
        TypeAdapter<?> branchFieldTypeAdapter = gson.getAdapter(branchFieldClass);

        // create new union field info
        unionFieldInfoMap.put(
            name,
            new UnionFieldInfo(
                ordinal,
                branchFieldName,
                branchFieldClass,
                branchFieldTypeAdapter
            )
        );
      }

      // return new type adapter
      return (TypeAdapter<T>) new UnionTypeAdapter(
          rawType,
          gson.getAdapter(discriminatorClass),
          FieldAccess.get(rawType),
          unionFieldInfoMap
      );

    } catch (Exception e) {
      // if method '<NestedType> get(int)' is not found,
      // we cannot provide a type adapter
      return null;
    }
  }

  private class UnionFieldInfo {

    private int ordinal;
    private String fieldName;
    private Class fieldClass;
    private TypeAdapter<?> typeAdapter;

    UnionFieldInfo(
        int ordinal,
        String fieldName,
        Class fieldClass,
        TypeAdapter<?> typeAdapter
    ) {
      this.ordinal = ordinal;
      this.fieldName = fieldName;
      this.fieldClass = fieldClass;
      this.typeAdapter = typeAdapter;
    }
  }

  private static class UnionTypeAdapter<T extends Union, E> extends TypeAdapter<T> {

    private Class unionClass;
    private TypeAdapter<?> discriminatorTypeAdapter;
    private FieldAccess fieldAccess;
    private Map<String, UnionFieldInfo> unionFieldInfoMap;

    private UnionTypeAdapter(
        Class unionClass,
        TypeAdapter<?> discriminatorTypeAdapter,
        FieldAccess fieldAccess,
        Map<String, UnionFieldInfo> unionFieldInfoMap
    ) {
      this.unionClass = unionClass;
      this.discriminatorTypeAdapter = discriminatorTypeAdapter;
      this.fieldAccess = fieldAccess;
      this.unionFieldInfoMap = unionFieldInfoMap;
    }

    private static <P> P returnTypedValue(P value) {
      return value;
    }

    @Override
    public void write(
        JsonWriter out,
        T value
    ) throws IOException {

      if (value == null) {
        out.nullValue();
        return;
      }

      out.beginObject();

      // get discriminator
      String discriminator = fieldAccess.get(value, "_d").toString();

      // write discriminator
      out.name("_d");
      out.value(discriminator);

      // get corresponding union field info
      UnionFieldInfo unionFieldInfo = unionFieldInfoMap.get(discriminator);

      // get value of field
      Object fieldObject = fieldAccess.get(value, unionFieldInfo.fieldName);

      // write field
      //unionFieldInfo.typeAdapter.write(out, fieldObject);

      out.endObject();
    }

    @Override
    public T read(
        JsonReader in
    ) throws IOException {

      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      try {
        in.beginObject();

        T union = (T) unionClass.newInstance();

        in.nextName();
        Object discriminatorObject = discriminatorTypeAdapter.read(in);
        fieldAccess.set(union, "_d", discriminatorObject);

//        in.nextName();
//        UnionFieldInfo unionFieldInfo = unionFieldInfoMap.get(discriminatorObject.toString());
//        Object fieldObject = unionFieldInfo.typeAdapter.read(in);
//        fieldAccess.set(union, unionFieldInfo.fieldName, fieldObject);

        in.endObject();

        return union;

      } catch (Exception e) {
        return null;
      }
    }
  }
}