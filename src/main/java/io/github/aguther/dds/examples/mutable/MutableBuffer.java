/*
 * MIT License
 *
 * Copyright (c) 2019 Andreas Guther
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

package io.github.aguther.dds.examples.mutable;

import io.github.aguther.dds.util.SampleHelper;
import com.rti.dds.topic.TypeSupportImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MutableBuffer {

  private static final Logger LOGGER = LogManager.getLogger(MutableBuffer.class);

  public static void main(
    final String[] args
  ) {

    // create first type
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Sample1 v1 -> v2");
    }
    final idl.v1.MutableType sample1 = new idl.v1.MutableType();
    sample1.key = 1;
    sample1.unionType._d = idl.v1.UnionTypeDiscriminant.ONE;
    sample1.unionType.one.number = 1;
    for (int i = 0; i < sample1.arrayType.length; i++) {
      idl.v1.StructTwo nested = new idl.v1.StructTwo();
      nested.number = i;
      nested.text = Integer.toString(i);

      sample1.arrayType[i] = nested;
    }
    for (int i = 0; i < sample1.sequenceType.getMaximum(); i++) {
      idl.v1.StructTwo structTwo = new idl.v1.StructTwo();
      structTwo.number = i;
      structTwo.text = Integer.toString(i);

      sample1.sequenceType.add(structTwo);
    }
    execute(
      idl.v1.MutableTypeTypeSupport.get_instance(),
      sample1,
      idl.v2.MutableTypeTypeSupport.get_instance(),
      new idl.v2.MutableType()
    );

    // create second type
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Sample2 v1 -> v2");
    }
    final idl.v1.MutableType sample2 = new idl.v1.MutableType();
    sample2.key = 2;
    sample2.unionType._d = idl.v1.UnionTypeDiscriminant.TWO;
    sample2.unionType.two.number = 2;
    sample2.unionType.two.text = "TWO";
    execute(
      idl.v1.MutableTypeTypeSupport.get_instance(),
      sample2,
      idl.v2.MutableTypeTypeSupport.get_instance(),
      new idl.v2.MutableType()
    );
  }

  private static <I, O> void execute(
    final TypeSupportImpl inputTypeSupport,
    final I inputSample,
    final TypeSupportImpl outputTypeSupport,
    final O outputSample
  ) {

    // print sample
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Input{}", inputSample.toString());
    }

    // serialize input to cdr buffer
    byte[] cdrBuffer = SampleHelper.getCdrBufferFromSample(
      inputTypeSupport,
      inputSample
    );

    // deserialize output from cdr buffer
    SampleHelper.deserializeSampleFromCdrBuffer(
      outputSample,
      outputTypeSupport,
      cdrBuffer
    );

    // print output
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Output{}", outputSample.toString());
    }
  }
}
