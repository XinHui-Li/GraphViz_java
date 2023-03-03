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

package pers.jyb.graph.def;

import java.util.Collections;
import java.util.Objects;

/**
 * 顶点操作的双向有向图。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public class DedirectedGraph<V> extends ProxyDedigraph<V, DirectedGraph<V>, DirectedGraph<V>>
    implements VertexDedigraph<V> {

  private static final long serialVersionUID = -7910958796521952954L;

  public DedirectedGraph() {
    this(new DirectedGraph<>(), new DirectedGraph<>());
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  public DedirectedGraph(int capacity) {
    this(new DirectedGraph<>(capacity), new DirectedGraph<>(capacity));
  }

  /**
   * 使用顶点数组初始化。
   *
   * @param vertices 顶点数组
   * @throws IllegalArgumentException 顶点数组为空
   */
  public DedirectedGraph(V[] vertices) {
    this(new DirectedGraph<>(vertices), new DirectedGraph<>(vertices));
  }

  private DedirectedGraph(DirectedGraph<V> digraph, DirectedGraph<V> reDigraph) {
    super(digraph, reDigraph);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<V> inIte(Object v) {
    return reDigraph.adjacent(v);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<V> outIte(Object v) {
    return digraph.adjacent(v);
  }

  @Override
  public int selfLoops(V v) {
    return digraph.selfLoops(v);
  }

  @Override
  public DedirectedGraph<V> copy() {
    return new DedirectedGraph<>(digraph.copy(), reDigraph.copy());
  }

  @Override
  public DedirectedGraph<V> reverse() {
    return new DedirectedGraph<>(digraph.reverse(), reDigraph.reverse());
  }

  @Override
  public void addEdge(V v, V w) {
    if (Objects.isNull(v) || Objects.isNull(w)) {
      throw new NullPointerException();
    }

    digraph.addEdge(v, w);
    reDigraph.addEdge(w, v);
  }

  @Override
  public boolean removeEdge(Object v, Object w) {
    return digraph.removeEdge(v, w) && reDigraph.removeEdge(w, v);
  }

  @Override
  public Iterable<V> adjacent(Object v) {
    return new BiConcatIterable<>(outAdjacent(v), inAdjacent(v));
  }

  @Override
  public Iterable<V> inAdjacent(Object v) {
    return new BiConcatIterable<>(reDigraph.adjacent(v), Collections.emptyList());
  }

  @Override
  public Iterable<V> outAdjacent(Object v) {
    return new BiConcatIterable<>(digraph.adjacent(v), Collections.emptyList());
  }
}
