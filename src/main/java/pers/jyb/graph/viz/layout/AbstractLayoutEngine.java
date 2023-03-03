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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.ClassUtils;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.util.GraphvizUtils;
import pers.jyb.graph.viz.api.Cluster;
import pers.jyb.graph.viz.api.ClusterAttrs;
import pers.jyb.graph.viz.api.GraphAttrs;
import pers.jyb.graph.viz.api.GraphContainer;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.Labeljust;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.ContainerDrawProp;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.draw.RenderEngine;

public abstract class AbstractLayoutEngine implements LayoutEngine {

  private static final Map<String, Object> DEFAULT_NODE_ATTRS_MAP;

  private static final Map<String, Object> DEFAULT_LINE_ATTRS_MAP;

  static {
    try {
      DEFAULT_NODE_ATTRS_MAP = ClassUtils.propValMap(DefaultVal.DEFAULT_NODE_ATTRS);
      DEFAULT_LINE_ATTRS_MAP = ClassUtils.propValMap(DefaultVal.DEFAULT_LINE_ATTRS);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to set default properties", e);
    }
  }

  @Override
  public DrawGraph layout(Graphviz graphviz, RenderEngine renderEngine) {
    Asserts.nullArgument(graphviz, "Graphviz");
    Asserts.illegalArgument(graphviz.nodeNum() == 0, "Graphviz container is empty!");

    DrawGraph drawGraph = new DrawGraph(graphviz);
    handleGraphviz(drawGraph.getGraphvizDrawProp());
    Object attachment = attachment(drawGraph);

    // Id record
    Map<Node, Integer> nodeId = new HashMap<>(graphviz.nodeNum());
    Map<Line, Integer> lineId = new HashMap<>(graphviz.lineNum());
    Map<GraphContainer, Integer> clusterId = new HashMap<>(graphviz.clusters().size());

    Consumer<GraphContainer> containerConsumer = c ->
        nodeLineClusterHandle(attachment, drawGraph, c, nodeId, lineId, clusterId);

    GraphvizUtils.dfs(
        Integer.MAX_VALUE,
        Boolean.FALSE,
        new HashSet<>(),
        null,
        graphviz,
        containerConsumer::accept,
        containerConsumer::accept,
        this::dfsNeedContinue
    );
    nodeLineClusterHandle(attachment, drawGraph, graphviz, nodeId, lineId, clusterId);

    layout(drawGraph, attachment);
    moveGraph(drawGraph, renderEngine, attachment);
    return drawGraph;
  }

  protected Object attachment(DrawGraph drawGraph) {
    return null;
  }

  protected void consumerNode(Node node,
                              Object attachment,
                              DrawGraph drawGraph,
                              GraphContainer parentContainer) {
  }

  protected void consumerLine(Line line, Object attachment, DrawGraph drawGraph) {
  }

  protected void afterLayoutShifter(Object attach) {
  }

  protected void afterRenderShifter(Object attach) {
  }

  protected void nodeContainerSet(NodeDrawProp nodeDrawProp, NodeAttrs nodeAttrs) {
    NodeShape nodeShape = nodeAttrs.getNodeShape();

    // Inner Label Box size
    FlatPoint labelBox = sizeInit(nodeAttrs);

    // Set node box size
    double height = nodeAttrs.getHeight() == null
        ? nodeShape.getDefaultHeight() * Graphviz.PIXLE
        : nodeAttrs.getHeight();
    double width = nodeAttrs.getWidth() == null
        ? nodeShape.getDefaultWidth() * Graphviz.PIXLE
        : nodeAttrs.getWidth();
    double verMargin = 0;
    double horMargin = 0;
    if (nodeAttrs.getMargin() != null && nodeShape.needMargin()) {
      verMargin += nodeAttrs.getMargin().getHeight();
      horMargin += nodeAttrs.getMargin().getWidth();
    }

    FlatPoint boxSize;
    if (Objects.equals(nodeAttrs.getFixedSize(), Boolean.TRUE)
        || nodeShape == NodeShapeEnum.POINT) {
      boxSize = new FlatPoint(height, width);
    } else {
      FlatPoint labelSize = new FlatPoint(verMargin + labelBox.getHeight(),
                                          horMargin + labelBox.getWidth());
      boxSize = nodeShape.minContainerSize(labelSize.getHeight(), labelSize.getWidth());
      boxSize.setHeight(Math.max(boxSize.getHeight(), height));
      boxSize.setWidth(Math.max(boxSize.getWidth(), width));
      nodeShape.ratio(boxSize);
      nodeDrawProp.setLabelSize(labelSize);
    }

    nodeDrawProp.setLeftBorder(0);
    nodeDrawProp.setRightBorder(boxSize.getWidth());
    nodeDrawProp.setUpBorder(0);
    nodeDrawProp.setDownBorder(boxSize.getHeight());
    if (nodeDrawProp.getLabelSize() == null) {
      nodeDrawProp.setLabelSize(labelBox);
    }

    if (nodeAttrs.getLabelloc() != null && nodeAttrs.getLabelloc() != Labelloc.CENTER) {
      double halfHeight = (verMargin + labelBox.getHeight()) / 2;
      double halfWidth = (horMargin + labelBox.getWidth()) / 2;
      nodeDrawProp.setLabelVerOffset(
          nodeAttrs.getLabelloc().getY(
              new FlatPoint(-halfWidth, -halfHeight),
              new FlatPoint(halfWidth, halfHeight),
              labelBox
          )
      );
    }
  }

  // label container set
  protected FlatPoint labelContainer(String label, double fontSize) {
    return AWTMeasureLabelSize.INSTANCE.measure(label, fontSize, 10);
  }

  protected void containerLabelPos(DrawGraph drawGraph) {
    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    if (graphvizDrawProp.getLabelSize() != null) {
      GraphAttrs graphAttrs = graphvizDrawProp.getGraphviz().graphAttrs();
      containerLabelPos(graphvizDrawProp, graphAttrs.getLabelloc(), graphAttrs.getLabeljust());
    }

    for (ClusterDrawProp cluster : drawGraph.clusters()) {
      if (cluster.getLabelSize() == null) {
        continue;
      }

      ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
      containerLabelPos(cluster, clusterAttrs.getLabelloc(), clusterAttrs.getLabeljust());
    }
  }

  protected abstract void layout(DrawGraph drawGraph, Object attachment);

  protected abstract List<ShifterStrategy> shifterStrategys(DrawGraph drawGraph);

  // -------------------------------- private method --------------------------------

  private void handleGraphviz(GraphvizDrawProp graphvizDrawProp) {
    Graphviz graphviz = graphvizDrawProp.getGraphviz();
    String label = graphviz.graphAttrs().getLabel();
    if (label == null) {
      return;
    }

    FlatPoint labelSize = labelContainer(label, graphviz.graphAttrs().getFontSize());
    graphvizDrawProp.setLabelSize(labelSize);
  }

  private void containerLabelPos(ContainerDrawProp containerDrawProp,
                                 Labelloc labelloc, Labeljust labeljust) {
    FlatPoint upperLeft = new FlatPoint(containerDrawProp.getLeftBorder(),
                                        containerDrawProp.getUpBorder());
    FlatPoint lowerRight = new FlatPoint(containerDrawProp.getRightBorder(),
                                         containerDrawProp.getDownBorder());

    FlatPoint labelPoint = new FlatPoint(
        labeljust.getX(upperLeft, lowerRight, containerDrawProp.getLabelSize()),
        labelloc.getY(upperLeft, lowerRight, containerDrawProp.getLabelSize())
    );
    containerDrawProp.setLabelCenter(labelPoint);
  }

  private boolean dfsNeedContinue(GraphContainer c) {
    return !c.isSubgraph() || c.isTransparent();
  }

  private void nodeLineClusterHandle(Object attachment,
                                     DrawGraph drawGraph,
                                     GraphContainer container,
                                     Map<Node, Integer> nodeId,
                                     Map<Line, Integer> lineId,
                                     Map<GraphContainer, Integer> clusterId) {
    Iterable<Node> nodes;
    Iterable<Line> lines;
    if (dfsNeedContinue(container)) {
      nodes = container.directNodes();
      lines = container.directLines();
    } else {
      nodes = container.nodes();
      lines = container.lines();
    }

    for (Node node : nodes) {
      nodeHandle(attachment, drawGraph, container, nodeId, node);
    }

    for (Line line : lines) {
      nodeHandle(attachment, drawGraph, container, nodeId, line.head());
      nodeHandle(attachment, drawGraph, container, nodeId, line.tail());

      lineHandle(attachment, drawGraph, container, lineId, line);
    }

    if (container.isCluster()) {
      clusterHandle(drawGraph, (Cluster) container, clusterId);

      Cluster cluster = (Cluster) container;
      String label = cluster.clusterAttrs().getLabel();
      double fontSize = cluster.clusterAttrs().getFontSize();

      if (label != null) {
        FlatPoint labelContainer = labelContainer(label, fontSize);
        drawGraph.getClusterDrawProp(cluster).setLabelSize(labelContainer);
      }
    }
  }

  private void nodeHandle(Object attachment,
                          DrawGraph drawGraph,
                          GraphContainer container,
                          Map<Node, Integer> nodeId,
                          Node node) {
    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node);

    NodeAttrs nodeAttrs = nodeDrawProp != null
        ? nodeDrawProp.nodeAttrs()
        : node.nodeAttrs().clone();

    try {
      // set template properties
      copyTempProperties(
          nodeAttrs,
          findFirstHaveTempParent(drawGraph.getGraphviz(), true, container),
          DEFAULT_NODE_ATTRS_MAP
      );
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Failed to access template property", e);
    }

    if (nodeDrawProp == null) {
      nodeDrawProp = new NodeDrawProp(node, nodeAttrs);
      drawGraph.nodePut(node, nodeDrawProp);
    } else {
      nodeDrawProp.setNodeAttrs(nodeAttrs);
    }

    Integer n = nodeId.get(node);
    if (n == null) {
      int nz = nodeId.size();
      nodeDrawProp.setId(nz);
      nodeId.put(node, nz);
    }

    // node container size calculate
    nodeContainerSet(nodeDrawProp, nodeAttrs);

    // node consume
    consumerNode(node, attachment, drawGraph, container);
  }

  private void lineHandle(Object attachment,
                          DrawGraph drawGraph,
                          GraphContainer container,
                          Map<Line, Integer> lineId, Line line) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);

    LineAttrs lineAttrs = lineDrawProp != null
        ? lineDrawProp.lineAttrs()
        : line.lineAttrs().clone();

    try {
      // set template properties
      copyTempProperties(
          lineAttrs,
          findFirstHaveTempParent(drawGraph.getGraphviz(), false, container),
          DEFAULT_LINE_ATTRS_MAP
      );
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Failed to access template property", e);
    }

    if (lineDrawProp == null) {
      lineDrawProp = new LineDrawProp(line, lineAttrs, drawGraph);
      drawGraph.linePut(line, lineDrawProp);
    }

    Integer n = lineId.get(line);
    if (n == null) {
      int nz = lineId.size();
      lineDrawProp.setId("line_" + nz);
      lineId.put(line, nz);
    }

    // line consume
    consumerLine(line, attachment, drawGraph);
  }

  private void clusterHandle(DrawGraph drawGraph, Cluster cluster,
                             Map<GraphContainer, Integer> clusterId) {
    if (drawGraph.haveCluster(cluster)) {
      return;
    }

    ClusterDrawProp clusterDrawProp = new ClusterDrawProp(cluster);
    drawGraph.clusterPut(cluster, clusterDrawProp);

    Integer n = clusterId.get(cluster);
    if (n == null) {
      int nz = clusterId.size();
      clusterDrawProp.setClusterNo(nz);
      clusterDrawProp.setId("cluster_" + nz);
      clusterId.put(cluster, nz);
    }
  }

  private GraphContainer findFirstHaveTempParent(Graphviz graphviz, boolean nodeTemp,
                                                 GraphContainer container) {
    GraphContainer p = container;

    while (p != null) {
      if ((nodeTemp && p.haveNodeTemp()) || (!nodeTemp && p.haveLineTemp())) {
        break;
      }

      p = graphviz.father(p);
    }

    return p;
  }

  private FlatPoint sizeInit(NodeAttrs nodeAttrs) {
    String label = nodeAttrs.getLabel();

    double fontSize = nodeAttrs.getFontSize() != null ? nodeAttrs.getFontSize() : 0D;
    return labelContainer(label, fontSize);
  }

  private void copyTempProperties(Object attrs,
                                  GraphContainer container,
                                  Map<String, Object> defaultVal) throws IllegalAccessException {
    Objects.requireNonNull(defaultVal);
    if (attrs == null) {
      return;
    }

    NodeShape nodeShape = null;
    Field nodeShapeField = null;
    Class<?> cls = attrs.getClass();
    Field[] fields = cls.getDeclaredFields();
    for (Field field : fields) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      field.setAccessible(true);
      Object v = field.get(attrs);
      if (v == null) {
        Object propVal;
        if (container == null) {
          propVal = null;
        } else if (attrs instanceof NodeAttrs) {
          propVal = container.getNodeAttr(field.getName());
        } else {
          propVal = container.getLineAttr(field.getName());
        }
        propVal = propVal != null ? propVal : defaultVal.get(field.getName());

        if (propVal == null) {
          field.setAccessible(false);
          continue;
        }

        field.set(attrs, propVal);
        field.setAccessible(false);
        v = propVal;
      }

      if (v instanceof NodeShape) {
        nodeShape = (NodeShape) v;
        nodeShapeField = field;
      }
    }

    // Compile a new NodeShape description function
    if (nodeShape != null && attrs instanceof NodeAttrs) {
      nodeShapeField.setAccessible(true);
      nodeShapeField.set(attrs, nodeShape.post((NodeAttrs) attrs));
      nodeShapeField.setAccessible(false);
    }
  }

  private void moveGraph(DrawGraph drawGraph, RenderEngine renderEngine, Object attach) {
    List<ShifterStrategy> layoutShifters = shifterStrategys(drawGraph);

    Shifter shifter;
    Set<FlatPoint> pointMark = null;
    if (CollectionUtils.isNotEmpty(layoutShifters)) {
      pointMark = new HashSet<>();
      shifter = new CombineShifter(pointMark, layoutShifters);
      executeShifter(drawGraph, shifter);
    }
    afterLayoutShifter(attach);

    if (pointMark != null) {
      pointMark.clear();
    }

    List<ShifterStrategy> renderShifters = renderEngine == null
        ? null : renderEngine.shifterStrategys(drawGraph);

    if (CollectionUtils.isNotEmpty(renderShifters)) {
      if (pointMark == null) {
        pointMark = new HashSet<>();
      }
      shifter = new CombineShifter(pointMark, renderShifters);
      executeShifter(drawGraph, shifter);
    }
    afterRenderShifter(attach);
  }

  private void executeShifter(DrawGraph drawGraph, Shifter shifter) {
    shifter.graph(drawGraph.getGraphvizDrawProp());
    drawGraph.clusters().forEach(shifter::cluster);
    drawGraph.nodes().forEach(shifter::node);
    drawGraph.lines().forEach(shifter::line);
  }
}
