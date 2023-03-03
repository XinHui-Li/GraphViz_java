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

package pers.jyb.graph.viz.layout.dot;

import java.util.Objects;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.Port;
import pers.jyb.graph.viz.api.ext.ShapePosition;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.draw.Rectangle;
import pers.jyb.graph.viz.layout.FlipShifterStrategy;

public class PortHelper {

  private PortHelper() {
  }

  public static Port getLineEndPointPort(Node node, Line line, DrawGraph drawGraph) {
    return getLineEndPointPort(node, line, drawGraph, true);
  }

  public static Port getLineEndPointPort(Node node, Line line, DrawGraph drawGraph,
                                         boolean needMove) {
    if (node == null || line == null || drawGraph == null) {
      return null;
    }

    LineAttrs lineAttrs = drawGraph.lineAttrs(line);
    Asserts.illegalArgument(lineAttrs == null, "can not find lineAttrs");
    if (node == line.tail()) {
      if (needMove) {
        return FlipShifterStrategy.movePort(drawGraph, lineAttrs.getTailPort());
      }

      return lineAttrs.getTailPort();
    }
    if (node == line.head()) {
      if (needMove) {
        return FlipShifterStrategy.movePort(drawGraph, lineAttrs.getHeadPort());
      }

      return lineAttrs.getHeadPort();
    }
    return null;
  }

  public static FlatPoint getPortPoint(Line line, DNode node, DrawGraph drawGraph) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(line, "line");
    Asserts.nullArgument(drawGraph, "drawGraph");
    Port port = PortHelper.getLineEndPointPort(node.getNode(), line, drawGraph, false);

    String labelPort = null;
    LineAttrs lineAttrs = drawGraph.lineAttrs(line);
    if (node.getNode() == line.tail()) {
      labelPort = lineAttrs.getTailLabelPort();
    } else if (node.getNode() == line.head()) {
      labelPort = lineAttrs.getHeadLabelPort();
    }

    return endPoint(labelPort, port, node.getNode(), drawGraph, node);
  }

  public static FlatPoint endPoint(String labelPort, Port port, Node node,
                                   DrawGraph drawGraph, ShapePosition shapePosition) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(shapePosition, "shapePosition");
    if (port == null) {
      return new FlatPoint(shapePosition.getX(), shapePosition.getY());
    }

    Rectangle rectangle = new Rectangle();
    rectangle.setUpBorder(shapePosition.getUpBorder());
    rectangle.setDownBorder(shapePosition.getDownBorder());
    rectangle.setLeftBorder(shapePosition.getLeftBorder());
    rectangle.setRightBorder(shapePosition.getRightBorder());

    FlipShifterStrategy.moveRectangle(drawGraph.rankdir(), rectangle);

    FlatPoint portPoint = new FlatPoint(
        rectangle.getX() + port.horOffset(rectangle),
        rectangle.getY() + port.verOffset(rectangle)
    );
    if (Objects.equals(portPoint.getX(), rectangle.getX())
        && Objects.equals(portPoint.getY(), rectangle.getY())) {
      return portPoint;
    }

    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node);
    Asserts.nullArgument(nodeDrawProp, "nodeDrawProp");
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();
    if (nodeShape.in(rectangle, portPoint)) {
      FlipShifterStrategy.movePointOpposite(drawGraph.rankdir(), shapePosition, portPoint);
      return portPoint;
    }

    double leftWidth = nodeShape.leftWidth(rectangle.getWidth());
    double topHeight = nodeShape.topHeight(rectangle.getHeight());
    FlatPoint center = new FlatPoint(rectangle.getLeftBorder() + leftWidth,
                                     rectangle.getUpBorder() + topHeight);

    FlatPoint p = AbstractLineRouter.straightLineClipShape(rectangle, nodeShape, center, portPoint);

    FlipShifterStrategy.movePointOpposite(drawGraph.rankdir(), shapePosition, p);
    return p;
  }

  public static FlatPoint notFlipEndPoint(String labelPort, Port port, NodeShape nodeShape,
                                          DrawGraph drawGraph, ShapePosition shapePosition) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    if (port == null) {
      return new FlatPoint(shapePosition.getX(), shapePosition.getY());
    }

    FlatPoint portPoint = new FlatPoint(
        shapePosition.getX() + port.horOffset(shapePosition),
        shapePosition.getY() + port.verOffset(shapePosition)
    );
    if (Objects.equals(portPoint.getX(), shapePosition.getX())
        && Objects.equals(portPoint.getY(), shapePosition.getY())) {
      return portPoint;
    }

    if (nodeShape.in(shapePosition, portPoint)) {
      return portPoint;
    }

    FlatPoint point = new FlatPoint(shapePosition.getX(),
                                    shapePosition.getY());
    return AbstractLineRouter.straightLineClipShape(shapePosition, point, portPoint);
  }

  public static int portCompare(Port p1, Port p2) {
    return Integer.compare(crossPortNo(p1), crossPortNo(p2));
  }

  public static int crossPortNo(Port port) {
    if (port == Port.SOUTH_WEST) {
      return Port.NORTH_WEST.getNo();
    }
    if (port == Port.SOUTH) {
      return Port.NORTH.getNo();
    }
    if (port == Port.SOUTH_EAST) {
      return Port.SOUTH_EAST.getNo();
    }
    return port != null ? port.getNo() : Port.NORTH.getNo();
  }
}
