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

package pers.jyb.graph.viz.api.attributes;

import java.io.Serializable;
import java.util.Arrays;
import pers.jyb.graph.util.Asserts;

public class Point implements Serializable {

  private static final long serialVersionUID = 6658547404565779344L;

  protected final double[] points;

  public Point(double... points) {
    Asserts.nullArgument(points, "points");
    Asserts.illegalArgument(points.length <= 1, "The length of points must be greater than 1");
    this.points = points;
  }

  public double[] points() {
    return points;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Point point = (Point) o;
    return Arrays.equals(points, point.points);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(points);
  }

  @Override
  public String toString() {
    return "Point{" +
        "points=" + Arrays.toString(points) +
        '}';
  }
}
