package com.github.aguther.dds.examples.shape.util;

public enum ShapeKind {
  SQUARE,
  CIRCLE,
  TRIANGLE;

  @Override
  public String toString() {
    switch (this) {
      case SQUARE:
        return "Square";
      case CIRCLE:
        return "Circle";
      case TRIANGLE:
        return "Triangle";
      default:
        return super.toString();
    }
  }
}
