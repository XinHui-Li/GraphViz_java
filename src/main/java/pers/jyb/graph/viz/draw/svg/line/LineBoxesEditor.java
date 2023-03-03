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

import org.w3c.dom.Element;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditors;
import pers.jyb.graph.viz.layout.dot.RouterBox;

public class LineBoxesEditor implements LineEditor<SvgBrush> {

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    if (CollectionUtils.isEmpty(lineDrawProp.getBoxes())) {
      return true;
    }

    for (int i = 0; i < lineDrawProp.getBoxes().size(); i++) {
      RouterBox routerBox = lineDrawProp.getBoxes().get(i);

      Element boxEle = brush.getOrCreateChildElementById(
          SvgBrush.getId(
              SvgEditors.lineId(lineDrawProp, brush),
              SvgConstants.POLYGON_ELE
          ) + SvgConstants.UNDERSCORE + "routerBox" + SvgConstants.UNDERSCORE + i,
          SvgConstants.POLYGON_ELE
      );

      String points = routerBox.getLeftBorder() + SvgConstants.COMMA + routerBox.getUpBorder()
          + SvgConstants.SPACE
          + routerBox.getRightBorder() + SvgConstants.COMMA + routerBox.getUpBorder()
          + SvgConstants.SPACE
          + routerBox.getRightBorder() + SvgConstants.COMMA + routerBox.getDownBorder()
          + SvgConstants.SPACE
          + routerBox.getLeftBorder() + SvgConstants.COMMA + routerBox.getDownBorder()
          + SvgConstants.SPACE
          + routerBox.getLeftBorder() + SvgConstants.COMMA + routerBox.getUpBorder();

      boxEle.setAttribute(SvgConstants.POINTS, points);
      boxEle.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
      boxEle.setAttribute(SvgConstants.STROKE, Color.BLUE.value());
    }

    return true;
  }
}
