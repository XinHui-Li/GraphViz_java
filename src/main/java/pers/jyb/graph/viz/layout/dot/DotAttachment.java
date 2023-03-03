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

package pers.jyb.graph.viz.layout.dot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pers.jyb.graph.def.BiConcatIterable;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.Cluster;
import pers.jyb.graph.viz.api.GraphContainer;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.Subgraph;
import pers.jyb.graph.viz.api.attributes.Port;
import pers.jyb.graph.viz.api.ext.ShapePosition;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.layout.FlipShifterStrategy;

class DotAttachment {

  private final DotDigraph dotDigraph;

  private final Map<Node, DNode> nodeRecord;

  private final DrawGraph drawGraph;

  private boolean haveClusters;

  private boolean haveSubgraphs;

  private List<DLine> labelLines;

  private DotLineClip lineClip;

  private SameRankAdjacentRecord sameRankAdjacentRecord;

  public DotAttachment(DotDigraph dotDigraph, DrawGraph drawGraph, Map<Node, DNode> nodeRecord) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.dotDigraph = dotDigraph;
    this.drawGraph = drawGraph;
    this.nodeRecord = nodeRecord;
  }

  void initLineClip() {
    lineClip = new DotLineClip(drawGraph, dotDigraph);
  }

  void clipAllLines() {
    if (lineClip != null) {
      lineClip.clipAllLines();
    }
  }

  Iterable<DNode> nodes(GraphContainer graphContainer) {
    return dotDigraph.nodes(graphContainer);
  }

  Iterable<Line> lines(GraphContainer graphContainer) {
    return dotDigraph.lines(graphContainer);
  }

  DotDigraph getDotDigraph() {
    return dotDigraph;
  }

  DrawGraph getDrawGraph() {
    return drawGraph;
  }

  Graphviz getGraphviz() {
    return drawGraph.getGraphviz();
  }

  DNode get(Node node) {
    return nodeRecord.get(node);
  }

  public SameRankAdjacentRecord getSameRankAdjacentRecord() {
    return sameRankAdjacentRecord;
  }

  public void setSameRankAdjacentRecord(
      SameRankAdjacentRecord sameRankAdjacentRecord) {
    this.sameRankAdjacentRecord = sameRankAdjacentRecord;
  }

  DNode mappingToDNode(Node node) {
    return new DNode(
        node,
        drawGraph.width(node),
        drawGraph.height(node),
        drawGraph.getGraphviz().graphAttrs().getNodeSep()
    );
  }

  List<DLine> getLabelLines() {
    return CollectionUtils.isEmpty(labelLines) ? Collections.emptyList() : labelLines;
  }

  void addNode(DNode node) {
    dotDigraph.add(node);
  }

  void addEdge(DLine line) {
    dotDigraph.addEdge(line);

    if (line.haveLabel()) {
      if (labelLines == null) {
        labelLines = new ArrayList<>(2);
      }

      labelLines.add(line);
    }
  }

  void put(Node node, DNode dNode) {
    nodeRecord.put(node, dNode);
  }

  void markHaveCluster() {
    this.haveClusters = true;
  }

  void markHaveSubgraph() {
    this.haveSubgraphs = true;
  }

  GraphContainer breakAncestryContinuesContainer(GraphContainer graphContainer) {
    if (graphContainer == null) {
      return null;
    }

    GraphContainer container = graphContainer;
    while (container != null && !container.isSubgraph() && !container.isTransparent()) {
      container = getGraphviz().father(graphContainer);
    }

    return container;
  }

  boolean haveClusters() {
    return haveClusters;
  }

  boolean haveSubgraphs() {
    return haveSubgraphs;
  }

  boolean notContain(GraphContainer father, GraphContainer container) {
    return notContain(drawGraph.getGraphviz(), father, container);
  }

  GraphContainer commonParent(DNode v, DNode w) {
    return commonParent(getGraphviz(), v, w);
  }

  GraphContainer clusterDirectContainer(GraphContainer parent, DNode node) {
    if (node.getContainer() == parent) {
      return null;
    }

    Graphviz graphviz = getGraphviz();
    GraphContainer father;
    GraphContainer current = node.getContainer();
    while ((father = graphviz.effectiveFather(current)) != parent && father != null) {
      current = father;
    }

    return father == parent ? current : null;
  }

  static Iterable<Cluster> clusters(GraphContainer container) {
    List<Iterable<Cluster>> iterables = null;

    for (Subgraph subgraph : container.subgraphs()) {
      if (!subgraph.isTransparent()) {
        continue;
      }

      Iterable<Cluster> clusters = clusters(subgraph);
      if (clusters == null) {
        continue;
      }
      if (iterables == null) {
        iterables = new ArrayList<>(2);
      }
      iterables.add(clusters);
    }

    if (iterables == null) {
      return container.clusters();
    }

    iterables.add(container.clusters());
    return new BiConcatIterable<>(iterables);
  }

  static boolean notContain(Graphviz graphviz, GraphContainer father, GraphContainer container) {
    if (father == null || container == null) {
      return true;
    }

    GraphContainer p = container;
    while (p != father && p != null) {
      p = graphviz.father(p);
    }
    return p == null;
  }


  /**
   * 向上查找包含两个节点的在指定的根容器当中的第一个公共的父容器。
   *
   * @param graphviz 根容器
   * @param n        节点
   * @param w        节点
   * @return 两个节点的第一个公共的父容器
   */
  static GraphContainer commonParent(Graphviz graphviz, DNode n, DNode w) {
    GraphContainer c1 = n.getContainer();
    GraphContainer c2 = w.getContainer();

    return commonParent(graphviz, c1, c2);
  }

  /**
   * 向上查找包含两个容器在指定的根容器当中的第一个公共的父容器。
   *
   * @param graphviz 根容器
   * @param c1       容器1
   * @param c2       容器2
   * @return 两个容器的第一个公共的父容器
   */
  static GraphContainer commonParent(Graphviz graphviz,
                                     GraphContainer c1,
                                     GraphContainer c2) {
    if (c1 == c2) {
      return c1;
    }

    if (graphviz.effectiveFather(c1) == c2) {
      return c2;
    }

    if (graphviz.effectiveFather(c2) == c1) {
      return c1;
    }

    GraphContainer t;
    GraphContainer tn = c1;
    GraphContainer tw = c2;
    Map<GraphContainer, GraphContainer> path = new HashMap<>(4);
    while (c1 != null || c2 != null) {
      if (c1 != null) {
        t = path.get(c1);
        if (t != null && t == tw) {
          return c1;
        }

        path.put(c1, tn);
        c1 = graphviz.effectiveFather(c1);
      }

      if (c2 != null) {
        t = path.get(c2);
        if (t != null && t == tn) {
          return c2;
        }

        path.put(c2, tw);
        c2 = graphviz.effectiveFather(c2);
      }
    }

    return c1;
  }

  public double nodeHorPortOffset(Node n, Line line, ShapePosition nodePosition) {
    return nodeHorPortOffset(n, line, drawGraph, nodePosition);
  }

  public static double nodeHorPortOffset(Node n, Line line, DrawGraph drawGraph,
                                         ShapePosition nodePosition) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    if (lineDrawProp == null) {
      return 0;
    }

    Port tailPort = null;
    Port headPort = null;
    if (n == line.tail()) {
      tailPort = lineDrawProp.lineAttrs().getTailPort();
      tailPort = FlipShifterStrategy.movePort(drawGraph, tailPort);
    }
    if (n == line.head()) {
      headPort = lineDrawProp.lineAttrs().getHeadPort();
      headPort = FlipShifterStrategy.movePort(drawGraph, headPort);
    }

    return drawGraph.nodeHorPortOffset(n, line, tailPort, headPort, nodePosition);
  }

  class DotLineClip extends LineClip {

    DotLineClip(DrawGraph drawGraph, DotDigraph dotDigraph) {
      this.drawGraph = drawGraph;
      this.dotDigraph = dotDigraph;
    }

    private void clipAllLines() {
      drawGraph.syncGraphvizBorder();

      for (LineDrawProp line : drawGraph.lines()) {
        PathClip<LineDrawProp> pathClip;
        if (line.isBesselCurve()) {
          pathClip = new CurvePathClip();
        } else {
          pathClip = new LineDrawPropPathClip();
        }

        clipProcess(line, pathClip, line);
        if (CollectionUtils.isEmpty(line)) {
          continue;
        }

        line.setStart(line.get(0));
        line.setEnd(line.get(line.size() - 1));
        setFloatLabel(line);
      }

      drawGraph.syncToGraphvizBorder();
    }
  }
}
