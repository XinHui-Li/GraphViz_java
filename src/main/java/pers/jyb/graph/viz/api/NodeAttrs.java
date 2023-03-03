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

package pers.jyb.graph.viz.api;

import java.io.Serializable;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.api.attributes.NodeStyle;

public class NodeAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = 4923498915561027736L;

  String id;

  Double height;

  Double width;

  FusionColor color;

  FusionColor fillColor;

  Color fontColor;

  String label;

  Labelloc labelloc;

  FlatPoint margin;

  NodeShape shape;

  Boolean fixedSize;

  Double fontSize;

  NodeStyle style;

  Integer side;

  public NodeAttrs() {
  }

  /*--------------------------------------------------- attributeVal ---------------------------------------------------*/

  public String getId() {
    return id;
  }

  public FusionColor getColor() {
    return color;
  }

  public FusionColor getFillColor() {
    return fillColor;
  }

  public Color getFontColor() {
    return fontColor;
  }

  public String getLabel() {
    return label;
  }

  public NodeShape getNodeShape() {
    return shape == null ? NodeShapeEnum.ELLIPSE : shape;
  }

  public Boolean getFixedSize() {
    return fixedSize;
  }

  public Double getFontSize() {
    return fontSize;
  }

  public NodeStyle getStyle() {
    return style;
  }

  public Double getHeight() {
    return height;
  }

  public Double getWidth() {
    return width;
  }

  public Labelloc getLabelloc() {
    return labelloc;
  }

  public FlatPoint getMargin() {
    return margin;
  }

  public Integer getSide() {
    return side;
  }

  @Override
  public NodeAttrs clone() {
    try {
      return (NodeAttrs) super.clone();
    } catch (CloneNotSupportedException ignore) {
      return null;
    }
  }
}
