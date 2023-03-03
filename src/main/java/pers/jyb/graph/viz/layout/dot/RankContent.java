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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import pers.jyb.graph.def.BaseGraph;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.layout.dot.RankContent.RankNode;

class RankContent implements Iterable<RankNode> {

  private final double rankSep;

  int minRank = Integer.MAX_VALUE;

  // 是否是正整数的层级索引
  private final boolean positiveRank;

  int maxRank = Integer.MIN_VALUE;

  Map<Integer, RankNode> rankNodeMap;

  RankContent(BaseGraph<DNode> graph, double rankSep,
              boolean positiveRank, Consumer<DNode[]> sortNodesConsumer) {
    this.rankNodeMap = new HashMap<>();
    this.rankSep = rankSep;
    this.positiveRank = positiveRank;

    // init RankContent
    normalize(graph, sortNodesConsumer);
  }

  protected RankContent() {
    this.rankSep = 0;
    this.positiveRank = false;
  }

  // 最小层级开始重新排列
  private void normalize(BaseGraph<DNode> graph, Consumer<DNode[]> sortNodesConsumer) {
    // 按照顶点层级进行排序
    DNode[] nodes = graph.toArray();
    if (nodes == null) {
      return;
    }

    Arrays.sort(nodes, Comparator.comparing(DNode::getRank));
    if (sortNodesConsumer != null) {
      sortNodesConsumer.accept(nodes);
    }

    RankNode pre;
    RankNode current = null;
    Integer currentRankIndex = null;
    int deviation = 0;
    if (positiveRank) {
      deviation = 1 - nodes[0].getRank();
    }

    for (DNode node : nodes) {
      int rankIndex = node.getRank() + deviation;
      node.setRank(rankIndex);

      if (current == null || !Objects.equals(currentRankIndex, rankIndex)) {
        // 插入超出层级1的RankNode
        if (positiveRank && current != null && rankIndex - currentRankIndex > 1) {
          for (int i = currentRankIndex + 1; i < rankIndex; i++) {
            RankNode rankNode = get(i);
            if (rankNode == null) {
              rankNode = new RankNode(current, null, i);
              rankNode.rankSep = rankSep;
            }

            put(i, rankNode);
            current.next = rankNode;
            current = rankNode;
          }
        }

        pre = current;
        currentRankIndex = rankIndex;
        current = new RankNode(pre, null, currentRankIndex);
        current.rankSep = rankSep;

        put(currentRankIndex, current);

        if (pre != null) {
          pre.next = current;
        }
      }

      if (rankIndex < minRank) {
        minRank = rankIndex;
      }
      if (rankIndex > maxRank) {
        maxRank = rankIndex;
      }
      current.add(node);
    }
  }

  /**
   * 在层级索引为{@code idx}的层级之前插入一个新的只含label节点的层级，只会插入层级的链表结构当中。
   *
   * @param idx 插入的层级
   * @return 插入后的RankNode
   */
  RankNode insertLabelRankNode(int idx) {
    RankNode rankNode = rankNodeMap.get(idx);
    // 当前层级必须是正常的层级，并且层级的上一层级也必须是正常的层级
    if (rankNode == null || rankNode.noNormalNode() || (rankNode.pre != null && rankNode.pre
        .noNormalNode())) {
      return null;
    }

    if (rankNode.pre != null) {
      rankNode.pre.setRankSep(rankNode.pre.getRankSep() / 2);
    }

    RankNode r = new RankNode(rankNode.pre, rankNode, 0);
    r.setRankSep(
        rankNode.pre != null ? rankNode.pre.getRankSep() : rankNode.getRankSep());
    if (rankNode.pre != null) {
      rankNode.pre.next = r;
    }
    rankNode.pre = r;

    return r;
  }

  /**
   * 需要同步RankNode相关的信息。
   */
  void rankIndexSync() {
    int rank = minRank;
    int minR = minRank;
    RankNode current = get(minR);
    RankNode adj;

    do {
      adj = current.pre();
      if (adj != null) {
        adj.rankIndex = --rank;
        rankNodeMap.put(adj.rankIndex, adj);
        minRank = rank;
      }
      current = adj;
    } while (current != null);

    rank = minR;
    current = get(minR);
    do {
      adj = current.next();
      if (adj != null) {
        adj.rankIndex = ++rank;
        rankNodeMap.put(adj.rankIndex, adj);
        maxRank = Math.max(maxRank, rank);
      }
      current = adj;
    } while (current != null);
  }

  void put(Integer rankIndex, RankNode rankNode) {
    rankNodeMap.put(rankIndex, rankNode);
  }

  int size() {
    return rankNodeMap.size();
  }

  void remove(Object key) {
    rankNodeMap.remove(key);
  }

  RankNode get(Object rankIndex) {
    return rankNodeMap.get(rankIndex);
  }

  DNode rankPreNode(DNode node) {
    if (node == null) {
      return null;
    }

    RankNode rankNode = get(node.getRankIgnoreModel());
    if (rankNode == null) {
      return null;
    }

    int pi = node.getRankIndex() - 1;
    if (RankNode.isNotRightIndex(pi, rankNode.size())) {
      return null;
    }

    return rankNode.get(pi);
  }

  DNode rankNextNode(DNode node) {
    if (node == null) {
      return null;
    }

    RankNode rankNode = get(node.getRankIgnoreModel());
    if (rankNode == null) {
      return null;
    }

    int ni = node.getRankIndex() + 1;
    if (RankNode.isNotRightIndex(ni, rankNode.size())) {
      return null;
    }

    return rankNode.get(ni);
  }

  int minRank() {
    return minRank;
  }

  int maxRank() {
    return maxRank;
  }

  @Override
  public String toString() {
    return "RankContent{\n" +
        "rankNodeMap=" + rankNodeMap +
        ", \nminRank=" + minRank +
        ", \nmaxRank=" + maxRank +
        "\n}";
  }

  @Override
  public Iterator<RankNode> iterator() {
    return rankNodeMap.values().iterator();
  }

  /**
   * 图的单独层级信息
   */
  static class RankNode implements Iterable<DNode> {

    private double startY;
    private double endY;

    private ArrayList<DNode> nodes;

    private int normalNodeNum;

    private int rankIndex;

    double rankSep;

    RankNode next;

    RankNode pre;

    boolean labelRank;

    RankNode(RankNode pre, RankNode next, int rankIndex) {
      this(pre, next, rankIndex, false);
    }

    RankNode(RankNode pre, RankNode next, int rankIndex, boolean isLabelRank) {
      this.pre = pre;
      this.next = next;
      this.rankIndex = rankIndex;
      this.nodes = new ArrayList<>();
      this.labelRank = isLabelRank;
    }

    public boolean isLabelRank() {
      return labelRank;
    }

    DNode get(int idx) {
      if (idx < 0 || idx >= size()) {
        return null;
      }
      return nodes.get(idx);
    }

    DNode set(int idx, DNode node) {
      return nodes.set(idx, node);
    }

    boolean add(DNode node) {
      if (node == null) {
        return false;
      }

      if (!node.isVirtual()) {
        normalNodeNum++;
      }

      return nodes.add(node);
    }

    void add(int idx, DNode node) {
      nodes.add(idx, node);
    }

    boolean remove(Object obj) {
      if (nodes.remove(obj)) {
        if (!((DNode) obj).isVirtual()) {
          normalNodeNum--;
        }
        return true;
      }

      return false;
    }

    int size() {
      return nodes.size();
    }

    boolean isEmpty() {
      return nodes.isEmpty();
    }

    boolean noNormalNode() {
      return normalNodeNum <= 0;
    }

    int normalNodeNum() {
      return normalNodeNum;
    }

    DNode first() {
      if (size() == 0) {
        return null;
      }

      return get(0);
    }

    DNode last() {
      if (size() == 0) {
        return null;
      }

      return get(size() - 1);
    }

    void setRankIndex(int rankIndex) {
      this.rankIndex = rankIndex;
    }

    int rankIndex() {
      return rankIndex;
    }

    /**
     * @return 前层级
     */
    RankNode pre() {
      return pre;
    }

    /**
     * @return 后层级
     */
    RankNode next() {
      return next;
    }

    double getStartX() {
      if (nodes.isEmpty()) {
        throw new IllegalStateException("empty rank");
      }

      DNode first = nodes.get(0);
      return (int) (first.getX() - first.leftWidth());
    }

    double getEndX() {
      if (nodes.isEmpty()) {
        throw new IllegalStateException("empty rank");
      }

      DNode last = nodes.get(size() - 1);
      return (last.getX() + last.rightWidth());
    }

    double getStartY() {
      return startY;
    }

    void setStartY(double startY) {
      this.startY = startY;
    }

    double getEndY() {
      return endY;
    }

    double getHeight() {
      return endY - startY;
    }

    void setRankSep(double rankSep) {
      this.rankSep = rankSep;
    }

    double getRankSep() {
      return rankSep;
    }

    void setEndY(double endY) {
      this.endY = endY;
    }

    static boolean isNotRightIndex(int index, int size) {
      return index < 0 || index >= size;
    }

    void sort(Comparator<DNode> comparator) {
      Asserts.nullArgument(comparator, "comparator");
      if (CollectionUtils.isEmpty(nodes)) {
        return;
      }

      nodes.sort(comparator);
    }

    @Override
    public Iterator<DNode> iterator() {
      return nodes.iterator();
    }

    @Override
    public String toString() {
      return "{hascode="
          + hashCode()
          + ",pre="
          + (pre != null ? pre.hashCode() : null)
          + ",next="
          + (next != null ? next.hashCode() : null)
          + ",rank="
          + rankIndex
          + ",size="
          + nodes.size()
          + "}";
    }
  }
}
