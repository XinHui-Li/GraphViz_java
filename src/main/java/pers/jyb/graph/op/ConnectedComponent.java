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

import pers.jyb.graph.def.BaseGraph;
import pers.jyb.graph.def.Digraph;
import pers.jyb.graph.def.DirectedEdge;
import pers.jyb.graph.def.Edge;
import pers.jyb.graph.def.Graph;
import pers.jyb.graph.def.UndirectedGraph;

/**
 * 图的连通分量
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public abstract class ConnectedComponent<V> extends AccessMarked<V> {

  /**
   * 记录连通分量的数量
   */
  protected int count;

  /**
   * 记录顶点所属的连通分量
   */
  protected int[] connectedIds;

  public ConnectedComponent(BaseGraph<V> graph) {
    super(graph);
    connectedIds = new int[marked.length];
  }

  /**
   * 判断顶点v和顶点w是否连通
   *
   * @param v 顶点
   * @param w 顶点
   * @return 两个顶点是否连通
   */
  public boolean connected(V v, V w) {
    return safeReturn(() -> {
      int index;
      return connectedIds[index = checkAndReturnIndex(v)]
          == connectedIds[index];
    }, false);
  }

  /**
   * 返回顶点的连通分量ID
   *
   * @param v 顶点
   * @return 连通分量ID，如果不存在连通分量ID，返回-1
   */
  public int connectedId(V v) {
    return safeReturn(
        () -> connectedIds[checkAndReturnIndex(v)],
        -1
    );
  }

  /**
   * 返回连通分量的数量
   *
   * @return 连通分量数量
   */
  public int count() {
    return count;
  }

  /**
   * 顶点无向图的连通分量
   *
   * @param <V>   顶点类型
   * @param graph 顶点无向图
   * @return 连通分量
   */
  public static <V> ConnectedComponent<V> build(Graph.VertexGraph<V> graph) {
    return new VertexGraphOpCC<>(graph);
  }

  /**
   * 顶点有向图的连通分量
   *
   * @param <V>     顶点类型
   * @param digraph 顶点有向图
   * @return 连通分量
   */
  public static <V> ConnectedComponent<V> build(Digraph.VertexDigraph<V> digraph) {
    return new VertexDigraphOpCC<>(digraph);
  }

  /**
   * 边无向图的连通分量
   *
   * @param <V>   顶点类型
   * @param <E>   边类型
   * @param graph 边无向图
   * @return 连通分量
   */
  public static <V, E extends Edge<V, E>> ConnectedComponent<V> build(
      Graph.EdgeGraph<V, E> graph) {
    return new EdgeGraphOpCC<>(graph);
  }

  /**
   * 边有向图的连通分量
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 边有向图
   * @return 连通分量
   */
  public static <V, E extends DirectedEdge<V, E>> ConnectedComponent<V> build(
      Digraph.EdgeDigraph<V, E> digraph) {
    return new EdgeDigraphOpCC<>(digraph);
  }

  /**
   * 顶点无向图的连通分量
   *
   * @param <V> 顶点类型
   */
  private static class VertexGraphOpCC<V> extends ConnectedComponent<V> {

    public VertexGraphOpCC(Graph.VertexGraph<V> graph) {
      super(graph);
      for (V v : graph) {
        if (!isMarked(v)) {
          count++;
          dfs(graph, v);
        }
      }
    }

    private void dfs(Graph.VertexGraph<V> graph, V v) {
      int index;
      marked[index = checkAndReturnIndex(v)] = true;
      connectedIds[index] = count;
      for (V w : graph.adjacent(v)) {
        if (!isMarked(w)) {
          dfs(graph, w);
        }
      }
    }
  }

  /**
   * 边无向图的连通分量
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeGraphOpCC<V, E extends Edge<V, E>> extends ConnectedComponent<V> {

    public EdgeGraphOpCC(Graph.EdgeGraph<V, E> graph) {
      super(graph);
      for (V v : graph) {
        if (!isMarked(v)) {
          count++;
          dfs(graph, v);
        }
      }
    }

    private void dfs(Graph.EdgeGraph<V, E> graph, V v) {
      int index;
      marked[index = checkAndReturnIndex(v)] = true;
      connectedIds[index] = count;
      for (E e : graph.adjacent(v)) {
        V w;
        if (!isMarked(w = e.other(v))) {
          dfs(graph, w);
        }
      }
    }
  }

  /**
   * 顶点有向图的连通分量
   *
   * @param <V> 顶点类型
   */
  private static class VertexDigraphOpCC<V> extends CCProxy<V> {

    public VertexDigraphOpCC(Digraph.VertexDigraph<V> digraph) {
      super(digraph);
    }

    @Override
    VertexGraphOpCC<V> init(Digraph<V> digraph) {
      Graph.VertexGraph<V> graph = new UndirectedGraph<>(digraph.toArray());
      Digraph.VertexDigraph<V> d = (Digraph.VertexDigraph<V>) digraph;
      for (V v : digraph) {
        for (V w : d.adjacent(v)) {
          graph.addEdge(v, w);
        }
      }
      return new VertexGraphOpCC<>(graph);
    }
  }

  /**
   * 边有向图的连通分量
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeDigraphOpCC<V, E extends DirectedEdge<V, E>> extends CCProxy<V> {

    public EdgeDigraphOpCC(Digraph.EdgeDigraph<V, E> digraph) {
      super(digraph);
    }

    @Override
    VertexGraphOpCC<V> init(Digraph<V> digraph) {
      Graph.VertexGraph<V> graph = new UndirectedGraph<>(digraph.toArray());
      Digraph.EdgeDigraph<V, E> d = (Digraph.EdgeDigraph<V, E>) digraph;
      for (V v : digraph) {
        for (E e : d.adjacent(v)) {
          graph.addEdge(v, e.other(v));
        }
      }
      return new VertexGraphOpCC<>(graph);
    }
  }

  /**
   * 代理连通分量
   *
   * @param <V> 顶点类型
   */
  private static abstract class CCProxy<V> extends ConnectedComponent<V> {

    private VertexGraphOpCC<V> vertexGraphOpCC;

    public CCProxy(Digraph<V> digraph) {
      super(digraph);
      vertexGraphOpCC = init(digraph);
    }

    /**
     * 初始化{@code VertexGraphOpCC}
     *
     * @param digraph 有向图
     * @return 顶点无向图连通分量
     * @throws NullPointerException 返回空的连通分量
     */
    abstract VertexGraphOpCC<V> init(Digraph<V> digraph);

    /**
     * 判断顶点v和顶点w是否连通
     *
     * @param v 顶点
     * @param w 顶点
     * @return 两个顶点是否连通
     */
    @Override
    public boolean connected(V v, V w) {
      return vertexGraphOpCC.connected(v, w);
    }

    /**
     * 返回顶点的连通分量ID
     *
     * @param v 顶点
     * @return 连通分量ID，如果不存在连通分量ID，返回-1
     */
    @Override
    public int connectedId(V v) {
      return vertexGraphOpCC.connectedId(v);
    }

    /**
     * 返回连通分量的数量
     *
     * @return 连通分量数量
     */
    @Override
    public int count() {
      return vertexGraphOpCC.count();
    }
  }
}