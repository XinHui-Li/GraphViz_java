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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pers.jyb.graph.def.EdgeDedigraph;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.util.StringUtils;
import pers.jyb.graph.viz.api.GraphAttrs;
import pers.jyb.graph.viz.api.GraphContainer;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.Rankdir;
import pers.jyb.graph.viz.api.attributes.Splines;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.layout.AbstractLayoutEngine;
import pers.jyb.graph.viz.layout.FlipShifterStrategy;
import pers.jyb.graph.viz.layout.ShifterStrategy;
import pers.jyb.graph.viz.layout.dot.DotLineRouter.DotLineRouterFactory;
import pers.jyb.graph.viz.layout.dot.LineHandler.LineRouterBuilder;
import pers.jyb.graph.viz.layout.dot.OrthogonalRouter.OrthogonalRouterFactory;
import pers.jyb.graph.viz.layout.dot.PolyLineRouter.PolyLineRouterFactory;
import pers.jyb.graph.viz.layout.dot.RoundedRouter.RoundedRouterFactory;
import pers.jyb.graph.viz.layout.dot.SplineRouter.SplineRouterFactory;

public class DotLayoutEngine extends AbstractLayoutEngine implements Serializable {

  private static final long serialVersionUID = 1932138711284862609L;

  private static final List<DotLineRouterFactory<?>> SPLINES_HANDLERS;

  static {
    SPLINES_HANDLERS = Arrays.asList(
        new RoundedRouterFactory(), new SplineRouterFactory(),
        new PolyLineRouterFactory(), new LineRouterBuilder(),
        new OrthogonalRouterFactory());
  }


  @Override
  public List<ShifterStrategy> shifterStrategys(DrawGraph drawGraph) {
    if (drawGraph.getGraphviz().graphAttrs().getRankdir() == Rankdir.TB) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new FlipShifterStrategy(drawGraph));
  }

  @Override
  protected Object attachment(DrawGraph drawGraph) {
    Map<Node, DNode> nodeRecord = new HashMap<>(drawGraph.getGraphviz().nodeNum());
    DotDigraph dotDigraph = new DotDigraph(
        drawGraph.getGraphviz().nodeNum(),
        drawGraph.getGraphviz(),
        nodeRecord
    );

    return new DotAttachment(dotDigraph, drawGraph, nodeRecord);
  }

  @Override
  protected void consumerNode(Node node,
                              Object attachment,
                              DrawGraph drawGraph,
                              GraphContainer parentContainer) {
    DotAttachment dotAttachment = (DotAttachment) attachment;

    DNode dn = dotAttachment.get(node);
    boolean dnIsNull = dn == null;
    if (dnIsNull) {
      if (drawGraph.needFlip()) {
        NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node);
        nodeDrawProp.flip();
      }

      dn = dotAttachment.mappingToDNode(node);
    }

    if (dn.getContainer() == null || dn.getContainer().isGraphviz()) {
      if (parentContainer.isSubgraph()) {
        if (!parentContainer.isTransparent()) {
          dotAttachment.markHaveSubgraph();
        }
        parentContainer = drawGraph.getGraphviz().effectiveFather(parentContainer);
      }
      dn.setContainer(parentContainer);
    }

    dn.setNodeAttrs(drawGraph.getNodeDrawProp(node).nodeAttrs());
    dotAttachment.put(node, dn);
    dotAttachment.addNode(dn);

    if (parentContainer.isCluster()) {
      dotAttachment.markHaveCluster();
    }
  }

  @Override
  protected void consumerLine(Line line, Object attachment, DrawGraph drawGraph) {
    DotAttachment dotAttachment = (DotAttachment) attachment;
    // must not be null
    DNode source = dotAttachment.get(line.tail());
    DNode target = dotAttachment.get(line.head());

    FlatPoint labelSize = null;
    LineAttrs lineAttrs = drawGraph.lineAttrs(line);

    if (needLabelNode(drawGraph, line)) {
      labelSize = lineLabelSizeInit(lineAttrs);
      if (labelSize != null && drawGraph.needFlip()) {
        labelSize.flip();
      }
    }

    DLine dLine = new DLine(
        source,
        target,
        line,
        lineAttrs,
        lineAttrs.getWeight() == null ? line.weight() : lineAttrs.getWeight(),
        lineAttrs.getMinlen() != null ? lineAttrs.getMinlen() : 1,
        labelSize
    );

    dotAttachment.addEdge(dLine);
  }

  @Override
  protected void afterLayoutShifter(Object attach) {
    DotAttachment dotAttachment = (DotAttachment) attach;
    DrawGraph drawGraph = dotAttachment.getDrawGraph();

    for (NodeDrawProp nodeDrawProp : drawGraph.nodes()) {
      nodeLabelSet(nodeDrawProp, drawGraph, true);
    }
  }

  @Override
  protected void afterRenderShifter(Object attach) {
    DotAttachment dotAttachment = (DotAttachment) attach;
    DrawGraph drawGraph = dotAttachment.getDrawGraph();
    if (drawGraph.needFlip()) {
      containerLabelPos(drawGraph);
    }

    dotAttachment.clipAllLines();
  }

  @Override
  protected void layout(DrawGraph drawGraph, Object attachment) {
    Asserts.nullArgument(drawGraph, "DrawGraph");

    DotAttachment dotAttachment = (DotAttachment) attachment;
    DotDigraph dotDigraph = dotAttachment.getDotDigraph();
    Graphviz graphviz = drawGraph.getGraphviz();
    GraphAttrs graphAttrs = graphviz.graphAttrs();
    dotAttachment.initLineClip();

    // Collapse subgraphs and clusters
    ContainerCollapse containerCollapse = new ContainerCollapse(dotAttachment, graphviz);
    RankContent rankContent = containerCollapse.getRankContent();

    if (dotAttachment.haveClusters() || dotAttachment.haveSubgraphs()) {
      /*
       * Find self loop line, remove it.
       * If there is an edge where the rank of from is greater than the rank of to, it needs to be flipped.
       * */
      handleLegalLine(dotDigraph);
      // Primitive graph RankContent
      rankContent = new RankContent(dotDigraph, graphAttrs.getRankSep(), true, null);
    }

    // Best node sorting between ranks
    MinCross minCross = new MinCross(rankContent, dotAttachment);
    EdgeDedigraph<DNode, DLine> digraphProxy = minCross.getDigraphProxy();

    // Handle line label related displays
    new LabelSupplement(rankContent, dotAttachment, digraphProxy);

    // Node coordinate
    if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("dot.coordinate.v1"))) {
      new Coordinate(graphAttrs.getNslimit(), rankContent, dotAttachment, digraphProxy);
    } else {
      new CoordinateV2(graphAttrs.getNslimit(), rankContent, dotAttachment, digraphProxy);
    }

    if (!drawGraph.needFlip()) {
      containerLabelPos(drawGraph);
    }
    splines(drawGraph, dotDigraph, rankContent, digraphProxy);
  }

  public static void nodeLabelSet(NodeDrawProp nodeDrawProp, DrawGraph drawGraph,
                                  boolean needSetCenter) {
    if (nodeDrawProp == null || drawGraph == null) {
      return;
    }

    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();
    FlatPoint labelCenter;
    if (Boolean.TRUE.equals(nodeDrawProp.nodeAttrs().getFixedSize())) {
      labelCenter = new FlatPoint(nodeDrawProp.getX(), nodeDrawProp.getY());
    } else {
      labelCenter = nodeShape.labelCenter(nodeDrawProp.getLabelSize(), nodeDrawProp);
    }

    double x = labelCenter.getX();
    double y = labelCenter.getY();

    Labelloc labelloc = nodeDrawProp.nodeAttrs().getLabelloc();
    if (labelloc != null && nodeDrawProp.getLabelSize() != null) {
      FlatPoint labelSize = nodeDrawProp.getLabelSize();
      y += nodeDrawProp.getLabelVerOffset();

      if (!needSetCenter) {
        drawGraph.updateXAxisRange(x - labelSize.getWidth() / 2);
        drawGraph.updateXAxisRange(x + labelSize.getWidth() / 2);
        drawGraph.updateYAxisRange(y - labelSize.getWidth() / 2);
        drawGraph.updateYAxisRange(y + labelSize.getWidth() / 2);
      }
    }

    if (needSetCenter) {
      nodeDrawProp.setLabelCenter(new FlatPoint(x, y));
    }
  }

  // --------------------------------------------- private method ---------------------------------------------

  protected void handleLegalLine(DotDigraph dotDigraph) {
    List<DLine> reverseLines = null;
    List<DLine> selfLoopLines = null;
    for (DNode node : dotDigraph) {
      for (DLine line : dotDigraph.adjacent(node)) {
        if (line.from().getRank() <= line.to().getRank()) {
          if (line.from() == line.to()) {
            if (selfLoopLines == null) {
              selfLoopLines = new ArrayList<>(2);
            }
            selfLoopLines.add(line);
          }

          continue;
        }

        if (reverseLines == null) {
          reverseLines = new ArrayList<>();
        }
        reverseLines.add(line);
      }
    }

    if (CollectionUtils.isNotEmpty(reverseLines)) {
      for (DLine reverseLine : reverseLines) {
        dotDigraph.reverseEdge(reverseLine);
      }
    }

    if (CollectionUtils.isNotEmpty(selfLoopLines)) {
      for (DLine selfLoopLine : selfLoopLines) {
        if (dotDigraph.removeEdge(selfLoopLine)) {
          selfLoopLine.from().addSelfLine(selfLoopLine);
        }
      }
    }
  }

  // spline generate
  private void splines(DrawGraph drawGraph, DotDigraph dotDigraph, RankContent rankContent,
                       EdgeDedigraph<DNode, DLine> digraphProxy) {
    Splines splines = drawGraph.getGraphviz().graphAttrs().getSplines();
    Map<Line, LineDrawProp> lineDrawPropMap = drawGraph.getLineDrawPropMap();

    if (splines == null
        || splines == Splines.NONE
        || lineDrawPropMap == null
        || digraphProxy.vertexNum() == 0) {
      return;
    }

    // spline handler hand out
    for (DotLineRouterFactory<?> linesHandlerFactory : SPLINES_HANDLERS) {
      DotLineRouter dotLineRouter = linesHandlerFactory
          .newInstance(drawGraph, dotDigraph, rankContent, digraphProxy);

      if (dotLineRouter.needDeal(splines)) {
        dotLineRouter.handle();
        break;
      }
    }
  }

  private boolean needLabelNode(DrawGraph drawGraph, Line line) {
    Map<Line, LineDrawProp> lineDrawPropMap = drawGraph.getLineDrawPropMap();
    // ignore Spline.NONE and self loop
    if (lineDrawPropMap == null) {
      return false;
    }

    return StringUtils.isNotEmpty(drawGraph.lineAttrs(line).getLabel());
  }

  private FlatPoint lineLabelSizeInit(LineAttrs lineAttrs) {
    String label = lineAttrs.getLabel();

    if (StringUtils.isEmpty(label)) {
      return null;
    }

    double fontSize = lineAttrs.getFontSize() != null ? lineAttrs.getFontSize() : 0D;
    return labelContainer(label, fontSize);
  }
}
