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

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.api.attributes.Point;
import pers.jyb.graph.viz.api.ext.ShapePosition;

public abstract class ContainerDrawProp extends Rectangle implements ShapePosition {

  private String id;

  // label container center
  protected FlatPoint labelCenter;

  // labelSize
  protected FlatPoint labelSize;

  public double topLowestHeight() {
    Asserts.nullArgument(margin(), "margin");
    Asserts.nullArgument(labelloc(), "labelloc");

    if (labelSize == null || labelloc() != Labelloc.TOP) {
      return getVerMargin();
    }

    return Math.max(getVerMargin(), labelSize.getHeight());
  }

  public double bottomLowestHeight() {
    Asserts.nullArgument(margin(), "margin");
    Asserts.nullArgument(labelloc(), "labelloc");

    if (labelSize == null || labelloc() != Labelloc.BOTTOM) {
      return getVerMargin();
    }

    return Math.max(getVerMargin(), labelSize.getHeight());
  }

  public double getHorMargin() {
    Point margin = margin();
    return margin.points()[0];
  }

  public double getVerMargin() {
    Point margin = margin();
    return margin.points()[1];
  }

  public void setId(String id) {
    this.id = id;
  }

  public String id() {
    return containerId() == null ? id : containerId();
  }

  public void checkBox() {
    Asserts
        .illegalArgument(leftBorder > rightBorder, "leftBorder must be smaller than rightBorder");
    Asserts.illegalArgument(upBorder > downBorder, "upBorder must be smaller than downBorder");
  }

  public FlatPoint getLabelCenter() {
    return labelCenter;
  }

  public void setLabelCenter(FlatPoint labelCenter) {
    this.labelCenter = labelCenter;
  }

  public void setLabelSize(FlatPoint labelSize) {
    this.labelSize = labelSize;
  }

  public FlatPoint getLabelSize() {
    return labelSize;
  }

  @Override
  public double getX() {
    return (leftBorder + rightBorder) / 2;
  }

  @Override
  public double getY() {
    return (upBorder + downBorder) / 2;
  }

  @Override
  public double getHeight() {
    return Math.abs(downBorder - upBorder);
  }

  @Override
  public double getWidth() {
    return Math.abs(rightBorder - leftBorder);
  }

  @Override
  public NodeShape nodeShape() {
    return NodeShapeEnum.RECT;
  }

  @Override
  public void flip() {
    super.flip();
    if (labelSize != null && !isNodeProp()) {
      labelSize.flip();
    }
  }

  public boolean isNodeProp() {
    return this instanceof NodeDrawProp;
  }

  protected abstract Labelloc labelloc();

  protected abstract Point margin();

  protected abstract String containerId();
}
