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

import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.draw.NodeEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;

public abstract class AbstractNodeShapeEditor extends SvgEditor<NodeDrawProp, SvgBrush>
    implements NodeEditor<SvgBrush> {

  protected String getShapeElement(NodeDrawProp nodeDrawProp) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();

    if (nodeShape == NodeShapeEnum.CIRCLE
        || nodeShape == NodeShapeEnum.ELLIPSE
        || nodeShape == NodeShapeEnum.POINT) {
      return NodeShapeEnum.ELLIPSE.getName();
    }

    return SvgConstants.POLYGON_ELE;
  }
}
