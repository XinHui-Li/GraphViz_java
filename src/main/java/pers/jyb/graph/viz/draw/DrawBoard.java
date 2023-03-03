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

package pers.jyb.graph.viz.draw;

import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.Node;

public interface DrawBoard<
    N extends Brush,
    L extends Brush,
    C extends Brush,
    G extends Brush> {

  G drawGraph(GraphvizDrawProp graphvizDrawProp);

  C drawCluster(ClusterDrawProp cluster);

  N drawNode(NodeDrawProp nodeDrawProp);

  L drawLine(LineDrawProp line);

  boolean removeNode(Node node);

  boolean removeLine(Line line);

  GraphResource graphResource() throws FailInitResouceException;
}
