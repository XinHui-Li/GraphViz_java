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

package pers.jyb.graph.viz.api.ext;

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;

public interface ShapePropCalc extends NodeShapePost {

  FlatPoint minContainerSize(double innerHeight, double innerWidth);

  /**
   * Confirm whether a specified point is within the shape through the center coordinates of the
   * shape and the width and height of the shape.
   *
   * @param box   box information
   * @param point coordinates of the point to be detected
   * @return true - point in shape false - point not in shape
   */
  boolean in(Box box, FlatPoint point);

  default void ratio(FlatPoint boxSize) {
  }

  default FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    Asserts.nullArgument(box, "box");
    Asserts.nullArgument(labelSize, "labelSize");
    box.check();

    return new FlatPoint(box.getX(), box.getY());
  }

  default void squareRatio(FlatPoint boxSize) {
    if (boxSize == null) {
      return;
    }

    double max = Math.max(boxSize.getWidth(), boxSize.getHeight());
    boxSize.setWidth(max);
    boxSize.setHeight(max);
  }

  default boolean needMargin() {
    return true;
  }
}
