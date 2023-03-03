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
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import pers.jyb.graph.def.BaseEdge;
import pers.jyb.graph.def.BaseGraph;
import pers.jyb.graph.def.Digraph;
import pers.jyb.graph.def.EdgeOpGraph;
import pers.jyb.graph.def.VertexOpGraph;

/**
 * <p>单点路径问题在图的处理邻域中十分重要。{@code SourcePaths}的做法是使用一个数
 * 组<tt>edgeTo</tt>记录每个顶点到源顶点的路径。
 *
 * <p>但是需要注意如下两点：
 * <pre>
 * 一：{@code SourcePaths}不管源顶点是否含有自环，源顶点对于它自己总是可达的。
 *
 * 二：{@code SourcePaths}无法寻找源顶点的简单环路径，寻找有向图的简单环请查看{@link DirectedCycle}。
 * </pre>
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public abstract class SourcePaths<V> extends AccessMarked<V> {

  /**
   * 记录源顶点<tt>source</tt>到其他顶点的所有路径数
   */
  protected int count;

  /**
   * 源顶点
   */
  protected final V source;

  /**
   * 使用一个源顶点{@code source}初始化，返回的{@code SourcePaths}记录此顶点到 其他各顶点的连接情况。
   *
   * @param graph  图
   * @param source 源顶点
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   */
  SourcePaths(BaseGraph<V> graph, V source) {
    super(graph);
    if (source == null) {
      throw new NullPointerException();
    }
    checkAndReturnIndex(source);
    this.source = source;
  }

  /**
   * 是否存在从<tt>source</tt>到<tt>v</tt>的路径
   *
   * @param v 顶点
   * @return 是否存在路径
   */
  public boolean hasPathTo(V v) {
    return safeReturn(() -> isMarked(v), false);
  }

  /**
   * 与起点source连通的顶点个数
   *
   * @return 连通顶点数量
   */
  public int count() {
    return count;
  }

  /**
   * 挑选一个源顶点，以深度作为先决条件开始检索路径
   *
   * @param <V>    顶点类型
   * @param graph  需要检索的图
   * @param source 源顶点
   * @return 检索结果路径
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   */
  public static <V> SourcePath.VertexOp<V> depth(VertexOpGraph<V> graph, V source) {
    return new SourceVertexOpDfsPaths<>(graph, source);
  }

  /**
   * 挑选一个源顶点，以深度作为先决条件开始检索路径
   *
   * @param <V>    顶点类型
   * @param <E>    边类型
   * @param graph  需要检索的图
   * @param source 源顶点
   * @return 检索结果路径
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   */
  public static <V, E extends BaseEdge<V,E>> SourcePath.EdgeOp<V, E> depth(
      EdgeOpGraph<V, E> graph, V source) {
    return new SourceEdgeOpDfsPaths<>(graph, source);
  }

  /**
   * 挑选一个源顶点，以广度作为先决条件开始检索路径，这会获取到任意顶点到源顶点之间的最少边路径
   *
   * @param <V>    顶点类型
   * @param graph  需要检索的图
   * @param source 源顶点
   * @return 检索结果路径
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   */
  public static <V> SourcePath.VertexOp<V> breadth(
      VertexOpGraph<V> graph, V source) {
    return new SourceVertexBfsPaths<>(graph, source);
  }

  /**
   * 挑选一个源顶点，以广度作为先决条件开始检索路径，这会获取到任意顶点到源顶点之间的最少边路径
   *
   * @param <V>    顶点类型
   * @param <E>    边类型
   * @param graph  需要检索的图
   * @param source 源顶点
   * @return 检索结果路径
   * @throws NullPointerException   空图或者空顶点
   * @throws NoSuchElementException 源顶点不在图中
   */
  public static <V, E extends BaseEdge<V,E>> SourcePath.EdgeOp<V, E> breadth(
      EdgeOpGraph<V, E> graph, V source) {
    return new SourceEdgeOpBfsPaths<>(graph, source);
  }

  /**
   * 顶点图的源到其他顶点路径
   *
   * @param <V> 顶点类型
   */
  private static class SourceVertexOp<V> extends SourcePaths<V>
      implements SourcePath.VertexOp<V> {

    /**
     * 是否为有向图
     */
    protected boolean isDiGragh;

    /**
     * 使用一个源顶点{@code source}初始化，返回的{@code SourcePaths}记录此顶点到 其他各顶点的连接情况。
     *
     * @param graph  顶点图
     * @param source 源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     */
    SourceVertexOp(VertexOpGraph<V> graph, V source) {
      super(graph, source);
      isDiGragh = graph instanceof Digraph;
    }

    @SuppressWarnings("unchecked")
    private Stack<V> pathTo(V v) {
      return safeReturn(() -> {
        V w = v;
        int index, soureIndex = checkAndReturnIndex(source);
        Stack<V> stack = null;
        while ((w = (V) edgeTo[index = checkAndReturnIndex(w)]) != null
            && index != soureIndex) {
          if (stack == null) {
            stack = new Stack<>();
          }
          stack.push(w);
        }
        return stack;
      }, null);
    }

    /**
     * 如果存在，返回<tt>source</tt>到<tt>v</tt>的路径，否则返回null
     *
     * @param v 顶点
     * @return 路径，顺序及为数组索引
     */
    @SuppressWarnings("unchecked")
    public V[] path(V v) {
      return safeReturn(() -> {
        Stack<V> stack = pathTo(v);
        if (stack == null || stack.isEmpty()) {
          return null;
        }
        V[] paths = (V[]) Array.newInstance(stack.peek().getClass(), stack.size());
        for (int i = 0; i < paths.length; i++) {
          paths[i] = stack.pop();
        }
        return paths;
      }, null);
    }

    /**
     * 返回路径的打印字符串
     *
     * @param v 顶点
     * @return 路径
     */
    @Override
    @SuppressWarnings("unchecked")
    public String printPath(V v) {
      return safeReturn(() -> {
        StringBuilder sb = new StringBuilder()
            .append("source is ")
            .append(source)
            .append(";target is ")
            .append(v)
            .append("\n")
            .append("path: ");
        V w;
        String pathChar = isDiGragh ? " --> " : " -- ";
        Stack<V> stack = pathTo(v);
        if (stack == null) {
          return sb.toString();
        }
        stack = (Stack<V>) stack.clone();
        while (stack.size() > 0) {
          w = stack.pop();
          sb.append(w).append(pathChar);
        }
        sb.append(v);
        return sb.toString();
      }, null);
    }
  }

  /**
   * 深度优先遍历获取路径
   *
   * @param <V> 顶点类型
   */
  private static class SourceVertexOpDfsPaths<V> extends SourceVertexOp<V> {

    public SourceVertexOpDfsPaths(VertexOpGraph<V> graph, V source) {
      super(graph, source);
      dfs(graph, source);
    }

    private void dfs(VertexOpGraph<V> graph, V source) {
      int wIndex;
      marked[checkAndReturnIndex(source)] = true;
      for (V w : graph.adjacent(source)) {
        if (!marked[wIndex = checkAndReturnIndex(w)]) {
          edgeTo[wIndex] = source;
          dfs(graph, w);
          count++;
        }
      }
    }
  }

  /**
   * 广度优先遍历获取路径，目的是找到两个顶点之间得最少边的路径
   *
   * @param <V> 顶点类型
   */
  private static class SourceVertexBfsPaths<V> extends SourceVertexOp<V> {

    private Queue<V> queue;

    SourceVertexBfsPaths(VertexOpGraph<V> graph, V source) {
      super(graph, source);
      if (queue == null) {
        queue = new LinkedBlockingQueue<>();
      }
      queue.add(source);
      bfs(graph, source);
      marked[checkAndReturnIndex(source)] = true;
    }

    private void bfs(VertexOpGraph<V> graph, V source) {
      int index;
      for (V w : graph.adjacent(source)) {
        if (marked[index = checkAndReturnIndex(w)]) {
          continue;
        }
        queue.add(w);
        marked[index] = true;
        edgeTo[index] = source;
        count++;
      }
      if (!queue.isEmpty()) {
        V t = queue.poll();
        bfs(graph, t);
      }
    }
  }

  /**
   * 边图的源到其他顶点路径
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class SourceEdgeOp<V, E extends BaseEdge<V,E>>
      extends SourcePaths<V> implements SourcePath.EdgeOp<V, E> {

    /**
     * 使用一个源顶点{@code source}初始化，返回的{@code SourcePaths}记录此顶点到 其他各顶点的连接情况。
     *
     * @param graph  边图
     * @param source 源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     */
    SourceEdgeOp(EdgeOpGraph<V, E> graph, V source) {
      super(graph, source);
    }

    @SuppressWarnings("unchecked")
    private Stack<E> pathTo(V v) {
      return safeReturn(() -> {
        V w = v;
        int index, soureIndex = checkAndReturnIndex(source);
        Stack<E> stack = null;
        E edge;
        while ((edge = (E) edgeTo[index = checkAndReturnIndex(w)]) != null
            && index != soureIndex) {
          if (stack == null) {
            stack = new Stack<>();
          }
          stack.push(edge);
          w = edge.other(w);
        }
        return stack;
      }, null);
    }

    /**
     * 返回路径的打印字符串
     *
     * @param v 顶点
     * @return 路径
     */
    @Override
    @SuppressWarnings("unchecked")
    public String printPath(V v) {
      return safeReturn(() -> {
        StringBuilder sb = new StringBuilder()
            .append("source is ")
            .append(source)
            .append(";target is ")
            .append(v)
            .append("\n")
            .append("path: ");
        E edge;
        Stack<E> stack = pathTo(v);
        if (stack == null) {
          return sb.toString();
        }
        stack = (Stack<E>) stack.clone();
        while (stack.size() > 0) {
          edge = stack.pop();
          if (edge == null) {
            continue;
          }
          sb.append(edge).append(";");
        }
        return sb.toString();
      }, null);
    }

    /**
     * 如果存在，返回<tt>source</tt>到<tt>v</tt>的路径，否则返回null
     *
     * @param v 顶点
     * @return 路径
     */
    @Override
    public E[] path(V v) {
      return safeReturn(() -> {
        Stack<E> stack = pathTo(v);
        if (stack == null || stack.isEmpty()) {
          return null;
        }
        @SuppressWarnings("unchecked")
        E[] paths = (E[]) Array.newInstance(stack.peek().getClass(), stack.size());
        for (int i = 0; i < paths.length; i++) {
          paths[i] = stack.pop();
        }
        return paths;
      }, null);
    }
  }

  /**
   * 深度优先遍历获取路径
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class SourceEdgeOpDfsPaths<V, E extends BaseEdge<V,E>>
      extends SourceEdgeOp<V, E> {

    /**
     * 使用一个源顶点{@code source}初始化，返回的{@code SourcePaths}记录此顶点到 其他各顶点的连接情况。
     *
     * @param graph  边图
     * @param source 源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     */
    SourceEdgeOpDfsPaths(EdgeOpGraph<V, E> graph, V source) {
      super(graph, source);
      dfs(graph, source);
    }

    private void dfs(EdgeOpGraph<V, E> graph, V v) {
      int wIndex;
      marked[checkAndReturnIndex(v)] = true;
      for (E e : graph.adjacent(v)) {
        V w = e.other(v);
        if (!marked[wIndex = checkAndReturnIndex(w)]) {
          edgeTo[wIndex] = e;
          dfs(graph, w);
          count++;
        }
      }
    }
  }

  /**
   * 广度优先遍历获取路径，目的是找到两个顶点之间得最少边的路径
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class SourceEdgeOpBfsPaths<V, E extends BaseEdge<V,E>>
      extends SourceEdgeOp<V, E> {

    private Queue<V> queue;

    /**
     * 使用一个源顶点{@code source}初始化，返回的{@code SourcePaths}记录此顶点到 其他各顶点的连接情况。
     *
     * @param graph  边图
     * @param source 源顶点
     * @throws NullPointerException   空图或者空顶点
     * @throws NoSuchElementException 源顶点不在图中
     */
    SourceEdgeOpBfsPaths(EdgeOpGraph<V, E> graph, V source) {
      super(graph, source);
      if (queue == null) {
        queue = new LinkedBlockingQueue<>();
      }
      queue.add(source);
      bfs(graph, source);
      marked[checkAndReturnIndex(source)] = true;
    }

    private void bfs(EdgeOpGraph<V, E> graph, V v) {
      int index;
      for (E e : graph.adjacent(v)) {
        V w = e.other(v);
        if (marked[index = checkAndReturnIndex(w)]) {
          continue;
        }
        queue.add(w);
        marked[index] = true;
        edgeTo[index] = e;
        count++;
      }
      if (!queue.isEmpty()) {
        V t = queue.poll();
        bfs(graph, t);
      }
    }
  }
}