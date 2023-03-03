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

import org.w3c.dom.Element;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class NodeColorEditor extends AbstractNodeShapeEditor {

  @Override
  public boolean edit(NodeDrawProp node, SvgBrush brush) {
    NodeAttrs nodeAttrs = node.nodeAttrs();

    for (Element element : brush.getEleGroup(SHAPE_GROUP_KEY)) {
      fusionColorProcess(
          nodeAttrs.getColor(), color -> element.setAttribute(SvgConstants.STROKE, color.value()),
          null
      );

      FusionColor fillColor = nodeAttrs.getFillColor();
      if (fillColor == null && nodeAttrs.getNodeShape() != NodeShapeEnum.POINT) {
        continue;
      }
      fusionColorProcess(
          nodeAttrs.getFillColor(),
          SvgEditors.defaultColor(),
          color -> element.setAttribute(SvgConstants.FILL, color.value()),
          null
      );
    }

    return true;
  }
}
