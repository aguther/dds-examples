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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.rti.dds.util.LoanableSequence;
import java.io.IOException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class SequenceTypeAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(
      Gson gson,
      TypeToken<T> typeToken
  ) {

    // get raw type
    Class rawType = typeToken.getRawType();

    // check if the type applies to this factory
    if (!LoanableSequence.class.isAssignableFrom(rawType)) {
      return null;
    }

    try {
      // we need the nested type of the sequence but since RTI is using generics
      // or any other proper interface, we need to get the class from the method
      // "<NestedType> get(int)" that is generated into every sequence

      // get method
      Method getMethod = rawType.getMethod("get", int.class);
      // get nested type
      Class nestedType = getMethod.getReturnType();

      // get type adapter for nested type
      TypeAdapter<?> nestedTypeAdapter = gson.getAdapter(nestedType);

      // return new type adapter
      return (TypeAdapter<T>) new SequenceTypeAdapter(
          rawType,
          nestedTypeAdapter
      );

    } catch (NoSuchMethodException e) {
      // if method '<NestedType> get(int)' is not found,
      // we cannot provide a type adapter
      return null;
    }
  }

  private static class SequenceTypeAdapter<T extends LoanableSequence, E> extends TypeAdapter<T> {

    private Class clazz;
    private TypeAdapter<E> typeAdapter;

    private SequenceTypeAdapter(
        Class clazz,
        TypeAdapter<E> typeAdapter
    ) {
      this.clazz = clazz;
      this.typeAdapter = typeAdapter;
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

      out.beginArray();

      for (Object aValue : value) {
        typeAdapter.write(out, (E) aValue);
      }

      out.endArray();
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
        T sequence = (T) clazz.newInstance();

        in.beginArray();

        while (in.hasNext()) {
          sequence.add(typeAdapter.read(in));
        }

        in.endArray();

        return sequence;

      } catch (InstantiationException | IllegalAccessException e) {
        return null;
      }
    }
  }
}