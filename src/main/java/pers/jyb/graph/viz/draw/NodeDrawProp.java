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

import java.io.Serializable;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.Point;

public class NodeDrawProp extends ContainerDrawProp implements Serializable {

  private static final long serialVersionUID = 2785583326128769032L;

  private NodeAttrs nodeAttrs;

  private int id;

  private final Node node;

  private final Point margin;

  private double labelVerOffset;

  public NodeDrawProp(Node node, NodeAttrs nodeAttrs) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(node, "nodeAttrs");
    this.node = node;
    this.nodeAttrs = nodeAttrs;
    FlatPoint p = nodeAttrs.getMargin();
    if (p != null) {
      this.margin = new Point(p.getX(), p.getY());
    } else {
      this.margin = null;
    }
  }

  public Node getNode() {
    return node;
  }

  public void setNodeAttrs(NodeAttrs nodeAttrs) {
    Asserts.nullArgument(nodeAttrs, "nodeAttrs");
    this.nodeAttrs = nodeAttrs;
  }

  public NodeAttrs nodeAttrs() {
    return nodeAttrs;
  }

  public String id() {
    return String.valueOf(id);
  }

  public int nodeNo() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  protected Labelloc labelloc() {
    return nodeAttrs.getLabelloc();
  }

  @Override
  protected Point margin() {
    return margin;
  }

  @Override
  protected String containerId() {
    return String.valueOf(id);
  }

  @Override
  public NodeShape nodeShape() {
    return nodeAttrs.getNodeShape();
  }

  public double getLabelVerOffset() {
    return labelVerOffset;
  }

  public void setLabelVerOffset(double labelVerOffset) {
    this.labelVerOffset = labelVerOffset;
  }

}
