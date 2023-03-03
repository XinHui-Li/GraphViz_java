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

package pers.jyb.graph.viz.draw.svg;

import static pers.jyb.graph.viz.draw.svg.SvgConstants.COMMA;
import static pers.jyb.graph.viz.draw.svg.SvgConstants.SPACE;

import java.util.List;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;

public class SvgEditors {

  private SvgEditors() {
  }

  public static Element getShapeElement(NodeDrawProp node, SvgBrush brush, String eleName) {
    String shapeId = SvgBrush.getId(nodeId(node.getNode(), brush), eleName);
    return brush.getOrCreateShapeEleById(shapeId, eleName);
  }

  public static Color defaultColor() {
    return Color.BLACK;
  }

  public static String nodeId(Node node, SvgBrush brush) {
    return brush.drawBoard().nodeId(node);
  }

  public static String lineId(LineDrawProp lineDrawProp, SvgBrush brush) {
    return brush.drawBoard().lineId(lineDrawProp);
  }

  public static String generatePolylinePoints(List<FlatPoint> positions) {
    Asserts.nullArgument(positions, "positions");
    Asserts.illegalArgument(CollectionUtils.isEmpty(positions),
                            "Wrong positions length, can not be empty and must be even");

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < positions.size(); i++) {
      FlatPoint p = positions.get(i);
      sb.append(p.getX()).append(COMMA).append(p.getY()).append(SPACE);
    }
    return sb.toString();
  }

  public static String generatePolylinePoints(double... positions) {
    Asserts.nullArgument(positions, "positions");
    Asserts.illegalArgument(positions.length == 0 || positions.length % 2 != 0,
                            "Wrong positions length, can not be empty and must be even");

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < positions.length; i += 2) {
      double horPos = positions[i];
      double verPos = positions[i + 1];
      sb.append(horPos).append(COMMA).append(verPos).append(SPACE);
    }
    return sb.toString();
  }

  public static String getPathPintStr(FlatPoint point) {
   return getPathPintStr(point, true);
  }

  public static String getPathPintStr(FlatPoint point, boolean needSpace) {
    Asserts.nullArgument(point, "point");
    String v = point.getX() + COMMA + point.getY();
    return needSpace ? v + SPACE : v;
  }
}