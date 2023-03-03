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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.Cluster;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.attributes.Layout;
import pers.jyb.graph.viz.api.attributes.Port;
import pers.jyb.graph.viz.api.attributes.Rankdir;
import pers.jyb.graph.viz.api.attributes.Splines;
import pers.jyb.graph.viz.api.ext.ShapePosition;

public class DrawGraph extends Rectangle implements Serializable {

  private static final String NODE_ID_PREFIX = "node_";

  private static final long serialVersionUID = -571472079515096134L;

  private final GraphvizDrawProp graphvizDrawProp;

  private final Map<Node, NodeDrawProp> nodeDrawPropMap;

  private final Map<Line, LineDrawProp> lineDrawPropMap;

  private Map<Cluster, ClusterDrawProp> clusterDrawPropMap;

  public DrawGraph(Graphviz graphviz) {
    Asserts.nullArgument(graphviz, "graphviz");
    this.graphvizDrawProp = new GraphvizDrawProp(graphviz);
    this.nodeDrawPropMap = new HashMap<>(graphviz.nodeNum());
    if (graphviz.graphAttrs().getSplines() != Splines.NONE) {
      this.lineDrawPropMap = new HashMap<>(graphviz.lineNum());
    } else {
      this.lineDrawPropMap = null;
    }
  }

  public void nodePut(Node node, NodeDrawProp nodeDrawProp) {
    if (node == null || nodeDrawProp == null) {
      return;
    }
    nodeDrawPropMap.put(node, nodeDrawProp);
  }

  public void linePut(Line line, LineDrawProp lineDrawProp) {
    if (line == null || lineDrawProp == null || lineDrawPropMap == null) {
      return;
    }

    lineDrawPropMap.put(line, lineDrawProp);
  }

  public void clusterPut(Cluster cluster, ClusterDrawProp clusterDrawProp) {
    if (cluster == null || clusterDrawProp == null) {
      return;
    }

    if (clusterDrawPropMap == null) {
      clusterDrawPropMap = new HashMap<>(graphvizDrawProp.getGraphviz().clusters().size());
    }

    clusterDrawPropMap.put(cluster, clusterDrawProp);
  }

  public boolean needFlip() {
    Layout layout = getGraphviz().graphAttrs().getLayout();
    Rankdir rankdir = getGraphviz().graphAttrs().getRankdir();
    return layout == Layout.DOT && rankdir != Rankdir.TB && rankdir != Rankdir.BT;
  }

  public Graphviz getGraphviz() {
    return graphvizDrawProp.getGraphviz();
  }

  public Rankdir rankdir() {
    return getGraphviz().graphAttrs().getRankdir();
  }

  public GraphvizDrawProp getGraphvizDrawProp() {
    return graphvizDrawProp;
  }

  public Collection<NodeDrawProp> nodes() {
    return nodeDrawPropMap.values();
  }

  public Collection<LineDrawProp> lines() {
    return lineDrawPropMap != null ? lineDrawPropMap.values() : Collections.emptyList();
  }

  public Collection<ClusterDrawProp> clusters() {
    return clusterDrawPropMap != null
        ? clusterDrawPropMap.values()
        : Collections.emptyList();
  }

  public NodeDrawProp getNodeDrawProp(Node node) {
    return nodeDrawPropMap.get(node);
  }

  public LineDrawProp getLineDrawProp(Line line) {
    if (line == null) {
      return null;
    }
    return getLineDrawPropMap().get(line);
  }

  public ClusterDrawProp getClusterDrawProp(Cluster cluster) {
    if (clusterDrawPropMap == null) {
      return null;
    }

    return clusterDrawPropMap.get(cluster);
  }

  public LineAttrs lineAttrs(Line line) {
    Asserts.nullArgument(line, "line");
    LineDrawProp lineDrawProp = getLineDrawPropMap().get(line);

    return lineDrawProp != null ? lineDrawProp.lineAttrs() : line.lineAttrs();
  }

  public Map<Line, LineDrawProp> getLineDrawPropMap() {
    return lineDrawPropMap != null ? lineDrawPropMap : Collections.emptyMap();
  }

  public double width(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);

    return nodeDrawProp != null ? nodeDrawProp.getWidth() : 0;
  }

  public double height(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);

    return nodeDrawProp != null ? nodeDrawProp.getHeight() : 0;
  }

  public boolean haveCluster(Cluster cluster) {
    if (clusterDrawPropMap == null || cluster == null) {
      return false;
    }

    return clusterDrawPropMap.get(cluster) != null;
  }

  public String nodeId(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);
    if (nodeDrawProp == null) {
      return null;
    }

    return nodeDrawProp.nodeAttrs().getId() != null
        ? nodeDrawProp.nodeAttrs().getId()
        : NODE_ID_PREFIX + nodeDrawProp.id();
  }

  public int nodeNo(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);

    return nodeDrawProp != null ? nodeDrawProp.nodeNo() : 0;
  }

  public int getNodeNum() {
    return nodeDrawPropMap == null ? 0 : nodeDrawPropMap.keySet().size();
  }

  public String lineId(Line line) {
    if (lineDrawPropMap == null) {
      return null;
    }
    LineDrawProp lineDrawProp = lineDrawPropMap.get(line);
    return lineDrawProp != null ? lineDrawProp.id() : null;
  }

  public String clusterId(Cluster cluster) {
    if (clusterDrawPropMap == null) {
      return null;
    }
    ClusterDrawProp clusterDrawProp = clusterDrawPropMap.get(cluster);
    return clusterDrawProp != null ? clusterDrawProp.id() : null;
  }

  public double nodeHorPortOffset(Node n, Line line,
                                  Port tailPort, Port headPort,
                                  ShapePosition nodePosition) {
    if (n == null || line == null || nodePosition == null) {
      return 0;
    }

    LineDrawProp lineDrawProp = getLineDrawProp(line);
    if (lineDrawProp == null) {
      return 0;
    }

    if (n == line.tail()) {
      return tailPort == null ? 0 : tailPort.horOffset(nodePosition);
    }
    if (n == line.head()) {
      return headPort == null ? 0 : headPort.horOffset(nodePosition);
    }

    return 0;
  }

  /**
   * Returns the port coordinates of the line on the node.
   *
   * <p>Explain why you need to manually pass the Port properties of tail and head, because in some
   * scenarios, the Port will be flipped and moved.There will be deviations when using the original
   * Port.
   *
   * @param n            node
   * @param line         line
   * @param tailPort     tail port attribute
   * @param headPort     head port attribute
   * @param nodePosition node position information
   * @return port coordinates of the line on the node.
   */
  public FlatPoint nodePortPosition(Node n, Line line,
                                    Port tailPort, Port headPort,
                                    ShapePosition nodePosition) {
    Asserts.nullArgument(nodePosition, "nodePosition");
    if (n == null || line == null) {
      return new FlatPoint(nodePosition.getX(), nodePosition.getY());
    }

    if (n == line.tail()) {
      return tailPort == null
          ? new FlatPoint(nodePosition.getX(), nodePosition.getY())
          : new FlatPoint(
              nodePosition.getX() + tailPort.horOffset(nodePosition),
              nodePosition.getY() + tailPort.verOffset(nodePosition)
          );
    }
    if (n == line.head()) {
      return headPort == null
          ? new FlatPoint(nodePosition.getX(), nodePosition.getY())
          : new FlatPoint(
              nodePosition.getX() + headPort.horOffset(nodePosition),
              nodePosition.getY() + headPort.verOffset(nodePosition)
          );
    }

    return new FlatPoint(nodePosition.getX(), nodePosition.getY());
  }

  public void syncGraphvizBorder() {
    leftBorder = graphvizDrawProp.getLeftBorder();
    rightBorder = graphvizDrawProp.getRightBorder();
    upBorder = graphvizDrawProp.getUpBorder();
    downBorder = graphvizDrawProp.getDownBorder();
  }

  public void syncToGraphvizBorder() {
    graphvizDrawProp.setLeftBorder(leftBorder);
    graphvizDrawProp.setRightBorder(rightBorder);
    graphvizDrawProp.setUpBorder(upBorder);
    graphvizDrawProp.setDownBorder(downBorder);
  }

  public void updateXAxisRange(double x) {
    super.updateXAxisRange(x);
  }

  public void updateYAxisRange(double y) {
    super.updateYAxisRange(y);
  }

  public double getMinX() {
    return leftBorder;
  }

  public double getMaxX() {
    return rightBorder;
  }

  public double getMinY() {
    return upBorder;
  }

  public double getMaxY() {
    return downBorder;
  }

  public double width() {
    return getMaxX() - getMinX();
  }

  public double height() {
    return getMaxY() - getMinY();
  }
}
