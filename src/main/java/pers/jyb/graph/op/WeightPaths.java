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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import pers.jyb.graph.def.Digraph;
import pers.jyb.graph.def.DirectedEdge;
import pers.jyb.graph.def.DirectedEdgeGraph;
import pers.jyb.graph.def.IllegalGraphException;

/**
 * <p>找到从一个顶点到达另一个顶点的成本最小或最大的路径。
 *
 * <p> <strong>顶点的松弛</strong><br>
 * 使用一个edgeTo记录到达顶点最短路径的上一条边，使用disTo记录顶点目前访问到的最小权重值。初始情况下， disTo默认到达所有顶点的权重值都为Double.MAX_VALUE，
 * 放松一个顶点就是循环顶点的所有边，如果对于此条边e = v -&gt; w来说，有disTo[w] &gt; disTo[v] +
 * e.weight()，则把这条更小的边记录到edgeTo[w]，把这个权 重值记录到disTo[w]。通过一直更新这个信息，最终会得到新的最短路径，直至等待访问的顶点队列为空。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 * @see DirectedEdge
 * @see DirectedEdgeGraph
 */
public class WeightPaths<V, E extends DirectedEdge<V, E>>
    extends AccessMarked<V> {

  /**
   * 表示权重值无穷大，两个顶点之间没有路径
   */
  private static final double NoEdge = Double.MAX_VALUE;

  /**
   * 记录顶点的最小或最大权重值
   */
  protected double[] disTo;

  /**
   * 通过图和顶点初始化
   *
   * @param digraph 边有向图
   * @param source  起始顶点
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   */
  private WeightPaths(Digraph.EdgeDigraph<V, E> digraph, V source) {
    super(digraph);
    disTo = new double[marked.length];
    int sourceIndex = checkAndReturnIndex(source);
    // 源顶点初始化为0，其他顶点初始化为NoEdge
    for (V v : digraph) {
      int i = checkAndReturnIndex(v);
      if (i == sourceIndex) {
        disTo[i] = 0;
      } else {
        disTo[i] = NoEdge;
      }
    }
  }

  /**
   * 源顶点到达顶点v的最小或最大权重值
   *
   * @param v 目标顶点
   * @return 路径的最小或最大权重值
   */
  public double disTo(V v) {
    return safeReturn(() -> disTo[checkAndReturnIndex(v)], NoEdge);
  }

  /**
   * 源顶点到顶点v的最短或最长路径
   *
   * @param v 目标顶点
   * @return 如果存在返回路径，否则返回null
   */
  @SuppressWarnings("unchecked")
  public E[] pathsTo(V v) {
    return safeReturn(() -> {
      int index;
      Stack<E> stack = new Stack<>();
      index = checkAndReturnIndex(v);
      Object e = edgeTo[index];
      if (e == null) {
        return null;
      }
      E edege = (E) e;
      while (disTo[index = checkAndReturnIndex(edege.from())] != 0) {
        stack.add(edege);
        edege = (E) edgeTo[index];
      }
      stack.add((E) edgeTo[checkAndReturnIndex(edege.to())]);
      if (!stack.isEmpty()) {
        E[] minPaths = (E[]) Array.newInstance(edege.getClass(), stack.size());
        index = 0;
        while (!stack.isEmpty()) {
          edege = stack.pop();
          minPaths[index++] = edege;
        }
        return minPaths;
      }
      return null;
    }, null);
  }

  /**
   * 图的最短路径，如果图中存在负权重的环，抛出{@code IllegalGraphException}
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 需要处理的有向图
   * @param source  源顶点
   * @return 权重路径对象
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   * @throws IllegalGraphException  有向图含有负权重的环
   */
  public static <V, E extends DirectedEdge<V, E>> WeightPaths<V, E> minPaths(
      Digraph.EdgeDigraph<V, E> digraph, V source
  ) {
    return new UniversalityShortestPaths<>(digraph, source);
  }

  /**
   * 处理无环有向图的最短路径问题，如果图中存在环，抛出{@code IllegalGraphException}
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 需要处理的有向图
   * @param source  源顶点
   * @return 权重路径对象
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   * @throws IllegalGraphException  图含有环
   */
  public static <V, E extends DirectedEdge<V, E>> WeightPaths<V, E> acyclicMinPaths(
      Digraph.EdgeDigraph<V, E> digraph, V source
  ) {
    return new AcyclicShortestPaths<>(digraph, source);
  }

  /**
   * 处理无环有向图的最长路径问题，如果图中存在环，抛出{@code IllegalGraphException}
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 需要处理的有向图
   * @param source  源顶点
   * @return 权重路径对象
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   * @throws IllegalGraphException  图含有环
   */
  public static <V, E extends DirectedEdge<V, E>> WeightPaths<V, E> acyclicMaxPaths(
      Digraph.EdgeDigraph<V, E> digraph, V source) {
    return new AcyclicLongestPaths<>(digraph, source);
  }

  /**
   * <p>Dijkstra算法变种实现的有向图的最短路径，支持含有环和负权重边的图。但是如果存在负权重的环
   * 的时候，会抛出{@code IllegalGraphException}异常
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class UniversalityShortestPaths<V, E extends DirectedEdge<V, E>>
      extends WeightPaths<V, E> {

    /**
     * 顶点访问的队列
     */
    private final Queue<V> queue;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 通过图和顶点初始化
     *
     * @param digraph 有向图
     * @param source  源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     * @throws IllegalGraphException  有向图含有负权重的环
     */
    @SuppressWarnings("unchecked")
    public UniversalityShortestPaths(Digraph.EdgeDigraph<V, E> digraph, V source) {
      super(digraph, source);

      queue = new LinkedBlockingQueue<>();

      queue.offer(source);
      while (!queue.isEmpty()) {
        V vertex = queue.poll();
        relax(digraph, vertex);
      }

      Arrays.fill(marked, false);

      for (V v : digraph) {
        int index = checkAndReturnIndex(v);
        marked[index] = true;
        if (v == source) {
          continue;
        }
        Digraph.EdgeDigraph<V, E> g = new DirectedEdgeGraph<>();
        E edge = (E) edgeTo[index];
        while (edge != null) {
          g.addEdge(edge);
          V w = edge.from();
          if (isMarked(w)) {
            break;
          }
          index = checkAndReturnIndex(w);
          marked[index] = true;
          edge = (E) edgeTo[index];
        }
        if (g.vertexNum() > 0 && findNegativeCycle(g)) {
          throw new IllegalGraphException(error);
        }
      }
    }

    /**
     * 顶点松弛
     *
     * @param digraph 图
     * @param v       需要放松的顶点
     */
    private void relax(Digraph.EdgeDigraph<V, E> digraph, V v) {
      for (E e : digraph.adjacent(v)) {
        int to;
        double weight;
        V w = e.to();
        if (disTo[to = checkAndReturnIndex(w)] == NoEdge) {
          queue.offer(w);
        }
        if (disTo[to] > (weight = disTo[checkAndReturnIndex(v)] + e.weight())
        ) {
          disTo[to] = weight;
          edgeTo[to] = e;
        }
      }
    }

    /**
     * 寻找图中是否含有负权重环
     *
     * @param graph 有向图
     * @return 是否含有环
     */
    private boolean findNegativeCycle(Digraph.EdgeDigraph<V, E> graph) {
      GraphCycle.EdgeOpGC<V, E> directedCycle = DirectedCycle.build(graph);
      error = directedCycle.toString().replace("cycle", "negative cycle");
      return directedCycle.hasCycle();
    }
  }

  /**
   * <p>处理无环有向图的最短路径问题
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class AcyclicShortestPaths<V, E extends DirectedEdge<V, E>>
      extends WeightPaths<V, E> {

    /**
     * 通过图和顶点初始化
     *
     * @param digraph 有向图
     * @param source  源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     * @throws IllegalGraphException  图含有环
     */
    public AcyclicShortestPaths(Digraph.EdgeDigraph<V, E> digraph, V source) {
      super(digraph, source);
      Topological<V> topological = Topological.build(digraph);

      V[] vertexs = topological.order();

      for (V v : vertexs) {
        relax(digraph, v);
      }
    }

    /**
     * 顶点松弛
     *
     * @param digraph 图
     * @param v       需要放松的顶点
     */
    private void relax(Digraph.EdgeDigraph<V, E> digraph, V v) {
      for (E e : digraph.adjacent(v)) {
        double weight;
        V w = e.to();
        int to = checkAndReturnIndex(w);
        if (disTo[to] > (weight = disTo[checkAndReturnIndex(v)] + e.weight())
        ) {
          disTo[to] = weight;
          edgeTo[to] = e;
        }
      }
    }
  }

  /**
   * <p>处理无环有向图的最长路径问题
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class AcyclicLongestPaths<V, E extends DirectedEdge<V, E>>
      extends WeightPaths<V, E> {

    /**
     * 通过图和顶点初始化
     *
     * @param digraph 有向图
     * @param source  源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     * @throws IllegalGraphException  图含有环
     */
    public AcyclicLongestPaths(Digraph.EdgeDigraph<V, E> digraph, V source) {
      super(digraph, source);
      Topological<V> topological = Topological.build(digraph);

      V[] vertexs = topological.order();

      for (V v : vertexs) {
        tension(digraph, v);
      }
    }

    /**
     * 顶点缩紧
     *
     * @param digraph 图
     * @param v       需要缩紧的顶点
     */
    private void tension(Digraph.EdgeDigraph<V, E> digraph, V v) {
      for (E e : digraph.adjacent(v)) {
        double weight;
        V w = e.to();
        int to = checkAndReturnIndex(w);
        if (disTo[to] > (weight = disTo[checkAndReturnIndex(v)] - e.weight())
        ) {
          disTo[to] = weight;
          edgeTo[to] = e;
        }
      }
    }

    @Override
    public double disTo(V v) {
      return safeReturn(() -> -disTo[checkAndReturnIndex(v)], NoEdge);
    }
  }
}
