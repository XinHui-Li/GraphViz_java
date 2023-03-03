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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.layout.Mark;
import pers.jyb.graph.viz.layout.dot.RankContent.RankNode;

/**
 * 第一步：弹出负切值的树边；
 * <p>
 * 第二步：找出替换此树边的非树边，此非树边的松弛量最小；
 * <p>
 * 第三步：从替换的非树边的两个端点开始一直向上寻找到第一个公共顶点，所有经过的树边都是需要改动切值的树边；
 * <p>
 * 第四步：重新计算low和lim和等级；
 * <p>
 * 第五步：如果顶点可以在多个层级内平移且不影响最终的边总和，顶点移向节点数量更少的层级；
 */
class NetworkSimplex {

  private static final Logger log = LoggerFactory.getLogger(NetworkSimplex.class);

  private RankContent rankContent;

  // 原图
  private final DotDigraph dotDigraph;

  private FeasiableTree feasiableTree;

  private Queue<ULine> negativeLine;

  // 需要修改切值的树边
  private ArrayList<ULine> updateCutvalLines;

  // 重新计算切值的路径的起始节点
  private DNode calcCutvalHead;

  // 是否是正整数的层级索引
  private final boolean positiveRank;

  private final double rankSep;

  public NetworkSimplex(FeasiableTree feasiableTree, int nsLimit, double rankSep,
                        Consumer<DNode[]> sortNodesConsumer) {
    this(feasiableTree, nsLimit, true, rankSep, sortNodesConsumer);
  }

  public NetworkSimplex(FeasiableTree feasiableTree, int nsLimit, boolean positiveRank,
                        double rankSep, Consumer<DNode[]> sortNodesConsumer) {
    Asserts.nullArgument(feasiableTree, "feasiableTree");
    Asserts.illegalArgument(
        feasiableTree.getDotDigraph() == null,
        "feasiableTree.getDotDigraph() can not be null"
    );
    Asserts.illegalArgument(rankSep < 0, "rankSpace (" + rankSep + ") must be > 0");
    this.feasiableTree = feasiableTree;
    this.dotDigraph = feasiableTree.getDotDigraph();
    this.negativeLine = feasiableTree.negativeLine();
    this.positiveRank = positiveRank;
    this.rankSep = rankSep;

    // 网络单纯形法，最佳层级分配
    networkSimplex(nsLimit);

    // 平衡顶点的层级 + 非连通图对齐
    alignUnconnectGraph(balance(sortNodesConsumer));

    // 清除中间对象的引用，辅助gc
    clear();
  }

  public RankContent getRankContent() {
    return rankContent;
  }

  /*
   * 网络单纯形法，计算无向树的切值，不断替换树边直至所有树边的切值不为负
   */
  private void networkSimplex(int nsLimit) {
    String prefix = null;
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled()) {
      prefix = "network simplex: ";
      log.debug("{} nodes={} edges={} maxiter={}",
                prefix,
                dotDigraph.vertexNum(),
                dotDigraph.edgeNum(),
                nsLimit
      );
    }

    ULine out;
    int count = 0;
    List<Set<DNode>> halfNodeRecord = null;

    while ((out = negativeTreeLine()) != null && count++ < nsLimit) {
      if (halfNodeRecord == null) {
        halfNodeRecord = new ArrayList<>(1);
      }
      halfNodeRecord.clear();
      ULine enter = findEnterLine(out, halfNodeRecord);

      if (enter == null) {
        continue;
      }

      enterLine(enter, out, halfNodeRecord.get(0));
      if (log.isDebugEnabled() && count % 100 == 0) {
        log.debug("{} {}", prefix, count);
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Network is done,total number of iterations is {},time is {}s", count,
                (System.currentTimeMillis() - start) / 1000);
    }
  }

  // 寻找替入边
  private ULine findEnterLine(ULine outLine, List<Set<DNode>> halfNodeRecord) {
    ULine[] minSlackLine = new ULine[]{null};
    int[] minSlack = new int[]{Integer.MAX_VALUE};

    Consumer<ULine> consumer = uLine -> {
      if (!FeasiableTree.isCross(outLine.getdLine(), uLine.getdLine()) || FeasiableTree
          .inTail(uLine.getdLine().from(), outLine.getdLine())) {
        return;
      }

      int slack = uLine.reduceLen();

      if (minSlackLine[0] == null || slack < minSlack[0]) {
        minSlackLine[0] = uLine;
        minSlack[0] = slack;
      }
    };

    halfNodeRecord.add(FeasiableTree.halfDfs(feasiableTree.graph(), outLine, consumer));

    return minSlackLine[0];
  }

  // 替入边
  private void enterLine(ULine enterLine, ULine outLine, Set<DNode> halfNodes) {
    /*
     * 1.找到所有需要改变切值的树边
     * 2.移除掉旧树边，添加新的树边
     * 3.更新节点的(low,lim)的值
     * 4.更新切值
     * */
    DotGraph tree = feasiableTree.tree();

    // 改变树边，并设置替入边的切值
    enterLine.getdLine().setCutVal(-outLine.getdLine().getCutVal());

    // 查找所有需要修改切值的边
    DNode root = findNeedUpdateCutvalLines(tree, enterLine);
    DNode largeLimNode = outLine.getdLine().from().getLim() > outLine.getdLine().to().getLim()
        ? outLine.getdLine().from()
        : outLine.getdLine().to();

    if (notInLimLowRange(root, largeLimNode)) {
      root = publicRoot(tree, root, largeLimNode, null);
    }

    tree.removeLine(outLine);
    tree.addEdge(enterLine);

    // 重新设置层级
    if (enterLine.reduceLen() != 0) {
      int r = enterLine.reduceLen();

      DNode t = FeasiableTree.inTail(outLine.either(), outLine.getdLine())
          ? outLine.either() : outLine.other(outLine.either());

      if (halfNodes.contains(t)) {
        r = -r;
      }

      for (DNode halfNode : halfNodes) {
        halfNode.setRank(halfNode.getRank() + r);
      }
    }

    // 改变low和lim的值，并且重置部分节点的层级
    new LowLimCalc(tree, root);

    // 更新边的切值
    updateCutval();

    // 清空所有需要修改切值的边
    updateCutvalLines.clear();
  }

  /*
   * 在不影响总跨度和的情况下，有些顶点的等级设置是在一定的区间内。平衡此内顶点，让顶点均匀的分布在
   * 每个层级内，这样就能够有更好的长宽显示比例。
   * */
  private Map<Integer, DNode> balance(Consumer<DNode[]> sortNodesConsumer) {
    DotGraph dotGraph = feasiableTree.graph();

    Map<Integer, DNode> connectLowRank = feasiableTree.isHaveUnconnectGraph()
        ? new HashMap<>()
        : null;
    this.rankContent = new RankContent(dotGraph, rankSep, positiveRank, sortNodesConsumer);

    // 每个顶点已贪婪的方式获取当前情况下最平衡的层级分配
    for (DNode node : dotGraph) {
      int connectNo = feasiableTree.getConnectNo(node);
      if (connectLowRank != null) {
        connectLowRank.compute(connectNo, (c, n) -> {
          if (n == null) {
            return node;
          }
          return n.getRank() < node.getRank() ? n : node;
        });
      }

      int currentRank = node.getRank();
      RankNode current = rankContent.get(currentRank);
      Integer preRank = current.pre != null ? current.pre.rankIndex() : null;
      Integer nextRank = current.next != null ? current.next.rankIndex() : null;

      // 层级边界节点无法参与移动
      if (preRank == null || nextRank == null) {
        continue;
      }

      double inAndOutWeight = 0D;
      // 当前顶点相邻顶点的上层节点的最大值
      int preMax = Integer.MIN_VALUE;
      // 当前顶点相邻顶点的下层节点的最小值
      int nextMin = Integer.MAX_VALUE;

      boolean canNotMove = false;

      // 循环顶点的所有出入度边
      for (ULine uLine : dotGraph.adjacent(node)) {
        DNode other = uLine.other(node);
        int otherRank = other.getRank();

        if (positiveRank) {
          if (otherRank < node.getRank()) {
            otherRank = otherRank + uLine.limit() - 1;
          } else {
            otherRank = otherRank - uLine.limit() + 1;
          }
        }

        if (otherRank < currentRank && (otherRank > preMax)) {
          preMax = otherRank;
        }

        if (otherRank > currentRank && otherRank < nextMin) {
          nextMin = otherRank;
        }

        // 如果没有移动空间了，直接跳过当前节点，避免不必要的循环
        if (canNotMove = (Objects.equals(preMax, preRank) && Objects.equals(nextMin, nextRank))) {
          break;
        }

        // 入度边
        if (isInEdge(node, uLine)) {
          inAndOutWeight += uLine.getdLine().weight();
        } else { // 出度边
          inAndOutWeight -= uLine.getdLine().weight();
        }
      }

      // 如果顶点不能移动，或者初入边的总权重不一致，或者最稀疏的层级就是当前，或者顶点只有入度或者出度边，不需要移动层级
      if (canNotMove
          || inAndOutWeight != 0
          || preMax == Integer.MIN_VALUE
          || nextMin == Integer.MAX_VALUE) {
        continue;
      }

      // 最稀疏的层级
      RankNode sparsestRank = current;
      // 选取区间内最小的层级
      RankNode preMaxNode = rankContent.get(preMax);
      RankNode nextMinNode = rankContent.get(nextMin);
      RankNode curNode = preMaxNode.next;

      while (curNode != null && curNode != nextMinNode) {
        if (curNode.size() >= sparsestRank.size() - 1) {
          curNode = curNode.next;
          continue;
        }

        // 记录最稀疏层级和层级的顶点数量
        sparsestRank = curNode;

        curNode = curNode.next;
      }

      if (sparsestRank == current) {
        continue;
      }

      updateRank(node, current, sparsestRank);
    }

    return connectLowRank;
  }

  // 修改顶点层级
  private void updateRank(DNode node, RankNode sourceNode, RankNode targetRank) {
    if (sourceNode == targetRank || node.getRank() != sourceNode.rankIndex()) {
      return;
    }

    // 移除掉旧顶点所在层级的记录
    sourceNode.remove(node);

    // 设置rank层级
    node.setRank(targetRank.rankIndex());
    targetRank.add(node);
  }

  /*
   * 寻找所有需要改动切值的边，从替入边的两个顶点开始，沿着生成树找到公共的第一个父节点，
   * 这个父节点在生成树中到达两个节点的路径就为所有需要调整切值的所有的边。
   * */
  private DNode findNeedUpdateCutvalLines(DotGraph tree, ULine enterLine) {
    DNode from = enterLine.getdLine().from();

    DNode to = enterLine.getdLine().to();

    DNode current = calcCutvalHead = from, root;

    // 找到替入边的两个顶点的公共的顶点，并添加from到此顶点的路径
    current = publicRoot(tree, to, current, this::addUpdateCutvalLines);
    root = current;

    // 添加公共顶点到to的路径
    while (current != to) {
      for (ULine uLine : tree.adjacent(current)) {
        DNode other = uLine.other(current);
        if (other.getLim() > current.getLim() || notInLimLowRange(other, to)) {
          continue;
        }

        current = other;

        addUpdateCutvalLines(uLine);
        break;
      }
    }

    return root;
  }

  private DNode publicRoot(DotGraph tree, DNode to, DNode current, Consumer<ULine> lineConsumer) {
    while (notInLimLowRange(current, to)) {
      for (ULine uLine : tree.adjacent(current)) {
        DNode other = uLine.other(current);
        if (other.getLim() < current.getLim()) {
          continue;
        }

        current = other;

        if (lineConsumer != null) {
          lineConsumer.accept(uLine);
        }
        break;
      }
    }
    return current;
  }

  /*
   * 更新所有需要更新切值的边的切值。并且这些树边的切值都可以通过相邻树边的切值来计算。
   * */
  private void updateCutval() {
    if (CollectionUtils.isEmpty(updateCutvalLines)) {
      return;
    }

    DotGraph tree = feasiableTree.tree();

    // 从改动切值的边路径组成的节点链表的头部开始计算
    DNode current = calcCutvalHead;
    for (ULine updateCutvalLine : updateCutvalLines) {
      // 计算切值
      double cutval = FeasiableTree.calcCutValByAdjTreeLine(
          feasiableTree.graph(),
          current,
          updateCutvalLine,
          tree::containEdge
      );

      updateCutvalLine.getdLine().setCutVal(cutval);
      current = updateCutvalLine.other(current);

      // 如果修改后的树边切值小于0，重新扔进计算切值的队列
      if (cutval < 0) {
        negativeLine.offer(updateCutvalLine);
      }
    }
  }

  // 判断某个顶点target的lim的值是否不在另外一个顶点source的区间[low,lim)内
  private boolean notInLimLowRange(DNode source, DNode target) {
    return source.getLow() > target.getLim() || source.getLim() < target.getLim();
  }

  // 添加替入边
  private void addUpdateCutvalLines(ULine uLine) {
    if (updateCutvalLines == null) {
      updateCutvalLines = new ArrayList<>();
    }

    updateCutvalLines.add(uLine);
  }

  // 寻找树中负权重的边
  private ULine negativeTreeLine() {
    if (CollectionUtils.isEmpty(negativeLine)) {
      return null;
    }

    ULine negative;
    do {
      if (CollectionUtils.isEmpty(negativeLine)) {
        return null;
      }
      negative = negativeLine.poll();
    } while (negative != null && negative.getdLine().getCutVal() >= 0);

    return negative;
  }

  // 判断相对于顶点node来说，uLine是否是入边
  private boolean isInEdge(DNode node, ULine uLine) {
    return uLine.getdLine().to() == node;
  }

  private void alignUnconnectGraph(Map<Integer, DNode> connectLowRank) {
    if (connectLowRank == null) {
      return;
    }

    DNode basic = null;
    for (DNode source : connectLowRank.values()) {
      if (source.getRank() == rankContent.minRank()) {
        basic = source;
      }
    }

    Set<DNode> mark = new HashSet<>();
    for (DNode source : connectLowRank.values()) {
      if (basic == null || basic.getRank() == source.getRank()) {
        basic = source;
        continue;
      }

      int rankOffset = source.getRank() - basic.getRank();
      dfs(mark, source, rankOffset);
    }
  }

  private void dfs(Set<DNode> mark, DNode node, int rankOffset) {
    mark.add(node);
    RankNode sourceRankNode = rankContent.get(node.getRank());
    RankNode targetRankNode = rankContent.get(node.getRank() - rankOffset);
    if (sourceRankNode == targetRankNode) {
      return;
    }

    updateRank(node, sourceRankNode, targetRankNode);

    for (ULine uLine : feasiableTree.tree().adjacent(node)) {
      DNode other = uLine.other(node);

      if (mark.contains(other)) {
        continue;
      }

      dfs(mark, other, rankOffset);
    }
  }

  private void clear() {
    updateCutvalLines = null;
    negativeLine = null;
    feasiableTree = null;
  }

  private static class LowLimCalc extends Mark<DNode> {

    // 逆后序栈节点计数
    private int reserveCount;

    // 记录后续顶点中的最小值
    private int low = Integer.MAX_VALUE;

    private DNode root;

    private LowLimCalc(DotGraph tree, DNode node) {
      super(tree.vertexNum());

      this.root = node;

      reserveCount = node.getLow() - 1;

      dfs(tree, node);
    }

    // 深度优先遍历，目的为了记录可行树和逆后序排序
    private void dfs(DotGraph tree, DNode v) {
      mark(v);
      // 记录当前顶点后续顶点中最小的lim的顶点的lim
      int tmpLow = Integer.MAX_VALUE;

      for (ULine e : tree.adjacent(v)) {
        DNode w = e.other(v);

        if (isMark(w) || !isRightNode(w)) {
          continue;
        }

        dfs(tree, w);
        // 记录下目前为止当前节点相邻节点的最小值
        tmpLow = Math.min(tmpLow, low);
        low = Integer.MAX_VALUE;
      }

      int lim = ++reserveCount;
      low = Math.min(tmpLow, lim);

      v.setLow(low);
      v.setLim(lim);
    }

    private boolean isRightNode(DNode node) {
      return node != root && node.getLim() >= root.getLow() && node.getLim() < root.getLim();
    }
  }
}
