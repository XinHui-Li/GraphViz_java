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
import pers.jyb.graph.viz.api.attributes.LineStyle;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class LineStyleEditor implements LineEditor<SvgBrush> {

  @Override
  public boolean edit(LineDrawProp line, SvgBrush brush) {
    LineStyle style = line.lineAttrs().getStyle();
    if (style == null) {
      return true;
    }

    if (style == LineStyle.INVIS) {
      brush.drawBoard().removeLine(line.getLine());
      return false;
    }

    Element pathEle = brush.getOrCreateChildElementById(
        SvgBrush.getId(
            SvgEditors.lineId(line, brush),
            SvgConstants.PATH_ELE
        ),
        SvgConstants.PATH_ELE
    );

    if (style == LineStyle.DASHED) {
      dashed(pathEle);
      return true;
    }

    if (style == LineStyle.DOTTED) {
      dotted(pathEle);
      return true;
    }

    if (style == LineStyle.BOLD) {
      bold(pathEle);
      return true;
    }

    return true;
  }

  private void dashed(Element pathEle) {
    pathEle.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    pathEle.setAttribute(SvgConstants.STROKE_DASHARRAY, "5,2");
  }

  private void dotted(Element pathEle) {
    pathEle.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    pathEle.setAttribute(SvgConstants.STROKE_DASHARRAY, "1,5");
  }

  private void bold(Element shape) {
    shape.setAttribute(SvgConstants.STROKE_WIDTH, "2");
  }
}
