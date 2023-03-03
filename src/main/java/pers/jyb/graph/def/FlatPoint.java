/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pers.jyb.graph.def;

import java.io.Serializable;
import java.util.Objects;
import pers.jyb.graph.op.Vectors;

/**
 * Two-dimensional vertex,use horizontal and vertical coordinates to describe the location.
 *
 * @author jiangyb
 */
public class FlatPoint implements Comparable<FlatPoint>, Cloneable, Serializable {

  private static final long serialVersionUID = -8316004807717552046L;

  public static final FlatPoint ZERO = new FlatPoint(0, 0);

  private double x;

  private double y;

  public FlatPoint(double height, double width) {
    this.x = height;
    this.y = width;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidth() {
    return getY();
  }

  public double getHeight() {
    return getX();
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setHeight(double height) {
    setX(height);
  }

  public void setY(double y) {
    this.y = y;
  }

  public void setWidth(double width) {
    setY(width);
  }

  /**
   * Return a coordinate point in the opposite position.
   *
   * @return a coordinate point in the opposite position
   */
  public FlatPoint reserve() {
    return new FlatPoint(-x, -y);
  }

  public double dist() {
    return Vectors.len(x, y);
  }

  public boolean similarX(double x, double error) {
    return Math.abs(this.x - x) <= error;
  }

  public boolean similarY(double y, double error) {
    return Math.abs(this.y - y) <= error;
  }

  @Override
  protected FlatPoint clone() {
    try {
      return (FlatPoint) super.clone();
    } catch (CloneNotSupportedException e) {
      return new FlatPoint(x, y);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FlatPoint flatPoint = (FlatPoint) o;
    return Double.compare(flatPoint.x, x) == 0 &&
        Double.compare(flatPoint.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return "FlatPoint{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }

  public void flip() {
    double tmp = x;
    x = y;
    y = tmp;
  }

  /**
   * Returns the distance between two vertices.
   *
   * @param p1 the first flat point
   * @param p2 the second flat point
   * @return the distance between two vertices
   */
  public static double twoFlatPointDistance(FlatPoint p1, FlatPoint p2) {
    Objects.requireNonNull(p1);
    Objects.requireNonNull(p2);

    return twoPointDistance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
  }

  public static double twoPointDistance(double p1x, double p1y, double p2x, double p2y) {
    if (p1x == p2x) {
      return Math.abs(p2y - p1y);
    }

    if (p1y == p2y) {
      return Math.abs(p2x - p1x);
    }

    return Math.sqrt(Math.pow(p2y - p1y, 2) + Math.pow(p2x - p1x, 2));
  }

  @Override
  public int compareTo(FlatPoint o) {
    if (o == null) {
      return 1;
    }

    int r = Double.compare(x, o.x);
    if (r != 0) {
      return r;
    }
    return Double.compare(y, o.y);
  }
}
