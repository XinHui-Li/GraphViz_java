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

import java.util.Optional;
import java.util.function.Consumer;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.StringUtils;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class LineLabelEditor extends SvgEditor<LineDrawProp, SvgBrush>
    implements LineEditor<SvgBrush> {

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    String label = lineDrawProp.lineAttrs().getLabel();

    if (StringUtils.isEmpty(label) || lineDrawProp.getLabelCenter() == null) {
      return true;
    }

    double fontSize = Optional.ofNullable(lineDrawProp.lineAttrs().getFontSize())
        .orElse(0D);

    FlatPoint labelCenter = lineDrawProp.getLabelCenter();

    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      String id = SvgBrush.getId(
          SvgEditors.lineId(lineDrawProp, brush),
          SvgConstants.TEXT_ELE + SvgConstants.UNDERSCORE + textLineAttribute.getLineNo()
      );

      Element text = brush.getOrCreateChildElementById(id, SvgConstants.TEXT_ELE);
      setText(text, fontSize, textLineAttribute);

      text.setTextContent(textLineAttribute.getLine());
    };

    text(new TextAttribute(labelCenter, fontSize, label,
                           lineDrawProp.lineAttrs().getFontColor(), lineConsumer));
    return true;
  }
}
