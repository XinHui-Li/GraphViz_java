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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import pers.jyb.graph.def.EdgeDedigraph;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.op.Curves;
import pers.jyb.graph.op.Curves.MultiBezierCurve;
import pers.jyb.graph.op.Curves.ThirdOrderBezierCurve;
import pers.jyb.graph.op.Vectors;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.api.attributes.Port;
import pers.jyb.graph.viz.api.ext.ShapePosition;
import pers.jyb.graph.viz.draw.DefaultShapePosition;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.layout.dot.RankContent.RankNode;

public abstract class AbstractDotLineRouter extends AbstractLineRouter
    implements DotLineRouter {

  protected static final double LABEL_NODE_SIDE_MAX_DISTANCE = 10;

  protected RankContent rankContent;
  protected EdgeDedigraph<DNode, DLine> digraphProxy;

  @Override
  public void handle() {
    Object attach = attach();

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (DNode node : rankNode) {
        if (nodeConsumer(node, attach)) {
          continue;
        }

        // All out edges
        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (line.isVirtual() || line.isHide()) {
            continue;
          }

          if (line.isParallelMerge() && (!line.isSameRank() || (line.isSameRank()
              && isAdj(line.from(), line.to())))
          ) {
            parallelLineHandle(line);
            continue;
          }

          lineConsumer(line, attach);
        }

        // Draw self loop
        selfLoopHandle(node);
      }
    }
  }

  /**
   * Before draw line, produce attachment for next method.
   *
   * @return draw line attachment
   */
  protected Object attach() {
    return null;
  }

  /**
   * The consumption action of the node when drawing the line.
   *
   * @param node   node
   * @param attach draw line attachment
   * @return True - continue draw line, False - consume next node
   */
  protected boolean nodeConsumer(DNode node, Object attach) {
    return false;
  }

  /**
   * The consumption action of the line.
   *
   * @param line   line
   * @param attach draw line attachment
   */
  protected void lineConsumer(DLine line, Object attach) {
  }

  /**
   * If the node has a self-loop edge, generate a simulated path of the self-loop edge.
   *
   * @param node node to be detected
   */
  protected void selfLoopHandle(DNode node) {
    if (node == null
        || node.isVirtual()
        || CollectionUtils.isEmpty(node.getSelfLines())
        || isSplineNone()) {
      return;
    }

    FlatPoint center = new FlatPoint(node.getX(), node.getY());
    for (DLine selfLine : node.getSelfLines()) {
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());
      if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() < 2) {
        continue;
      }

      for (FlatPoint point : lineDrawProp) {
        point.setX(node.getX() + point.getX());
        point.setY(node.getY() + point.getY());
      }
      if (lineDrawProp.getLabelCenter() != null) {
        FlatPoint labelCenter = lineDrawProp.getLabelCenter();
        labelCenter.setX(node.getX() + labelCenter.getX());
        labelCenter.setY(node.getY() + labelCenter.getY());
      }

      CurvePathClip curvePathClip = new CurvePathClip();
      if (lineDrawProp.size() == 2) {
        curvePathClip.setNoPathDirection(lineDrawProp.get(lineDrawProp.size() / 2));
        twoSelfLineDraw(selfLine);
      } else {
        largeTwoSelfLineDraw(center, selfLine);
      }

      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        lineDrawProp.setStart(lineDrawProp.get(0));
        lineDrawProp.setEnd(lineDrawProp.get(lineDrawProp.size() - 1));
      }
    }
  }

  /**
   * Logic for drawing parallel edges with the same endpoints.
   *
   * @param parallelLines parallel lines
   */
  protected void handleSameEndpointParallelLines(List<DLine> parallelLines) {
    symmetryParallelLine(parallelLines);
  }

  /**
   * If the edge is a union of multiple parallel edges, generate a simulated path of the paralle
   * edges.
   *
   * @param line line to be detected
   */
  protected void parallelLineHandle(DLine line) {
    if (line == null || !line.isParallelMerge() || isSplineNone()) {
      return;
    }

    Map<Integer, List<DLine>> parallelLineRecordMap = groupParallelLineByEndpoint(line);

    for (Entry<Integer, List<DLine>> entry : parallelLineRecordMap.entrySet()) {
      List<DLine> parallelLines = entry.getValue();
      handleSameEndpointParallelLines(parallelLines);
    }
  }

  /**
   * Draw parallel sides with an axis of symmetry.
   *
   * @param parallelLines parallel edges
   */
  protected void symmetryParallelLine(List<DLine> parallelLines) {
    if (CollectionUtils.isEmpty(parallelLines)) {
      return;
    }

    DLine line = parallelLines.get(0);
    DNode from = line.from();
    DNode to = line.to();
    FlatPoint fromPoint = new FlatPoint(from.getX(), from.getY());
    FlatPoint toPoint = new FlatPoint(to.getX(), to.getY());

    double distUnit = (drawGraph.getGraphviz().graphAttrs().getNodeSep()
        + drawGraph.getGraphviz().graphAttrs().getRankSep()
        + FlatPoint.twoFlatPointDistance(fromPoint, toPoint)) / 20;

    for (int i = 0; i < parallelLines.size(); i++) {
      parallelEdges(parallelLines.get(i), parallelLines.size(), distUnit, i + 1);
    }
  }

  /**
   * If the line is cut by multiple virtual nodes, consume each virtual line segment through
   * lineSegmentConsumer.
   *
   * @param line     line
   * @param consumer line consumer
   */
  protected void lineSegmentConsumer(DLine line, Consumer<DLine> consumer) {
    DNode to = line.to();
    while (to.isVirtual()) {
      if (consumer != null) {
        consumer.accept(line);
      }

      for (DLine dLine : digraphProxy.outAdjacent(to)) {
        to = dLine.to();
        line = dLine;
        break;
      }
    }

    if (consumer != null && !to.isVirtual()) {
      consumer.accept(line);
    }
  }

  protected boolean isAdj(DNode n, DNode w) {
    if (Math.abs(w.getRankIndex() - n.getRankIndex()) <= 1) {
      return true;
    }

    // Skip virtual vertices between two vertices
    DNode largeRankIndexNode = n.getRankIndex() > w.getRankIndex() ? n : w;
    DNode current = n == largeRankIndexNode ? w : n;
    do {
      current = rankContent.rankNextNode(current);
    } while (current != null && current != largeRankIndexNode && current.isVirtual());

    return current == largeRankIndexNode;
  }

  protected Map<Integer, List<DLine>> groupParallelLineByEndpoint(DLine line) {
    Map<Integer, List<DLine>> parallelLineRecordMap = new HashMap<>(1);

    for (int i = 0; i < line.getParallelNums(); i++) {
      DLine edge = line.parallelLine(i);
      DNode from = edge.from();
      DNode to = edge.to();
      Port fromPort = PortHelper.getLineEndPointPort(from.getNode(), edge.getLine(), drawGraph);
      Port toPort = PortHelper.getLineEndPointPort(to.getNode(), edge.getLine(), drawGraph);
      int hash = ((fromPort != null ? fromPort.name() : "") + (toPort != null ? toPort.name() : ""))
          .hashCode();
      parallelLineRecordMap.computeIfAbsent(hash, h -> new ArrayList<>(2)).add(edge);
    }

    return parallelLineRecordMap;
  }

  // ----------------------------------------------------- static method -----------------------------------------------------

  public static ShapePosition newArrowShapePosition(FlatPoint point, double arrowSize) {
    Asserts.nullArgument(point, "point");
    return new DefaultShapePosition(point.getX(), point.getY(),
                                    arrowSize * 2, arrowSize * 2,
                                    NodeShapeEnum.CIRCLE);
  }

  public static <E extends FlatPoint> E getPoint(List<E> path, int i) {
    if (i < 0 || i >= path.size()) {
      return null;
    }

    return path.get(i);
  }

  public static <E extends FlatPoint> E getFirst(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(0);
  }

  public static <E extends FlatPoint> E getLast(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(path.size() - 1);
  }

  public static <E extends FlatPoint> InOutPointPair findInOutPair(int unit, List<E> path,
                                                                   boolean firstStart,
                                                                   ShapePosition shapePosition) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    Asserts.nullArgument(shapePosition.nodeShape(), "shapePosition.nodeShape()");

    Integer idx = null;
    Integer count = null;

    NodeShape nodeShape = shapePosition.nodeShape();

    E point = getFirst(path);
    if (firstStart && point != null && nodeShape.in(shapePosition, point)) {
      idx = 0;
      count = unit;
    } else {
      point = getLast(path);
      if (point != null && nodeShape.in(shapePosition, point)) {
        idx = path.size() - 1;
        count = -unit;
      }
    }

    if (idx == null) {
      return null;
    }

    E pre = null;
    do {
      if (pre != null) {
        boolean preIn = nodeShape.in(shapePosition, pre);
        boolean pointIn = nodeShape.in(shapePosition, point);

        if (preIn != pointIn) {
          return new InOutPointPair(
              idx - count,
              count > 0,
              preIn ? pre : point,
              pointIn ? pre : point
          );
        }
      }

      idx += count;
      pre = point;
      point = getPoint(path, idx);
    } while (point != null);

    return null;
  }

  // ----------------------------------------------------- private method -----------------------------------------------------
  private void largeTwoSelfLineDraw(FlatPoint center, DLine selfLine) {
    Asserts.illegalArgument(selfLine == null || selfLine.isVirtual(), "error self loop no");

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());

    FlatPoint mid = lineDrawProp.get(lineDrawProp.size() / 2);
    FlatPoint start = lineDrawProp.get(0);
    FlatPoint end = lineDrawProp.get(lineDrawProp.size() - 1);

    MultiBezierCurve curves = Curves.fitCurves(Arrays.asList(start, mid, end),
                                               Vectors.addVector(
                                                   Vectors.subVector(start, center),
                                                   Vectors.subVector(mid, center)
                                               ),
                                               Vectors.addVector(
                                                   Vectors.subVector(end, center),
                                                   Vectors.subVector(mid, center)
                                               ), 0);

    lineDrawProp.clear();
    lineDrawProp.markIsBesselCurve();
    lineDrawProp.addAll(multiBezierCurveToPoints(curves));
  }

  private void twoSelfLineDraw(DLine selfLine) {
    Asserts.illegalArgument(selfLine == null || selfLine.isVirtual(), "error self loop no");

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());
    if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() != 2) {
      return;
    }

    FlatPoint start = lineDrawProp.get(0);
    FlatPoint end = lineDrawProp.get(lineDrawProp.size() - 1);
    FlatPoint axis = Vectors.subVector(end, start);
    FlatPoint vertical = new FlatPoint(axis.getY(), -axis.getX());
    FlatPoint verticalOpposite = vertical.reserve();
    lineDrawProp.clear();

    double dist = axis.dist() / 4;
    lineDrawProp.add(start);
    lineDrawProp.add(Vectors.addVector(start, Vectors.vectorScale(vertical, dist)));
    lineDrawProp.add(Vectors.addVector(end, Vectors.vectorScale(vertical, dist)));
    lineDrawProp.add(end);
    lineDrawProp.add(Vectors.addVector(end, Vectors.vectorScale(verticalOpposite, dist)));
    lineDrawProp.add(Vectors.addVector(start, Vectors.vectorScale(verticalOpposite, dist)));
    lineDrawProp.add(start);

    lineDrawProp.markIsBesselCurve();
  }

  private void parallelEdges(DLine parallelLine, int size,
                             double distUnit, int no) {
    DNode from = parallelLine.from();
    DNode to = parallelLine.to();

    FlatPoint fromPoint = PortHelper.getPortPoint(parallelLine.getLine(), from, drawGraph);
    FlatPoint toPoint = PortHelper.getPortPoint(parallelLine.getLine(), to, drawGraph);

    double hypotenuseLen = hypotenuseLen(distUnit, no, size);

    Line iLine = parallelLine.getLine();
    Asserts.illegalArgument(iLine == null, "error parallel edge no");

    FlatPoint v2Center = Vectors.addVector(
        Vectors.multipleVector(Vectors.subVector(fromPoint, toPoint), 0.75),
        toPoint
    );

    FlatPoint v3Center = Vectors.addVector(
        Vectors.multipleVector(Vectors.subVector(fromPoint, toPoint), 0.25),
        toPoint
    );

    ThirdOrderBezierCurve curve = new ThirdOrderBezierCurve(
        fromPoint,
        newParallelControlPoint(parallelLine, size, no,
                                hypotenuseLen, fromPoint, toPoint, v2Center),
        newParallelControlPoint(parallelLine, size, no,
                                hypotenuseLen, fromPoint, toPoint, v3Center),
        toPoint
    );

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(parallelLine.getLine());
    lineDrawProp.clear();
    lineDrawProp.addAll(thirdOrderBezierCurveToPoints(curve));
    lineDrawProp.markIsBesselCurve();
    lineDrawProp.setIsHeadStart(from.getNode());
    lineDrawProp.fakeInit();
  }

  private FlatPoint newParallelControlPoint(DLine line, int size, int no, double hypotenuseLen,
                                            FlatPoint f, FlatPoint t, FlatPoint v3Center) {
    return new FlatPoint(
        v3Center.getX() + xDist(f.getX(), f.getY(), t.getX(),
                                t.getY(), hypotenuseLen, no, size / 2),
        v3Center.getY() + yDist(f.getX(), f.getY(), t.getX(), t.getY(), hypotenuseLen, no,
                                size / 2, line.from().getRank() == line.to().getRank())
    );
  }

  private double hypotenuseLen(double unit, int segmentNum, int parallelEdgesNum) {
    if ((parallelEdgesNum & 1) == 1) {
      return Math.abs(unit * (parallelEdgesNum - 1) / 2 - (segmentNum - 1) * unit);
    }

    if (segmentNum <= (parallelEdgesNum >> 1)) {
      return unit * segmentNum - unit / 2;
    }

    return Math.abs(unit * segmentNum - (parallelEdgesNum >> 1) * unit - unit / 2);
  }

  private double xDist(double startX, double startY, double endX, double endY, double hypotenuseLen,
                       int segmentNum, int mid) {
    if (startY == endY) {
      return 0;
    }

    if (startX == endX) {
      return segmentNum <= mid ? -hypotenuseLen : hypotenuseLen;
    }

    double slop = (endY - startY) / (endX - startX);
    double xd = Math.sqrt(Math.pow(hypotenuseLen, 2) / (1 + 1 / Math.pow(slop, 2)));

    return segmentNum <= mid ? -xd : xd;
  }

  private double yDist(double startX, double startY, double endX, double endY, double hypotenuseLen,
                       int segmentNum, int mid, boolean isSameRank) {

    if (startX == endX) {
      return 0;
    }

    if (startY == endY) {
      return segmentNum <= mid ? -hypotenuseLen : hypotenuseLen;
    }

    double slop = (endY - startY) / (endX - startX);
    double yd = Math.sqrt(Math.pow(hypotenuseLen, 2) / (1 + Math.pow(slop, 2)));

    if (isSameRank) {
      return segmentNum <= mid ? -yd : yd;
    }
    return segmentNum <= mid == slop < 0 ? -yd : yd;
  }

  // --------------------------------------------- Abstract DotLinesHandlerFactory ---------------------------------------------

  public abstract static class AbstractDotLineRouterFactory<T extends AbstractDotLineRouter>
      implements DotLineRouterFactory<T> {

    @Override
    public T newInstance(DrawGraph drawGraph, DotDigraph dotDigraph, RankContent rankContent,
                         EdgeDedigraph<DNode, DLine> digraphProxy) {
      Asserts.nullArgument(drawGraph, "drawGraph");
      Asserts.nullArgument(dotDigraph, "dotDigraph");
      Asserts.nullArgument(rankContent, "rankContent");
      Asserts.nullArgument(digraphProxy, "digraphProxy");

      T t = newInstance();
      Asserts.nullArgument(t, "DotLineRouter");
      t.drawGraph = drawGraph;
      t.dotDigraph = dotDigraph;
      t.rankContent = rankContent;
      t.digraphProxy = digraphProxy;
      return t;
    }

    protected abstract T newInstance();
  }

  static class InOutPointPair {

    private final int idx;

    private final boolean deleteBefore;

    private final FlatPoint in;

    private final FlatPoint out;

    public InOutPointPair(int idx, boolean deleteBefore, FlatPoint in, FlatPoint out) {
      this.idx = idx;
      this.deleteBefore = deleteBefore;
      this.in = in;
      this.out = out;
    }

    public int getIdx() {
      return idx;
    }

    public boolean isDeleteBefore() {
      return deleteBefore;
    }

    public FlatPoint getIn() {
      return in;
    }

    public FlatPoint getOut() {
      return out;
    }
  }
}
