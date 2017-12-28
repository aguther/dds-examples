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

package com.github.aguther.dds.gson;

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

  private class UnionMemberInfo {

    private String fieldName;
    private TypeAdapter<?> typeAdapter;

    UnionMemberInfo(
//        int ordinal,
        String fieldName,
        TypeAdapter<?> typeAdapter
    ) {
      this.fieldName = fieldName;
      this.typeAdapter = typeAdapter;
    }
  }

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
      String packagePrefix = "";
      final Pattern packagePrefixPattern = Pattern.compile(
          String.format("^(.*)%s$", unionTypeCode.name().replace("::", ".")));
      final Matcher packagePrefixMatcher = packagePrefixPattern.matcher(rawType.getName());
      if (packagePrefixMatcher.matches()) {
        packagePrefix = packagePrefixMatcher.group(1);
      }

      // construct type name of discriminator
      String discriminatorTypeName = packagePrefix.concat(
          discriminatorTypeCode.name().replace("::", "."));
      Class discriminatorClass = Class.forName(discriminatorTypeName);

      // create map with union information
      Map<String, UnionMemberInfo> unionMemberInfoMap = new HashMap<>();

      // iterate over discriminator values
      for (int i = 0; i < discriminatorTypeCode.member_count(); i++) {
        // get enum string and ordinal
        String discriminatorString = discriminatorTypeCode.member_name(i);
        int discriminatorOrdinal = discriminatorTypeCode.member_ordinal(i);

        // find corresponding field in union
        int memberId = unionTypeCode.find_member_by_label(discriminatorOrdinal);
        String memberName = unionTypeCode.member_name(memberId);

        // get type code of field
        TypeCode memberTypeCode = unionTypeCode.member_type(memberId);

        // construct type name of field
        String memberTypeName = packagePrefix.concat(
            memberTypeCode.name().replace("::", "."));

        // get class of field
        Class memberClass = Class.forName(memberTypeName);

        // get type adapter of field
        TypeAdapter<?> memberTypeAdapter = gson.getAdapter(memberClass);

        // create new union field info
        unionMemberInfoMap.put(
            discriminatorString,
            new UnionMemberInfo(
                memberName,
                memberTypeAdapter
            )
        );
      }

      // return new type adapter
      return (TypeAdapter<T>) new UnionTypeAdapter(
          rawType,
          gson.getAdapter(discriminatorClass),
          FieldAccess.get(rawType),
          unionMemberInfoMap
      );

    } catch (Exception e) {
      // if method '<NestedType> get(int)' is not found,
      // we cannot provide a type adapter
      return null;
    }
  }

  private static class UnionTypeAdapter<T extends Union> extends TypeAdapter<T> {

    private static final String DISCRIMINATOR_FIELD_NAME = "_d";

    private Class unionClass;
    private TypeAdapter<?> discriminatorTypeAdapter;
    private FieldAccess fieldAccess;
    private Map<String, UnionMemberInfo> memberInfoMap;

    private UnionTypeAdapter(
        Class unionClass,
        TypeAdapter<?> discriminatorTypeAdapter,
        FieldAccess fieldAccess,
        Map<String, UnionMemberInfo> memberInfoMap
    ) {
      this.unionClass = unionClass;
      this.discriminatorTypeAdapter = discriminatorTypeAdapter;
      this.fieldAccess = fieldAccess;
      this.memberInfoMap = memberInfoMap;
    }

    @Override
    public void write(
        JsonWriter out,
        T value
    ) throws IOException {

      // support null objects
      if (value == null) {
        out.nullValue();
        return;
      }

      // assert begin of object
      out.beginObject();

      // get discriminator
      Object discriminator = fieldAccess.get(value, DISCRIMINATOR_FIELD_NAME);

      // write discriminator
      out.name(DISCRIMINATOR_FIELD_NAME);
      ((TypeAdapter<Object>) discriminatorTypeAdapter).write(out, discriminator);

      // get corresponding union field info
      UnionMemberInfo unionMemberInfo = memberInfoMap.get(discriminator.toString());

      // write field
      out.name(unionMemberInfo.fieldName);
      ((TypeAdapter<Object>) unionMemberInfo.typeAdapter).write(
          out,
          fieldAccess.get(value, unionMemberInfo.fieldName)
      );

      // assert end of object
      out.endObject();
    }

    @Override
    public T read(
        JsonReader in
    ) throws IOException {

      // support null objects
      if (in.peek() == JsonToken.NULL) {
        // consume object
        in.nextNull();

        // return null
        return null;
      }

      try {
        // try to read union
        return readUnion(in);

      } catch (Exception e) {
        // skip remaining values
        while (in.hasNext()) {
          in.skipValue();
        }

        // assert end of object
        in.endObject();

        // we failed in converting the union
        return null;
      }
    }

    private T readUnion(
        JsonReader in
    ) throws IOException, InstantiationException, IllegalAccessException {

      // assert begin of object
      in.beginObject();

      // create new union with default values
      T union = (T) unionClass.newInstance();

      // gets populated when active branch is known
      UnionMemberInfo unionMemberInfo = null;

      while (in.hasNext()) {
        // get name of next item
        String name = in.nextName();

        if (DISCRIMINATOR_FIELD_NAME.equals(name)) {
          // read discriminator
          Object discriminatorObject = discriminatorTypeAdapter.read(in);

          // if discriminator is not valid, return union with defaults
          if (discriminatorObject == null) {
            // skip remaining values
            while (in.hasNext()) {
              in.skipValue();
            }

            // assert end of object
            in.endObject();

            // return empty union with default values
            return union;
          }

          // remember discriminator
          unionMemberInfo = memberInfoMap.get(discriminatorObject.toString());

          // set discriminator on union
          fieldAccess.set(union, DISCRIMINATOR_FIELD_NAME, discriminatorObject);
        } else if (unionMemberInfo != null
            && unionMemberInfo.fieldName.equals(name)) {

          Object fieldObject = unionMemberInfo.typeAdapter.read(in);
          fieldAccess.set(union, unionMemberInfo.fieldName, fieldObject);
        } else {
          in.skipValue();
        }
      }

      // assert end of object
      in.endObject();

      // return result
      return union;
    }
  }
}