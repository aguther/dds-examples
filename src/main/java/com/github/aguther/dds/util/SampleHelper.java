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

import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.dynamicdata.DynamicDataProperty_t;
import com.rti.dds.topic.TypeSupportImpl;
import com.rti.dds.typecode.TypeCode;

public class SampleHelper {

  private SampleHelper() {
  }

  public static <T> byte[] createCdrBufferForSample(
      final TypeSupportImpl typeSupportImpl,
      final T sample
  ) {
    // calculate needed size
    int size = (int) typeSupportImpl.serialize_to_cdr_buffer(null, 0, sample);

    // create buffer and return it
    return new byte[size];
  }

  public static <T> byte[] getCdrBufferFromSample(
      final TypeSupportImpl typeSupport,
      final T sample
  ) {
    // create buffer of correct size
    byte[] buffer = createCdrBufferForSample(typeSupport, sample);

    // serialize to buffer
    serializeSampleToCdrBuffer(
        buffer,
        typeSupport,
        sample
    );

    // return buffer
    return buffer;
  }

  public static <T> void serializeSampleToCdrBuffer(
      byte[] buffer,
      final TypeSupportImpl typeSupport,
      final T sample
  ) {
    typeSupport.serialize_to_cdr_buffer(
        buffer,
        buffer.length,
        sample
    );
  }

  @SuppressWarnings("unchecked")
  public static <T> T getSampleFromCdrBuffer(
      final TypeSupportImpl typeSupport,
      final Class clazz,
      final byte[] buffer
  ) throws IllegalAccessException, InstantiationException {

    // create sample of T
    T sample = (T) clazz.newInstance();

    // deserialize from buffer
    deserializeSampleFromCdrBuffer(
        sample,
        typeSupport,
        buffer
    );

    // return sample
    return sample;
  }

  public static <T> void deserializeSampleFromCdrBuffer(
      T sample,
      final TypeSupportImpl typeSupport,
      final byte[] buffer
  ) {
    typeSupport.deserialize_from_cdr_buffer(
        sample,
        buffer,
        buffer.length
    );
  }

  public static <T> DynamicData convertToDynamicData(
      final TypeSupportImpl typeSupport,
      final TypeCode typeCode,
      final T sample
  ) {
    return convertToDynamicData(
        typeSupport,
        typeCode,
        DynamicData.PROPERTY_DEFAULT,
        sample
    );
  }

  public static <T> DynamicData convertToDynamicData(
      final TypeSupportImpl typeSupport,
      final TypeCode typeCode,
      final DynamicDataProperty_t dynamicDataProperty,
      final T sample
  ) {
    // create dynamic data
    DynamicData dynamicData = new DynamicData(typeCode, dynamicDataProperty);

    // convert sample
    convertToDynamicData(
        dynamicData,
        typeSupport,
        sample
    );

    // return the result
    return dynamicData;
  }

  public static <T> void convertToDynamicData(
      DynamicData dynamicData,
      final TypeSupportImpl typeSupport,
      final T sample
  ) {
    // get cdr buffer from sample
    byte[] cdrBuffer = getCdrBufferFromSample(typeSupport, sample);

    // load cdr buffer
    dynamicData.from_cdr_buffer(cdrBuffer);
  }

  public static <T> void convertFromDynamicData(
      T sample,
      final TypeSupportImpl typeSupport,
      final DynamicData dynamicData
  ) {
    // create buffer
    byte[] cdrBuffer = new byte[dynamicData.get_serialized_size()];

    // convert to cdr buffer
    dynamicData.to_cdr_buffer(cdrBuffer);

    // convert to sample
    typeSupport.deserialize_from_cdr_buffer(sample, cdrBuffer, cdrBuffer.length);
  }
}
