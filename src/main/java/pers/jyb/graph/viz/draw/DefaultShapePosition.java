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

package pers.jyb.graph.viz.draw;

import java.util.Objects;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.ext.DefaultBox;
import pers.jyb.graph.viz.api.ext.ShapePosition;

public class DefaultShapePosition extends DefaultBox implements ShapePosition {

  private final NodeShape nodeShape;

  public DefaultShapePosition(double x, double y, double height,
                              double width, NodeShape nodeShape) {
    super(x - width / 2, x + width / 2, y - height / 2, y + height / 2);
    Asserts.nullArgument(nodeShape, "nodeShape");
    this.nodeShape = nodeShape;
  }

  @Override
  public NodeShape nodeShape() {
    return nodeShape;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DefaultShapePosition that = (DefaultShapePosition) o;
    return nodeShape == that.nodeShape;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), nodeShape);
  }
}
