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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.Point;
import pers.jyb.graph.viz.layout.OrthoVisGraph.Segment;

public class GraphvizDrawProp extends ContainerDrawProp implements Serializable {

  private static final long serialVersionUID = 4820693703994091283L;

  private Graphviz graphviz;

  private List<Segment> grid;

  public GraphvizDrawProp(Graphviz graphviz) {
    Asserts.nullArgument(graphviz, "graphviz");
    this.graphviz = graphviz;
  }

  public Graphviz getGraphviz() {
    return graphviz;
  }

  public void setGraphviz(Graphviz graphviz) {
    this.graphviz = graphviz;
  }

  @Override
  protected Labelloc labelloc() {
    return graphviz.graphAttrs().getLabelloc();
  }

  @Override
  protected Point margin() {
    return graphviz.graphAttrs().getMargin();
  }

  @Override
  protected String containerId() {
    return graphviz.id();
  }

  public List<Segment> getGrid() {
    return grid;
  }

  public void addSegment(Segment segment) {
    if (segment == null) {
      return;
    }

    if (grid == null) {
      grid = new ArrayList<>();
    }
    grid.add(segment);
  }
}
