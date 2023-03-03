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
import java.util.Objects;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.def.VertexIndex;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeStyle;

public class Node extends VertexIndex implements Comparable<Node>, Serializable {

  private static final long serialVersionUID = 4616284202432237469L;

  private final NodeAttrs nodeAttrs;

  private Node(NodeAttrs nodeAttrs) {
    Objects.requireNonNull(nodeAttrs);
    this.nodeAttrs = nodeAttrs;
  }

  public NodeAttrs nodeAttrs() {
    return nodeAttrs;
  }

  public static NodeBuilder builder() {
    return new NodeBuilder();
  }

  @Override
  public int compareTo(Node o) {
    if (o == null || o.nodeAttrs.label == null) {
      return 1;
    }

    if (this.nodeAttrs.label == null) {
      return -1;
    }

    int c = this.nodeAttrs.label.compareTo(o.nodeAttrs.label);

    if (c == 0 && nodeAttrs.id != null) {
      c = nodeAttrs.id.compareTo(o.nodeAttrs.id);
    }

    return c != 0 ? c : this.hashCode() - o.hashCode();
  }

  /*------------------------------------------ buidler ---------------------------------------*/

  public static final class NodeBuilder {

    private final NodeAttrs nodeAttrs;

    private NodeBuilder() {
      nodeAttrs = new NodeAttrs();
    }

    public NodeBuilder id(String id) {
      Asserts.nullArgument(id, "id");
      nodeAttrs.id = id;
      return this;
    }

    public NodeBuilder height(double height) {
      Asserts.illegalArgument(
          height < 0,
          "height (" + height + ") must be > 0"
      );
      nodeAttrs.height = height * Graphviz.PIXLE;
      return this;
    }

    public NodeBuilder width(double width) {
      Asserts.illegalArgument(
          width < 0,
          "width (" + width + ") must be > 0"
      );
      nodeAttrs.width = width * Graphviz.PIXLE;
      return this;
    }

    public NodeBuilder shape(NodeShape shape) {
      Asserts.nullArgument(shape, "shape");
      nodeAttrs.shape = shape;
      return this;
    }

    public NodeBuilder color(Color color) {
      Asserts.nullArgument(color, "color");
      nodeAttrs.color = new FusionColor(color);
      return this;
    }

    public NodeBuilder fillColor(Color fillColor) {
      Asserts.nullArgument(fillColor, "fillColor");
      nodeAttrs.fillColor = new FusionColor(fillColor);
      return this;
    }

    public NodeBuilder fontColor(Color fontColor) {
      Asserts.nullArgument(fontColor, "fontColor");
      nodeAttrs.fontColor = fontColor;
      return this;
    }

    public NodeBuilder label(String label) {
      nodeAttrs.label = label;
      return this;
    }

    public NodeBuilder labelloc(Labelloc labelloc) {
      Asserts.nullArgument(labelloc, "labelloc");
      nodeAttrs.labelloc = labelloc;
      return this;
    }

    public NodeBuilder margin(double margin) {
      return margin(margin, margin);
    }

    public NodeBuilder margin(double horMargin, double verMargin) {
      Asserts.illegalArgument(
          horMargin < 0,
          "Horizontal margin (" + horMargin + ") must be > 0"
      );
      Asserts.illegalArgument(
          verMargin < 0,
          "Vertical margin (" + verMargin + ") must be > 0"
      );
      nodeAttrs.margin = new FlatPoint(verMargin, horMargin);
      return this;
    }

    public NodeBuilder fixedSize(boolean fixedSize) {
      nodeAttrs.fixedSize = fixedSize;
      return this;
    }

    public NodeBuilder fontSize(double fontSize) {
      Asserts.illegalArgument(
          fontSize < 0,
          "fontSize (" + fontSize + ") must be > 0"
      );
      nodeAttrs.fontSize = fontSize;
      return this;
    }

    public NodeBuilder style(NodeStyle nodeStyle) {
      Asserts.nullArgument(nodeStyle, "nodeStyle");
      nodeAttrs.style = nodeStyle;
      return this;
    }

    public NodeBuilder side(int side) {
      Asserts.illegalArgument(side < 4, "side can not be lower than 4");
      Asserts.illegalArgument(side > 20, "side can not be large than 20");
      nodeAttrs.side = side;
      return this;
    }

    public Node build() {
      return new Node(nodeAttrs.clone());
    }
  }

}
