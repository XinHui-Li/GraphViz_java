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

import java.util.Map.Entry;
import java.util.function.Consumer;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.viz.api.FloatLabel;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class LineFloatLabelsEditor extends SvgEditor<LineDrawProp, SvgBrush>
    implements LineEditor<SvgBrush> {

  private static final String FLOAT_LABEL = "float_label";

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    int i = 0;
    for (Entry<FloatLabel, FlatPoint> flatPointEntry : lineDrawProp
        .getFloatLabelFlatCenters().entrySet()) {

      FloatLabel floatLabel = flatPointEntry.getKey();
      FlatPoint flatPointCenter = flatPointEntry.getValue();

      final int n = i;
      Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
        String id = SvgBrush.getId(
            SvgEditors.lineId(lineDrawProp, brush),
            SvgConstants.TEXT_ELE
                + SvgConstants.UNDERSCORE + FLOAT_LABEL
                + n + textLineAttribute.getLineNo()
        );

        Element text = brush.getOrCreateChildElementById(id, SvgConstants.TEXT_ELE);
        setText(text, floatLabel.getFontSize(), textLineAttribute);

        text.setTextContent(textLineAttribute.getLine());
      };

      text(new TextAttribute(flatPointCenter, floatLabel.getFontSize(), floatLabel.getLabel(),
                             lineDrawProp.lineAttrs().getFontColor(), lineConsumer));
      i++;
    }

    return true;
  }
}
