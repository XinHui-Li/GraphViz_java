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

import java.util.LinkedList;
import pers.jyb.graph.def.Digraph;
import pers.jyb.graph.def.DirectedEdge;

import java.lang.reflect.Array;

/**
 * 有向图的强连通
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public abstract class StrongConnectedComponent<V> extends AccessMarked<V> {

  /**
   * 强连通数量
   */
  protected int count;

  /**
   * 顶点的强连通记录
   */
  protected int[] connectedIds;

  public StrongConnectedComponent(Digraph<V> digraph) {
    super(digraph);
    connectedIds = new int[marked.length];
  }

  /**
   * 判断顶点v和顶点w是否强连通
   *
   * @param v 顶点
   * @param w 顶点
   * @return 两个顶点是否强连通
   */
  public boolean connected(V v, V w) {
    return safeReturn(() ->
            connectedIds[checkAndReturnIndex(v)]
                == connectedIds[checkAndReturnIndex(w)]
        , false);
  }

  /**
   * 返回顶点的连通分量ID
   *
   * @param v 顶点
   * @return 连通分量ID，如果不存在强连通分量ID，返回-1
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
   * 转换逆后序数组
   *
   * @param reservePost 顶点的逆后序排序
   * @return 顶点数组
   */
  protected V[] convert(LinkedList<V> reservePost) {
    @SuppressWarnings("unchecked")
    V[] order = (V[]) Array.newInstance(
        reservePost.peek().getClass(),
        reservePost.size()
    );
    int i = 0;
    while (reservePost.size() > 0) {
      order[i++] = reservePost.pop();
    }
    return order;
  }

  /**
   * 顶点有向图的强连通分量
   *
   * @param <V>     顶点类型
   * @param digraph 顶点有向图
   * @return 强连通分量
   */
  public static <V> StrongConnectedComponent<V> build(Digraph.VertexDigraph<V> digraph) {
    return new VertexOpSCC<>(digraph);
  }

  /**
   * 边有向图的强连通分量
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 边有向图
   * @return 强连通分量
   */
  public static <V, E extends DirectedEdge<V, E>> StrongConnectedComponent<V> build(
      Digraph.EdgeDigraph<V, E> digraph) {
    return new EdgeOpSCC<>(digraph);
  }

  /**
   * 顶点有向图的强连通分量
   *
   * @param <V> 顶点类型
   */
  private static class VertexOpSCC<V> extends StrongConnectedComponent<V> {

    public VertexOpSCC(Digraph.VertexDigraph<V> digraph) {
      super(digraph);
      DepthFirstOrder<V> topological = DepthFirstOrder.build(digraph);
      LinkedList<V> reservePost = topological.reservePost();
      if (reservePost.isEmpty()) {
        return;
      }
      V[] order = convert(reservePost);
      for (int i = order.length - 1; i > 0; i--) {
        if (!isMarked(order[i])) {
          count++;
          dfs(digraph, order[i]);
        }
      }
    }

    private void dfs(Digraph.VertexDigraph<V> digraph, V v) {
      int index;
      marked[index = checkAndReturnIndex(v)] = true;
      connectedIds[index] = count;
      for (V w : digraph.adjacent(v)) {
        if (!isMarked(w)) {
          dfs(digraph, w);
        }
      }
    }
  }

  /**
   * 边有向图的强连通分量
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeOpSCC<V, E extends DirectedEdge<V, E>>
      extends StrongConnectedComponent<V> {

    public EdgeOpSCC(Digraph.EdgeDigraph<V, E> digraph) {
      super(digraph);
      DepthFirstOrder<V> topological = DepthFirstOrder.build(digraph);
      LinkedList<V> reservePost = topological.reservePost();
      if (reservePost.isEmpty()) {
        return;
      }
      V[] order = convert(reservePost);
      for (int i = order.length - 1; i > 0; i--) {
        if (!isMarked(order[i])) {
          count++;
          dfs(digraph, order[i]);
        }
      }
    }

    private void dfs(Digraph.EdgeDigraph<V, E> digraph, V v) {
      int index;
      marked[index = checkAndReturnIndex(v)] = true;
      connectedIds[index] = count;
      for (E e : digraph.adjacent(v)) {
        V w;
        if (!isMarked(w = e.other(v))) {
          dfs(digraph, w);
        }
      }
    }
  }
}
