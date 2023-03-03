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

import java.util.Optional;
import java.util.function.Consumer;
import org.w3c.dom.Element;
import pers.jyb.graph.util.StringUtils;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.draw.NodeEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class NodeLabelEditor extends SvgEditor<NodeDrawProp, SvgBrush> implements
    NodeEditor<SvgBrush> {

  @Override
  public boolean edit(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    NodeAttrs nodeAttrs = nodeDrawProp.nodeAttrs();
    String label = nodeAttrs.getLabel();

    if (StringUtils.isEmpty(label) || nodeAttrs.getNodeShape() == NodeShapeEnum.POINT) {
      return true;
    }

    double fontSize = Optional.ofNullable(nodeAttrs.getFontSize()).orElse(0D);

    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      String id = SvgBrush.getId(
          SvgEditors.nodeId(nodeDrawProp.getNode(), brush),
          SvgConstants.TEXT_ELE + SvgConstants.UNDERSCORE + textLineAttribute.getLineNo()
      );

      Element text = brush.getOrCreateChildElementById(id, SvgConstants.TEXT_ELE);
      setText(text, fontSize, textLineAttribute);

      text.setTextContent(textLineAttribute.getLine());
    };

    text(new TextAttribute(nodeDrawProp.getLabelCenter(), fontSize, label,
                           nodeAttrs.getFontColor(), lineConsumer));
    return nodeDrawProp.nodeAttrs().getNodeShape() != NodeShapeEnum.PLAIN_TEXT;
  }
}
