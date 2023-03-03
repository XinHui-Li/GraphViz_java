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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import pers.jyb.graph.def.Graphs;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.layout.Mark;

/**
 * 第一步：根据原图生成一个无向树；
 * <p>
 * 第二步：根据此无向树生成顶点的(low,lim)标志；
 * <p>
 * 第三步：把生成树的所有树边都设置成紧的，并计算顶点的等级；
 * <p>
 * 第四步：生成树边的切值；
 * <p>
 */
class FeasiableTree {

  // 原图
  private final DotDigraph dotDigraph;

  // 所有为负切值的树边
  private Queue<ULine> negativeLine;

  // 无向图
  private final DotGraph graph;

  // 无向树
  private final DotGraph tree;

  private final boolean haveUnconnectGraph;

  private final Map<DNode, Integer> nodeConnectRecord;

  FeasiableTree(DotDigraph digraph) {
    if (Graphs.isEmpty(digraph)) {
      throw new IllegalArgumentException("Graph can not be empty");
    }
    this.dotDigraph = digraph;

    // 初始等级分配
    RankInit rankInit = new RankInit(digraph);
    this.graph = rankInit.graph;
    // 遍历开始顶点，根据图的连通性来
    Collection<DNode> sources = rankInit.sources;
    this.tree = rankInit.tree;
    this.haveUnconnectGraph = sources.size() > 1;
    this.nodeConnectRecord = rankInit.nodeConnectRecord;

    // 顶点的(low,lim)，初始rank分配，生成树的边的初始切值计算
    if (digraph.edgeNum() != 0) {
      PropInit propInit = new PropInit(digraph, graph, tree, sources);

      this.negativeLine = propInit.negativeLine;
    }
  }

  public boolean isHaveUnconnectGraph() {
    return haveUnconnectGraph;
  }

  /**
   * 返回节点的连通分量。
   *
   * @param node 节点
   * @return 节点的连通分量
   */
  int getConnectNo(DNode node) {
    if (nodeConnectRecord == null) {
      return 1;
    }

    Integer connectNo = nodeConnectRecord.get(node);
    return connectNo != null ? connectNo : 1;
  }

  /**
   * @return 原有向图
   */
  DotDigraph getDotDigraph() {
    return dotDigraph;
  }

  /**
   * 如果生成树有切值为负的边，返回此边，否则返回null。
   *
   * @return 生成树中切值为负的最小边
   */
  Queue<ULine> negativeLine() {
    return negativeLine;
  }

  /**
   * @return 返回原图的无向图
   */
  DotGraph graph() {
    return graph;
  }

  /**
   * @return 返回树图
   */
  DotGraph tree() {
    return tree;
  }

  /**
   * 判断节点是否是在树边分割的两个组件的tail部分。
   *
   * @param node     需要判断的node
   * @param treeLine 树边
   * @return true - 节点在tail组件 false - 节点在head组件
   */
  static boolean inTail(DNode node, DLine treeLine) {
    DNode from = treeLine.from();
    DNode to = treeLine.to();

    boolean directed;
    DNode tail = (directed = from.getLim() < to.getLim()) ? from : to;

    return directed
        ==
        (tail.getLow() <= node.getLim() && tail.getLim() >= node.getLim());
  }

  /**
   * 判断某条边相对于某条树边来说是否是横跨边。
   *
   * @param treeLine 树边
   * @param line     判断边
   * @return 是否是横跨边
   */
  static boolean isCross(DLine treeLine, DLine line) {
    DNode from = line.from();
    DNode to = line.to();
    return inTail(from, treeLine) ^ inTail(to, treeLine);
  }

  /**
   * 判断某条边相对于某条树边来说的横跨head和tail的值，如果没有横跨就是0。
   *
   * @param treeLine 树边
   * @param line     判断边
   * @return 增量权重值
   */
  static double lineCrossVal(DLine treeLine, DLine line) {
    DNode from = line.from();
    DNode to = line.to();

    boolean fromInTail;
    boolean isCross = (fromInTail = inTail(from, treeLine)) ^ inTail(to, treeLine);

    if (!isCross) {
      return 0;
    }

    if (fromInTail) {
      return line.weight();
    }

    return -line.weight();
  }

  /**
   * 通过只迭代节点较少的组件来计算切值，避免扫描所有边。
   *
   * @param graph    无向图
   * @param treeLine 树边
   * @return 计算后的树边切值
   */
  static double halfDfsCalcCutVal(DotGraph graph, ULine treeLine) {
    double[] cutVal = new double[]{0D};
    Consumer<ULine> consumer = uLine ->
        cutVal[0] += lineCrossVal(
            treeLine.getdLine(),
            uLine.getdLine()
        );

    halfDfs(graph, treeLine, consumer);

    return cutVal[0];
  }

  /**
   * 通过只迭代节点较少的组件，寻找所有横跨两个组件的边，并执行一些消费行为。
   *
   * @param graph         无向图
   * @param treeLine      树边
   * @param dLineConsumer 横跨边的消费者
   */
  static Set<DNode> halfDfs(DotGraph graph,
                            ULine treeLine,
                            Consumer<ULine> dLineConsumer) {
    Objects.requireNonNull(graph);
    Objects.requireNonNull(treeLine);

    if (dLineConsumer == null) {
      return Collections.emptySet();
    }

    DLine treeDLine = treeLine.getdLine();
    DNode from = treeDLine.from();
    DNode to = treeDLine.to();

    DNode tailNode = inTail(to, treeDLine) ? to : from;
    DNode headNode = to == tailNode ? from : to;

    Set<DNode> queenRecord = new HashSet<>();
    Queue<DNode> halfNodeQueen = new LinkedList<>();

    // 迭代节点更加少的一侧组件
    DNode startNode = headNode.getLim() - headNode.getLow() < tailNode.getLim() - tailNode.getLow()
        ? headNode
        : tailNode;
    halfNodeQueen.offer(startNode);
    queenRecord.add(startNode);

    while (!halfNodeQueen.isEmpty()) {
      DNode node = halfNodeQueen.poll();

      for (ULine uLine : graph.adjacent(node)) {
        if (isCross(treeDLine, uLine.getdLine())) {
          dLineConsumer.accept(uLine);
          continue;
        }

        DNode other = uLine.other(node);

        if (!queenRecord.contains(other)) {
          halfNodeQueen.offer(other);
          queenRecord.add(other);
        }
      }
    }

    return queenRecord;
  }

  /**
   * 当计算某个树边的切值的时候，如果树边的某个顶点{@code node}符合条件：与顶点相连的所有树边（除去要计算的树边本身）的切值均已知，
   * 那么就可以使用此方法计算这个树边的切值。需要提供生成树的原图，并且提供一个断言{@code isTree}判断原图中的某条边是否属于生成树 当中的树边，此方法就可以计算出此树边的切值。
   *
   * @param graph    生成树的原图
   * @param node     树边的某个顶点，与此顶点相连的其他树边的切值都已经计算出来
   * @param treeLine 需要计算切值的树边
   * @param isTree   判断边是否是树边的逻辑
   * @return 边的切值
   * @throws NullPointerException 空参数
   */
  static double calcCutValByAdjTreeLine(DotGraph graph,
                                        DNode node,
                                        ULine treeLine,
                                        Predicate<ULine> isTree) {
    if (graph == null || node == null || treeLine == null || isTree == null) {
      throw new NullPointerException();
    }

    /*
     * 关键点： 当某条树边某个顶点v所有的相邻树边的（除它本身以外）切值均已知，那么这条树边的切值可以总结为：
     * 1.当顶点v的某条边于树边的出入度一致
     * （1）边为树边 减树边的切值，再加上树边的权重值
     * （2）边为非树边 加上权重值
     * 2.当顶点v的某条边于树边的出入度不一致
     * （1）边为树边加树边的切值，再减去树边的权重值
     * （2）边为非树边 减去权重值
     * */
    double cutVal = 0;
    for (ULine uLine : graph.adjacent(node)) {
      // 跳过现在要计算的树边
      if (uLine.getdLine() == treeLine.getdLine()) {
        cutVal += treeLine.getdLine().weight();
        continue;
      }

      // 出入度一致
      if (isSameInOut(treeLine.getdLine(), uLine.getdLine())) {
        // 是树边
        if (isTree.test(uLine)) {
          cutVal = cutVal - uLine.getdLine().getCutVal() + uLine.getdLine().weight();
        } else { // 非树边
          cutVal += uLine.getdLine().weight();
        }
      } else { // 出入度不一致
        // 是树边
        if (isTree.test(uLine)) {
          cutVal = cutVal + uLine.getdLine().getCutVal() - uLine.getdLine().weight();
        } else { // 非树边
          cutVal -= uLine.getdLine().weight();
        }
      }
    }

    return cutVal;
  }

  // 指定树边和顶点，输入另一条此顶点的相邻边，判断是否出入度一致
  private static boolean isSameInOut(DLine treeLine, DLine targetLine) {
    return treeLine.from() == targetLine.from() || treeLine.to() == targetLine.to();
  }

  // 初始等级分配
  private static class RankInit extends Mark<DNode> {

    // 无向图
    private final DotGraph graph;

    // 初始可行生成树
    private final DotGraph tree;

    // 遍历开始顶点
    private Collection<DNode> sources;

    private Map<DNode, Integer> nodeConnectRecord;

    public RankInit(DotDigraph dotDigraph) {
      int edgeNum = dotDigraph.edgeNum();
      this.graph = new DotGraph(dotDigraph.vertexNum(), edgeNum);

      if (edgeNum == 0) {
        this.tree = graph;
      } else {
        this.tree = new DotGraph(dotDigraph.vertexNum());
      }

      Queue<ULine> minLines = new PriorityQueue<>(Comparator.comparing(ULine::reduceLen));

      // 初始层级生成，保证from顶点的层级高于to顶点的层级
      initRank(dotDigraph, minLines);

      // 初始可行树的生成
      generateTree(minLines);

      clear();
    }

    private void initRank(DotDigraph dotDigraph, Queue<ULine> minLines) {
      // 逆后续
      for (DNode node : dotDigraph) {
        if (isMark(node)) {
          continue;
        }

        dfs(dotDigraph, minLines, node);
      }

      connectSource();
    }

    private void connectSource() {
      clear();

      Map<Integer, DNode> sourceMap = new HashMap<>(1);
      int connectNo = 1;
      for (DNode node : graph) {
        if (isMark(node)) {
          continue;
        }

        dfs(node, connectNo++, sourceMap);
      }

      sources = sourceMap.values();
    }

    private void dfs(DotDigraph dotDigraph, Queue<ULine> minLines, DNode from) {
      mark(from);
      graph.add(from);
      int minRank = 0;

      ULine minLine = null;
      for (DLine dLine : dotDigraph.adjacent(from)) {
        DNode to = dLine.other(from);

        ULine uLine = new ULine(dLine.from(), to, dLine, dLine.weight());
        graph.addEdge(uLine);

        if (!isMark(to)) {
          dfs(dotDigraph, minLines, to);
        }

        minRank = Math.min(minRank, to.getRank() - dLine.limit());
        from.setRank(minRank);

        if (minLine == null || minLine.reduceLen() > uLine.reduceLen()) {
          minLine = uLine;
        }
      }

      if (minLine != null) {
        minLines.add(minLine);
      }
    }

    private void dfs(DNode node, int connectNo, Map<Integer, DNode> sourceMap) {
      mark(node);

      if (connectNo > 1) {
        if (nodeConnectRecord == null) {
          nodeConnectRecord = new HashMap<>();
        }
        nodeConnectRecord.put(node, connectNo);
      }

      DNode sn = sourceMap.get(connectNo);
      if (sn == null || node.getRank() < sn.getRank()) {
        sourceMap.put(connectNo, node);
      }

      for (ULine uLine : graph.adjacent(node)) {
        DNode other = uLine.other(node);

        if (isMark(other)) {
          continue;
        }

        dfs(other, connectNo, sourceMap);
      }
    }

    private void generateTree(Queue<ULine> minLines) {
      Queue<ULine> treeAdjacentEdges = new PriorityQueue<>(
          Comparator.comparing(ULine::reduceLen)
      );

      while (tree.vertexNum() < graph.vertexNum() && CollectionUtils.isNotEmpty(minLines)) {
        treeAdjacentEdges.clear();
        treeAdjacentEdges.add(minLines.poll());

        while (CollectionUtils.isNotEmpty(treeAdjacentEdges)) {
          ULine uLine = treeAdjacentEdges.poll();

          if (uLine == null || tree.containEdge(uLine)) {
            continue;
          }

          DLine dLine = uLine.getdLine();
          DNode next = tree.containNode(dLine.from()) ? dLine.to() : dLine.from();

          if (tree.containNode(next)) {
            continue;
          }

          // 如果此时加入的树边不是“紧的”，重新设置所有树中顶点的坐标让其变为“紧边”
          int reduceLen = dLine.reduceLen();
          if (reduceLen != 0) {
            int delta = next == dLine.from() ? -reduceLen : reduceLen;

            for (DNode dNode : tree) {
              dNode.setRank(dNode.getRank() + delta);
            }

            // 重新设置优先队列，上步操作可能破坏排序
            treeAdjacentEdges = newMinQueue(treeAdjacentEdges);
          }

          // 把新进入树的顶点的相邻未进入树的边加入队列
          addAdjEdgesQueen(treeAdjacentEdges, uLine);

          tree.addEdge(uLine);
        }
      }
    }

    private void addAdjEdgesQueen(Queue<ULine> treeAdjacentEdges, ULine uLine) {
      DNode from = uLine.getdLine().from();
      DNode to = uLine.getdLine().to();

      if (!tree.containNode(from)) {
        addAdjEdgesQueen(treeAdjacentEdges, uLine, from);
      }

      if (!tree.containNode(to)) {
        addAdjEdgesQueen(treeAdjacentEdges, uLine, to);
      }
    }

    private void addAdjEdgesQueen(Queue<ULine> treeAdjacentEdges, ULine uLine, DNode node) {
      for (ULine line : graph.adjacent(node)) {
        if (line == uLine ||
            (tree.containNode(line.getdLine().from()) && tree.containNode(line.getdLine().to()))) {
          continue;
        }

        treeAdjacentEdges.offer(line);
      }
    }

    private Queue<ULine> newMinQueue(Queue<ULine> treeAdjacentEdges) {
      if (CollectionUtils.isEmpty(treeAdjacentEdges)) {
        return treeAdjacentEdges;
      }
      PriorityQueue<ULine> uLines = new PriorityQueue<>(
          treeAdjacentEdges.size(),
          Comparator.comparing(ULine::reduceLen)
      );

      uLines.addAll(treeAdjacentEdges);
      return uLines;
    }
  }

  // 属性设置
  private static class PropInit extends Mark<DNode> {

    // 逆后序栈节点计数
    private int reserveCount = 0;

    // 记录后续顶点中的最小值
    private int low = Integer.MAX_VALUE;

    // 所有边界结点，即断开树边后的两个组件中，必会有一个组件只含有一个顶点
    private Queue<DNode> cutQueen;

    // 计算边的切值是否已经计算
    private Set<DLine> lineCache;

    // 所有负切值的树边
    private Queue<ULine> negativeLine;

    // 原始的有向图
    private final DotDigraph dotDigraph;

    // 标记顶点是否是边界节点
    private final Set<DNode> isBorder;

    // 初始生成树
    private final DotGraph tree;

    // 记录顶点已经完成切值计算的数量和是否已经进入过计算切值的队列
    private Map<DNode, CutValRecord> nodeCountValRecord;

    private PropInit(DotDigraph dotDigraph, DotGraph graph, DotGraph tree, Collection<DNode> sourceNodes) {
      super(dotDigraph.vertexNum());
      this.dotDigraph = dotDigraph;
      this.tree = tree;

      this.isBorder = new HashSet<>();
      // 初始化节点的(low,lim)
      for (DNode source : sourceNodes) {
        if (isMark(source)) {
          continue;
        }
        dfs(source);
      }

      // 计算所有树边的切值
      computeCutVal(graph);
    }

    // 深度优先遍历，目的为了记录可行树和逆后序排序
    private void dfs(DNode v) {
      mark(v);
      // 记录当前顶点后续顶点中最小的lim的顶点的lim
      int tmpLow = Integer.MAX_VALUE;

      for (ULine e : tree.adjacent(v)) {
        DNode w = e.other(v);

        if (isMark(w)) {
          continue;
        }

        dfs(w);
        // 记录下目前为止当前节点相邻节点的最小值
        tmpLow = Math.min(tmpLow, low);
        low = Integer.MAX_VALUE;
      }

      // 叶子节点为边界节点，或者source节点在生成树中度数为1，source节点为边界节点
      if (tree.degree(v) == 1) {
        isBorder.add(v);
        offerCutQueen(v);
      }

      int lim = ++reserveCount;
      low = Math.min(tmpLow, lim);
      v.setLow(low);
      v.setLim(lim);
    }

    private void computeCutVal(DotGraph graph) {
      // 从树边的边界节点开始，一层一层的进入里层节点的方式计算切值
      while (CollectionUtils.isNotEmpty(cutQueen)) {
        DNode node = cutQueen.poll();

        // 边界节点的计算逻辑
        if (isBorder.contains(node)) {
          calcBorderCutVal(graph, node);
          continue;
        }

        // 普通节点相邻树边计算
        calcNormalCutVal(graph, node);
      }
    }

    // 计算普通节点的切值
    private void calcNormalCutVal(DotGraph graph, DNode node) {
      int degreeThreshold = tree.degree(node) - 1;
      for (ULine uLine : tree.adjacent(node)) {

        if (lineCacheContain(uLine.getdLine())) {
          continue;
        }

        // 证明当前顶点其他所有树边的切值都已经计算完成
        DNode nextNode;
        DNode other = uLine.other(node);
        if (getNodeHavedCalcLineNum(nextNode = node) == degreeThreshold
            || getNodeHavedCalcLineNum(nextNode = other) == tree.degree(other) - 1) {
          calcCutValByAdjNode(graph, nextNode, uLine);
        } else {
          // 直接循环所有横跨两个组件的边计算切值
          npCalcCutVal(graph, uLine);
        }

        // 新顶点扔入队列
        offerCutQueen(other);
      }

    }

    /*--------------------------------------树边各种场景的切值计算--------------------------------------*/

    // 计算边界节点的切值
    private void calcBorderCutVal(DotGraph graph, DNode border) {
      ULine uTreeLine = null;
      for (ULine uLine : tree.adjacent(border)) {
        uTreeLine = uLine;
        break;
      }

      // 肯定不能为null，否则是找到错误的边界节点
      if (uTreeLine == null) {
        throw new RuntimeException("Find the wrong border node!");
      }

      DLine treeLine = uTreeLine.getdLine();

      // 如果已经计算过切值，跳过
      if (lineCacheContain(treeLine)) {
        return;
      }

      double cutVal = 0;
      boolean borderIsFrom = treeLine.from() == border;
      for (ULine edge : graph.adjacent(border)) {

        if (borderIsFrom == (edge.getdLine().from() == border)) {
          cutVal += edge.getdLine().weight();
        } else {
          cutVal -= edge.getdLine().weight();
        }
      }

      // 设置切值边标记树边已经访问
      setCutValAndMarkTreeLine(cutVal, uTreeLine);

      // 将树边的另一个节点加入访问队列
      offerCutQueen(treeLine.other(border));
    }

    // 通过相邻边直接计算切值
    private void calcCutValByAdjNode(
        DotGraph graph,
        DNode node,
        ULine treeLine
    ) {
      // 设置切值并标记树边已经被访问
      setCutValAndMarkTreeLine(
          calcCutValByAdjTreeLine(graph, node, treeLine, tree::containEdge),
          treeLine
      );
    }

    // 通过穷举横跨两个组件的边来计算树边的切值
    private void npCalcCutVal(DotGraph graph, ULine treeLine) {
      setCutValAndMarkTreeLine(halfDfsCalcCutVal(graph, treeLine), treeLine);
    }

    /*--------------------------------------其他计算操作--------------------------------------*/

    private int getNodeHavedCalcLineNum(DNode node) {
      CutValRecord cutValRecord = getCutValRecord(node);
      return cutValRecord.calcNum;
    }

    private void increaseNodeHavedCalcLineNum(DNode node) {
      CutValRecord cutValRecord = getCutValRecord(node);

      cutValRecord.calcNum++;
    }

    private CutValRecord getCutValRecord(DNode node) {
      if (nodeCountValRecord == null) {
        nodeCountValRecord = new HashMap<>(dotDigraph.vertexNum());
      }

      CutValRecord cutValRecord = nodeCountValRecord.get(node);

      if (cutValRecord == null) {
        cutValRecord = new CutValRecord();
        nodeCountValRecord.put(node, cutValRecord);
      }

      return cutValRecord;
    }

    // 设置树边的切值，并且标记树边已经被计算，并且增加树边两个顶点的切值计数
    private void setCutValAndMarkTreeLine(double cutVal, ULine treeLine) {
      // 设置切值
      treeLine.getdLine().setCutVal(cutVal);

      if (lineCache == null) {
        lineCache = new HashSet<>();
      }

      // 标记访问
      lineCache.add(treeLine.getdLine());
      increaseNodeHavedCalcLineNum(treeLine.getdLine().from());
      increaseNodeHavedCalcLineNum(treeLine.getdLine().to());

      if (cutVal < 0) {
        if (negativeLine == null) {
          negativeLine = new LinkedBlockingQueue<>();
        }
        negativeLine.offer(treeLine);
      }
    }

    // 判断此条边是否已经被计算切值
    private boolean lineCacheContain(DLine treeLine) {
      if (lineCache == null) {
        return false;
      }

      return lineCache.contains(treeLine);
    }

    // 把顶点添加进入计算切值的队列
    private void offerCutQueen(DNode node) {
      if (cutQueen == null) {
        cutQueen = new LinkedBlockingQueue<>();
      }

      CutValRecord cutValRecord = getCutValRecord(node);
      // 还未计算完成
      if (!cutValRecord.isInCutQueen) {
        cutQueen.offer(node);
        cutValRecord.isInCutQueen = true;
      }
    }
  }

  private static class CutValRecord {

    private int calcNum;

    private boolean isInCutQueen;
  }
}