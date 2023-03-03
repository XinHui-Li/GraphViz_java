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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pers.jyb.graph.def.AbstractDirectedEdge;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.attributes.LineStyle;

class DLine extends AbstractDirectedEdge<DNode, DLine> {

  private static final long serialVersionUID = -4923098199188113451L;

  /*
   * 原始边
   * */
  private final Line line;

  /*
   * 边的切值
   * */
  private double cutVal;

  /*
   * 所有合并的平行边
   */
  private List<DLine> parallelLineRecord;

  /*
   * 边两个顶点之间进行网络单纯性法的限制
   */
  private int limit;

  private final boolean realTimeLimit;

  /*
   * 边label的尺寸
   */
  private final FlatPoint labelSize;

  private final LineAttrs lineAttrs;

  DLine(DNode left, DNode right, Line line,
        LineAttrs lineAttrs, double weight, int limit) {
    this(left, right, line, lineAttrs, weight, limit, null);
  }

  DLine(DNode left, DNode right, double weight, int limit, boolean realTimeLimit) {
    this(left, right, null, null, weight, limit, null, realTimeLimit);
  }

  DLine(DNode left, DNode right, Line line,
        LineAttrs lineAttrs, double weight,
        int limit, FlatPoint labelSize) {
    this(left, right, line, lineAttrs, weight, limit, labelSize, false);
  }

  DLine(DNode left, DNode right, Line line,
        LineAttrs lineAttrs, double weight, int limit,
        FlatPoint labelSize, boolean realTimeLimit) {
    super(left, right, weight);

    this.line = line;
    this.limit = limit;
    this.labelSize = labelSize;
    if (line != null) {
      Asserts.nullArgument(lineAttrs, "lineAttrs");
    }
    this.lineAttrs = lineAttrs;
    this.realTimeLimit = realTimeLimit;
  }

  Line getLine() {
    return line;
  }

  LineAttrs lineAttrs() {
    return lineAttrs;
  }

  DNode getLowRankNode() {
    return from().getRankIgnoreModel() < to().getRankIgnoreModel() ? from() : to();
  }

  DNode getLargeRankNode() {
    return from().getRankIgnoreModel() >= to().getRankIgnoreModel() ? from() : to();
  }

  double getCutVal() {
    return cutVal;
  }

  void setCutVal(double cutVal) {
    this.cutVal = cutVal;
  }

  void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * 网络单纯形法的边长度限制。
   *
   * @return 网络单纯形法的边长度限制
   */
  int limit() {
    if (realTimeLimit) {
      return (int) from().rightWidth() + limit + (int) to().leftWidth();
    }
    return limit;
  }

  /**
   * 边的松弛量。
   *
   * @return 松弛量
   */
  int slack() {
    return to().getRank() - from().getRank();
  }

  /**
   * 边可以减少的长度
   *
   * @return 边可以减少的长度
   */
  int reduceLen() {
    int slack = Math.abs(slack());

    return slack - limit();
  }

  /**
   * 返回是否是平行边聚合边。
   *
   * @return 是否是平行边聚合边
   */
  boolean isParallelMerge() {
    return CollectionUtils.isNotEmpty(parallelLineRecord);
  }

  /**
   * 返回边的两个顶点是否是同一层级。
   *
   * @return 边的两个顶点是否是同一层级
   */
  boolean isSameRank() {
    return from().getRankIgnoreModel() == to().getRankIgnoreModel();
  }

  /**
   * 返回边是否是同层级相邻的。
   *
   * @return 边是否是同层级相邻的
   */
  boolean isSameRankAdj() {
    return isSameRank() && Math.abs(from().getRankIndex() - to().getRankIndex()) == 1;
  }

  /**
   * 返回平行边数量
   *
   * @return 返回平行边数量
   */
  int getParallelNums() {
    return CollectionUtils.isEmpty(parallelLineRecord) ? 1 : parallelLineRecord.size();
  }

  /**
   * Return parallel lines.
   *
   * @return parallel lines
   */
  public List<DLine> getParallelLineRecord() {
    return CollectionUtils.isEmpty(parallelLineRecord)
        ? Collections.singletonList(this)
        : Collections.unmodifiableList(parallelLineRecord);
  }

  /**
   * 根据平行边的编号返回对应的平行边，如果没有平行边，永远返回当前边。
   *
   * @param no 平行边的编号
   * @return 指定平行边
   * @throws IndexOutOfBoundsException 非法的平行边编号
   */
  DLine parallelLine(int no) {
    return CollectionUtils.isEmpty(parallelLineRecord) ? this : parallelLineRecord.get(no);
  }

  /**
   * 添加平行边记录。
   *
   * @param edge 平行边
   */
  void addParallelEdge(DLine edge) {
    if (parallelLineRecord == null) {
      parallelLineRecord = new ArrayList<>(2);
      parallelLineRecord.add(this);
    }

    parallelLineRecord.add(edge);
  }

  /**
   * 是否是真实的线。
   *
   * @return 是否是真实的线
   */
  boolean isVirtual() {
    return line == null;
  }

  /**
   * @return label节点尺寸
   */
  FlatPoint getLabelSize() {
    return labelSize;
  }

  /**
   * @return 边是否含有label
   */
  boolean haveLabel() {
    if (isParallelMerge()) {
      for (int i = 0; i < getParallelNums(); i++) {
        DLine l = parallelLine(i);
        if (l == this) {
          if (labelSize != null) {
            return true;
          }
        } else if (l.getLabelSize() != null) {
          return true;
        }
      }

      return false;
    }

    return labelSize != null;
  }

  /**
   * @return 是否是被翻转的边
   */
  boolean isReversal() {
    if (isVirtual()) {
      return false;
    }
    return line.tail() == to().getNode();
  }

  /**
   * @return 线段是否隐藏
   */
  boolean isHide() {
    if (isVirtual()) {
      return false;
    }

    return lineAttrs().getStyle() == LineStyle.INVIS;
  }

  @Override
  public double weight() {
    if (!isParallelMerge()) {
      return line != null ? line.weight() : weight;
    }

    double w = 0;
    for (int i = 0; i < parallelLineRecord.size(); i++) {
      DLine l = parallelLineRecord.get(i);
      if (l.isVirtual()) {
        continue;
      }

      w += Optional.ofNullable(l.lineAttrs().getWeight()).orElse(1D);
    }

    return w;
  }

  @Override
  public DLine reverse() {
    return new DLine(right, left, line, lineAttrs,
                     weight, limit, labelSize, realTimeLimit);
  }

  @Override
  public DLine copy() {
    DLine repl = new DLine(left, right, line, lineAttrs,
                           weight, limit, labelSize, realTimeLimit);
    repl.cutVal = cutVal;
    return repl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DLine line1 = (DLine) o;
    return Double.compare(line1.cutVal, cutVal) == 0 &&
        limit == line1.limit &&
        Objects.equals(line, line1.line);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), line, cutVal, limit);
  }

  @Override
  public String toString() {
    return "{from:" + left + "," +
        "to:" + right + "," +
        "weight:" + weight + "," +
        "limit:" + limit + "," +
        "cutval:" + cutVal + "}";
  }
}
