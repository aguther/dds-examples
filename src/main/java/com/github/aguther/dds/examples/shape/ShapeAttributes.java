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

package com.github.aguther.dds.examples.shape;

import idl.ShapeFillKind;

public class ShapeAttributes {

  // base attributes
  private String color;
  private int size;
  // extended attributes
  private ShapeFillKind fillKind;
  private float angle;

  public ShapeAttributes() {
    this(
        "BLUE",
        30,
        ShapeFillKind.SOLID_FILL,
        0.0f
    );
  }

  public ShapeAttributes(
      final String color,
      final int size
  ) {
    this(
        color,
        size,
        ShapeFillKind.SOLID_FILL,
        0.0f
    );
  }

  public ShapeAttributes(
      final String color,
      final int size,
      final ShapeFillKind fillKind,
      final float angle
  ) {
    this.color = color;
    this.size = size;
    this.fillKind = fillKind;
    this.angle = angle;
  }

  public String getColor() {
    return color;
  }

  public int getSize() {
    return size;
  }

  public ShapeFillKind getFillKind() {
    return fillKind;
  }

  public float getAngle() {
    return angle;
  }
}
