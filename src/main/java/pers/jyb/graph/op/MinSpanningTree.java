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

package pers.jyb.graph.op;

import pers.jyb.graph.def.Edge;
import pers.jyb.graph.def.Graph;
import pers.jyb.graph.def.Graphs;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import pers.jyb.graph.def.UndirectedEdgeGraph;

/**
 * <p>加权图模型能自然的表示许多应用。在道路规划图中，每条边表示需要修的路线，而权值则可以表示路线成本。
 * 在电路图中，边表示导线，权值则可能表示导线的长度或成本，或是信号通过这条线路所需的时间。在这些情形中， 最令人感兴趣的自然而然就是将成本最小化，这对应的就是寻找一副加权无向图的<i>最小生成树</i>。
 *
 * <p><strong>切分定理</strong>
 * <p>切分定理表示把加权图中的所有顶点分为两个集合，检查横跨两个集合的所有边，并且寻找除这些边中权重值最
 * 小的那条边，这条横跨两个集合的权重最小的横切边一定属于最小生成树。
 *
 * <p><strong>切分过程</strong>
 * <p>寻找最小生成树的前提是保证无向图的连通性！在这个前提下，初始情况下，寻找图中任意一个顶点V，把整幅图切
 * 分为两个集合（仅含有V顶点的集合和除了V顶点的其他所有顶点的集合），然后慢慢的扩充仅含有V顶点的集合。这边 需要有几个动作：
 * <pre>
 *     1. 标记新访问的节点为已经访问
 *     2. 把这个顶点的所有邻边加入到一个优先队列
 *     3. 弹出优先队列的首位元素，即权重最小的边
 *     4. 如果这条边是有效的，把这条边添加到最小生成树，然后使用这条边中的没有访问的顶点继续步骤1，直到优先队列为空
 * </pre>
 * 在上述的描述当中，什么样的边是有效的？当已经访问的顶点中有两个顶点指向同一个未访问的顶点，并且在某次切分 当中两条边中的某一条正好作为最小横切边进入优先队列，此时另外一条边应当失效！对应的现象是，当优先队列弹出
 * 最小权重值的边，结果发现这条边的两个顶点都已经被访问。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 * @see Edge
 * @see UndirectedEdgeGraph
 */
public class MinSpanningTree<V, E extends Edge<V, E>>
    extends AccessMarked<V> {

  /**
   * 最小生成树
   */
  private final Queue<E> mst;

  /**
   * 最小生成树的权重
   */
  private Double weight;

  /**
   * 存储已访问的集合和未访问的集合的横切边
   */
  private final PriorityQueue<E> crossCutEdges;

  public MinSpanningTree(Graph.EdgeGraph<V, E> graph) {
    super(graph);
    crossCutEdges = new PriorityQueue<>();
    mst = new LinkedBlockingQueue<>(graph.vertexNum() - 1);
    visit(graph, Graphs.random(graph));
    while (!crossCutEdges.isEmpty()) {
      E minEdge = crossCutEdges.poll();
      V either = minEdge.either(), other = minEdge.other(either);
      /*
       * 比如第一个顶点为A，A跟B顶点有两条边，并且恰好为优先级前两位的边，
       * 当把最优的A连接B加入最小生成树，此时优先队列里面还有一条A连接B，
       * 它是此时优先级最高的，当把这条边弹出来的时候，发现已经B被访问了。
       * */
      if (isMarked(either) && isMarked(other)) // 去除无效的边
      {
        continue;
      }
      mst.offer(minEdge);
      if (!isMarked(either)) {
        visit(graph, either);
      }
      if (!isMarked(other)) {
        visit(graph, other);
      }
    }
  }

  private void visit(Graph.EdgeGraph<V, E> graph, V v) {
    if (v == null) {
      return;
    }
    marked[checkAndReturnIndex(v)] = true;
    for (E e : graph.adjacent(v)) {
      if (!isMarked(e.other(v))) {
        crossCutEdges.offer(e);
      }
    }
  }

  /**
   * 最小生成树的所有的边
   *
   * @return 最小生成树
   */
  public Iterable<E> edges() {
    return mst;
  }

  /**
   * 最小生成树的权重
   *
   * @return 最小生成树的权重
   */
  public double weight() {
    if (weight != null) {
      return weight;
    }
    weight = (double) 0;
    for (E e : mst) {
      weight += e.weight();
    }
    return weight;
  }
}