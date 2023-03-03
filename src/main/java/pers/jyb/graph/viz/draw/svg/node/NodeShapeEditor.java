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

package pers.jyb.graph.viz.draw.svg.node;

import static pers.jyb.graph.viz.api.ext.CylinderPropCalc.TOP_LEN;
import static pers.jyb.graph.viz.api.ext.NotePropCalc.RIGHT_UP_LEN;

import java.util.Arrays;
import org.w3c.dom.Element;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.api.ext.StarPropCalc;
import pers.jyb.graph.viz.draw.CustomizeShapeRender;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class NodeShapeEditor extends AbstractNodeShapeEditor {

  @Override
  public boolean edit(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();

    CustomizeShapeRender customizeShapeRender = CustomizeShapeRender
        .getCustomizeShapeRender(nodeShape.getName());
    if (customizeShapeRender != null) {
      customizeShapeRender.drawSvg(brush, nodeDrawProp);
    } else if (nodeShape instanceof NodeShapeEnum) {
      drawNodeShapeEnum(nodeDrawProp, (NodeShapeEnum) nodeShape, brush);
    }
    return true;
  }

  private void drawNodeShapeEnum(NodeDrawProp nodeDrawProp,
                                 NodeShapeEnum nodeShape, SvgBrush brush) {
    switch (nodeShape) {
      case ELLIPSE:
        ellipse(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case CIRCLE:
      case POINT:
        circle(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case UNDERLINE:
        underline(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case RECT:
        rect(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case TRIANGLE:
        triangle(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case DIAMOND:
        diamond(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case TRAPEZIUM:
        trapezium(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case PARALLELOGRAM:
        parallelogram(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case STAR:
        start(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
      case NOTE:
        note(nodeDrawProp, brush);
        break;
      case CYLINDER:
        cylinder(nodeDrawProp, brush);
        break;
      default:
        circle(nodeDrawProp, signleElement(nodeDrawProp, brush));
        break;
    }
  }

  private Element signleElement(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    return SvgEditors.getShapeElement(nodeDrawProp, brush, getShapeElement(nodeDrawProp));
  }

  private void ellipse(NodeDrawProp nodeDrawProp, Element shapeElement) {
    ellipse(nodeDrawProp, shapeElement, nodeDrawProp.getHeight() / 2, nodeDrawProp.getWidth() / 2);
  }

  private void circle(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double radius = nodeDrawProp.getHeight() / 2;
    ellipse(nodeDrawProp, shapeElement, radius, radius);
  }

  private void ellipse(NodeDrawProp nodeDrawProp,
                       Element shapeElement,
                       double height,
                       double width) {
    double x = nodeDrawProp.getX();
    double y = nodeDrawProp.getY();
    shapeElement.setAttribute(CX, String.valueOf(x));
    shapeElement.setAttribute(CY, String.valueOf(y));
    shapeElement.setAttribute(RX, String.valueOf(width));
    shapeElement.setAttribute(RY, String.valueOf(height));
  }

  private void underline(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(POINTS, points);
  }

  private void rect(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = generateBox(nodeDrawProp);
    shapeElement.setAttribute(POINTS, points);
  }

  private void triangle(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = generatePolylinePoints(nodeDrawProp.getX(),
                                           nodeDrawProp.getUpBorder(),
                                           nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(POINTS, points);
  }

  private void diamond(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getY(),
                                           nodeDrawProp.getX(),
                                           nodeDrawProp.getUpBorder(),
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getY(),
                                           nodeDrawProp.getX(),
                                           nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getY());
    shapeElement.setAttribute(POINTS, points);
  }

  private void trapezium(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double leftTopX = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 4;
    double rightTopX = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 4;
    String points = generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           leftTopX, nodeDrawProp.getUpBorder(),
                                           rightTopX, nodeDrawProp.getUpBorder(),
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(POINTS, points);
  }

  private void parallelogram(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double leftTopX = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 5;
    double rightDownX = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 5;
    String points = generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           leftTopX, nodeDrawProp.getUpBorder(),
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getUpBorder(),
                                           rightDownX, nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(POINTS, points);
  }

  private void start(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double outerRadius = nodeDrawProp.getHeight() / 2;
    double innerRadius = outerRadius / StarPropCalc.IN_OUT_RATIO;

    double arc = StarPropCalc.START_ARC;
    double[] ps = new double[22];
    for (int i = 0; i < 10; i++) {
      if (i % 2 == 0) {
        ps[i * 2] = nodeDrawProp.getX() + Math.cos(arc) * outerRadius;
        ps[i * 2 + 1] = nodeDrawProp.getY() - Math.sin(arc) * outerRadius;
      } else {
        ps[i * 2] = nodeDrawProp.getX() + Math.cos(arc) * innerRadius;
        ps[i * 2 + 1] = nodeDrawProp.getY() - Math.sin(arc) * innerRadius;
      }
      ps[20] = ps[0];
      ps[21] = ps[1];

      arc += StarPropCalc.UNIT_ARC;
    }

    shapeElement.setAttribute(POINTS, generatePolylinePoints(ps));
  }

  private void note(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    String nodeId = SvgEditors.nodeId(nodeDrawProp.getNode(), brush);
    SvgEditors.getShapeElement(nodeDrawProp, brush, getShapeElement(nodeDrawProp));
    String shape = NodeShapeEnum.NOTE.getName();

    Element firstEle = brush.getOrCreateChildElementById(nodeId + shape + "0", POLYGON_ELE);
    String points = generatePolylinePoints(nodeDrawProp.getLeftBorder(), nodeDrawProp.getUpBorder(),
                                           nodeDrawProp.getRightBorder() - RIGHT_UP_LEN,
                                           nodeDrawProp.getUpBorder(),
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getUpBorder() + RIGHT_UP_LEN,
                                           nodeDrawProp.getRightBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getDownBorder(),
                                           nodeDrawProp.getLeftBorder(),
                                           nodeDrawProp.getUpBorder());
    firstEle.setAttribute(POINTS, points);

    Element secondEle = brush.getOrCreateChildElementById(nodeId + shape + "1", POLYGON_ELE);
    points = generatePolylinePoints(nodeDrawProp.getRightBorder() - RIGHT_UP_LEN,
                                    nodeDrawProp.getUpBorder(),
                                    nodeDrawProp.getRightBorder() - RIGHT_UP_LEN,
                                    nodeDrawProp.getUpBorder() + RIGHT_UP_LEN,
                                    nodeDrawProp.getRightBorder(),
                                    nodeDrawProp.getUpBorder() + RIGHT_UP_LEN);
    secondEle.setAttribute(POINTS, points);

    brush.addGroup(SHAPE_GROUP_KEY, Arrays.asList(firstEle, secondEle));
  }

  private void cylinder(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    String nodeId = SvgEditors.nodeId(nodeDrawProp.getNode(), brush);
    SvgEditors.getShapeElement(nodeDrawProp, brush, getShapeElement(nodeDrawProp));
    String shape = NodeShapeEnum.CYLINDER.getName();

    StringBuilder sb = new StringBuilder(PATH_START_M);
    double up = nodeDrawProp.getUpBorder() + TOP_LEN;
    double down = nodeDrawProp.getDownBorder() - TOP_LEN;
    double v2x = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 4;
    double v3x = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 4;
    sb.append(nodeDrawProp.getLeftBorder()).append(COMMA).append(up).append(CURVE_PATH_MARK)
        .append(nodeDrawProp.getLeftBorder()).append(COMMA).append(up).append(SPACE)
        .append(nodeDrawProp.getLeftBorder()).append(COMMA).append(down).append(SPACE)
        .append(nodeDrawProp.getLeftBorder()).append(COMMA).append(down).append(SPACE)
        .append(v2x).append(COMMA).append(nodeDrawProp.getDownBorder()).append(SPACE)
        .append(v3x).append(COMMA).append(nodeDrawProp.getDownBorder()).append(SPACE)
        .append(nodeDrawProp.getRightBorder()).append(COMMA).append(down).append(SPACE)
        .append(nodeDrawProp.getRightBorder()).append(COMMA).append(down).append(SPACE)
        .append(nodeDrawProp.getRightBorder()).append(COMMA).append(up).append(SPACE)
        .append(nodeDrawProp.getRightBorder()).append(COMMA).append(up).append(SPACE)
        .append(v3x).append(COMMA).append(nodeDrawProp.getUpBorder()).append(SPACE)
        .append(v2x).append(COMMA).append(nodeDrawProp.getUpBorder()).append(SPACE)
        .append(nodeDrawProp.getLeftBorder()).append(COMMA).append(up);
    Element firstEle = brush.getOrCreateChildElementById(nodeId + shape + "0", PATH_ELE);
    firstEle.setAttribute(D, sb.toString());

    sb = new StringBuilder(PATH_START_M);
    sb.append(nodeDrawProp.getLeftBorder()).append(COMMA).append(up).append(CURVE_PATH_MARK)
        .append(v2x).append(COMMA).append(up + TOP_LEN).append(SPACE)
        .append(v3x).append(COMMA).append(up + TOP_LEN).append(SPACE)
        .append(nodeDrawProp.getRightBorder()).append(COMMA).append(up);
    Element secondEle = brush.getOrCreateChildElementById(nodeId + shape + "1", PATH_ELE);
    secondEle.setAttribute(D, sb.toString());

    brush.addGroup(SHAPE_GROUP_KEY, Arrays.asList(firstEle, secondEle));
  }
}
