/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Guther
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

import com.esotericsoftware.reflectasm.MethodAccess;
import com.github.aguther.dds.util.SampleHelper;
import com.rti.dds.infrastructure.Copyable;
import com.rti.dds.topic.TypeSupportImpl;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaCdrTypeSerializer implements Serializer<Copyable> {

  public static final String KEY_SERIALIZER_CLASS_CONFIG_TYPE_SUPPORT = String.format(
      "%s.typeSupport",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
  );

  public static final String VALUE_SERIALIZER_CLASS_CONFIG_TYPE_SUPPORT = String.format(
      "%s.typeSupport",
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
  );

  private TypeSupportImpl typeSupport;

  @Override
  public void configure(
      Map<String, ?> configs,
      boolean isKey
  ) {
    try {
      // get class of target type support
      Class<?> typeSupportClassName = Class.forName(isKey ?
          configs.get(KEY_SERIALIZER_CLASS_CONFIG_TYPE_SUPPORT).toString()
          : configs.get(VALUE_SERIALIZER_CLASS_CONFIG_TYPE_SUPPORT).toString()
      );

      // get method for instance
      MethodAccess typeSupportMethodAccess = MethodAccess.get(typeSupportClassName);

      // get type support instance
      typeSupport = (TypeSupportImpl) typeSupportMethodAccess.invoke(null, "getInstance");

    } catch (ClassNotFoundException e) {
      // nothing we can do here due to API restrictions
    }
  }

  @Override
  public byte[] serialize(
      String topic,
      Copyable data
  ) {
    if (typeSupport == null || data == null) {
      return null;
    }
    return SampleHelper.getCdrBufferFromSample(typeSupport, data);
  }

  @Override
  public void close() {
  }
}
