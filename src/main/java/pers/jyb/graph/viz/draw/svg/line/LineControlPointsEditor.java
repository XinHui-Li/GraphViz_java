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

package pers.jyb.graph.viz.draw.svg.line;

import java.util.Objects;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class LineControlPointsEditor implements LineEditor<SvgBrush> {

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    if (!Objects.equals(lineDrawProp.lineAttrs().getControlPoints(), Boolean.TRUE)) {
      return true;
    }

    for (int i = 0; i < lineDrawProp.size(); i++) {
      FlatPoint point = lineDrawProp.get(i);
      Element controlPointsEle = brush.getOrCreateChildElementById(
          SvgBrush.getId(
              SvgEditors.lineId(lineDrawProp, brush),
              SvgConstants.ELLIPSE_ELE
          ) + SvgConstants.UNDERSCORE + i,
          SvgConstants.ELLIPSE_ELE
      );

      controlPointsEle.setAttribute(SvgConstants.CX, String.valueOf(point.getX()));
      controlPointsEle.setAttribute(SvgConstants.CY, String.valueOf(point.getY()));
      controlPointsEle.setAttribute(SvgConstants.RX, "2");
      controlPointsEle.setAttribute(SvgConstants.RY, "2");
      controlPointsEle.setAttribute(SvgConstants.FILL, Color.RED.value());
    }
    return true;
  }
}
