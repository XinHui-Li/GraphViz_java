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

import pers.jyb.graph.def.Digraph;
import pers.jyb.graph.def.DirectedEdge;
import pers.jyb.graph.def.IllegalGraphException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pers.jyb.graph.util.CollectionUtils;

/**
 * <p>对于一个有向无环图来说<tt>G = (V,E)</tt>，其拓扑排序是G中所有顶点的一种线性
 * 次序，该次序满足如下条件：<br>
 * <i>如果图G包含边<tt>(u,v)</tt>，则顶点u在拓扑排序中处于顶点v的前面。</i>
 *
 * <p>对于拓扑排序来说，非常依赖一个前置条件：无环。假如说一个有向图代表的是某种任
 * 务调度的优先级，图指明了哪些任务必须在哪些任务之前完成。如果此时任务x必须在任务y 之前完成，而任务y必须在任务z之前完成，但任务z又必须在任务x之前完成，那么是无法对
 * 图中的顶点x,y,z进行拓扑排序的。
 *
 * <p>此外，基于拓扑排序，图的等级分配{@link #rank()}意味着，如果两个顶点分配到同
 * 一个等级，它们之间没有依赖关系，这也就代表两个顶点之间不存在路径。这通常在绘制图 的时候需要格外关注。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public abstract class Topological<V> extends AccessMarked<V> {

  // 顶点的拓扑排序
  protected V[] order;

  // 顶点等级分配结果
  protected int[] rv;

  // 顶点的等级分配
  protected Map<Integer, List<V>> rankVertex;

  protected Topological(Digraph<V> digraph) {
    super(digraph);
    rv = new int[marked.length];
    Arrays.fill(rv, 1);
  }

  /**
   * 返回拓扑排序后的顶点数组，排序顺序等于数组索引
   *
   * @return 排完序的顶点数组
   */
  public V[] order() {
    return order;
  }

  /**
   * 无环有向图顶点的等级分配。
   *
   * @return 等级分配结果
   */
  public Map<Integer, List<V>> rank() {
    return rankVertex;
  }

  /**
   * 返回顶点的等级
   *
   * @param v 顶点
   * @return 顶点的等级，如果是图中不存在的顶点，返回-1
   */
  public int getRank(V v) {
    return safeReturn(() -> rv[checkAndReturnIndex(v)], -1);
  }

  /**
   * 顶点有向图的拓扑排序和顶点等级分配
   *
   * @param <V>     顶点类型
   * @param digraph 顶点有向图
   * @return 拓扑排序对象
   * @throws NullPointerException  空图
   * @throws IllegalGraphException 图含有环
   */
  public static <V> Topological<V> build(Digraph.VertexDigraph<V> digraph) {
    return new VertexOpTL<>(digraph);
  }

  /**
   * 边有向图的拓扑排序和顶点等级分配
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 顶点有向图
   * @return 拓扑排序对象
   * @throws NullPointerException  空图
   * @throws IllegalGraphException 图含有环
   */
  public static <V, E extends DirectedEdge<V, E>> Topological<V> build(
      Digraph.EdgeDigraph<V, E> digraph) {
    return new EdgeOpTL<>(digraph);
  }

  /**
   * 顶点有向图的拓扑排序和顶点等级分配
   *
   * @param <V> 顶点类型
   */
  private static class VertexOpTL<V> extends Topological<V> {

    /**
     * 根据顶点有向图初始化
     *
     * @param digraph 顶点有向图
     * @throws IllegalGraphException 图含有环
     */
    @SuppressWarnings("unchecked")
    VertexOpTL(Digraph.VertexDigraph<V> digraph) {
      super(digraph);
      check(digraph);
      DepthFirstOrder<V> depthFirstOrder = DepthFirstOrder.build(digraph);
      LinkedList<V> reservePost = depthFirstOrder.reservePost();
      if (reservePost.isEmpty()) {
        return;
      }
      order = (V[]) Array.newInstance(
          reservePost.peek().getClass(),
          reservePost.size()
      );
      int i = 0;
      while (CollectionUtils.isNotEmpty(reservePost)) {
        order[i++] = reservePost.pop();
      }
      for (V value : order) {
        dfs(digraph, value);
      }
      rankVertex = Stream.of(digraph.toArray())
          .collect(Collectors.groupingBy(v -> rv[checkAndReturnIndex(v)]));
    }

    private void dfs(Digraph.VertexDigraph<V> digraph, V v) {
      int vi = checkAndReturnIndex(v), val = rv[vi] + 1, wi;
      for (V w : digraph.adjacent(v)) {
        if (val > rv[wi = checkAndReturnIndex(w)]) {
          rv[wi] = val;
        }
        dfs(digraph, w);
      }
    }

    // 检测图的合法性
    private void check(Digraph.VertexDigraph<V> digraph) {
      if (digraph.vertexNum() == 0) {
        throw new IllegalGraphException("Graph is empty");
      }
      GraphCycle<V> cycle = DirectedCycle.build(digraph);
      if (cycle.hasCycle()) {
        throw new IllegalGraphException(cycle.toString());
      }
    }
  }

  /**
   * 边有向图的拓扑排序和顶点等级分配
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeOpTL<V, E extends DirectedEdge<V, E>>
      extends Topological<V> {

    /**
     * 根据边有向图初始化
     *
     * @param digraph 边有向图
     * @throws IllegalGraphException 图含有环
     */
    @SuppressWarnings("unchecked")
    public EdgeOpTL(Digraph.EdgeDigraph<V, E> digraph) {
      super(digraph);
      check(digraph);
      DepthFirstOrder<V> depthFirstOrder = DepthFirstOrder.build(digraph);
      LinkedList<V> reservePost = depthFirstOrder.reservePost();
      if (reservePost.isEmpty()) {
        return;
      }
      order = (V[]) Array.newInstance(
          reservePost.peek().getClass(),
          reservePost.size()
      );
      int i = 0;
      while (CollectionUtils.isNotEmpty(reservePost)) {
        order[i++] = reservePost.pop();
      }
      for (V value : order) {
        dfs(digraph, value);
      }
      rankVertex = Stream.of(digraph.toArray())
          .collect(Collectors.groupingBy(v -> rv[checkAndReturnIndex(v)]));
    }

    private void dfs(Digraph.EdgeDigraph<V, E> digraph, V v) {
      int vi = checkAndReturnIndex(v), val = rv[vi] + 1, wi;
      for (E e : digraph.adjacent(v)) {
        V w;
        if (val > rv[wi = checkAndReturnIndex(w = e.to())]) {
          rv[wi] = val;
        }
        dfs(digraph, w);
      }
    }

    // 检测图的合法性
    private void check(Digraph.EdgeDigraph<V, E> digraph) {
      if (digraph.vertexNum() == 0) {
        throw new IllegalGraphException("Graph is empty");
      }
      GraphCycle<V> cycle = DirectedCycle.build(digraph);
      if (cycle.hasCycle()) {
        throw new IllegalGraphException(cycle.toString());
      }
    }
  }
}
