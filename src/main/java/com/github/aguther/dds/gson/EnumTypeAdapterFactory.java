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
import com.rti.dds.util.Enum;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class EnumTypeAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(
      final Gson gson,
      final TypeToken<T> type
  ) {

    if (!Enum.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    return (TypeAdapter<T>) new EnumTypeAdapter(type.getRawType());
  }

  private static class EnumTypeAdapter<T extends Enum> extends TypeAdapter<T> {

    private Class clazz;

    private EnumTypeAdapter(
        final Class clazz
    ) {
      this.clazz = clazz;
    }

    @Override
    public void write(
        final JsonWriter out,
        final T value
    ) throws IOException {

      if (value == null) {
        out.nullValue();
        return;
      }

      out.value(value.toString());
    }

    @Override
    public T read(
        final JsonReader in
    ) throws IOException {

      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      return (T) Enum.valueOf(clazz, in.nextString());
    }
  }
}
