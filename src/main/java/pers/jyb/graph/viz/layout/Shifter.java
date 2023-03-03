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

import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;

public interface Shifter {

  void graph(GraphvizDrawProp graphvizDrawProp);

  void cluster(ClusterDrawProp clusterDrawProp);

  void node(NodeDrawProp nodeDrawProp);

  void line(LineDrawProp lineDrawProp);
}
