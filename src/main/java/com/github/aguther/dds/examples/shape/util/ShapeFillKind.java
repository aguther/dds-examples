package com.github.aguther.dds.examples.shape.util;

public enum ShapeFillKind {
  SOLID,
  TRANSPARENT,
  HORIZONTAL_HATCH,
  VERTICAL_HATCH;

  public static idl.ShapeFillKind toShapeFillKind(
    ShapeFillKind value
  ) {
    return idl.ShapeFillKind.valueOf(value.ordinal());
  }
}
