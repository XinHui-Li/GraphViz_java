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

package pers.jyb.graph.viz.layout;

import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.ArrowShape;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Dir;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;

class DefaultVal {

  private DefaultVal() {
  }

  static final NodeAttrs DEFAULT_NODE_ATTRS = Node
      .builder()
      .shape(NodeShapeEnum.ELLIPSE)
      .margin(5, 5)
      .labelloc(Labelloc.CENTER)
      .fontSize(14)
      .fontColor(Color.BLACK)
      .build()
      .nodeAttrs();

  static final LineAttrs DEFAULT_LINE_ATTRS = Line
      .builder(Node.builder().build(), Node.builder().build())
      .controlPoints(false)
      .showboxes(false)
      .radian(20)
      .arrowHead(ArrowShape.NORMAL)
      .arrowTail(ArrowShape.NORMAL)
      .headclip(true)
      .tailclip(true)
      .arrowSize(0.12D)
      .fontSize(14)
      .minlen(1)
      .weight(1)
      .dir(Dir.FOWARD)
      .build()
      .lineAttrs();
}
