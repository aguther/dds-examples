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
import com.rti.dds.infrastructure.Copyable;
import com.rti.dds.topic.TypeSupportImpl;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;

public class KafkaCdrTypeDeserializer implements Deserializer<Copyable> {

  public static final String KEY_DESERIALIZER_CLASS_CONFIG_TYPE = String.format(
      "%s.type",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
  );
  public static final String KEY_DESERIALIZER_CLASS_CONFIG_TYPE_SUPPORT = String.format(
      "%s.typeSupport",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
  );

  public static final String VALUE_DESERIALIZER_CLASS_CONFIG_TYPE = String.format(
      "%s.type",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
  );
  public static final String VALUE_DESERIALIZER_CLASS_CONFIG_TYPE_SUPPORT = String.format(
      "%s.typeSupport",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
  );

  private TypeSupportImpl typeSupport;
  private Class clazz;

  @Override
  public void configure(
      Map<String, ?> configs,
      boolean isKey
  ) {
    try {
      // get class of target type
      clazz = Class.forName(isKey ?
          configs.get(KEY_DESERIALIZER_CLASS_CONFIG_TYPE).toString()
          : configs.get(VALUE_DESERIALIZER_CLASS_CONFIG_TYPE).toString()
      );

      // get class of target type support
      Class<?> typeSupportClassName = Class.forName(isKey ?
          configs.get(KEY_DESERIALIZER_CLASS_CONFIG_TYPE_SUPPORT).toString()
          : configs.get(VALUE_DESERIALIZER_CLASS_CONFIG_TYPE_SUPPORT).toString()
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
  public Copyable deserialize(
      String topic,
      byte[] data
  ) {
    try {
      if (clazz == null || typeSupport == null || data == null || data.length == 0) {
        return null;
      }
      return SampleHelper.getSampleFromCdrBuffer(typeSupport, clazz, data);
    } catch (IllegalAccessException | InstantiationException e) {
      return null;
    }
  }

  @Override
  public void close() {
  }
}
