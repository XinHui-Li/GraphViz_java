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

import java.util.function.Consumer;
import org.w3c.dom.Element;
import pers.jyb.graph.util.StringUtils;
import pers.jyb.graph.viz.api.GraphAttrs;
import pers.jyb.graph.viz.draw.GraphEditor;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgDrawBoard;
import pers.jyb.graph.viz.draw.svg.SvgEditor;

public class GraphLabelEditor extends SvgEditor<GraphvizDrawProp, SvgBrush>
    implements GraphEditor<SvgBrush> {

  @Override
  public boolean edit(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
    GraphAttrs graphAttrs = graphvizDrawProp.getGraphviz().graphAttrs();
    String label = graphAttrs.getLabel();
    if (StringUtils.isEmpty(label)) {
      return true;
    }

    double fontSize = graphAttrs.getFontSize();
    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      Element text = brush.getOrCreateChildElementById(
          SvgBrush.getId(SvgDrawBoard.GRAPH_ROOT, SvgConstants.TEXT_ELE)
              + SvgConstants.UNDERSCORE + textLineAttribute.getLineNo(),
          SvgConstants.TEXT_ELE
      );
      setText(text, fontSize, textLineAttribute);
      text.setTextContent(textLineAttribute.getLine());
    };

    text(new TextAttribute(graphvizDrawProp.getLabelCenter(), fontSize, label,
                           graphAttrs.getFontColor(), lineConsumer));
    return true;
  }
}
