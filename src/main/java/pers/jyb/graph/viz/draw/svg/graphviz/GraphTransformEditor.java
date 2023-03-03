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

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.viz.api.GraphAttrs;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.draw.GraphEditor;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;

public class GraphTransformEditor implements GraphEditor<SvgBrush> {

  private static final String TRANSFORM_VAL = "scale(%s) rotate(%s)";

  @Override
  public boolean edit(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
    Graphviz graphviz = graphvizDrawProp.getGraphviz();
    GraphAttrs graphAttrs = graphviz.graphAttrs();

    String transform = TRANSFORM_VAL;
    FlatPoint scale = graphAttrs.getScale();
    if (scale == null) {
      transform = String.format(transform, "1 1", "%s");
    } else {
      String scaleStr = scale.getX() + " " + scale.getY();
      transform = String.format(transform, scaleStr, "%s");
    }

    transform = String.format(transform, "0");
    brush.setAttr(SvgConstants.TRANSFORM, transform);
    return true;
  }
}
