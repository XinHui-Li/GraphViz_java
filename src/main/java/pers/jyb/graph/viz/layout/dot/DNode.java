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
import java.util.Comparator;
import java.util.List;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.def.VertexIndex;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.GraphContainer;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.NodeShapeEnum;
import pers.jyb.graph.viz.api.attributes.Splines;
import pers.jyb.graph.viz.api.ext.Box;
import pers.jyb.graph.viz.api.ext.ShapePosition;
import pers.jyb.graph.viz.draw.DrawGraph;

class DNode extends VertexIndex implements Box, ShapePosition {

  private static final long serialVersionUID = -7182604069185202045L;

  public static final int FLAT_LABEL_GAP = 5;

  private static final int RANK_MODEL_BIT = Integer.SIZE - 1;

  private static final int AUX_MODE = 1;

  private static final int NOT_ADJSUT_MID = 0x400;

  // 原顶点
  private final Node node;

  // 顶点所在层级
  private int rank;

  // 顶点所在层级索引
  private int rankIndex;

  // 坐标生成的辅助层级
  private int auxRank;

  // 在当前遍历树当中，经过当前顶点的遍历路径当中，最小的后续遍历值
  private int low;

  // 当前顶点的后续遍历值
  private int lim;

  // 中值，用来对同层级进行排序的
  private double median;

  // x坐标
  private double x;

  // y坐标
  private double y;

  // 宽
  private double width;

  // 高
  private double height;

  // 节点之间的距离
  private double nodeSep;

  // Node status
  private int status;

  // 自环边
  private List<DLine> selfLines;

  // 父容器，如果为null代表是root容器
  private GraphContainer container;

  private NodeAttrs nodeAttrs;

  private Line labelLine;

  private final DLine flatLabelLine;

  private NodeSizeExpander nodeSizeExpander;

  DNode(Node node, double width, double height, double nodeSep) {
    this(node, width, height, nodeSep, null, null);
  }

  DNode(Node node, double width, double height, double nodeSep, Line labelLine) {
    this(node, width, height, nodeSep, labelLine, null);
  }

  DNode(Node node, double width, double height, double nodeSep, DLine labelLine) {
    this(node, width, height, nodeSep, null, labelLine);
  }

  private DNode(Node node, double width, double height, double nodeSep, Line labelLine,
                DLine flatLabelLine) {
    this.node = node;
    this.width = width;
    this.height = height;
    this.nodeSep = nodeSep;
    this.labelLine = labelLine;
    this.flatLabelLine = flatLabelLine;

    setFlatLabelSize(flatLabelLine);
  }

  private void setFlatLabelSize(DLine flatLabelLine) {
    if (flatLabelLine == null) {
      return;
    }

    this.height = 0;
    this.width = 0;
    for (int i = 0; i < flatLabelLine.getParallelNums(); i++) {
      DLine line = flatLabelLine.parallelLine(i);
      FlatPoint labelSize = line.getLabelSize();
      if (labelSize == null) {
        this.height += FLAT_LABEL_GAP;
        continue;
      }

      this.height += labelSize.getHeight();
      this.width = Math.max(labelSize.getWidth(), this.width);
    }
  }

  static DNode newVirtualNode(double nodeSep, GraphContainer container) {
    DNode node = new DNode(null, 20, 1, nodeSep);
    node.setContainer(container);
    return node;
  }

  Node getNode() {
    return node;
  }

  NodeAttrs nodeAttrs() {
    return nodeAttrs;
  }

  void setNodeAttrs(NodeAttrs nodeAttrs) {
    this.nodeAttrs = nodeAttrs;
  }

  /**
   * @return 顶点是否是虚拟顶点
   */
  boolean isVirtual() {
    return node == null;
  }

  /**
   * @return 顶点是否是虚拟label顶点
   */
  boolean isLabelNode() {
    return labelLine != null;
  }

  /**
   * 设置label line。
   *
   * @param labelLine label line
   */
  void setLabelLine(Line labelLine) {
    this.labelLine = labelLine;
  }

  /**
   * @return 顶点是否是FlatEdge顶点
   */
  boolean isFlatLabelNode() {
    return flatLabelLine != null;
  }

  int getRankIgnoreModel() {
    int s = status;
    switchNormalModel();
    int rank = getRank();
    this.status = s;
    return rank;
  }

  int getRank() {
    if (isNormalModel()) {
      return rank;
    } else if (isAuxModel()) {
      return auxRank;
    } else {
      throw new IllegalStateException("Node unknow statusl");
    }
  }

  void setRank(int rank) {
    if (isNormalModel()) {
      this.rank = rank;
    } else if (isAuxModel()) {
      this.auxRank = rank;
    } else {
      throw new IllegalStateException("Node unknow statusl");
    }
  }

  void setAuxRank(int auxRank) {
    this.auxRank = auxRank;
  }

  int getLow() {
    return low;
  }

  void setLow(int low) {
    this.low = low;
  }

  int getLim() {
    return lim;
  }

  void setLim(int lim) {
    this.lim = lim;
  }

  double getMedian() {
    return median;
  }

  void setMedian(double median) {
    this.median = median;
  }

  @Override
  public double getX() {
    return x;
  }

  void setX(double x) {
    this.x = x;
  }

  @Override
  public double getY() {
    return y;
  }

  void setY(double y) {
    this.y = y;
  }

  @Override
  public double getWidth() {
    return width;
  }

  void setWidth(int width) {
    this.width = width;
  }

  @Override
  public double getHeight() {
    return height;
  }

  void setHeight(int height) {
    this.height = height;
  }

  boolean isNormalModel() {
    return (status << RANK_MODEL_BIT >>> RANK_MODEL_BIT) == 0;
  }

  boolean isAuxModel() {
    return (status << RANK_MODEL_BIT >>> RANK_MODEL_BIT) == AUX_MODE;
  }

  boolean notAdjust() {
    return (status & NOT_ADJSUT_MID) == NOT_ADJSUT_MID;
  }

  void switchAuxModel() {
    status |= AUX_MODE;
  }

  void switchNormalModel() {
    status >>= 1;
    status <<= 1;
  }

  void markNotAdjustMid() {
    status |= NOT_ADJSUT_MID;
  }

  int getSelfLoopCount() {
    return selfLines == null ? 0 : selfLines.size();
  }

  void addSelfLine(DLine line) {
    if (line == null) {
      return;
    }

    if (selfLines == null) {
      selfLines = new ArrayList<>(2);
    }
    selfLines.add(line);
  }

  void sortSelfLine(Comparator<DLine> lineComparator) {
    if (lineComparator == null || CollectionUtils.isEmpty(selfLines)) {
      return;
    }

    selfLines.sort(lineComparator);
  }

  /**
   * 返回对应索引的自环边。
   *
   * @param index 自环边的索引
   * @return 自环边
   * @throws IndexOutOfBoundsException 超出范围的索引
   */
  DLine selfLine(int index) {
    return CollectionUtils.isEmpty(selfLines) ? null : selfLines.get(index);
  }

  /**
   * @return 是否含有子环
   */
  boolean haveSelfLine() {
    return CollectionUtils.isNotEmpty(selfLines);
  }

  List<DLine> getSelfLines() {
    return selfLines == null ? Collections.emptyList() : selfLines;
  }

  void initNodeSizeExpander(DrawGraph drawGraph) {
    if (isVirtual() || !haveSelfLine() || nodeSizeExpander != null) {
      return;
    }

    Splines splines = drawGraph.getGraphviz().graphAttrs().getSplines();
    if (splines == Splines.ORTHO) {
      nodeSizeExpander = new OrthoNodeSizeExpander(this);
    } else {
      nodeSizeExpander = new PortNodeSizeExpander(drawGraph, this);
    }
  }

  DLine getFlatLabelLine() {
    return flatLabelLine;
  }

  double leftWidth() {
    if (isLabelNode()) {
      return 0;
    }
    double lw = nodeShape().leftWidth(width);
    if (nodeSizeExpander != null) {
      lw += nodeSizeExpander.getLeftWidthOffset();
    }
    return lw;
  }

  double rightWidth() {
    if (isLabelNode()) {
      return width;
    }

    double rw = nodeShape().rightWidth(width);
    if (nodeSizeExpander != null) {
      rw += nodeSizeExpander.getRightWidthOffset();
    }
    return rw;
  }

  double topHeight() {
    double th = nodeShape().topHeight(height);
    if (nodeSizeExpander != null) {
      th += nodeSizeExpander.getTopHeightOffset();
    }
    return th;
  }

  double bottomHeight() {
    double bh = nodeShape().bottomHeight(height);
    if (nodeSizeExpander != null) {
      bh += nodeSizeExpander.getBottomHeightOffset();
    }
    return bh;
  }

  double realLeftWidth() {
    return nodeShape().leftWidth(width);
  }

  double realRightWidth() {
    if (isLabelNode()) {
      return width;
    } else {
      return nodeShape().rightWidth(width);
    }
  }

  double realTopHeight() {
    return nodeShape().topHeight(height);
  }

  double realBottomHeight() {
    return nodeShape().bottomHeight(height);
  }

  @Override
  public double getLeftBorder() {
    if (isLabelNode()) {
      return getX() - leftWidth();
    }
    return getX() - realLeftWidth();
  }

  @Override
  public double getRightBorder() {
    if (isLabelNode()) {
      return getX() + rightWidth();
    }
    return getX() + realRightWidth();
  }

  @Override
  public double getUpBorder() {
    return getY() - realTopHeight();
  }

  @Override
  public double getDownBorder() {
    return getY() + realBottomHeight();
  }

  double getNodeSep() {
    return nodeSep;
  }

  void nodeSepHalving() {
    this.nodeSep /= 2;
  }

  String name() {
    if (isVirtual() || nodeAttrs == null) {
      if (isLabelNode()) {
        return labelLine.lineAttrs().getLabel();
      } else {
        return String.valueOf(hashCode());
      }
    } else {
      return nodeAttrs.getLabel() != null
          ? nodeAttrs.getLabel() : "none";
    }
  }

  int getAuxRank() {
    return auxRank;
  }

  int getRankIndex() {
    return rankIndex;
  }

  void setRankIndex(int rankIndex) {
    if (status == AUX_MODE) {
      return;
    }
    this.rankIndex = rankIndex;
  }

  Line getLabelLine() {
    return labelLine;
  }

  @Override
  public NodeShape nodeShape() {
    if (isVirtual() || nodeAttrs == null) {
      return NodeShapeEnum.CIRCLE;
    }

    return nodeAttrs.getNodeShape();
  }

  GraphContainer getContainer() {
    return container;
  }

  void setContainer(GraphContainer container) {
    this.container = container;
  }

  @Override
  public String toString() {
    return "{name=" + name() + ",rank=" + getRank() + ",width=" + width + "}";
  }
}
