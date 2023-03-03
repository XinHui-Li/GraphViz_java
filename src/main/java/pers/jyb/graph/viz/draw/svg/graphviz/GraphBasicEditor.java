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

package pers.jyb.graph.viz.draw.svg.graphviz;

import org.w3c.dom.Element;
import pers.jyb.graph.viz.api.GraphAttrs;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.draw.GraphEditor;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgDrawBoard;
import pers.jyb.graph.viz.draw.svg.SvgEditor;

public class GraphBasicEditor extends SvgEditor<GraphvizDrawProp, SvgBrush>
    implements GraphEditor<SvgBrush> {

  @Override
  public boolean edit(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
    GraphAttrs graphAttrs = graphvizDrawProp.getGraphviz().graphAttrs();

    Element background = brush.getOrCreateChildElementById(
        SvgBrush.getId(SvgDrawBoard.GRAPH_ROOT, POLYGON_ELE), POLYGON_ELE
    );

    double leftBorder = graphvizDrawProp.getLeftBorder();
    double rightBorder = graphvizDrawProp.getRightBorder();
    double topBorder = graphvizDrawProp.getUpBorder();
    double bottomBorder = graphvizDrawProp.getDownBorder();
    String points = leftBorder + COMMA + topBorder + SPACE
        + rightBorder + COMMA + topBorder + SPACE
        + rightBorder + COMMA + bottomBorder + SPACE
        + leftBorder + COMMA + bottomBorder + SPACE
        + leftBorder + COMMA + topBorder;

    // Set back group color
    setBgColor(graphAttrs, background);
    background.setAttribute(POINTS, points);
    return true;
  }

  private void setBgColor(GraphAttrs graphAttrs, Element background) {
    FusionColor bgColor = graphAttrs.getBgColor();
    if (bgColor == null) {
      setDefaultBgColor(background);
    } else {
      Color color = bgColor.getColor();
      if (color != null) {
        background.setAttribute(FILL, color.value());
      }
    }
  }

  private void setDefaultBgColor(Element background) {
    background.setAttribute(FILL, Color.WHITE.value());
  }
}
