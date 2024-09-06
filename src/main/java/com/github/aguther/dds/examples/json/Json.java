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

package com.github.aguther.dds.examples.json;

import com.github.aguther.dds.gson.EnumTypeAdapterFactory;
import com.github.aguther.dds.gson.SequenceTypeAdapterFactory;
import com.github.aguther.dds.gson.UnionTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Json {

  private static final Logger LOGGER = LogManager.getLogger(Json.class);

  private static final Gson gson = new GsonBuilder()
    .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
    .registerTypeAdapterFactory(new SequenceTypeAdapterFactory())
    .registerTypeAdapterFactory(new UnionTypeAdapterFactory())
    .setPrettyPrinting()
    .create();

  public static void main(
    final String[] args
  ) {

    // convert v1 type to JSON and back
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Converting v1 to json and back to v1");
    }
    convertToJsonAndBack(
      getMutableTypeV1(),
      idl.v1.MutableType.class,
      true
    );

    // convert v2 type to JSON and back
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Converting v2 to json and back to v2");
    }
    convertToJsonAndBack(
      getMutableTypeV2(),
      idl.v2.MutableType.class,
      true
    );

    // convert v1 to v2 via JSON
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Converting v1 to json and back to v2");
    }
    convertToJsonAndBack(
      getMutableTypeV1(),
      idl.v2.MutableType.class,
      false
    );

    // convert v2 to v1 via JSON
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Converting v2 to json and back to v1");
    }
    convertToJsonAndBack(
      getMutableTypeV2(),
      idl.v1.MutableType.class,
      false
    );
  }

  @SuppressWarnings("unchecked")
  private static <I, O> void convertToJsonAndBack(
    final I input,
    final Class outputClass,
    final boolean doComparison
  ) {

    // print sample
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Input{}", input.toString());
    }

    // serialize input to json
    String json = gson.toJson(input);

    // print json
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("JSON:\n{}", json);
    }

    // deserialize from json
    O output = (O) gson.fromJson(json, outputClass);

    // print output
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Output{}", output.toString());
    }

    // if requested, compare input and output
    if (doComparison) {
      if (input.equals(output)) {
        LOGGER.info("Samples are equal!");
      } else {
        LOGGER.error("Samples are NOT equal!");
      }
    }
  }

  private static idl.v1.MutableType getMutableTypeV1() {
    final idl.v1.MutableType sample = new idl.v1.MutableType();
    sample.key = 10;

    sample.unionType._d = idl.v1.UnionTypeDiscriminant.TWO;
    sample.unionType.two.number = 2;
    sample.unionType.two.text = "TWO";

    for (int i = 0; i < sample.arrayType.length; i++) {
      idl.v1.StructTwo nested = new idl.v1.StructTwo();
      nested.number = i;
      nested.text = Integer.toString(i);

      sample.arrayType[i] = nested;
    }

    for (int i = 0; i < sample.sequenceType.getMaximum(); i++) {
      idl.v1.StructTwo nested = new idl.v1.StructTwo();
      nested.number = i;
      nested.text = Integer.toString(i);

      sample.sequenceType.add(nested);
    }

    return sample;
  }

  private static idl.v2.MutableType getMutableTypeV2() {

    final idl.v2.MutableType sample = new idl.v2.MutableType();
    sample.key = 10;

    sample.unionType._d = idl.v2.UnionTypeDiscriminant.TWO;
    sample.unionType.two.number = 2;
    sample.unionType.two.text = "TWO";

    for (int i = 0; i < sample.arrayType.length; i++) {
      idl.v2.StructTwo nested = new idl.v2.StructTwo();
      nested.number = i;
      nested.text = Integer.toString(i);

      sample.arrayType[i] = nested;
    }

    for (int i = 0; i < sample.sequenceType.getMaximum(); i++) {
      idl.v2.StructTwo nested = new idl.v2.StructTwo();
      nested.number = i;
      nested.text = Integer.toString(i);

      sample.sequenceType.add(nested);
    }

    sample.newNumber = Integer.MAX_VALUE;

    return sample;
  }
}
