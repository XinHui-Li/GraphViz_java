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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import pers.jyb.graph.util.CollectionUtils;

/**
 * <p>在和有向图相关的实际应用中，有向环特别的重要。通常我们一般只会关注其中一小部
 * 分，或者只是想知道一副图中是否存在有向环，进行拓扑排序{@link Topological}的时 候这点需要格外的关注。
 *
 * <p>判断图中是否含有有向环，一种常用的方式就是使用深度优先遍历，并且维护一个当前
 * 正在访问的顶点栈。当访问边v-&gt;w的时候，如果在栈中已经表示已经访问过w顶点，表示图 中含有环，此时需要记录环路径。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public abstract class DirectedCycle<V> extends AccessMarked<V>
    implements GraphCycle<V> {

  /**
   * 标记路径中的顶点是否已经存在
   */
  protected boolean[] onStack;

  private DirectedCycle(Digraph<V> graph) {
    super(graph);
    onStack = new boolean[graph.vertexNum()];
  }

  /**
   * 顶点图的首环寻找
   *
   * @param <V>     顶点类型
   * @param digraph 有向图
   * @return 有向图发现的第一个简单环
   */
  public static <V> VertexOpGC<V> build(Digraph.VertexDigraph<V> digraph) {
    return new VertexOpDC<>(digraph);
  }

  /**
   * 边图首环寻找
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 有向图
   * @return 有向图发现的第一个简单环
   */
  public static <V, E extends DirectedEdge<V, E>> EdgeOpGC<V, E> build(
      Digraph.EdgeDigraph<V, E> digraph) {
    return new EdgeOpDC<>(digraph);
  }

  /**
   * 顶点图所有环寻找
   *
   * @param <V>     顶点类型
   * @param digraph 有向图
   * @return 有向图所有简单环
   */
  public static <V> GraphCycle.VertexOpAllGC<V> buildAll(Digraph.VertexDigraph<V> digraph) {
    return new VertexOpAllGC<>(digraph);
  }

  /**
   * 边图所有环寻找
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 有向图
   * @return 有向图所有简单环
   */
  public static <V, E extends DirectedEdge<V, E>> EdgeOpAllGC<V, E> buildAll(
      Digraph.EdgeDigraph<V, E> digraph) {
    return new EdgeOpAllDC<>(digraph);
  }

  /**
   * 顶点图的首环寻找
   *
   * @param <V> 顶点类型
   */
  private static class VertexOpDC<V> extends DirectedCycle<V>
      implements VertexOpGC<V> {

    /**
     * 存在的环路径，只能存储发现的第一条环
     */
    private Stack<V> cycle;

    VertexOpDC(Digraph.VertexDigraph<V> digraph) {
      super(digraph);
      for (V v : digraph) {
        if (!isMarked(v)) {
          dfs(digraph, v);
        }
      }
    }

    private void dfs(Digraph.VertexDigraph<V> digraph, V v) {
      int index, wIndex;
      // 记录当前访问的路径中含有当前顶点
      onStack[index = checkAndReturnIndex(v)] = true;
      marked[index] = true;
      for (V w : digraph.adjacent(v)) {
        if (hasCycle()) {
          return;
        } else if (!marked[wIndex = checkAndReturnIndex(w)]) {
          edgeTo[wIndex] = v;
          marked[wIndex] = true;
          dfs(digraph, w);
        }
        // 存在环，记录下环路径
        else if (onStack[wIndex]) {
          cycle = new Stack<>();
          int i = index;
          while (i != wIndex) {
            @SuppressWarnings("unchecked")
            V vertex = (V) edgeTo[i];
            cycle.push(vertex);
            i = checkAndReturnIndex(vertex);
          }
          cycle.push(v);
        }
      }
      // 路径检索完毕，移除路径的顶点记录
      onStack[index] = false;
    }

    /**
     * 是否含有环
     *
     * @return 图中是否存在环
     */
    @Override
    public boolean hasCycle() {
      return cycle != null;
    }

    /**
     * 如果图中存在环，返回发现的第一个环。向量图中的环的顺序等于{@link Stack#pop}的顺序
     *
     * @return 环路径
     */
    @Override
    public Stack<V> cycle() {
      return cycle;
    }

    @Override
    public String toString() {
      if (!hasCycle()) {
        return "No cycle";
      }
      StringBuilder sb = new StringBuilder()
          .append("Find cycle: ");
      @SuppressWarnings("unchecked")
      Stack<V> replCycle = (Stack<V>) cycle.clone();
      while (replCycle.size() > 0) {
        sb.append(replCycle.pop()).append(" --> ");
      }
      sb.append(cycle.peek());
      return sb.toString();
    }
  }

  /**
   * 边图首环寻找
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeOpDC<V, E extends DirectedEdge<V, E>>
      extends DirectedCycle<V> implements EdgeOpGC<V, E> {

    /**
     * 存在的环路径，只能存储发现的第一条环
     */
    private Stack<E> cycle;

    EdgeOpDC(Digraph.EdgeDigraph<V, E> digraph) {
      super(digraph);
      for (V v : digraph) {
        if (!isMarked(v)) {
          dfs(digraph, v);
        }
      }
    }

    private void dfs(Digraph.EdgeDigraph<V, E> digraph, V v) {
      int index, wIndex;
      // 记录当前访问的路径中含有当前顶点
      onStack[index = checkAndReturnIndex(v)] = true;
      marked[index] = true;
      for (E e : digraph.adjacent(v)) {
        V w = e.to();
        if (hasCycle()) {
          return;
        } else if (!marked[wIndex = checkAndReturnIndex(w)]) {
          edgeTo[wIndex] = e;
          marked[wIndex] = true;
          dfs(digraph, w);
        }
        // 存在环，记录下环路径
        else if (onStack[wIndex]) {
          cycle = new Stack<>();
          int i = index;
          while (i != wIndex) {
            @SuppressWarnings("unchecked")
            E edge = (E) edgeTo[i];
            cycle.push(edge);
            i = checkAndReturnIndex(edge.from());
          }
          cycle.push(e);
        }
      }
      // 路径检索完毕，移除路径的顶点记录
      onStack[index] = false;
    }

    @Override
    public boolean hasCycle() {
      return cycle != null;
    }

    @Override
    public Stack<E> cycle() {
      return cycle;
    }

    @Override
    public String toString() {
      if (!hasCycle()) {
        return "No cycle";
      }
      StringBuilder sb = new StringBuilder()
          .append("Find cycle: ")
          .append("\n");
      @SuppressWarnings("unchecked")
      Stack<E> replCycle = (Stack<E>) cycle.clone();
      while (replCycle.size() > 0) {
        sb.append(replCycle.pop())
            .append(" ");
      }
      return sb.toString();
    }
  }

  /**
   * 顶点图所有环寻找
   *
   * @param <V> 顶点类型
   */
  private static class VertexOpAllGC<V> extends DirectedCycle<V>
      implements GraphCycle.VertexOpAllGC<V> {

    // 图中所有环记录
    List<Stack<V>> cycles;

    // 当前深度遍历的源顶点
    V s;

    private VertexOpAllGC(Digraph.VertexDigraph<V> digraph) {
      super(digraph);
      for (V v : digraph) {
        s = v;
        if (!isMarked(v)) {
          dfs(digraph, v);
        }
        marked[checkAndReturnIndex(v)] = true;
      }
    }

    private void dfs(Digraph.VertexDigraph<V> digraph, V v) {
      int index, wIndex;
      // 简单路径的栈
      onStack[index = checkAndReturnIndex(v)] = true;
      for (V w : digraph.adjacent(v)) {
        wIndex = checkAndReturnIndex(w);
        boolean mark = isMarked(w);
        // 发现环，记录下路径
        if (w == s && !mark) {
          Stack<V> cycle = new Stack<>();
          int i = index;
          while (i != wIndex) {
            @SuppressWarnings("unchecked")
            V vertex = (V) edgeTo[i];
            cycle.push(vertex);
            i = checkAndReturnIndex(vertex);
          }
          cycle.push(v);
          if (cycles == null) {
            cycles = new ArrayList<>();
          }
          cycles.add(cycle);
        } else if (!onStack[wIndex] && !mark) { // 记录下简单路径
          edgeTo[wIndex] = v;
          dfs(digraph, w);
        }
      }
      // 路径检索完毕，移除路径的顶点记录
      onStack[index] = false;
    }

    @Override
    public boolean hasCycle() {
      return cycles != null;
    }

    @Override
    public List<Stack<V>> cycles() {
      return cycles;
    }

    @Override
    public String toString() {
      if (!hasCycle()) {
        return "No cycle";
      }
      StringBuilder sb = new StringBuilder()
          .append("Find cycles: ")
          .append("\n");
      for (Stack<V> cycle : cycles) {
        @SuppressWarnings("unchecked")
        Stack<V> replCycle = (Stack<V>) cycle.clone();
        while (replCycle.size() > 0) {
          sb.append(replCycle.pop()).append(" --> ");
        }
        sb.append(cycle.peek());
        sb.append("\n");
      }
      return sb.toString();
    }
  }

  /**
   * 边图所有环寻找
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeOpAllDC<V, E extends DirectedEdge<V, E>>
      extends DirectedCycle<V> implements EdgeOpAllGC<V, E> {

    // 图中所有环的记录
    List<Stack<E>> cycles;

    // 当前深度遍历的源顶点
    V s;

    private EdgeOpAllDC(Digraph.EdgeDigraph<V, E> digraph) {
      super(digraph);
      for (V v : digraph) {
        s = v;
        if (!isMarked(v)) {
          dfs(digraph, v);
        }
        marked[checkAndReturnIndex(v)] = true;
      }
    }

    private void dfs(Digraph.EdgeDigraph<V, E> digraph, V v) {
      int index, wIndex;
      // 简单路径的栈
      onStack[index = checkAndReturnIndex(v)] = true;
      for (E e : digraph.adjacent(v)) {
        V w = e.to();
        wIndex = checkAndReturnIndex(w);
        boolean mark = isMarked(w);
        // 发现环，记录下路径
        if (w == s && !mark) {
          Stack<E> cycle = new Stack<>();
          int i = index;
          while (i != wIndex) {
            @SuppressWarnings("unchecked")
            E edge = (E) edgeTo[i];
            cycle.push(edge);
            i = checkAndReturnIndex(edge.from());
          }
          cycle.push(e);
          if (cycles == null) {
            cycles = new ArrayList<>();
          }
          cycles.add(cycle);
        } else if (!onStack[wIndex] && !mark) { // 记录下简单路径
          edgeTo[wIndex] = e;
          dfs(digraph, w);
        }
      }
      // 路径检索完毕，移除路径的顶点记录
      onStack[index] = false;
    }

    @Override
    public boolean hasCycle() {
      return cycles != null;
    }

    @Override
    public List<Stack<E>> cycle() {
      return cycles;
    }

    @Override
    public String toString() {
      if (!hasCycle()) {
        return "No cycle";
      }
      StringBuilder sb = new StringBuilder()
          .append("Find cycles: ")
          .append("\n");
      for (Stack<E> cycle : cycles) {
        @SuppressWarnings("unchecked")
        Stack<E> replCycle = (Stack<E>) cycle.clone();
        while (CollectionUtils.isNotEmpty(replCycle)) {
          sb.append(replCycle.pop())
              .append(" ");
        }
        sb.append("\n");
      }
      return sb.toString();
    }
  }
}