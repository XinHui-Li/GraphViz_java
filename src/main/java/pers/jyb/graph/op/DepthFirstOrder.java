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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>在基于深度优先搜索的有向图的排序当中，人们通常对以下三种排列顺序感兴趣：
 * <pre>
 *     * 前序：在递归调用之前将顶点加入队列
 *     * 后续：在递归调用之后将顶点键入队列
 *     * 逆后序：在递归调用之后将顶点压入栈
 * </pre>
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
class DepthFirstOrder<V> extends AccessMarked<V> {

  /**
   * 前序队列
   */
  protected Queue<V> pre;

  /**
   * 后序队列
   */
  protected Queue<V> post;

  /**
   * 逆后序栈
   */
  protected LinkedList<V> reservePost;

  private DepthFirstOrder(Digraph<V> graph) {
    super(graph);
    pre = new LinkedBlockingQueue<>(graph.vertexNum());
    post = new LinkedBlockingQueue<>(graph.vertexNum());
    reservePost = new LinkedList<>();
  }

  /**
   * 前序排列
   *
   * @return 前序排列
   */
  Queue<V> pre() {
    return pre;
  }

  /**
   * 后序排列
   *
   * @return 后序排列
   */
  Queue<V> post() {
    return post;
  }

  /**
   * 逆后序排列
   *
   * @return 逆后序排列
   */
  LinkedList<V> reservePost() {
    return reservePost;
  }

  /**
   * 根据顶点操作图{@code Digraph.VertexDigraph<V>}进行前，后，逆排序
   *
   * @param <V>     顶点类型
   * @param digraph 顶点有向图
   * @return 深度优先排序对象
   */
  public static <V> DepthFirstOrder<V> build(Digraph.VertexDigraph<V> digraph) {
    return new VertexOpDFO<>(digraph);
  }

  /**
   * 根据顶点操作图{@code Digraph.EdgeDigraph<V,E>}进行前，后，逆排序
   *
   * @param <V>     顶点类型
   * @param <E>     边类型
   * @param digraph 顶点有向图
   * @return 深度优先排序对象
   */
  public static <V, E extends DirectedEdge<V, E>> DepthFirstOrder<V> build(
      Digraph.EdgeDigraph<V, E> digraph) {
    return new EdgeOpDFO<>(digraph);
  }

  /**
   * 顶点操作图的前，后，逆排序
   *
   * @param <V> 顶点类型
   */
  private static class VertexOpDFO<V> extends DepthFirstOrder<V> {

    VertexOpDFO(Digraph.VertexDigraph<V> digraph) {
      super(digraph);
      for (V v : digraph) {
        if (!isMarked(v)) {
          dfs(digraph, v);
        }
      }
    }

    private void dfs(Digraph.VertexDigraph<V> digraph, V v) {
      pre.add(v);
      marked[checkAndReturnIndex(v)] = true;
      for (V w : digraph.adjacent(v)) {
        if (!isMarked(w)) {
          dfs(digraph, w);
        }
      }
      post.add(v);
      reservePost.push(v);
    }
  }

  /**
   * 边操作图的前，后，逆排序
   *
   * @param <V> 顶点类型
   * @param <E> 边
   */
  private static class EdgeOpDFO<V, E extends DirectedEdge<V, E>>
      extends DepthFirstOrder<V> {

    EdgeOpDFO(Digraph.EdgeDigraph<V, E> digraph) {
      super(digraph);
      for (V v : digraph) {
        if (!isMarked(v)) {
          dfs(digraph, v);
        }
      }
    }

    private void dfs(Digraph.EdgeDigraph<V, E> digraph, V v) {
      pre.add(v);
      marked[checkAndReturnIndex(v)] = true;
      for (E e : digraph.adjacent(v)) {
        V w = e.to();
        if (!isMarked(w)) {
          dfs(digraph, w);
        }
      }
      post.add(v);
      reservePost.push(v);
    }
  }
}